package de.mat3.badintent.utils;

import de.robv.android.xposed.XC_MethodHook;


public class LoggingMetaData {

    public String classname;
    public String methodHookId = "NONE";
    public boolean finished = false;
    public XC_MethodHook.MethodHookParam param;

    protected static Long counter = new Long(1);

    public LoggingMetaData(String classname) {
        this.classname = classname;
    }

    public void beforeHookedMethod(XC_MethodHook.MethodHookParam param) {
        methodHookId = Long.toString(counter++) + ":" + param.hashCode();
        this.param = param;
    }

    public void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
        finished = true;
        this.param = param;
    }


}
