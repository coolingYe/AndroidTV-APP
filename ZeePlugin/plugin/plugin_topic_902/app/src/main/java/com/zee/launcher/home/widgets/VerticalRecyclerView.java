package com.zee.launcher.home.widgets;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

public class VerticalRecyclerView extends RecyclerView {

    public VerticalRecyclerView(@NonNull Context context) {
        super(context);
    }

    public VerticalRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public VerticalRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();

    }

    private void initView(){
        setDescendantFocusability(FOCUS_BEFORE_DESCENDANTS);
        setHasFixedSize(true);
        setWillNotDraw(true);
        setOverScrollMode(View.OVER_SCROLL_NEVER);
        setChildrenDrawingOrderEnabled(true);

        setClipChildren(false);
        setClipToPadding(false);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean result = super.dispatchKeyEvent(event);
        View focusView = getFocusedChild();
        if (focusView == null) {
            return result;
        }
        int dy = 0;
        if (getChildCount() > 0) {
            View firstView = this.getChildAt(0);
            dy = firstView.getHeight();
        }

        int keyCode = event.getKeyCode();
        if (event.getAction() == KeyEvent.ACTION_DOWN
                && (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN)) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                View nextFocusView = FocusFinder.getInstance().findNextFocus(this, focusView, View.FOCUS_UP);
                if (nextFocusView == null) {
                    if(canScrollVertically(-1)) {
                        smoothScrollBy(0, -dy);
                        return true;
                    }
                }
            } else {
                View nextFocusView = FocusFinder.getInstance().findNextFocus(this, focusView, View.FOCUS_DOWN);
                if (nextFocusView == null) {
                    smoothScrollBy(0, dy);
                    return true;
                }
            }
        }
        return result;
    }

    @Override
    public void requestChildFocus(View child, View focused) {
        super.requestChildFocus(child, focused);
    }

    @Override
    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        return super.requestFocus(direction, previouslyFocusedRect);
    }

    /*private long mLastKeyDownTime;
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        long current = System.currentTimeMillis();
        if (event.getAction() != KeyEvent.ACTION_DOWN || getChildCount() == 0) {
            return super.dispatchKeyEvent(event);
        }
        // 限制两个KEY_DOWN事件的最低间隔为120ms
        if (isComputingLayout() || current - mLastKeyDownTime <= 300) {
            return true;
        }
        mLastKeyDownTime = current;
        return super.dispatchKeyEvent(event);
    }*/

}
