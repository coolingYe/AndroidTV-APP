package com.zee.device.base.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.card.MaterialCardView;
import com.zee.device.base.R;
import com.zee.device.base.utils.CommonUtils;
import com.zee.device.base.utils.DisplayUtil;


public class UpgradeTipDialog extends Dialog implements View.OnFocusChangeListener{
    private String titleText, messageText;
    private TextView titleView;
    private TextView messageView;
    private MaterialCardView positiveView;
    private MaterialCardView cancelView;
    private MaterialCardView confirmView;
    private ConstraintLayout positiveCancelLayout;
    private boolean showConfirmButton = false;

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
        positiveView = findViewById(R.id.card_positive_dialog);
        cancelView = findViewById(R.id.card_cancel_dialog);
        confirmView = findViewById(R.id.card_confirm_dialog);

        positiveCancelLayout = findViewById(R.id.layout_positive_cancel_dialog);

        positiveView.setOnFocusChangeListener(this);
        cancelView.setOnFocusChangeListener(this);
        confirmView.setOnFocusChangeListener(this);

        if(titleText != null){
            titleView.setText(titleText);
        }

        if(messageText != null){
            messageView.setText(messageText);
        }

        if(showConfirmButton) {
            positiveCancelLayout.setVisibility(View.GONE);
            confirmView.setVisibility(View.VISIBLE);
        }else{
            positiveCancelLayout.setVisibility(View.VISIBLE);
            confirmView.setVisibility(View.GONE);
        }

        positiveView.setOnClickListener(v -> {
            if(onClickListener != null){
                onClickListener.onPositive(v);
            }
        });

        cancelView.setOnClickListener(v -> {
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

    public void showConfirmButton(boolean showConfirmButton){
        this.showConfirmButton = showConfirmButton;
        if(showConfirmButton) {
            if(positiveCancelLayout != null && confirmView != null) {
                positiveCancelLayout.setVisibility(View.GONE);
                confirmView.setVisibility(View.VISIBLE);
            }
        }else{
            if(positiveCancelLayout != null && confirmView != null) {
                positiveCancelLayout.setVisibility(View.VISIBLE);
                confirmView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        MaterialCardView cardView = (view instanceof MaterialCardView) ? (MaterialCardView) view : null;
        if (cardView == null) return;
        final int strokeWidth = DisplayUtil.dip2px(view.getContext(), 1);
        if (hasFocus) {
            cardView.setStrokeColor(view.getContext().getResources().getColor(R.color.selectedStrokeColorPurple));
            cardView.setStrokeWidth(strokeWidth);
            CommonUtils.scaleView(view, 1.1f);
        } else {
            cardView.setStrokeColor(view.getContext().getResources().getColor(R.color.unselectedStrokeColor));
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
