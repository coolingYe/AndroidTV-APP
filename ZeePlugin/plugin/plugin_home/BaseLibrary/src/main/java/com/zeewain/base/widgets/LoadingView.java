package com.zeewain.base.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.zeewain.base.R;

public class LoadingView extends LinearLayout {
    private ImageView imageView;
    private TextView textView;
    private Animation animation;
    public LoadingView(Context context) {
        this(context, null);
    }

    public LoadingView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context){
        LayoutInflater.from(context).inflate(R.layout.layout_loading_view, this);
        imageView = findViewById(R.id.iv_loading);
        textView = findViewById(R.id.txt_loading);

        animation = AnimationUtils.loadAnimation(imageView.getContext(), R.anim.rotate_loading_anim);
        LinearInterpolator interpolator = new LinearInterpolator();
        animation.setInterpolator(interpolator);
    }

    public void setText(String text){
        textView.setText(text);
    }

    public void startAnim(){
        imageView.clearAnimation();
        imageView.setAnimation(animation);
        animation.start();
    }

    public void stopAnim(){
        animation.cancel();
        imageView.clearAnimation();
    }

    @Override
    protected void onDetachedFromWindow() {
        if(imageView != null)
            imageView.clearAnimation();
        super.onDetachedFromWindow();
    }
}
