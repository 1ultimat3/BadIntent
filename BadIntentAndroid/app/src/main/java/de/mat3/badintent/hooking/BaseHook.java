package de.mat3.badintent.hooking;

import java.lang.reflect.Method;

import de.mat3.badintent.utils.LoggingInstrumentation;
import de.mat3.badintent.utils.LoggingMetaData;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * BaseHook has the objective to support the Xposed module developer to writing hooks more efficient.
 */
public class BaseHook {


    protected String targetPackage;
    protected LoadPackageParam lpparam;
    protected ClassLoader cloader;

    public BaseHook(LoadPackageParam lpparam, String targetPackage) {
        this.lpparam = lpparam;
        this.targetPackage = targetPackage;
        cloader = lpparam.classLoader;
    }

    public BaseHook(BaseHook h) {
        this.lpparam = h.lpparam;
        this.targetPackage = h.targetPackage;
        cloader = lpparam.classLoader;
    }


    public void logAll(String classname){
        logAllConstructors(classname);
        logAllMethods(classname);
    }

    /**
     * This methods logs all methods and results in a best effort approach.
     * @param classname
     *
     */
    public void logAllMethods(final String classname) {
        try {
            Class<?> aClass = cloader.loadClass(classname);
            for (Method method : aClass.getDeclaredMethods()) {
                logMethod(classname, method.getName());
            }
        }
        catch (RuntimeException e) { /** best effort */ }
        catch (Exception e) { /** best effort */ }
    }

    public void logMethod(String classname, String methodname) {
        try {
            Class<?> aClass = cloader.loadClass(classname);
            final LoggingMetaData metaData = new LoggingMetaData(classname);

            XposedBridge.hookAllMethods(aClass, methodname, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    metaData.beforeHookedMethod(param);
                    LoggingInstrumentation.printParameters(metaData);
                    super.beforeHookedMethod(param);
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    metaData.afterHookedMethod(param);
                    LoggingInstrumentation.printResult(metaData);
                    super.afterHookedMethod(param);

                }
            });

        }
        catch (RuntimeException e) { /** best effort */ }
        catch (Exception e) { /** best effort */ }
    }


    /**
     * This methods logs all methods and results in a best effort approach.
     * @param classname
     *
     */
    public void logAllConstructors(final String classname) {
        try {
            Class<?> aClass = cloader.loadClass(classname);
            final LoggingMetaData metaData = new LoggingMetaData(classname);

            XposedBridge.hookAllConstructors(aClass, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    metaData.beforeHookedMethod(param);
                    LoggingInstrumentation.printParameters(metaData);
                    super.beforeHookedMethod(param);
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    metaData.afterHookedMethod(param);
                    LoggingInstrumentation.printResult(metaData);
                    super.afterHookedMethod(param);

                }
            });
        }
        catch (RuntimeException e) { /** best effort */ }
        catch (Exception e) { /** best effort */ }
    }

    public void overwriteResults(String classname, String method, final Object result){
        try {
            Class<?> aClass = cloader.loadClass(classname);
            XposedBridge.hookAllMethods(aClass, method, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    param.setResult(result);
                }
            });

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


}
