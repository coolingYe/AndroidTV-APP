package com.zee.launcher.home.ui.home;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class VerticalItemDecoration extends RecyclerView.ItemDecoration{

    private final int span;
    private final int firstSpan;
    private final int secSpan;

    public VerticalItemDecoration(int span, int firstSpan, int secSpan) {
        this.span = span;
        this.firstSpan = firstSpan;
        this.secSpan = secSpan;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int position = parent.getChildAdapterPosition(view);
        if(position == 0){
            outRect.top = firstSpan;
        } else if(position == 1){
            outRect.top = secSpan;
        } else{
            outRect.top = span;
        }

    }
}
