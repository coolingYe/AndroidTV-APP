package com.zee.launcher.home.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

public class HorizontalRecyclerView extends RecyclerView {
    private float initialX = 0;
    private float initialY = 0;
    private OnHandleKeyEventListener onHandleKeyEventListener;
    private ViewPager2 parentViewPager = null;
    private RecyclerView parentRecyclerView = null;
    private boolean keepFlag = false;

    public HorizontalRecyclerView(@NonNull Context context) {
        super(context);
    }

    public HorizontalRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public HorizontalRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView(){
        setDescendantFocusability(FOCUS_BEFORE_DESCENDANTS);
        setFocusable(true);
        setHasFixedSize(true);
        setWillNotDraw(true);
        setOverScrollMode(View.OVER_SCROLL_NEVER);
        setChildrenDrawingOrderEnabled(true);

        setClipChildren(false);
        setClipToPadding(false);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(onHandleKeyEventListener != null){
            boolean isHandled = onHandleKeyEventListener.handleKeyEvent(event);
            if(isHandled) return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialX = ev.getX();
                initialY = ev.getY();
                keepFlag = false;
                getParentView();
                requestParentDisallow(true);
                break;
            case MotionEvent.ACTION_MOVE:
                float dealtX = ev.getX() - initialX;
                float dealtY = ev.getY() - initialY;

                if (Math.abs(dealtX) >= Math.abs(dealtY)) {
                    if(!keepFlag) {
                        keepFlag = true;
                        if (canScrollHorizontally(-(int) dealtX)) {
                            requestParentDisallow(true);
                        } else {
                            requestParentDisallow(false);
                        }
                    }
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

    public void requestParentDisallow(boolean enable){
        if(parentViewPager != null) {
            parentViewPager.requestDisallowInterceptTouchEvent(enable);
        }

        if(parentRecyclerView != null) {
            parentRecyclerView.requestDisallowInterceptTouchEvent(enable);
        }
    }

    public void getParentView(){
        ViewParent parent = getParent();
        while (parent != null && !(parent instanceof ViewPager2)) {
            parent = parent.getParent();
        }
        if(parent != null) {
            parentViewPager = (ViewPager2)parent;
        }

        parent = getParent();
        while (parent != null && !(parent instanceof RecyclerView)) {
            parent = parent.getParent();
        }
        if(parent != null) {
            parentRecyclerView = (RecyclerView)parent;
        }
    }

    public void setOnHandleKeyEventListener(OnHandleKeyEventListener onHandleKeyEventListener) {
        this.onHandleKeyEventListener = onHandleKeyEventListener;
    }

    public interface OnHandleKeyEventListener {
        boolean handleKeyEvent(KeyEvent event);
    }
}
