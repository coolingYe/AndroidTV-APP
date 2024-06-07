package com.zeewain.base.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextClock;

public class CustomTextClock extends TextClock {
    public CustomTextClock(Context context) {
        super(context);
    }

    public CustomTextClock(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomTextClock(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomTextClock(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean is24HourModeEnabled() {
        return true;
    }
}
