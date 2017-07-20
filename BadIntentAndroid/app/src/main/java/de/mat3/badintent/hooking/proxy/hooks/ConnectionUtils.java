package de.mat3.badintent.hooking.proxy.hooks;


import android.support.annotation.NonNull;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;

import de.mat3.badintent.app.BadIntentConstants;
import de.robv.android.xposed.XSharedPreferences;

public class ConnectionUtils {

    /**
     * Opens proxyfied connection based on BadIntent preferences.
     * @param url
     * @param sPrefs
     * @return opened HttpURLConnection
     * @throws IOException
     */
    public static HttpURLConnection getBadIntentHttpURLConnection(URL url, XSharedPreferences sPrefs) throws IOException {
        final HttpURLConnection conn;
        if (sPrefs.getBoolean(BadIntentConstants.USE_SYSTEM_PROXY, true)){
            conn = (HttpURLConnection) url.openConnection();
        } else {
            String host = sPrefs.getString(BadIntentConstants.PROXY_HOST, "localhost");
            int port = Integer.parseInt(sPrefs.getString(BadIntentConstants.PROXY_PORT, "8080"));
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
            conn = (HttpURLConnection) url.openConnection(proxy);
        }
        return conn;
    }

    /**
     * Get REST URL based on the BadIntent preferences.
     * @param resource needs to start with /
     * @param sPrefs
     * @param port
     * @return
     * @throws MalformedURLException
     */
    public static URL getBadIntentURL(String resource, XSharedPreferences sPrefs, int port) throws MalformedURLException {
        String ip = sPrefs.getString(BadIntentConstants.TARGET_IP, "localhost");
        return new URL("http://" + ip + ":" + port + resource);
    }

    public static Thread readResponseAndCloseConnection(final HttpURLConnection conn) {
        return new Thread(new Runnable() {
            @Override
            public void run() {
                BufferedReader reader = null;
                try {
                    reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuffer buffer = new StringBuffer();
                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }
                } catch (IOException e) {
                    Log.e(TransactionHooks.TAG, "could not receive HTTP data!");
                    Log.e(TransactionHooks.TAG, " " + e.getMessage());
                } finally {
                    try {
                        if (reader != null) {
                            reader.close();
                        }
                    } catch (IOException e) {
                        //best effort
                    }
                }
            }
        });
    }
}
