package com.zee.launcher.home.ui.home;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class HorizontalItemDecoration extends RecyclerView.ItemDecoration{

    private final int span;
    private final int firstSpan;

    public HorizontalItemDecoration(int span, int firstSpan) {
        this.span = span;
        this.firstSpan = firstSpan;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int position = parent.getChildAdapterPosition(view);
        if(position == 0){
            outRect.left = firstSpan;
        }else{
            outRect.left = span;
        }

    }
}
