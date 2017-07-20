package burp;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dao.AppInfo;
import ui.ActiveAppView;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ActiveAppsTab implements ITab {

    /** identified bad intent ports are collected here */
    public final static Queue<String> openTasks = new ConcurrentLinkedQueue<>();

    protected Collection<AppInfo> activeApps = Collections.synchronizedCollection(new LinkedList<AppInfo>());
    protected Thread activityCheckerThread;
    protected  ActiveAppView activeAppView;

    public ActiveAppsTab(){
        activeAppView = new ActiveAppView(activeApps);
        activityCheckerThread = new Thread(new Runnable() {

            Gson gson = new GsonBuilder().create();

            @Override
            public void run() {
                System.out.println("starting app info checker thread");
                while (true) {
                    //process open tasks first
                    for (int i = 0; i < openTasks.size(); i++) {
                        processOpenTask();
                    }
                    //process existing apps (check if still active)
                    List<AppInfo> appInfoRemovalList = new LinkedList<>();
                    for(AppInfo info : activeApps) {
                        try {
                            getAppInfo(info.endpoint);
                        } catch(Exception e) {
                            System.out.println("appinfo not present / removing  " + info.endpoint);
                            appInfoRemovalList.add(info);
                        }
                    }
                    //remove app infos now
                    for(AppInfo remove : appInfoRemovalList){
                        activeApps.remove(remove);
                    }
                    if(openTasks.isEmpty()) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                        /* best effort */
                        }
                    }
                    activeAppView.rebuild();
                }

            }

            private void processOpenTask() {
                try {
                    String target = openTasks.remove();
                    if(!activeApp(target)) {
                        AppInfo appInfo = getAppInfo(target);
                        activeApps.add(appInfo);
                    } else {
                        //update existing app value
                        AppInfo appInfo = getAppInfo(target);
                        AppInfo appInfoOld = getActiveApp(target);
                        activeApps.remove(appInfoOld);
                        activeApps.add(appInfo);
                    }

                } catch (MalformedURLException e) {
                    System.out.println(e.getLocalizedMessage());
                } catch (IOException e) {
                    System.out.println(e.getLocalizedMessage());
                }
            }

            private AppInfo getAppInfo(String target) throws IOException {
                HttpURLConnection conn = (HttpURLConnection) new URL(target).openConnection();
                InputStream response = conn.getInputStream();
                AppInfo appInfo = gson.fromJson(new InputStreamReader(response), AppInfo.class);
                appInfo.endpoint = target;
                return appInfo;
            }

            private boolean activeApp(String target) {
                if (target == null) return false;
                for (AppInfo appInfo : activeApps) {
                    if(target.equals(appInfo.endpoint)) {
                        return true;
                    }
                }
                return false;
            }

            private AppInfo getActiveApp(String target) {
                if (target == null) return null;
                for (AppInfo appInfo : activeApps) {
                    if(target.equals(appInfo.endpoint)) {
                        return appInfo;
                    }
                }
                return null;
            }
        });
        activityCheckerThread.start();
    }

    @Override
    public String getTabCaption() {
        return "ActiveApps";
    }

    @Override
    public Component getUiComponent() {
        return activeAppView;
    }

    public static void addOpenTask(InetAddress clientIpAddress, int port) {
        String target = "http://" + clientIpAddress.getHostAddress() + ":" + port + "/info";
        openTasks.add(target);
    }
}
