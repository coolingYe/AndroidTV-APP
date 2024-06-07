package com.zee.setting.views;

import android.content.Context;
import android.util.AttributeSet;

import androidx.core.widget.NestedScrollView;

public class FocusedScrollView extends NestedScrollView {
    public FocusedScrollView(Context context) {
        super(context);
    }

    public FocusedScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FocusedScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean isFocused() {
        return true;

    }


}

