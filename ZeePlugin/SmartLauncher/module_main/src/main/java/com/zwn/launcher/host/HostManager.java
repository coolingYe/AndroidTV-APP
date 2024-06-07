package com.zwn.launcher.host;

import android.os.RemoteException;
import com.zeewain.ai.ZeeAiManager;
import com.zeewain.base.utils.CareLog;
import com.zeewain.base.utils.CommonUtils;
import com.zwn.launcher.service.ZeeServiceManager;

public class HostManager extends IHostManager.Stub {
    private static final String TAG = "HostManager";

    @Override
    public void installPlugin(String fileId) throws RemoteException {
        CareLog.i(TAG, "HostManager installPlugin fileId=" + fileId);
        if(!ZeeServiceManager.isInQueue(fileId)) {
            ZeeServiceManager.addToQueueNextCheckInstall(fileId);
        }
    }

    @Override
    public String getInstallingFileId() throws RemoteException {
        CareLog.i(TAG, "HostManager getInstallingFileId()");
        return ZeeServiceManager.installingFileId;
    }

    @Override
    public String getLastPluginPackageName() throws RemoteException {
        CareLog.i(TAG, "HostManager getLastPluginPackageName()");
        return ZeeServiceManager.lastPluginPackageName;
    }

    @Override
    public void setLastPluginPackageName(String packageName) throws RemoteException {
        CareLog.i(TAG, "HostManager setLastPluginPackageName() packageName=" + packageName);
        ZeeServiceManager.lastPluginPackageName = packageName;
    }

    @Override
    public String getLastUnzipDonePlugin() throws RemoteException {
        CareLog.i(TAG, "HostManager getLastUnzipDonePlugin()");
        return ZeeServiceManager.lastUnzipDonePlugin;
    }

    @Override
    public void setLastUnzipDonePlugin(String fileId) throws RemoteException {
        CareLog.i(TAG, "HostManager setLastUnzipDonePlugin() fileId=" + fileId);
        ZeeServiceManager.lastUnzipDonePlugin = fileId;
    }

    @Override
    public void logoutClear() throws RemoteException {
        CareLog.i(TAG, "HostManager logoutClear()");
        CommonUtils.logoutClear();
    }

    @Override
    public void gotoLoginPage() throws RemoteException {
        CareLog.i(TAG, "HostManager gotoLoginPage()");
    }

    @Override
    public boolean isGestureAiEnable() throws RemoteException {
        CareLog.i(TAG, "HostManager isGestureAiEnable()");
        return ZeeServiceManager.isSettingGestureAIEnable();
    }

    @Override
    public void startGestureAi(boolean withActive) throws RemoteException {
        CareLog.i(TAG, "HostManager startGestureAi() withActive=" + withActive);
        ZeeAiManager.getInstance().startGestureAI(withActive);
    }

    @Override
    public void stopGestureAi() throws RemoteException {
        CareLog.i(TAG, "HostManager stopGestureAi()");
        ZeeAiManager.getInstance().stopGestureAI();
    }

    @Override
    public boolean isGestureAIActive() throws RemoteException {
        CareLog.i(TAG, "HostManager isGestureAIActive()");
        return ZeeAiManager.getInstance().isGestureAIActive();
    }
}
