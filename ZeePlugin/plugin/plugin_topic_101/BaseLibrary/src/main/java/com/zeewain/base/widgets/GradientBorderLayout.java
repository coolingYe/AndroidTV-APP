package com.zeewain.base.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;

import androidx.annotation.ColorInt;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.zeewain.base.R;

/**
 * 1. 无法直接在xml的Design中使用ConstraintLayout的各种连线对齐，需要在xml的代码中赋予相应的属性值
 */
public class GradientBorderLayout extends ConstraintLayout {
    private static final String TAG = "GradientBorderLayout";

    public static final int BORDER_DRAW_MODE_HORIZONTAL_LINE = 0;
    public static final int BORDER_DRAW_MODE_VERTICAL_LINE = 1;
    public static final int BORDER_DRAW_MODE_SLASH = 2;
    public static final int BORDER_DRAW_MODE_BACKSLASH = 3;

    private final Paint mBorderPaint = new Paint();

    private float mBorderSize;
    private @ColorInt final int mStartColor;
    private @ColorInt final int mEndColor;
    private final int mDrawMode;
    private final float mCornersRadius;

    private int mWidth;
    private int mHeight;

    public GradientBorderLayout(Context context) {
        this(context, null);
    }

    public GradientBorderLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GradientBorderLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GradientBorderLayout);

        mBorderSize = a.getDimension(R.styleable.GradientBorderLayout_xBorderWidth, 1.5f);
        if (mBorderSize < 1.5f) {
            mBorderSize = 1.5f;
        }
        mStartColor = a.getColor(R.styleable.GradientBorderLayout_xStartColor, Color.TRANSPARENT);
        mEndColor = a.getColor(R.styleable.GradientBorderLayout_xEndColor, Color.TRANSPARENT);
        mDrawMode = a.getInt(R.styleable.GradientBorderLayout_xDrawMode, BORDER_DRAW_MODE_HORIZONTAL_LINE);
        mCornersRadius = a.getDimension(R.styleable.GradientBorderLayout_xCornersRadius, 0);
        
        a.recycle();
    }

    @Override
    @SuppressLint("DrawAllocation")
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        if (mHeight == 0 || mWidth == 0) {
            mWidth = getWidth();
            mHeight = getHeight();
        }

        drawBorder(canvas);
    }

    /**
     * 绘制边框
     * @param canvas 画布
     */
    private void drawBorder(Canvas canvas) {
        switch (mDrawMode) {
            case BORDER_DRAW_MODE_HORIZONTAL_LINE:
                mBorderPaint.setShader(new LinearGradient(0, 0, mWidth, 0, mStartColor, mEndColor, Shader.TileMode.MIRROR));
                break;
            case BORDER_DRAW_MODE_VERTICAL_LINE:
                mBorderPaint.setShader(new LinearGradient(0, 0, 0, mHeight, mStartColor, mEndColor, Shader.TileMode.MIRROR));
                break;
            case BORDER_DRAW_MODE_SLASH:
                mBorderPaint.setShader(new LinearGradient(0, mHeight, mWidth, 0, mStartColor, mEndColor, Shader.TileMode.MIRROR));
                break;
            case BORDER_DRAW_MODE_BACKSLASH:
                mBorderPaint.setShader(new LinearGradient(0, 0, mWidth, mHeight, mStartColor, mEndColor, Shader.TileMode.MIRROR));
                break;
        }
        float param = mBorderSize / 2;
        mBorderPaint.setStrokeWidth(mBorderSize);
        mBorderPaint.setAntiAlias(true);
        // https://stackoverflow.com/questions/21747186/rounded-edges-are-not-smoothusing-drawroundrectangle-in-android
        // mBorderPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        mBorderPaint.setStyle(Paint.Style.STROKE);
        canvas.drawRoundRect(param, param, mWidth - param, mHeight - param, mCornersRadius, mCornersRadius, mBorderPaint);
    }
}
