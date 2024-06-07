package com.zee.launcher.home.ui.direct;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;

import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.Gson;
import com.zee.launcher.home.BuildConfig;
import com.zee.launcher.home.R;
import com.zee.launcher.home.data.DataRepository;

import com.zee.launcher.home.data.protocol.request.PublishReq;

import com.zee.launcher.home.data.protocol.request.UpgradeReq;
import com.zee.launcher.home.data.protocol.response.AkSkResp;
import com.zee.launcher.home.data.protocol.response.AlgorithmInfoResp;
import com.zee.launcher.home.data.protocol.response.ModelInfoResp;
import com.zee.launcher.home.data.protocol.response.PublishResp;

import com.zee.launcher.home.utils.DownloadHelper;
import com.zee.manager.IZeeManager;
import com.zeewain.base.BaseApplication;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.config.SharePrefer;
import com.zeewain.base.model.LoadState;
import com.zeewain.base.ui.BaseActivity;
import com.zeewain.base.utils.ApkUtil;
import com.zeewain.base.utils.CareLog;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.DensityUtils;
import com.zeewain.base.utils.FileUtils;
import com.zeewain.base.utils.NetworkUtil;
import com.zeewain.base.utils.ZipUtils;
import com.zeewain.base.widgets.GradientProgressView;
import com.zeewain.base.widgets.LoadingView;
import com.zeewain.base.widgets.NetworkErrView;
import com.zwn.launcher.host.HostManager;
import com.zwn.lib_download.DownloadListener;
import com.zwn.lib_download.DownloadService;
import com.zwn.lib_download.db.CareController;
import com.zwn.lib_download.model.DownloadInfo;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class DirectLoadingActivity extends BaseActivity {
    private static final String TAG = "DirectLoadingActivity";
    private static final int MSG_DOWNLOAD_ON_PROGRESS = 1;
    private static final int MSG_DOWNLOAD_ON_FAILED = 2;
    private static final int MSG_DOWNLOAD_ON_UPDATE = 3;
    public static final int MSG_START_PLUGIN = 4;
    public static final int MSG_LOADING_OPEN = 5;
    public static final int MSG_LOADING_CLOSE = 6;
    private static final String KEY_PROGRESS = "Progress";

    private String skuId;
    private String currentFileId;
    private String lastVersion;
    private DownloadService.DownloadBinder downloadBinder = null;
    private IZeeManager zeeManager = null;
    private DirectLoadViewModel directLoadViewModel;
    private ConstraintLayout layoutDetailContent;
    private NetworkErrView networkErrViewDetail;
    private LoadingView loadingViewDetail;
    private GradientProgressView gradientProgressViewDetail;

    private final AtomicInteger pendingPrepareCount = new AtomicInteger();
    private static final List<File> unzipFiles = new ArrayList<>();
    private final List<String> modelFileList = new ArrayList<>();
    private final ExecutorService mFixedPool = Executors.newFixedThreadPool(2);
    private static final ConcurrentHashMap<String, String> downloadRelatedDataMap = new ConcurrentHashMap<>(5);
    private static volatile boolean isUnzipCalled = false;
    private static volatile boolean isPrepareStart = false;
    private int failedTryCount = 3;
    private MyBroadcastReceiver myBroadcastReceiver;
    private DownloadInfo currentDownloadInfo;
    private final float downloadProgressRatio = 0.9f;

    private final MyHandler handler = new MyHandler(Looper.myLooper(), this);
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadBinder = (DownloadService.DownloadBinder) service;
            if (downloadBinder != null) {
                downloadBinder.registerDownloadListener(downloadListener);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    private final DownloadListener downloadListener = new DownloadListener() {
        @Override
        public void onProgress(String fileId, int progress, long loadedSize, long fileSize) {
            if (fileId.equals(currentFileId)) {
                Message message = Message.obtain(handler);
                message.what = MSG_DOWNLOAD_ON_PROGRESS;
                Bundle bundle = new Bundle();
                bundle.putInt(KEY_PROGRESS, progress);
                message.setData(bundle);
                handler.sendMessage(message);
            }
        }

        @Override
        public void onSuccess(String fileId, int type, File file) {
            if (fileId.equals(currentFileId)) {
                if (BuildConfig.FLAVOR.equals("single")) {
                    handleCommonApkInstall(fileId);
                }
                handler.sendEmptyMessage(MSG_DOWNLOAD_ON_UPDATE);
            }else if (downloadRelatedDataMap.containsKey(fileId)) {
                downloadRelatedDataMap.remove(fileId);
                checkToUnzip();
            }
        }

        @Override
        public void onFailed(String fileId, int type, int code) {
            if (fileId.equals(currentFileId)) {
                handler.sendEmptyMessage(MSG_DOWNLOAD_ON_FAILED);
            } else if (downloadRelatedDataMap.containsKey(fileId)) {
                failedTryCount--;
                if (failedTryCount > 0) {
                    downloadBinder.startDownload(fileId);
                } else {
                    CareLog.e(TAG, "onFailed() " + fileId);
                    runOnUiThread(() -> {
                        showToast("加载资源失败！");
                        delayFinish();
                    });
                }
            } else {
                runOnUiThread(() -> {
                    if (!NetworkUtil.isNetworkAvailable(DirectLoadingActivity.this)) {
                        showToast("网络连接异常！加载资源失败！");
                        delayFinish();
                    }
                });
            }
        }

        @Override
        public void onPaused(String fileId) {
        }

        @Override
        public void onCancelled(String fileId) {
        }

        @Override
        public void onUpdate(String fileId) {
            if (fileId.equals(currentFileId)) {
                handler.sendEmptyMessage(MSG_DOWNLOAD_ON_UPDATE);
            }
        }
    };

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

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        savedInstanceState.setClassLoader(getClass().getClassLoader());
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DensityUtils.autoWidth(getApplication(), this);
        setContentView(R.layout.activity_direct_loading);
        skuId = getIntent().getStringExtra("skuId");
        if (TextUtils.isEmpty(skuId)) {
            finish();
            return;
        }else if(!CommonUtils.isUserLogin()){
            logoutClear();
            finish();
            return;
        }

        DirectLoadViewModelFactory factory = new DirectLoadViewModelFactory(DataRepository.getInstance());
        directLoadViewModel = new ViewModelProvider(this, factory).get(DirectLoadViewModel.class);

        layoutDetailContent = findViewById(R.id.layout_detail_content);
        networkErrViewDetail = findViewById(R.id.networkErrView_detail);
        loadingViewDetail = findViewById(R.id.loadingView_detail);
        gradientProgressViewDetail = findViewById(R.id.gradient_progress_view_detail);

        bindService();
        bindManagerService();
        registerBroadCast();

        initClickListener();
        initViewObservable();
        directLoadViewModel.reqProDetailInfo(skuId);
    }

    private void initClickListener() {
        networkErrViewDetail.setRetryClickListener(() -> directLoadViewModel.reqProDetailInfo(skuId));
    }

    private void initViewObservable() {
        directLoadViewModel.mldDetailLoadState.observe(this, loadState -> {
            if (LoadState.Loading == loadState) {
                networkErrViewDetail.setVisibility(View.GONE);
                layoutDetailContent.setVisibility(View.GONE);
                loadingViewDetail.setVisibility(View.VISIBLE);
                loadingViewDetail.startAnim();
            } else if (LoadState.Success == loadState) {
                if (directLoadViewModel.proDetailResp.getPutawayStatus() == 1) {
                    directLoadViewModel.getPublishVersionInfo(new PublishReq(directLoadViewModel.proDetailResp.getSoftwareCode()));
                } else {
                    loadingViewDetail.stopAnim();
                    loadingViewDetail.setVisibility(View.GONE);
                    showToast("已下架");
                    delayFinish();
                }
            } else {
                if(directLoadViewModel.tryReqProDetailInfoTimes > 0) {
                    directLoadViewModel.reqProDetailInfo(skuId);
                }else {
                    loadingViewDetail.stopAnim();
                    loadingViewDetail.setVisibility(View.GONE);
                    //networkErrViewDetail.setVisibility(View.VISIBLE);
                    showToast("请求数据失败，请检查下网络是否异常！");
                    delayFinish();
                }
            }
        });

        directLoadViewModel.mPublishState.observe(this, loadState -> {
            if (LoadState.Loading == loadState) {
                if(View.VISIBLE != loadingViewDetail.getVisibility()){
                    networkErrViewDetail.setVisibility(View.GONE);
                    layoutDetailContent.setVisibility(View.GONE);
                    loadingViewDetail.setVisibility(View.VISIBLE);
                    loadingViewDetail.startAnim();
                }
            } else if (LoadState.Success == loadState) {
                PublishResp publishResp = directLoadViewModel.publishResp;
                if ((publishResp != null) && (publishResp.getSoftwareInfo() != null)) {
                    currentFileId = publishResp.getSoftwareInfo().getSoftwareCode();
                    lastVersion = publishResp.getSoftwareVersion();
                    gradientProgressViewDetail.setProgress(0);
                    DownloadInfo dbDownloadInfo = CareController.instance.getDownloadInfoByFileId(currentFileId);
                    if (dbDownloadInfo != null) {
                        if (dbDownloadInfo.loadedSize > 0) {
                            int progress = (int) (((dbDownloadInfo.loadedSize * 1.0f / dbDownloadInfo.fileSize) * 100) * downloadProgressRatio);
                            gradientProgressViewDetail.setProgress(progress);
                        }
                        if (dbDownloadInfo.status == DownloadInfo.STATUS_SUCCESS || dbDownloadInfo.status == DownloadInfo.STATUS_STOPPED) {
                            if(!dbDownloadInfo.version.equals(lastVersion)) {
                                directLoadViewModel.getUpgradeVersionInfo(new UpgradeReq(dbDownloadInfo.version, publishResp.getSoftwareInfo().getSoftwareCode()));
                            }else{
                                handLoadApp();
                            }
                        }else{
                            loadingViewDetail.stopAnim();
                            loadingViewDetail.setVisibility(View.GONE);
                            layoutDetailContent.setVisibility(View.VISIBLE);
                        }
                    } else {
                        handLoadApp();
                    }
                } else {
                    loadingViewDetail.stopAnim();
                    loadingViewDetail.setVisibility(View.GONE);
                    showToast("未发布");
                    delayFinish();
                }
            } else {
                if(directLoadViewModel.tryReqPublishVersionInfoTimes > 0 && directLoadViewModel.proDetailResp.getPutawayStatus() == 1) {
                    directLoadViewModel.getPublishVersionInfo(new PublishReq(directLoadViewModel.proDetailResp.getSoftwareCode()));
                }else {
                    loadingViewDetail.stopAnim();
                    loadingViewDetail.setVisibility(View.GONE);
                    //networkErrViewDetail.setVisibility(View.VISIBLE);
                    showToast("请求数据失败，请检查下网络是否异常！");
                    delayFinish();
                }
            }
        });

        directLoadViewModel.mUpgradeState.observe(this, dataLoadState -> {
            if (LoadState.Loading == dataLoadState.loadState) {
                if(View.VISIBLE != loadingViewDetail.getVisibility()){
                    networkErrViewDetail.setVisibility(View.GONE);
                    layoutDetailContent.setVisibility(View.GONE);
                    loadingViewDetail.setVisibility(View.VISIBLE);
                    loadingViewDetail.startAnim();
                }
            } else if (LoadState.Success == dataLoadState.loadState) {
                handLoadApp();
            } else {
                if(directLoadViewModel.tryReqUpgradeVersionInfoTimes > 0) {
                    directLoadViewModel.getUpgradeVersionInfo(dataLoadState.data);
                }else {
                    loadingViewDetail.stopAnim();
                    loadingViewDetail.setVisibility(View.GONE);
                    //networkErrViewDetail.setVisibility(View.VISIBLE);
                    showToast("请求数据失败，请检查下网络是否异常！");
                    delayFinish();
                }
            }
        });
    }

    private void registerBroadCast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BaseConstants.PACKAGE_INSTALLED_ACTION);
        myBroadcastReceiver = new MyBroadcastReceiver();
        registerReceiver(myBroadcastReceiver, intentFilter);
    }

    private Context getUseContext() {
        return HostManager.getUseContext(this);
    }

    private void bindService() {
        Intent bindIntent = new Intent();
        bindIntent.setComponent(new ComponentName(HostManager.getUseContext(this).getPackageName(), DownloadService.class.getName()));
        HostManager.getUseContext(this).bindService(bindIntent, serviceConnection, BIND_AUTO_CREATE);
    }

    public void bindManagerService() {
        Intent bindIntent = new Intent(BaseConstants.MANAGER_SERVICE_ACTION);
        bindIntent.setPackage(BaseConstants.MANAGER_PACKAGE_NAME);
        HostManager.getUseContext(this).bindService(bindIntent, managerServiceConnection, BIND_AUTO_CREATE);
    }

    public void unbindManagerService() {
        HostManager.getUseContext(this).unbindService(managerServiceConnection);
    }

    private boolean handleAlgorithmLib() {
        if (downloadBinder != null) {
            List<AlgorithmInfoResp> algorithmInfoList = directLoadViewModel.publishResp.getRelevancyAlgorithmVersions();
            if (directLoadViewModel.upgradeResp != null) {
                algorithmInfoList = directLoadViewModel.upgradeResp.getRelevancyAlgorithmVersions();//是否应该并集？
            }
            if (algorithmInfoList != null) {
                for (int i = 0; i < algorithmInfoList.size(); i++) {
                    AlgorithmInfoResp algorithmInfoResp = algorithmInfoList.get(i);
                    boolean successHandle = handleModelBin(algorithmInfoResp.relevancyModelVersions);
                    if (!successHandle) {
                        return false;
                    }
                    DownloadInfo downloadAlgorithm = CareController.instance.getDownloadInfoByFileId(algorithmInfoResp.versionId);
                    if (downloadAlgorithm == null) {
                        boolean addOk = downloadBinder.startDownload(DownloadHelper.buildAlgorithmDownloadInfo(getUseContext(), algorithmInfoResp));
                        if (!addOk) {
                            return false;
                        }
                    } else if (downloadAlgorithm.status == DownloadInfo.STATUS_STOPPED) {
                        downloadBinder.startDownload(downloadAlgorithm.fileId);
                    }
                }
            }
            return true;
        }
        return false;
    }

    private boolean handleModelBin(List<ModelInfoResp> relatedModelList) {
        if (relatedModelList != null) {
            for (int i = 0; i < relatedModelList.size(); i++) {
                ModelInfoResp modelInfoResp = relatedModelList.get(i);
                DownloadInfo downloadModel = CareController.instance.getDownloadInfoByFileId(modelInfoResp.versionId);
                if (downloadModel == null) {
                    boolean addOk = downloadBinder.startDownload(DownloadHelper.buildModelDownloadInfo(getUseContext(), modelInfoResp));
                    if (!addOk) {
                        return false;
                    }
                } else if (downloadModel.status == DownloadInfo.STATUS_STOPPED) {
                    downloadBinder.startDownload(downloadModel.fileId);
                }
            }
        }
        return true;
    }

    private void handLoadApp() {
        loadingViewDetail.stopAnim();
        loadingViewDetail.setVisibility(View.GONE);
        layoutDetailContent.setVisibility(View.VISIBLE);
        if (handleAlgorithmLib()) {
            if (directLoadViewModel.upgradeResp != null) {//处理升级
                DownloadInfo downloadInfo = DownloadHelper.buildUpgradeDownloadInfo(getUseContext(), directLoadViewModel.proDetailResp, directLoadViewModel.publishResp, directLoadViewModel.upgradeResp);
                boolean success = downloadBinder.startDownload(downloadInfo);
                if (success) {
                    directLoadViewModel.upgradeResp = null;
                }else{
                    showToast("更新失败！");
                    delayFinish();
                }
            } else {
                handleDownload();
            }
        }else{
            showToast("算法或模型添加失败！");
            delayFinish();
        }
    }

    private void handleDownload() {
        if (downloadBinder != null) {
            DownloadInfo dbDownloadInfo = CareController.instance.getDownloadInfoByFileId(currentFileId);
            if (dbDownloadInfo == null) {
                DownloadInfo downloadInfo = DownloadHelper.buildDownloadInfo(getUseContext(), directLoadViewModel.proDetailResp, directLoadViewModel.publishResp);
                downloadBinder.startDownload(downloadInfo);
            } else {
                if (dbDownloadInfo.status == DownloadInfo.STATUS_LOADING || dbDownloadInfo.status == DownloadInfo.STATUS_PENDING) {
                    //downloadBinder.pauseDownload(dbDownloadInfo.fileId);
                } else if (dbDownloadInfo.status == DownloadInfo.STATUS_STOPPED) {
                    downloadBinder.startDownload(dbDownloadInfo);
                } else {
                    String lastPluginPackageName = HostManager.getLastPluginPackageName();
                    if(null != lastPluginPackageName
                            && !dbDownloadInfo.mainClassPath.equals(lastPluginPackageName)){
                        removeRecentTask(lastPluginPackageName);
                    }
                    startLoadingApplication();
                }
            }
        }
    }

    private void updateDownloadTip() {
        DownloadInfo dbDownloadInfo = CareController.instance.getDownloadInfoByFileId(currentFileId);
        if (dbDownloadInfo != null && dbDownloadInfo.status == DownloadInfo.STATUS_SUCCESS) {
            sendProgressMsg((int)(100  + (100 * (1-downloadProgressRatio))/5), 0);
            sendProgressMsg((int)(100  + (100 * (1-downloadProgressRatio))/5 * 2), 800);
            sendProgressMsg((int)(100  + (100 * (1-downloadProgressRatio))/5 * 3), 2000);
            sendProgressMsg((int)(100  + (100 * (1-downloadProgressRatio))/5 * 4), 3000);
            String lastPluginPackageName = HostManager.getLastPluginPackageName();
            if(null != lastPluginPackageName
                    && !dbDownloadInfo.mainClassPath.equals(lastPluginPackageName)){
                removeRecentTask(lastPluginPackageName);
            }
            startLoadingApplication();
        }
    }

    private void updateDownloadTipOnFailed() {
        if (!NetworkUtil.isNetworkAvailable(DirectLoadingActivity.this)) {
            showToast("网络连接异常！加载资源失败！");
        } else {
            showToast("加载资源失败！");
        }
        delayFinish();
    }

    private void updateDownloadTip(int progress) {
        gradientProgressViewDetail.setProgress((progress * downloadProgressRatio));
    }

    public void removeRecentTask(String packageName) {
        if (zeeManager != null) {
            try {
                zeeManager.removeRecentTask(packageName);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }


    private void startLoadingApplication() {
        currentDownloadInfo = CareController.instance.getDownloadInfoByFileId(currentFileId);
        CareLog.i(TAG, "currentDownloadInfo " + currentDownloadInfo);

        isPrepareStart = false;
        isUnzipCalled = false;
        pendingPrepareCount.set(1);

        initDownloadRelatedDataMap(currentDownloadInfo.relyIds);
        checkRelyDownloadData();
        nextToDo(currentFileId, currentDownloadInfo);
    }

    private void nextToDo(String pluginName, DownloadInfo downloadInfo){
        File pluginFile = new File(downloadInfo.filePath);
        if(pluginName.equals(HostManager.getInstallingFileId())){
            CareLog.i(TAG, "app installing");
        }else if (pluginFile.exists()) {//new version
            if(downloadInfo.packageMd5.equals(FileUtils.file2MD5(pluginFile))) {
                if (BuildConfig.FLAVOR.equals("single")) {
                    handleCommonApkInstall(downloadInfo.fileId);
                }else{
                    HostManager.installPlugin(downloadInfo.fileId);
                }
            }else{
                pluginFile.delete();
                CareController.instance.deleteDownloadInfo(downloadInfo.fileId);
                CareLog.e(TAG, "app file damage！ packageName=" + downloadInfo.mainClassPath);
                showToast("应用文件损坏！");
                delayFinish();
            }
        }else{
            if(ApkUtil.isAppInstalled(this, downloadInfo.mainClassPath)){
                prepareStartPlugin();
            }else{
                CareLog.e(TAG, "app file not exists and app not installed");
                showToast("应用不存在！");
                CareController.instance.deleteDownloadInfo(downloadInfo.fileId);
                delayFinish();
            }
        }
    }

    private void delayFinish() {
        loadingViewDetail.postDelayed(() -> finish(), 800);
    }

    private void initDownloadRelatedDataMap(String relyIds){
        if(relyIds != null && !relyIds.isEmpty()) {
            String[] relyIdArray = relyIds.split(",");
            for (String relyId : relyIdArray) {
                DownloadInfo downloadLib = CareController.instance.getDownloadInfoByFileId(relyId);
                if(downloadLib == null){
                    CareLog.e(TAG, "downloadLib null, relyId=" + relyId);
                    finish();
                    return;
                }else {
                    String relyModelIds = downloadLib.relyIds;
                    if(relyModelIds != null && !relyModelIds.isEmpty()){
                        String[] relyModelIdArray = relyModelIds.split(",");
                        for (String relyModelId : relyModelIdArray) {
                            DownloadInfo downloadModel = CareController.instance.getDownloadInfoByFileId(relyModelId);
                            if(downloadModel == null){
                                CareLog.e(TAG, "downloadModel null, relyModelId=" + relyModelId);
                                finish();
                                return;
                            }else {
                                modelFileList.add(downloadModel.filePath);
                                if (downloadModel.status != DownloadInfo.STATUS_SUCCESS) {
                                    downloadRelatedDataMap.put(downloadModel.fileId, downloadModel.fileId);
                                }
                            }
                        }
                    }
                    if (downloadLib.status != DownloadInfo.STATUS_SUCCESS) {
                        downloadRelatedDataMap.put(downloadLib.fileId, downloadLib.fileId);
                    }
                }
            }
        }
    }

    private void checkRelyDownloadData(){
        Iterator<Map.Entry<String, String>> iterator = downloadRelatedDataMap.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<String, String> entry = iterator.next();
            String fileId = entry.getKey();
            DownloadInfo downloadLib = CareController.instance.getDownloadInfoByFileId(fileId);
            if (downloadLib.status == DownloadInfo.STATUS_SUCCESS) {
                iterator.remove();
            }else if(downloadLib.status == DownloadInfo.STATUS_STOPPED) {
                downloadBinder.startDownload(fileId);
            }
        }
        checkToUnzip();
    }

    private void checkToUnzip(){
        if(downloadRelatedDataMap.size() == 0 && isPrepareStart){
            handler.removeMessages(MSG_DOWNLOAD_ON_PROGRESS);
            sendProgressMsg((int)(100  + (100 * (1-downloadProgressRatio))/5 * 5), 0);
            sendProgressMsg((int)(100  + (100 * (1-downloadProgressRatio))/5 * 6), 1000);
            unzipShareLib();
        }
    }

    private synchronized void prepareStartPlugin(){
        if(!isPrepareStart) {
            isPrepareStart = true;
            directLoadViewModel.reqStartCourseware();
            checkToUnzip();
        }
    }

    private synchronized void unzipShareLib() {
        if (!isUnzipCalled) {
            isUnzipCalled = true;
            mFixedPool.execute(() -> {
                try {
                    if(currentDownloadInfo == null) return;
                    long currentTime = System.currentTimeMillis();
                    PackageManager packageManager = getPackageManager();
                    PackageInfo packageInfo = packageManager.getPackageInfo(currentDownloadInfo.mainClassPath, 0);

                    String desDir = BaseConstants.PLUGIN_MODEL_PATH + "/";

                    if (modelFileList.size() > 0) {
                        boolean copyModelSuccess = FileUtils.copyFilesTo(modelFileList, desDir);
                        if (!copyModelSuccess) {
                            CareLog.e(TAG, "copyModel() failed !");
                            runOnUiThread(() -> {
                                showToast("拷贝模型失败！");
                                delayFinish();
                            });
                            return;
                        }
                    }
                    CareLog.i(TAG, "copyModel() cost time=" + (System.currentTimeMillis() - currentTime));

                    if (!currentDownloadInfo.fileId.equals(HostManager.getLastUnzipDonePlugin())) {
                        CareLog.e(TAG, "unzipShareLib() start==>");
                        HostManager.setLastUnzipDonePlugin(null);
                        if (unzipFiles.size() > 0) {
                            for (int i = 0; i < unzipFiles.size(); i++) {
                                File file = unzipFiles.get(i);
                                if (file.exists()) {
                                    boolean delResult = file.delete();
                                    if (!delResult) CareLog.e(TAG, "file.delete err " + file.getPath());
                                }
                            }
                        }
                        unzipFiles.clear();
                        if (currentDownloadInfo.relyIds != null && !currentDownloadInfo.relyIds.isEmpty()) {
                            String[] relyIdArray = currentDownloadInfo.relyIds.split(",");
                            for (String relyId : relyIdArray) {
                                DownloadInfo downloadLib = CareController.instance.getDownloadInfoByFileId(relyId);
                                if (downloadLib.status == DownloadInfo.STATUS_SUCCESS) {
                                    unzipFiles.addAll(ZipUtils.unzipFile(downloadLib.filePath, packageInfo.applicationInfo.nativeLibraryDir));
                                } else {
                                    CareLog.e(TAG, "something wrong!!! unzipShareLib() downloadLib not ready " + downloadLib);
                                }
                            }
                        }
                        HostManager.setLastUnzipDonePlugin(currentDownloadInfo.fileId);
                        CareLog.e(TAG, "unzipShareLib() cost time=" + (System.currentTimeMillis() - currentTime) + "<<<<-done-");
                    } else {
                        CareLog.e(TAG, "no need unzipShareLib() <-----");
                    }

                } catch (Exception exception) {
                    exception.printStackTrace();
                    CareLog.e(TAG, "unzipShareLib() failed! " + exception);
                } finally {
                    isUnzipCalled = false;
                    decrementCountAndCheck();
                }
            });
        }
    }

    private void decrementCountAndCheck(){
        int newPendingCount = pendingPrepareCount.decrementAndGet();
        if(newPendingCount == 0){
            CareLog.i(TAG, "decrementCountAndCheck() Done");
            sendMsgToStartPlugin();
        }
    }

    private void sendMsgToStartPlugin(){
        handler.sendEmptyMessageDelayed(MSG_START_PLUGIN, 100);
    }

    private void startPluginActivity() {
        doStartApplicationWithPackageName(currentDownloadInfo.mainClassPath);
    }

    private boolean isUseAuthV2() {
        CareLog.i(TAG, "isUseAuthV2()" + currentDownloadInfo.relyIds);
        String relyIds = currentDownloadInfo.relyIds;
        if (relyIds != null && !relyIds.isEmpty()) {
            List<DownloadInfo> downloadList = CareController.instance.getAllDownloadInfo("fileId in (" + relyIds + ") and type=" + BaseConstants.DownloadFileType.SHARE_LIB);
            for (DownloadInfo downloadInfo : downloadList) {
                CareLog.i(TAG, "rely downloadLib ==>" + downloadInfo);
                if (downloadInfo.version.compareToIgnoreCase("0.6.7") > 0) {
                    CareLog.i(TAG, "rely downloadLib is over 0.6.7");
                    return true;
                }
            }
        }
        return false;
    }

    private void logoutClear(){
        BaseApplication.userToken = null;
        HostManager.logoutClear();
        HostManager.gotoLoginPage(this);
    }

    public AkSkResp getAkSkResp(){
        String akSkInfoString = HostManager.getHostSpString(SharePrefer.akSkInfo, null);
        if (akSkInfoString != null && !akSkInfoString.isEmpty()) {
            Gson gson = new Gson();
            return gson.fromJson(akSkInfoString, AkSkResp.class);
        }
        return null;
    }

    private void doStartApplicationWithPackageName(String packageName) {
        PackageInfo packageinfo = null;
        try {
            packageinfo = getPackageManager().getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageinfo == null) {
            CareLog.e(TAG, "getPackageInfo() " + packageName + ", null");
            showToast("获取应用信息失败！");
            delayFinish();
            return;
        }

        AkSkResp akSkResp = getAkSkResp();
        if(akSkResp == null){
            showToast("AkSk获取失败！");
            logoutClear();
            delayFinish();
            return;
        }

        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        //resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(packageinfo.packageName);

        // 通过getPackageManager()的queryIntentActivities方法遍历
        List<ResolveInfo> resolveInfoList = getPackageManager().queryIntentActivities(resolveIntent, 0);
        if (resolveInfoList == null || resolveInfoList.size() == 0) {
            showToast("获取应用入口失败！");
            delayFinish();
            return;
        }
        ResolveInfo resolveInfo = resolveInfoList.iterator().next();
        if (resolveInfo != null) {
            //String packageName = resolveinfo.activityInfo.packageName;
            // 这个就是我们要找的该APP的LAUNCHER的Activity[组织形式：packageName.mainActivityName]
            String className = resolveInfo.activityInfo.name;

            Intent intent = new Intent(Intent.ACTION_MAIN);
            //intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.putExtra(BaseConstants.EXTRA_AUTH_AK_CODE, akSkResp.akCode);
            intent.putExtra(BaseConstants.EXTRA_AUTH_SK_CODE, akSkResp.skCode);
            intent.putExtra(BaseConstants.EXTRA_HOST_PKG, packageName);
            if (isUseAuthV2()) {
                intent.putExtra(BaseConstants.EXTRA_AUTH_URI, (BaseApplication.baseUrl + BaseApplication.basePath + "/auth"));
                intent.putExtra(BaseConstants.EXTRA_LICENSE_PATH, BaseConstants.LICENSE_V2_FILE_PATH);
            } else {
                intent.putExtra(BaseConstants.EXTRA_AUTH_URI, (BaseApplication.baseUrl + BaseApplication.basePath + "/auth/client/get-license"));
                intent.putExtra(BaseConstants.EXTRA_LICENSE_PATH, BaseConstants.LICENSE_FILE_PATH);
            }
            intent.putExtra(BaseConstants.EXTRA_MODELS_DIR_PATH, BaseConstants.PLUGIN_MODEL_PATH + "/");

            String userToken = HostManager.getHostSpString(SharePrefer.userToken, "");
            intent.putExtra(BaseConstants.EXTRA_AUTH_TOKEN, userToken);

            HostManager.setLastPluginPackageName(packageName);
            if (HostManager.isGestureAiEnable()) {
                HostManager.stopGestureAi();
            }

            if (!isFinishing() && !isDestroyed() && isWindowOnFocus)
            {
                ComponentName cn = new ComponentName(packageName, className);
                intent.setComponent(cn);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                HostManager.getUseContext(this).startActivity(intent);
                finish();
            }
        }
    }

    public boolean isWindowOnFocus = false;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        isWindowOnFocus = hasFocus;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (isUnzipCalled) {
                return !isUnzipCalled;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            CareLog.i(TAG, "onReceive() intent=" + intent);
            Bundle extras = intent.getExtras();
            if (BaseConstants.PACKAGE_INSTALLED_ACTION.equals(intent.getAction())) {
                int status = extras.getInt(PackageInstaller.EXTRA_STATUS);
                String message = extras.getString(PackageInstaller.EXTRA_STATUS_MESSAGE);
                String pluginName = extras.getString(BaseConstants.EXTRA_PLUGIN_NAME);
                CareLog.i(TAG, "PACKAGE_INSTALLED_ACTION status=" + status + ", message=" + message + ", pluginName=" + pluginName);
                if (currentDownloadInfo != null && currentDownloadInfo.fileId.equals(pluginName)) {
                    switch (status) {
                        case PackageInstaller.STATUS_PENDING_USER_ACTION:
                            // This test app isn't privileged, so the user has to confirm the install.
                            Intent confirmIntent = (Intent) extras.get(Intent.EXTRA_INTENT);
                            startActivity(confirmIntent);
                            break;

                        case PackageInstaller.STATUS_SUCCESS:
                            if (BuildConfig.FLAVOR.equals("single")) {
                                String pluginFilePath = extras.getString(BaseConstants.EXTRA_PLUGIN_FILE_PATH);
                                FileUtils.deleteFile(pluginFilePath);
                            }
                            handler.postDelayed(() -> prepareStartPlugin(), 1000);

                            break;

                        case PackageInstaller.STATUS_FAILURE:
                        case PackageInstaller.STATUS_FAILURE_ABORTED:
                        case PackageInstaller.STATUS_FAILURE_BLOCKED:
                        case PackageInstaller.STATUS_FAILURE_CONFLICT:
                        case PackageInstaller.STATUS_FAILURE_INCOMPATIBLE:
                        case PackageInstaller.STATUS_FAILURE_INVALID:
                        case PackageInstaller.STATUS_FAILURE_STORAGE:
                            showToast("安装应用失败！ 状态码：" + status);
                            delayFinish();
                            break;
                        default:
                            showToast("安装应用失败！ 未知状态码：" + status);
                            delayFinish();
                    }
                }
            }
        }
    }

    public void sendProgressMsg(int progress, long delay){
        Message message = Message.obtain(handler);
        message.what = MSG_DOWNLOAD_ON_PROGRESS;
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_PROGRESS, progress);
        message.setData(bundle);
        handler.sendMessageDelayed(message, delay);
    }

    public static class MyHandler extends Handler {
        private final WeakReference<Activity> mActivity;

        public MyHandler(Looper looper, Activity activity) {
            super(looper);
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Activity activity = mActivity.get();
            if (activity != null) {
                if (msg.what == MSG_DOWNLOAD_ON_PROGRESS) {
                    int progress = msg.getData().getInt(KEY_PROGRESS);
                    ((DirectLoadingActivity) activity).updateDownloadTip(progress);
                } else if (msg.what == MSG_DOWNLOAD_ON_FAILED) {
                    ((DirectLoadingActivity) activity).updateDownloadTipOnFailed();
                } else if (msg.what == MSG_DOWNLOAD_ON_UPDATE) {
                    ((DirectLoadingActivity) activity).updateDownloadTip();
                } else if (msg.what == MSG_START_PLUGIN) {
                    ((DirectLoadingActivity) activity).startPluginActivity();
                } else if (msg.what == MSG_LOADING_OPEN) {
                    ((DirectLoadingActivity) activity).loadingViewDetail.setVisibility(View.VISIBLE);
                    ((DirectLoadingActivity) activity).loadingViewDetail.setText("努力加载中...");
                    ((DirectLoadingActivity) activity).loadingViewDetail.startAnim();
                } else if (msg.what == MSG_LOADING_CLOSE) {
                    ((DirectLoadingActivity) activity).loadingViewDetail.setVisibility(View.GONE);
                    ((DirectLoadingActivity) activity).loadingViewDetail.stopAnim();
                    ((DirectLoadingActivity) activity).layoutDetailContent.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        CareLog.e(TAG, "onDestroy()");
        handler.removeCallbacksAndMessages(null);
        loadingViewDetail.stopAnim();
        if (downloadBinder != null) {
            downloadBinder.unRegisterDownloadListener(downloadListener);
            HostManager.getUseContext(this).unbindService(serviceConnection);
        }
        if (zeeManager != null) {
            unbindManagerService();
            zeeManager = null;
        }
        if (myBroadcastReceiver != null) {
            unregisterReceiver(myBroadcastReceiver);
        }
        downloadRelatedDataMap.clear();
        super.onDestroy();
    }

    private synchronized void handleCommonApkInstall(String fileId){
        final DownloadInfo downloadInfo = CareController.instance.getDownloadInfoByFileId(fileId);
        if(downloadInfo != null && downloadInfo.status == DownloadInfo.STATUS_SUCCESS &&  FileUtils.isFileExist(downloadInfo.filePath)) {
            mFixedPool.execute(() -> {
                Intent intent = new Intent();
                intent.setAction(BaseConstants.PACKAGE_INSTALLED_ACTION);
                intent.putExtra(BaseConstants.EXTRA_PLUGIN_NAME, downloadInfo.fileId);
                intent.putExtra(BaseConstants.EXTRA_INSTALLED_PACKAGE_NAME, downloadInfo.mainClassPath);
                intent.putExtra(BaseConstants.EXTRA_INSTALLED_APP_TYPE, downloadInfo.type);
                intent.putExtra(BaseConstants.EXTRA_PLUGIN_FILE_PATH, downloadInfo.filePath);
                intent.setPackage(getPackageName());

                CareLog.i(TAG, "handleCommonApkInstall() prepare install fileId = " + downloadInfo.fileId);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE );
                IntentSender statusReceiver = pendingIntent.getIntentSender();
                boolean success = ApkUtil.installApkSession(this, downloadInfo.filePath, statusReceiver);
                if (!success) {
                    CareLog.e(TAG, "handleCommonApkInstall() failed to install " + fileId);
                }
            });
        }

    }

}


