package com.zee.device.base.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.zee.device.base.R;


public class RoundDialog extends Dialog {

    View customView;

    private boolean backCancel = true;
    private boolean onTouchOutsideCanceled = false;

    public RoundDialog(@NonNull Context context) {
        this(context, androidx.appcompat.R.style.Theme_AppCompat_Dialog);
    }

    public RoundDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_round_layout);
        Window window = getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setCanceledOnTouchOutside(onTouchOutsideCanceled);
        setCancelable(backCancel);
        initView();
    }

    private void initView(){
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.width= ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.height= ViewGroup.LayoutParams.WRAP_CONTENT;
        getWindow().getDecorView().setPadding(0, 0, 0, 0);
        getWindow().setAttributes(layoutParams);

        LinearLayout linearLayout = (LinearLayout)findViewById(R.id.ll_round_layout);
        linearLayout.addView(customView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));

    }

    public void addCustomView(View customView){
        this.customView = customView;
    }

    public void setBackCancel(boolean backCancel) {
        this.backCancel = backCancel;
    }

    public void setOnTouchOutsideCanceled(boolean onTouchOutsideCanceled) {
        this.onTouchOutsideCanceled = onTouchOutsideCanceled;
    }
}
