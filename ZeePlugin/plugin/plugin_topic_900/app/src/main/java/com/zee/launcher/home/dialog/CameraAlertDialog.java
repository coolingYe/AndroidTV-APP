package com.zee.launcher.home.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.zee.launcher.home.R;


public class CameraAlertDialog extends AlertDialog {
    private String messageText;
    private TextView messageView;
    private ConstraintLayout confirmView;
    private OnClickListener onClickListener;

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
        setMessageText("请确保网络正常后，重启设备！");
        confirmView.setOnClickListener(v -> {
            if (onClickListener != null) {
                dismiss();
                onClickListener.onConfirm(v);
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


    public interface OnClickListener {
        void onConfirm(View v);
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
