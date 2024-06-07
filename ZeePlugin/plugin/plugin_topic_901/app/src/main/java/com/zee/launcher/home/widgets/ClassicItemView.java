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

public class ClassicItemView extends ConstraintLayout {
    private ImageView imgClassicItem;
    private ImageView imgClassicItemLoading;
    private ImageView imgGestureTip;

    public ClassicItemView(@NonNull Context context) {
        this(context, null);
    }

    public ClassicItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClassicItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context){
        LayoutInflater.from(context).inflate(R.layout.layout_classic_item_view, this);
        imgClassicItem = findViewById(R.id.img_classic_item);
        imgClassicItemLoading = findViewById(R.id.img_classic_item_loading);
        imgGestureTip = findViewById(R.id.img_gesture_tip);
    }

    public void setClassicItemBackground(int resId){
        imgClassicItem.setBackgroundResource(resId);
    }

    public void setClassicItemGestureTip(int resId){
        imgGestureTip.setImageResource(resId);
    }

    public void startClassicItemAnim(){
        AnimationDrawable animationDrawable = (AnimationDrawable) imgClassicItem.getBackground();
        animationDrawable.stop();
        animationDrawable.start();
    }

    public void startLoading(int index){
        AnimationDrawable animationDrawable = (AnimationDrawable) imgClassicItemLoading.getBackground();
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
