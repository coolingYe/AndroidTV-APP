// IHostManager.aidl
package com.zwn.launcher.host;

// Declare any non-default types here with import statements

interface IHostManager {
    void installPlugin(String fileId);

    String getInstallingFileId();

    String getLastPluginPackageName();

    void setLastPluginPackageName(String packageName);

    String getLastUnzipDonePlugin();

    void setLastUnzipDonePlugin(String fileId);

    void logoutClear();

    void gotoLoginPage();

    boolean isGestureAiEnable();

    void startGestureAi(boolean withActive);

    void stopGestureAi();

    boolean isGestureAIActive();

}