package com.zwn.user.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.zwn.user.adapter.UserCenterCategoryAdapter;

public class UserCenterRecyclerView extends RecyclerView {
    public UserCenterRecyclerView(@NonNull Context context) {
        super(context);
    }

    public UserCenterRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public UserCenterRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        setFocusable(true);
    }

    @Override
    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
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
            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                UserCenterCategoryAdapter userCenterAdapter = ((UserCenterCategoryAdapter)getAdapter());
                int count =  getLayoutManager().getItemCount();
                int nextPosition = userCenterAdapter.getSelectedPosition() + 1;
                if(nextPosition > count -1){
                    return false;
                }
                userCenterAdapter.setFocusedPosition(userCenterAdapter.getSelectedPosition() + 1);
                return true;
            }else if(keyCode == KeyEvent.KEYCODE_DPAD_UP){
                UserCenterCategoryAdapter userCenterAdapter = ((UserCenterCategoryAdapter)getAdapter());
                int nextPosition = userCenterAdapter.getSelectedPosition() - 1;
                if(nextPosition < 0){
                    return false;
                }
                userCenterAdapter.setFocusedPosition(userCenterAdapter.getSelectedPosition() - 1);
                return true;
            }
        }
        return false;
    }

    public void setAdapter(UserCenterCategoryAdapter adapter) {
        super.setAdapter(adapter);
    }
}
