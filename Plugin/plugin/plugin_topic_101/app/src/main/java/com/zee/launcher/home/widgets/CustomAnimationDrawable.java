package com.zee.launcher.home.widgets;

import android.graphics.drawable.AnimationDrawable;

public class CustomAnimationDrawable extends AnimationDrawable {
    private AnimationEndListener animationEndListener;
    public CustomAnimationDrawable(AnimationDrawable animationDrawable, boolean oneShot) {
        setOneShot(oneShot);
        for(int i = 0; i < animationDrawable.getNumberOfFrames(); i++) {
            this.addFrame(animationDrawable.getFrame(i), animationDrawable.getDuration(i));
        }
    }

    public void setAnimationEndListener(AnimationEndListener animationEndListener) {
        this.animationEndListener = animationEndListener;
    }

    @Override
    public boolean selectDrawable(int index) {
        if (index != 0 && index == getNumberOfFrames() - 1) {
            if (animationEndListener != null) animationEndListener.onAnimationEnd();
        }
        return super.selectDrawable(index);
    }

    public interface AnimationEndListener {
        void onAnimationEnd();
    }
}
