package com.zee.setting.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.zee.setting.R;
import com.zee.setting.base.BaseActivity;
import com.zee.setting.cache.SPUtils;
import com.zee.setting.cache.SharePrefer;
import com.zee.setting.utils.DensityUtils;
import com.zee.setting.utils.RemoteManage;
import com.zee.setting.views.LoadingView;

import java.util.Timer;
import java.util.TimerTask;

public class GestureExplainActivity extends BaseActivity {

    private int num = 0;
    private static final int FULL_SCREEN_FLAG =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
    private LoadingView loadingView;
    private TextView title;
    private int duration = 2000;
    private Handler handler = new Handler();
    private FrameLayout promptLayout;
    private ImageView imgPrompt;
    private GestureActionReceiver gestureActionReceiver;
    private TextView tvPrompt;
    private boolean isOpenGesture;
    private boolean isRunning;
    private boolean isActive = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DensityUtils.autoWidth(getApplication(), this);
        setContentView(R.layout.activity_gesture_explain);
        initData();
        initView();
        registerReceiver();
        // test();


    }

    private void initData() {
        Log.i("ttthhh", "initData");
        String gestureSwitch = SPUtils.getInstance().getString(SharePrefer.Gesture);
        if (gestureSwitch.equals("open")) {
            isOpenGesture = true;
//            RemoteManage.getInstance().sendMessage("hide");
            // ToastUtils.showToast(this, "手势开关是打开的");
        } else if (gestureSwitch.equals("close")) {
            isOpenGesture = false;
            // ToastUtils.showToast(this, "手势开关是关闭的");
        }
        //进入此界面前已经是激活状态，进入此界面后屏蔽按键控制
        boolean isActivated = RemoteManage.getInstance().getIsActivated();
        isActive = isActivated;
        if (isActive) {
            RemoteManage.getInstance().sendMessage("close_key_control");
        }
    }

    private void initView() {
        Log.i("ttthhh", "initView");
        loadingView = findViewById(R.id.load);
        title = findViewById(R.id.title);
        promptLayout = findViewById(R.id.prompt_layout);
        imgPrompt = findViewById(R.id.img_prompt);
        tvPrompt = findViewById(R.id.tv_prompt);
        View btnBack = findViewById(R.id.gesture_back);
        btnBack.setOnClickListener(v -> finish());
        //startShowPrompt("raise");
    }

    private void startShowPrompt(String gesture) {
        if (isRunning) {
            return;
        }

        if (gesture.equals("raise")) {
            imgPrompt.setImageResource(R.mipmap.ic_raise_hand_grey);
            tvPrompt.setText("举手激活");
        } else if (gesture.equals("ok")) {
            imgPrompt.setImageResource(R.mipmap.ic_ok_grey);
            tvPrompt.setText("OK确认键");
        } else if (gesture.equals("back")) {
            imgPrompt.setImageResource(R.mipmap.ic_fist_grey);
            tvPrompt.setText("握拳返回键");
        } else if (gesture.equals("up")) {
            imgPrompt.setImageResource(R.mipmap.ic_up_grey);
            tvPrompt.setText("上挥上键");
        } else if (gesture.equals("down")) {
            imgPrompt.setImageResource(R.mipmap.ic_down_grey);
            tvPrompt.setText("下挥下键");
        } else if (gesture.equals("left")) {
            imgPrompt.setImageResource(R.mipmap.ic_left_grey);
            tvPrompt.setText("左挥左键");
        } else if (gesture.equals("right")) {
            imgPrompt.setImageResource(R.mipmap.ic_right_grey);
            tvPrompt.setText("右挥右键");
        }
        promptLayout.setVisibility(View.VISIBLE);
        if (gesture.equals("raise")) {
            cancelLoadview();
            if (!isRunning) {
                isRunning = true;
                loadingView.setVisibility(View.VISIBLE);
                loadingView.startAnimation(0, 100, duration);
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        postDelayHide();
                    }
                }, duration);
            }

        } else {
            if (!isRunning) {
                isRunning = true;
                postDelayHide();
            }

        }


    }

    private void postDelayHide() {
        loadingView.setVisibility(View.GONE);
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                promptLayout.setVisibility(View.GONE);
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isRunning = false;
                    }
                }, 1000);


            }
        }, duration);
    }

    private void cancelLoadview() {
        loadingView.clearAnimation();
        loadingView.setProgress(0);

    }


    private void test() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (num == 0) {
                            RemoteManage.getInstance().sendMessage("start");
                        } else if (num == 1) {
                            RemoteManage.getInstance().sendMessage("hide");
                        } else if (num == 2) {
                            RemoteManage.getInstance().sendMessage("show");
                        } else if (num == 3) {
                            RemoteManage.getInstance().sendMessage("close");
                        }
                        num = num + 1;
                        if (num == 4) {
                            num = 0;
                        }
                    }
                });

            }
        }, 10000, 10000);
    }

    @Override
    protected void onDestroy() {
        Log.i("ttthhh", "onDestroy");
        handler.removeCallbacksAndMessages(null);
        unregisterReceiver(gestureActionReceiver);
        if (isActive) {
            RemoteManage.getInstance().sendMessage("open_key_control");
        }
        super.onDestroy();

    }

    private void registerReceiver() {
        Log.i("ttthhh", "registerReceiver");
        gestureActionReceiver = new GestureActionReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("gesture_action");
        registerReceiver(gestureActionReceiver, filter);
    }

    public class GestureActionReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String intentAction = intent.getAction();
            if (intentAction.equals("gesture_action")) {
                String gesture = intent.getStringExtra("gesture");
                Log.i("ssshhh", "gesture_activity=" + gesture);
                if (gesture.equals("active") && (isActive == false)) {
                    startShowPrompt("raise");
                    isActive = true;
                } else if (gesture.equals("inactive")) {
                    isActive = false;
                }

                if (isActive) {
                    if (gesture.equals("SlipLeft")) {
                        startShowPrompt("left");
                    } else if (gesture.equals("SlipRight")) {
                        startShowPrompt("right");
                    } else if (gesture.equals("SlipUp")) {
                        startShowPrompt("up");
                    } else if (gesture.equals("SlipDown")) {
                        startShowPrompt("down");
                    } else if (gesture.equals("ok")) {
                        startShowPrompt("ok");
                    } else if (gesture.equals("fist")) {
                        startShowPrompt("back");
                    }
                }


            }
        }
    }


}
