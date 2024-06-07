package com.zee.setting.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.zee.setting.R;


public class DashboardView extends View {

    private int mRadius; // 扇形半径
    private int mStartAngle = 150; // 起始角度
    private int mSweepAngle = 240; // 绘制角度
    private int mMin = 0; // 最小值
   private int mMax = 300; // 最大值
//    private int mMax = 10; // 最大值
    private int mSection = 10; // 值域（mMax-mMin）等分份数
    private int mPortion = 10; // 一个mSection等分份数
    private String mHeaderText = "Mbps"; // 表头
    private float mRealTimeValue = mMin; // 实时读数
    private boolean isShowValue = true; // 是否显示实时读数
    private int mStrokeWidth; // 画笔宽度
    private int mStrokeCircleWidth = 20; // 圆环画笔宽度
    private int mLength1; // 长刻度的相对圆弧的长度
    private int mLength2; // 刻度读数顶部的相对圆弧的长度
    private int mPLRadius; // 指针长半径
    private int mPSRadius; // 指针短半径

    private int mPadding;
    private float mCenterX, mCenterY; // 圆心坐标
    private Paint mPaint;
    private RectF mRectFArc;
    private Path mPath;
    private RectF mRectFInnerArc;
    private Rect mRectText;
    private String[] mTexts;
    private float ratio = 0.0f;

    public DashboardView(Context context) {
        this(context, null);
    }

    public DashboardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DashboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        mStrokeWidth = dp2px(1);
        mLength1 = dp2px(8) + mStrokeWidth;
        mLength2 = mLength1 + dp2px(2);
        mPSRadius = dp2px(10);


        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        mRectFArc = new RectF();
        mPath = new Path();
        mRectFInnerArc = new RectF();
        mRectText = new Rect();

        mTexts = new String[mSection + 1]; // 需要显示mSection + 1个刻度读数
        for (int i = 0; i < mTexts.length; i++) {
            int n = (mMax - mMin) / mSection;
            mTexts[i] = String.valueOf(mMin + i * n);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mPadding = Math.max(
                Math.max(getPaddingLeft(), getPaddingTop()),
                Math.max(getPaddingRight(), getPaddingBottom())
        );
        setPadding(mPadding, mPadding, mPadding, mPadding);

        int width = resolveSize(dp2px(200), widthMeasureSpec);
        mRadius = (width - mPadding * 2 - mStrokeWidth * 2) / 2;
        Log.i("ppphhh", "mPadding=" + mPadding);
        Log.i("ppphhh", "mStrokeWidth=" + mStrokeWidth);
        Log.i("ppphhh", "mRadius=" + mRadius);


        mPaint.setTextSize(sp2px(16));
        if (isShowValue) { // 显示实时读数，View高度增加字体高度3倍
            mPaint.getTextBounds("0", 0, "0".length(), mRectText);
        } else {
            mPaint.getTextBounds("0", 0, 0, mRectText);
        }
        // 由半径+指针短半径+实时读数文字高度确定的高度
        int height1 = mRadius + mStrokeWidth * 2 + mPSRadius + mRectText.height() * 3;
        // 由起始角度确定的高度
        float[] point1 = getCoordinatePoint(mRadius, mStartAngle);
        // 由结束角度确定的高度
        float[] point2 = getCoordinatePoint(mRadius, mStartAngle + mSweepAngle);
        // 取最大值
        int max = (int) Math.max(
                height1,
                Math.max(point1[1] + mRadius + mStrokeWidth * 2, point2[1] + mRadius + mStrokeWidth * 2)
        );
        setMeasuredDimension(width, max + getPaddingTop() + getPaddingBottom());


        mCenterX = mCenterY = getMeasuredWidth() / 2f;
        mRectFArc.set(
                getPaddingLeft() + mStrokeWidth,
                getPaddingTop() + mStrokeWidth,
                getMeasuredWidth() - getPaddingRight() - mStrokeWidth,
                getMeasuredWidth() - getPaddingBottom() - mStrokeWidth
        );

        mPaint.setTextSize(sp2px(10));
        mPaint.getTextBounds("0", 0, "0".length(), mRectText);
        mRectFInnerArc.set(
                getPaddingLeft() + mLength2 + mRectText.height(),
                getPaddingTop() + mLength2 + mRectText.height(),
                getMeasuredWidth() - getPaddingRight() - mLength2 - mRectText.height(),
                getMeasuredWidth() - getPaddingBottom() - mLength2 - mRectText.height()
        );

//          mPLRadius = mRadius - (mLength2 + mRectText.height() + dp2px(5));
           mPLRadius = mRadius - (mLength2 + mRectText.height() + dp2px(20));


    }

    public void setSelect(float ratio) {
        if (ratio>=1.0){
            this.ratio = 1.0f;
        }else {
            this.ratio = ratio;
        }

        this.invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        /**
         * 画圆弧
         */
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setColor(ContextCompat.getColor(getContext(), R.color.white));

        RectF rect0 = new RectF((mRectFArc.left - dp2px(5)), (mRectFArc.top - dp2px(5)), (mRectFArc.right + dp2px(5)), (mRectFArc.bottom + dp2px(5)));
        canvas.drawArc(rect0, mStartAngle, mSweepAngle, false, mPaint);
        //  canvas.drawArc(mRectFArc, mStartAngle, mSweepAngle, false, mPaint);

        Log.i("ppphhh", "left=" + mRectFArc.left + "--top" + mRectFArc.top
                + "---mRectFArc.right=" + mRectFArc.right + "---mRectFArc.bottom=" + mRectFArc.bottom);

        mPaint.setStrokeWidth(mStrokeCircleWidth);
       // mPaint.setColor(Color.GRAY);
        mPaint.setColor(Color.WHITE);//外层圆环
        RectF rect = new RectF((rect0.left - dp2px(10)), (rect0.top - dp2px(10)), (rect0.right + dp2px(10)), (rect0.bottom + dp2px(10)));
        canvas.drawArc(rect, mStartAngle, mSweepAngle, false, mPaint);
       // mPaint.setColor(Color.RED);
        mPaint.setColor(Color.BLUE);
        canvas.drawArc(rect, mStartAngle, mSweepAngle * ratio, false, mPaint);

        //恢复颜色
        mPaint.setStrokeWidth(mStrokeWidth);
    //    mPaint.setColor(ContextCompat.getColor(getContext(), R.color.src_644));


        /**
         * 画长刻度
         * 画好起始角度的一条刻度后通过canvas绕着原点旋转来画剩下的长刻度
         */
        double cos = Math.cos(Math.toRadians(mStartAngle - 180));
        double sin = Math.sin(Math.toRadians(mStartAngle - 180));
        float x0 = (float) (mPadding + mStrokeWidth + mRadius * (1 - cos));
        float y0 = (float) (mPadding + mStrokeWidth + mRadius * (1 - sin));
    /*    float x1 = (float) (mPadding + mStrokeWidth + mRadius - (mRadius - mLength1) * cos);
        float y1 = (float) (mPadding + mStrokeWidth + mRadius - (mRadius - mLength1) * sin);*/
        float x1 = (float) (mPadding + mStrokeWidth + mRadius - (mRadius - mLength1 / 0.8f) * cos);
        float y1 = (float) (mPadding + mStrokeWidth + mRadius - (mRadius - mLength1 / 0.8f) * sin);

        canvas.save();
        //修改-------------------
        if (ratio>0){
            mPaint.setColor(Color.BLUE);
        }else {
            mPaint.setColor(Color.WHITE);
        }
        canvas.drawLine(x0, y0, x1, y1, mPaint);
        float angle = mSweepAngle * 1f / mSection;
        for (int i = 0; i < mSection; i++) {
            canvas.rotate(angle, mCenterX, mCenterY);
            //修改-------------------
           // if (i<5){
            if ((i+1)<=(ratio*10)){
                mPaint.setColor(Color.BLUE);
            }else {
                mPaint.setColor(Color.WHITE);
            }
            canvas.drawLine(x0, y0, x1, y1, mPaint);
        }
        canvas.restore();
        //恢复
        mPaint.setColor(ContextCompat.getColor(getContext(), R.color.white));

        /**
         * 画短刻度
         * 同样采用canvas的旋转原理
         */
        canvas.save();
        mPaint.setStrokeWidth(1);
      /*  float x2 = (float) (mPadding + mStrokeWidth + mRadius - (mRadius - mLength1 / 2f) * cos);
        float y2 = (float) (mPadding + mStrokeWidth + mRadius - (mRadius - mLength1 / 2f) * sin);*/
        float x2 = (float) (mPadding + mStrokeWidth + mRadius - (mRadius - mLength1 / 0.8f) * cos);
        float y2 = (float) (mPadding + mStrokeWidth + mRadius - (mRadius - mLength1 / 0.8f) * sin);
        //修改-------------------
        if (ratio>0){
            mPaint.setColor(Color.BLUE);
        }else {
            mPaint.setColor(Color.WHITE);
        }
        canvas.drawLine(x0, y0, x2, y2, mPaint);
        angle = mSweepAngle * 1f / (mSection * mPortion);
        for (int i = 1; i < mSection * mPortion; i++) {
            canvas.rotate(angle, mCenterX, mCenterY);
            if (i % mPortion == 0) { // 避免与长刻度画重合
                continue;
            }
            //修改----------
            if ((i)<=(ratio*10* mPortion)){
                mPaint.setColor(Color.BLUE);
            }else {
                mPaint.setColor(Color.WHITE);
            }
            canvas.drawLine(x0, y0, x2, y2, mPaint);
        }
        canvas.restore();
        //恢复
        mPaint.setColor(ContextCompat.getColor(getContext(), R.color.src_644));

        /**
         * 画长刻度读数
         * 添加一个圆弧path，文字沿着path绘制
         */
        mPaint.setTextSize(sp2px(10));
        mPaint.setTextAlign(Paint.Align.LEFT);
        mPaint.setStyle(Paint.Style.FILL);
//        Log.i("ssshhh","mTexts.length="+mTexts.length);
        for (int i = 0; i < mTexts.length; i++) {
            mPaint.getTextBounds(mTexts[i], 0, mTexts[i].length(), mRectText);
            // 粗略把文字的宽度视为圆心角2*θ对应的弧长，利用弧长公式得到θ，下面用于修正角度
            float θ = (float) (180 * mRectText.width() / 2 /
                    (Math.PI * (mRadius - mLength2 - mRectText.height())));

            mPath.reset();
            mPath.addArc(
                    mRectFInnerArc,
                    mStartAngle + i * (mSweepAngle / mSection) - θ, // 正起始角度减去θ使文字居中对准长刻度
                    mSweepAngle
            );
           //canvas.drawTextOnPath(mTexts[i], mPath, 0, 0, mPaint);

        }


        /**
         * 画表头
         * 没有表头就不画
         */
        if (!TextUtils.isEmpty(mHeaderText)) {
            mPaint.setTextSize(sp2px(14));
            mPaint.setTextAlign(Paint.Align.CENTER);
            mPaint.getTextBounds(mHeaderText, 0, mHeaderText.length(), mRectText);
          //  canvas.drawText(mHeaderText, mCenterX, mCenterY / 2f + mRectText.height(), mPaint);
        }

        /**
         * 画指针
         */
        float θ = mStartAngle + mSweepAngle * (mRealTimeValue - mMin) / (mMax - mMin); // 指针与水平线夹角
       // int d = dp2px(5); // 指针由两个等腰三角形构成，d为共底边长的一半
        int d = dp2px(8); // 指针由两个等腰三角形构成，d为共底边长的一半
        mPath.reset();
        float[] p1 = getCoordinatePoint(d, θ - 90);
        mPath.moveTo(p1[0], p1[1]);
        float[] p2 = getCoordinatePoint(mPLRadius, θ);
        mPath.lineTo(p2[0], p2[1]);
        float[] p3 = getCoordinatePoint(d, θ + 90);
        mPath.lineTo(p3[0], p3[1]);
        float[] p4 = getCoordinatePoint(mPSRadius, θ - 180);
        //mPath.lineTo(p4[0], p4[1]);
        mPath.close();
        canvas.drawPath(mPath, mPaint);


        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPath.moveTo(p3[0], p3[1]);
        mPath.quadTo(p4[0], p4[1], p1[0], p1[1]);
        canvas.drawPath(mPath, mPaint);


        /**
         * 画指针围绕的镂空圆心
         */
        // mPaint.setColor(Color.WHITE);
     /*   mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeCap(Paint.Cap.ROUND);*/
        canvas.drawCircle(mCenterX, mCenterY, dp2px(2), mPaint);
        //Log.i("ppphhh", "mCenterX=" + mCenterX + "--mCenterY" + mCenterY);
        mPaint.setStyle(Paint.Style.STROKE);

        /**
         * 画实时度数值
         */
        if (isShowValue) {
            mPaint.setTextSize(sp2px(16));
            mPaint.setTextAlign(Paint.Align.CENTER);
            mPaint.setColor(ContextCompat.getColor(getContext(), R.color.src_644));
            String value = String.valueOf(mRealTimeValue);
            mPaint.getTextBounds(value, 0, value.length(), mRectText);
           // canvas.drawText(value, mCenterX, mCenterY + mPSRadius + mRectText.height() * 2, mPaint);
            mPaint.setColor(Color.parseColor("#333333"));
            mPaint.setTextSize(sp2px(26));
            mPaint.setTypeface(Typeface.createFromAsset(getResources().getAssets(),"fonts/SOURCEHANSANSCN-EXTRALIGHT.OTF"));
            //mPaint.setFakeBoldText(true);

          /*  Log.i("ssshhh","height="+ mRectText.height());
            Log.i("ssshhh","mCenterY="+ mCenterY);
            Log.i("ssshhh","mPSRadius="+ mPSRadius);
            Log.i("ssshhh","dp2px(10)="+ dp2px(10));*/
          //  canvas.drawText(value, mCenterX, mCenterY + mPSRadius + mRectText.height() * 2-dp2px(10), mPaint);

            canvas.drawText(value, mCenterX, mCenterY + mPSRadius + 12 * 2-dp2px(10)+100, mPaint);
            mPaint.setTextSize(sp2px(18));
            canvas.drawText(mHeaderText, mCenterX, mCenterY + mPSRadius + 12 * 3+100 , mPaint);
            //恢复
            mPaint.setTextSize(sp2px(16));
            mPaint.setColor(ContextCompat.getColor(getContext(), R.color.src_644));
        }
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                Resources.getSystem().getDisplayMetrics());
    }

    private int sp2px(int sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp,
                Resources.getSystem().getDisplayMetrics());
    }



    public float[] getCoordinatePoint(int radius, float angle) {
        float[] point = new float[2];

        double arcAngle = Math.toRadians(angle); //将角度转换为弧度
        if (angle < 90) {
            point[0] = (float) (mCenterX + Math.cos(arcAngle) * radius);
            point[1] = (float) (mCenterY + Math.sin(arcAngle) * radius);
        } else if (angle == 90) {
            point[0] = mCenterX;
            point[1] = mCenterY + radius;
        } else if (angle > 90 && angle < 180) {
            arcAngle = Math.PI * (180 - angle) / 180.0;
            point[0] = (float) (mCenterX - Math.cos(arcAngle) * radius);
            point[1] = (float) (mCenterY + Math.sin(arcAngle) * radius);
        } else if (angle == 180) {
            point[0] = mCenterX - radius;
            point[1] = mCenterY;
        } else if (angle > 180 && angle < 270) {
            arcAngle = Math.PI * (angle - 180) / 180.0;
            point[0] = (float) (mCenterX - Math.cos(arcAngle) * radius);
            point[1] = (float) (mCenterY - Math.sin(arcAngle) * radius);
        } else if (angle == 270) {
            point[0] = mCenterX;
            point[1] = mCenterY - radius;
        } else {
            arcAngle = Math.PI * (360 - angle) / 180.0;
            point[0] = (float) (mCenterX + Math.cos(arcAngle) * radius);
            point[1] = (float) (mCenterY - Math.sin(arcAngle) * radius);
        }

        return point;
    }

    public float getRealTimeValue() {
        return mRealTimeValue;
    }

    public void setRealTimeValue(float realTimeValue) {
        if (mRealTimeValue == realTimeValue || realTimeValue < mMin || realTimeValue > mMax) {
            return;
        }
        mRealTimeValue = realTimeValue;
        postInvalidate();
    }
}
