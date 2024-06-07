package com.zee.launcher.home.ui.fitness;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;

import com.zee.launcher.home.HomeApplication;
import com.zee.launcher.home.R;
import com.zee.launcher.home.dialog.CameraAlertDialog;
import com.zee.launcher.home.gesture.config.Config;
import com.zee.launcher.home.service.GestureCameraService;
import com.zee.launcher.home.ui.direct.DirectLoadingActivity;
import com.zee.launcher.home.ui.helong.PageTurningActivity;
import com.zee.launcher.home.widgets.BackView;
import com.zee.launcher.home.widgets.PluginItemView;
import com.zeewain.base.ui.BaseActivity;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.DensityUtils;

import java.util.ArrayList;
import java.util.Set;

public class NationalFitnessActivity extends BaseActivity implements View.OnFocusChangeListener, GestureCameraService.GestureListener, CameraAlertDialog.OnClickListener {

    private ArrayList<String> skuIds;
    private PluginItemView pluginItemDG;
    private PluginItemView pluginItemTZB;
    private BackView backView;
    private boolean isOpenPluginItem = false;
    private CameraAlertDialog cameraAlertDialog;
    private final Handler mHandler = new Handler(Looper.myLooper());

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

        DensityUtils.autoWidth(getApplication(), this);
        setContentView(R.layout.activity_national_fitness);

        skuIds = getIntent().getStringArrayListExtra("skuIds");
        if (skuIds == null || skuIds.size() < 2) {
            finish();
            return;
        }

        initView();
        initListener();
    }

    private void initView() {
        pluginItemDG = findViewById(R.id.plugin_item_dg);
        pluginItemDG.setPluginItemBackground(R.drawable.plugin_dg_anim);
        pluginItemDG.setPluginItemGestureTip(R.mipmap.img_gesture_left_hand);
        pluginItemDG.setOnFocusChangeListener(this);

        pluginItemTZB = findViewById(R.id.plugin_item_tzb);
        pluginItemTZB.setPluginItemBackground(R.drawable.plugin_tzb_anim);
        pluginItemTZB.setPluginItemGestureTip(R.mipmap.img_gesture_right_hand);
        pluginItemTZB.setOnFocusChangeListener(this);

        backView = findViewById(R.id.back_view_national_fitness);
        backView.setOnFocusChangeListener(this);

        pluginItemDG.postDelayed(new Runnable() {
            @Override
            public void run() {
                pluginItemDG.setVisibility(View.VISIBLE);
                pluginItemDG.startPluginItemAnim();
                pluginItemTZB.setVisibility(View.VISIBLE);
                pluginItemTZB.startPluginItemAnim();
            }
        }, 500);
    }

    private void initListener() {
        pluginItemDG.setOnClickListener(v -> {
            isOpenPluginItem = true;
            HomeApplication.getInstance().gestureCameraService.pauseGesture();
            Intent intent = new Intent();
            intent.setClass(v.getContext(), DirectLoadingActivity.class);
            intent.putExtra("skuId", skuIds.get(1));
            startActivity(intent);
        });

        pluginItemDG.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DPAD_UP && event.getAction() == KeyEvent.ACTION_DOWN) {
                v.clearAnimation();
                v.startAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.host_shake_y));
            }
            return false;
        });

        pluginItemTZB.setOnClickListener(v -> {
            isOpenPluginItem = true;
            HomeApplication.getInstance().gestureCameraService.pauseGesture();
            Intent intent = new Intent();
            intent.setClass(v.getContext(), DirectLoadingActivity.class);
            intent.putExtra("skuId", skuIds.get(0));
            startActivity(intent);
        });

        pluginItemTZB.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && event.getAction() == KeyEvent.ACTION_DOWN) {
                v.clearAnimation();
                v.startAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.host_shake));
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP && event.getAction() == KeyEvent.ACTION_DOWN) {
                v.clearAnimation();
                v.startAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.host_shake_y));
            }
            return false;
        });

        backView.setOnClickListener(v -> finish());
        backView.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && event.getAction() == KeyEvent.ACTION_DOWN) {
                v.clearAnimation();
                v.startAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.host_shake));
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && event.getAction() == KeyEvent.ACTION_DOWN) {
                v.clearAnimation();
                v.startAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.host_shake_y));
            }
            return false;
        });
    }


    private void showCameraAlert(int errorType) {

        cameraAlertDialog = new CameraAlertDialog(this);
        cameraAlertDialog.setOnClickListener(this);

        cameraAlertDialog.setErrorType(errorType);
        cameraAlertDialog.show();
    }


    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            CommonUtils.scaleView(v, 1.15f);
        } else {
            v.clearAnimation();
            CommonUtils.scaleView(v, 1f);
        }
    }

    @Override
    protected void onResume() {
        Log.e("wang", "onResume: ness");
        super.onResume();
        if (isOpenPluginItem) {
            HomeApplication.getInstance().gestureCameraService.resumeGesture();
        } else {
            pluginItemDG.post(() -> HomeApplication.getInstance().gestureCameraService.setGestureListener(NationalFitnessActivity.this, 2));

        }
    }

    @Override
    protected void onDestroy() {
        if (pluginItemDG != null && pluginItemDG.getHandler() != null) {
            pluginItemDG.getHandler().removeCallbacksAndMessages(null);
        }
        super.onDestroy();
    }

    @Override
    public void onLeftHandUpProgress(int progress) {
        pluginItemTZB.startLoading(progress);
        if (progress == GestureCameraService.classAndPluginProgress) {
            isOpenPluginItem = true;
            HomeApplication.getInstance().gestureCameraService.pauseGesture();
            Intent intent = new Intent();
            intent.setClass(NationalFitnessActivity.this, DirectLoadingActivity.class);
            intent.putExtra("skuId", skuIds.get(0));
            startActivity(intent);
        }
    }

    @Override
    public void onRightHandUpProgress(int progress) {
        pluginItemDG.startLoading(progress);
        if (progress == GestureCameraService.classAndPluginProgress) {
            isOpenPluginItem = true;
            HomeApplication.getInstance().gestureCameraService.pauseGesture();
            Intent intent = new Intent();
            intent.setClass(NationalFitnessActivity.this, DirectLoadingActivity.class);
            intent.putExtra("skuId", skuIds.get(1));
            startActivity(intent);
        }
    }

    @Override
    public void onExpandProgress(int progress) {

    }

    @Override
    public void onThrowbackProgress(int progress) {
        backView.startLoading(progress);
        if (progress == GestureCameraService.backProgress) {
            finish();
        }
    }

    @Override
    public void onSlipLeft() {

    }

    @Override
    public void onSlipRight() {

    }

    @Override
    public void onError(int errType) {
        runOnUiThread(() -> showCameraAlert(errType));
    }

    @Override
    public void onConfirm(View v, int errType) {
        switch (errType) {
            case Config.CameraError_EMPTY:
                break;
            case Config.CameraError_INVALID:
            case Config.CameraError_ERROR:
                mHandler.postDelayed(() -> HomeApplication.getInstance().gestureCameraService.resumeHideGesture(), 1000);
                break;
            case Config.CameraError_UnKNOW:
                break;
        }
    }
}