package com.zee.launcher.home.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.zee.launcher.home.widgets.banner.Banner;

public class BannerConstraintLayout extends ConstraintLayout {
    private ViewPager2 chileViewPager2;
    private ViewPager2 parentViewPager2;
    private RecyclerView parentRecyclerView = null;
    private float initialX = 0;
    private float initialY = 0;
    private OnHandleKeyEventListener onHandleKeyEventListener;

    public BannerConstraintLayout(@NonNull Context context) {
        this(context, null);
    }

    public BannerConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BannerConstraintLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        setFocusable(true);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (onHandleKeyEventListener != null) {
            boolean isHandled = onHandleKeyEventListener.handleKeyEvent(event);
            if (isHandled) return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialX = ev.getX();
                initialY = ev.getY();
                getChileView();
                getParentView();
                requestParentDisallow(true);
                break;
            case MotionEvent.ACTION_MOVE:
                float dealtX = ev.getX() - initialX;
                float dealtY = ev.getY() - initialY;
                if (Math.abs(dealtX) >= Math.abs(dealtY)) {
                    requestParentDisallow(canChildScroll(-(int) dealtX));
                } else {
                    requestParentDisallow(false);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                break;

        }

        return super.dispatchTouchEvent(ev);
    }

    private boolean canChildScroll(int direction) {
        if (chileViewPager2 != null) {
            return chileViewPager2.canScrollHorizontally(direction);
        }
        return false;
    }

    private void requestParentDisallow(boolean enable) {
        if (parentViewPager2 != null) {
            parentViewPager2.requestDisallowInterceptTouchEvent(enable);
        }

        if (parentRecyclerView != null) {
            parentRecyclerView.requestDisallowInterceptTouchEvent(enable);
        }
    }

    private void getParentView() {
        ViewParent parent = getParent();
        while (parent != null && !(parent instanceof ViewPager2)) {
            parent = parent.getParent();
        }
        if (parent != null) {
            parentViewPager2 = (ViewPager2) parent;
        }

        parent = getParent();
        while (parent != null && !(parent instanceof RecyclerView)) {
            parent = parent.getParent();
        }
        if (parent != null) {
            parentRecyclerView = (RecyclerView) parent;
        }
    }

    protected void getChileView() {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            if (getChildAt(i) instanceof Banner) {
                chileViewPager2 = ((Banner) getChildAt(i)).getViewPager2();
                break;
            }
        }
    }

    public void setOnHandleKeyEventListener(OnHandleKeyEventListener onHandleKeyEventListener) {
        this.onHandleKeyEventListener = onHandleKeyEventListener;
    }

    public interface OnHandleKeyEventListener {
        boolean handleKeyEvent(KeyEvent event);
    }
}
