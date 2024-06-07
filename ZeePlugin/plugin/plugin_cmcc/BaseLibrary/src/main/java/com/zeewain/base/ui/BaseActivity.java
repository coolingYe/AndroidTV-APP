package com.zeewain.base.ui;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.zeewain.base.R;
import com.zeewain.base.utils.ToastUtils;
import com.zeewain.base.views.LoadingDialog;
import com.zeewain.base.widgets.TopBarView;

import java.util.Objects;

public class BaseActivity extends AppCompatActivity {
    private LoadingDialog loadingDialog;
    public TopBarView topBarView;

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
        ToastUtils.cancel();
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
        getWindow().setBackgroundDrawableResource(R.mipmap.img_main_home_bg);
        if (topBarView != null) {
            topBarView.setTabResourceIds(
                    new int[]{R.mipmap.top_bar_user_login, R.drawable.selector_btn},
                    new int[]{R.mipmap.top_bar_settings, R.drawable.selector_btn_frame},
                    new int[]{R.mipmap.top_bar_wifi_err, R.mipmap.top_bar_wifi, R.drawable.selector_btn_frame},
                    new int[]{R.drawable.selector_top_bar_back_cmcc1, 0xFFFFFFFF});
            topBarView.updateWifiInfo();
        }

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
        ToastUtils.setView(R.layout.layout_toast_view);
        TextView textView = Objects.requireNonNull(ToastUtils.getView()).findViewById(R.id.tv_toast_msg);
        textView.setText(msg);
        ToastUtils.showShort(msg);
    }
}