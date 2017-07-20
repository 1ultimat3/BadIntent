package de.mat3.badintent.utils;

import de.mat3.badintent.hooking.BaseHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;



public class HookingManager {

    protected XC_LoadPackage.LoadPackageParam lpparam;
    protected String target;
    protected BaseHook hooks;

    public HookingManager(XC_LoadPackage.LoadPackageParam lpparam, String target) {
        this.lpparam = lpparam;
        this.target = target;
        hooks = new BaseHook(lpparam, target);
    }


    public boolean continueHooking(){
        return continueHooking(this.lpparam, this.target);
    }

    /**
     * Determines if the target package is currently being loaded.
     * @param lpparam
     * @param targetPackage
     * @return true iff target package is currently loaded.
     */
    public static boolean continueHooking(XC_LoadPackage.LoadPackageParam lpparam, String targetPackage) {
        return lpparam.packageName.equals(targetPackage);
    }

    public BaseHook getBaseHook() {
        return hooks;
    }
}
