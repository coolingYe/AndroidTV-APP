package com.zee.device.helper.widgets;

import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.zee.device.helper.R;

public class SwipeRecyclerView extends RecyclerView {
    private static final String TAG = "SwipeRecyclerView";
    /**
     * 滚动事件的阙值
     */
    private int scrollSlop;
    /**
     * 拦截模式下，是否有滚动过
     */
    private boolean isScrollInIntercept;
    /**
     * 滚动相关类
     */
    private Scroller mScroller;
    /**
     * Down 事件的坐标点，用于 Fling 动作
     */
    private float rawDownX, rawDownY;
    /**
     * Move 事件(上次事件)的坐标点，用于 Scroll 动作
     */
    private float lastX;
    /**
     * 记录的主手指
     */
    private int pointerId;
    /**
     * 一次事件流中，是水平滑动，还是竖直滑动的标识
     */
    private boolean isHorScroll, isVerScroll;
    /**
     * Down 事件按下时的 ViewHolder，lastHolder 为手指按下时，正在显示菜单栏的 ViewHolder
     * 拦截模式下，是对 lastHolder 进行操作，非拦截模式下，对 curHolder 进行操作
     */
    private ViewHolder curHolder, lastHolder;
    /**
     * 菜单是否显示，将 RV 分成两种模式：拦截模式和非拦截模式
     * 菜单栏显示时，二次点击的不是当前显示菜单的 item，就是拦截模式，否则就是非拦截模式
     */
    private boolean isInterceptMode;
    /**
     * 菜单栏的最大显示宽度，也是每个 item 可滚动的最大宽度
     */
    private int maxScrollDistance;
    /**
     * 当前滑动的方向，大于 0 表示向左，小于 0 表示向右，等于 0 则表示既不左滑也不右滑
     * 此值还用于判断菜单显示隐藏的方法是否需要回调
     */
    private int scrollDirection;

    private OnMenuClickListener mOnMenuClickListener;
    private boolean isClickMenu = false;

    public SwipeRecyclerView(@NonNull Context context) {
        this(context, null);
    }

    public SwipeRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        ViewConfiguration configuration = ViewConfiguration.get(getContext());
        scrollSlop = configuration.getScaledTouchSlop();
        mScroller = new Scroller(getContext());
    }

    /**
     * 设置菜单栏状态变化的回调
     */
    public SwipeRecyclerView setOnMenuStateChangeListener(OnMenuClickListener listener) {
        mOnMenuClickListener = listener;
        return this;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        curHolder = null;
        lastHolder = null;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        // 拦截模式下，事件全部拦截
        int pointerIndex;
        float x, y;
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            if (!mScroller.isFinished()) {
                mScroller.forceFinished(true);
            }
            // 避免多指事件干扰
            pointerIndex = event.getActionIndex();
            x = event.getX(pointerIndex);
            y = event.getY(pointerIndex);

            ViewHolder holder = findViewHolder(event);
            // 显示菜单栏，就拦截
            if (isClickMenu) {
                curHolder = holder;
                lastHolder = curHolder;
                isClickMenu = false;
            } else {
                lastHolder = curHolder;
            }
            if (isMenuShowing(lastHolder)) {
                maxScrollDistance = calculateMaxScrollDistance(lastHolder);
                // 菜单栏显示时，如果二次点击的不是当前显示菜单栏的 item，则需要拦截事件，隐藏菜单栏
                if (holder != null && lastHolder != null && holder.getAdapterPosition()
                        != lastHolder.getAdapterPosition()) {
                    hideMenu(lastHolder);
                    // ----------------------------拦截模式----------------------------
                } else if (isClickMenu(lastHolder, event)) {
                    isInterceptMode = false;
                    isClickMenu = true;
                    hideMenu(lastHolder);
                    mOnMenuClickListener.onMenuClick(holder.getAdapterPosition());
                    return super.onInterceptTouchEvent(event);
                }
                isInterceptMode = true;
            } else {
                // ----------------------------非拦截模式----------------------------
                isInterceptMode = false;
                maxScrollDistance = calculateMaxScrollDistance(holder);
            }
            curHolder = holder;
            // 二次点击时，点击的不是显示菜单的 item，则拦截，隐藏 item
            rawDownX = lastX = x;
            rawDownY = y;
            // Down 事件时，必定有至少一根手指，这个手指的 index 至少为 0
            pointerId = event.getPointerId(0);
            // 避免自动 Scroll 的影响
            isHorScroll = isVerScroll = false;
            isScrollInIntercept = false;
            scrollDirection = 0;

            if (isInterceptMode) {
                return true;
            }
            return super.onInterceptTouchEvent(event);
        }
        return super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int pointerIndex;
        float x, y;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_MOVE:
                pointerIndex = event.findPointerIndex(pointerId);
                x = event.getX(pointerIndex);
                y = event.getY(pointerIndex);
                // 优先检测竖直方向的滑动
                if (!isVerScroll && Math.abs(x - rawDownX) < Math.abs(y - rawDownY) && Math.abs(y - rawDownY) > scrollSlop) {
                    isVerScroll = true;
                }
                if (!isHorScroll && Math.abs(x - rawDownX) > Math.abs(y - rawDownY) && Math.abs(x - rawDownX) > scrollSlop) {
                    isHorScroll = true;
                }
                // ----------------------------拦截模式----------------------------
                if (isInterceptMode) {
                    if (lastHolder == null || curHolder == null) {
                        return true;
                    }
                    if (lastHolder.getAdapterPosition() != curHolder.getAdapterPosition()) {
                        return true;
                    } else {
                        pointerIndex = event.findPointerIndex(pointerId);
                        // 大于阙值，才进行滚动
                        if (Math.abs(rawDownX - event.getX(pointerIndex)) > scrollSlop) {
                            isScrollInIntercept = true;
                            return dealScroll(lastHolder, event);
                        }
                    }
                }
                // ----------------------------非拦截模式----------------------------
                // 竖直滚动，不额外实现逻辑
                if (isVerScroll) {
                    return super.onTouchEvent(event);
                }
                // 水平滚动
                return dealScroll(curHolder, event);
            case MotionEvent.ACTION_POINTER_UP:
                // 此段代码是 Google 官方写法
                // 抬起的手指的索引
                pointerIndex = event.getActionIndex();
                int tempPointerId = event.getPointerId(pointerIndex);
                // 如果抬起的手指是当前追踪的手指，则换下个手指追踪
                if (tempPointerId == pointerId) {
                    int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    lastX = event.getX(newPointerIndex);
                    pointerId = event.getPointerId(newPointerIndex);
                }
                return super.onInterceptTouchEvent(event);
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // ----------------------------拦截模式----------------------------
                if (isInterceptMode) {
                    if (isMenuShowing(lastHolder)) {
                        if (isScrollInIntercept) {
                            // 滚动后当做 Fling 处理
                            dealFling(lastHolder, event);
                        } else {
                            hideMenu(lastHolder);
                        }
                    }
                    return true;
                }
                // ----------------------------非拦截模式----------------------------
                if (isVerScroll) {
                    hideMenu(lastHolder);
                    return super.onTouchEvent(event);
                } else {
                    dealFling(curHolder, event);
                    return true;
                }
        }
        return super.onTouchEvent(event);
    }

    /**
     * Scroll 动作是在 Move 事件中触发的
     *
     * @return true 表示需要滚动，false 不需要滚动
     */
    private boolean dealScroll(ViewHolder holder, MotionEvent event) {
        if (holder == null) {
            return false;
        }
        if (maxScrollDistance <= 0) {
            return false;
        }
        int pointerIndex = event.findPointerIndex(pointerId);
        float x = event.getX(pointerIndex);
        int offsetX = (int) (lastX - x);
        // 判断条件范围
        // 手指从右向左移动，布局也从右向左移动，左移到达最大值
        if (offsetX > 0 && holder.itemView.getScrollX() + offsetX > maxScrollDistance) {
            scrollDirection = offsetX;
            offsetX = maxScrollDistance - holder.itemView.getScrollX();
        }
        // 手指从左向右移动，布局也从左向右移动，右移到达最大值
        if (offsetX < 0 && holder.itemView.getScrollX() + offsetX < 0) {
            scrollDirection = offsetX;
            offsetX = -holder.itemView.getScrollX();
        }

        // 竖直方向不移动
        curHolder.itemView.scrollBy(offsetX, 0);
        invalidate();

        lastX = x;
        return true;
    }

    /**
     * 是否判定为水平方向的 Fling，如果是，则执行 Fling 动作
     * <p>
     * Fling 动作是在 Up 事件中触发的
     */
    private boolean dealFling(ViewHolder holder, MotionEvent event) {
        float nowX = event.getX();
        float offsetX = rawDownX - nowX;

        if (holder == null || holder.itemView.getScrollX() <= 0) {
            return false;
        }
        // 没有可滚动的距离
        if (maxScrollDistance < 0) {
            return false;
        }
        int dx;
        // 菜单栏显示
        // 拖动距离不足 1/3 时动作不生效
        if (Math.abs(offsetX) < (float) maxScrollDistance / 3) {
            if (offsetX > 0) {
                dx = -holder.itemView.getScrollX();
            } else {
                // 回弹不隐藏
                dx = maxScrollDistance - holder.itemView.getScrollX();
            }
        } else {
            scrollDirection = (int) offsetX;
            if (offsetX > 0) {
                dx = maxScrollDistance - holder.itemView.getScrollX();
            } else {
                dx = -holder.itemView.getScrollX();
            }
            // 不需要滑动，说明到达最大值，不再回调
            scrollDirection = dx;
        }

        // 滑动到目标位置
        mScroller.startScroll(holder.itemView.getScrollX(), 0, dx, 0);
        invalidate();

        return true;
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            // 拦截模式下，需要的是上个显示菜单的 item 滚动，而不是当前被点的 item 滚动
            if (isInterceptMode) {
                if (lastHolder == null) {
                    return;
                }
                lastHolder.itemView.scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
                onScrolling(mScroller.getCurrX(), mScroller.getCurrY());
            } else {
                if (curHolder == null) {
                    return;
                }
                curHolder.itemView.scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
                onScrolling(mScroller.getCurrX(), mScroller.getCurrY());
            }
            invalidate();
        }
    }

    private void onScrolling(int scrollX, int scrollY) {
        // 左滑达到最大值
        if (scrollDirection > 0 && scrollX >= maxScrollDistance) {
            notifyItem(true);
            // 回调一次之后，不再回调
            scrollDirection = 0;
        } else if (scrollDirection < 0 && scrollX <= 0) {
            notifyItem(false);
            scrollDirection = 0;
        }
    }

    /**
     * -1 代表不可滑动
     */
    private int calculateMaxScrollDistance(ViewHolder viewHolder) {

        if (viewHolder == null) {
            return -1;
        }
        // TODO View 的 id 需要自己定义
        ViewGroup rootViewGroup = viewHolder.itemView.findViewById(R.id.menu);
        if (rootViewGroup == null || rootViewGroup.getChildCount() < 0) {
            return -1;
        }
        int result = 0;
        View view;
        // 根布局是水平方向的 LL，直接从左向右加 item 的宽度
        for (int i = 0; i < rootViewGroup.getChildCount(); i++) {
            view = rootViewGroup.getChildAt(i);
            if (view == null || view.getVisibility() == View.GONE) {
                continue;
            }
            result += view.getMeasuredWidth();
        }
        return result;
    }

    private ViewHolder findViewHolder(MotionEvent event) {
        View view = findChildViewUnder(event.getX(), event.getY());
        if (view == null) {
            return null;
        }
        return findContainingViewHolder(view);
    }

    /**
     * 菜单栏是否在显示的标识
     *
     * @return true 表示菜单在显示，false 表示菜单未显示
     */
    private boolean isMenuShowing(ViewHolder holder) {
        if (holder == null) {
            return false;
        }
        return holder.itemView.getScrollX() > 0;
    }

    /**
     * 隐藏菜单栏
     */
    private boolean hideMenu(ViewHolder holder) {
        if (holder == null) {
            return false;
        }
        mScroller.startScroll(holder.itemView.getScrollX(), 0, -holder.itemView.getScrollX(), 0);
        invalidate();
        return true;
    }

    private void notifyItem(boolean isVisible) {
        ViewHolder holder;
        if (isInterceptMode) {
            holder = lastHolder;
        } else {
            holder = curHolder;
        }
    }

    private boolean isClickMenu(ViewHolder holder, MotionEvent event) {
        if (!isMenuShowing(holder)) {
            return false;
        }
        View menuView = holder.itemView.findViewById(R.id.menu);
        if (menuView == null) {
            return false;
        }

        int pointerIndex = event.getActionIndex();
        float x, y;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            x = event.getRawX(pointerIndex);
            y = event.getRawY(pointerIndex);
        } else {
            x = event.getRawX();
            y = event.getRawY();
        }

        Rect location = new Rect();
        menuView.getGlobalVisibleRect(location);
        return location.contains((int) x, (int) y);
    }

    public interface OnMenuClickListener {
        /**
         * 菜单栏菜单点击时，会回调此接口
         *
         * @param menuPos   菜单栏点击的位置，对应  RV 中的 pos
         */
        void onMenuClick(int menuPos);
    }
}