package com.zeewain.base.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;

import com.zeewain.base.R;
import com.zeewain.base.utils.FontUtils;

public class GradientTextView extends androidx.appcompat.widget.AppCompatTextView {

    public static final int DRAW_MODE_VERTICAL = 0;
    public static final int DRAW_MODE_HORIZONTAL = 1;

    @ColorInt
    private int mStartColor;
    @ColorInt
    private int mEndColor;

    private int mDrawMode;
    private boolean mHasDraw;

    private LinearGradient mLinearGradient;

    public GradientTextView(Context context) {
        super(context);
    }

    @SuppressLint("Recycle")
    public GradientTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.GradientTextView);
        mStartColor = typedArray.getColor(R.styleable.GradientTextView_startColor, 0xFF75D6FF);
        mEndColor = typedArray.getColor(R.styleable.GradientTextView_endColor, 0xFFBD62FF);
        mDrawMode = typedArray.getInt(R.styleable.GradientTextView_drawMode, DRAW_MODE_VERTICAL);
        mHasDraw = typedArray.getBoolean(R.styleable.GradientTextView_hasDraw, true);
    }

    public GradientTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setStartEndColor(int[] colors) {
        this.mStartColor = colors[0];
        this.mEndColor = colors[1];
    }

    public void setDrawMode(int drawMode) {
        this.mDrawMode = drawMode;
    }

    public void setHasDraw(boolean hasDraw) {
        this.mHasDraw = hasDraw;
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onLayout(boolean changed,
                            int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            if (mHasDraw) {
                switch (mDrawMode) {
                    case DRAW_MODE_VERTICAL:
                        mLinearGradient = new LinearGradient(
                                0, 0, 0, getMeasuredHeight(),
                                mStartColor, mEndColor, Shader.TileMode.CLAMP);
                        getPaint().setShader(mLinearGradient);
                        break;
                    case DRAW_MODE_HORIZONTAL:
                        mLinearGradient = new LinearGradient(
                                0, 0, getMeasuredWidth(), 0,
                                mStartColor, mEndColor, Shader.TileMode.CLAMP);
                        getPaint().setShader(mLinearGradient);
                        break;
                }
            }
            setTypeface(FontUtils.typeface);
        }
    }

    public LinearGradient getLinearGradient() {
       return new LinearGradient(
               0, 0, getMeasuredWidth(), 0,
               mStartColor, mEndColor, Shader.TileMode.CLAMP);
    }

    public LinearGradient getLinearGradient1() {
        return new LinearGradient(
                0, 0, getMeasuredWidth(), 0,
                0xFFFFFFFF, 0xFFFFFFFF, Shader.TileMode.CLAMP);
    }
}

