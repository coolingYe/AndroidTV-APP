package com.zee.launcher.home.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.card.MaterialCardView;
import com.zee.launcher.home.R;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.DisplayUtil;


public class UpgradeTipDialog extends AlertDialog implements View.OnFocusChangeListener {
    private String titleText, messageText, positiveText, cancelText, confirmText;
    private TextView titleView;
    private TextView messageView;
    private TextView positiveView;
    private TextView cancelView;
    private MaterialCardView cardPositiveView;
    private MaterialCardView cardCancelView;
    private TextView confirmView;
    private ConstraintLayout positiveCancelLayout;

    private OnClickListener onClickListener;

    public UpgradeTipDialog(@NonNull Context context) {
        super(context, R.style.UpgradeDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_upgrade_tip);

        setCanceledOnTouchOutside(false);

        titleView = findViewById(R.id.txt_title_dialog);
        messageView = findViewById(R.id.txt_message_dialog);
        positiveView = findViewById(R.id.txt_positive_dialog);
        cancelView = findViewById(R.id.txt_cancel_dialog);
        cardPositiveView = findViewById(R.id.card_positive_dialog);
        cardCancelView = findViewById(R.id.card_cancel_dialog);
        confirmView = findViewById(R.id.txt_confirm_dialog);

        positiveCancelLayout = findViewById(R.id.layout_positive_cancel_dialog);

        if(titleText != null){
            titleView.setText(titleText);
        }

        if(messageText != null){
            messageView.setText(messageText);
        }

        if(positiveText != null){
            positiveView.setText(positiveText);
        }

        if(cancelText != null){
            cancelView.setText(cancelText);
        }

        if(confirmText != null){
            confirmView.setText(confirmText);
            positiveCancelLayout.setVisibility(View.GONE);
            confirmView.setVisibility(View.VISIBLE);
        }else{
            confirmView.setVisibility(View.GONE);
            positiveCancelLayout.setVisibility(View.VISIBLE);
        }

        cardCancelView.setOnFocusChangeListener(this);
        cardPositiveView.setOnFocusChangeListener(this);
        cardPositiveView.setOnClickListener(v -> {
            if(onClickListener != null){
                onClickListener.onPositive(v);
            }
        });

        cardCancelView.setOnClickListener(v -> {
            if(onClickListener != null){
                onClickListener.onCancel(v);
            }
        });

        confirmView.setOnClickListener(v -> {
            if(onClickListener != null){
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

    public void setTitleText(String titleText) {
        this.titleText = titleText;
        if(titleView != null){
            titleView.setText(titleText);
        }
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
        if(messageView != null){
            messageView.setText(messageText);
        }
    }

    public void setPositiveText(String positiveText) {
        this.positiveText = positiveText;
        if(positiveView != null){
            positiveView.setText(positiveText);
        }
    }

    public void setCancelText(String cancelText) {
        this.cancelText = cancelText;
        if(cancelView != null){
            cancelView.setText(cancelText);
        }
    }

    public void setConfirmText(String confirmText) {
        this.confirmText = confirmText;
        if(confirmView != null){
            confirmView.setText(confirmText);
        }
    }

    @Override
    public void onFocusChange(View view, boolean b) {
        MaterialCardView cardView = (view instanceof MaterialCardView) ? (MaterialCardView) view : null;
        if (cardView == null) return;
        final int strokeWidth = DisplayUtil.dip2px(view.getContext(), 1);
        if (b) {
            cardView.setStrokeColor(0xFF6C64F4);
            cardView.setStrokeWidth(strokeWidth);
            CommonUtils.scaleView(view, 1.1f);
        } else {
            cardView.setStrokeColor(0x00FFFFFF);
            cardView.setStrokeWidth(0);
            view.clearAnimation();
            CommonUtils.scaleView(view, 1f);
        }
    }

    public interface OnClickListener {
        void onConfirm(View v);
        void onPositive(View v);
        void onCancel(View v);
    }

    public UpgradeTipDialog setOnClickListener(OnClickListener onClickListener) {
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
    }
}
