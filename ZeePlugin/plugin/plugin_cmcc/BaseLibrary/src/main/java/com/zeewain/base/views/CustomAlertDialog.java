package com.zeewain.base.views;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.card.MaterialCardView;
import com.zeewain.base.R;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.DisplayUtil;
import com.zeewain.base.utils.FontUtils;


public class CustomAlertDialog extends AlertDialog implements View.OnFocusChangeListener {
    private String messageText, messageSummaryText, positiveText, cancelText, confirmText;
    private TextView messageView;
    private TextView messageSummaryView;
    private TextView positiveView;
    private TextView cancelView;
    private TextView confirmView;
    private MaterialCardView cardViewCancel;
    private MaterialCardView cardViewPositive;

    private OnClickListener onClickListener;

    public CustomAlertDialog(@NonNull Context context) {
        super(context, R.style.CustomAlertDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_custom_alert);

        setCanceledOnTouchOutside(false);

        messageView = findViewById(R.id.txt_message_dialog);
        messageSummaryView = findViewById(R.id.txt_message_summary_dialog);
        positiveView = findViewById(R.id.txt_positive_dialog);
        cancelView = findViewById(R.id.txt_cancel_dialog);
        confirmView = findViewById(R.id.txt_confirm_dialog);

        cardViewCancel = findViewById(R.id.card_cancel_dialog);
        cardViewPositive =  findViewById(R.id.card_positive_dialog);
        cardViewPositive.setOnFocusChangeListener(this);
        cardViewCancel.setOnFocusChangeListener(this);

        ConstraintLayout positiveCancelLayout = findViewById(R.id.layout_positive_cancel_dialog);

        if(messageText != null){
            messageView.setText(messageText);
        }

        if(messageSummaryText != null){
            messageSummaryView.setText(messageSummaryText);
            messageSummaryView.setVisibility(View.VISIBLE);
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

        cardViewPositive.setOnClickListener(v -> {
            if(onClickListener != null){
                onClickListener.onPositive(v);
            }
        });

        cardViewCancel.setOnClickListener(v -> {
            if(onClickListener != null){
                onClickListener.onCancel(v);
            }
        });

        confirmView.setOnClickListener(v -> {
            if(onClickListener != null){
                onClickListener.onConfirm(v);
            }
        });

        initChannel();
    }

    private void initChannel() {
        cancelView.setTypeface(FontUtils.typefaceBold);
        positiveView.setTypeface(FontUtils.typefaceBold);
        confirmView.setTypeface(FontUtils.typefaceBold);
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
        if(messageView != null){
            messageView.setText(messageText);
        }
    }

    public void setMessageSummaryText(String messageSummaryText) {
        this.messageSummaryText = messageSummaryText;
        if(messageSummaryView != null){
            messageSummaryView.setText(messageSummaryText);
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
    public void onFocusChange(View v, boolean hasFocus) {
        MaterialCardView cardView = (v instanceof MaterialCardView) ? (MaterialCardView) v : null;
        if (cardView == null) return;
        final int strokeWidth = DisplayUtil.dip2px(v.getContext(), 2);
        if (hasFocus) {
            cardView.setStrokeColor(v.getContext().getColor(R.color.all_focused_frame_color));
            cardView.setStrokeWidth(strokeWidth);
            CommonUtils.scaleView(v, 1.1f);
        } else {
            cardView.setStrokeColor(0x00FFFFFF);
            cardView.setStrokeWidth(0);
            v.clearAnimation();
            CommonUtils.scaleView(v, 1f);
        }
    }

    public interface OnClickListener {
        void onConfirm(View v);
        void onPositive(View v);
        void onCancel(View v);
    }

    public CustomAlertDialog setOnClickListener(OnClickListener onClickListener) {
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
