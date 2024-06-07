package com.zee.launcher.home;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AnimationUtils;

import com.zee.launcher.home.dialog.CameraAlertDialog;
import com.zee.launcher.home.gesture.config.Config;
import com.zee.launcher.home.service.GestureCameraService;
import com.zee.launcher.home.ui.direct.DirectLoadingActivity;
import com.zee.launcher.home.ui.fitness.NationalFitnessActivity;
import com.zee.launcher.home.ui.helong.PageTurningActivity;
import com.zee.launcher.home.widgets.ClassicItemView;
import com.zeewain.base.utils.CommonUtils;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;

import com.zee.launcher.home.data.DataRepository;
import com.zeewain.base.model.LoadState;
import com.zeewain.base.ui.BaseActivity;
import com.zeewain.base.utils.DensityUtils;
import com.zeewain.base.widgets.LoadingView;
import com.zeewain.base.widgets.NetworkErrView;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends BaseActivity implements View.OnFocusChangeListener, GestureCameraService.GestureListener, CameraAlertDialog.OnClickListener {
    private static final String TAG = "MainActivity";
    private MainViewModel viewModel;
    private LoadingView loadingViewHomeClassic;
    private NetworkErrView networkErrViewHomeClassic;
    private ClassicItemView classicItemHL;
    private ClassicItemView classicItemJD;
    private ClassicItemView classicItemJS;
    private ConstraintLayout clMainContentRoot;
    private CameraAlertDialog cameraAlertDialog;
    private Handler mHandler = new Handler(Looper.myLooper());
    private int lastOpenId = 0;

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        savedInstanceState.setClassLoader(getClass().getClassLoader());
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(savedInstanceState != null) {
            savedInstanceState.setClassLoader(getClass().getClassLoader());
            Bundle bundle = savedInstanceState.getBundle("androidx.lifecycle.BundlableSavedStateRegistry.key");
            if(bundle != null) {
                Set<String> keySet = bundle.keySet();
                if (keySet != null) {
                    for(String key: keySet){
                        Object object = bundle.get(key);
                        if(object instanceof Bundle){
                            ((Bundle)object).setClassLoader(getClass().getClassLoader());
                        }
                    }
                }
            }
        }
        super.onCreate(savedInstanceState);
        MainViewModelFactory factory = new MainViewModelFactory(DataRepository.getInstance());
        viewModel = new ViewModelProvider(this, factory).get(MainViewModel.class);
        DensityUtils.autoWidth(getApplication(), this);
        setContentView(R.layout.activity_main);
        initView();
        initListener();
        initViewObservable();
        Log.e(TAG, "onCreate: ");
        if (!CommonUtils.isUserLogin()) {
            viewModel.reqTouristLogin(CommonUtils.getDeviceSn());
        } else {
            viewModel.reqServicePackInfo();
        }
    }

    private void initView() {
        loadingViewHomeClassic = findViewById(R.id.loadingView_home_classic);
        networkErrViewHomeClassic = findViewById(R.id.networkErrView_home_classic);
        clMainContentRoot = findViewById(R.id.cl_main_content_root);

        classicItemHL = findViewById(R.id.classic_item_hl);
        classicItemHL.setClassicItemBackground(R.drawable.hl_item_anim);
        classicItemHL.setClassicItemGestureTip(R.mipmap.img_gesture_left_hand);
        classicItemHL.setOnFocusChangeListener(this);

        classicItemJD = findViewById(R.id.classic_item_jd);
        classicItemJD.setClassicItemBackground(R.drawable.jd_item_anim);
        classicItemJD.setClassicItemGestureTip(R.mipmap.img_gesture_right_hand);
        classicItemJD.setOnFocusChangeListener(this);

        classicItemJS = findViewById(R.id.classic_item_js);
        classicItemJS.setClassicItemBackground(R.drawable.js_item_anim);
        classicItemJS.setClassicItemGestureTip(R.mipmap.img_gesture_arm_extension);
        classicItemJS.setOnFocusChangeListener(this);

    }

    private void initListener() {
        networkErrViewHomeClassic.setRetryClickListener(() -> {
            viewModel.tryReqServicePackInfoTimes = 3;
            viewModel.reqServicePackInfo();
        });

        classicItemHL.setOnClickListener(v -> {
            if (HomeApplication.getInstance().gestureCameraService == null) return;
            HomeApplication.getInstance().gestureCameraService.camera2Helper.runClassifier = false;
            lastOpenId = classicItemHL.getId();
            Intent intent = new Intent();
            intent.setClass(v.getContext(), PageTurningActivity.class);
            startActivity(intent);
        });

        classicItemHL.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && event.getAction() == KeyEvent.ACTION_DOWN) {
                v.clearAnimation();
                v.startAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.host_shake));
            } else if ((keyCode == KeyEvent.KEYCODE_DPAD_UP | keyCode == KeyEvent.KEYCODE_DPAD_DOWN) && event.getAction() == KeyEvent.ACTION_DOWN) {
                v.clearAnimation();
                v.startAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.host_shake_y));
            }
            return false;
        });

        classicItemJD.setOnClickListener(v -> {
            if (HomeApplication.getInstance().gestureCameraService == null) return;
            HomeApplication.getInstance().gestureCameraService.pauseGesture();
            lastOpenId = classicItemJD.getId();
            Intent intent = new Intent();
            intent.setClass(v.getContext(), DirectLoadingActivity.class);
            intent.putExtra("skuId", viewModel.knowledgeQuizSkuId);
            startActivity(intent);
        });

        classicItemJD.setOnKeyListener((v, keyCode, event) -> {
            if ((keyCode == KeyEvent.KEYCODE_DPAD_UP | keyCode == KeyEvent.KEYCODE_DPAD_DOWN) && event.getAction() == KeyEvent.ACTION_DOWN) {
                v.clearAnimation();
                v.startAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.host_shake_y));
            }
            return false;
        });

        classicItemJS.setOnClickListener(v -> {
            if (HomeApplication.getInstance().gestureCameraService == null) return;
            HomeApplication.getInstance().gestureCameraService.camera2Helper.runClassifier = false;
            lastOpenId = classicItemJS.getId();
            Intent intent = new Intent();
            intent.setClass(v.getContext(), NationalFitnessActivity.class);
            intent.putExtra("skuIds", (ArrayList<String>) viewModel.getSkuIdListByPageName("全民健身"));
            startActivity(intent);
        });

        classicItemJS.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && event.getAction() == KeyEvent.ACTION_DOWN) {
                v.clearAnimation();
                v.startAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.host_shake));
            } else if ((keyCode == KeyEvent.KEYCODE_DPAD_UP | keyCode == KeyEvent.KEYCODE_DPAD_DOWN) && event.getAction() == KeyEvent.ACTION_DOWN) {
                v.clearAnimation();
                v.startAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.host_shake_y));
            }
            return false;
        });
    }

    private void initViewObservable() {
        viewModel.mldServicePackInfoLoadState.observe(this, loadState -> {
            if (LoadState.Loading == loadState) {
                if (View.VISIBLE != loadingViewHomeClassic.getVisibility()) {
                    loadingViewHomeClassic.setVisibility(View.VISIBLE);
                    loadingViewHomeClassic.startAnim();
                    networkErrViewHomeClassic.setVisibility(View.GONE);
                    clMainContentRoot.setVisibility(View.GONE);
                }
            } else if (LoadState.Success == loadState) {
                loadingViewHomeClassic.stopAnim();
                loadingViewHomeClassic.setVisibility(View.GONE);
                networkErrViewHomeClassic.setVisibility(View.GONE);
                clMainContentRoot.setVisibility(View.VISIBLE);

                classicItemHL.startClassicItemAnim();
                classicItemJD.startClassicItemAnim();
                classicItemJS.startClassicItemAnim();

                startGestureService();
            } else {
                if (viewModel.tryReqServicePackInfoTimes > 0) {
                    viewModel.reqServicePackInfo();
                } else {
                    loadingViewHomeClassic.stopAnim();
                    loadingViewHomeClassic.setVisibility(View.GONE);
                    networkErrViewHomeClassic.setVisibility(View.VISIBLE);
                    networkErrViewHomeClassic.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            networkErrViewHomeClassic.retryBtnRequestFocus();
                        }
                    }, 100);

                }
            }
        });

        viewModel.mldTouristLoginState.observe(this, loadState -> {
            if (LoadState.Success == loadState) {
                viewModel.reqServicePackInfo();
            } else if (LoadState.Failed == loadState) {
                loadingViewHomeClassic.getHandler().removeCallbacksAndMessages(null);
                loadingViewHomeClassic.postDelayed(() -> viewModel.reqTouristLogin(CommonUtils.getDeviceSn()), 2000);
            } else {
                if (View.VISIBLE != loadingViewHomeClassic.getVisibility()) {
                    loadingViewHomeClassic.setVisibility(View.VISIBLE);
                    loadingViewHomeClassic.startAnim();
                    networkErrViewHomeClassic.setVisibility(View.GONE);
                }
            }
        });
        viewModel.mldToastMsg.observe(this, this::showToast);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        Log.e(TAG, "onFocusChange: ");
        if (hasFocus) {
            CommonUtils.scaleView(v, 1.15f);
        } else {
            v.clearAnimation();
            CommonUtils.scaleView(v, 1f);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void startGestureService() {
        if (HomeApplication.getInstance().gestureCameraService != null) {
            HomeApplication.getInstance().gestureCameraService.clearGestureListener();
            HomeApplication.getInstance().gestureCameraService.pauseGesture();
            HomeApplication.getInstance().gestureCameraService.stopSelf();
            HomeApplication.getInstance().unBindService();
        }
        startNewGestureService();
    }

    private void startNewGestureService() {
        if (isHaveCamera()) {
            if (!Settings.canDrawOverlays(this)) {
                startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())), 0);
            } else {
                try {
                    HomeApplication.getInstance().serviceConnection = new ServiceConnection() {
                        @Override
                        public void onServiceConnected(ComponentName name, IBinder service) {
                            if (service != null) {
                                GestureCameraService.ServiceBinder binder = (GestureCameraService.ServiceBinder) service;
                                HomeApplication.getInstance().gestureCameraService = binder.getService();
                                if (HomeApplication.getInstance().gestureCameraService.camera2Helper != null) {
                                    HomeApplication.getInstance().gestureCameraService.setGestureListener(MainActivity.this, 0);
                                    Log.d(TAG, "onServiceConnected: ");
                                } else {
                                    HomeApplication.getInstance().gestureCameraService = null;
                                    HomeApplication.getInstance().serviceConnection = null;
                                }
                            }
                        }

                        @Override
                        public void onServiceDisconnected(ComponentName name) {
                            Log.d(TAG, "onServiceDisconnected: ");
                        }
                    };

                } catch (Exception e) {
                    e.printStackTrace();
                }
                HomeApplication.getInstance().bindGService();
            }
        } else {
            showCameraAlert(Config.CameraError_EMPTY);
        }
    }

    private void showCameraAlert(int errorType) {
        cameraAlertDialog = new CameraAlertDialog(this);
        cameraAlertDialog.setOnClickListener(this);
        cameraAlertDialog.setErrorType(errorType);
        cameraAlertDialog.show();
    }


    public void stopFloatingService() {
        HomeApplication.getInstance().unBindService();
    }

    private boolean isHaveCamera() {
        try {
            CameraManager mCameraManager = (CameraManager) this.getSystemService(Context.CAMERA_SERVICE);
            String[] cameraIdList = mCameraManager.getCameraIdList();
            return cameraIdList != null && cameraIdList.length > 0;
        } catch (Exception ignored) {

        }
        return false;
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume: ");
        if (lastOpenId == classicItemHL.getId()) {
            HomeApplication.getInstance().gestureCameraService.setGestureListener(this, 0);
            lastOpenId = 0;
        } else if (lastOpenId == classicItemJD.getId()) {
            HomeApplication.getInstance().gestureCameraService.resumeGesture();
            lastOpenId = 0;
        } else if (lastOpenId == classicItemJS.getId()) {
            if (HomeApplication.getInstance().gestureCameraService.camera2Helper.runClassifier) {
                HomeApplication.getInstance().gestureCameraService.setResumeGestureListener(this, 0);
                lastOpenId = 0;
            }
        }
        super.onResume();
    }


    @Override
    protected void onPause() {
        Log.i(TAG, "onPause: ");
        super.onPause();
    }


    @Override
    protected void onDestroy() {
        Log.i(TAG, "main onDestroy: ");
        if (loadingViewHomeClassic != null && loadingViewHomeClassic.getHandler() != null) {
            loadingViewHomeClassic.getHandler().removeCallbacksAndMessages(null);
        }
        super.onDestroy();
    }


    @Override
    protected void onNewIntent(Intent intent) {
        Log.e(TAG, "onNewIntent: ");
        super.onNewIntent(intent);
    }

    @Override
    public void onLeftHandUpProgress(int progress) {
        Log.i(TAG, "onLeftHandUpProgress: " + progress);
        classicItemJD.startLoading(progress);
        if (progress == GestureCameraService.classAndPluginProgress) {
            HomeApplication.getInstance().gestureCameraService.pauseGesture();
            lastOpenId = classicItemJD.getId();
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, DirectLoadingActivity.class);
            intent.putExtra("skuId", viewModel.knowledgeQuizSkuId);
            startActivity(intent);
        }
    }

    @Override
    public void onRightHandUpProgress(int progress) {
        Log.i(TAG, "onRightHandUpProgress: " + progress);
        classicItemHL.startLoading(progress);
        if (progress == GestureCameraService.classAndPluginProgress) {
            lastOpenId = classicItemHL.getId();
//            HomeApplication.getInstance().gestureCameraService.showGestureView(false);
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, PageTurningActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onExpandProgress(int progress) {
        Log.i(TAG, "onExpandProgress: " + progress);
        classicItemJS.startLoading(progress);
        if (progress == GestureCameraService.classAndPluginProgress) {
            lastOpenId = classicItemJS.getId();
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, NationalFitnessActivity.class);
            intent.putExtra("skuIds", (ArrayList<String>) viewModel.getSkuIdListByPageName("全民健身"));
            startActivity(intent);
        }
    }

    @Override
    public void onThrowbackProgress(int progress) {

    }

    @Override
    public void onSlipLeft() {

    }

    @Override
    public void onSlipRight() {

    }

    @Override
    public void onError(int errType) {
        mHandler.post(() -> showCameraAlert(errType));
    }

    @Override
    public void onConfirm(View v, int errType) {
        switch (errType) {
            case Config.CameraError_EMPTY:
                mHandler.postDelayed(this::startGestureService, 1000);
                break;
            case Config.CameraError_ERROR:
            case Config.CameraError_INVALID:
                mHandler.postDelayed(() -> HomeApplication.getInstance().gestureCameraService.resumeGesture(), 1000);
                break;
            case Config.CameraError_UnKNOW:
                break;
        }
    }
}