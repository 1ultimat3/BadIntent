package de.mat3.badintent.hooking.proxy;

import android.os.Parcel;
import android.util.Log;

import com.google.common.collect.Sets;

import java.util.Set;

import de.mat3.badintent.app.BadIntentConstants;
import de.mat3.badintent.hooking.proxy.dao.ParcelOperation;
import de.robv.android.xposed.XSharedPreferences;

/**
 * AppInformation contains data about the currently hooked App
 */
public enum AppInformation {

    Instance;

    public String packageName;
    public int port;
    public XSharedPreferences sPrefs;
    public Set<String> usedInterfaceTokens = Sets.newConcurrentHashSet();
    public Set<String> interceptedInterfaceTokens = Sets.newConcurrentHashSet();

    private static final String TAG = "BadIntentAppInfo";

    /**
     * Determine if the parcel has to be intercepted with respect to:
     *   1. interface token regex
     *   2. current package regex
     * @param data
     * @return true iff should be intercepted
     */
    public boolean intercept(Parcel data) {
        String filterInterfaceToken = sPrefs.getString(BadIntentConstants.FILTER_INTERFACE_TOKEN, "");
        String filterPackageName = sPrefs.getString(
                BadIntentConstants.FILTER_PACKAGE_NAME, "");
        //check if should be intercepted
        if (packageName.matches(filterPackageName)) {
            ParcelContainer container = ParcelAgent.Instance.getOrCreateParcelContainer(data);

            if (container.operationList.size() == 0) {
                Log.w(TAG, "Container without operations identified...");
                return false;
            } else {
                ParcelOperation op = container.operationList.get(0);
                if (op.operationType.equals(ParcelOperation.ParcelType.INTERFACE_TOKEN)) {
                    //add for statistic purposes
                    String interfaceTokenValue = op.operationValue.toString();
                    usedInterfaceTokens.add(interfaceTokenValue);

                    //check if filter matches
                    boolean matches = interfaceTokenValue.matches(filterInterfaceToken);
                    if (matches) {
                        interceptedInterfaceTokens.add(interfaceTokenValue);
                    }
                    return matches;
                } else {
                    //Log.w(TAG, "no interface token found, no intercept!");
                    return false;
                }
            }
        } else {
            return false;
        }

    }

    /**
     * Determine if the package has to be intercepted
     * @return true iff should be intercepted
     */
    public boolean intercept() {
        String filterPackageName = sPrefs.getString(
                BadIntentConstants.FILTER_PACKAGE_NAME, "");
        //check if should be intercepted
        return packageName.matches(filterPackageName);
    }
}
