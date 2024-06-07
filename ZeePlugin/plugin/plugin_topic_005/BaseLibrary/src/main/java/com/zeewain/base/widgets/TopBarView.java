package com.zeewain.base.widgets;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.zeewain.base.R;
import com.zeewain.base.utils.CommonUtils;


public class TopBarView extends ConstraintLayout implements View.OnFocusChangeListener{
    public LinearLayout userRootLayout;
    private ImageView imgUser, imgWifi, imgSettings, imgBack;
    private TextView txtUserInfo;
    private TextView txtLogo;
    private FrameLayout flCenterLayout;
    private OnSettingsItemClickListener onSettingsItemClickListener;
    private NetworkChangeReceiver networkChangeReceiver;
    private static boolean isShowTxtLogo = true;
    public TextView tvUserCommonCount;
    public TextView tvHomeTitle;

    public TopBarView(@NonNull Context context) {
        this(context, null);
    }

    public TopBarView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TopBarView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
        initListener();
    }

    private void initView(Context context){
        LayoutInflater.from(context).inflate(R.layout.layout_top_bar, this);
        userRootLayout = findViewById(R.id.ll_user_top_bar);
        imgUser = findViewById(R.id.img_user_top_bar);
        txtUserInfo = findViewById(R.id.txt_user_top_bar);
        txtLogo = findViewById(R.id.txt_top_bar_logo);

        userRootLayout.setNextFocusLeftId(R.id.ll_user_top_bar);

        imgWifi = findViewById(R.id.img_wifi_top_bar);
        imgWifi.setNextFocusRightId(R.id.img_wifi_top_bar);
        imgSettings = findViewById(R.id.img_settings_top_bar);
        imgBack = findViewById(R.id.img_top_bar_back);
        imgBack.setNextFocusLeftId(R.id.img_top_bar_back);
        tvUserCommonCount = findViewById(R.id.tv_user_common_count);
        tvHomeTitle = findViewById(R.id.tv_home_title);

        flCenterLayout = findViewById(R.id.fl_top_bar_center);
        updateUserImg("");
        setShowTxtLogo(isShowTxtLogo);
        updateWifiInfo();

    }

    public void showUserCommonCount(int count){
        tvUserCommonCount .setVisibility(View.VISIBLE);
        if (count!=0){
            tvUserCommonCount.setText(String.format("全部(%d)", count));
        }else {
            tvUserCommonCount.setText("全部");
        }

    }

    public void setDeFaultFocusView(){
        userRootLayout.requestFocus();
    }

    public void setShowHomeTitle(boolean isShow){
        if (isShow){
            tvHomeTitle.setVisibility(View.VISIBLE);
        }else {
            tvHomeTitle.setVisibility(View.GONE);
        }

    }

    public void addCenterView(View view){
        flCenterLayout.removeAllViews();
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        flCenterLayout.addView(view, layoutParams);
    }

    private void initListener(){
        imgBack.setOnFocusChangeListener(this);
        imgWifi.setOnFocusChangeListener(this);
        imgSettings.setOnFocusChangeListener(this);
        userRootLayout.setOnFocusChangeListener(this);

        imgBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getContext() instanceof AppCompatActivity){
                    ((AppCompatActivity)v.getContext()).finish();
                }
            }
        });
        imgBack.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && event.getAction() == KeyEvent.ACTION_DOWN) {
                v.clearAnimation();
                v.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.host_shake));
            }else if(keyCode == KeyEvent.KEYCODE_DPAD_UP && event.getAction() == KeyEvent.ACTION_DOWN){
                v.clearAnimation();
                v.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.host_shake_y));
            }
            return false;
        });

        userRootLayout.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(v.getContext(), Class.forName("com.zwn.user.ui.UserCenterActivity"));
                v.getContext().startActivity(intent);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        });
        userRootLayout.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && event.getAction() == KeyEvent.ACTION_DOWN) {
                v.clearAnimation();
                v.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.host_shake));
            }else if(keyCode == KeyEvent.KEYCODE_DPAD_UP && event.getAction() == KeyEvent.ACTION_DOWN){
                v.clearAnimation();
                v.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.host_shake_y));
            }
            return false;
        });


        imgWifi.setOnClickListener(v -> {
            CommonUtils.startSettingsActivity(v.getContext());
        });
        imgWifi.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && event.getAction() == KeyEvent.ACTION_DOWN) {
                v.clearAnimation();
                v.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.host_shake));
            }else if(keyCode == KeyEvent.KEYCODE_DPAD_UP && event.getAction() == KeyEvent.ACTION_DOWN){
                v.clearAnimation();
                v.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.host_shake_y));
            }
            return false;
        });

        imgSettings.setOnClickListener(v -> {
            if(onSettingsItemClickListener != null){
                onSettingsItemClickListener.onClick(v);
            }else{
                CommonUtils.startSettingsActivity(v.getContext());
            }
        });
        imgSettings.setOnKeyListener((v, keyCode, event) -> {
            if(keyCode == KeyEvent.KEYCODE_DPAD_UP && event.getAction() == KeyEvent.ACTION_DOWN){
                v.clearAnimation();
                v.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.host_shake_y));
            }
            return false;
        });
    }

    public void setBackEnable(boolean enable){
        if(enable){
            userRootLayout.setVisibility(View.GONE);
            imgBack.setVisibility(View.VISIBLE);
        }else{
            userRootLayout.setVisibility(View.VISIBLE);
            imgBack.setVisibility(View.GONE);
        }
    }

    public void updateUserImg(String account){
        if(CommonUtils.isUserLogin()){
//            imgUser.setImageResource(R.mipmap.top_bar_user_login);
            if(account != null && account.length() > 0){
                txtUserInfo.setVisibility(View.VISIBLE);
                txtUserInfo.setText(account);
            }else{
                txtUserInfo.setVisibility(View.GONE);
            }
        }else{
//            imgUser.setImageResource(R.mipmap.top_bar_user_login);
            txtUserInfo.setVisibility(View.VISIBLE);
            txtUserInfo.setText("登录");
        }
    }

    public void setShowTxtLogo(boolean show){
        isShowTxtLogo = show;
        if(isShowTxtLogo){
            txtLogo.setVisibility(View.VISIBLE);
        }else{
            txtLogo.setVisibility(View.GONE);
        }
    }

    public void setOnSettingsClickListener(OnSettingsItemClickListener onSettingsItemClickListener) {
        this.onSettingsItemClickListener = onSettingsItemClickListener;
    }

    public void setImgUser(int resId){
        imgUser.setImageResource(resId);
    }

    public void setImgSettings(int resId){
        imgSettings.setImageResource(resId);
    }

    public void disappearImgUser() {
        imgUser.setVisibility(GONE);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if(hasFocus){
            CommonUtils.scaleView(v, 1.1f);
        }else{
            v.clearAnimation();
            CommonUtils.scaleView(v, 1f);
        }
    }

    public interface OnSettingsItemClickListener{
        void onClick(View view);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        registerBroadCast();
    }

    @Override
    protected void onDetachedFromWindow() {
        unRegisterBroadCast();
        super.onDetachedFromWindow();
    }

    class NetworkChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateWifiInfo();
        }
    }

    private void updateWifiInfo(){
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo != null && networkInfo.isConnected()) {
            imgWifi.setImageResource(R.drawable.selector_top_bar_wifi);
        }else{
            imgWifi.setImageResource(R.drawable.selector_top_bar_wifi_err);
        }
    }

    private void registerBroadCast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        networkChangeReceiver = new NetworkChangeReceiver();
        getContext().registerReceiver(networkChangeReceiver, intentFilter);
    }

    private void unRegisterBroadCast(){
        getContext().unregisterReceiver(networkChangeReceiver);
    }
}
