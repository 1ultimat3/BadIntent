package de.mat3.badintent.hooking.proxy.hooks;

import android.util.Log;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import de.mat3.badintent.app.BadIntentConstants;
import de.mat3.badintent.hooking.BaseHook;
import de.mat3.badintent.hooking.proxy.AppInformation;
import de.mat3.badintent.hooking.proxy.dao.SerializationUtils;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;



/**
 * This hook sends all log data of this app via Burp to the local log REST resource.
 */
public class LogHooks extends BaseHook {

    protected XSharedPreferences prefs;
    protected int port;

    public LogHooks(BaseHook h, XSharedPreferences sPrefs, int port) {
        super(h);
        prefs = sPrefs;
        this.port = port;

    }

    private class XposedAndroidLogMethodHook extends XC_MethodHook {
        protected String logType;

        public XposedAndroidLogMethodHook(String logType) {
            this.logType = logType;
        }

        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            try {
                prefs.reload();
                if (prefs.getBoolean(BadIntentConstants.CAPTURE_LOG, false) && AppInformation.Instance.intercept()) {
                    String tag = (String) param.args[0];
                    String log = (String) param.args[1];
                    URL url = ConnectionUtils.getBadIntentURL("/log", prefs, port);
                    HttpURLConnection conn = ConnectionUtils.getBadIntentHttpURLConnection(url, prefs);

                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("__BadIntent__", "Log");
                    conn.setRequestProperty("__BadIntent__.package", lpparam.packageName);
                    conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setDoOutput(true);
                    conn.connect();

                    DataOutputStream writer = new DataOutputStream(conn.getOutputStream());

                    Map<String, String> data = new HashMap<>();
                    data.put("tag", tag);
                    data.put("log", log);
                    data.put("logType", logType);
                    writer.writeBytes(SerializationUtils.createGson().toJson(data));
                    writer.flush();
                    writer.close();

                    ConnectionUtils.readResponseAndCloseConnection(conn).start();
                }
            } catch (Exception e) {/* best effort */}
        }
    }

    public void hookLogs(){
        XposedBridge.hookAllMethods(Log.class, "d", new XposedAndroidLogMethodHook("debug"));
        XposedBridge.hookAllMethods(Log.class, "v", new XposedAndroidLogMethodHook("verbose"));
        XposedBridge.hookAllMethods(Log.class, "i", new XposedAndroidLogMethodHook("info"));
        XposedBridge.hookAllMethods(Log.class, "w", new XposedAndroidLogMethodHook("warning"));
        XposedBridge.hookAllMethods(Log.class, "e", new XposedAndroidLogMethodHook("exception"));
        XposedBridge.hookAllMethods(Log.class, "wtf", new XposedAndroidLogMethodHook("wtf"));
    }


}
