package de.mat3.badintent.hooking.proxy.hooks;

import android.os.IBinder;
import android.os.Parcel;
import android.os.StrictMode;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;

import de.mat3.badintent.hooking.BaseHook;
import de.mat3.badintent.hooking.proxy.AppInformation;
import de.mat3.badintent.hooking.proxy.ParcelAgent;
import de.mat3.badintent.hooking.proxy.ParcelContainer;
import de.mat3.badintent.hooking.proxy.dao.SerializationUtils;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;



/**
 * Various Binder related hooks are set in this class, which are required to redirect Parcels before the actual transaction occurs.
 */
public class TransactionHooks extends BaseHook {

    protected XSharedPreferences sPrefs;
    protected int port;

    protected static final String TAG = "BadIntentTransactions";

    /**
     * @param h
     * @param sharedPreferences which is used to determine the proxy settings (BadIntent HTTP-Proxy)
     * @param port on which the current app's BadIntent RestAPI is listening
     */
    public TransactionHooks(BaseHook h, XSharedPreferences sharedPreferences, int port) {
        super(h);
        sPrefs = sharedPreferences;
        AppInformation.Instance.sPrefs = sPrefs;
        this.port = port;
    }

    /**
     * After completion of the Parcel creation, the relevant Binder transactions are changed in order to
     * use the separate user-modified Parcel.
     *
     * This BinderProxy hook is implemented as a co-routine to @RestAPI.
     *
     * BinderProxy Hook             RestAPI
     *    |                             |           State Parcel Container = New
     *    |---beforeTransact----------->| HTTP      State Parcel Container = Sent
     *    |                             | (Possible HTTP changes)
     *    |<--updates Parcel Container--|           State Parcel Container = Received
     *    |                             |
     *    | execTransaction             |
     *    |---afterTransact------------>|           State Parcel Container = Reply
     *    |<----------------------------| Writes HTTP Response [possibly marshalled reply]
     *    |                             |           Setting DONE, clearing Container
     */
    public void hookBinder() {
        try {
            Log.d(TAG, "hooking android.os.BinderProxy");
            XposedHelpers.findAndHookMethod("android.os.BinderProxy", cloader, "transact", int.class, Parcel.class, Parcel.class, int.class, new XC_MethodHook() {

                //main identification of states and actions
                ThreadLocal<Parcel> originalParcel = new ThreadLocal<Parcel>();

                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    //wait here for my Parcel to be received by REST API
                    int code = (int) param.args[0];
                    Parcel data = (Parcel) param.args[1];
                    Parcel reply = (Parcel) param.args[2];
                    int flags = (int) param.args[3];

                    //set parcel in thread local storage
                    originalParcel.set(data);

                    //Log.d(TAG, "+++++++++++++++++++++\nCaller: " + param.thisObject.toString());
                    // Log.d(TAG, "PackageName: " + lpparam.packageName + " "));
                    ParcelContainer container = ParcelAgent.Instance.getParcelContainer(data);

                    ParcelAgent.Instance.waitForState(container, ParcelContainer.State.NEW, ParcelContainer.State.DONE);

                    ParcelAgent.Instance.addBinder((IBinder) param.thisObject, container);

                    //checking if parcel is defined to be intercepted
                    if (!AppInformation.Instance.intercept(data)) {
                        ParcelAgent.Instance.updateState(container, ParcelContainer.State.BYPASS);
                        return;
                    } else {
                        //send parcel via HTTP
                        ParcelAgent.Instance.updateState(container, ParcelContainer.State.SENT);
                        Thread waitingThread;
                        try {
                            waitingThread = sendParcel(container, code, flags);
                            waitingThread.start();

                            //wait until the parcel values are received via HTTP
                            ParcelAgent.Instance.waitForState(container, ParcelContainer.State.RECEIVED, ParcelContainer.State.ERROR);
                            if(container.getState() == ParcelContainer.State.ERROR) {
                                Log.e(TAG, "setting back state to ERROR and omitting further intercept process");
                                return;
                            }
                            Parcel newData = ParcelAgent.Instance.createNewParcelBaseOnContainer(container);
                            //Log.d(TAG, "adding replacer for parcelID: " + container.getParcelID());
                            param.args[1] = newData;
                            //materialize original parcel, because the parcel can be used after the transaction
                            ParcelAgent.Instance.materialize(container, data);
                        } catch (IOException e) {
                            //catch exceptions during sendParcel; set state to ERROR -> write directly to binder
                            Log.e(TAG, e.getLocalizedMessage());
                            ParcelAgent.Instance.updateState(container, ParcelContainer.State.ERROR);
                        }
                    }
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Parcel data = (Parcel) param.args[1];
                    Parcel reply = (Parcel) param.args[2];

                    ParcelContainer originalContainer = ParcelAgent.Instance.getParcelContainer(originalParcel.get());
                    ParcelContainer.State directState = originalContainer.getState();

                    if (directState == ParcelContainer.State.BYPASS || directState == ParcelContainer.State.ERROR) {
                        //Do nothing
                    } else {
                        //assume intercepted workflow
                        originalContainer.setReply(reply);
                        originalContainer.setState(ParcelContainer.State.REPLY);
                        ParcelAgent.Instance.waitForState(originalContainer, ParcelContainer.State.DONE, ParcelContainer.State.ERROR);
                        data.recycle();
                    }

                    //finish and clear transaction
                    originalContainer.setState(ParcelContainer.State.DONE);
                    // Log.d(TAG, "clearing Operations " + originalContainer.getParcelID());
                    originalContainer.getOperationList().clear();
                    // Log.d(TAG, "-----------------------------------");
                }
            });
        } catch (RuntimeException e) {
            Log.e(TAG, e.getLocalizedMessage());
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage());
        }

    }

    protected Thread sendParcel(ParcelContainer container, int code, int flags) throws IOException {
        //Prevent from casting exceptions with respect to "Network Access on Main Thread"
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        String resource = "/parcel/" + container.getParcelID();

        URL url = ConnectionUtils.getBadIntentURL(resource, sPrefs, port);
        String jsonData = SerializationUtils.getJson(container, code, flags);

        final HttpURLConnection conn = ConnectionUtils.getBadIntentHttpURLConnection(url, sPrefs);

        conn.setRequestMethod("POST");
        conn.setRequestProperty("__BadIntent__", "Parcel");
        conn.setRequestProperty("__BadIntent__.package", lpparam.packageName);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);
        conn.connect();

        DataOutputStream writer = new DataOutputStream(conn.getOutputStream());

        writer.writeBytes(jsonData);
        writer.flush();
        writer.close();

        //create new thread, which reads HTTP result
        return ConnectionUtils.readResponseAndCloseConnection(conn);


    }

}
