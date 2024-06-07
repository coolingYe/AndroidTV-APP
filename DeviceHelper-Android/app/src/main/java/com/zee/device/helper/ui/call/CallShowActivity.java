package com.zee.device.helper.ui.call;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.card.MaterialCardView;
import com.zee.device.base.config.BaseConstants;
import com.zee.device.base.utils.DensityUtils;
import com.zee.device.base.utils.SPUtils;
import com.zee.device.helper.ZeeApplication;
import com.zee.wireless.camera.R;

import org.webrtc.SurfaceViewRenderer;

public class CallShowActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "CallNoActivity";
    private SurfaceViewRenderer mLocalSurfaceView;
    private boolean isBack = false;
    private CareBroadcastReceiver careBroadcastReceiver;

    private TextView tvCameraScale360p;
    private TextView tvCameraScale720p;
    private ImageView ivResolution;
    private LinearLayout llResolution;
    private void switchCamera() {
        ZeeApplication.getInstance().mService.switchCamera();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        hideSystemUI();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        DensityUtils.autoWidth(getApplication(), this);
        setContentView(R.layout.activity_call);
        ImageView back = findViewById(R.id.iv_back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isBack = true;
                finish();
            }
        });

        ImageView imgWindow = findViewById(R.id.camera_unfull);
        imgWindow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                ZeeApplication.getInstance().mService.showAll();
            }
        });

        llResolution = findViewById(R.id.ll_resolution);
        ivResolution =findViewById(R.id.camera_resolution);
        tvCameraScale360p = findViewById(R.id.tv_camera_scale_360p);
        tvCameraScale720p = findViewById(R.id.tv_camera_scale_720p);
        tvCameraScale360p.setOnClickListener(this);
        tvCameraScale720p.setOnClickListener(this);
        ivResolution.setOnClickListener(this);

        MaterialCardView cardCameraSwitchView = findViewById(R.id.card_camera_switch);
        cardCameraSwitchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchCamera();
            }
        });
        mLocalSurfaceView = findViewById(R.id.localSurfaceView);
        mLocalSurfaceView.init(ZeeApplication.getInstance().mService.mRootEglBase.getEglBaseContext(), null);
        mLocalSurfaceView.setMirror(true);
        if (ZeeApplication.getInstance().mService.frontCameraId == null) {
            cardCameraSwitchView.setVisibility(View.GONE);
        }
        ZeeApplication.getInstance().mService.mVideoTrack.addSink(mLocalSurfaceView); // 设置渲染到本地surfaceview上

        int currentWidth = ZeeApplication.getInstance().mService.getCurrentWidth();
        switch (currentWidth) {
            case 360:
                tvCameraScale360p.setTextColor(0xffb880ff);
                tvCameraScale720p.setTextColor(0xff999999);
                break;
            case 720:
                tvCameraScale360p.setTextColor(0xff999999);
                tvCameraScale720p.setTextColor(0xffb880ff);
                break;
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ZeeApplication.getInstance().mService.hideAll();
        registerCareReceiver();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!isBack) ZeeApplication.getInstance().mService.showAll();
        unregisterCareReceiver();
    }

    @Override
    protected void onDestroy() {
        ZeeApplication.getInstance().mService.mVideoTrack.removeSink(mLocalSurfaceView);
        mLocalSurfaceView.release();
        if (isBack) {
            ZeeApplication.getInstance().unBindService();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        isBack = true;
        super.onBackPressed();
    }

    private void registerCareReceiver() {
        careBroadcastReceiver = new CareBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BaseConstants.ACTION_EXIT_WEBRTC_PREVIEW);
        registerReceiver(careBroadcastReceiver, filter);
    }

    private void unregisterCareReceiver() {
        if (careBroadcastReceiver != null) {
            unregisterReceiver(careBroadcastReceiver);
        }
    }

    private boolean checkClick = false;
    private boolean hasResolution = false;
    @Override
    public void onClick(View view) {
        if (view.getId() == ivResolution.getId()) {
            hasResolution = !hasResolution;
            llResolution.setVisibility(hasResolution? View.VISIBLE : View.GONE);
        } else if (view.getId() == tvCameraScale360p.getId()) {
            if (ZeeApplication.getInstance().mService.getCurrentWidth() != BaseConstants.VIDEO_RESOLUTION_WIDTH_360) {
                ZeeApplication.getInstance().mService.changeCameraCaptureScale(BaseConstants.VIDEO_RESOLUTION_WIDTH_360, BaseConstants.VIDEO_RESOLUTION_HEIGHT_600);
                SPUtils.getInstance().put("VIDEO_CURRENT_WIDTH", BaseConstants.VIDEO_RESOLUTION_WIDTH_360);
                SPUtils.getInstance().put("VIDEO_CURRENT_HEIGHT", BaseConstants.VIDEO_RESOLUTION_HEIGHT_600);
                tvCameraScale360p.setTextColor(0xffb880ff);
                tvCameraScale720p.setTextColor(0xff999999);
                ZeeApplication.getInstance().mService.setCurrentWidth(BaseConstants.VIDEO_RESOLUTION_WIDTH_360);
                ZeeApplication.getInstance().mService.setCurrentHeight(BaseConstants.VIDEO_RESOLUTION_HEIGHT_600);
            }
        } else if (view.getId() == tvCameraScale720p.getId()) {
            if (ZeeApplication.getInstance().mService.getCurrentWidth() != BaseConstants.VIDEO_RESOLUTION_WIDTH_720) {
                ZeeApplication.getInstance().mService.changeCameraCaptureScale(BaseConstants.VIDEO_RESOLUTION_WIDTH_720, BaseConstants.VIDEO_RESOLUTION_HEIGHT_1280);
                SPUtils.getInstance().put("VIDEO_CURRENT_WIDTH", BaseConstants.VIDEO_RESOLUTION_WIDTH_720);
                SPUtils.getInstance().put("VIDEO_CURRENT_HEIGHT", BaseConstants.VIDEO_RESOLUTION_HEIGHT_1280);
                tvCameraScale360p.setTextColor(0xff999999);
                tvCameraScale720p.setTextColor(0xffb880ff);
                ZeeApplication.getInstance().mService.setCurrentWidth(BaseConstants.VIDEO_RESOLUTION_WIDTH_720);
                ZeeApplication.getInstance().mService.setCurrentHeight(BaseConstants.VIDEO_RESOLUTION_HEIGHT_1280);
            }
        }
    }

    private void scaleVisibility(View view1, View view2) {
        if (checkClick) {
            checkClick = false;
            view1.setVisibility(View.GONE);
            view2.setVisibility(View.GONE);
        } else {
            checkClick = true;
            view1.setVisibility(View.VISIBLE);
            view2.setVisibility(View.VISIBLE);
        }
    }

    class CareBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BaseConstants.ACTION_EXIT_WEBRTC_PREVIEW.equals(intent.getAction())) {
                finish();
            }
        }
    }
}