package com.zee.guide.ui;


import android.Manifest;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.KeyEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.FragmentNavigator;
import androidx.navigation.fragment.NavHostFragment;

import com.zee.guide.R;
import com.zee.guide.data.GuideRepository;
import com.zeewain.base.BaseApplication;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.config.SharePrefer;
import com.zeewain.base.data.protocol.response.UpgradeResp;
import com.zeewain.base.model.LoadState;
import com.zeewain.base.ui.BaseActivity;
import com.zeewain.base.utils.ApkUtil;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.DensityUtils;
import com.zee.guide.ui.start.StartFragment;
import com.zeewain.base.utils.FileUtils;
import com.zeewain.base.utils.SPUtils;

import java.io.IOException;

public class GuideActivity extends BaseActivity {

    private final int REQUEST_CODE_PERMISSIONS = 1;
    private final int REQUEST_CODE_SDCARD_PERMISSION = 201;
    private final String[] REQUIRED_PERMISSIONS = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private static final int REQ_CODE_UPGRADE_RESULT = 1300;
    private GuideViewModel viewModel;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GuideViewModelFactory factory = new GuideViewModelFactory(GuideRepository.getInstance());
        viewModel = new ViewModelProvider(this, factory).get(GuideViewModel.class);

        DensityUtils.autoWidth(getApplication(), this);
        setContentView(R.layout.activity_guide);

        initViewObservable();

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        assert fragment != null;
        navController = NavHostFragment.findNavController(fragment);

        setLauncherWallpaper();

        requestPermission();

        boolean clearAllCache = getIntent().getBooleanExtra(BaseConstants.EXTRA_CLEAR_ALL_CACHE, false);
        if(clearAllCache){
            FileUtils.deleteFolder(BaseConstants.PRIVATE_DATA_PATH);
            if(ApkUtil.getAppVersionCode(this, BaseConstants.MANAGER_PACKAGE_NAME) >= 32){
                CommonUtils.runCmdByManagerActivity(this, "正在清除数据！", "pm clear " + this.getPackageName());
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

    private void startUpgradeActivity(UpgradeResp upgradeResp){
        try {
            Intent intent = new Intent(this, Class.forName(BaseConstants.UPGRADE_PKG_CLASS_NAME));
            Bundle bundle = new Bundle();
            bundle.putSerializable(BaseConstants.EXTRA_UPGRADE_INFO, upgradeResp);
            intent.putExtra(BaseConstants.EXTRA_UPGRADE_INFO, bundle);
            startActivityForResult(intent, REQ_CODE_UPGRADE_RESULT);
        } catch (ClassNotFoundException ignored) {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(REQ_CODE_UPGRADE_RESULT == requestCode){
            if(resultCode == RESULT_CANCELED && viewModel.hostAppUpgradeResp != null && !viewModel.hostAppUpgradeResp.isForcible()){
                viewModel.isUserDiscardUpgrade = true;
            }
        }else if(requestCode == REQUEST_CODE_SDCARD_PERMISSION){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                if (Environment.isExternalStorageManager()) {
                    checkPermission();
                }else{
                    showToast("请开启Sdcard使用权限！");
                    requestPermission();
                }
            }
        }
    }

    private void initViewObservable() {
        viewModel.mldToastMsg.observe(this, this::showToast);

        viewModel.mldManagerAppUpgradeState.observe(this, loadState -> {
            if(LoadState.Success == loadState){
                if(viewModel.managerAppUpgradeResp != null){
                    BaseApplication.handleManagerUpgrade(viewModel.managerAppUpgradeResp);
                }
                viewModel.reqHostAppUpgrade(ApkUtil.getAppVersionName(this));
            }else if(LoadState.Loading == loadState){
                showLoadingDialog();
            }else{
                hideLoadingDialog();
            }
        });

        viewModel.mldHostAppUpgradeState.observe(this, loadState -> {
            if (LoadState.Success == loadState) {
                hideLoadingDialog();
                UpgradeResp upgradeResp = viewModel.hostAppUpgradeResp;
                if (upgradeResp != null) {
                    startUpgradeActivity(upgradeResp);
                }else{
                    viewModel.reqDeviceInfo(CommonUtils.getDeviceSn());
                }
            }else if(LoadState.Failed == loadState){
                hideLoadingDialog();
            }
        });

        viewModel.mldDeviceInfoLoadState.observe(this, loadState -> {
            if(LoadState.Success == loadState){
                viewModel.reqServicePackInfo();
            }else if(LoadState.Failed == loadState){
                hideLoadingDialog();
            }else{
                showLoadingDialog();
            }
        });

        viewModel.mldServicePackInfoLoadState.observe(this, loadState -> {
            if(LoadState.Success == loadState){
                hideLoadingDialog();
                SPUtils.getInstance().put(SharePrefer.ShowAIGestureItem, viewModel.isAiGestureEnable());
                if(viewModel.useTopicLogin()){
                    toMainActivity();
                }else {
                    toLoginActivity();
                }
            }else{
                hideLoadingDialog();
            }
        });
    }

    private void toMainActivity(){
        SPUtils.getInstance().put(SharePrefer.TopicLogin, true);
        if(CommonUtils.startMainActivity(this)){
            finish();
        }
    }

    private void toLoginActivity(){
        try {
            Intent intent = new Intent(this, Class.forName(BaseConstants.LOGIN_PKG_CLASS_NAME));
            if(viewModel.deviceInfoResp == null || viewModel.deviceInfoResp.activateStatus == BaseConstants.DeviceStatus.UNACTIVATED) {
                intent.putExtra(BaseConstants.EXTRA_REGISTER, true);
            }
            startActivity(intent);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
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
        }else{
            checkPermission();
        }
    }

    private void checkPermission(){
        if (allPermissionsGranted()) {
            onPermissionsGrantedDone();
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                onPermissionsGrantedDone();
            } else {
                showToast("请开启权限！");
            }
        }
    }

    private void onPermissionsGrantedDone(){

    }

    @Override
    public void onBackPressed() {
        NavDestination currentDestination = navController.getCurrentDestination();
        if(currentDestination instanceof FragmentNavigator.Destination){
            FragmentNavigator.Destination destination = (FragmentNavigator.Destination) currentDestination;
            if(destination.getClassName().equals(StartFragment.class.getName())){
                return;
            }
        }
        super.onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
            NavDestination currentDestination = navController.getCurrentDestination();
            if(currentDestination instanceof FragmentNavigator.Destination){
                FragmentNavigator.Destination destination = (FragmentNavigator.Destination) currentDestination;
                if(destination.getClassName().equals(StartFragment.class.getName())){
                    return true;
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}