package com.zee.launcher.home.widgets;

import android.animation.Keyframe;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.zeewain.base.utils.DisplayUtil;
import com.zee.launcher.home.R;

public class ScanningConstraintLayout extends ConstraintLayout implements View.OnFocusChangeListener{
    private static final long ANIMATOR_DURATION = 800L;
    private static final int DEFAULT_RADIUS_DP = 18;

    private static Bitmap lightBitmap;

    private Bitmap rounderBitmap;

    private Paint scanPaint;

    private Paint rounderPaint;

    private float offset;

    private float startOffset;

    private float endOffset;

    private ValueAnimator valueAnimator;

    private PorterDuffXfermode porterDuffXfermode;

    private final int lightImageResId;

    private final int rounderRadius;

    private boolean isFocused = false;

    public ScanningConstraintLayout(Context context) {
        this(context, null);
    }

    public ScanningConstraintLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScanningConstraintLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ScanningConstraintLayout);
        lightImageResId = typedArray.getResourceId(R.styleable.ScanningConstraintLayout_lightImage, R.mipmap.img_light);
        rounderRadius = typedArray.getDimensionPixelOffset(R.styleable.ScanningConstraintLayout_layoutRadius, DEFAULT_RADIUS_DP);
        typedArray.recycle();
        setFocusable(true);
        setOnFocusChangeListener(this);
        init();
    }

    private void init() {
        lightBitmap = BitmapFactory.decodeResource(getResources(), lightImageResId);
        offset = startOffset = -lightBitmap.getWidth();

        //初始化画笔 设置抗锯齿和防抖动
        scanPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        scanPaint.setDither(true);
        scanPaint.setFilterBitmap(true);//加快显示速度，本设置项依赖于dither和xfermode的设置

        rounderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rounderPaint.setDither(true);
        rounderPaint.setStyle(Paint.Style.FILL);
        rounderPaint.setColor(Color.WHITE);
        rounderPaint.setFilterBitmap(true);

        porterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.DST_IN);
    }

    @Override
    protected void onDetachedFromWindow() {
        if (valueAnimator != null && valueAnimator.isRunning()) {
            valueAnimator.cancel();
        }
        if(rounderBitmap != null) {
            rounderBitmap.recycle();
            rounderBitmap = null;
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        endOffset = w;
    }

    private void createRounderBitmap() {
        rounderBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ALPHA_8);
        Canvas canvas = new Canvas(rounderBitmap);
        canvas.drawRoundRect(new RectF(0, 0, getWidth(), getHeight()), DisplayUtil.dip2px(getContext(), rounderRadius), DisplayUtil.dip2px(getContext(), rounderRadius), rounderPaint);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        if(isFocused) {
            if(rounderBitmap == null){
                createRounderBitmap();
            }
            int sc = canvas.saveLayer(0, 0, getWidth(), getHeight(), scanPaint, Canvas.ALL_SAVE_FLAG);
            canvas.drawBitmap(lightBitmap, offset, 0, scanPaint);
            scanPaint.setXfermode(porterDuffXfermode);
            canvas.drawBitmap(rounderBitmap, 0, 0, scanPaint);
            scanPaint.setXfermode(null);
            canvas.restoreToCount(sc);
        }
    }

    private void initAnimator() {
        PropertyValuesHolder pvhLeft = PropertyValuesHolder.ofKeyframe(View.TRANSLATION_X,
                Keyframe.ofFloat(0f, startOffset),
                Keyframe.ofFloat(1f, endOffset)
        );

        valueAnimator = ValueAnimator.ofPropertyValuesHolder(pvhLeft);
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.setDuration(ANIMATOR_DURATION);
        valueAnimator.setStartDelay(200);
        valueAnimator.addUpdateListener(animation -> {
            offset = (float) animation.getAnimatedValue();
            postInvalidate();
        });
    }


    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (v != null) {
            isFocused = hasFocus;
            if (hasFocus) {
                start();
            } else {
                stop();
            }
        }
    }

    private void start() {
        post(() -> {
            if (valueAnimator == null) {
                initAnimator();
            } else if (valueAnimator.isRunning()) {
                valueAnimator.cancel();
            }
            valueAnimator.start();
        });
    }

    private void stop() {
        if (valueAnimator != null && valueAnimator.isRunning()) {
            valueAnimator.cancel();
        }
        offset = startOffset;
        if(rounderBitmap != null){
            rounderBitmap.recycle();
            rounderBitmap = null;
        }
        postInvalidate();
    }

    public void startAnimator(){
        isFocused = true;
        start();
    }

    public void stopAnimator(){
        isFocused = false;
        stop();
    }
}
