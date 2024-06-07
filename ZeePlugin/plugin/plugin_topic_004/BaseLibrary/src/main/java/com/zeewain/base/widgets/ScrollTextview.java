package com.zeewain.base.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ScrollTextview extends androidx.appcompat.widget.AppCompatTextView {

    private View mNextFocusUp;
    private View mNextFocusLeft;
    private View mNextFocusDown;

    public ScrollTextview(@NonNull Context context) {
        super(context);
    }

    public ScrollTextview(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ScrollTextview(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setNextFocusUp(View mNextFocusUp) {
        this.mNextFocusUp = mNextFocusUp;
        initKeyListener();
    }

    public void setNextFocusLeft(View mNextFocusLeft) {
        this.mNextFocusLeft = mNextFocusLeft;
        initKeyListener();
    }

    public void setNextFocusDown(View mNextFocusDown) {
        this.mNextFocusDown = mNextFocusDown;
        initKeyListener();
    }

    private void initKeyListener() {
        setOnKeyListener((view, keyCode, keyEvent) -> {
            boolean handled = false;
            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                switch (keyEvent.getKeyCode()) {
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        if (mNextFocusLeft != null) {
                            clearFocus();
                            mNextFocusLeft.requestFocus();
                            handled = true;
                        }
                        break;
                    case KeyEvent.KEYCODE_DPAD_UP:
                        if (view.getScrollY() == 0) {
                            if (mNextFocusUp != null) {
                                clearFocus();
                                mNextFocusUp.requestFocus();
                                handled = true;
                            }
                        }
                        break;
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        int lines = getLayout().getLineCount();
                        int lastLineBottom = getLayout().getLineBottom(lines - 1);
                        int textViewHeight = getHeight();
                        int maxScrollY = lastLineBottom - textViewHeight;
                        if (view.getScrollY() == maxScrollY) {
                            if (mNextFocusDown != null) {
                                clearFocus();
                                mNextFocusDown.requestFocus();
                                handled = true;
                            }
                        }
                        break;
                }
            }
            return handled;
        });
    }

}
