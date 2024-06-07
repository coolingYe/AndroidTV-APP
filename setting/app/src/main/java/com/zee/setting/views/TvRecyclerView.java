package com.zee.setting.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zee.setting.R;

public  class TvRecyclerView extends RecyclerView {
    //正常跟随滚动
    private static final int SCROLL_NORMAL = 0;
    //居中滚动
    private static final int SCROLL_FOLLOW = 1;

    //滚动模式
    private int scrollModel;

    //当前选中的position
    private int mSelectedPosition = 0;

    //下一个聚焦的View
    private View mNextFocused;

    public TvRecyclerView(@NonNull Context context) {
        this(context, null);
    }

    public TvRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public TvRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }





    /**
     * 初始化
     *
     * @param context
     * @param attrs
     * @param defStyle
     */
    private void init(Context context, AttributeSet attrs, int defStyle) {
        initView();

        initAttr(attrs);
    }

    /**
     * 初始化View
     * 为避免recycleview焦点混乱常用的一些设置
     */
    private void initView() {
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
        setHasFixedSize(true);
        setWillNotDraw(true);
        setOverScrollMode(View.OVER_SCROLL_NEVER);
        setChildrenDrawingOrderEnabled(true);

        setClipChildren(false);
        setClipToPadding(false);

        setClickable(false);
        setFocusable(true);
        setFocusableInTouchMode(true);
        /**
         防止RecyclerView刷新时焦点不错乱bug的步骤如下:
         (1)adapter执行setHasStableIds(true)方法
         (2)重写getItemId()方法,让每个view都有各自的id
         (3)RecyclerView的动画必须去掉
         */
        setItemAnimator(null);
    }

    /**
     * 初始化样式
     * 是否居中滚动
     *
     * @param attrs
     */
    private void initAttr(AttributeSet attrs) {
        TypedArray typeArray = getContext().obtainStyledAttributes(attrs, R.styleable.TvRecyclerView);
        scrollModel = typeArray.getInteger(R.styleable.TvRecyclerView_scrollMode, 0);
    }

    /**
     * 恢复回收之前的状态
     *
     * @param state
     */
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle bundle = (Bundle) state;
        Parcelable superData = bundle.getParcelable("super_data");
        super.onRestoreInstanceState(superData);
        setItemSelected(bundle.getInt("select_pos", 0));
    }

    /**
     * 回收之前保存状态
     *
     * @return
     */
    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        Parcelable superData = super.onSaveInstanceState();
        bundle.putParcelable("super_data", superData);
        bundle.putInt("select_pos", mSelectedPosition);
        return bundle;
    }

    /**
     * 解决4.4版本抢焦点的问题
     *
     * @return
     */
    @Override
    public boolean isInTouchMode() {
        if (Build.VERSION.SDK_INT == 19) {
            return !(hasFocus() && !super.isInTouchMode());
        } else {
            return super.isInTouchMode();
        }
    }

    @Override
    public void requestChildFocus(View child, View focused) {
        super.requestChildFocus(child, focused);
    }

    @Override
    public boolean requestChildRectangleOnScreen(View child, Rect rect, boolean immediate) {
        final int parentLeft = getPaddingLeft();
        final int parentRight = getWidth() - getPaddingRight();

        final int parentTop = getPaddingTop();
        final int parentBottom = getHeight() - getPaddingBottom();

        final int childLeft = child.getLeft() + rect.left;
        final int childTop = child.getTop() + rect.top;

        final int childRight = childLeft + rect.width();
        final int childBottom = childTop + rect.height();

        final int offScreenLeft = Math.min(0, childLeft - parentLeft);
        final int offScreenRight = Math.max(0, childRight - parentRight);

        final int offScreenTop = Math.min(0, childTop - parentTop);
        final int offScreenBottom = Math.max(0, childBottom - parentBottom);


        final boolean canScrollHorizontal = getLayoutManager().canScrollHorizontally();
        final boolean canScrollVertical = getLayoutManager().canScrollVertically();

        // Favor the "start" layout direction over the end when bringing one side or the other
        // of a large rect into view. If we decide to bring in end because start is already
        // visible, limit the scroll such that start won't go out of bounds.
        final int dx;
        if (canScrollHorizontal) {
            if (ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL) {
                dx = offScreenRight != 0 ? offScreenRight
                        : Math.max(offScreenLeft, childRight - parentRight);
            } else {
                dx = offScreenLeft != 0 ? offScreenLeft
                        : Math.min(childLeft - parentLeft, offScreenRight);
            }
        } else {
            dx = 0;
        }

        // Favor bringing the top into view over the bottom. If top is already visible and
        // we should scroll to make bottom visible, make sure top does not go out of bounds.
        final int dy;
        if (canScrollVertical) {
            dy = offScreenTop != 0 ? offScreenTop : Math.min(childTop - parentTop, offScreenBottom);
        } else {
            dy = 0;
        }

        if (dx != 0 || dy != 0) {
            if (immediate) {
                scrollBy(dx, dy);
            } else {
                smoothScrollBy(dx, dy);
            }
            postInvalidate();
            return true;
        }
        return false;
    }

    /**
     * 判断是垂直，还是横向.
     */
    private boolean isVertical() {
        LayoutManager manager = getLayoutManager();
        if (manager != null) {
            LinearLayoutManager layout = (LinearLayoutManager) getLayoutManager();
            return layout.getOrientation() == LinearLayoutManager.VERTICAL;

        }
        return false;
    }

    /**
     * 滚动的相关响应
     * computeScroll在父控件执行drawChild时，会调用这个方法
     */
    @Override
    public void computeScroll() {
        super.computeScroll();

        //滚动后更新当前选中的position
        if (mNextFocused != null) {
            mSelectedPosition = getChildAdapterPosition(mNextFocused);
        } else {
            mSelectedPosition = getChildAdapterPosition(getFocusedChild());
        }
    }

    /**
     * 返回迭代的绘制子类索引。如果你想改变子类的绘制顺序就要重写该方法
     * 提示：为了能够调用该方法，你必须首先调用setChildrenDrawingOrderEnabled(boolean)来允许子类排序
     *
     * @param childCount 子类个数
     * @param i          当前迭代顺序
     * @return 绘制该迭代子类的索引
     */
    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        View view = getFocusedChild();
        if (null != view) {

            int position = getChildAdapterPosition(view) - getFirstVisiblePosition();
            if (position < 0) {
                return i;
            } else {
                if (i == childCount - 1) {
                    if (position > i) {
                        position = i;
                    }
                    return position;
                }
                if (i == position) {
                    return childCount - 1;
                }
            }
        }
        return i;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean result = super.dispatchKeyEvent(event);
        View focusView = this.getFocusedChild();

        if (focusView == null) {
            return result;
        } else {
            if (event.getAction() == KeyEvent.ACTION_UP) {
                //不能拦截KeyEvent.KEYCODE_BACK
                //否则onBackPress不会触发
                if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                    return super.dispatchKeyEvent(event);
                } else {
                    return true;
                }
            } else {
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                        View rightView = mNextFocused = FocusFinder.getInstance().findNextFocus(this, focusView, View.FOCUS_RIGHT);
                        setViewPosition(mNextFocused);
                        if (rightView != null) {
                            rightView.requestFocus();
                            return true;
                        } else {
                            return false;
                        }
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        View leftView = mNextFocused = FocusFinder.getInstance().findNextFocus(this, focusView, View.FOCUS_LEFT);
                        setViewPosition(mNextFocused);
                        if (leftView != null) {
                            mSelectedPosition = getChildAdapterPosition(leftView);
                        } else {
                            mSelectedPosition = getChildAdapterPosition(getFocusedChild());
                        }
                        if (leftView != null) {
                            leftView.requestFocus();
                            return true;
                        } else {
                            return false;
                        }
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                        View downView = mNextFocused = FocusFinder.getInstance().findNextFocus(this, focusView, View.FOCUS_DOWN);
                        setViewPosition(mNextFocused);
                        if (downView != null) {

                            downView.requestFocus();
                            if (scrollModel == SCROLL_NORMAL) {
                                //跟随滚动直接返回true
                                return true;
                            } else {
                                //居中滚动计算出滚动距离，将view滚动到中间
                                int downOffset = downView.getTop() + downView.getHeight() / 2 - getHeight() / 2;
                                this.smoothScrollBy(0, downOffset);
                                return true;
                            }
                        } else {
                            return isBottomEdge(getLayoutManager().getPosition(this.getFocusedChild()));
                        }
                    case KeyEvent.KEYCODE_DPAD_UP:
                        View upView = mNextFocused = FocusFinder.getInstance().findNextFocus(this, focusView, View.FOCUS_UP);
                        setViewPosition(mNextFocused);
                        if (upView != null) {
                            upView.requestFocus();
                            if (scrollModel == SCROLL_NORMAL) {
                                return true;
                            } else {
                                int upOffset = getHeight() / 2 - (upView.getBottom() - upView.getHeight() / 2);
                                this.smoothScrollBy(0, -upOffset);
                                return true;
                            }
                        } else {
                            return isTopEdge(getLayoutManager().getPosition(this.getFocusedChild()));
                        }
                }
            }
        }
        return result;
    }

    private void setViewPosition(View mNextFocused) {
        if (mNextFocused != null) {
            mSelectedPosition = getChildAdapterPosition(mNextFocused);
        } else {
            mSelectedPosition = getChildAdapterPosition(getFocusedChild());
        }
    }

    //防止Activity时,RecyclerView崩溃
    @Override
    protected void onDetachedFromWindow() {
        if (getLayoutManager() != null) {
            super.onDetachedFromWindow();
        }
    }

    /**
     * 设置选中的item
     *
     * @param position
     */
    public void setItemSelected(int position) {
        if (mSelectedPosition == position) {
            return;
        }

        if (position >= getAdapter().getItemCount()) {
            position = getAdapter().getItemCount() - 1;
        }
        mSelectedPosition = position;
        requestLayout();
    }


    /**
     * 是否是最右边的item，如果是竖向，表示右边，如果是横向表示下边
     *
     * @param childPosition
     * @return
     */
    public boolean isRightEdge(int childPosition) {
        LayoutManager layoutManager = getLayoutManager();

        if (layoutManager instanceof GridLayoutManager) {

            GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            GridLayoutManager.SpanSizeLookup spanSizeLookUp = gridLayoutManager.getSpanSizeLookup();

            int totalSpanCount = gridLayoutManager.getSpanCount();
            int totalItemCount = gridLayoutManager.getItemCount();
            int childSpanCount = 0;

            for (int i = 0; i <= childPosition; i++) {
                childSpanCount += spanSizeLookUp.getSpanSize(i);
            }
            if (isVertical()) {
                if (childSpanCount % gridLayoutManager.getSpanCount() == 0) {
                    return true;
                }
            } else {
                int lastColumnSize = totalItemCount % totalSpanCount;
                if (lastColumnSize == 0) {
                    lastColumnSize = totalSpanCount;
                }
                if (childSpanCount > totalItemCount - lastColumnSize) {
                    return true;
                }
            }

        } else if (layoutManager instanceof LinearLayoutManager) {
            if (isVertical()) {
                return true;
            } else {
                return childPosition == getLayoutManager().getItemCount() - 1;
            }
        }

        return false;
    }

    /**
     * 是否是最左边的item，如果是竖向，表示左方，如果是横向，表示上边
     *
     * @param childPosition
     * @return
     */
    public boolean isLeftEdge(int childPosition) {
        LayoutManager layoutManager = getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            GridLayoutManager.SpanSizeLookup spanSizeLookUp = gridLayoutManager.getSpanSizeLookup();

            int totalSpanCount = gridLayoutManager.getSpanCount();
            int childSpanCount = 0;
            for (int i = 0; i <= childPosition; i++) {
                childSpanCount += spanSizeLookUp.getSpanSize(i);
            }
            if (isVertical()) {
                if (childSpanCount % gridLayoutManager.getSpanCount() == 1) {
                    return true;
                }
            } else {
                if (childSpanCount <= totalSpanCount) {
                    return true;
                }
            }

        } else if (layoutManager instanceof LinearLayoutManager) {
            if (isVertical()) {
                return true;
            } else {
                return childPosition == 0;
            }

        }

        return false;
    }

    /**
     * 是否是最上边的item，以recyclerview的方向做参考
     *
     * @param childPosition
     * @return
     */
    public boolean isTopEdge(int childPosition) {
        LayoutManager layoutManager = getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            GridLayoutManager.SpanSizeLookup spanSizeLookUp = gridLayoutManager.getSpanSizeLookup();

            int totalSpanCount = gridLayoutManager.getSpanCount();

            int childSpanCount = 0;
            for (int i = 0; i <= childPosition; i++) {
                childSpanCount += spanSizeLookUp.getSpanSize(i);
            }

            if (isVertical()) {
                if (childSpanCount <= totalSpanCount) {
                    return true;
                }
            } else {
                if (childSpanCount % totalSpanCount == 1) {
                    return true;
                }
            }


        } else if (layoutManager instanceof LinearLayoutManager) {
            if (isVertical()) {
                return childPosition == 0;
            } else {
                return true;
            }

        }

        return false;
    }

    /**
     * 是否是最下边的item，以recyclerview的方向做参考
     *
     * @param childPosition
     * @return
     */
    public boolean isBottomEdge(int childPosition) {
        LayoutManager layoutManager = getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            GridLayoutManager.SpanSizeLookup spanSizeLookUp = gridLayoutManager.getSpanSizeLookup();
            int itemCount = gridLayoutManager.getItemCount();
            int childSpanCount = 0;
            int totalSpanCount = gridLayoutManager.getSpanCount();
            for (int i = 0; i <= childPosition; i++) {
                childSpanCount += spanSizeLookUp.getSpanSize(i);
            }
            if (isVertical()) {
                //最后一行item的个数
                int lastRowCount = itemCount % totalSpanCount;
                if (lastRowCount == 0) {
                    lastRowCount = gridLayoutManager.getSpanCount();
                }
                if (childSpanCount > itemCount - lastRowCount) {
                    return true;
                }
            } else {
                if (childSpanCount % totalSpanCount == 0) {
                    return true;
                }
            }

        } else if (layoutManager instanceof LinearLayoutManager) {
            if (isVertical()) {
                return childPosition == getLayoutManager().getItemCount() - 1;
            } else {
                return true;
            }

        }
        return false;
    }

    /**
     * 判断是否已经滑动到底部
     *
     * @param recyclerView
     * @return
     */
    private boolean isVisBottom(RecyclerView recyclerView) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
        int visibleItemCount = layoutManager.getChildCount();
        int totalItemCount = layoutManager.getItemCount();
        if (visibleItemCount > 0 && lastVisibleItemPosition == totalItemCount - 1) {
            return true;
        } else {
            return false;
        }
    }

    public int getFirstVisiblePosition() {
        if (getChildCount() == 0)
            return 0;
        else
            return getChildAdapterPosition(getChildAt(0));
    }

    public int getLastVisiblePosition() {
        final int childCount = getChildCount();
        if (childCount == 0)
            return 0;
        else
            return getChildAdapterPosition(getChildAt(childCount - 1));
    }

    private int getFreeWidth() {
        return getWidth() - getPaddingLeft() - getPaddingRight();
    }

    private int getFreeHeight() {
        return getHeight() - getPaddingTop() - getPaddingBottom();
    }

    public int getSelectedPosition() {
        return mSelectedPosition;
    }

    public void setSelectionPostion(int selectionPostion) {
        mSelectedPosition = selectionPostion;
    }
}