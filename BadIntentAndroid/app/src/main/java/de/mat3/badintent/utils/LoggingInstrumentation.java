package de.mat3.badintent.utils;

import android.util.Base64;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Member;
import java.security.interfaces.RSAPrivateKey;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Pretty printing and logging instrumentation
 */

public class LoggingInstrumentation {

    public static String TAG;

    public static String toString(Object object) {
        try {
            if (object == null) {
                return "null";
            } else if (object.getClass().isArray()) {
                if (object.getClass().getComponentType().isPrimitive()) {
                    if (object instanceof byte[]) {
                        return "ByteArray (Base64): " + Base64.encodeToString((byte[]) object, Base64.DEFAULT);
                    } else {
                        return "PrimitiveArray: " + object.toString();
                    }
                } else {
                    Object[] array = (Object[]) object;
                    StringBuilder arrStr = new StringBuilder("Array:");
                    for(Object item : array) {
                        arrStr.append(" (" + LoggingInstrumentation.toString(item) + ") ");
                    }
                    return "Array: " + arrStr.toString();
                }
            } else if (object instanceof File) {
                return "File: " + ((File) object).getAbsolutePath();
            } else if (object instanceof PBEKeySpec) {
                PBEKeySpec spec = (PBEKeySpec) object;
                return "PBEKeySpec: Password:" + new String(spec.getPassword()) + " Iterations:" + spec.getIterationCount() + " Salt(Base64):" + Base64.encodeToString(spec.getSalt(), Base64.DEFAULT);
            } else if (object instanceof RSAPrivateKey) {
                RSAPrivateKey priv = (RSAPrivateKey) object;
                return "RSAPrivateKey: " + priv.getAlgorithm() + " Encoded(Base64):" + Base64.encodeToString(priv.getEncoded(), Base64.DEFAULT);
            } else if (object instanceof SecretKeySpec) {
                SecretKeySpec key = (SecretKeySpec) object;
                return "SecretKeySpec (" + key.getAlgorithm() + "): Encoded(Base64): " + Base64.encodeToString(key.getEncoded(), Base64.DEFAULT);
            } else if (object instanceof IvParameterSpec) {
                IvParameterSpec iv = (IvParameterSpec) object;
                return "IvParameterSpec: Encoded(Base64): " + Base64.encodeToString(iv.getIV(), Base64.DEFAULT);
            }
            else {
                return object.toString();
            }
        } catch (RuntimeException e) {
            /**best effort */
            return "Could not create string representation of " + object.getClass();
        } catch (Exception e) {
            /**best effort */
            return "Could not create string representation of " + object.getClass();
        }
    }

    /**
     * Pretty print and log all method parameters
     * @param metaData
     */
    public static void printParameters(LoggingMetaData metaData) {
        Member method = metaData.param.method;
        if (metaData.param.args == null) {
            Log.i(TAG, metaData.methodHookId + " Call " + method.getName() + " (" + metaData.classname + ") without parameters");
        } else if (metaData.param.args.length > 0) {
            StringBuilder s = new StringBuilder(metaData.methodHookId + " Call " + method.getName() + " (" + metaData.classname + ") with parameters " + metaData.param.args.length + ":\n");
            for (Object param : metaData.param.args) {
                s.append(toString(param) + " | ");
            }
            Log.i(TAG, s.toString());
        } else {
            Log.i(TAG, metaData.methodHookId + " Call " + method.getName() + " (" + metaData.classname + ")");
        }
    }

    /**
     * Pretty print and log result
     * @param metaData
     */
    public static void printResult(LoggingMetaData metaData) {
        Member method = metaData.param.method;

        String s = toString(metaData.param.getResult());
        if (!s.equals("null")) {
            Log.i(TAG, metaData.methodHookId + " Result " + method.getName() + " (" + metaData.classname + "):\n" + s);
        }
    }

}
