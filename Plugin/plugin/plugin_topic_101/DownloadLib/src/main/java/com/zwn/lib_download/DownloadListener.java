package com.zwn.lib_download;
import java.io.File;

public interface DownloadListener {
    void onProgress(String fileId, int progress, long loadedSize, long fileSize);
    void onSuccess(String fileId, int type, File file);
    void onFailed(String fileId, int type, int code);
    void onPaused(String fileId);
    void onCancelled(String fileId);
    void onUpdate(String fileId);
}
