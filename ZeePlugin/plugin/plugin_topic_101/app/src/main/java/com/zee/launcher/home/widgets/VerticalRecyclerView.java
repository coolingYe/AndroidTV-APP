package com.zee.launcher.home.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.function.Consumer;

public class VerticalRecyclerView extends RecyclerView {

    private Consumer<View> onUnCanScrollDownListener;
    private OnHandleKeyEventListener onHandleKeyEventListener;

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
//        setHasFixedSize(true);
//        setWillNotDraw(true);
//        setOverScrollMode(View.OVER_SCROLL_NEVER);
//        setChildrenDrawingOrderEnabled(true);

        setClipChildren(false);
        setClipToPadding(false);
    }

    public void setOnUnCanScrollDownListener(Consumer<View> onUnCanScrollDownListener) {
        this.onUnCanScrollDownListener = onUnCanScrollDownListener;
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
    public View focusSearch(View focused, int direction) {
        View realNextFocus = super.focusSearch(focused, direction);
        View nextFocus = FocusFinder.getInstance().findNextFocus(this, focused, direction);
        if (direction == FOCUS_DOWN) {
            if (nextFocus == null && !canScrollVertically(1)) {
                if (onUnCanScrollDownListener != null) {
                    onUnCanScrollDownListener.accept(focused);
                }
            }
        }
        return realNextFocus;
    }

    public void setOnHandleKeyEventListener(OnHandleKeyEventListener onHandleKeyEventListener) {
        this.onHandleKeyEventListener = onHandleKeyEventListener;
    }

    public interface OnHandleKeyEventListener {
        boolean handleKeyEvent(KeyEvent event);
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
