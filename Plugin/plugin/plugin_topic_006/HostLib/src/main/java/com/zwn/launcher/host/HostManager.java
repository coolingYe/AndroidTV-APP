package com.zwn.launcher.host;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.RemoteException;

import com.qihoo360.replugin.RePlugin;

public class HostManager {
    private volatile static IHostManager hostManager;
    private static volatile SharedPreferences hostSp;

    public static IHostManager getHostManager(){
        if(hostManager == null){
            synchronized (HostManager.class){
                if(hostManager == null){
                    if(BuildConfig.FLAVOR.contains("plugin")){
                        IBinder iBinder = RePlugin.getGlobalBinder("HostManager");
                        hostManager = IHostManager.Stub.asInterface(iBinder);
                    }else{
                        hostManager =  new SimulateHostManager();
                    }

                }
            }
        }
        return hostManager;
    }

    public static SharedPreferences getHostSp(){
        if(hostSp == null){
            synchronized (HostManager.class){
                if(hostSp == null){
                    hostSp = RePlugin.getHostContext().getSharedPreferences("spUtils", Context.MODE_PRIVATE);
                }
            }
        }
        return hostSp;
    }

    public static void installPlugin(String fileId){
        try {
            getHostManager().installPlugin(fileId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static String getInstallingFileId(){
        try {
            return getHostManager().getInstallingFileId();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getLastPluginPackageName(){
        try {
            return getHostManager().getLastPluginPackageName();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setLastPluginPackageName(String packageName){
        try {
            getHostManager().setLastPluginPackageName(packageName);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static String getLastUnzipDonePlugin(){
        try {
            return getHostManager().getLastUnzipDonePlugin();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setLastUnzipDonePlugin(String fileId){
        try {
            getHostManager().setLastUnzipDonePlugin(fileId);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static String getHostSpString(String key, String defValue){
        if (BuildConfig.FLAVOR.contains("plugin")) {
            return getHostSp().getString(key, defValue);
        } else {
            if("UserToken".equals(key)){
                return "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ0b2tlbklkIjoiMzExNTU0MDgzNTk1ODE2OTY1IiwidXNlcklkIjoyNDg4NzE4OTA5MjY2MjA2NzksInVzZXJDb2RlIjoibGFib3JfdW5pb25fMDAxIiwiZXhwaXJlVGltZSI6IjIwNDQtMDEtMTEgMTQ6MDc6MjEiLCJhcHBDb2RlIjoibWFsbF91bXMiLCJ1c2VyVHlwZSI6MiwiaXNzIjoiemVld2FpbiIsImV4cCI6MjMzNjEwNTI0MX0.VzzaMTTxNcD1Ii0lv6jtmuAJL2u3pucu8tPXAW6cWTM";
            } else if ("UserAccount".equals(key)) {
                return "紫为云工会";
            } else if ("PlatformInfo".equals(key)) {
                return "AndroidRockchipTVAIIP";
            } else if ("BaseUrl".equals(key)) {
                return "https://dev.local.zeewain.com";
            } else if ("BasePath".equals(key)) {
                return "/api";
            } else if ("DeviceSN".equals(key)) {
                return "SN0333933564646664583938";
            } else {
                return null;
            }
        }
    }

    public static void logoutClear(){
        try {
            getHostManager().logoutClear();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static boolean isGestureAiEnable(){
        try {
            return getHostManager().isGestureAiEnable();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void startGestureAi(boolean withActive){
        try {
            getHostManager().startGestureAi(withActive);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void stopGestureAi(){
        try {
            getHostManager().stopGestureAi();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static boolean isGestureAIActive(){
        try {
            return getHostManager().isGestureAIActive();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static void gotoLoginPage(Context context){
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(RePlugin.getHostContext().getPackageName(), "com.zee.launcher.login.ui.LoginActivity"));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    public static Context getUseContext(Context context){
        if (BuildConfig.FLAVOR.contains("plugin")) {
            return RePlugin.getHostContext();
        }else return context;
    }

    static class SimulateHostManager implements IHostManager{

        @Override
        public IBinder asBinder() {
            return null;
        }

        @Override
        public void installPlugin(String fileId) throws RemoteException {

        }

        @Override
        public String getInstallingFileId() throws RemoteException {
            return null;
        }

        @Override
        public String getLastPluginPackageName() throws RemoteException {
            return null;
        }

        @Override
        public void setLastPluginPackageName(String packageName) throws RemoteException {

        }

        @Override
        public String getLastUnzipDonePlugin() throws RemoteException {
            return null;
        }

        @Override
        public void setLastUnzipDonePlugin(String fileId) throws RemoteException {

        }

        @Override
        public void logoutClear() throws RemoteException {

        }

        @Override
        public void gotoLoginPage() throws RemoteException {

        }

        @Override
        public boolean isGestureAiEnable() throws RemoteException {
            return false;
        }

        @Override
        public void startGestureAi(boolean withActive) throws RemoteException {

        }

        @Override
        public void stopGestureAi() throws RemoteException {

        }

        @Override
        public boolean isGestureAIActive() throws RemoteException {
            return false;
        }
    }
}
