package de.mat3.badintent.hooking.proxy.hooks;


import android.os.Parcel;

import de.mat3.badintent.hooking.proxy.ParcelAgent;
import de.robv.android.xposed.XC_MethodHook;

/**
 * Base XC_MethodHook for a type-specific Parcel writing-transaction.
 */
public abstract class ParcelBaseWriteHook extends XC_MethodHook {

    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        if (!isParcelAgentCaller()) {
            interruptWrite((Parcel) param.thisObject, param.args);
        }
    }

    /**
     * This method is needed to handle the type-dependent interrupt.
     *
     * @param parcel
     * @param args
     */
    protected abstract void interruptWrite(Parcel parcel, Object args[]);

    /**
     * This method checks if the ParcelAgent is part of the current stack trace
     *
     * @return true iff ParcelAgent is in stack trace
     */
    private boolean isParcelAgentCaller() {
        for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
            if (stackTraceElement.getClassName().equals(ParcelAgent.class.getName())) {
                return true;
            }
        }
        return false;
    }

}
