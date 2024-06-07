package com.zwn.user.ui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class UserCenterCategoryItemDecoration extends RecyclerView.ItemDecoration{

    private final int span;
    private final int specialSpan;
    private final Paint linePaint;

    public UserCenterCategoryItemDecoration(int span, int specialSpan) {
        this.span = span;
        this.specialSpan = specialSpan;

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setColor(0x19999999);
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            if(i == 4) {
                View view = parent.getChildAt(i);
                int left = parent.getPaddingLeft();
                int right = parent.getWidth() - parent.getPaddingRight();
                c.drawRect(left, view.getTop() - 1, right, view.getTop(), linePaint);
            }
        }
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int position = parent.getChildAdapterPosition(view);
        if(position == 4){
            outRect.top = specialSpan;
        }else{
            outRect.top = span;
        }

    }
}
