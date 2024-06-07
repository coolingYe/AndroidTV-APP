package com.zee.launcher.home.widgets;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.zee.launcher.home.R;
import com.zee.launcher.home.adapter.MainItemAdapter;

public class MainItemRecyclerView extends RecyclerView {
    public MainItemRecyclerView(@NonNull Context context) {
        super(context);
    }

    public MainItemRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MainItemRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        setFocusable(true);
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, @Nullable Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if(!gainFocus){
            if(getAdapter() != null){
                MainItemAdapter mainTabAdapter = ((MainItemAdapter)getAdapter());
                mainTabAdapter.setFocusedPosition(-1);
            }
        }else{
            if(getAdapter() != null){
                MainItemAdapter mainTabAdapter = ((MainItemAdapter)getAdapter());
                mainTabAdapter.setFocusedPosition(1);
            }
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(event.getAction() == KeyEvent.ACTION_DOWN && getLayoutManager() != null && getAdapter() != null) {
            int keyCode = event.getKeyCode();
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                MainItemAdapter mainTabAdapter = ((MainItemAdapter)getAdapter());
                int count =  getLayoutManager().getItemCount();
                int nextPosition = mainTabAdapter.getSelectedPosition() + 1;
                if(nextPosition > count -1){
                    return false;
                }
                mainTabAdapter.setFocusedPosition(mainTabAdapter.getSelectedPosition() + 1);
                return true;
            }else if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT){
                MainItemAdapter mainTabAdapter = ((MainItemAdapter)getAdapter());
                int nextPosition = mainTabAdapter.getSelectedPosition() - 1;
                if(nextPosition < 0){
                    return false;
                }
                mainTabAdapter.setFocusedPosition(mainTabAdapter.getSelectedPosition() - 1);
                return true;
            }else if(keyCode == KeyEvent.KEYCODE_DPAD_CENTER){
                MainItemAdapter mainTabAdapter = ((MainItemAdapter)getAdapter());
                mainTabAdapter.setClickedPosition(mainTabAdapter.getSelectedPosition());
            }else if(keyCode == KeyEvent.KEYCODE_DPAD_UP){
                MainItemAdapter mainTabAdapter = ((MainItemAdapter)getAdapter());
                /*if(mainTabAdapter.getSelectedPosition() == 0 || mainTabAdapter.getSelectedPosition() == 1 ){
                    setNextFocusUpId(R.id.cl_top_user);
                }else{
                    setNextFocusUpId(R.id.cl_top_settings);
                }*/
            }else if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
                return true;
            }
        }
        return false;
    }

    public void setAdapter(MainItemAdapter adapter) {
        super.setAdapter(adapter);
    }
}
