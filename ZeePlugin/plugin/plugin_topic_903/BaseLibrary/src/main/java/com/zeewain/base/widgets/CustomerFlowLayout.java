package com.zeewain.base.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.zeewain.base.R;
import com.zeewain.base.utils.DisplayUtil;

import java.util.ArrayList;
import java.util.List;

public class CustomerFlowLayout extends ViewGroup {
    private final List<Rect> mChildRect = new ArrayList<>();
    private int textPaddingStart;
    private int textPaddingTop;

    public CustomerFlowLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public CustomerFlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        @SuppressLint("CustomViewStyleable") TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomerFlowView);
        textPaddingStart = (int)typedArray.getDimension(R.styleable.CustomerFlowView_textPaddingStart, DisplayUtil.dip2px(context, 3));
        textPaddingTop = (int)typedArray.getDimension(R.styleable.CustomerFlowView_textPaddingTop, DisplayUtil.dip2px(context, 3));
        typedArray.recycle();
    }

    public CustomerFlowLayout(Context context) {
        super(context);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mChildRect.clear();
        int layoutWidth = MeasureSpec.getSize(widthMeasureSpec);
        int layoutHeight = 0;
        int childCount = getChildCount();
        int childLeft = getPaddingLeft();
        int childTop = getPaddingTop();
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            childView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
            int childWidth = childView.getMeasuredWidth();
            int childHeight = childView.getMeasuredHeight();
            if (childLeft + childWidth > layoutWidth) {
                childLeft = 0;
                childTop = childTop + childHeight + textPaddingTop;
            }
            mChildRect.add(new Rect(childLeft, childTop, childLeft + childWidth, childTop + childHeight));
            childLeft += childWidth + textPaddingStart;
            if (i == childCount - 1) {
                layoutHeight = childTop + childHeight;
            }
        }
        setMeasuredDimension(layoutWidth, layoutHeight);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childView = getChildAt(i);
            Rect location = mChildRect.get(i);
            childView.layout(location.left, location.top, location.right, location.bottom);
        }
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    private OnTagItemClickListener onTagItemClickListener;

    public interface OnTagItemClickListener {
        void onClick(View v);
    }

    public void setOnTagItemClickListener(OnTagItemClickListener onTagItemClickListener) {
        this.onTagItemClickListener = onTagItemClickListener;
    }


    public static class Rect {
        public int left;
        public int top;
        public int right;
        public int bottom;

        public Rect(int left, int top, int right, int bottom) {
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }
    }

}


