package de.mat3.badintent.hooking.proxy.hooks;

import android.os.IBinder;
import android.os.Parcel;
import android.util.Log;

import java.io.FileDescriptor;
import java.io.IOException;

import de.mat3.badintent.hooking.BaseHook;
import de.mat3.badintent.hooking.proxy.ParcelAgent;
import de.mat3.badintent.hooking.proxy.RestAPI;
import de.robv.android.xposed.XposedHelpers;


/**
 * Interrupts and stores (native invoking) parcel operations in a custom container.
 */
public class ParcelProxyHooks extends BaseHook {

    protected RestAPI rest;
    protected int port;

    protected static final String TAG = "BadIntentParcelProxy";

    public ParcelProxyHooks(BaseHook h, int port) throws IOException {
        super(h);
        this.port = port;
        startBackgroundThreads();
    }

    protected void startBackgroundThreads() throws IOException {
        Log.i(TAG, "starting internal WebServer on port: " + port);
        rest = new RestAPI("0.0.0.0", port);
        rest.start();
    }

    public void hookParcel() {
        try {
            XposedHelpers.findAndHookMethod("android.os.Parcel", cloader, "writeString", String.class, new ParcelBaseWriteHook() {
                @Override
                protected void interruptWrite(Parcel parcel, Object[] args) {
                    ParcelAgent.Instance.interruptString(parcel, (String) args[0]);
                }
            });
            XposedHelpers.findAndHookMethod("android.os.Parcel", cloader, "writeInt", int.class, new ParcelBaseWriteHook() {
                @Override
                protected void interruptWrite(Parcel parcel, Object[] args) {
                    ParcelAgent.Instance.interruptInteger(parcel, (Integer) args[0]);
                }
            });
            XposedHelpers.findAndHookMethod("android.os.Parcel", cloader, "writeLong", long.class, new ParcelBaseWriteHook() {
                @Override
                protected void interruptWrite(Parcel parcel, Object[] args) {
                    ParcelAgent.Instance.interruptLong(parcel, (Long) args[0]);
                }
            });
            XposedHelpers.findAndHookMethod("android.os.Parcel", cloader, "writeFloat", float.class, new ParcelBaseWriteHook() {
                @Override
                protected void interruptWrite(Parcel parcel, Object[] args) {
                    ParcelAgent.Instance.interruptFloat(parcel, (Float) args[0]);
                }
            });
            XposedHelpers.findAndHookMethod("android.os.Parcel", cloader, "writeDouble", double.class, new ParcelBaseWriteHook() {
                @Override
                protected void interruptWrite(Parcel parcel, Object[] args) {
                    ParcelAgent.Instance.interruptDouble(parcel, (Double) args[0]);
                }
            });
            XposedHelpers.findAndHookMethod("android.os.Parcel", cloader, "writeByteArray", byte[].class, int.class, int.class, new ParcelBaseWriteHook() {
                @Override
                protected void interruptWrite(Parcel parcel, Object[] args) {
                    ParcelAgent.Instance.interruptByteArray(parcel, (byte[]) args[0], (int) args[1], (int) args[2]);
                }
            });
            XposedHelpers.findAndHookMethod("android.os.Parcel", cloader, "writeFileDescriptor", FileDescriptor.class, new ParcelBaseWriteHook() {
                @Override
                protected void interruptWrite(Parcel parcel, Object[] args) {
                    ParcelAgent.Instance.interruptFileDescriptor(parcel, (FileDescriptor) args[0]);
                }
            });
            XposedHelpers.findAndHookMethod("android.os.Parcel", cloader, "writeStrongBinder", IBinder.class, new ParcelBaseWriteHook() {
                @Override
                protected void interruptWrite(Parcel parcel, Object[] args) {
                    ParcelAgent.Instance.interruptStrongBinder(parcel, (IBinder) args[0]);
                }
            });
            XposedHelpers.findAndHookMethod("android.os.Parcel", cloader, "writeInterfaceToken", String.class, new ParcelBaseWriteHook() {
                @Override
                protected void interruptWrite(Parcel parcel, Object[] args) {
                    ParcelAgent.Instance.interruptInterfaceToken(parcel, (String) args[0]);
                }
            });
            XposedHelpers.findAndHookMethod("android.os.Parcel", cloader, "setDataPosition", int.class, new ParcelBaseWriteHook() {
                @Override
                protected void interruptWrite(Parcel parcel, Object[] args) {
                    ParcelAgent.Instance.interruptDataPosition(parcel, (int) args[0]);
                }
            });
            try {
                XposedHelpers.findAndHookMethod("android.os.Parcel", cloader, "writeBlob", byte[].class, int.class, int.class, new ParcelBaseWriteHook() {
                    @Override
                    protected void interruptWrite(Parcel parcel, Object[] args) {
                        ParcelAgent.Instance.interruptBlob(parcel, (byte[]) args[0], (int) args[1], (int) args[2]);
                        }
            });
            } catch (NoSuchMethodError e) { /* method does not exist in Android 4.4 */
                Log.e(TAG, e.getLocalizedMessage());
            }

            XposedHelpers.findAndHookMethod("android.os.Parcel", cloader, "setDataCapacity", int.class, new ParcelBaseWriteHook() {
                @Override
                protected void interruptWrite(Parcel parcel, Object[] args) {
                    ParcelAgent.Instance.interruptDataCapacity(parcel, (int) args[0]);
                }
            });

            XposedHelpers.findAndHookMethod("android.os.Parcel", cloader, "setDataSize", int.class, new ParcelBaseWriteHook() {
                @Override
                protected void interruptWrite(Parcel parcel, Object[] args) {
                    ParcelAgent.Instance.interruptDataSize(parcel, (int) args[0]);
                }
            });

            XposedHelpers.findAndHookMethod("android.os.Parcel", cloader, "pushAllowFds", boolean.class, new ParcelBaseWriteHook() {
                @Override
                protected void interruptWrite(Parcel parcel, Object[] args) {
                    try {
                        ParcelAgent.Instance.interruptPushAllowFds(parcel, (boolean) args[0]);
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                }
            });

            XposedHelpers.findAndHookMethod("android.os.Parcel", cloader, "appendFrom", Parcel.class, int.class, int.class, new ParcelBaseWriteHook() {
                @Override
                protected void interruptWrite(Parcel parcel, Object[] args) {
                    ParcelAgent.Instance.interruptAppendFrom(parcel, (Parcel) args[0], (int) args[1], (int) args[2]);
                }
            });

            try {
                XposedHelpers.findAndHookMethod("android.os.Parcel", cloader, "writeRawFileDescriptor", FileDescriptor.class, new ParcelBaseWriteHook() {
                    @Override
                    protected void interruptWrite(Parcel parcel, Object[] args) {
                        ParcelAgent.Instance.interruptFileDescriptor(parcel, (FileDescriptor) args[0]);
                    }
                });
            } catch (NoSuchMethodError e) { /* obsolete method writeRawFileDescriptor */
                Log.e(TAG, e.getLocalizedMessage());
            }
        } catch (RuntimeException e) {
            Log.e(TAG, e.getLocalizedMessage());
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage());
        }
    }
}
