package com.zee.setting.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.zee.setting.R;
import com.zee.setting.utils.DensityUtils;

public class CustomPopupWindow extends PopupWindow implements View.OnClickListener {

    private View mPopView;
    private OnItemClickListener mListener;
    private static final int FULL_SCREEN_FLAG =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;

    public CustomPopupWindow(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        init(context);
        setPopupWindow();


    }

    /**
     * 初始化
     *
     * @param context
     */
    private void init(Context context) {
        // TODO Auto-generated method stub
        LayoutInflater inflater = LayoutInflater.from(context);
        //绑定布局
//        mPopView = inflater.inflate(R.layout.dialog_gesture_show, null);
        mPopView = inflater.inflate(R.layout.dialog_gesture_show_new, null);


    }

    /**
     * 设置窗口的相关属性
     */
    @SuppressLint("InlinedApi")
    private void setPopupWindow() {
        this.setContentView(mPopView);// 设置View
        this.setBackgroundDrawable(getContentView().getResources().getDrawable(R.mipmap.ic_gesture_rect_grey));
        this.setWidth(WindowManager.LayoutParams.MATCH_PARENT);// 设置弹出窗口的宽
        this.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);// 设置弹出窗口的高
        this.setFocusable(false);


    }



    public void showPopMenu(View view) {
        dismissPopupWindow();
        this.showAsDropDown(view);
        this.getContentView().setSystemUiVisibility(FULL_SCREEN_FLAG);
        this.setFocusable(true);
        this.setOutsideTouchable(true);
        this.update();


    }

    public void showPopMenu() {
        dismissPopupWindow();
        this.showAtLocation(mPopView.getRootView(),Gravity.TOP|Gravity.CENTER_HORIZONTAL,0,0);
        this.getContentView().setSystemUiVisibility(FULL_SCREEN_FLAG);
        this.setFocusable(true);
        this.setOutsideTouchable(true);
        this.update();


    }

    public void dismissPopupWindow() {
        if (this != null && this.isShowing()) {
            this.dismiss();
        }
    }

    /**
     * 定义一个接口，公布出去 在Activity中操作按钮的单击事件
     */
    public interface OnItemClickListener {
        void setOnItemClick(View v);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (mListener != null) {
            mListener.setOnItemClick(v);
        }
    }

}
