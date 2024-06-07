package com.zeewain.base.ui;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.zeewain.base.R;
import com.zeewain.base.utils.ToastUtils;
import com.zeewain.base.views.LoadingDialog;

public class BaseActivity extends AppCompatActivity {
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        hideSystemUI();
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        ToastUtils.cancel();
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
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        /*View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);*/

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        getWindow().setAttributes(params);
        getWindow().setBackgroundDrawableResource(R.mipmap.img_overall_bg);
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

    public void showToast(int resId) {
        showToast(getString(resId));
    }

    public void showToast(String msg) {
        ToastUtils.showShort(msg);
    }
}