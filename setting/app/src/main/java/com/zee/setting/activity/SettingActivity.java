package com.zee.setting.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.zee.setting.BuildConfig;
import com.zee.setting.R;
import com.zee.setting.base.BaseActivity;
import com.zee.setting.base.BaseConstants;
import com.zee.setting.bean.CameraBean;
import com.zee.setting.cache.SPUtils;
import com.zee.setting.cache.SharePrefer;
import com.zee.setting.fragment.FragmentAI;
import com.zee.setting.fragment.FragmentAbout;
import com.zee.setting.fragment.FragmentBluetooth;
import com.zee.setting.fragment.FragmentCommon;
import com.zee.setting.fragment.FragmentNetwork;
import com.zee.setting.fragment.FragmentSystem;
import com.zee.setting.fragment.PreviewFragment;
import com.zee.setting.service.ConnectService;
import com.zee.setting.utils.CommonUtils;
import com.zee.setting.utils.DensityUtils;
import com.zee.setting.utils.RetrofitClient;
import com.zee.setting.utils.SystemProperties;
import com.zee.setting.utils.ToastUtils;
import com.zeewain.ai.IMyAidlInterface;

import net.sunniwell.aar.focuscontrol.layout.FocusControlConstraintLayout;

import java.util.concurrent.ExecutorService;


public class SettingActivity extends BaseActivity implements View.OnFocusChangeListener, View.OnClickListener {
    private static final String TAG = "ZeeSettings";
    private FragmentNetwork fragmentNetwork;
    private FragmentBluetooth fragmentBluetooth;
    private FragmentCommon fragmentCommon;
    private FragmentAI fragmentAI;
    private FragmentSystem fragmentSystem;
    private FragmentAbout fragmentAbout;
    private PreviewFragment previewFragment;
    private Fragment currentFragment;

    private final int REQUEST_CODE_PERMISSIONS = 1;
    private final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_SYNC_SETTINGS,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_SETTINGS,
            Manifest.permission.WRITE_SECURE_SETTINGS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,


    };
    private DrawerLayout drawerLayout;
    private FocusControlConstraintLayout network;
    private FocusControlConstraintLayout bluetooth;
    private FocusControlConstraintLayout common;
    private FocusControlConstraintLayout ai;
    private FocusControlConstraintLayout system;
    private FocusControlConstraintLayout about;

    private ImageView commonIcon;

    private final Handler handler = new Handler();
    private IMyAidlInterface mIService;
    private ExecutorService cachedThreadPool;

    //private boolean installed;
    private long mStartTime;
    private long exitTime;

    private BadgeDrawable badgeCommonIcon;

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()");
        mStartTime = System.currentTimeMillis();
        //处理同时按下确认返回键导致问题
        long mLastStartTime = SPUtils.getInstance().getLong(SharePrefer.StartTime, 0);
        long timeSpace = Math.abs(mStartTime - mLastStartTime);
        SPUtils.getInstance().put(SharePrefer.StartTime, this.mStartTime);
        if (timeSpace < 1000) {
            finish();
        }

        DensityUtils.autoWidth(getApplication(), this);
        setContentView(R.layout.activity_setting);
        initView();
    }

    private void initView() {
        drawerLayout = findViewById(R.id.drawer_layout);
        network = findViewById(R.id.network);
        bluetooth = findViewById(R.id.bluetooth);
        common = findViewById(R.id.common);
        ai = findViewById(R.id.ai);
        system = findViewById(R.id.system);
        about = findViewById(R.id.about);
        commonIcon = findViewById(R.id.img_common);

        drawerLayout.setScrimColor(Color.parseColor("#4D000000"));
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                drawerLayout.openDrawer(GravityCompat.START);// 显示抽屉布局
                setDefaultSelectedItem();
            }
        }, 200);


        network.setOnFocusChangeListener(this);
        bluetooth.setOnFocusChangeListener(this);
        common.setOnFocusChangeListener(this);
        ai.setOnFocusChangeListener(this);
        system.setOnFocusChangeListener(this);
        about.setOnFocusChangeListener(this);
        network.setOnClickListener(this);
        bluetooth.setOnClickListener(this);
        common.setOnClickListener(this);
        ai.setOnClickListener(this);
        system.setOnClickListener(this);
        about.setOnClickListener(this);

        initBadge();
        initListener();

        WindowManager windowManager = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        int rotation = windowManager.getDefaultDisplay().getRotation();

        if(Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation){
            FrameLayout flContainerPreviewLayout = findViewById(R.id.fl_container_preview);
            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) flContainerPreviewLayout.getLayoutParams();
            layoutParams.width = 270;
            layoutParams.height = 480;
            flContainerPreviewLayout.setLayoutParams(layoutParams);
        }

        addPreviewFragment();

        requestPermission();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.i(TAG, "onNewIntent()");
        if(intent.getBooleanExtra(BaseConstants.EXTRA_CAMERA_EVENT, false)) {
            Log.i(TAG, "onNewIntent() via CameraEvent");
            if (currentFragment instanceof FragmentCommon) {
                fragmentCommon.openCameraSelectList();
            } else {
                common.requestFocus();
                setSelect(2);
                common.postDelayed(() -> {
                    if (fragmentCommon != null) {
                        fragmentCommon.openCameraSelectList();
                    }
                }, 500);
            }
        }
    }

    private void addPreviewFragment() {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        previewFragment = PreviewFragment.newInstance();
        transaction.add(R.id.fl_container_preview, previewFragment);
        transaction.commit();
    }

    public synchronized void onCameraSelectChanged(CameraBean cameraBean) {
        if (previewFragment != null) {
            if (previewFragment.cameraBean != null && previewFragment.isCameraOpened && cameraBean.cameraId.equals(previewFragment.cameraBean.cameraId)) {
                return;
            }
            previewFragment.closeCamera();
            previewFragment.openCamera(cameraBean);
        }
    }

    public void callCameraClose() {
        if (previewFragment != null) {
            previewFragment.closeCamera();
        }
    }

    private void initListener() {
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                SettingActivity.this.finish();
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
    }

    private void initBadge() {
        if (!SystemProperties.getBoolean(BaseConstants.SP_KEY_CAMERA_FUNCTION_SWITCH, false)) {
            return;
        }
        boolean hasCommonUpdate = SystemProperties.getBoolean(BaseConstants.SP_KEY_CAMERA_UPDATE_STATE, true);
        commonIcon.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressLint("UnsafeOptInUsageError")
            @Override
            public void onGlobalLayout() {
                badgeCommonIcon = BadgeDrawable.create(SettingActivity.this);
                badgeCommonIcon.setBadgeGravity(BadgeDrawable.TOP_END);
                badgeCommonIcon.setBackgroundColor(0xFFFF0715);
                badgeCommonIcon.setVisible(hasCommonUpdate);
                BadgeUtils.attachBadgeDrawable(badgeCommonIcon, commonIcon);
                commonIcon.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

    }

    private synchronized void setSelect(int i) {
        if (isFinishing() || isDestroyed()) return;
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        //hideFragment(transaction);//我们先把所有的Fragment隐藏了，然后下面再开始处理具体要显示的Fragment
        if (currentFragment != null) {
            transaction.hide(currentFragment);
        }
        Log.i(TAG, "setSelect() ===> " + i);
        switch (i) {
            case 0:
                if (fragmentNetwork == null) {
                    fragmentNetwork = new FragmentNetwork();
                    if (currentFragment != null) {
                        transaction.add(R.id.container, fragmentNetwork);
                    } else {
                        transaction.replace(R.id.container, fragmentNetwork);
                    }
                } else {
                    transaction.show(fragmentNetwork);
                }
                currentFragment = fragmentNetwork;
                break;
            case 1:
                if (fragmentBluetooth == null) {
                    fragmentBluetooth = new FragmentBluetooth();
                    if (currentFragment != null) {
                        transaction.add(R.id.container, fragmentBluetooth);
                    } else {
                        transaction.replace(R.id.container, fragmentBluetooth);
                    }
                } else {
                    transaction.show(fragmentBluetooth);
                }
                currentFragment = fragmentBluetooth;
                break;
            case 2:
                if (fragmentCommon == null) {
                    fragmentCommon = new FragmentCommon();
                    fragmentCommon.setBadgeCameraCallback(aBoolean -> badgeCommonIcon.setVisible(aBoolean));
                    if (currentFragment != null) {
                        transaction.add(R.id.container, fragmentCommon);
                    } else {
                        transaction.replace(R.id.container, fragmentCommon);
                    }
                } else {
                    transaction.show(fragmentCommon);
                }
                currentFragment = fragmentCommon;
                break;
            case 3:
                if (fragmentAI == null) {
                    fragmentAI = new FragmentAI();
                    if (currentFragment != null) {
                        transaction.add(R.id.container, fragmentAI);
                    } else {
                        transaction.replace(R.id.container, fragmentAI);
                    }
                } else {
                    transaction.show(fragmentAI);
                }
                currentFragment = fragmentAI;
                break;
            case 4:
                if (fragmentSystem == null) {
                    fragmentSystem = new FragmentSystem();
                    if (currentFragment != null) {
                        transaction.add(R.id.container, fragmentSystem);
                    } else {
                        transaction.replace(R.id.container, fragmentSystem);
                    }
                } else {
                    transaction.show(fragmentSystem);
                }
                currentFragment = fragmentSystem;
                break;
            case 5:
                if (fragmentAbout == null) {
                    fragmentAbout = new FragmentAbout();
                    if (currentFragment != null) {
                        transaction.add(R.id.container, fragmentAbout);
                    } else {
                        transaction.replace(R.id.container, fragmentAbout);
                    }
                } else {
                    transaction.show(fragmentAbout);
                }
                currentFragment = fragmentAbout;
                break;
        }
        if (!SettingActivity.this.isFinishing()) {
            // transaction.commit();//提交事务
            transaction.commitAllowingStateLoss();
        }


    }

    @Override
    public void onFocusChange(View v, boolean b) {
        if (b) {
            if (drawerLayout.isOpen()) {
                changeSelectFragment(v);
            }
        }
    }

    private void changeSelectFragment(View v) {
        int id = v.getId();
        if (id == R.id.network) {
            setSelect(0);
        } else if (id == R.id.bluetooth) {
            setSelect(1);
        } else if (id == R.id.common) {
            setSelect(2);
        } else if (id == R.id.ai) {
            setSelect(3);
        } else if (id == R.id.system) {
            setSelect(4);
        } else if (id == R.id.about) {
            setSelect(5);
        }
    }

    private void requestPermission() {
        // 先判断有没有权限
        if (allPermissionsGranted()) {
            Settings.System.putInt(getContentResolver(), Settings.System.SOUND_EFFECTS_ENABLED, 1);
            onPermissionsGrantedDone();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                onPermissionsGrantedDone();
            } else {
                ToastUtils.showToast(this, "请开启权限！");
            }
        }
    }

    private void onPermissionsGrantedDone() {
        ConnectService.initConnectService(this);
        setCameraEventListener();

        Log.i(TAG, "onPermissionsGrantedDone()");
        if (drawerLayout.isOpen()) {
            setDefaultSelectedItem();
        }
    }

    private void setDefaultSelectedItem() {//默认选中
        Log.i(TAG, "setDefaultSelectedItem()");
        if(getIntent().getBooleanExtra(BaseConstants.EXTRA_CAMERA_EVENT, false)) {
            common.requestFocus();
            setSelect(2);

            common.postDelayed(() -> {
                if (fragmentCommon != null) {
                    fragmentCommon.openCameraSelectList();
                }
            }, 500);
        } else {
            network.requestFocus();
            setSelect(0);
        }
    }

    private void setCameraEventListener() {
        common.postDelayed(() -> {
            if (ConnectService.getInstance() != null) {
                ConnectService.getInstance().setOnCameraEventListener(isUsbCamera -> {
                    Log.i(TAG, "onCameraUpdate() isUsbCamera=" + isUsbCamera);
                    if (fragmentCommon !=null && fragmentCommon.isVisible()) {
                        fragmentCommon.openCameraSelectList();

                        fragmentCommon.updateCameraInfo();
                    }
                });
            } else {
                setCameraEventListener();
            }
        }, 500);
    }


    @Override
    public void onClick(View v) {
        changeSelectFragment(v);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!BuildConfig.DEBUG) {
            String baseUrl = CommonUtils.getBaseUrl(this);
            if (baseUrl != null) {
                RetrofitClient.baseUrl = baseUrl;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (ConnectService.getInstance() != null) {
            ConnectService.getInstance().setOnCameraEventListener(null);
        }
    }

    @Override
    public void onBackPressed() {
        drawerLayout.closeDrawer(GravityCompat.START);
        // super.onBackPressed();


    }

    public void exit() {
        exitTime = System.currentTimeMillis();
        if (Math.abs(exitTime - mStartTime) > 1000) {
            finish();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
