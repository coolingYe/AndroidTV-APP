package com.zee.launcher.home.gesture.zeewainpose;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.zee.launcher.home.gesture.utils.LocationEnum;
import com.zeewain.zeepose.FaceInfo;
import com.zeewain.zeepose.HandInfo;
import com.zeewain.zeepose.HolisticInfo;
import com.zeewain.zeepose.PoseInfo;
import com.zeewain.zeepose.base.Point2f;
import com.zeewain.zeepose.base.Point3f;

import java.util.ArrayList;

public class Overlay extends View {

    private static final String TAG = "zeepose";
    private static final int HANDPOINTNUM = 21;
    private static final int POSEPOINTNUM1 = 17;
    private static final int POSEPOINTNUM2 = 29;
    private static final int POSEPOINTNUM3 = 33;

    private FaceInfo[] faceInfos = null;
    private HandInfo[] handInfos = null;
    private PoseInfo[] poseInfos = null;
    private HolisticInfo holisticInfo = null;

    private ArrayList<Point3f> handPoint;
    private int[][] handLineIndex = new int[][]{{0, 1}, {1, 2}, {2, 3}, {3, 4}, {0, 5},
            {5, 6}, {6, 7}, {7, 8}, {5, 9}, {9, 10}, {10, 11}, {11, 12}, {9, 13}, {13, 14},
            {14, 15}, {15, 16}, {13, 17}, {0, 17}, {17, 18}, {18, 19}, {19, 20}};

    private ArrayList<Point3f> posePoint;
    private int[][] pose17Index = new int[][] {{0, 2}, {2, 4}, {0, 1}, {1, 3},
            {5, 6}, {5, 7}, {7, 9},{6, 8}, {8, 10},
            {6, 12}, {5, 11},
            {11, 12}, {11, 13}, {13, 15}, {12, 14}, {14, 16}
            };

    private int[][] pose29Index = new int[][] {{0, 2}, {2, 4}, {0, 1}, {1, 3},
            {5, 6}, {5, 7}, {7, 9},{9, 15}, {9, 13}, {9, 11},
            {6, 8}, {8, 10}, {10, 12}, {10, 14}, {10, 16},
            {17, 18},
            {6, 18}, {18, 20}, {20, 22},
            {5, 17}, {17, 19}, {19, 21},
            {22, 24}, {24, 26}, {22, 28},
            {21, 23}, {21, 25}, {21, 27}
    };

    private int[][] pose33Index = new int[][] {{0, 1}, {1, 2}, {2, 3}, {3, 7},
            {0, 4}, {4, 5}, {5, 6}, {6, 8},
            {9, 10},
            {11, 13}, {13, 15}, {15, 21}, {15, 17}, {15, 19}, {17, 19},
            {12, 14}, {14, 16}, {16, 22}, {16, 18}, {16, 20}, {20, 22},
            {11, 23}, {23, 25}, {25, 27}, {27, 29}, {27, 31}, {29, 31},
            {12, 24}, {24, 26}, {26, 28}, {28, 30}, {28, 32}, {30, 32},
            {11, 12}, {23, 24}
    };

    private LocationEnum location = null;
    private float scale = 0f;

    private final Paint circlePaint = new Paint();
    private final Paint rectPaint = new Paint();
    private final Paint linePaint = new Paint();

    public Overlay(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void initOverlay(LocationEnum locationEnum) {
        location = locationEnum;

        circlePaint.setAntiAlias(true);
        circlePaint.setColor(Color.GREEN);
        circlePaint.setStrokeWidth(10.0f);
        circlePaint.setStyle(Paint.Style.STROKE);

        rectPaint.setColor(Color.GREEN);
        rectPaint.setStrokeWidth(4.0f);
        rectPaint.setStyle(Paint.Style.STROKE);

        linePaint.setColor(Color.BLUE);
        linePaint.setStrokeWidth(4.0f);
        linePaint.setStyle(Paint.Style.FILL);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        @SuppressLint("DrawAllocation") Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);

        @SuppressLint("DrawAllocation") Paint textpaint = new Paint();
        textpaint.setColor(Color.BLUE);
        textpaint.setTextSize(32);
        textpaint.setTextAlign(Paint.Align.LEFT);

//        long startTime = System.currentTimeMillis();
        switch (location) {
            case FACE:
                if (faceInfos != null) {
                    for (FaceInfo faceInfo : faceInfos) {
                        if (faceInfo.landmarks != null){
                            Point2f[] landmarks = faceInfo.landmarks;
                            for (Point2f landmark : landmarks) {
                                float x = landmark.x * scale;
                                float y = landmark.y * scale;
                                canvas.drawCircle(x, y, 2, circlePaint);
                            }
                        }

                        float x1 = faceInfo.rect.x * scale;
                        float y1 = faceInfo.rect.y * scale;
                        float x2 = x1 + faceInfo.rect.width * scale;
                        float y2 = y1 + faceInfo.rect.height * scale;
                        canvas.drawRect(new Rect((int) x1, (int) y1, (int) x2, (int) y2), rectPaint);
                    }
                }
                break;
            case HAND:
                if (handInfos != null) {
                    for (HandInfo handInfo : handInfos) {
                        if (handPoint != null) {
                            handPoint.clear();
                        }

                        handPoint = new ArrayList<>();
                        Point2f[] landmarks = handInfo.landmarks;
                        if (landmarks != null)   //由于初始化可选择不进行手部关键点检测，因此需要校验该landmarks
                        {
                            for (Point2f landmark : landmarks) {
                                float x = landmark.x * scale;
                                float y = landmark.y * scale;
                                canvas.drawCircle(x, y, 2, circlePaint);
                                handPoint.add(new Point3f(x, y, 0));
                            }
                        }
                        if (handPoint.size() == HANDPOINTNUM) {
                            for (int[] index : handLineIndex) {
                                canvas.drawLine(handPoint.get(index[0]).x, handPoint.get(index[0]).y,
                                        handPoint.get(index[1]).x, handPoint.get(index[1]).y, linePaint);
                            }
                        }
                        float x1 = handInfo.rect.x * scale;
                        float y1 = handInfo.rect.y * scale;
                        float x2 = x1 + handInfo.rect.width * scale;
                        float y2 = y1 + handInfo.rect.height * scale;
                        canvas.drawRect(new Rect((int) x1, (int) y1, (int) x2, (int) y2), rectPaint);
                    }
                }
                break;
            case SINGLEPOSE:
                if (poseInfos != null) {
                    for (PoseInfo poseInfo : poseInfos) {
                        if (posePoint != null) {
                            posePoint.clear();
                        }
                        posePoint = new ArrayList<>();
                        Point2f[] landmarks = poseInfo.landmarks;
                        for (Point2f landmark : landmarks) {
                            float x = landmark.x * scale;
                            float y = landmark.y * scale;
                            canvas.drawCircle(x, y, 2, circlePaint);
                            posePoint.add(new Point3f(x, y, 0));
                        }

                        linePaint.setColor(Color.GREEN);
                        if (posePoint.size() == POSEPOINTNUM1)
                        {
                            for (int[] index : pose17Index) {
                                canvas.drawLine(posePoint.get(index[0]).x, posePoint.get(index[0]).y,
                                        posePoint.get(index[1]).x, posePoint.get(index[1]).y, linePaint);
                            }
                        }else if(posePoint.size() == POSEPOINTNUM2)
                        {
                            for (int[] index : pose29Index) {
                                canvas.drawLine(posePoint.get(index[0]).x, posePoint.get(index[0]).y,
                                        posePoint.get(index[1]).x, posePoint.get(index[1]).y, linePaint);
                            }
                        }else if(posePoint.size() == POSEPOINTNUM3)
                        {
                            for (int[] index : pose33Index) {
                                canvas.drawLine(posePoint.get(index[0]).x, posePoint.get(index[0]).y,
                                        posePoint.get(index[1]).x, posePoint.get(index[1]).y, linePaint);
                            }
                        }
                    }
                }
                break;
            case DOUBLEPOSE:
                ;
            case PERSONPOSE:
                if (poseInfos != null) {
                    for (PoseInfo poseInfo : poseInfos) {
                        if (posePoint != null) {
                            posePoint.clear();
                        }
                        posePoint = new ArrayList<>();
                        Point2f[] landmarks = poseInfo.landmarks;
                        for (Point2f landmark : landmarks) {
                            float x = landmark.x * scale;
                            float y = landmark.y * scale;
                            canvas.drawCircle(x, y, 2, circlePaint);
                            posePoint.add(new Point3f(x, y, 0));
                        }

                        linePaint.setColor(Color.GREEN);
                        if (posePoint.size() == POSEPOINTNUM1)
                        {
                            for (int[] index : pose17Index) {
                                canvas.drawLine(posePoint.get(index[0]).x, posePoint.get(index[0]).y,
                                        posePoint.get(index[1]).x, posePoint.get(index[1]).y, linePaint);
                            }
                        }else if(posePoint.size() == POSEPOINTNUM2)
                        {
                            for (int[] index : pose29Index) {
                                canvas.drawLine(posePoint.get(index[0]).x, posePoint.get(index[0]).y,
                                        posePoint.get(index[1]).x, posePoint.get(index[1]).y, linePaint);
                            }
                        }else if(posePoint.size() == POSEPOINTNUM3)
                        {
                            for (int[] index : pose33Index) {
                                canvas.drawLine(posePoint.get(index[0]).x, posePoint.get(index[0]).y,
                                        posePoint.get(index[1]).x, posePoint.get(index[1]).y, linePaint);
                            }
                        }
                    }
                }
                break;
            case HOLISTIC:
                if (holisticInfo != null) {
                    //身体骨骼关键点
                    {
                        if (holisticInfo.poseInfo.landmarks != null && holisticInfo.poseInfo.landmarks.length > 0)
                        {
                            if (posePoint != null) {
                                posePoint.clear();
                            }
                            posePoint = new ArrayList<>();
                            Point2f[] landmarks = holisticInfo.poseInfo.landmarks;
                            for (Point2f landmark : landmarks) {
                                float x = landmark.x * scale;
                                float y = landmark.y * scale;
                                canvas.drawCircle(x, y, 2, circlePaint);
                                posePoint.add(new Point3f(x, y, 0));
                            }
                            linePaint.setColor(Color.GREEN);
                            if (posePoint.size() == POSEPOINTNUM1){
                                for (int[] index : pose17Index) {
                                    canvas.drawLine(posePoint.get(index[0]).x, posePoint.get(index[0]).y,
                                            posePoint.get(index[1]).x, posePoint.get(index[1]).y, linePaint);
                                }
                            }else if (posePoint.size() == POSEPOINTNUM2){
                                for (int[] index : pose29Index) {
                                    canvas.drawLine(posePoint.get(index[0]).x, posePoint.get(index[0]).y,
                                            posePoint.get(index[1]).x, posePoint.get(index[1]).y, linePaint);
                                }
                            }else if (posePoint.size() == POSEPOINTNUM3) {
                                for (int[] index : pose33Index) {
                                    canvas.drawLine(posePoint.get(index[0]).x, posePoint.get(index[0]).y,
                                            posePoint.get(index[1]).x, posePoint.get(index[1]).y, linePaint);
                                }
                            }
//                            float x1 = holisticInfo.poseInfo.rect.x * scale;
//                            float y1 = holisticInfo.poseInfo.rect.y * scale;
//                            float x2 = x1 + holisticInfo.poseInfo.rect.width * scale;
//                            float y2 = y1 + holisticInfo.poseInfo.rect.height * scale;
//                            canvas.drawRect(new Rect((int) x1, (int) y1, (int) x2, (int) y2), rectPaint);
                        }
                    }

                    //脸部关键点
                    {
                        if (holisticInfo.faceInfo.landmarks != null && holisticInfo.faceInfo.landmarks.length >0){
                            Point2f[] landmarks = holisticInfo.faceInfo.landmarks;
                            for (Point2f landmark : landmarks) {
                                float x = landmark.x * scale;
                                float y = landmark.y * scale;
                                canvas.drawCircle(x, y, 2, circlePaint);
                            }
//
//                            float x1 = holisticInfo.faceInfo.rect.x * scale;
//                            float y1 = holisticInfo.faceInfo.rect.y * scale;
//                            float x2 = x1 + holisticInfo.faceInfo.rect.width * scale;
//                            float y2 = y1 + holisticInfo.faceInfo.rect.height * scale;
//                            canvas.drawRect(new Rect((int) x1, (int) y1, (int) x2, (int) y2), rectPaint);
                        }
                    }

                    //左手部关键点
                    {
                        if (holisticInfo.leftHandInfo.landmarks != null && holisticInfo.leftHandInfo.landmarks.length > 0)   //由于初始化可选择不进行手部关键点检测，因此需要校验该landmarks
                        {
                            if (handPoint != null) {
                                handPoint.clear();
                            }
                            handPoint = new ArrayList<>();
                            Point2f[] landmarks = holisticInfo.leftHandInfo.landmarks;
                            for (Point2f landmark : landmarks) {
                                float x = landmark.x * scale;
                                float y = landmark.y * scale;
                                canvas.drawCircle(x, y, 2, circlePaint);
                                handPoint.add(new Point3f(x, y, 0));
                            }
                            if (handPoint.size() == HANDPOINTNUM) {
                                for (int[] index : handLineIndex) {
                                    canvas.drawLine(handPoint.get(index[0]).x, handPoint.get(index[0]).y,
                                            handPoint.get(index[1]).x, handPoint.get(index[1]).y, linePaint);
                                }
                            }
                            float x1 = holisticInfo.leftHandInfo.rect.x * scale;
                            float y1 = holisticInfo.leftHandInfo.rect.y * scale;
                            float x2 = x1 + holisticInfo.leftHandInfo.rect.width * scale;
                            float y2 = y1 + holisticInfo.leftHandInfo.rect.height * scale;
                            canvas.drawRect(new Rect((int) x1, (int) y1, (int) x2, (int) y2), rectPaint);
                        }
                    }

                    //右手部关键点
                    {
                        if (holisticInfo.rightHandInfo.landmarks != null && holisticInfo.rightHandInfo.landmarks.length > 0)   //由于初始化可选择不进行手部关键点检测，因此需要校验该landmarks
                        {
                            if (handPoint != null) {
                                handPoint.clear();
                            }
                            handPoint = new ArrayList<>();
                            Point2f[] landmarks = holisticInfo.rightHandInfo.landmarks;
                            for (Point2f landmark : landmarks) {
                                float x = landmark.x * scale;
                                float y = landmark.y * scale;
                                canvas.drawCircle(x, y, 2, circlePaint);
                                handPoint.add(new Point3f(x, y, 0));
                            }
                            if (handPoint.size() == HANDPOINTNUM) {
                                for (int[] index : handLineIndex) {
                                    canvas.drawLine(handPoint.get(index[0]).x, handPoint.get(index[0]).y,
                                            handPoint.get(index[1]).x, handPoint.get(index[1]).y, linePaint);
                                }
                            }
                            float x1 = holisticInfo.rightHandInfo.rect.x * scale;
                            float y1 = holisticInfo.rightHandInfo.rect.y * scale;
                            float x2 = x1 + holisticInfo.rightHandInfo.rect.width * scale;
                            float y2 = y1 + holisticInfo.rightHandInfo.rect.height * scale;
                            canvas.drawRect(new Rect((int) x1, (int) y1, (int) x2, (int) y2), rectPaint);
                        }
                    }
                }
                break;
            default:
                break;
        }
//        costTime += System.currentTimeMillis() - startTime;
//        frame++;
    }

    public void drawFacePoint(FaceInfo[] faceInfos, float scale){
        this.faceInfos = faceInfos;
        this.scale = scale;
        this.invalidate();
    }

    public void drawHandPoint(HandInfo[] handInfos, float scale) {
        this.handInfos = handInfos;
        this.scale = scale;
        this.invalidate();
    }

    public void drawPosePoint(PoseInfo[] poseInfos, float scale) {
        this.poseInfos = poseInfos;
        this.scale = scale;
        this.invalidate();
    }

    public void drawHolisticPoint(HolisticInfo holisticInfo, float scale) {
        this.holisticInfo = holisticInfo;
        this.scale = scale;
        this.invalidate();
    }

    public void release() {
        faceInfos = null;
        handInfos = null;
        poseInfos = null;
        holisticInfo = null;
        if (handPoint != null) {
            handPoint.clear();
        }
    }
}