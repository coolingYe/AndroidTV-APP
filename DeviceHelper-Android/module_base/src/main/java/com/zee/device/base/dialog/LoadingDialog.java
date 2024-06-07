package com.zee.device.base.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.zee.device.base.R;


public class LoadingDialog extends Dialog {
    private Animation animation;
    private ImageView imageView;
    private TextView textView;

    public LoadingDialog(@NonNull Context context) {
        this(context, androidx.appcompat.R.style.Theme_AppCompat_Dialog);
    }

    private LoadingDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        setContentView(R.layout.layout_dialog_loading);
        setCancelable(true);
        setCanceledOnTouchOutside(true);
        getWindow().getAttributes().gravity = Gravity.CENTER;

        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.dimAmount = 0.2f;
        getWindow().setAttributes(lp);

        imageView = findViewById(R.id.img_progress_loading);
        textView = findViewById(R.id.tv_loading_tip);
        animation = AnimationUtils.loadAnimation(imageView.getContext(), R.anim.rotate_loading_anim);
        LinearInterpolator interpolator = new LinearInterpolator();
        animation.setInterpolator(interpolator);
    }

    public void showLoading(){
        show();
        imageView.clearAnimation();
        imageView.setAnimation(animation);
        animation.start();
    }

    public void hideLoading(){
        animation.cancel();
        imageView.clearAnimation();
        super.dismiss();
    }

    public void setLoadingTip(String tip){
        if(tip == null || tip.isEmpty()){
            textView.setText("");
        }else{
            textView.setText(tip);
        }
    }

}
