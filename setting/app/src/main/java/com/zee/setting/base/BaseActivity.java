package com.zee.setting.base;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.zee.setting.utils.ToastUtils;
import com.zee.setting.views.LoadingDialog;


public class BaseActivity extends AppCompatActivity {
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        hideSystemUI();
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        hideLoadingDialog();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus){
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY|View.SYSTEM_UI_FLAG_FULLSCREEN;
//        params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        getWindow().setAttributes(params);

    }

    public void showLoadingDialog() {
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(this);
        }
        loadingDialog.showLoading();
    }

    public void hideLoadingDialog() {
        if (loadingDialog != null) {
            loadingDialog.hideLoading();
        }
    }

    public void showToast(int resId){
        showToast(getString(resId));
    }
    public void showToast(String msg){
       // ToastUtils.showShort(msg);
        ToastUtils.showToast(this,msg);
    }
}