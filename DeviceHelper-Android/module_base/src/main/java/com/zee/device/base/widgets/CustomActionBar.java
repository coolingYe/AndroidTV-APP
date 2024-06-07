package com.zee.device.base.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.zee.device.base.R;


public class CustomActionBar extends ConstraintLayout {
    private ImageView imgBack, imgFilter;
    private TextView tvTitle;
    private TextView tvSubTitle;
    private FrameLayout flContent;
    private OnFilterImageClickListener onFilterImageClickListener;

    private static final int THEME_LIGHT = 0;
    private static final int THEME_DARK = 1;
    private int cTheme;


    public CustomActionBar(@NonNull Context context) {
        this(context, null);
    }

    public CustomActionBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomActionBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomActionBar);
        cTheme = typedArray.getInt(R.styleable.CustomActionBar_cTheme, THEME_LIGHT);
        String title = typedArray.getString(R.styleable.CustomActionBar_title);
        typedArray.recycle();
        initView(context, title);
        initListener();
    }

    private void initView(Context context, String title){
        LayoutInflater.from(context).inflate(R.layout.layout_custom_action_bar, this);
        imgBack = findViewById(R.id.img_action_bar_back);
        tvTitle = findViewById(R.id.tv_action_bar_title);
        tvSubTitle = findViewById(R.id.tv_action_bar_sub_title);
        flContent = findViewById(R.id.fl_action_bar_content);
        imgFilter = findViewById(R.id.img_action_bar_filter);

        if (cTheme == THEME_LIGHT){
            tvTitle.setTextColor(0xFF333333);
            imgBack.setImageResource(R.mipmap.ic_action_bar_back_dark);
        }else{
            tvTitle.setTextColor(0xFFFFFFFF);
            imgBack.setImageResource(R.mipmap.ic_action_bar_back_light);
        }

        if(title != null){
            tvTitle.setText(title);
        }
    }

    private void initListener(){
        imgBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(v.getContext() instanceof AppCompatActivity){
                    ((AppCompatActivity)v.getContext()).finish();
                }
            }
        });

        imgFilter.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(onFilterImageClickListener != null){
                    onFilterImageClickListener.onClickListener(v);
                }
            }
        });
    }

    public void setTitle(String title) {
        if(tvTitle != null) {
            tvTitle.setText(title);
        }
    }

    public void setSubTitle(String title) {
        if(tvSubTitle != null) {
            tvSubTitle.setVisibility(View.VISIBLE);
            tvSubTitle.setText(title);
        }
    }

    public void addContentView(View view){
        if(flContent != null){
            tvTitle.setVisibility(View.GONE);
            flContent.setVisibility(View.VISIBLE);
            flContent.addView(view);
        }
    }

    public void addContentView(View view, ViewGroup.LayoutParams params) {
        if(flContent != null){
            tvTitle.setVisibility(View.GONE);
            flContent.setVisibility(View.VISIBLE);
            flContent.addView(view, params);
        }
    }

    public void setFilterVisibility(int visibility) {
        imgFilter.setVisibility(visibility);
    }

    public void setFilterImage(@DrawableRes int resId) {
        imgFilter.setImageResource(resId);
    }

    public void setOnFilterImageClickListener(OnFilterImageClickListener onFilterImageClickListener) {
        this.onFilterImageClickListener = onFilterImageClickListener;
    }

    public interface OnFilterImageClickListener{
        void onClickListener(View view);
    }

}
