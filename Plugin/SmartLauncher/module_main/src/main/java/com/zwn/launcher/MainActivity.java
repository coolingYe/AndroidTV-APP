package com.zwn.launcher;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;


import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;
import com.qihoo360.replugin.RePlugin;
import com.zee.guide.data.protocol.response.ServicePkgInfoResp;
import com.zee.guide.ui.GuideActivity;
import com.zee.launcher.login.ui.LoginActivity;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.config.SharePrefer;
import com.zeewain.base.data.protocol.request.UpgradeReq;
import com.zeewain.base.data.protocol.response.BaseResp;
import com.zeewain.base.data.protocol.response.DeviceInfoResp;
import com.zeewain.base.model.LoadState;
import com.zeewain.base.ui.BaseActivity;
import com.zeewain.base.utils.ApkUtil;
import com.zeewain.base.utils.CareLog;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.DensityUtils;
import com.zeewain.base.utils.DisplayUtil;
import com.zeewain.base.utils.FileUtils;
import com.zeewain.base.utils.NetworkUtil;
import com.zeewain.base.utils.PluginDiskCacheManager;
import com.zeewain.base.utils.SPUtils;

import com.zeewain.base.widgets.LoadingView;
import com.zeewain.base.widgets.NetworkErrView;
import com.zwn.launcher.data.DataRepository;
import com.zeewain.base.data.protocol.response.PublishResp;
import com.zeewain.base.data.protocol.response.UpgradeResp;
import com.zwn.launcher.data.protocol.response.DeviceCheckResp;
import com.zwn.launcher.dialog.UpgradeTipDialog;
import com.zwn.launcher.service.ZeeServiceManager;
import com.zwn.launcher.ui.upgrade.UpgradeDialogActivity;
import com.zwn.launcher.utils.ApiCacheHelper;
import com.zwn.launcher.utils.DeviceConfig;
import com.zwn.launcher.utils.DownloadHelper;
import com.zwn.lib_download.DownloadListener;
import com.zwn.lib_download.db.CareController;
import com.zwn.lib_download.model.DownloadInfo;


import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.qihoo360.replugin.model.PluginInfo;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;


public class MainActivity extends BaseActivity{
    private static final String TAG = "MainActivity";
    private final int REQUEST_CODE_PERMISSIONS = 1;
    private final int REQUEST_CODE_SDCARD_PERMISSION = 201;
    private final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private MainViewModel mainViewModel;
    private LoadingView loadingViewMain;
    private NetworkErrView networkErrViewMain;
    private LinearLayout layoutMainDeviceOperate;
    private MaterialCardView cardDeviceReboot;
    private MaterialCardView cardDeviceShutdown;
    private TextView tvMainTip;
    private String hostPluginFileId;
    private String hostMainPlugin;
    private String hostMainPluginClassPath;
    private final ExecutorService mThreadPool = Executors.newCachedThreadPool();
    private boolean isLastGestureAIActive = false;
    private long lastProgressTime = 0;
    private long startPluginCount = 1;
    private static int checkNetApiCount = 10;
    private static final int MSG_CHECK_NET_API = 1000;
    private final MyHandler handler = new MyHandler(Looper.myLooper(),this);
    private final DownloadListener downloadListener = new DownloadListener() {
        @Override
        public void onProgress(String fileId, int progress, long loadedSize, long fileSize) {
            if (fileId.equals(hostPluginFileId)) {
                runOnUiThread(() -> {
                    long waitTimeSec = 0;
                    if(lastProgressTime == 0) {
                        lastProgressTime = System.currentTimeMillis();
                    }else{
                        waitTimeSec = (((System.currentTimeMillis() - lastProgressTime) * (100 - progress)) + 5000);
                        lastProgressTime = System.currentTimeMillis();
                    }

                    if(loadingViewMain != null && loadingViewMain.getVisibility() == View.VISIBLE) {
                        long minutes = waitTimeSec/1000/60;
                        if(minutes > 1) {
                            loadingViewMain.setText("正在更新组件，大概需要 " + minutes + " 分钟");
                        }else{
                            loadingViewMain.setText("正在更新组件，大概需要 1 分钟");
                        }
                    }
                });
            }
        }

        @Override
        public void onSuccess(String fileId, int type, File file) {
            if (fileId.equals(hostPluginFileId)) {
                runOnUiThread(() -> {
                    lastProgressTime = 0;
                    installPlugin(fileId, file.getAbsolutePath());
                });
            }
        }

        @Override
        public void onFailed(String fileId, int type, int code) {
            if (fileId.equals(hostPluginFileId)) {
                runOnUiThread(() -> {
                    lastProgressTime = 0;
                    showNetworkErr();
                });
            }
        }

        @Override
        public void onPaused(String fileId) {}

        @Override
        public void onCancelled(String fileId) {}

        @Override
        public void onUpdate(String fileId) {}
    };

    private void installPlugin(final String fileId, final String filePath){
        mThreadPool.execute(() -> {
            long currentTime = System.currentTimeMillis();
            PluginInfo info = RePlugin.install(filePath);
            CareLog.d(TAG, "installPlugin() cost= " + (System.currentTimeMillis() - currentTime) + ", filePath=" + filePath);
            if(info != null){
                runOnUiThread(() -> {
                    if(loadingViewMain != null && loadingViewMain.getVisibility() == View.VISIBLE) {
                        loadingViewMain.setText("加载中");
                    }

                    startMainPluginActivity();
                });
                CareLog.d(TAG, "installPlugin() 2 cost= " + (System.currentTimeMillis() - currentTime));
            }else{//安装插件失败 need retry?
                CareLog.e(TAG, "installPlugin() failed! fileId=" + fileId + ", filePath=" + filePath);
                runOnUiThread(() -> {
                    loadingViewMain.setVisibility(View.GONE);
                    layoutMainDeviceOperate.setVisibility(View.VISIBLE);
                    tvMainTip.setText("主题更新失败！请重启设备！");
                    cardDeviceReboot.requestFocus();
                });
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CareLog.i(TAG, "onCreate()");
        boolean isGuideDone = SPUtils.getInstance().getBoolean(SharePrefer.GuideDone);
        if(!isGuideDone){
            Intent intent = new Intent();
            intent.setClass(this, GuideActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        if (((ZeeApplication)getApplication()).mainActivity != null) {
            ((ZeeApplication)getApplication()).mainActivity.finish();
            ((ZeeApplication)getApplication()).mainActivity = null;
        }

        ((ZeeApplication)getApplication()).mainActivity = this;

        requestPermission();
        DensityUtils.autoWidth(getApplication(), this);
        setContentView(R.layout.activity_main);
        CareLog.i(TAG, "new version: " + ApkUtil.getAppVersionName(this));

        mThreadPool.execute(() -> ZeeServiceManager.checkPluginAppRelay(MainActivity.this));

        checkApkInstallResult();
        //ZeeServiceManager.getInstance().bindDownloadService(this);
        //ZeeServiceManager.getInstance().bindManagerService(this);
        ZeeServiceManager.getInstance().registerDownloadListener(downloadListener);

        MainViewModelFactory factory = new MainViewModelFactory(DataRepository.getInstance());
        mainViewModel = new ViewModelProvider(this, factory).get(MainViewModel.class);

        initView();
        initListener();
        initViewObservable();

        registerBroadCast();

        if(!CommonUtils.createOrClearPluginModelDir()){
            showToast("模型目录创建失败！");
        }

        checkHostApkStats();

        CommonUtils.savePluginCareInfo();

        loadingViewMain.setVisibility(View.VISIBLE);
        loadingViewMain.startAnim();
        loadingViewMain.requestFocus();

        boolean isRefresh = getIntent().getBooleanExtra(BaseConstants.EXTRA_REFRESH_DATA, false);
        if(isRefresh){
            loadingViewMain.setText("联网数据加载中...");
            loadingViewMain.postDelayed(() -> mainViewModel.reqManagerAppUpgrade(ApkUtil.getAppVersionName(MainActivity.this, BaseConstants.MANAGER_PACKAGE_NAME)), 1500);
        }else{
            if (NetworkUtil.isNetworkAvailable(this)) {
                loadingViewMain.postDelayed(() -> mainViewModel.reqManagerAppUpgrade(ApkUtil.getAppVersionName(MainActivity.this, BaseConstants.MANAGER_PACKAGE_NAME)), 1000);
            } else {
                loadingViewMain.setTag(true);
                loadingViewMain.postDelayed(this::checkShouldInitData, 9000);
            }
        }
        setLauncherWallpaper();
        disableAppHibernation();
    }

    void disableAppHibernation() {
        if (DeviceConfig.getBoolean("app_hibernation", "app_hibernation_enabled", true)) {
            DeviceConfig.setProperty("app_hibernation", "app_hibernation_enabled", "false", false);
        }

        DeviceConfig.setProperty("permissions", "auto_revoke_check_frequency_millis", "0", false);//test valid
    }

    private void reqCommonAppUpgrade(){
        mainViewModel.reqCommonAppUpgrade(ApkUtil.getAppVersionName(this, BaseConstants.SETTINGS_APP_PACKAGE_NAME), BaseConstants.SETTINGS_APP_SOFTWARE_CODE);
        mainViewModel.reqCommonAppUpgrade(ApkUtil.getAppVersionName(this, BaseConstants.ZEE_GESTURE_AI_APP_PACKAGE_NAME), BaseConstants.ZEE_GESTURE_AI_APP_SOFTWARE_CODE);
    }

    private synchronized void checkShouldInitData(){
        if(loadingViewMain.getTag() != null){
            boolean needLoadData = (Boolean)loadingViewMain.getTag();
            loadingViewMain.setTag(false);
            if(needLoadData){
                mainViewModel.reqManagerAppUpgrade(ApkUtil.getAppVersionName(this, BaseConstants.MANAGER_PACKAGE_NAME));
            }
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_CODE_SDCARD_PERMISSION);
            }else{
                checkPermission();
            }
        } else {
            checkPermission();
        }
    }

    private void checkPermission(){
        if (allPermissionsGranted()) {

        } else {
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }


    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_SDCARD_PERMISSION){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                if (Environment.isExternalStorageManager()) {
                    checkPermission();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {

            } else {
                showToast("请开启权限！");
            }
        }
    }

    private void checkApkInstallResult(){
        String apkPath = getIntent().getStringExtra(BaseConstants.EXTRA_APK_PATH);
        if(apkPath != null && !apkPath.isEmpty()){
            boolean apkInstallResult = getIntent().getBooleanExtra(BaseConstants.EXTRA_APK_INSTALL_RESULT, false);
            String fileId = getIntent().getStringExtra(BaseConstants.EXTRA_PLUGIN_NAME);
            CareLog.i(TAG, "handleApkInstallResult() apkPath: " + apkPath + ", result=" + apkInstallResult + ", fileId=" + fileId);
            if(apkInstallResult){
                showToast("版本更新成功！");
            }
            if(fileId != null && CareController.instance.deleteDownloadInfo(fileId) > 0) {
                FileUtils.deleteFile(apkPath);
            }
        }
    }

    private void checkHostApkStats(){
        DownloadInfo downloadInfo = CareController.instance.getDownloadInfoByFileId(BaseConstants.HOST_APP_SOFTWARE_CODE);
        if(downloadInfo != null && downloadInfo.status == DownloadInfo.STATUS_SUCCESS){
            File file = new File(downloadInfo.filePath);
            if (!file.exists()) {
                CareController.instance.deleteDownloadInfo(BaseConstants.HOST_APP_SOFTWARE_CODE);
            }
        }
    }

    private void setLauncherWallpaper(){
        boolean isSetWallpaperDone = SPUtils.getInstance().getBoolean(SharePrefer.SetWallpaperDone, false);
        if(!isSetWallpaperDone) {
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(this.getApplicationContext());
            try {
                wallpaperManager.setBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.img_default_bg));
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                SPUtils.getInstance().put(SharePrefer.SetWallpaperDone, true);
            }
        }
    }

    private void initView(){
        loadingViewMain = findViewById(R.id.loadingView_main);
        networkErrViewMain = findViewById(R.id.networkErrView_main);
        layoutMainDeviceOperate = findViewById(R.id.ll_main_device_operate);
        cardDeviceReboot = findViewById(R.id.card_device_reboot);
        cardDeviceShutdown = findViewById(R.id.card_device_shutdown);
        tvMainTip = findViewById(R.id.tv_main_tip);
    }

    private void initListener() {
        cardDeviceReboot.setOnClickListener(v -> {
            if(hostPluginFileId != null && hostMainPlugin != null) {
                CareController.instance.deleteDownloadInfo(hostPluginFileId);
                RePlugin.uninstall(hostMainPlugin);
            }
            SPUtils.getInstance().put(SharePrefer.StartPluginFailedTimes, 0);
            Intent intent = new Intent(Intent.ACTION_REBOOT);
            intent.putExtra("nowait", 1);
            intent.putExtra("interval", 1);
            intent.putExtra("window", 0);
            sendBroadcast(intent);
        });

        cardDeviceReboot.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                cardDeviceReboot.setStrokeColor(0xFFFFFFFF);
                final int strokeWidth = DisplayUtil.dip2px(v.getContext(), 1);
                cardDeviceReboot.setStrokeWidth(strokeWidth);
                CommonUtils.scaleView(v, 1.1f);
            } else {
                cardDeviceReboot.setStrokeColor(0x00FFFFFF);
                cardDeviceReboot.setStrokeWidth(0);
                v.clearAnimation();
                CommonUtils.scaleView(v, 1f);
            }
        });

        cardDeviceShutdown.setOnClickListener(v -> {
            if(hostPluginFileId != null && hostMainPlugin != null) {
                CareController.instance.deleteDownloadInfo(hostPluginFileId);
                RePlugin.uninstall(hostMainPlugin);
            }
            SPUtils.getInstance().put(SharePrefer.StartPluginFailedTimes, 0);
            Intent intent = new Intent("com.android.internal.intent.action.REQUEST_SHUTDOWN");
            intent.putExtra("android.intent.extra.KEY_CONFIRM", false);//其中false换成true,会弹出是否关机的确认窗口
            startActivity(intent);
        });

        cardDeviceShutdown.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                cardDeviceShutdown.setStrokeColor(0xFFFFFFFF);
                final int strokeWidth = DisplayUtil.dip2px(v.getContext(), 1);
                cardDeviceShutdown.setStrokeWidth(strokeWidth);
                CommonUtils.scaleView(v, 1.1f);
            } else {
                cardDeviceShutdown.setStrokeColor(0x00FFFFFF);
                cardDeviceShutdown.setStrokeWidth(0);
                v.clearAnimation();
                CommonUtils.scaleView(v, 1f);
            }
        });

        networkErrViewMain.setRetryClickListener(() -> {
            if(LoadState.Failed == mainViewModel.mldManagerAppUpgradeState.getValue()){
                mainViewModel.reqManagerAppUpgrade(ApkUtil.getAppVersionName(this, BaseConstants.MANAGER_PACKAGE_NAME));
            }else if(LoadState.Failed == mainViewModel.mldHostAppUpgradeState.getValue()){
                mainViewModel.reqHostAppUpgrade(ApkUtil.getAppVersionName(this));
            }else if(LoadState.Failed == mainViewModel.mldServicePackInfoLoadState.getValue()){
                mainViewModel.reqUmsServicePackInfo();
            }else if(LoadState.Failed == mainViewModel.mldThemeInfoLoadState.getValue()){
                mainViewModel.reqThemeInfo();
            }else if(LoadState.Failed == mainViewModel.mldHostPluginPublishState.getValue()){
                mainViewModel.reqHostPluginPublishInfo(mainViewModel.themeInfoResp.softwareCode);
            }else{
                if(loadingViewMain.getVisibility() != View.VISIBLE) {
                    loadingViewMain.setVisibility(View.VISIBLE);
                    loadingViewMain.startAnim();
                }
                networkErrViewMain.setVisibility(View.GONE);
                if(mainViewModel.hostPluginPublishResp != null)
                    handleHostPluginPublishResp(mainViewModel.hostPluginPublishResp);
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void initViewObservable() {
        mainViewModel.mldCommonAppUpgradeState.observe(this, upgradeLoadState -> {
            if (LoadState.Success == upgradeLoadState.loadState) {
                if(upgradeLoadState.upgradeResp != null){
                    if(ZeeServiceManager.getInstance().getDownloadBinder() != null) {
                        ZeeServiceManager.getInstance().handleCommonAppUpgrade(upgradeLoadState.upgradeResp, upgradeLoadState.softwareCode);
                    }
                }
            }
        });

        mainViewModel.mldManagerAppUpgradeState.observe(this, loadState -> {
            if (LoadState.Success == loadState) {
                if(mainViewModel.managerAppUpgradeResp != null){
                    ZeeServiceManager.getInstance().handleManagerAppUpgrade(mainViewModel.managerAppUpgradeResp);
                }
                mainViewModel.reqHostAppUpgrade(ApkUtil.getAppVersionName(this));
            }else if(LoadState.Failed == loadState){
                loadingViewMain.stopAnim();
                loadingViewMain.setVisibility(View.GONE);
                networkErrViewMain.setVisibility(View.VISIBLE);
                networkErrViewMain.requestFocus();
            }else{
                if(loadingViewMain.getVisibility() != View.VISIBLE) {
                    loadingViewMain.setVisibility(View.VISIBLE);
                    loadingViewMain.startAnim();
                }
                networkErrViewMain.setVisibility(View.GONE);
            }
        });

        mainViewModel.mldHostAppUpgradeState.observe(this, loadState -> {
            if (LoadState.Success == loadState) {
                if (mainViewModel.hostAppUpgradeResp != null) {
                    showUpgradeTipDialog(this, mainViewModel.hostAppUpgradeResp);
                }else{
                    mainViewModel.reqUmsServicePackInfo();
                }
            }else if(LoadState.Failed == loadState){
                loadingViewMain.stopAnim();
                loadingViewMain.setVisibility(View.GONE);
                networkErrViewMain.setVisibility(View.VISIBLE);
                networkErrViewMain.requestFocus();
            }else{
                if(loadingViewMain.getVisibility() != View.VISIBLE) {
                    loadingViewMain.setVisibility(View.VISIBLE);
                    loadingViewMain.startAnim();
                }
                networkErrViewMain.setVisibility(View.GONE);
            }
        });

        mainViewModel.mldTimeCheckState.observe(this, timestamp -> {
            handUpdateSystemTime(timestamp);
        });

        mainViewModel.mldThemeInfoLoadState.observe(this, loadState -> {
            if (LoadState.Success == loadState) {
                mainViewModel.reqHostPluginPublishInfo(mainViewModel.themeInfoResp.softwareCode);
            }else if(LoadState.Failed == loadState){
                loadingViewMain.stopAnim();
                loadingViewMain.setVisibility(View.GONE);
                networkErrViewMain.setVisibility(View.VISIBLE);
                networkErrViewMain.requestFocus();
            }else{
                if(loadingViewMain.getVisibility() != View.VISIBLE) {
                    loadingViewMain.setVisibility(View.VISIBLE);
                    loadingViewMain.startAnim();
                }
                networkErrViewMain.setVisibility(View.GONE);
            }
        });

        mainViewModel.mldHostPluginPublishState.observe(this, loadState -> {
            if (LoadState.Success == loadState) {
                if(mainViewModel.hostPluginPublishResp != null)
                    handleHostPluginPublishResp(mainViewModel.hostPluginPublishResp);
            }else if(LoadState.Failed == loadState){
                loadingViewMain.stopAnim();
                loadingViewMain.setVisibility(View.GONE);
                networkErrViewMain.setVisibility(View.VISIBLE);
                networkErrViewMain.requestFocus();
            }else{
                if(loadingViewMain.getVisibility() != View.VISIBLE) {
                    loadingViewMain.setVisibility(View.VISIBLE);
                    loadingViewMain.startAnim();
                }
                networkErrViewMain.setVisibility(View.GONE);
            }
        });

        mainViewModel.mldServicePackInfoLoadState.observe(this, loadState -> {
            if (LoadState.Success == loadState) {
                if (mainViewModel.isFromCacheServicePkgInfoResp) {
                    checkLoginAndReqTheme();
                } else {
                    handleServicePackResp(mainViewModel.servicePkgInfoResp);
                }
            }else if(LoadState.Failed == loadState){
                loadingViewMain.stopAnim();
                loadingViewMain.setVisibility(View.GONE);
                networkErrViewMain.setVisibility(View.VISIBLE);
                networkErrViewMain.requestFocus();
            }else{
                if(loadingViewMain.getVisibility() != View.VISIBLE) {
                    loadingViewMain.setVisibility(View.VISIBLE);
                    loadingViewMain.startAnim();
                }
                networkErrViewMain.setVisibility(View.GONE);
            }
        });

        mainViewModel.mldDeviceUnActivated.observe(this, aBoolean -> {
            if (aBoolean) {
                handleDeviceUnActivated();
            }
        });

        mainViewModel.mldToastMsg.observe(this, this::showToast);
    }

    private void handUpdateSystemTime(long currentServerTimestamp){
        //1717207200000L beijing 2024-06-01 10:00:00
        if(currentServerTimestamp > 1717207200000L && (Math.abs(System.currentTimeMillis() - currentServerTimestamp) > 120000)){
            ((AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE)).setTime(currentServerTimestamp);
        }
    }

    private void checkLoginAndReqTheme(){
        boolean isTopicLogin = SPUtils.getInstance().getBoolean(SharePrefer.TopicLogin, false);
        if(!isTopicLogin && !CommonUtils.isUserLogin()){
            Intent intent = new Intent();
            intent.setClass(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }else {
            reqCommonAppUpgrade();
            mainViewModel.reqThemeInfo();
        }
    }

    private UpgradeTipDialog upgradeTipDialog;
    public void showUpgradeTipDialog(final Context context, final UpgradeResp upgradeResp) {
        if(upgradeTipDialog == null){
            upgradeTipDialog = new UpgradeTipDialog(context);
            upgradeTipDialog.setTitleText("检测到新版本");
            upgradeTipDialog.setMessageText("V" + upgradeResp.getSoftwareVersion());
            upgradeTipDialog.showConfirmButton(upgradeResp.isForcible());
            if (upgradeResp.isForcible()) {
                upgradeTipDialog.setOnCountDoneListener(() -> {
                    if (upgradeTipDialog != null){
                        upgradeTipDialog.cancel();
                        UpgradeDialogActivity.showUpgradeDialog(context, upgradeResp);
                    }
                });
            }
            upgradeTipDialog.setOnCancelListener(dialog -> {
                upgradeTipDialog.stopCountDown();
                upgradeTipDialog.setOnCountDoneListener(null);
                upgradeTipDialog.setOnClickListener(null);
                upgradeTipDialog = null;
            });
            upgradeTipDialog.setOnClickListener(new UpgradeTipDialog.OnClickListener() {
                @Override
                public void onConfirm(View v) {
                    upgradeTipDialog.cancel();
                    UpgradeDialogActivity.showUpgradeDialog(context, upgradeResp);
                }

                @Override
                public void onPositive(View v) {
                    upgradeTipDialog.cancel();
                    UpgradeDialogActivity.showUpgradeDialog(context, upgradeResp);
                }

                @Override
                public void onCancel(View v) {
                    upgradeTipDialog.cancel();
                    mainViewModel.reqUmsServicePackInfo();
                }
            });
        }
        if(!upgradeTipDialog.isShowing()) {
            upgradeTipDialog.show();
            upgradeTipDialog.startConfirmCountDown(10);
        }
    }

    private void showNetworkErr(){
        if(loadingViewMain != null && networkErrViewMain != null) {
            loadingViewMain.stopAnim();
            loadingViewMain.setVisibility(View.GONE);
            networkErrViewMain.setVisibility(View.VISIBLE);
            networkErrViewMain.requestFocus();
        }
    }

    private synchronized void handleHostPluginPublishResp(PublishResp publishResp){
        if(ZeeServiceManager.getInstance().getDownloadBinder() == null){
            CareLog.e(TAG, "handleHostPluginPublishResp(), but downloadBinder null!!!");
            return;
        }

        hostPluginFileId = publishResp.getSoftwareInfo().getSoftwareCode();
        hostMainPlugin = publishResp.getSoftwareInfo().getSoftwareExtendInfo().getMainPlugin();
        hostMainPluginClassPath = publishResp.getSoftwareInfo().getSoftwareExtendInfo().getMainClassPath();

        String lastVersion = publishResp.getSoftwareVersion();

        ZeeApplication.pluginBaseInfo = hostMainPlugin + "_" + lastVersion;

        DownloadInfo dbDownloadInfo = CareController.instance.getDownloadInfoByFileId(hostPluginFileId);
        boolean startDownloadResult = false;
        if (dbDownloadInfo != null) {
            if (!dbDownloadInfo.version.equals(lastVersion)){
                startDownloadResult = ZeeServiceManager.getInstance().getDownloadBinder().startDownload(DownloadHelper.buildHostPluginDownloadInfo(this, publishResp));
            }else{
                if(dbDownloadInfo.status == DownloadInfo.STATUS_STOPPED){
                    startDownloadResult = ZeeServiceManager.getInstance().getDownloadBinder().startDownload(dbDownloadInfo);
                }else if(dbDownloadInfo.status == DownloadInfo.STATUS_SUCCESS){
                    if(RePlugin.isPluginInstalled(hostMainPlugin)){
                        startMainPluginActivity();
                        return;
                    }else{//未安装，文件存在，则进行安装
                        File file = new File(dbDownloadInfo.filePath);
                        if(file.exists() && dbDownloadInfo.packageMd5.equals(FileUtils.file2MD5(file))){
                            installPlugin(dbDownloadInfo.fileId, file.getAbsolutePath());
                            return;
                        }else{
                            file.delete();
                            CareController.instance.deleteDownloadInfo(dbDownloadInfo.fileId);
                            startDownloadResult = ZeeServiceManager.getInstance().getDownloadBinder().startDownload(DownloadHelper.buildHostPluginDownloadInfo(this, publishResp));
                        }
                    }
                }else{
                    startDownloadResult = true;
                }
            }
        }else{
            startDownloadResult = ZeeServiceManager.getInstance().getDownloadBinder().startDownload(DownloadHelper.buildHostPluginDownloadInfo(this, publishResp));
        }

        if(startDownloadResult){
            loadingViewMain.setText("正在更新组件，请稍候...");
        }else{
            CareController.instance.deleteDownloadInfo(hostPluginFileId);
            showNetworkErr();
        }
    }

    private void startMainPluginActivity(){
        CareLog.i(TAG, "startMainPluginActivity() hostMainPlugin=" + hostMainPlugin + ", hostMainPluginClassPath=" + hostMainPluginClassPath);
        List<PluginInfo> pluginInfoList = RePlugin.getPluginInfoList();
        for(PluginInfo pluginInfo: pluginInfoList) {
            CareLog.i(TAG, "startMainPluginActivity() exist pluginInfo=" + pluginInfo);
        }

        PackageInfo packageInfo = RePlugin.fetchPackageInfo(hostMainPlugin);
        if(packageInfo != null){
            CareLog.i(TAG, "startMainPluginActivity " + packageInfo.packageName + ", versionName=" + packageInfo.versionName + ", versionCode=" + packageInfo.versionCode);
            ZeeApplication.pluginBaseInfo = packageInfo.packageName + "_" + packageInfo.versionName + "_" + packageInfo.versionCode;
        }

        PluginInfo hostMainPluginInfo = RePlugin.getPluginInfo(hostMainPlugin);
        if(hostMainPluginInfo == null) {
            showToast("主题配置错误！");
            return;
        }
        CareLog.i(TAG, "startMainPluginActivity " + hostMainPluginInfo + ", startPluginCount=" + startPluginCount + ", isNeedUpdate=" + hostMainPluginInfo.isNeedUpdate());

        if(mainViewModel.hostAppUpgradeResp == null && hostMainPluginInfo.isNeedUpdate()){
            rebootMainProcess(this);
            return;
        }

        Intent intent = RePlugin.createIntent(hostMainPlugin, hostMainPluginClassPath);
        intent.putExtra(BaseConstants.EXTRA_START_HOST_PLUGIN_COUNT, startPluginCount);
        boolean startResult = RePlugin.startActivity(this, intent);
        if(!startResult){
            int failedTimes = SPUtils.getInstance().getInt(SharePrefer.StartPluginFailedTimes, 0);
            CareLog.e(TAG, "startMainPluginActivity failed!");
            showToast("配置主题失败！");
            failedTimes = failedTimes + 1;
            SPUtils.getInstance().put(SharePrefer.StartPluginFailedTimes, failedTimes);
            if(failedTimes == 1){
                layoutMainDeviceOperate.postDelayed(() -> rebootMainProcess(getApplicationContext()), 500);
            }else if(failedTimes == 2 || failedTimes == 3){
                CareController.instance.deleteDownloadInfo(hostPluginFileId);
                RePlugin.uninstall(hostMainPlugin);
                clearGlideCache();
                layoutMainDeviceOperate.postDelayed(() -> rebootMainProcess(getApplicationContext()), 1000);
            }else{
                loadingViewMain.setVisibility(View.GONE);
                layoutMainDeviceOperate.setVisibility(View.VISIBLE);
                tvMainTip.setText("配置主题失败！请重启设备！");
                cardDeviceReboot.requestFocus();
                //todo only to clear all data?
            }
        }else{
            startPluginCount ++;
            SPUtils.getInstance().put(SharePrefer.StartPluginFailedTimes, 0);
        }
    }

    private void clearGlideCache(){
        mThreadPool.execute(() -> Glide.get(getApplication()).clearDiskCache());
    }

    private void rebootMainProcess(final Context context){
        /*Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);*/

        CommonUtils.execRuntimeCommand("am force-stop com.zee.launcher");
    }

    @Override
    protected void onResume() {
        super.onResume();
        CareLog.i(TAG, "onResume()");
        mainViewModel.checkCrashLog();

        if(mainViewModel.hostAppUpgradeResp != null){
            DownloadInfo dbDownloadInfo = CareController.instance.getDownloadInfoByFileId(BaseConstants.HOST_APP_SOFTWARE_CODE);
            if (dbDownloadInfo != null) {
                if (dbDownloadInfo.version.equals(mainViewModel.hostAppUpgradeResp.getSoftwareVersion())
                        && dbDownloadInfo.status != DownloadInfo.STATUS_SUCCESS) {
                    UpgradeDialogActivity.showUpgradeDialog(this, mainViewModel.hostAppUpgradeResp);
                    return;
                } else if (dbDownloadInfo.version.equals(mainViewModel.hostAppUpgradeResp.getSoftwareVersion())
                        && dbDownloadInfo.status == DownloadInfo.STATUS_SUCCESS) {
                    showUpgradeTipDialog(this, mainViewModel.hostAppUpgradeResp);
                    return;
                }else if(!dbDownloadInfo.version.equals(mainViewModel.hostAppUpgradeResp.getSoftwareVersion())){
                    showUpgradeTipDialog(this, mainViewModel.hostAppUpgradeResp);
                    return;
                }
            }else{
                showUpgradeTipDialog(this, mainViewModel.hostAppUpgradeResp);
                return;
            }
        }

        if(hostMainPlugin != null && RePlugin.isPluginInstalled(hostMainPlugin)){
            CareLog.i(TAG, "onResume() hostMainPlugin=" + hostMainPlugin);
            checkServicePackChangedOrNot();
            startMainPluginActivity();
        }

        bindAiServiceAndCheckStart();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        CareLog.i(TAG, "onNewIntent()");
        String onTopRecentTasksPkg = CommonUtils.queryOnTopRecentTasksPkg(this);
        if (onTopRecentTasksPkg != null && onTopRecentTasksPkg.startsWith("com.ZWN.")) {
            ZeeServiceManager.addUserEventRecord(BaseConstants.UserEventCode.PRESS_HOME_KEY_ON_UNITY, onTopRecentTasksPkg);
        }

        ZeeServiceManager.getInstance().handleRemoveAllRecentTasks(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop()");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        CareLog.i(TAG, "onDestroy()");
        if(upgradeTipDialog != null){
            upgradeTipDialog.cancel();
        }
        handler.removeMessages(MSG_CHECK_NET_API);
        ZeeServiceManager.getInstance().unRegisterDownloadListener(downloadListener);
        unRegisterBroadCast();
        super.onDestroy();
    }

    private void bindAiServiceAndCheckStart(){
        ZeeServiceManager.getInstance().bindGestureAiService(this);
        networkErrViewMain.postDelayed(() -> {
            if(ZeeServiceManager.isSettingGestureAIEnable()){
                ZeeServiceManager.getInstance().startGestureAi(false);
            }
        }, 1000);
    }

    public void onNetChange() {
        CareLog.i(TAG, "onNetChange()");
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork && activeNetwork.isConnected()) {
            CareLog.i(TAG, "onNetChange() isConnected");
            if(null == mainViewModel.mldManagerAppUpgradeState.getValue()) {
                checkShouldInitData();
            }else if(LoadState.Failed == mainViewModel.mldManagerAppUpgradeState.getValue()){
                mainViewModel.reqManagerAppUpgrade(ApkUtil.getAppVersionName(this, BaseConstants.MANAGER_PACKAGE_NAME));
            }else if(LoadState.Failed == mainViewModel.mldHostAppUpgradeState.getValue()){
                mainViewModel.reqHostAppUpgrade(ApkUtil.getAppVersionName(this));
            }else if(LoadState.Failed == mainViewModel.mldServicePackInfoLoadState.getValue()){
                mainViewModel.reqUmsServicePackInfo();
            }else if(LoadState.Failed == mainViewModel.mldThemeInfoLoadState.getValue()){
                mainViewModel.reqThemeInfo();
            }else if(LoadState.Failed == mainViewModel.mldHostPluginPublishState.getValue()){
                mainViewModel.reqHostPluginPublishInfo(hostPluginFileId);
            }else if((mainViewModel.isFromCacheManagerAppUpgrade && LoadState.Success == mainViewModel.mldManagerAppUpgradeState.getValue())
                    || (mainViewModel.isFromCacheHostAppUpgrade && LoadState.Success == mainViewModel.mldHostAppUpgradeState.getValue())
                    || (mainViewModel.isFromCacheServicePkgInfoResp && LoadState.Success == mainViewModel.mldServicePackInfoLoadState.getValue())
                    || (mainViewModel.isFromCacheThemeInfo && LoadState.Success == mainViewModel.mldThemeInfoLoadState.getValue())
                    || ( mainViewModel.isFromCacheHostPluginPublish && LoadState.Success == mainViewModel.mldHostPluginPublishState.getValue())){
                checkNetApiCount = 10;
                handler.removeMessages(MSG_CHECK_NET_API);
                handler.sendEmptyMessageDelayed(MSG_CHECK_NET_API, 100);
                return;
            }else if(mainViewModel.lastManagerAppUpgradeRespTime > 0
                    && LoadState.Success == mainViewModel.mldHostPluginPublishState.getValue()
                    && (System.currentTimeMillis() - mainViewModel.lastManagerAppUpgradeRespTime) > 3 * 86400000 /*10 * 60 * 1000*/){
                checkNetApiCount = 10;
                handler.removeMessages(MSG_CHECK_NET_API);
                handler.sendEmptyMessageDelayed(MSG_CHECK_NET_API, 100);
                return;
            }

            mThreadPool.execute(() -> {
                if (ZeeServiceManager.getInstance().getDownloadBinder() != null){
                    if (mainViewModel.hostAppUpgradeResp != null) {
                        DownloadInfo dbDownloadInfo = CareController.instance.getDownloadInfoByFileId(BaseConstants.HOST_APP_SOFTWARE_CODE);
                        if ((dbDownloadInfo != null) && (dbDownloadInfo.status != DownloadInfo.STATUS_SUCCESS)) {
                            ZeeServiceManager.getInstance().getDownloadBinder().startDownload(dbDownloadInfo.fileId);
                        }
                    } else {
                        DownloadInfo pendingDownloadInfo = CareController.instance.getLatestPendingDownloadInfo();
                        if (pendingDownloadInfo != null) {
                            ZeeServiceManager.getInstance().getDownloadBinder().startDownload(pendingDownloadInfo.fileId);
                        }
                    }
                }
            });
        }
    }

    private void gotoMainActivity(){
        CareLog.i(TAG, "gotoMainActivity()");
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, MainActivity.class);
        intent.putExtra(BaseConstants.EXTRA_REFRESH_DATA, true);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK /*| Intent.FLAG_ACTIVITY_NO_HISTORY*/);
        startActivity(intent);
        finish();
    }

    public void checkManagerAppUpgradeApi() {
        DataRepository.getInstance().getUpgradeVersionInfo(new UpgradeReq(ApkUtil.getAppVersionName(this, BaseConstants.MANAGER_PACKAGE_NAME), BaseConstants.MANAGER_APP_SOFTWARE_CODE))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<BaseResp<UpgradeResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<UpgradeResp> response) {
                        if(response.isCache){
                            handler.removeMessages(MSG_CHECK_NET_API);
                            handler.sendEmptyMessageDelayed(MSG_CHECK_NET_API, 1000);
                        }else{
                            gotoMainActivity();
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        handler.removeMessages(MSG_CHECK_NET_API);
                        handler.sendEmptyMessageDelayed(MSG_CHECK_NET_API, 1000);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void handleServicePackResp(ServicePkgInfoResp servicePkgInfoResp) {
        final ServicePkgInfoResp pluginCacheServicePkgInfoResp = ApiCacheHelper.getPluginCacheServicePkgInfoResp(this);
        if (pluginCacheServicePkgInfoResp == null || pluginCacheServicePkgInfoResp.packId == 0 ) {
            checkLoginAndReqTheme();
            return;
        }

        CareLog.i(TAG, "handleServicePackResp() plugin packId=" + pluginCacheServicePkgInfoResp.packId);

        if (servicePkgInfoResp.extendJson != null && servicePkgInfoResp.extendJson.loginMode == 2) {
            SPUtils.getInstance().put(SharePrefer.TopicLogin, true);
        } else {
            SPUtils.getInstance().put(SharePrefer.TopicLogin, false);
        }

        final ServicePkgInfoResp hostCacheServicePkgInfoResp = ApiCacheHelper.getHostCacheServicePkgInfoResp();
        if (hostCacheServicePkgInfoResp != null) {
            if (pluginCacheServicePkgInfoResp.packId != hostCacheServicePkgInfoResp.packId
                    || !pluginCacheServicePkgInfoResp.themeJson.toString().equals(hostCacheServicePkgInfoResp.themeJson.toString())) {
                mThreadPool.execute(() -> handleServicePkgChanged(servicePkgInfoResp, pluginCacheServicePkgInfoResp));
            } else {
                checkLoginAndReqTheme();
            }
        } else {
            checkLoginAndReqTheme();
        }
    }

    private void checkServicePackChangedOrNot() {
        CareLog.i(TAG, "checkServicePackChangedOrNot()");
        final ServicePkgInfoResp pluginCacheServicePkgInfoResp = ApiCacheHelper.getPluginCacheServicePkgInfoResp(this);
        if (pluginCacheServicePkgInfoResp == null || pluginCacheServicePkgInfoResp.packId == 0 ) return;

        CareLog.i(TAG, "checkServicePackChangedOrNot() plugin packId=" + pluginCacheServicePkgInfoResp.packId);

        DataRepository.getInstance().getUmsServicePackInfo(CommonUtils.getDeviceSn())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<BaseResp<ServicePkgInfoResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<ServicePkgInfoResp> response) {
                        if(!response.isCache && response.code == BaseConstants.API_HANDLE_SUCCESS){
                            if (response.data != null && response.data.packId > 0 && response.data.themeJson != null) {
                                CareLog.i(TAG, "checkServicePackChangedOrNot() cmp start");
                                if (response.data.extendJson != null && response.data.extendJson.loginMode == 2) {
                                    SPUtils.getInstance().put(SharePrefer.TopicLogin, true);
                                } else {
                                    SPUtils.getInstance().put(SharePrefer.TopicLogin, false);
                                }
                                final ServicePkgInfoResp hostCacheServicePkgInfoResp = ApiCacheHelper.getHostCacheServicePkgInfoResp();
                                if (hostCacheServicePkgInfoResp != null) {
                                    if (pluginCacheServicePkgInfoResp.packId != hostCacheServicePkgInfoResp.packId
                                            || !pluginCacheServicePkgInfoResp.themeJson.toString().equals(hostCacheServicePkgInfoResp.themeJson.toString())) {
                                        mThreadPool.execute(() -> handleServicePkgChanged(response.data, pluginCacheServicePkgInfoResp));
                                    }
                                }
                            } else {
                                checkDeviceActivateStatus();
                            }
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {}

                    @Override
                    public void onComplete() {}
                });
    }

    private void checkDeviceActivateStatus() {
        DataRepository.getInstance().getDeviceInfo(CommonUtils.getDeviceSn())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<BaseResp<DeviceInfoResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<DeviceInfoResp> resp) {
                        if (resp.code == BaseConstants.API_HANDLE_SUCCESS) {
                            if (resp.data != null && resp.data.activateStatus == 0) {
                                handleDeviceUnActivated();
                            }
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) { }

                    @Override
                    public void onComplete() { }
                });
    }

    private void handleDeviceUnActivated() {
        SPUtils.getInstance().clear();
        CommonUtils.startGuideActivity(this, true);
        finish();
    }

    private synchronized void handleServicePkgChanged(ServicePkgInfoResp newServicePkgInfoResp, ServicePkgInfoResp oldServicePkgInfoResp) {
        CareLog.i(TAG, "handleServicePkgChanged()");
        PluginDiskCacheManager.init(this.getApplicationContext());
        PluginDiskCacheManager.getInstance().delete();
        PluginDiskCacheManager.getInstance().close();

        StringBuilder sb = new StringBuilder();
        for (String skuId : newServicePkgInfoResp.coursewareSku) {
            sb.append(",").append(skuId);
        }
        List<DownloadInfo>  downloadInfoList;
        if(sb.length() > 0) {
            sb.deleteCharAt(0);
            downloadInfoList = CareController.instance.getAllDownloadInfo("type=" + BaseConstants.DownloadFileType.PLUGIN_APP
                    + " and extraId NOT IN (" + sb.toString() + ")");
        } else {
            downloadInfoList = CareController.instance.getAllDownloadInfo("type=" + BaseConstants.DownloadFileType.PLUGIN_APP);
        }

        for (DownloadInfo downloadInfo: downloadInfoList) {
            if (downloadInfo.status == DownloadInfo.STATUS_PENDING || downloadInfo.status == DownloadInfo.STATUS_LOADING) {
                if (ZeeServiceManager.getInstance().downloadBinder != null) {
                    ZeeServiceManager.getInstance().downloadBinder.pauseDownload(downloadInfo.fileId);
                }
            }
            int result = CareController.instance.deleteDownloadInfo(downloadInfo.fileId);
            CareLog.i(TAG, "handleServicePkgUpdate() deleteDownloadInfo " + downloadInfo.fileId + ", " + downloadInfo.mainClassPath);
            if (result > 0) {
                File apkFile = new File(downloadInfo.filePath);
                if(apkFile.exists()){
                    apkFile.delete();
                }
                ZeeServiceManager.getInstance().handleDeletePackage(downloadInfo.mainClassPath);
            }
        }

        String newPackageSoftwareCode = newServicePkgInfoResp.themeJson.getString("packageSoftwareCode");
        String oldPackageSoftwareCode = oldServicePkgInfoResp.themeJson.getString("packageSoftwareCode");

        CareLog.i(TAG, "handleServicePkgChanged() newPackageSoftwareCode=" + newPackageSoftwareCode + ", oldPackageSoftwareCode=" + oldPackageSoftwareCode);

        if (!TextUtils.isEmpty(oldPackageSoftwareCode) && !oldPackageSoftwareCode.equals(newPackageSoftwareCode)) {

            String pluginName = oldPackageSoftwareCode.replace("ZWN_SW_ANDROID", "PLUGIN");
            PluginInfo pluginInfo = RePlugin.getPluginInfo(pluginName);
            if (pluginInfo != null) {
                DownloadInfo oldThemePluginDownloadInfo = CareController.instance.getDownloadInfoByFileId(oldPackageSoftwareCode);
                int result = CareController.instance.deleteDownloadInfo(oldPackageSoftwareCode);
                if (result > 0) {
                    CareLog.i(TAG, "handleServicePkgChanged() RePlugin.uninstall=" + pluginName + " <<<------");
                    pluginInfo.deleteObsolote(this);
                    RePlugin.uninstall(pluginName);
                    if (oldThemePluginDownloadInfo != null) {
                        File apkFile = new File(oldThemePluginDownloadInfo.filePath);
                        if(apkFile.exists()){
                            apkFile.delete();
                        }
                    }
                }
            }
        }

        checkDeviceHold();
    }

    private void checkDeviceHold() {
        CareLog.i(TAG, "checkDeviceHold()");
        DataRepository.getInstance().checkDeviceHold(CommonUtils.getDeviceSn())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<BaseResp<DeviceCheckResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<DeviceCheckResp> response) {
                        if(response.code == BaseConstants.API_HANDLE_SUCCESS && response.data != null && response.data.holdingDevice){
                            rebootMainProcess(MainActivity.this);
                        } else {
                            CommonUtils.logoutClear();
                            rebootMainProcess(MainActivity.this);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        CommonUtils.logoutClear();
                        rebootMainProcess(MainActivity.this);
                    }

                    @Override
                    public void onComplete() {}
                });
    }

    private void registerBroadCast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        intentFilter.addAction(BaseConstants.GESTURE_AI_SERVICE_CHECK_ACTION);
        intentFilter.addAction(BaseConstants.ACTION_CRASH_MSG);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        careBroadcastReceiver = new CareBroadcastReceiver();
        registerReceiver(careBroadcastReceiver, intentFilter);
    }

    private void unRegisterBroadCast() {
        if(careBroadcastReceiver != null)
            unregisterReceiver(careBroadcastReceiver);
    }

    private CareBroadcastReceiver careBroadcastReceiver;

    class CareBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())){
                onNetChange();
            }else if(BaseConstants.GESTURE_AI_SERVICE_CHECK_ACTION.equals(intent.getAction())){
                bindAiServiceAndCheckStart();
            }else if(BaseConstants.ACTION_CRASH_MSG.equals(intent.getAction())){
                String crashMsg = intent.getStringExtra(BaseConstants.EXTRA_CRASH_MSG);
                String pkg = intent.getStringExtra(BaseConstants.EXTRA_CRASH_PKG);
                CareLog.i(TAG, "crashMsg: " + crashMsg);
                CareLog.i(TAG, "pkg: " + pkg);
                if(crashMsg != null){
                    mainViewModel.reqUploadLog(crashMsg, pkg);
                }
            }else if(Intent.ACTION_SCREEN_ON.equals(intent.getAction())){
                CareLog.i(TAG, "ACTION_SCREEN_ON" );
                if(ZeeServiceManager.isSettingGestureAIEnable()){
                    ZeeServiceManager.getInstance().startGestureAi(isLastGestureAIActive);
                }
            }else if(Intent.ACTION_SCREEN_OFF.equals(intent.getAction())){
                CareLog.i(TAG, "ACTION_SCREEN_OFF" );
                if(ZeeServiceManager.isSettingGestureAIEnable()){
                    isLastGestureAIActive = ZeeServiceManager.getInstance().isGestureAIActive();
                    ZeeServiceManager.getInstance().stopGestureAi();
                }

            }
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
                if (msg.what == MSG_CHECK_NET_API) {
                    checkNetApiCount--;
                    if(checkNetApiCount > 0)
                        ((MainActivity) activity).checkManagerAppUpgradeApi();
                }
            }
        }
    }
}