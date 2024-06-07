package com.zee.device.home.widgets;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class LinearDividerDecoration extends RecyclerView.ItemDecoration {
    private int dividerHeight;
    private final int orientation;
    private final Paint paint;

    private int paddingLeft = 0;
    private int paddingRight = 0;

    public LinearDividerDecoration(@RecyclerView.Orientation int orientation, int dividerHeight, int dividerColor) {
        this.orientation = orientation;
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(dividerColor);
        this.dividerHeight = dividerHeight;
    }

    public void setVerticalPadding(int paddingLeft, int paddingRight){
        this.paddingLeft = paddingLeft;
        this.paddingRight = paddingRight;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        if (orientation == LinearLayoutManager.VERTICAL) {
            outRect.set(0, 0, 0, dividerHeight);
        } else {
            outRect.set(0, 0, dividerHeight, 0);
        }
    }

    @Override
    public void onDraw(@NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        canvas.save();
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount -1; ++i) {
            View childAt = parent.getChildAt(i);
            int left = 0;
            int right = parent.getWidth();
            int top = childAt.getBottom() ;
            int bottom = childAt.getBottom() + dividerHeight;
            canvas.drawRect(left + paddingLeft, top, right - paddingRight, bottom, paint);
        }
        canvas.restore();
    }

}

