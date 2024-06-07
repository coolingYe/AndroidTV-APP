package com.zwn.launcher.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.zee.unity.IExternalCallback;
import com.zee.unity.IExternalServices;
import com.zee.launcher.login.data.protocol.response.AkSkResp;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.config.SharePrefer;
import com.zeewain.base.data.protocol.request.PublishReq;
import com.zeewain.base.data.protocol.request.UpgradeReq;
import com.zeewain.base.data.protocol.response.AlgorithmInfoResp;
import com.zeewain.base.data.protocol.response.BaseResp;
import com.zeewain.base.data.protocol.response.ModelInfoResp;
import com.zeewain.base.data.protocol.response.PublishResp;
import com.zeewain.base.data.protocol.response.UpgradeResp;
import com.zeewain.base.utils.ApkUtil;
import com.zeewain.base.utils.CareLog;
import com.zeewain.base.utils.FileUtils;
import com.zeewain.base.utils.SPUtils;
import com.zeewain.base.utils.ZipUtils;
import com.zwn.launcher.data.DataRepository;
import com.zwn.launcher.utils.DownloadHelper;
import com.zwn.lib_download.DownloadListener;
import com.zwn.lib_download.db.CareController;
import com.zwn.lib_download.model.DownloadInfo;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class ExternalServices extends Service {
    private static final String TAG = "ExternalServices";
    private static final String INTENT_EXTRA_PACKAGE_NAME = "PackageName";
    private static final ConcurrentHashMap<String, ExternalClientCallback> externalClientCallBackMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Long> reqStartDownloadPluginAppMap = new ConcurrentHashMap<>();
    private static final ExecutorService mFixedPool = Executors.newFixedThreadPool(2);
    private static final List<File> unzipFiles = new ArrayList<>();
    private static final List<String> modelFileList = new ArrayList<>();
    private static volatile boolean isUnzipCalled = false;
    private static String lastUnzipDonePlugin;
    private static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        CareLog.i(TAG, "onCreate()");
        appContext = getApplicationContext();
        ZeeServiceManager.getInstance().registerDownloadListener(downloadListener);
        ZeeServiceManager.setInstallAppListener(installAppListener);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        CareLog.i(TAG, "onBind() " + intent.getStringExtra(INTENT_EXTRA_PACKAGE_NAME));
        return externalServiceBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        CareLog.i(TAG, "onRebind() " + intent.getStringExtra(INTENT_EXTRA_PACKAGE_NAME));
    }

    @Override
    public boolean onUnbind(Intent intent) {
        String packageName = intent.getStringExtra(INTENT_EXTRA_PACKAGE_NAME);
        CareLog.i(TAG, "onUnbind() " + packageName);
        if(packageName != null) {
            externalClientCallBackMap.remove(packageName);
        }
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        CareLog.i(TAG, "onDestroy() ");
        externalClientCallBackMap.clear();
        ZeeServiceManager.setInstallAppListener(null);
        ZeeServiceManager.getInstance().unRegisterDownloadListener(downloadListener);
        super.onDestroy();
    }

    private final ZeeServiceManager.InstallAppListener installAppListener = (pluginName, status) -> {
        if(pluginName.equals(lastUnzipDonePlugin)){
            lastUnzipDonePlugin = null;
        }
        for (String key : externalClientCallBackMap.keySet()) {
            ExternalClientCallback clientCallback = externalClientCallBackMap.get(key);
            if(clientCallback != null){
                try {
                    clientCallback.iExternalCallback.onInstallStatus(pluginName, status);
                } catch (RemoteException e) {
                    CareLog.e(TAG, "onInstallStatus() err " + e);
                }
            }
        }
    };

    private final DownloadListener downloadListener = new DownloadListener() {
        @Override
        public void onProgress(String fileId, int progress, long loadedSize, long fileSize) {
            for (String key : externalClientCallBackMap.keySet()) {
                ExternalClientCallback clientCallback = externalClientCallBackMap.get(key);
                if(clientCallback != null){
                    try {
                        clientCallback.iExternalCallback.onProgress(fileId, progress, loadedSize, fileSize);
                    } catch (RemoteException e) {
                        CareLog.e(TAG, "onProgress() err " + e);
                    }
                }
            }
        }

        @Override
        public void onSuccess(String fileId, int type, File file) {
            for (String key : externalClientCallBackMap.keySet()) {
                ExternalClientCallback clientCallback = externalClientCallBackMap.get(key);
                if(clientCallback != null){
                    try {
                        clientCallback.iExternalCallback.onSuccess(fileId, type, file.getAbsolutePath());
                    } catch (RemoteException e) {
                        CareLog.e(TAG, "onSuccess() err " + e);
                    }
                }
            }
        }

        @Override
        public void onFailed(String fileId, int type, int code) {
            for (String key : externalClientCallBackMap.keySet()) {
                ExternalClientCallback clientCallback = externalClientCallBackMap.get(key);
                if(clientCallback != null){
                    try {
                        clientCallback.iExternalCallback.onFailed(fileId, type, code);
                    } catch (RemoteException e) {
                        CareLog.e(TAG, "onFailed() err " + e);
                    }
                }
            }
        }

        @Override
        public void onPaused(String fileId) {
            for (String key : externalClientCallBackMap.keySet()) {
                ExternalClientCallback clientCallback = externalClientCallBackMap.get(key);
                if(clientCallback != null){
                    try {
                        clientCallback.iExternalCallback.onPaused(fileId);
                    } catch (RemoteException e) {
                        CareLog.e(TAG, "onPaused() err " + e);
                    }
                }
            }
        }

        @Override
        public void onCancelled(String fileId) {
            for (String key : externalClientCallBackMap.keySet()) {
                ExternalClientCallback clientCallback = externalClientCallBackMap.get(key);
                if(clientCallback != null){
                    try {
                        clientCallback.iExternalCallback.onCancelled(fileId);
                    } catch (RemoteException e) {
                        CareLog.e(TAG, "onCancelled() err " + e);
                    }
                }
            }
        }

        @Override
        public void onUpdate(String fileId) {
            for (String key : externalClientCallBackMap.keySet()) {
                ExternalClientCallback clientCallback = externalClientCallBackMap.get(key);
                if(clientCallback != null){
                    try {
                        clientCallback.iExternalCallback.onUpdate(fileId);
                    } catch (RemoteException e) {
                        CareLog.e(TAG, "onUpdate() err " + e);
                    }
                }
            }
        }
    };

    private final IExternalServices.Stub externalServiceBinder = new IExternalServices.Stub() {

        @Override
        public void addExternalCallback(String uid, IExternalCallback callback) throws RemoteException {
            ExternalClientCallback externalClientCallback = new ExternalClientCallback(uid, callback);
            externalClientCallBackMap.put(uid, externalClientCallback);
            callback.asBinder().linkToDeath(externalClientCallback,0);

            CareLog.i(TAG, "addExternalCallback() externalClientCallBackMap size=" + externalClientCallBackMap.size());
            for (String key : externalClientCallBackMap.keySet()) {
                ExternalClientCallback clientCallback = externalClientCallBackMap.get(key);
                CareLog.i(TAG, "externalClientCallBackMap contains " + clientCallback);
            }
        }

        @Override
        public void removeExternalCallback(String uid, IExternalCallback callback) throws RemoteException {
            ExternalClientCallback externalClientCallback = externalClientCallBackMap.remove(uid);
            if(externalClientCallback != null) {
                externalClientCallback.iExternalCallback.asBinder().unlinkToDeath(externalClientCallback, 0);
            }

            CareLog.i(TAG, "removeExternalCallback() externalClientCallBackMap size=" + externalClientCallBackMap.size());
        }

        @Override
        public boolean prepareStartPluginApp(String uid, String fileId) throws RemoteException {
            DownloadInfo downloadInfo = CareController.instance.getDownloadInfoByFileId(fileId);
            if(downloadInfo != null && downloadInfo.status == DownloadInfo.STATUS_SUCCESS){
                return unzipShareLib(uid, downloadInfo);
            }
            return false;
        }

        @Override
        public int startPluginApp(String uid, String fileId) throws RemoteException {
            return startAppBySoftwareCode(fileId);
        }

        @Override
        public void reqStartDownloadPluginApp(String uid, String fileId) throws RemoteException {
            if(ZeeServiceManager.getInstance().downloadBinder != null){
                if(isProcessingReqStartDownload(fileId)){
                    sendStartDownloadMsg(uid, fileId, StartDownloadStatus.PROCESSING, "上次的请求正在处理中");
                }else {
                    startDownloadPluginApp(uid, fileId);
                }
            }else{
                sendStartDownloadMsg(uid, fileId, StartDownloadStatus.FAILED, "下载服务连接失败！");
            }
        }
    };

    private static boolean isInInstallingQueue(String fileId){
        if(fileId.equals(ZeeServiceManager.installingFileId)){
            return true;
        }else if(ZeeServiceManager.isInQueue(fileId)){
            return true;
        }
        return false;
    }

    private static void installPlugin(String fileId){
        if(!ZeeServiceManager.isInQueue(fileId)) {
            ZeeServiceManager.addToQueueNextCheckInstall(fileId);
        }
    }

    private static void sendStartDownloadMsg(String uid, String softwareCode, int status, String errMsg){
        Long startDownloadReqTimeLong;
        if(status == StartDownloadStatus.PROCESSING){
            startDownloadReqTimeLong = reqStartDownloadPluginAppMap.get(softwareCode);
        } else {
            startDownloadReqTimeLong = reqStartDownloadPluginAppMap.remove(softwareCode);
        }
        if(startDownloadReqTimeLong == null){
            CareLog.e(TAG, "====>>> reqStartDownloadPluginAppMap not contains " + softwareCode);
        }else{
            CareLog.i(TAG, "sendStartDownloadMsg() softwareCode=" + softwareCode + ", status=" + status
                    + ", errMsg=" + errMsg + ", costTime=" + (System.currentTimeMillis() - startDownloadReqTimeLong));
        }

        JSONObject baseJson = new JSONObject();
        try {
            baseJson.put("cmd", 207);
            baseJson.put("seq", 1);
            baseJson.put("state", 0);
            JSONObject bodyJson = new JSONObject();
            bodyJson.put("softwareCode", softwareCode);
            bodyJson.put("status", status);
            bodyJson.put("errMsg", errMsg);
            baseJson.put("body", bodyJson);

            ExternalClientCallback clientCallback = externalClientCallBackMap.get(uid);
            if(clientCallback != null){
                clientCallback.iExternalCallback.onMessage(baseJson.toString());
            }
        } catch (JSONException e) {
            CareLog.e(TAG, "JSONException() " + e);
        } catch (RemoteException e) {
            CareLog.e(TAG, "RemoteException() err " + e);
        }
    }

    private static synchronized boolean isProcessingReqStartDownload(final String softwareCode){
        return reqStartDownloadPluginAppMap.containsKey(softwareCode);
    }

    private static void startDownloadPluginApp(final String uid, final String softwareCode){
        reqStartDownloadPluginAppMap.put(softwareCode, System.currentTimeMillis());

        DataRepository.getInstance().getPublishedVersionInfo(new PublishReq(softwareCode))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<BaseResp<PublishResp>>() {
                    @Override
                    public void onNext(BaseResp<PublishResp> response) {
                        if (response.code == BaseConstants.API_HANDLE_SUCCESS){
                            PublishResp publishResp = response.data;
                            if (publishResp != null && publishResp.getSoftwareInfo() != null) {
                                handlePluginAppPublishResp(uid, response.data, true);
                            }else {
                                sendStartDownloadMsg(uid, softwareCode, StartDownloadStatus.FAILED, "接口返回数据异常！");
                            }
                        }else {
                            sendStartDownloadMsg(uid, softwareCode, StartDownloadStatus.FAILED, response.message);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        sendStartDownloadMsg(uid, softwareCode, StartDownloadStatus.FAILED, "网络异常！");
                    }

                    @Override
                    public void onComplete() {}
                });
    }

    private static void handlePluginAppPublishResp(final String uid, PublishResp publishResp, boolean isCareUpgrade){
        final String currentFileId = publishResp.getSoftwareInfo().getSoftwareCode();
        String lastVersion = publishResp.getSoftwareVersion();
        DownloadInfo dbDownloadInfo = CareController.instance.getDownloadInfoByFileId(currentFileId);
        if (dbDownloadInfo != null) {
            handleSlaveMasterDescUpdate(uid, dbDownloadInfo);//更新依赖包的主包描述
            if (dbDownloadInfo.status == DownloadInfo.STATUS_SUCCESS) {
                if (isCareUpgrade && !dbDownloadInfo.version.equals(lastVersion)) {
                    getPluginAppLatestVersion(uid, dbDownloadInfo.version, publishResp);
                } else {//版本相同，监测算法依赖、模型依赖更新
                    if(!handleAlgorithmLib(publishResp, null)){
                        sendStartDownloadMsg(uid, currentFileId, StartDownloadStatus.FAILED, "启动算法或者模型下载失败！");
                    }else{//已下载，依赖已添加成功；
                        CareLog.i(TAG, "reqStartDownloadPluginApp() " + dbDownloadInfo.fileId + " downloaded! relies added!");
                        if(isAlgorithmLibReady(publishResp, null)){
                            CareLog.i(TAG, "reqStartDownloadPluginApp() " + dbDownloadInfo.fileId + " relies downloaded!");
                            File downloadFile = new File(dbDownloadInfo.filePath);
                            if(downloadFile.exists()){
                                CareLog.i(TAG, "reqStartDownloadPluginApp() " + dbDownloadInfo.fileId + " downloaded filePath=" + dbDownloadInfo.filePath);
                                if(!isInInstallingQueue(currentFileId)){
                                    installPlugin(currentFileId);
                                }
                                sendStartDownloadMsg(uid, currentFileId, StartDownloadStatus.INSTALLING, "正在安装应用！");
                            }else{
                                if(ApkUtil.isAppInstalled(appContext, dbDownloadInfo.mainClassPath)){
                                    CareLog.i(TAG, "reqStartDownloadPluginApp() " + dbDownloadInfo.fileId + " installed！relies downloaded!");
                                    sendStartDownloadMsg(uid, currentFileId, StartDownloadStatus.READY, "");
                                }else{
                                    CareLog.e(TAG, "reqStartDownloadPluginApp() " + dbDownloadInfo.fileId + "====>> file lost? " + dbDownloadInfo.filePath);
                                    sendStartDownloadMsg(uid, currentFileId, StartDownloadStatus.FAILED, "未知异常，应用文件丢失！");
                                }
                            }
                        }else{//有依赖未下载成功
                            sendStartDownloadMsg(uid, currentFileId, StartDownloadStatus.LOADING_RELIES, "正在下载依赖内容！");
                        }
                    }
                }
            } else if (dbDownloadInfo.status == DownloadInfo.STATUS_STOPPED) {
                if (isCareUpgrade && !dbDownloadInfo.version.equals(lastVersion)) {
                    getPluginAppLatestVersion(uid, dbDownloadInfo.version, publishResp);
                } else {
                    if (!handleAlgorithmLib(publishResp, null)) {
                        sendStartDownloadMsg(uid, currentFileId, StartDownloadStatus.FAILED, "启动算法或者模型下载失败！");
                    } else {
                        if (!ZeeServiceManager.getInstance().getDownloadBinder().startDownload(currentFileId)) {
                            sendStartDownloadMsg(uid, currentFileId, StartDownloadStatus.FAILED, "启动下载失败！");
                        } else {//进入下载中...
                            sendStartDownloadMsg(uid, currentFileId, StartDownloadStatus.LOADING, "正在下载中！");
                        }
                    }
                }
            } else {//正在下载
                sendStartDownloadMsg(uid, currentFileId, StartDownloadStatus.LOADING, "正在下载中！");
            }
        } else{//未有下载
            if (!handleAlgorithmLib(publishResp, null)) {
                sendStartDownloadMsg(uid, currentFileId, StartDownloadStatus.FAILED, "启动算法或者模型下载失败！");
            } else {//依赖已添加成功，开始下载课件
                DownloadInfo nextAddDownloadInfo = DownloadHelper.buildDownloadInfo(publishResp, appContext);
                nextAddDownloadInfo.describe = uid + ";";
                if (!ZeeServiceManager.getInstance().getDownloadBinder().startDownload(nextAddDownloadInfo)) {
                    sendStartDownloadMsg(uid, currentFileId, StartDownloadStatus.FAILED, "启动下载失败！");
                } else {//开始下载成功，进入下载中...
                    sendStartDownloadMsg(uid, currentFileId, StartDownloadStatus.LOADING, "正在下载中！");
                }
            }
        }
    }

    private static void getPluginAppLatestVersion(final String uid, String originalSoftwareVersion, PublishResp publishResp){
        final String currentFileId = publishResp.getSoftwareInfo().getSoftwareCode();
        DataRepository.getInstance().getUpgradeVersionInfo(new UpgradeReq(originalSoftwareVersion, publishResp.getSoftwareInfo().getSoftwareCode()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<BaseResp<UpgradeResp>>() {
                    @Override
                    public void onNext(BaseResp<UpgradeResp> response) {
                        if(response.code == BaseConstants.API_HANDLE_SUCCESS){
                            UpgradeResp upgradeResp = response.data;
                            if (upgradeResp != null && upgradeResp.getVersionId() != null && !upgradeResp.getVersionId().isEmpty()) {
                                if(upgradeResp.isForcible()){
                                    if (handleAlgorithmLib(publishResp, upgradeResp)) {
                                        DownloadInfo downloadInfo = DownloadHelper.buildUpgradeDownloadInfo(publishResp, upgradeResp, appContext);
                                        if (!ZeeServiceManager.getInstance().getDownloadBinder().startDownload(downloadInfo)) {
                                            sendStartDownloadMsg(uid, currentFileId, StartDownloadStatus.FAILED, "启动下载失败！");
                                        } else {
                                            sendStartDownloadMsg(uid, currentFileId, StartDownloadStatus.LOADING, "正在下载中！");
                                        }
                                    } else {
                                        sendStartDownloadMsg(uid, currentFileId, StartDownloadStatus.FAILED, "启动算法或者模型下载失败！");
                                    }
                                }else{
                                    handlePluginAppPublishResp(uid, publishResp, false);
                                }
                            }else {
                                sendStartDownloadMsg(uid, currentFileId, StartDownloadStatus.FAILED, "接口返回数据异常！");
                            }
                        }else{
                            sendStartDownloadMsg(uid, currentFileId, StartDownloadStatus.FAILED, response.message);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        sendStartDownloadMsg(uid, currentFileId, StartDownloadStatus.FAILED, "网络异常！");
                    }

                    @Override
                    public void onComplete() {}
                });
    }

    private static boolean handleAlgorithmLib(PublishResp publishResp, UpgradeResp upgradeResp){
        List<AlgorithmInfoResp> algorithmInfoList = publishResp.getRelevancyAlgorithmVersions();
        if(upgradeResp != null ){
            algorithmInfoList = upgradeResp.getRelevancyAlgorithmVersions();//是否应该并集？
        }
        if(algorithmInfoList != null) {
            for (int i = 0; i < algorithmInfoList.size(); i++) {
                AlgorithmInfoResp algorithmInfoResp = algorithmInfoList.get(i);
                boolean successHandle = handleModelBin(algorithmInfoResp.relevancyModelVersions);
                if(!successHandle){
                    return false;
                }
                DownloadInfo downloadAlgorithm = CareController.instance.getDownloadInfoByFileId(algorithmInfoResp.versionId);
                if (downloadAlgorithm == null) {
                    boolean addOk = ZeeServiceManager.getInstance().getDownloadBinder().startDownload(DownloadHelper.buildAlgorithmDownloadInfo(algorithmInfoResp, appContext));
                    if (!addOk) {
                        return false;
                    }
                } else if (downloadAlgorithm.status == DownloadInfo.STATUS_STOPPED) {
                    ZeeServiceManager.getInstance().getDownloadBinder().startDownload(downloadAlgorithm.fileId);
                }
            }
        }
        return true;
    }

    private static boolean handleModelBin(List<ModelInfoResp> relatedModelList){
        if(relatedModelList != null){
            for (int i = 0; i < relatedModelList.size(); i++) {
                ModelInfoResp modelInfoResp = relatedModelList.get(i);
                DownloadInfo downloadModel = CareController.instance.getDownloadInfoByFileId(modelInfoResp.versionId);
                if (downloadModel == null) {
                    boolean addOk = ZeeServiceManager.getInstance().getDownloadBinder().startDownload(DownloadHelper.buildModelDownloadInfo(modelInfoResp, appContext));
                    if (!addOk) {
                        return false;
                    }
                } else if (downloadModel.status == DownloadInfo.STATUS_STOPPED) {
                    ZeeServiceManager.getInstance().getDownloadBinder().startDownload(downloadModel.fileId);
                }
            }
        }
        return true;
    }

    private static void handleSlaveMasterDescUpdate(String uid, DownloadInfo slaveDownloadInfo){
        if (TextUtils.isEmpty(slaveDownloadInfo.describe)) {
            String desc =  uid + ";";
            CareController.instance.updateDownloadInfoDesc(slaveDownloadInfo.fileId, desc);
        } else {
            if (!slaveDownloadInfo.describe.contains(uid)) {
                String desc = uid + ";" + slaveDownloadInfo.describe;
                CareController.instance.updateDownloadInfoDesc(slaveDownloadInfo.fileId, desc);
            }
        }
    }

    private static boolean isAlgorithmLibReady(PublishResp publishResp, UpgradeResp upgradeResp){
        List<AlgorithmInfoResp> algorithmInfoList = publishResp.getRelevancyAlgorithmVersions();
        if(upgradeResp != null ){
            algorithmInfoList = upgradeResp.getRelevancyAlgorithmVersions();//是否应该并集？
        }
        if(algorithmInfoList != null) {
            for (int i = 0; i < algorithmInfoList.size(); i++) {
                AlgorithmInfoResp algorithmInfoResp = algorithmInfoList.get(i);
                if(!isModelBinReady(algorithmInfoResp.relevancyModelVersions)){
                    return false;
                }
                DownloadInfo downloadAlgorithm = CareController.instance.getDownloadInfoByFileId(algorithmInfoResp.versionId);
                if (downloadAlgorithm == null) {
                    return false;
                } else if (downloadAlgorithm.status != DownloadInfo.STATUS_SUCCESS) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isModelBinReady(List<ModelInfoResp> relatedModelList){
        if(relatedModelList != null){
            for (int i = 0; i < relatedModelList.size(); i++) {
                ModelInfoResp modelInfoResp = relatedModelList.get(i);
                DownloadInfo downloadModel = CareController.instance.getDownloadInfoByFileId(modelInfoResp.versionId);
                if (downloadModel == null) {
                    return false;
                } else if (downloadModel.status != DownloadInfo.STATUS_SUCCESS) {
                    return false;
                }
            }
        }
        return true;
    }

    private static synchronized boolean unzipShareLib(String uid, DownloadInfo pluginAppDownloadInfo){
        if(!isUnzipCalled) {
            isUnzipCalled = true;
            mFixedPool.execute(() -> {
                try {
                    if(!initDownloadRelatedData(pluginAppDownloadInfo.relyIds)){
                        prepareStartResp(uid, pluginAppDownloadInfo.fileId, PrepareStartAppStatus.FAILED);
                        return;
                    }
                    long currentTime = System.currentTimeMillis();
                    PackageManager packageManager = appContext.getPackageManager();
                    PackageInfo packageInfo = packageManager.getPackageInfo(pluginAppDownloadInfo.mainClassPath, 0);

                    String desDir = BaseConstants.PLUGIN_MODEL_PATH +"/";

                    if(modelFileList.size() > 0){
                        boolean copyModelSuccess = FileUtils.copyFilesTo(modelFileList, desDir);
                        if(!copyModelSuccess){
                            CareLog.e(TAG, "copyModel() failed !");
                            prepareStartResp(uid, pluginAppDownloadInfo.fileId, PrepareStartAppStatus.FAILED);
                            return;
                        }
                    }
                    CareLog.i(TAG, "copyModel() cost time=" + (System.currentTimeMillis() - currentTime));

                    //if(!pluginAppDownloadInfo.fileId.equals(lastUnzipDonePlugin)) {
                    CareLog.e(TAG, "unzipShareLib() start==>");
                    lastUnzipDonePlugin = null;
                    if(unzipFiles.size() > 0){
                        for (int i = 0; i < unzipFiles.size(); i++) {
                            File file = unzipFiles.get(i);
                            if (file.exists()) {
                                boolean delResult = file.delete();
                                if(!delResult) CareLog.e(TAG, "file.delete err " + file.getPath());
                            }
                        }
                    }
                    unzipFiles.clear();
                    if (pluginAppDownloadInfo.relyIds != null && !pluginAppDownloadInfo.relyIds.isEmpty()) {
                        String[] relyIdArray = pluginAppDownloadInfo.relyIds.split(",");
                        for (String relyId : relyIdArray) {
                            DownloadInfo downloadLib = CareController.instance.getDownloadInfoByFileId(relyId);
                            if (downloadLib.status == DownloadInfo.STATUS_SUCCESS) {
                                unzipFiles.addAll(ZipUtils.unzipFile(downloadLib.filePath, packageInfo.applicationInfo.nativeLibraryDir));
                            } else {
                                CareLog.e(TAG, "something wrong!!! unzipShareLib() downloadLib not ready " + downloadLib);
                            }
                        }
                    }
                    lastUnzipDonePlugin = pluginAppDownloadInfo.fileId;
                    CareLog.e(TAG, "unzipShareLib() cost time=" + (System.currentTimeMillis() - currentTime) + "<<<<-done-");
                    prepareStartResp(uid, pluginAppDownloadInfo.fileId, PrepareStartAppStatus.SUCCESS);
                } catch (Exception exception) {
                    exception.printStackTrace();
                    CareLog.e(TAG, "unzipShareLib() failed! " + exception);
                    prepareStartResp(uid, pluginAppDownloadInfo.fileId, PrepareStartAppStatus.FAILED);
                } finally {
                    isUnzipCalled = false;
                }
            });
            return true;
        }
        return false;
    }

    private static boolean initDownloadRelatedData(String relyIds){
        if(relyIds != null && !relyIds.isEmpty()) {
            String[] relyIdArray = relyIds.split(",");
            for (String relyId : relyIdArray) {
                DownloadInfo downloadLib = CareController.instance.getDownloadInfoByFileId(relyId);
                if(downloadLib == null || downloadLib.status != DownloadInfo.STATUS_SUCCESS){
                    CareLog.e(TAG, "downloadLib null or not downloaded, relyId=" + relyId);
                    return false;
                }else {
                    String relyModelIds = downloadLib.relyIds;
                    if(relyModelIds != null && !relyModelIds.isEmpty()){
                        String[] relyModelIdArray = relyModelIds.split(",");
                        for (String relyModelId : relyModelIdArray) {
                            DownloadInfo downloadModel = CareController.instance.getDownloadInfoByFileId(relyModelId);
                            if(downloadModel == null || downloadModel.status != DownloadInfo.STATUS_SUCCESS){
                                CareLog.e(TAG, "downloadModel null or not downloaded, relyModelId=" + relyModelId);
                                return false;
                            }else {
                                modelFileList.add(downloadModel.filePath);
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private static void prepareStartResp(String uid, String softwareCode, int status){
        ExternalClientCallback clientCallback = externalClientCallBackMap.get(uid);
        if(clientCallback != null){
            try {
                clientCallback.iExternalCallback.onPrepareStartStatus(softwareCode, status);
            } catch (RemoteException e) {
                CareLog.e(TAG, "onPrepareStartStatus() err " + e);
            }
        }
    }

    private static synchronized int startAppBySoftwareCode(String softwareCode) {
        DownloadInfo downloadInfo = CareController.instance.getDownloadInfoByFileId(softwareCode);
        if(downloadInfo == null || downloadInfo.status != DownloadInfo.STATUS_SUCCESS){
            CareLog.e(TAG, "startAppBySoftwareCode() " + downloadInfo);
            return StartAppStatus.NOT_RECORD;
        }

        String userToken = SPUtils.getInstance().getString(SharePrefer.userToken);
        if(userToken == null || userToken.isEmpty()){
            CareLog.e(TAG, "startAppBySoftwareCode() err userToken " + userToken);
            return StartAppStatus.NOT_LOGIN;
        }

        String akSkInfoString = SPUtils.getInstance().getString(SharePrefer.akSkInfo);
        if(akSkInfoString == null || akSkInfoString.isEmpty()){
            CareLog.e(TAG, "startAppBySoftwareCode() err akSkInfoString " + akSkInfoString);
            return StartAppStatus.NOT_LOGIN;
        }

        AkSkResp akSkResp = new Gson().fromJson(akSkInfoString, AkSkResp.class);
        if(akSkResp == null) {
            CareLog.e(TAG, "startAppBySoftwareCode() err akSkResp null");
            return StartAppStatus.NOT_LOGIN;
        }

        String packageName = downloadInfo.mainClassPath;
        PackageInfo packageinfo = null;
        try {
            packageinfo = appContext.getPackageManager().getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            CareLog.e(TAG, "startAppBySoftwareCode() getPackageInfo " + e);
        }
        if (packageinfo == null) {
            CareLog.e(TAG, "startAppBySoftwareCode() getPackageInfo " + packageName + " is null");
            return StartAppStatus.NOT_FOUND;
        }

        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.setPackage(packageinfo.packageName);
        List<ResolveInfo> resolveInfoList = appContext.getPackageManager().queryIntentActivities(resolveIntent, 0);
        if(resolveInfoList == null || resolveInfoList.size() == 0){
            return StartAppStatus.NOT_FOUND;
        }
        ResolveInfo resolveInfo = resolveInfoList.iterator().next();
        if (resolveInfo == null) {
            return StartAppStatus.NOT_FOUND;
        }

        String className = resolveInfo.activityInfo.name;
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.putExtra(BaseConstants.EXTRA_AUTH_AK_CODE, akSkResp.akCode);
        intent.putExtra(BaseConstants.EXTRA_AUTH_SK_CODE, akSkResp.skCode);
        intent.putExtra(BaseConstants.EXTRA_HOST_PKG, packageName);
        intent.putExtra(BaseConstants.EXTRA_AUTH_URI, (BaseConstants.baseUrl + BaseConstants.basePath + "/auth"));
        intent.putExtra(BaseConstants.EXTRA_LICENSE_PATH, BaseConstants.LICENSE_V2_FILE_PATH);
        intent.putExtra(BaseConstants.EXTRA_MODELS_DIR_PATH, BaseConstants.PLUGIN_MODEL_PATH + "/");
        intent.putExtra(BaseConstants.EXTRA_AUTH_TOKEN, userToken);
        ComponentName cn = new ComponentName(packageName, className);
        intent.setComponent(cn);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        appContext.startActivity(intent);
        return StartAppStatus.SUCCESS;
    }

    static class ExternalClientCallback implements IBinder.DeathRecipient{
        public String uid;
        public IExternalCallback iExternalCallback;

        public ExternalClientCallback(String uid, IExternalCallback iExternalCallback) {
            this.uid = uid;
            this.iExternalCallback = iExternalCallback;
        }

        @Override
        public void binderDied() {
            CareLog.e(TAG, "binderDied() uid=" + uid);
            externalClientCallBackMap.remove(uid);
        }

        @Override
        public String toString() {
            return "{" +
                    "uid='" + uid + '\'' +
                    '}';
        }
    }

    static class StartDownloadStatus{
        public static final int PROCESSING = -2;        //上一次的请求还在处理中
        public static final int FAILED = -1;            //处理失败，网络异常、接口异常、启动下载失败、下载异常
        public static final int READY = 0;              //已下载，已安装
        public static final int LOADING = 1;            //正在下载
        public static final int LOADING_RELIES = 2;     //正在下载依赖内容
        public static final int INSTALLING = 10;        //正在安装
    }

    static class PrepareStartAppStatus{
        public static final int FAILED = -1;            //准备失败
        public static final int SUCCESS = 0;            //准备成功
    }

    static class StartAppStatus{
        public static final int NOT_LOGIN = -3;         //未登录
        public static final int NOT_FOUND = -2;         //未找到安装的应用
        public static final int NOT_RECORD = -1;        //未有下载记录
        public static final int SUCCESS = 0;            //OK
    }
}
