package de.mat3.badintent.app;

import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.text.format.Formatter;


public class BadIntentPreferencesActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //try to set this WiFi IP (in case there is no [valid] value set yet)
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        String wifiIP = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        SharedPreferences sPrefs = getSharedPreferences(AppAnalyzer.PREFNAME, MODE_WORLD_READABLE);
        String target_ip = sPrefs.getString(BadIntentConstants.TARGET_IP, " ");
        if (target_ip.equals(" ") || target_ip.equals("0.0.0.0")) {
            sPrefs.edit()
                    .putString(BadIntentConstants.TARGET_IP, wifiIP)
                    .apply();
        }
        addPreferencesFromResource(R.xml.bad_intent_preferences);

    }

}
