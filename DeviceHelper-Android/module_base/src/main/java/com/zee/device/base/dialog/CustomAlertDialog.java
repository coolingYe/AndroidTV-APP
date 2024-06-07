package com.zee.device.base.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.card.MaterialCardView;
import com.zee.device.base.R;


public class CustomAlertDialog extends AlertDialog {
    private String messageText, positiveText, cancelText, confirmText;
    private TextView messageView;
    private TextView positiveView;
    private TextView cancelView;
    private TextView confirmView;

    private TextView helpView;

    private OnClickListener onClickListener;

    private boolean isShowHelp = false;

    public CustomAlertDialog(@NonNull Context context) {
        super(context, R.style.CustomAlertDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_common_tip_layout);
        setCanceledOnTouchOutside(false);

        messageView = findViewById(R.id.tv_dialog_message);
        positiveView = findViewById(R.id.tv_dialog_positive);
        cancelView = findViewById(R.id.tv_dialog_cancel);
        confirmView = findViewById(R.id.tv_dialog_confirm);
        helpView = findViewById(R.id.tv_dialog_help);

        MaterialCardView cardViewCancel = findViewById(R.id.mcv_dialog_cancel);
        MaterialCardView cardViewPositive = findViewById(R.id.mcv_dialog_positive);
        MaterialCardView cardViewConfirm = findViewById(R.id.mcv_dialog_confirm);

        ConstraintLayout positiveCancelLayout = findViewById(R.id.layout_positive_cancel_dialog);

        if (messageText != null) {
            messageView.setText(messageText);
        }

        if (positiveText != null) {
            positiveView.setText(positiveText);
        }

        if (cancelText != null) {
            cancelView.setText(cancelText);
        }

        if (confirmText != null) {
            confirmView.setText(confirmText);
            positiveCancelLayout.setVisibility(View.GONE);
            cardViewConfirm.setVisibility(View.VISIBLE);
        } else {
            cardViewConfirm.setVisibility(View.GONE);
            positiveCancelLayout.setVisibility(View.VISIBLE);
        }

        helpView.setVisibility(isShowHelp ? View.VISIBLE : View.GONE);

        cardViewPositive.setOnClickListener(v -> {
            if (onClickListener != null) {
                onClickListener.onPositive(v);
            }
        });

        cardViewCancel.setOnClickListener(v -> {
            if (onClickListener != null) {
                onClickListener.onCancel(v);
            }
        });

        cardViewConfirm.setOnClickListener(v -> {
            if (onClickListener != null) {
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

    public void setPositiveText(String positiveText) {
        this.positiveText = positiveText;
        if (positiveView != null) {
            positiveView.setText(positiveText);
        }
    }

    public void setCancelText(String cancelText) {
        this.cancelText = cancelText;
        if (cancelView != null) {
            cancelView.setText(cancelText);
        }
    }

    public void setConfirmText(String confirmText) {
        this.confirmText = confirmText;
        if (confirmView != null) {
            confirmView.setText(confirmText);
        }
    }

    public void showHelpText(boolean isShow) {
        isShowHelp = isShow;
        if (helpView != null) {
            helpView.setVisibility(isShow ? View.VISIBLE : View.GONE);
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
}
