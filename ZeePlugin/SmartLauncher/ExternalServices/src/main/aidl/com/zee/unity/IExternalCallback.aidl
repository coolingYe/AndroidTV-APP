// IExternalCallback.aidl
package com.zee.unity;

// Declare any non-default types here with import statements

interface IExternalCallback {
    void onProgress(String fileId, int progress, long loadedSize, long fileSize);
    void onSuccess(String fileId, int type, String filePath);
    void onFailed(String fileId, int type, int code);
    void onPaused(String fileId);
    void onCancelled(String fileId);
    void onUpdate(String fileId);

    void onInstallStatus(String fileId, int status);
    void onPrepareStartStatus(String fileId, int status);
    void onMessage(String message);
}