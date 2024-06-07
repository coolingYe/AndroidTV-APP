package com.zee.launcher.home.widgets;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.zee.launcher.home.adapter.MainTabAdapter;

public class MainTabRecyclerView extends RecyclerView {
    private int lastFocusPosition = 0;
    public MainTabRecyclerView(@NonNull Context context) {
        super(context);
    }

    public MainTabRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MainTabRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        setFocusable(true);
    }

    /*@Override
    public void requestChildFocus(View child, View focused) {
        super.requestChildFocus(child, focused);
        if(null != child){
            lastFocusPosition = getChildViewHolder(child).getAdapterPosition();
        }
    }*/

    @Override
    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        /*if(getLayoutManager() != null){
            View lastFocusedView = getLayoutManager().findViewByPosition(lastFocusPosition);
            if(lastFocusedView != null) {
                lastFocusedView.requestFocus();
                return false;
            }
        }*/
        return super.requestFocus(direction, previouslyFocusedRect);
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, @Nullable Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if(!gainFocus){
            if(getAdapter() != null){
                getAdapter().notifyDataSetChanged();
            }
        }else{
            if(getAdapter() != null){
                getAdapter().notifyDataSetChanged();
            }
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(event.getAction() == KeyEvent.ACTION_DOWN && getLayoutManager() != null && getAdapter() != null) {
            int keyCode = event.getKeyCode();
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                MainTabAdapter mainTabAdapter = ((MainTabAdapter)getAdapter());
                int count =  getLayoutManager().getItemCount();
                int nextPosition = mainTabAdapter.getSelectedPosition() + 1;
                if(nextPosition > count -1){
                    return false;
                }
                mainTabAdapter.setFocusedPosition(mainTabAdapter.getSelectedPosition() + 1);
                return true;
            }else if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT){
                MainTabAdapter mainTabAdapter = ((MainTabAdapter)getAdapter());
                int nextPosition = mainTabAdapter.getSelectedPosition() - 1;
                if(nextPosition < 0){
                    return false;
                }
                mainTabAdapter.setFocusedPosition(mainTabAdapter.getSelectedPosition() - 1);
                return true;
            }else if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN){

                //nextFocusView = FocusFinder.getInstance().findNextFocus(recyclerView, focusedView, View.FOCUS_RIGHT);
            }
        }
        return false;
    }

    public void setAdapter(MainTabAdapter adapter) {
        super.setAdapter(adapter);
    }
}
