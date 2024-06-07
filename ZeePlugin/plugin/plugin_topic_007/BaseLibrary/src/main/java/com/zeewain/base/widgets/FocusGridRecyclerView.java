package com.zeewain.base.widgets;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zeewain.base.R;


public class FocusGridRecyclerView extends RecyclerView {
    public FocusGridRecyclerView(@NonNull Context context) {
        super(context);
    }

    public FocusGridRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FocusGridRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, @Nullable Rect previouslyFocusedRect) {

        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        executeKeyEvent(event);
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            int keyCode = event.getKeyCode();
            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                View focusedView = getFocusedChild();
                if (focusedView != null) {
                    View nextFocusView;
                    try {
                        if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                            nextFocusView = FocusFinder.getInstance().findNextFocus(this, focusedView, View.FOCUS_UP);
                        } else {
                            nextFocusView = FocusFinder.getInstance().findNextFocus(this, focusedView, View.FOCUS_DOWN);
                        }
                    } catch (Exception e) {
                        nextFocusView = null;
                    }
                    if (nextFocusView == null) {
                        if (getLayoutManager() != null && getAdapter() != null) {
                            GridLayoutManager gridLayoutManager = (GridLayoutManager) getLayoutManager();
                            if (gridLayoutManager.findFirstCompletelyVisibleItemPosition() == 0) {
                                return super.dispatchKeyEvent(event);
                            }
                        }
                        focusedView.requestFocus();
                        return true;
                    }
                }
            }
        }
        return super.dispatchKeyEvent(event);
    }

    public boolean executeKeyEvent(@NonNull KeyEvent event) {
        boolean handled = false;
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    handled = arrowScroll(FOCUS_LEFT);
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    handled = arrowScroll(FOCUS_RIGHT);
                    break;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    handled = arrowScroll(FOCUS_DOWN);
                    break;
            }
        }
        return handled;
    }

    public boolean arrowScroll(int direction) {

        View currentFocused = findFocus();
        if (currentFocused == this) {
            currentFocused = null;
        } else if (currentFocused != null) {
            boolean isChild = false;
            for (ViewParent parent = currentFocused.getParent(); parent instanceof ViewGroup;
                 parent = parent.getParent()) {
                if (parent == this) {
                    isChild = true;
                    break;
                }
            }
            if (!isChild) {
                // This would cause the focus search down below to fail in fun ways.
                final StringBuilder sb = new StringBuilder();
                sb.append(currentFocused.getClass().getSimpleName());
                for (ViewParent parent = currentFocused.getParent(); parent instanceof ViewGroup;
                     parent = parent.getParent()) {
                    sb.append(" => ").append(parent.getClass().getSimpleName());
                }
                currentFocused = null;
            }
        }

        boolean handled = false;

        View nextFocused = FocusFinder.getInstance().findNextFocus(this, currentFocused,
                direction);
        if (nextFocused == null || nextFocused == currentFocused) {
            if (direction == FOCUS_LEFT || direction == FOCUS_RIGHT) {
                shakeX(currentFocused);
                handled = true;
            } else if (direction == FOCUS_UP || direction == FOCUS_DOWN) {
                shakeY(currentFocused);
                handled = true;
            }
        }
        return handled;
    }

    private void shakeX(View currentFocused) {
        if (currentFocused != null && getScrollState() == SCROLL_STATE_IDLE) {
            currentFocused.clearAnimation();
            currentFocused.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.host_shake));
        }
    }

    private void shakeY(View currentFocused) {
        if (currentFocused != null && getScrollState() == SCROLL_STATE_IDLE) {
            currentFocused.clearAnimation();
            currentFocused.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.host_shake_y));
        }
    }
}
