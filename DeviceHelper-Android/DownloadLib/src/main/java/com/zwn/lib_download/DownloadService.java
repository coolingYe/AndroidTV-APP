package com.zwn.lib_download;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;

import com.zwn.lib_download.db.CareController;
import com.zwn.lib_download.model.DownloadInfo;
import com.zwn.lib_download.model.LoadedInfo;
import com.zwn.lib_download.utils.FileHelper;
import com.zwn.lib_download.utils.LogHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;

public class DownloadService extends Service{
    private static final String TAG = "DownloadService";
    private final DownloadBinder downloadBinder = new DownloadBinder();
    private static final int DEFAULT_TASK_NUM = 3;
    private static final ConcurrentHashMap<String, DownloadTask> downloadingMap = new ConcurrentHashMap<>(DEFAULT_TASK_NUM);
    private static final ConcurrentHashMap<String, LoadedInfo> loadedSizeMap = new ConcurrentHashMap<>(6);
    private static final int MSG_LOADED_HANDLE = 1;
    private static final List<DownloadListener> downloadListenerList = new ArrayList<>();
    private static OkHttpClient okHttpClient;

    @SuppressLint("HandlerLeak")
    private static final Handler mHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_LOADED_HANDLE) {
                List<LoadedInfo> loadedInfoList = new ArrayList<>(loadedSizeMap.values());
                loadedSizeMap.clear();
                if (loadedInfoList.size() > 0)
                    CareController.instance.bulkUpdateLoadedSize(loadedInfoList);
                sendEmptyMessageDelayed(MSG_LOADED_HANDLE, 500);
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        LogHelper.i(TAG, "onCreate()");
        CareController.init(getApplicationContext());
        CareController.instance.updateDownloadInfoStop();
        mHandler.sendEmptyMessageDelayed(MSG_LOADED_HANDLE, 1000);
        initOkHttpClient();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogHelper.i(TAG, "onStartCommand()");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        LogHelper.i(TAG, "onDestroy()");
        super.onDestroy();
    }

    private void initOkHttpClient(){
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(8, 10, TimeUnit.SECONDS))
                .build();
    }

    @Override
    public IBinder onBind(Intent intent) {
        LogHelper.i(TAG, "onBind()");
        return downloadBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        LogHelper.i(TAG, "onUnbind()");
        return super.onUnbind(intent);
    }

    public static class DownloadBinder extends Binder {

        public boolean startDownload(String fileId){

            DownloadInfo downloadInfo = CareController.instance.getDownloadInfoByFileId(fileId);
            if(downloadInfo == null) return false;
            if(downloadInfo.status == DownloadInfo.STATUS_STOPPED){
                CareController.instance.updateDownloadInfoStatus(fileId, DownloadInfo.STATUS_PENDING);
            }else if(downloadInfo.status == DownloadInfo.STATUS_SUCCESS){
                return true;
            }

            if(downloadingMap.size() < DEFAULT_TASK_NUM && !downloadingMap.containsKey(fileId)){
                DownloadTask downloadTask = new DownloadTask(downloadInfo, downloadListener, okHttpClient);
                downloadingMap.put(downloadInfo.fileId, downloadTask);
                downloadTask.execute();
            }
            return true;
        }

        public boolean startDownload(DownloadInfo downloadInfo){
            downloadInfo.loadedSize = 0;
            downloadInfo.fileSize = 0;
            downloadInfo.saveTime = new Date().getTime();
            downloadInfo.status = DownloadInfo.STATUS_PENDING;

            LogHelper.i(TAG, "startDownload() " + downloadInfo);

            boolean isSuccess = CareController.instance.addDownloadInfo(downloadInfo);

            if(!isSuccess){
                DownloadInfo downloadInfoTmp = CareController.instance.getDownloadInfoByFileId(downloadInfo.fileId);
                if(downloadInfoTmp == null) return false;
                if(downloadInfoTmp.status == DownloadInfo.STATUS_STOPPED){
                    if(!downloadInfoTmp.version.equals(downloadInfo.version)){
                        CareController.instance.updateDownloadInfoNewVersion(downloadInfo, DownloadInfo.STATUS_PENDING);
                        File file = new File(downloadInfoTmp.filePath);
                        if(file.exists()){
                            file.delete();
                        }
                        downloadListener.onUpdate(downloadInfo.fileId);
                    }else{
                        CareController.instance.updateDownloadInfoStatus(downloadInfo.fileId, DownloadInfo.STATUS_PENDING);
                        downloadListener.onUpdate(downloadInfo.fileId);
                    }
                }else if(downloadInfoTmp.status == DownloadInfo.STATUS_SUCCESS){
                    if(!downloadInfoTmp.version.equals(downloadInfo.version)){
                        CareController.instance.updateDownloadInfoNewVersion(downloadInfo, DownloadInfo.STATUS_PENDING);
                        File file = new File(downloadInfoTmp.filePath);
                        if(file.exists()){
                            file.delete();
                        }
                        downloadListener.onUpdate(downloadInfo.fileId);
                    }else{
                        downloadListener.onUpdate(downloadInfo.fileId);
                        return true;
                    }
                }
            }else{
                downloadListener.onUpdate(downloadInfo.fileId);
            }

            if(downloadingMap.size() < DEFAULT_TASK_NUM && !downloadingMap.containsKey(downloadInfo.fileId)){
                DownloadTask downloadTask = new DownloadTask(downloadInfo, downloadListener, okHttpClient);
                downloadingMap.put(downloadInfo.fileId, downloadTask);
                downloadTask.execute();
            }
            return true;
        }

        public void pauseDownload(String fileId){
            if(downloadingMap.containsKey(fileId)){
                DownloadTask downloadTask = downloadingMap.get(fileId);
                if(downloadTask != null) {
                    downloadTask.pausedDownload();
                    downloadingMap.remove(fileId);
                }
            }
            CareController.instance.updateDownloadInfoStatus(fileId, DownloadInfo.STATUS_STOPPED);
            downloadListener.onUpdate(fileId);
        }

        public void cancelDownload(String fileId){
            if(downloadingMap.containsKey(fileId)){
                DownloadTask downloadTask = downloadingMap.get(fileId);
                if(downloadTask != null) {
                    downloadTask.cancelDownload();
                    downloadingMap.remove(fileId);
                    CareController.instance.deleteDownloadInfo(fileId);
                    downloadListener.onUpdate(fileId);
                }
            }else{
                DownloadInfo downloadInfo = CareController.instance.getDownloadInfoByFileId(fileId);
                if(downloadInfo != null){
                    File file = new File(downloadInfo.filePath);
                    if(file.exists()){
                        file.delete();
                    }
                }
                CareController.instance.deleteDownloadInfo(fileId);
                downloadListener.onUpdate(fileId);
            }
        }

        public void registerDownloadListener(DownloadListener downloadListener){
            downloadListenerList.add(downloadListener);
        }

        public void unRegisterDownloadListener(DownloadListener downloadListener){
            downloadListenerList.remove(downloadListener);
        }
    }

    private static final DownloadListener downloadListener = new DownloadListener() {
        @Override
        public void onProgress(String fileId, int progress, long loadedSize, long fileSize) {
            LogHelper.i(TAG, "onProgress() fileId=" + fileId + ", progress=" + progress);
            if(downloadingMap.containsKey(fileId)) {
                CareController.instance.updateDownloadInfoLoading(fileId, DownloadInfo.STATUS_LOADING, fileSize);

                for(int i=0; i<downloadListenerList.size(); i++){
                    downloadListenerList.get(i).onProgress(fileId, progress, loadedSize, fileSize);
                }
            }
            loadedSizeMap.put(fileId, new LoadedInfo(fileId, loadedSize));
        }

        @Override
        public void onSuccess(String fileId, int type, File file) {
            LogHelper.i(TAG, "onSuccess() fileId=" + fileId);
            downloadingMap.remove(fileId);
            String downloadFileMd5 = CareController.instance.getDownloadFileMd5(fileId);
            if(downloadFileMd5 != null && !downloadFileMd5.isEmpty()){//the file has md5 value;
                String fileMD5 = FileHelper.file2MD5(file);
                if(!downloadFileMd5.equals(fileMD5)){
                    LogHelper.e(TAG, "onSuccess() fileId=" + fileId + ", file=" + file.getAbsolutePath() + ", md5 check failed!<<<---");
                    if(file.delete()){
                        CareController.instance.updateDownloadInfoStatus(fileId, DownloadInfo.STATUS_PENDING);
                    }else{
                        CareController.instance.updateDownloadInfoStatus(fileId, DownloadInfo.STATUS_STOPPED);
                    }
                }else{
                    CareController.instance.updateDownloadInfoStatus(fileId, DownloadInfo.STATUS_SUCCESS);
                    for(int i=0; i<downloadListenerList.size(); i++){
                        downloadListenerList.get(i).onSuccess(fileId, type, file);
                    }
                }
            }else{
                CareController.instance.updateDownloadInfoStatus(fileId, DownloadInfo.STATUS_SUCCESS);
                for(int i=0; i<downloadListenerList.size(); i++){
                    downloadListenerList.get(i).onSuccess(fileId, type, file);
                }
            }

            nextToDo();
        }

        private synchronized void nextToDo(){
            List<DownloadInfo> downloadInfoList = CareController.instance.getLatestPendingList();

            for(int i=0; i<downloadInfoList.size(); i++){
                DownloadInfo downloadInfo = downloadInfoList.get(i);
                if(downloadingMap.size() < DEFAULT_TASK_NUM ){
                    if(!downloadingMap.containsKey(downloadInfo.fileId)) {
                        DownloadTask downloadTask = new DownloadTask(downloadInfo, downloadListener, okHttpClient);
                        downloadingMap.put(downloadInfo.fileId, downloadTask);
                        downloadTask.execute();
                    }
                }else{
                    break;
                }
            }
        }

        @Override
        public void onFailed(String fileId, int type, int code) {
            LogHelper.i(TAG, "onFailed() fileId=" + fileId);
            downloadingMap.remove(fileId);
            CareController.instance.updateFailedDownloadStatus(fileId);

            for(int i=0; i<downloadListenerList.size(); i++){
                downloadListenerList.get(i).onFailed(fileId, type, code);
            }
        }

        @Override
        public void onPaused(String fileId) {
            LogHelper.i(TAG, "onPaused() fileId=" + fileId);
            downloadingMap.remove(fileId);
            CareController.instance.updateDownloadInfoStatus(fileId, DownloadInfo.STATUS_STOPPED);

            for(int i=0; i<downloadListenerList.size(); i++){
                downloadListenerList.get(i).onPaused(fileId);
            }
        }

        @Override
        public void onCancelled(String fileId) {
            LogHelper.i(TAG, "onCancelled() fileId=" + fileId);
            downloadingMap.remove(fileId);
            CareController.instance.deleteDownloadInfo(fileId);

            for(int i=0; i<downloadListenerList.size(); i++){
                downloadListenerList.get(i).onCancelled(fileId);
            }
        }

        @Override
        public void onUpdate(String fileId) {
            for(int i=0; i<downloadListenerList.size(); i++){
                downloadListenerList.get(i).onUpdate(fileId);
            }
        }
    };
}

