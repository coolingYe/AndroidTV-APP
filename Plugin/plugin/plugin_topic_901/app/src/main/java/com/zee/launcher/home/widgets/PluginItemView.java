package com.zee.launcher.home.widgets;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.zee.launcher.home.R;

public class PluginItemView extends ConstraintLayout {
    private ImageView imgPluginItem;
    private ImageView imgPluginItemLoading;
    private ImageView imgGestureTip;

    public PluginItemView(@NonNull Context context) {
        this(context, null);
    }

    public PluginItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PluginItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context){
        LayoutInflater.from(context).inflate(R.layout.layout_plugin_item_view, this);
        imgPluginItem = findViewById(R.id.img_plugin_item);
        imgPluginItemLoading = findViewById(R.id.img_plugin_item_loading);
        imgGestureTip = findViewById(R.id.img_gesture_tip);
    }

    public void setPluginItemBackground(int resId){
        imgPluginItem.setBackgroundResource(resId);
    }

    public void setPluginItemGestureTip(int resId){
        imgGestureTip.setImageResource(resId);
    }

    public void startPluginItemAnim(){
        AnimationDrawable animationDrawable = (AnimationDrawable) imgPluginItem.getBackground();
        animationDrawable.stop();
        animationDrawable.start();
    }

    public void startLoading(int index){
        AnimationDrawable animationDrawable = (AnimationDrawable) imgPluginItemLoading.getBackground();
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
