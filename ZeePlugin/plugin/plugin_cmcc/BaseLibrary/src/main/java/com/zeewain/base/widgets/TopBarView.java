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
import android.widget.TextClock;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.zeewain.base.R;
import com.zeewain.base.utils.CommonUtils;

import java.util.List;


public class TopBarView extends ConstraintLayout implements View.OnFocusChangeListener {
    private LinearLayout userRootLayout;
    private ImageView imgUser, imgWifi, imgSettings, imgBack;
    private TextView txtUserInfo;
    private TextView txtLogo;
    private FrameLayout flCenterLayout;
    private OnSettingsItemClickListener onSettingsItemClickListener;
    private NetworkChangeReceiver networkChangeReceiver;
    private static boolean isShowTxtLogo = true;
    private int userImageId, userSelectedImageId, userImageBackgroundId;
    private int wifiSelectedImageId, notNetWordWifiImageId, normalWifiImageId;

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

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.layout_top_bar, this);
        userRootLayout = findViewById(R.id.ll_user_top_bar);
        imgUser = findViewById(R.id.img_user_top_bar);
        txtUserInfo = findViewById(R.id.txt_user_top_bar);
        txtLogo = findViewById(R.id.txt_top_bar_logo);

        userRootLayout.setNextFocusLeftId(userRootLayout.getId());

        imgWifi = findViewById(R.id.img_wifi_top_bar);
        imgWifi.setNextFocusRightId(imgWifi.getId());
        imgSettings = findViewById(R.id.img_settings_top_bar);
        imgBack = findViewById(R.id.img_top_bar_back);
        imgBack.setNextFocusLeftId(imgBack.getId());

        flCenterLayout = findViewById(R.id.fl_top_bar_center);
        updateUserImg("");
        setShowTxtLogo(isShowTxtLogo);
    }

    public void setTabResourceIds(int[] userResIds, int[] settingResIds, int[] wifiResIds, int[] otherResIds) {
        if (userResIds.length == 2) {
            userImageId = userResIds[0];
            userImageBackgroundId = userResIds[1];
            imgUser.setImageResource(userImageId);
            userRootLayout.setBackgroundResource(userImageBackgroundId);
        }

        if (settingResIds.length == 2) {
            imgSettings.setImageResource(settingResIds[0]);
            imgSettings.setBackgroundResource(settingResIds[1]);
        }

        if (wifiResIds.length == 3) {
            notNetWordWifiImageId = wifiResIds[0];
            normalWifiImageId = wifiResIds[1];
            wifiSelectedImageId = wifiResIds[2];
            imgWifi.setBackgroundResource(wifiSelectedImageId);
        }

        if (otherResIds.length == 2) {
            imgBack.setBackgroundResource(otherResIds[0]);
            TextClock textClock = findViewById(R.id.tp_time);
            textClock.setTextColor(otherResIds[1]);
            txtLogo.setTextColor(otherResIds[1]);
        }
    }

    public void addCenterView(View view) {
        flCenterLayout.removeAllViews();
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        flCenterLayout.addView(view, layoutParams);
    }

    private void initListener() {
        imgBack.setOnFocusChangeListener(this);
        imgWifi.setOnFocusChangeListener(this);
        imgSettings.setOnFocusChangeListener(this);
        userRootLayout.setOnFocusChangeListener(this);

        imgBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getContext() instanceof AppCompatActivity) {
                    ((AppCompatActivity) v.getContext()).finish();
                }
            }
        });
        imgBack.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && event.getAction() == KeyEvent.ACTION_DOWN) {
                v.clearAnimation();
                v.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.host_shake));
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP && event.getAction() == KeyEvent.ACTION_DOWN) {
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
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP && event.getAction() == KeyEvent.ACTION_DOWN) {
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
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP && event.getAction() == KeyEvent.ACTION_DOWN) {
                v.clearAnimation();
                v.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.host_shake_y));
            }
            return false;
        });

        imgSettings.setOnClickListener(v -> {
            if (onSettingsItemClickListener != null) {
                onSettingsItemClickListener.onClick(v);
            } else {
                CommonUtils.startSettingsActivity(v.getContext());
            }
        });
        imgSettings.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DPAD_UP && event.getAction() == KeyEvent.ACTION_DOWN) {
                v.clearAnimation();
                v.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.host_shake_y));
            }
            return false;
        });
    }

    public void setBackBtnNextFocusDownId (int id) {
        imgBack.setNextFocusDownId(id);
    }

    public void setSettingsBtnNextFocusDownId (int id) {
        imgSettings.setNextFocusDownId(id);
    }

    public void setBackEnable(boolean enable) {
        if (enable) {
            userRootLayout.setVisibility(View.GONE);
            imgBack.setVisibility(View.VISIBLE);
        } else {
            userRootLayout.setVisibility(View.VISIBLE);
            imgBack.setVisibility(View.GONE);
        }
    }

    public void updateUserImg(String account) {
        if (CommonUtils.isUserLogin()) {
            if (account != null && account.length() > 0) {
                txtUserInfo.setVisibility(View.VISIBLE);
                txtUserInfo.setText(account);
            } else {
                txtUserInfo.setVisibility(View.GONE);
            }
        } else {
            txtUserInfo.setVisibility(View.VISIBLE);
            txtUserInfo.setText("登录");
        }
    }

    public void setShowTxtLogo(boolean show) {
        isShowTxtLogo = show;
        if (isShowTxtLogo) {
            txtLogo.setVisibility(View.VISIBLE);
        } else {
            txtLogo.setVisibility(View.GONE);
        }
    }

    public void setOnSettingsClickListener(OnSettingsItemClickListener onSettingsItemClickListener) {
        this.onSettingsItemClickListener = onSettingsItemClickListener;
    }

    public void setImgUser(int resId) {
        imgUser.setImageResource(resId);
    }

    public void setImgSettings(int resId) {
        imgSettings.setImageResource(resId);
    }

    public void disappearImgUser() {
        imgUser.setVisibility(GONE);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            CommonUtils.scaleView(v, 1.1f);
        } else {
            v.clearAnimation();
            CommonUtils.scaleView(v, 1f);
        }
    }

    public interface OnSettingsItemClickListener {
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

    public void updateWifiInfo() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo != null && networkInfo.isConnected()) {
            if (normalWifiImageId == 0) return;
            imgWifi.setImageResource(normalWifiImageId);
        } else {
            if (notNetWordWifiImageId == 0) return;
            imgWifi.setImageResource(notNetWordWifiImageId);
        }
    }

    private void registerBroadCast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        networkChangeReceiver = new NetworkChangeReceiver();
        getContext().registerReceiver(networkChangeReceiver, intentFilter);
    }

    private void unRegisterBroadCast() {
        getContext().unregisterReceiver(networkChangeReceiver);
    }
}
