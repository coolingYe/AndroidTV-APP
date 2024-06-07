package com.zwn.user.ui.user;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.zwn.user.R;

public class QrAlertDialog extends AlertDialog {

    private ImageView ivQrCode;
    private Bitmap bitmap;

    protected QrAlertDialog(@NonNull Context context) {
        super(context, R.style.CustomAlertDialog);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_user_qr);
        setCanceledOnTouchOutside(false);
        ImageView ivClose = findViewById(R.id.iv_dialog_close);
        if (ivClose != null) {
            ivClose.setOnClickListener(v -> cancel());
        }
        TextView tvHelpDesc = findViewById(R.id.tv_dialog_help_desc_2);
        if (tvHelpDesc != null) {
            tvHelpDesc.setText(tvHelpDesc.getText().toString().replace("{}", getContext().getString(R.string.user_info_support_contact)));
        }
        ivQrCode = findViewById(R.id.iv_dialog_qr);
        if (bitmap != null) {
            if (ivQrCode != null) {
                ivQrCode.setImageBitmap(bitmap);
            }
        }
    }

    public void setQrCode(Bitmap bitmap) {
        this.bitmap = bitmap;
        if (ivQrCode != null) {
            ivQrCode.setImageBitmap(bitmap);
        }
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
