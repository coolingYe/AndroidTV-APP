package com.zee.launcher.home.ui.direct;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Settings;
import android.text.TextUtils;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.Gson;
import com.zee.launcher.home.BuildConfig;
import com.zee.launcher.home.MainActivity;
import com.zee.launcher.home.R;
import com.zee.launcher.home.data.DataRepository;

import com.zee.launcher.home.data.protocol.request.PublishReq;

import com.zee.launcher.home.data.protocol.request.UpgradeReq;
import com.zee.launcher.home.data.protocol.response.AkSkResp;
import com.zee.launcher.home.data.protocol.response.AlgorithmInfoResp;
import com.zee.launcher.home.data.protocol.response.ModelInfoResp;
import com.zee.launcher.home.data.protocol.response.PublishResp;
import com.zee.launcher.home.dialog.UpgradeTipDialog;

import com.zee.launcher.home.service.FloatingCameraService;
import com.zee.launcher.home.utils.DownloadHelper;
import com.zee.launcher.home.utils.LoadingPluginHelper;
import com.zee.manager.IZeeManager;
import com.zeewain.base.BaseApplication;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.config.SharePrefer;
import com.zeewain.base.model.LoadState;
import com.zeewain.base.ui.BaseActivity;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.NetworkUtil;
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
import java.util.List;

public class DirectLoadingActivity extends BaseActivity {
    private final static int REQUEST_CODE_LOGIN = 1000;
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
    private boolean isProductRelease = true;
    private boolean isLastGestureAIActive = false;
    private DownloadService.DownloadBinder downloadBinder = null;
    private IZeeManager zeeManager = null;
    private DirectLoadViewModel directLoadViewModel;
    private LoadingPluginHelper pluginHelper;

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
            Log.e("wang", "onSuccess: " + fileId + "currentFileId " + currentFileId);
            if (fileId.equals(currentFileId)) {
                handler.sendEmptyMessage(MSG_DOWNLOAD_ON_UPDATE);
            }
        }

        @Override
        public void onFailed(String fileId, int type, int code) {
            if (fileId.equals(currentFileId)) {
                handler.sendEmptyMessage(MSG_DOWNLOAD_ON_FAILED);
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
            Log.i(TAG, "onServiceConnected()");
            zeeManager = IZeeManager.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "onServiceDisconnected()");
            zeeManager = null;
        }
    };

    private FrameLayout layoutDownloadDetail;
    private NetworkErrView networkErrViewDetail;
    private LoadingView loadingViewDetail;
    private GradientProgressView gradientProgressViewDetail;
    public boolean openApp = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Transparent);
        setContentView(R.layout.activity_direct_loading);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        skuId = getIntent().getStringExtra("skuId");
        if (TextUtils.isEmpty(skuId)) {
            finish();
            return;
        }else if(!CommonUtils.isUserLogin()){
            logoutClear();
            finish();
            return;
        }
        bindService();
        bindManagerService();

        DirectLoadViewModelFactory factory = new DirectLoadViewModelFactory(DataRepository.getInstance());
        directLoadViewModel = new ViewModelProvider(this, factory).get(DirectLoadViewModel.class);

        layoutDownloadDetail = findViewById(R.id.layout_download_detail);
        networkErrViewDetail = findViewById(R.id.networkErrView_detail);
        loadingViewDetail = findViewById(R.id.loadingView_detail);
        gradientProgressViewDetail = findViewById(R.id.gradient_progress_view_detail);

        initClickListener();
        initViewObservable();
        directLoadViewModel.initDataReq(skuId);

        if (BuildConfig.FLAVOR == "plugin") {
            if (HostManager.isGestureAiEnable()) {
                isLastGestureAIActive = HostManager.isGestureAIActive();
            }
        }
    }


    private void initClickListener() {
        networkErrViewDetail.setRetryClickListener(() -> directLoadViewModel.initDataReq(skuId));
    }

    private void handLoadApp() {
        if (!isProductRelease) {
            showToast("没有发布版本所以不能下载");
            return;
        }

        if (handleAlgorithmLib()) {
            if (directLoadViewModel.upgradeResp != null) {//处理升级
                handleUpgrade();
            } else {
                handleDownload();
            }
        }
    }


    private void initViewObservable() {
        directLoadViewModel.mldInitLoadState.observe(this, loadState -> {
            if (LoadState.Loading == loadState) {
                networkErrViewDetail.setVisibility(View.GONE);
                loadingViewDetail.setVisibility(View.VISIBLE);
                loadingViewDetail.startAnim();
            } else if (LoadState.Success == loadState) {
                if (directLoadViewModel.proDetailResp.getPutawayStatus() == 1 && CommonUtils.isUserLogin()) {
                    directLoadViewModel.getPublishVersionInfo(new PublishReq(directLoadViewModel.proDetailResp.getSoftwareCode()));
                    layoutDownloadDetail.setVisibility(View.VISIBLE);
                } else {
                    if (directLoadViewModel.proDetailResp.getPutawayStatus() == 2) {
                        layoutDownloadDetail.setVisibility(View.GONE);
                    } else {
                        layoutDownloadDetail.setVisibility(View.VISIBLE);
                    }
                    loadingViewDetail.stopAnim();
                    loadingViewDetail.setVisibility(View.GONE);
                }
            } else if (LoadState.Failed == loadState) {
                loadingViewDetail.stopAnim();
                loadingViewDetail.setVisibility(View.GONE);
                networkErrViewDetail.setVisibility(View.VISIBLE);
                layoutDownloadDetail.setVisibility(View.GONE);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        });

        directLoadViewModel.mPublishState.observe(this, loadState -> {
            if (LoadState.Loading == loadState) {
                loadingViewDetail.setVisibility(View.VISIBLE);
            } else if (LoadState.Success == loadState) {
                PublishResp publishResp = directLoadViewModel.publishResp;
                if ((publishResp != null) && (publishResp.getSoftwareInfo() != null)) {
                    isProductRelease = true;
                    currentFileId = publishResp.getSoftwareInfo().getSoftwareCode();
                    lastVersion = publishResp.getSoftwareVersion();
                    gradientProgressViewDetail.setProgress(0);
                    DownloadInfo dbDownloadInfo = CareController.instance.getDownloadInfoByFileId(currentFileId);
                    if (dbDownloadInfo != null) {
                        if (dbDownloadInfo.status == DownloadInfo.STATUS_SUCCESS) {
                        } else if (dbDownloadInfo.status == DownloadInfo.STATUS_STOPPED) {
                            Log.i("wang", "show: " + "继续");
                        } else if (dbDownloadInfo.status == DownloadInfo.STATUS_LOADING) {
                            Log.i("wang", "show: " + "下载中");
                        } else if (dbDownloadInfo.status == DownloadInfo.STATUS_PENDING) {
                            Log.i("wang", "show: " + "等待中");
                        }
                        if (dbDownloadInfo.loadedSize > 0) {
                            int progress = (int) ((dbDownloadInfo.loadedSize * 1.0f / dbDownloadInfo.fileSize) * 100);
                            gradientProgressViewDetail.setProgress(progress);
                        }
                        if (!dbDownloadInfo.version.equals(lastVersion)) {
                            directLoadViewModel.getUpgradeVersionInfo(new UpgradeReq(dbDownloadInfo.version, publishResp.getSoftwareInfo().getSoftwareCode()));
                        } else {
                            handLoadApp();
                        }
                    } else {
                        handLoadApp();
                    }
                } else {
                    isProductRelease = false;
                    Log.i("wang", "show: " + "未发布");
                    gradientProgressViewDetail.setProgress(0);
                }
            } else if (LoadState.Failed == loadState) {
                loadingViewDetail.stopAnim();
                loadingViewDetail.setVisibility(View.GONE);
                networkErrViewDetail.setVisibility(View.VISIBLE);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            }
        });

        directLoadViewModel.mUpgradeState.observe(this, loadState -> {
            if (LoadState.Success == loadState) {
                if (directLoadViewModel.upgradeResp != null) {
                    handLoadApp();
                }
            }
        });


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
                            showToast("添加失败");
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
                        showToast("添加失败");
                        return false;
                    }
                } else if (downloadModel.status == DownloadInfo.STATUS_STOPPED) {
                    downloadBinder.startDownload(downloadModel.fileId);
                }
            }
        }
        return true;
    }

    private void handleUpgrade() {
        DownloadInfo downloadInfo = DownloadHelper.buildUpgradeDownloadInfo(getUseContext(), directLoadViewModel.proDetailResp, directLoadViewModel.publishResp, directLoadViewModel.upgradeResp);
        boolean success = downloadBinder.startDownload(downloadInfo);
        if (success) {
            directLoadViewModel.upgradeResp = null;
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
                    downloadBinder.pauseDownload(dbDownloadInfo.fileId);
                } else if (dbDownloadInfo.status == DownloadInfo.STATUS_STOPPED) {
                    downloadBinder.startDownload(dbDownloadInfo);
                } else {
                    String lastPluginPackageName = HostManager.getLastPluginPackageName();
                    removeRecentTask(lastPluginPackageName);
                    removeRecentTask(dbDownloadInfo.mainClassPath);
                    startLoadingApplication();
                }
            }
        }
    }

    private void updateDownloadTip() {
        DownloadInfo dbDownloadInfo = CareController.instance.getDownloadInfoByFileId(currentFileId);
        if (dbDownloadInfo == null) {
            Log.i("wang", "show: " + "下载");
        } else {
            if (dbDownloadInfo.status == DownloadInfo.STATUS_LOADING) {
                Log.i("wang", "show: " + "下载中");
            } else if (dbDownloadInfo.status == DownloadInfo.STATUS_PENDING) {
                Log.i("wang", "show: " + "等待中");
            } else if (dbDownloadInfo.status == DownloadInfo.STATUS_STOPPED) {
                Log.i("wang", "show: " + "继续");
            } else if (dbDownloadInfo.status == DownloadInfo.STATUS_SUCCESS) {
                Log.i("wang", "show: " + "开始体验");
                handLoadApp();
            }
        }
    }

    private void updateDownloadTipOnFailed() {
        Log.i("wang", "show: " + "课件下载失败");
        if (!NetworkUtil.isNetworkAvailable(DirectLoadingActivity.this)) {
            showToast("网络连接异常！");
        } else {
            showToast("课件下载失败!");
        }
        startFloatingService();
        finish();
    }

    private void updateDownloadTip(int progress) {
        Log.i("wang", "show: " + String.format("下载中(%s%%)", progress));
        gradientProgressViewDetail.setProgress(progress);
    }

    private void showUpgradeDialog() {
        final UpgradeTipDialog upgradeDialog = new UpgradeTipDialog(this);
        upgradeDialog.show();
        upgradeDialog.setMessageText("检测到新版本");
        upgradeDialog.setMessageText("V" + directLoadViewModel.upgradeResp.getSoftwareVersion());
        upgradeDialog.setPositiveText("立即更新");
        upgradeDialog.setCancelText("继续");
        upgradeDialog.setOnClickListener(new UpgradeTipDialog.OnClickListener() {
            @Override
            public void onConfirm(View v) {
            }

            @Override
            public void onPositive(View v) {
                upgradeDialog.cancel();
                DownloadInfo downloadInfo = DownloadHelper.buildUpgradeDownloadInfo(getUseContext(), directLoadViewModel.proDetailResp, directLoadViewModel.publishResp, directLoadViewModel.upgradeResp);
                boolean success = downloadBinder.startDownload(downloadInfo);
                if (success) {
                    directLoadViewModel.upgradeResp = null;
                }
            }

            @Override
            public void onCancel(View v) {
                upgradeDialog.cancel();
                handleDownload();
            }
        });
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
        String akSkInfoString;
        if (BuildConfig.FLAVOR == "plugin") {
            akSkInfoString = HostManager.getHostSpString(SharePrefer.akSkInfo, null);
        } else {
            akSkInfoString = "{\"akCode\":\"6493510285763018752\",\"authVersion\":\"1.0.0\",\"skCode\":\"fD08DqyQwgTxMTrfoAwRq7lRHlUutuvS6kmK0PK1tZY\\u003d\"}";
        }
        if (akSkInfoString != null && !akSkInfoString.isEmpty()) {
            Gson gson = new Gson();
            AkSkResp akSkResp = gson.fromJson(akSkInfoString, AkSkResp.class);
            if (akSkResp != null) {
                if (HostManager.isGestureAiEnable()) {
                    isLastGestureAIActive = HostManager.isGestureAIActive();
                }
                pluginHelper = new LoadingPluginHelper(akSkResp.akCode, akSkResp.skCode, directLoadViewModel.proDetailResp.getProductTitle(), directLoadViewModel.proDetailResp.getUseImgUrl(), currentFileId, handler, this);
                if (BuildConfig.FLAVOR == "plugin") {
                    pluginHelper.startLoad();
                } else {
                    Intent intent = getPackageManager().getLaunchIntentForPackage("com.example.myapplication");
                    if (intent != null) {
                        startActivity(intent);
                        openApp = true;
                    } else {
                        showToast("发布系统没有安装!");
                    }
                }

            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_CODE_LOGIN == requestCode) {
            directLoadViewModel.initDataReq(skuId);
        }
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
                    ((DirectLoadingActivity) activity).pluginHelper.startPluginActivity();
                } else if (msg.what == MSG_LOADING_OPEN) {
                    ((DirectLoadingActivity) activity).loadingViewDetail.setVisibility(View.VISIBLE);
                    ((DirectLoadingActivity) activity).loadingViewDetail.setText("努力加载中...");
                    ((DirectLoadingActivity) activity).loadingViewDetail.startAnim();
                } else if (msg.what == MSG_LOADING_CLOSE) {
                    ((DirectLoadingActivity) activity).loadingViewDetail.setVisibility(View.GONE);
                    ((DirectLoadingActivity) activity).loadingViewDetail.stopAnim();
                    ((DirectLoadingActivity) activity).layoutDownloadDetail.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        Log.e("wang", "onDestroy: ");
        if (downloadBinder != null) {
            downloadBinder.unRegisterDownloadListener(downloadListener);
            HostManager.getUseContext(this).unbindService(serviceConnection);
        }
        if (zeeManager != null) {
            unbindManagerService();
            zeeManager = null;
        }
        if (pluginHelper != null) {
            pluginHelper.onDestroy();
            pluginHelper = null;
        }
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME)) {
            Log.e("wang", "onKeyDown: dir");
        }
        return true;
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.e("wang", "onResume: ");
        if (openApp) {
            startFloatingService();
            finish();
        }
    }

    @Override
    protected void onPause() {
        openApp = true;
        super.onPause();
    }

    public boolean isWindowOnFocus = false;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        isWindowOnFocus = hasFocus;
    }


    public void startFloatingService() {
        if (Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(getApplicationContext(), FloatingCameraService.class);
            startService(intent);
        }
    }


    public void logoutClear(){
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
}


