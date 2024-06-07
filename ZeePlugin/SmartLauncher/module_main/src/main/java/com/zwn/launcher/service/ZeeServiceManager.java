package com.zwn.launcher.service;

import static android.content.Context.ALARM_SERVICE;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.zee.manager.IZeeManager;
import com.zeewain.ai.ZeeAiManager;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.data.protocol.request.UserEventRecordReq;
import com.zeewain.base.data.protocol.response.BaseResp;
import com.zeewain.base.utils.ApkUtil;
import com.zeewain.base.utils.CareLog;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.DateTimeUtils;
import com.zeewain.base.utils.FileUtils;
import com.zeewain.base.utils.ToastUtils;
import com.zwn.launcher.data.DataRepository;
import com.zeewain.base.data.protocol.response.UpgradeResp;
import com.zwn.launcher.data.protocol.request.DeviceStatusErrorInfo;
import com.zwn.launcher.data.protocol.request.DeviceStatusReportReq;
import com.zwn.launcher.data.protocol.request.SnSoftwareCodeReq;
import com.zwn.launcher.utils.DownloadHelper;
import com.zwn.launcher.utils.SntpTimeUtil;
import com.zwn.lib_download.DownloadListener;
import com.zwn.lib_download.DownloadService;
import com.zwn.lib_download.db.CareController;
import com.zwn.lib_download.model.AppLibInfo;
import com.zwn.lib_download.model.DownloadInfo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class ZeeServiceManager {
    private static final String TAG = "ZeeService";
    private static Context appContext;
    private CareBroadcastReceiver careBroadcastReceiver;
    private static final ExecutorService mFixedPool = Executors.newFixedThreadPool(2);
    public static String installingFileId = null;
    public static String lastUnzipDonePlugin = null;
    public static String lastPluginPackageName = null;
    public static final ConcurrentLinkedQueue<String> installingQueue = new ConcurrentLinkedQueue<>();
    private static final List<DownloadListener> downloadListenerList = new ArrayList<>();
    private static volatile ZeeServiceManager instance;
    private static InstallAppListener installAppListener;
    private static final int MSG_CHECK_HOST_UPGRADE = 1111;
    private static final int MSG_SETTINGS_APP_INSTALLED = 1200;
    private static final int MSG_GESTURE_AI_APP_INSTALLED = 1201;
    private static final String DATA_APK_PATH = "dataApkPath";
    private static final String DATA_APK_FILE_ID = "dataApkFileId";
    private static final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == MSG_CHECK_HOST_UPGRADE){
                String dataApkPath = msg.getData().getString(DATA_APK_PATH);
                String dataApkFileId = msg.getData().getString(DATA_APK_FILE_ID);
                handleHostInstall(dataApkPath, dataApkFileId);
            } else if(msg.what == MSG_SETTINGS_APP_INSTALLED){
                if (appContext != null) {
                    sendSettingsReceiverAction(appContext);
                }
            } else if(msg.what == MSG_GESTURE_AI_APP_INSTALLED){
                if (appContext != null) {
                    sendGestureAiServiceCheck(appContext);
                }
            }
        }
    };

    public static void init(Context context){
        if(instance == null){
            synchronized (ZeeServiceManager.class){
                if(instance == null){
                    instance = new ZeeServiceManager(context);
                    CareLog.i(TAG, "ZeeServiceManager init() done");
                }
            }
        }
    }

    public static ZeeServiceManager getInstance() {
        return instance;
    }

    private ZeeServiceManager(Context context){
        appContext = context.getApplicationContext();
        registerBroadCast();
        scheduleNextPing(appContext);
    }

    public void registerDownloadListener(DownloadListener downloadListener){
        downloadListenerList.add(downloadListener);
    }

    public void unRegisterDownloadListener(DownloadListener downloadListener){
        downloadListenerList.remove(downloadListener);
    }

    public DownloadService.DownloadBinder downloadBinder = null;

    public DownloadService.DownloadBinder getDownloadBinder() {
        return downloadBinder;
    }

    private final DownloadListener downloadListener = new DownloadListener() {
        @Override
        public void onProgress(String fileId, int progress, long loadedSize, long fileSize) {
            for(int i=0; i<downloadListenerList.size(); i++){
                downloadListenerList.get(i).onProgress(fileId, progress, loadedSize, fileSize);
            }
        }

        @Override
        public void onSuccess(String fileId, int type, File file) {
            for(int i=0; i<downloadListenerList.size(); i++){
                downloadListenerList.get(i).onSuccess(fileId, type, file);
            }
            if(BaseConstants.DownloadFileType.PLUGIN_APP == type || BaseConstants.DownloadFileType.MANAGER_APP == type
                    || BaseConstants.DownloadFileType.SETTINGS_APP == type || BaseConstants.DownloadFileType.ZEE_GESTURE_AI_APP == type){
                addToQueueNextCheckInstall(fileId);
            }else if(BaseConstants.DownloadFileType.HOST_APP == type){
                handleHostInstall(file.getPath(), fileId);
            }
        }

        @Override
        public void onFailed(String fileId, int type, int code) {
            if(code == -2){
                ToastUtils.showShort("存储空间已低于%5限制了！");
            }
            for(int i=0; i<downloadListenerList.size(); i++){
                downloadListenerList.get(i).onFailed(fileId, type, code);
            }
        }

        @Override
        public void onPaused(String fileId) {
            for(int i=0; i<downloadListenerList.size(); i++){
                downloadListenerList.get(i).onPaused(fileId);
            }
        }

        @Override
        public void onCancelled(String fileId) {
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

    public static void addToQueueNextCheckInstall(String fileId){
        installingQueue.offer(fileId);
        handleCommonApkInstall();
    }

    public static boolean isInQueue(String fileId){
        return installingQueue.contains(fileId);
    }

    private synchronized static void handleCommonApkInstall(){
        if(installingFileId == null){
            installingFileId = installingQueue.poll();
            CareLog.i(TAG, "handleCommonApkInstall() prepare for installation fileId=" + installingFileId);
            if(installingFileId != null){
                final DownloadInfo downloadInfo = CareController.instance.getDownloadInfoByFileId(installingFileId);
                if(downloadInfo != null && downloadInfo.status == DownloadInfo.STATUS_SUCCESS &&  FileUtils.isFileExist(downloadInfo.filePath)){
                    mFixedPool.execute(() -> {
                        AppLibInfo appLibInfo = new AppLibInfo(downloadInfo.fileId, downloadInfo.mainClassPath);
                        boolean addResult = CareController.instance.addAppLibInfo(appLibInfo);
                        if(!addResult){
                            CareController.instance.updateAppLibInfo(appLibInfo);
                        }

                        //used for third party app default enable all Permission
                            /*String pkgNames = SystemProperties.get(BaseConstants.PERSIST_SYS_PERMISSION_PKG);
                            if(!TextUtils.isEmpty(pkgNames)){
                                SystemProperties.set(BaseConstants.PERSIST_SYS_PERMISSION_PKG, pkgNames + ";" + downloadInfo.mainClassPath);
                            }else{
                                SystemProperties.set(BaseConstants.PERSIST_SYS_PERMISSION_PKG, downloadInfo.mainClassPath);
                            }*/
                        Intent intent = new Intent();
                        intent.setAction(BaseConstants.PACKAGE_INSTALLED_ACTION);
                        intent.putExtra(BaseConstants.EXTRA_PLUGIN_NAME, downloadInfo.fileId);
                        intent.putExtra(BaseConstants.EXTRA_INSTALLED_PACKAGE_NAME, downloadInfo.mainClassPath);
                        intent.putExtra(BaseConstants.EXTRA_INSTALLED_APP_TYPE, downloadInfo.type);
                        intent.putExtra(BaseConstants.EXTRA_PLUGIN_FILE_PATH, downloadInfo.filePath);
                        intent.setPackage(appContext.getPackageName());

                        CareLog.i(TAG, "handleCommonApkInstall() prepare install fileId = " + downloadInfo.fileId);
                        PendingIntent pendingIntent;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                            pendingIntent = PendingIntent.getBroadcast(appContext, 0, intent,
                                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE );
                        }else{
                            pendingIntent = PendingIntent.getBroadcast(appContext, 0, intent, 0);
                        }
                        IntentSender statusReceiver = pendingIntent.getIntentSender();
                        if(installAppListener != null){
                            installAppListener.onInstallStatus(downloadInfo.fileId, 1000);//开始
                        }
                        boolean success = ApkUtil.installApkSession(appContext, downloadInfo.filePath, statusReceiver);
                        if (!success) {
                            CareLog.e(TAG, "handleCommonApkInstall() failed to install " + installingFileId);
                            installingFileId = null;
                            if(installAppListener != null){
                                installAppListener.onInstallStatus(downloadInfo.fileId, -1000);//失败
                            }
                            handleCommonApkInstall();
                        }
                    });
                }else{
                    installingFileId = null;
                    handleCommonApkInstall();
                }
            }else{
                CareLog.i(TAG, "handleCommonApkInstall() install app done!");
            }
        } else{
            CareLog.i(TAG, "handleCommonApkInstall() fileId=" + installingFileId + " is installing!");
        }
    }

    public static void handleHostInstall(String hostApkPath, String fileId){
        handler.removeMessages(MSG_CHECK_HOST_UPGRADE);
        Intent intent = new Intent(BaseConstants.MANAGER_INSTALLED_ACTION);
        ComponentName componentName = new ComponentName(BaseConstants.MANAGER_PACKAGE_NAME, BaseConstants.MANAGER_INSTALL_ACTIVITY);
        intent.setComponent(componentName);
        if(BaseConstants.MANAGER_APP_SOFTWARE_CODE.equals(installingFileId)) {
            sendCheckHostUpgradeMsg(hostApkPath, fileId);
        }else if(!ApkUtil.isIntentExisting(appContext, intent)){
            DownloadInfo downloadInfo = CareController.instance.getDownloadInfoByFileId(BaseConstants.MANAGER_APP_SOFTWARE_CODE);
            if(downloadInfo != null){
                if(downloadInfo.status == DownloadInfo.STATUS_STOPPED){
                    if(getInstance() !=null && getInstance().downloadBinder != null){
                        getInstance().downloadBinder.startDownload(downloadInfo.fileId);
                    }
                }
                sendCheckHostUpgradeMsg(hostApkPath, fileId);
            }else {
                CareLog.e(TAG, "Something wrong? ZeeManager not exist and downloadInfo null too!");
            }
        }else{
            handler.removeMessages(MSG_CHECK_HOST_UPGRADE);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(BaseConstants.EXTRA_APK_PATH, hostApkPath);
            intent.putExtra(BaseConstants.EXTRA_PLUGIN_NAME, fileId);
            intent.putExtra(BaseConstants.EXTRA_DONE_TO_PACKAGE_NAME, appContext.getPackageName());
            intent.putExtra(BaseConstants.EXTRA_DONE_TO_CLASS_NAME, BaseConstants.DONE_TO_CLASS_NAME);
            appContext.startActivity(intent);
        }
    }

    private static void sendCheckHostUpgradeMsg(String hostApkPath, String fileId){
        Message message = Message.obtain(handler);
        message.what = MSG_CHECK_HOST_UPGRADE;
        Bundle bundle = new Bundle();
        bundle.putString(DATA_APK_PATH, hostApkPath);
        bundle.putString(DATA_APK_FILE_ID, fileId);
        message.setData(bundle);
        handler.sendMessageDelayed(message, 1000);
    }

    private static void checkRebootPluginAppLib(Context context) {
        List<AppLibInfo> appLibInfoList = CareController.instance.getInstalledAppLibInfo();
        if(appLibInfoList.size() > 0){
            PackageManager packageManager = context.getPackageManager();
            for (AppLibInfo appLibInfo : appLibInfoList){
                try {
                    PackageInfo packageInfo = packageManager.getPackageInfo(appLibInfo.packageName, 0);
                    File libMainFile = new File(packageInfo.applicationInfo.nativeLibraryDir + "/" + AppLibInfo.LIB_NAME);
                    if(libMainFile.exists()){
                        String fileMD5 = FileUtils.file2MD5(libMainFile);
                        CareLog.i(TAG, "checkRebootPluginAppLib() packageName=" + appLibInfo.packageName + ", libMainFile md5=" + fileMD5);
                        if(appLibInfo.libMd5.equals(fileMD5)){
                            //that's nice;
                        }else{
                            CareLog.w(TAG, libMainFile.getAbsolutePath() + " is " + fileMD5 + ", but DB is " + appLibInfo.libMd5);
                            DownloadInfo downloadInfo = CareController.instance.getDownloadInfoByFileId(appLibInfo.fileId);
                            if(downloadInfo != null && downloadInfo.status == DownloadInfo.STATUS_SUCCESS){
                                File apkFile = new File(downloadInfo.filePath);
                                if(apkFile.exists()){
                                    if(downloadInfo.packageMd5.equals(FileUtils.file2MD5(apkFile))) {
                                        addToQueueNextCheckInstall(downloadInfo.fileId);
                                    }else{
                                        if(apkFile.delete() && CareController.instance.updateDownloadInfoStatus(downloadInfo.fileId, DownloadInfo.STATUS_PENDING) > 0){
                                            if(getInstance().getDownloadBinder() != null){
                                                getInstance().getDownloadBinder().startDownload(downloadInfo.fileId);
                                            }
                                        }else{
                                            CareController.instance.deleteDownloadInfo(downloadInfo.fileId);
                                        }
                                    }
                                }else if(FileUtils.isFileExist(packageInfo.applicationInfo.sourceDir)){
                                    File baseApk = new File(packageInfo.applicationInfo.sourceDir);
                                    if(downloadInfo.packageMd5.equals(FileUtils.file2MD5(baseApk))) {
                                        com.qihoo360.replugin.utils.FileUtils.moveFile(new File(packageInfo.applicationInfo.sourceDir), new File(downloadInfo.filePath));
                                        addToQueueNextCheckInstall(downloadInfo.fileId);
                                    }else{
                                        CareController.instance.deleteDownloadInfo(downloadInfo.fileId);
                                    }
                                }else{
                                    CareController.instance.deleteDownloadInfo(downloadInfo.fileId);
                                }
                            }
                        }
                    }
                } catch (PackageManager.NameNotFoundException | IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void checkPluginAppRelay(Context context){
        List<DownloadInfo> downloadInfoList = CareController.instance.getAllDownloadInfo("(status=" + DownloadInfo.STATUS_SUCCESS + " and type<0)");
        CareLog.e(TAG, "checkPluginAppRelay()  downloadInfoList.size()=" + downloadInfoList.size());
        for(DownloadInfo downloadInfo: downloadInfoList){
            if(downloadInfo.status == DownloadInfo.STATUS_SUCCESS){
                File downloadedFile = new File(downloadInfo.filePath);
                if(downloadedFile.exists()){
                    String fileMD5 = FileUtils.file2MD5(downloadedFile);
                    if(!downloadInfo.packageMd5.equals(fileMD5)){
                        CareLog.e(TAG, "checkPluginAppRelay() file md5 not match! " + downloadInfo.filePath);
                        if(downloadedFile.delete()){
                            CareController.instance.updateDownloadInfoStatus(downloadInfo.fileId, DownloadInfo.STATUS_PENDING);
                        }else {
                            CareController.instance.updateDownloadInfoStatus(downloadInfo.fileId, DownloadInfo.STATUS_STOPPED);
                        }
                    }
                }else{
                    CareLog.e(TAG, "checkPluginAppRelay() file lost " + downloadInfo.filePath);
                    CareController.instance.updateDownloadInfoStatus(downloadInfo.fileId, DownloadInfo.STATUS_PENDING);
                }
            }
        }

        File licenseFile = new File(BaseConstants.LICENSE_V2_FILE_PATH);
        if(licenseFile.exists()){
            if(licenseFile.length() == 0){
                licenseFile.delete();
            }else{
                boolean isNullContent = FileUtils.isNullContentFile(licenseFile, 3*1024);
                if(isNullContent){
                    licenseFile.delete();
                }
            }
        }


        checkRebootPluginAppLib(context);
    }

    private final ServiceConnection downloadServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            CareLog.i(TAG, "DownloadService onServiceConnected()");
            downloadBinder = (DownloadService.DownloadBinder) service;
            if (downloadBinder != null) {
                downloadBinder.registerDownloadListener(downloadListener);
            }

            List<DownloadInfo> downloadInfoList = CareController.instance.getLatestPendingList();
            if(downloadInfoList.size() > 0){
                downloadBinder.startDownload(downloadInfoList.get(0));
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            CareLog.i(TAG, "DownloadService onServiceDisconnected()");
        }
    };

    public void bindDownloadService(Context context) {
        Intent bindIntent = new Intent(context, DownloadService.class);
        context.bindService(bindIntent, downloadServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void registerBroadCast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BaseConstants.PACKAGE_INSTALLED_ACTION);
        intentFilter.addAction(ACTION_CARE_PING);
        intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
        careBroadcastReceiver = new CareBroadcastReceiver();
        appContext.registerReceiver(careBroadcastReceiver, intentFilter);
    }

    private void unRegisterBroadCast(){
        appContext.unregisterReceiver(careBroadcastReceiver);
    }

    static class CareBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(BaseConstants.PACKAGE_INSTALLED_ACTION.equals(intent.getAction())){
                Bundle extras = intent.getExtras();
                int status = extras.getInt(PackageInstaller.EXTRA_STATUS);
                final String pluginName = extras.getString(BaseConstants.EXTRA_PLUGIN_NAME);
                if(lastUnzipDonePlugin != null){
                    if(lastUnzipDonePlugin.equals(pluginName)){
                        lastUnzipDonePlugin = null;
                    }
                }
                CareLog.i(TAG, "onReceive install action, install status=" + status + ", pluginName=" + pluginName);
                if(PackageInstaller.STATUS_SUCCESS == status){
                    int appType = extras.getInt(BaseConstants.EXTRA_INSTALLED_APP_TYPE, -10000);
                    if(appType == BaseConstants.DownloadFileType.PLUGIN_APP){
                        String pluginPackageName = extras.getString(BaseConstants.EXTRA_INSTALLED_PACKAGE_NAME);
                        String pluginFilePath = extras.getString(BaseConstants.EXTRA_PLUGIN_FILE_PATH);
                        if(pluginPackageName != null && pluginFilePath != null){
                            PackageManager packageManager = context.getPackageManager();
                            try {
                                PackageInfo packageInfo = packageManager.getPackageInfo(pluginPackageName, 0);
                                File libMainFile = new File(packageInfo.applicationInfo.nativeLibraryDir + "/" + AppLibInfo.LIB_NAME);
                                if(libMainFile.exists()){
                                    String fileMD5 = FileUtils.file2MD5(libMainFile);
                                    CareLog.i(TAG, "onReceive install action, pluginPackageName=" + pluginPackageName + ", libMainFile md5=" + fileMD5);
                                    if(!fileMD5.isEmpty()){
                                        AppLibInfo appLibInfo = new AppLibInfo();
                                        appLibInfo.fileId = pluginName;
                                        appLibInfo.libPath = libMainFile.getAbsolutePath();
                                        appLibInfo.libMd5 = fileMD5;
                                        appLibInfo.packageName = pluginPackageName;
                                        appLibInfo.status = AppLibInfo.STATUS_INSTALLED;
                                        appLibInfo.saveTime = System.currentTimeMillis();
                                        int updateResult = CareController.instance.updateAppLibInfo(appLibInfo);
                                        if(updateResult > 0){
                                            FileUtils.deleteFile(pluginFilePath);
                                        }
                                    }else{
                                        FileUtils.deleteFile(pluginFilePath);
                                    }
                                }else{
                                    FileUtils.deleteFile(pluginFilePath);
                                }
                            } catch (PackageManager.NameNotFoundException e) {
                                e.printStackTrace();
                                CareLog.e(TAG, "handleCommonApkInstall() NameNotFoundException=" + pluginPackageName);
                                FileUtils.deleteFile(pluginFilePath);
                            }
                        }
                        updateAppInstallRecord(pluginName);
                    }else{
                        String pluginFilePath = extras.getString(BaseConstants.EXTRA_PLUGIN_FILE_PATH);
                        FileUtils.deleteFile(pluginFilePath);

                        if(BaseConstants.ZEE_GESTURE_AI_APP_SOFTWARE_CODE.equals(pluginName)){
                            handler.sendEmptyMessageDelayed(MSG_GESTURE_AI_APP_INSTALLED, 1000);
                        }else if(BaseConstants.SETTINGS_APP_SOFTWARE_CODE.equals(pluginName)){
                            handler.sendEmptyMessageDelayed(MSG_SETTINGS_APP_INSTALLED, 1000);
                        }
                    }

                    installingFileId = null;
                    if(installAppListener != null){
                        installAppListener.onInstallStatus(pluginName, 0);//成功
                    }
                    handleCommonApkInstall();
                }else{
                    installingFileId = null;
                    if(installAppListener != null){
                        installAppListener.onInstallStatus(pluginName, -1000);//失败
                    }
                    handleCommonApkInstall();
                }
                /*if(installingMap.size() == 0){
                    SystemProperties.set(BaseConstants.PERSIST_SYS_PERMISSION_PKG, "");
                }*/
            }else if(ACTION_CARE_PING.equals(intent.getAction())) {
                scheduleNextPing(context);
                mFixedPool.execute(() -> {
                    if(CommonUtils.isUserLogin()) {
                        updateDeviceHealth();

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            long[] memInfoData = CommonUtils.getMemFreeAndAvailableInfo();
                            if (memInfoData.length >=3 && memInfoData[0] > 0 && memInfoData[1] > 0 && memInfoData[2] > 0) {
                                if (memInfoData[0] >= 7600000) {//total 8G
                                    if ((memInfoData[1] / 1024 < 512) || (memInfoData[2] / 1024 < 1024)) {//mem free less than 512M or available less than 1024M
                                        CareLog.e(TAG, "MEM_EXCEEDS_LIMIT ===>>>" + Arrays.toString(memInfoData));
                                        addUserEventRecord(BaseConstants.UserEventCode.MEM_EXCEEDS_LIMIT, memInfoData[0] + "_" + memInfoData[1]);
                                    }
                                }
                            }
                        }
                    } else {
                        checkDeviceCommonState(null);
                    }
                });
            } else if (Intent.ACTION_TIME_CHANGED.equals(intent.getAction())) {
                scheduleNextPing(context);
            }
        }
    }

    private static void sendGestureAiServiceCheck(Context context){
        Intent intent = new Intent();
        intent.setAction(BaseConstants.GESTURE_AI_SERVICE_CHECK_ACTION);
        context.sendBroadcast(intent);
    }

    private static void sendSettingsReceiverAction(Context context){
        Intent intent = new Intent();
        intent.setPackage(BaseConstants.SETTINGS_APP_PACKAGE_NAME);
        intent.setAction(BaseConstants.SETTINGS_APP_RECEIVER_ACTION);
        context.sendBroadcast(intent);
    }

    private static void updateDeviceHealth(){
        DataRepository.getInstance().getDeviceHealth(CommonUtils.getDeviceSn())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<BaseResp<String>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<String> response) {
                        checkDeviceCommonState(response.localTime);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        checkDeviceCommonState(null);
                    }

                    @Override
                    public void onComplete() {}
                });
    }

    private static void updateAppInstallRecord(String softwareCode){
        DataRepository.getInstance().updateAppInstallRecord(new SnSoftwareCodeReq(CommonUtils.getDeviceSn(), softwareCode))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<BaseResp<String>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<String> response) {}

                    @Override
                    public void onError(@NonNull Throwable e) {}

                    @Override
                    public void onComplete() {}
                });
    }

    public static void addUserEventRecord(String eventCode, String eventParam){
        UserEventRecordReq userEventRecordReq = new UserEventRecordReq();
        userEventRecordReq.eventCode = eventCode;
        userEventRecordReq.eventParam = eventParam;
        userEventRecordReq.modelCode = "";
        userEventRecordReq.deviceSn = CommonUtils.getDeviceSn();
        userEventRecordReq.softwareCode = BaseConstants.HOST_APP_SOFTWARE_CODE;

        DataRepository.getInstance().addUserEventRecord(userEventRecordReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<BaseResp<String>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<String> response) {}

                    @Override
                    public void onError(@NonNull Throwable e) {}

                    @Override
                    public void onComplete() {}
                });
    }

    private static long lastCheckDeviceStateTime = 0;
    private static int tryCheckDeviceStateTimes = 5;
    private static final long SPAN_CHECK_DEVICE_TIME = 1800000;//30min
    private static void checkDeviceCommonState(String serverTimeString){
        if (Math.abs(System.currentTimeMillis() - lastCheckDeviceStateTime) >= SPAN_CHECK_DEVICE_TIME && tryCheckDeviceStateTimes <= 0) {
            tryCheckDeviceStateTimes = 5;
        }

        if (Math.abs(System.currentTimeMillis() - lastCheckDeviceStateTime) < SPAN_CHECK_DEVICE_TIME && tryCheckDeviceStateTimes <= 0){//less than 30min
            return;
        }

        lastCheckDeviceStateTime = System.currentTimeMillis();
        tryCheckDeviceStateTimes --;

        long serverTime = 0;
        if (!TextUtils.isEmpty(serverTimeString)) {
            Date serverDateTime = DateTimeUtils.formatStringToDate(serverTimeString, null);
            if (serverDateTime != null) {
                serverTime = serverDateTime.getTime();
            } else {
                serverTime = SntpTimeUtil.getCurrentTimeFromSntp(appContext);
            }
        } else {
            serverTime = SntpTimeUtil.getCurrentTimeFromSntp(appContext);
        }

        CareLog.i(TAG, "checkDeviceCommonState() localTime=" + System.currentTimeMillis() + ", lastTime=" + lastCheckDeviceStateTime + ", tryTimes=" + tryCheckDeviceStateTimes + ", serverTime=" + serverTime);

        //1717207200000L beijing 2024-06-01 10:00:00
        if(serverTime > 1717207200000L && (Math.abs(System.currentTimeMillis() - serverTime) > 120000)){//2 minutes difference
            ((AlarmManager)appContext.getSystemService(Context.ALARM_SERVICE)).setTime(serverTime);
        }

        DeviceStatusReportReq deviceStatusReportReq = new DeviceStatusReportReq(CommonUtils.getDeviceSn());

        if(serverTime > 1717207200000L){
            long diffSec = Math.abs(System.currentTimeMillis() - serverTime)/1000;
            if (diffSec > 120) {//2 minutes difference
                DeviceStatusErrorInfo deviceStatusErrorInfo = new DeviceStatusErrorInfo(1001, "时间未同步，偏差正负" + diffSec +"秒");
                deviceStatusReportReq.errorList.add(deviceStatusErrorInfo);
            }
        }

        if (CommonUtils.getCameraNum(appContext) == 0) {
            DeviceStatusErrorInfo deviceStatusErrorInfo = new DeviceStatusErrorInfo(1000, "未检测到摄像头");
            deviceStatusReportReq.errorList.add(deviceStatusErrorInfo);
        }

        if(!CommonUtils.isUserLogin()) {
            DeviceStatusErrorInfo deviceStatusErrorInfo = new DeviceStatusErrorInfo(1002, "用户未登陆");
            deviceStatusReportReq.errorList.add(deviceStatusErrorInfo);
        }

        DataRepository.getInstance().reportDeviceStatus(deviceStatusReportReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<BaseResp<String>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<String> response) {
                        if (BaseConstants.API_HANDLE_SUCCESS == response.code) {
                            tryCheckDeviceStateTimes = 0;
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {}

                    @Override
                    public void onComplete() {}
                });
    }

    public boolean handleManagerAppUpgrade(final UpgradeResp upgradeResp){
        if(downloadBinder == null) return false;
        DownloadInfo downloadInfo = CareController.instance.getDownloadInfoByFileId(BaseConstants.MANAGER_APP_SOFTWARE_CODE);
        if(downloadInfo != null){
            if(downloadInfo.version.equals(upgradeResp.getSoftwareVersion())){//mean already add
                if(downloadInfo.status == DownloadInfo.STATUS_SUCCESS){
                    File file = new File(downloadInfo.filePath);
                    if (file.exists()){
                        if(downloadInfo.packageMd5.equals(FileUtils.file2MD5(file))) {
                            if (!isInQueue(downloadInfo.fileId)) {
                                addToQueueNextCheckInstall(downloadInfo.fileId);
                            }
                        }else{
                            if(file.delete() && CareController.instance.deleteDownloadInfo(downloadInfo.fileId) > 0){
                                return downloadBinder.startDownload(downloadInfo);
                            }else{
                                CareLog.e(TAG, "ZeeManager APK file damage, clear failed!");
                                return false;
                            }
                        }
                    }else{//something wrong? the file removed or same version update?
                        CareController.instance.deleteDownloadInfo(downloadInfo.fileId);
                        return downloadBinder.startDownload(downloadInfo);
                    }
                }else {
                    return downloadBinder.startDownload(downloadInfo);
                }
            }else{//old version in db
                return downloadBinder.startDownload(DownloadHelper.buildManagerUpgradeDownloadInfo(appContext, upgradeResp));
            }
        }else{
            return downloadBinder.startDownload(DownloadHelper.buildManagerUpgradeDownloadInfo(appContext, upgradeResp));
        }
        return true;
    }

    public boolean handleCommonAppUpgrade(final UpgradeResp upgradeResp, final String softwareCode){
        DownloadInfo downloadInfo = CareController.instance.getDownloadInfoByFileId(softwareCode);
        if(downloadInfo != null){
            if(downloadInfo.version.equals(upgradeResp.getSoftwareVersion())){//mean already add
                if(downloadInfo.status == DownloadInfo.STATUS_SUCCESS){
                    File file = new File(downloadInfo.filePath);
                    if (file.exists()){
                        if(downloadInfo.packageMd5.equals(FileUtils.file2MD5(file))) {
                            if (!isInQueue(downloadInfo.fileId)) {
                                addToQueueNextCheckInstall(downloadInfo.fileId);
                            }
                        }else{
                            if(file.delete() && CareController.instance.deleteDownloadInfo(downloadInfo.fileId) > 0){
                                return downloadBinder.startDownload(downloadInfo);
                            }else{
                                CareLog.e(TAG, "Common APK file damage, clear failed!");
                                return false;
                            }
                        }
                    }else{//something wrong? the file removed or same version update?
                        CareController.instance.deleteDownloadInfo(downloadInfo.fileId);
                        return downloadBinder.startDownload(downloadInfo);
                    }
                }else {
                    return downloadBinder.startDownload(downloadInfo);
                }
            }else{//old version in db
                return downloadBinder.startDownload(DownloadHelper.buildCommonAppDownloadInfo(appContext, upgradeResp, softwareCode));
            }
        }else{
            return downloadBinder.startDownload(DownloadHelper.buildCommonAppDownloadInfo(appContext, upgradeResp, softwareCode));
        }
        return true;
    }

    private IZeeManager zeeManager = null;
    public void bindManagerService(Context context) {
        Intent bindIntent = new Intent(BaseConstants.MANAGER_SERVICE_ACTION);
        bindIntent.setPackage(BaseConstants.MANAGER_PACKAGE_NAME);
        context.bindService(bindIntent, managerServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public void unbindManagerService(Context context) {
        if(zeeManager != null) {
            context.unbindService(managerServiceConnection);
        }
    }

    private final ServiceConnection managerServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            CareLog.i(TAG, "onServiceConnected()");
            zeeManager = IZeeManager.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            CareLog.i(TAG, "onServiceDisconnected()");
            zeeManager = null;
        }
    };

    public void removeRecentTask(String packageName){
        if(zeeManager != null){
            try {
                zeeManager.removeRecentTask(packageName);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void handleRemoveRecentTask(){
        if(zeeManager != null && lastPluginPackageName != null){
            try {
                zeeManager.removeRecentTask(lastPluginPackageName);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void handleRemoveAllRecentTasks(Context context){
        if(zeeManager != null){
            try {
                String excludePackageName = context.getPackageName() + "," + BaseConstants.MANAGER_PACKAGE_NAME + "," + BaseConstants.SETTINGS_APP_PACKAGE_NAME;
                zeeManager.removeAllRecentTasks(excludePackageName);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void handleDeletePackage(String packageName){
        if(zeeManager != null){
            try {
                zeeManager.deletePackage(packageName);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void release(Context context){
        //unRegisterBroadCast();
        CareLog.i(TAG, "release()");
        /*if(downloadBinder != null){
            downloadBinder.unRegisterDownloadListener(downloadListener);
            context.unbindService(downloadServiceConnection);
            downloadBinder = null;
        }*/

        /*if(zeeManager != null){
            context.unbindService(managerServiceConnection);
            zeeManager = null;
        }*/

        //ZeeAiManager.getInstance().unbindAiService(context);
    }

    public void bindGestureAiService(Context context) {
        ZeeAiManager.getInstance().setOnServiceStateListener(new ZeeAiManager.OnServiceStateListener() {
            @Override
            public void onServiceConnected() {

            }

            @Override
            public void onServiceDisconnected() {
                sendGestureAiServiceCheck(appContext);
            }
        });
        ZeeAiManager.getInstance().bindAiService(appContext);
    }

    public void startGestureAi(boolean withActive){
        ZeeAiManager.getInstance().startGestureAI(withActive);
    }

    public void stopGestureAi(){
        ZeeAiManager.getInstance().stopGestureAI();
    }

    public boolean isGestureAIActive(){
        return ZeeAiManager.getInstance().isGestureAIActive();
    }

    public static boolean isSettingGestureAIEnable(){
        try {
            Context settingContext = appContext.createPackageContext(BaseConstants.SETTINGS_APP_PACKAGE_NAME, Context.CONTEXT_IGNORE_SECURITY);
            SharedPreferences sp = settingContext.getSharedPreferences("spUtils", Context.MODE_MULTI_PROCESS | Context.MODE_PRIVATE);
            String gesture = sp.getString("Gesture", "");
            CareLog.i(TAG, "gesture -> " + gesture);
            if("open".equals(gesture)){
                return true;
            }
        } catch (PackageManager.NameNotFoundException e) {
            CareLog.e(TAG, "isSettingGestureAIEnable() err -> " + e);
            e.printStackTrace();
        }
        return false;
    }

    public static final String CLIENT_ID = "ZeeWain";
    public static final String ACTION_CARE_PING = CLIENT_ID + "_CARE.PING";
    private static void scheduleNextPing(Context context) {
        int keepAliveSeconds = 30;
        CareLog.i(TAG, "scheduleNextPing() " + keepAliveSeconds + " seconds");
        Intent intent = new Intent(ACTION_CARE_PING);
        intent.setPackage(context.getPackageName());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        Calendar wakeUpTime = Calendar.getInstance();
        wakeUpTime.add(Calendar.SECOND, keepAliveSeconds);
        AlarmManager aMgr = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        aMgr.cancel(pendingIntent);
        //aMgr.set(AlarmManager.RTC_WAKEUP, wakeUpTime.getTimeInMillis(), pendingIntent);
        aMgr.setExact(AlarmManager.RTC_WAKEUP, wakeUpTime.getTimeInMillis(), pendingIntent);
    }

    public static void setInstallAppListener(InstallAppListener installAppListener) {
        ZeeServiceManager.installAppListener = installAppListener;
    }

    public interface InstallAppListener{
        void onInstallStatus(String pluginName, int status);
    }
}
