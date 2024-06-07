package com.zee.launcher.home;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.zee.launcher.home.dialog.CameraAlertDialog;
import com.zee.launcher.home.gesture.zeewainpose.Config;
import com.zee.launcher.home.utils.DeviceUtils;
import com.zeewain.base.utils.CommonUtils;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.zee.launcher.home.data.DataRepository;
import com.zee.launcher.home.data.layout.GlobalLayout;
import com.zee.launcher.home.service.FloatingCameraService;
import com.zee.launcher.home.ui.direct.DirectLoadingActivity;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.model.LoadState;
import com.zeewain.base.ui.BaseActivity;
import com.zeewain.base.utils.DensityUtils;
import com.zeewain.base.utils.EthernetUtils;
import com.zeewain.base.utils.SystemProperties;
import com.zeewain.base.widgets.LoadingView;
import com.zeewain.base.widgets.NetworkErrView;

import java.util.List;

public class MainActivity extends BaseActivity {
    private MainViewModel viewModel;
    private LoadingView loadingViewHomeClassic;
    private NetworkErrView networkErrViewHomeClassic;
    public static String skuid1 = "";
    public static String skuid2 = "";
    private StateReceiver stateReceiver;
    private boolean isAdv = false;
    private boolean showError = true;
    private String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainViewModelFactory factory = new MainViewModelFactory(DataRepository.getInstance());
        viewModel = new ViewModelProvider(this, factory).get(MainViewModel.class);
        DensityUtils.autoWidth(getApplication(), this);
        setContentView(R.layout.activity_main);
        loadingViewHomeClassic = findViewById(R.id.loadingView_home_classic);
        networkErrViewHomeClassic = findViewById(R.id.networkErrView_home_classic);
        initListener();
        initViewObservable();
        setSerialNum();
        setMacProperties();
        if (!CommonUtils.isUserLogin()) {
            viewModel.reqTouristLogin(CommonUtils.getDeviceSn());
        } else {
            viewModel.reqServicePackInfo();
        }
    }

    private void setSerialNum() {
        String serialNum = DeviceUtils.getSerialNum();
        if (!TextUtils.isEmpty(serialNum)) {
            SystemProperties.set("persist.sys.zee.device.serialno", serialNum);
        } else {
            Log.i(TAG, "获取序列号失败");
        }
    }

    private void setMacProperties() {
        String macAddress = EthernetUtils.getEthernetMac();
        SystemProperties.set("persist.sys.zee.device.wired_mac", macAddress);

        macAddress = EthernetUtils.getWifiMac();
        SystemProperties.set("persist.sys.zee.device.wireless_mac", macAddress);
    }

    private void startReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("floatCamera");
        stateReceiver = new StateReceiver();
        registerReceiver(stateReceiver, filter);
    }

    private void startAdvertisement() {
        if (!isAdv) {
            isAdv = true;
            Intent intent = getPackageManager().getLaunchIntentForPackage(BaseConstants.AdV_APP_PAGE_NAME);
            if (intent != null) {
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.setAction(Intent.ACTION_MAIN);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                startActivity(intent);
            } else {
                showToast("发布系统没有安装!");
            }
        }
    }

    private void initView(GlobalLayout globalLayout) {
        if (globalLayout != null) {
            if (globalLayout.layout.pages.size() > 0) {
                if (globalLayout.layout.pages.get(0).content.size() > 0) {
                    if (globalLayout.layout.pages.get(0).content.get(0).config != null) {
                        if (globalLayout.layout.pages.get(0).content.get(0).config.appSkus.size() > 0) {
                            if (globalLayout.layout.pages.get(0).content.get(0).config.appSkus.size() >= 2) {
                                skuid1 = globalLayout.layout.pages.get(0).content.get(0).config.appSkus.get(0);
                                skuid2 = globalLayout.layout.pages.get(0).content.get(0).config.appSkus.get(1);
                                Log.e(TAG, "initView: " + skuid1 + skuid2);
                                startNext();
                            }
                        }
                    }
                }
            }
        }
    }

    private void startNext() {
        startReceiver();
        // 开启手势服务
        if (!FloatingCameraService.isStart) {
            startFloatingService();
        } else {
            startAdvertisement();
        }
    }

    @SuppressLint({"ResourceType", "SetTextI18n"})
    private void initListener() {
        networkErrViewHomeClassic.setRetryClickListener(() -> {
            viewModel.reqServicePackInfo();
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void initViewObservable() {
        viewModel.mldServicePackInfoLoadState.observe(this, loadState -> {
            if (LoadState.Loading == loadState) {
                loadingViewHomeClassic.setVisibility(View.VISIBLE);
                networkErrViewHomeClassic.setVisibility(View.GONE);
                loadingViewHomeClassic.startAnim();
            } else if (LoadState.Success == loadState) {
                loadingViewHomeClassic.stopAnim();
                loadingViewHomeClassic.setVisibility(View.GONE);
                networkErrViewHomeClassic.setVisibility(View.GONE);
                initView(viewModel.globalLayout);
            } else {
                loadingViewHomeClassic.stopAnim();
                loadingViewHomeClassic.setVisibility(View.GONE);
                networkErrViewHomeClassic.setVisibility(View.VISIBLE);
            }
        });

        viewModel.mldTouristLoginState.observe(this, new Observer<LoadState>() {
            @Override
            public void onChanged(LoadState loadState) {
                if (LoadState.Success == loadState) {
                    Log.e(TAG, "登录成功");
                    viewModel.reqServicePackInfo();
                } else if (LoadState.Failed == loadState) {
                    Log.e(TAG, "登录失败 3秒后重试");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.e(TAG, "登录重试: ");
                            viewModel.reqTouristLogin(CommonUtils.getDeviceSn());
                        }
                    }, 3000);

                }
            }
        });

        viewModel.mldToastMsg.observe(this, this::showToast);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    public void startFloatingService() {
        if (!Settings.canDrawOverlays(this)) {
            showToast("当前无权限，请授权");
            startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())), 0);
        } else {
            Intent gestureIntent = new Intent(getApplicationContext(), FloatingCameraService.class);
            startService(gestureIntent);
        }
    }


    public void stopFloatingService() {
        if (Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(getApplicationContext(), FloatingCameraService.class);
            stopService(intent);
        }
    }

    @Override
    protected void onResume() {
        Log.e(TAG, "MainActivity onResume: ");
        if (!FloatingCameraService.isStart && isAdv) {
            Log.e(TAG, "onResume: 启动 isServiceRunning" + FloatingCameraService.isStart + " isAdv " + isAdv);
            throw new IllegalArgumentException();
        } else {
            Log.e(TAG, "onResume: 不启动 isServiceRunning" + FloatingCameraService.isStart + " isAdv " + isAdv);
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "MainActivity onDestroy: ");
//        stopFloatingService();
        if (stateReceiver != null) {
            unregisterReceiver(stateReceiver);
        }
        super.onDestroy();
    }


    public class StateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String intentAction = intent.getAction();
            if (intentAction.equals("floatCamera")) {
                String state = intent.getStringExtra("state");
                if (state.equals("close")) {
                    Intent dirIntent = new Intent(getApplication(), DirectLoadingActivity.class);
                    String skuIdType = intent.getStringExtra("skuIdType");
                    if (skuIdType.equals("left")) {
                        dirIntent.putExtra("skuId", skuid1);
                    } else {
                        dirIntent.putExtra("skuId", skuid2);
                    }
                    startActivity(dirIntent);
                    stopFloatingService();
                } else if (state.equals("start")) {
                    Log.e(TAG, "onReceive: " + "startAdv");
                    startAdvertisement();
                } else if (state.equals("error")) {
                    int errorType = intent.getIntExtra("errorType", 0);
                    Log.e(TAG, "onReceive: " + "error" + errorType);
                    if (showError) {
                        if (errorType == Config.ShowCode.CODE_CAMERA_INVALID) {
                            showToast("未找到可使用的摄像头！");
                        } else {
                            showToast("摄像头异常，请重新插拔USB摄像头或者重启设备！");
                        }
                        showError = false;
                        handleCameraErrExit(errorType);
                    }
                } else if (state.equals("authError")) {
                    CameraAlertDialog cameraAlertDialog = new CameraAlertDialog(context);
                    cameraAlertDialog.setOnClickListener(new CameraAlertDialog.OnClickListener() {
                        @Override
                        public void onConfirm(View v) {
                            try {
                                Intent intent = new Intent();
                                intent.setAction(BaseConstants.ZEE_SETTINGS_ACTIVITY_ACTION);
                                context.startActivity(intent);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    cameraAlertDialog.show();
                }
            }
        }
    }

    private void handleCameraErrExit(int err) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isIntentExisting(MainActivity.this, Config.ACTION_START_SHOW)) {
                    startShowAction(err);
                }
                finish();
            }
        }, 500);
    }

    public boolean isIntentExisting(Context context, String action) {
        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent(action);
        List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (resolveInfo.size() > 0) {
            return true;
        }
        return false;
    }

    private void startShowAction(int code) {
        Intent intent = new Intent();
        intent.setAction(Config.ACTION_START_SHOW);
        intent.putExtra(Config.EXTRA_SHOW_ACTION, code);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

}