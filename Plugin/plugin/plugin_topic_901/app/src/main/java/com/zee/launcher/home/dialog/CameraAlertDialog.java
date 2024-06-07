package com.zee.launcher.home.dialog;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.zee.launcher.home.R;
import com.zee.launcher.home.gesture.config.Config;


public class CameraAlertDialog extends AlertDialog {
    private String messageText;
    private TextView messageView;
    private ConstraintLayout confirmView;
    private OnClickListener onClickListener;
    private int cameraErrType = Config.CameraError_NORMAL;

    public CameraAlertDialog(@NonNull Context context) {
        super(context, R.style.CustomAlertDialog);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_camera_alert);
        setCanceledOnTouchOutside(false);
        messageView = findViewById(R.id.txt_show_tip);
        confirmView = findViewById(R.id.scl_confirm);
        if (messageText != null) {
            messageView.setText(messageText);
        }
        confirmView.setOnClickListener(v -> {
            if (onClickListener != null) {
                dismiss();
                onClickListener.onConfirm(v, cameraErrType);
            }
        });
        setOnKeyListener((dialog, keyCode, event) -> {
            if ((keyCode == KeyEvent.KEYCODE_BACK)) {
                return true;
            }
            return false;
        });
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
        if (messageView != null) {
            messageView.setText(messageText);
        }
    }

    public void setErrorType(int errorType) {
        if (errorType == Config.CameraError_EMPTY) {
            setMessageText("未找到可使用的摄像头！");
        } else if (errorType == Config.CameraError_INVALID) {
            setMessageText("摄像头异常，请重新插拔USB摄像头或者重启设备！");
        } else if (errorType == Config.CameraError_ERROR) {
            setMessageText("摄像头异常，请重新插拔USB摄像头或者重启设备！");
        } else if (errorType == Config.CameraError_UnKNOW) {
            setMessageText("未知错误！");
        }
        cameraErrType = errorType;
    }


    public interface OnClickListener {
        void onConfirm(View v, int errType);
    }

    public CameraAlertDialog setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
        return this;
    }

    private void fullScreenImmersive(View view) {
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        view.setSystemUiVisibility(uiOptions);
    }

    @Override
    public void show() {
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        super.show();
        fullScreenImmersive(getWindow().getDecorView());
        this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        confirmView.requestFocus();
    }
}
