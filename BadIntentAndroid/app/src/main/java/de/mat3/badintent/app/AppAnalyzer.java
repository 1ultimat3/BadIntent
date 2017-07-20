package de.mat3.badintent.app;


import android.os.StrictMode;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;

import de.mat3.badintent.hooking.BaseHook;
import de.mat3.badintent.hooking.proxy.AppInformation;
import de.mat3.badintent.hooking.proxy.hooks.LogHooks;
import de.mat3.badintent.hooking.proxy.hooks.ParcelProxyHooks;
import de.mat3.badintent.hooking.proxy.hooks.TransactionHooks;
import de.mat3.badintent.utils.HookingManager;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_LoadPackage;


public class AppAnalyzer implements IXposedHookLoadPackage {

    public static final String PREFNAME = "de.mat3.badintent_preferences";
    public static final String PREFERENCE_PATH = "/data/data/de.mat3.badintent/shared_prefs/" + PREFNAME + ".xml";
    public static XSharedPreferences sPrefs;
    protected static final String TAG = "BadIntentAnalyzer";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        sPrefs = new XSharedPreferences(new File(PREFERENCE_PATH));
        sPrefs.reload();

        String target = lpparam.packageName;

        //bypass various apps depending on preferences
        String[] bypass = getBypassList();
        String packageNameFilter = sPrefs.getString("package_filter", "");
        for (String bypassElement : bypass) {
            /* check if package filter does not equal this package
            NOTE: this is a special condition in order to overwrite the hook_system_switch;
            However, the packageNameFilter has to exactly match exactly the target package.
             */
            if (target.matches(bypassElement) && !target.equals(packageNameFilter)){
                return;
            }
        }

        //disable strict mode in order to prevent unclosed connections file usage
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().build());

        HookingManager hookingManager = new HookingManager(lpparam, target);

        int port = getRandomPort();

        //updating current app's meta data
        AppInformation.Instance.packageName = lpparam.packageName;
        AppInformation.Instance.port = port;
        Log.d(TAG, "Hooking package:" + target + " port: " + port);
        if (hookingManager.continueHooking()) {
            BaseHook base = hookingManager.getBaseHook();
            ParcelProxyHooks parcelProxyHooks = new ParcelProxyHooks(base, port);
            parcelProxyHooks.hookParcel();
            TransactionHooks transactionHooks = new TransactionHooks(base, sPrefs, port);
            transactionHooks.hookBinder();
            LogHooks logHooks = new LogHooks(base, sPrefs, port);
            logHooks.hookLogs();
        }
    }

    /**
     * Bypass various typical system apps
     * Note: this is only a best effort strategy;
     * in order to get complete list of system apps use
     * "pm list packages -s"
     *
     * @return list of package regex which should be bypassed
     */
    protected String[] getBypassList() {
        boolean hookSystem = sPrefs.getBoolean(BadIntentConstants.HOOK_SYSTEM_SWITCH, false);
        if (hookSystem) {
            //only bypass android core
            return new String[]{
                    "android"
            };
        } else {
            return new String[]{
                    "android",
                    "com.android.*",
                    "com.google.*",
                    "de.mat3.badintent",
            };
        }
    }

    protected static int getRandomPort() throws IOException {
        ServerSocket s = new ServerSocket(0);
        int port = s.getLocalPort();
        s.close();
        return port;
    }
}
