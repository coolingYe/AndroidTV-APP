package com.zee.launcher.home.widgets;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zee.launcher.home.R;

public class BackView extends FrameLayout {

    private FrameLayout flBackView;

    public BackView(@NonNull Context context) {
        this(context, null);
    }

    public BackView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BackView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context){
        LayoutInflater.from(context).inflate(R.layout.layout_back_view, this);
        flBackView = findViewById(R.id.fl_back_view);
        setBackgroundResource(R.drawable.selector_transparent_bg);
    }

    public void startLoading(int index){
        AnimationDrawable animationDrawable = (AnimationDrawable)flBackView.getBackground();
        if(animationDrawable != null && animationDrawable.getNumberOfFrames() > 0) {
            if (index < 0) {
                index = 0;
            } else if (index > animationDrawable.getNumberOfFrames() - 1) {
                index = animationDrawable.getNumberOfFrames() - 1;
            }
            animationDrawable.selectDrawable(index);
        }
    }
}
