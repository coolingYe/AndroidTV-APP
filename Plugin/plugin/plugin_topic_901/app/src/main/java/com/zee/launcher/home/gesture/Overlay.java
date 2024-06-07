package com.zee.launcher.home.gesture;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.zeewain.zeepose.FaceInfo;
import com.zeewain.zeepose.HandInfo;
import com.zeewain.zeepose.HolisticInfo;
import com.zeewain.zeepose.Pose3D2DInfo;
import com.zeewain.zeepose.PoseInfo;
import com.zeewain.zeepose.base.Point2f;
import com.zeewain.zeepose.base.Point3f;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

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
    private Rect checkRect = null;
    public int[] paintColors = null;
    private float scale = 0f;
    private boolean isDraw = false;
    //    private HolisticInfo[] holisticInfos = null;
    public ConcurrentHashMap<Integer, Integer> showPoseArrayIndexMap = new ConcurrentHashMap<>();
    public ConcurrentHashMap<Integer, Integer> showPosePointMap = new ConcurrentHashMap<>();

    private ArrayList<Point3f> handPoint;
    private int[][] handLineIndex = new int[][]{{0, 1}, {1, 2}, {2, 3}, {3, 4}, {0, 5},
            {5, 6}, {6, 7}, {7, 8}, {5, 9}, {9, 10}, {10, 11}, {11, 12}, {9, 13}, {13, 14},
            {14, 15}, {15, 16}, {13, 17}, {0, 17}, {17, 18}, {18, 19}, {19, 20}};

    private ArrayList<Point3f> posePoint;
    private int[][] pose17Index = new int[][]{{0, 2}, {2, 4}, {0, 1}, {1, 3},
            {5, 6}, {5, 7}, {7, 9}, {6, 8}, {8, 10},
            {6, 12}, {5, 11},
            {11, 12}, {11, 13}, {13, 15}, {12, 14}, {14, 16}
    };

    private int[][] pose29Index = new int[][]{{0, 2}, {2, 4}, {0, 1}, {1, 3},
            {5, 6}, {5, 7}, {7, 9}, {9, 15}, {9, 13}, {9, 11},
            {6, 8}, {8, 10}, {10, 12}, {10, 14}, {10, 16},
            {17, 18},
            {6, 18}, {18, 20}, {20, 22},
            {5, 17}, {17, 19}, {19, 21},
            {22, 24}, {24, 26}, {22, 28},
            {21, 23}, {21, 25}, {21, 27}
    };

    private int[][] pose33Index = new int[][]{{0, 1}, {1, 2}, {2, 3}, {3, 7},
            {0, 4}, {4, 5}, {5, 6}, {6, 8},
            {9, 10},
            {11, 13}, {13, 15}, {15, 21}, {15, 17}, {15, 19}, {17, 19},
            {12, 14}, {14, 16}, {16, 22}, {16, 18}, {16, 20}, {20, 22},
            {11, 23}, {23, 25}, {25, 27}, {27, 29}, {27, 31}, {29, 31},
            {12, 24}, {24, 26}, {26, 28}, {28, 30}, {28, 32}, {30, 32},
            {11, 12}, {23, 24}
    };

    private LocationEnum location = null;

    private final Paint circlePaint = new Paint();
    private final Paint rectPaint = new Paint();
    private final Paint linePaint = new Paint();
    private Path proPath;
    private final int maxProgress = 100;
    private int curProgress = 0;
    private float pointRadius = 4f;
    private boolean showLine = true;
    private boolean showHeadPoint = false;

    public Overlay(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void initOverlay(LocationEnum locationEnum) {
        location = locationEnum;

        circlePaint.setAntiAlias(true);
        circlePaint.setColor(Color.GREEN);
        circlePaint.setStrokeWidth(3f);
        circlePaint.setStyle(Paint.Style.FILL);


        rectPaint.setAntiAlias(true);
        rectPaint.setColor(Color.GRAY);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(3.f);
        rectPaint.setStrokeJoin(Paint.Join.ROUND);


        linePaint.setAntiAlias(true);//设置画笔抗锯齿
        linePaint.setStyle(Paint.Style.STROKE);//设置画笔（忘了）
        linePaint.setStrokeWidth(3.f);//设置画笔宽度
        linePaint.setColor(Color.BLUE);//设置画笔颜色
        linePaint.setStrokeJoin(Paint.Join.ROUND);
        linePaint.setStrokeCap(Paint.Cap.ROUND);


        paintColors = new int[]{Color.GREEN};
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        switch (location) {
            case FACE:
                if (faceInfos != null) {
                    for (FaceInfo faceInfo : faceInfos) {
                        if (faceInfo.rect != null) {
                            float x1 = faceInfo.rect.x;
                            float y1 = faceInfo.rect.y;
                            float x2 = x1 + faceInfo.rect.width;
                            float y2 = y1 + faceInfo.rect.height;
                            canvas.drawRect(new Rect((int) x1, (int) y1, (int) x2, (int) y2), rectPaint);
                        }
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
                                float x = landmark.x;
                                float y = landmark.y;
                                canvas.drawCircle(x, y, pointRadius, circlePaint);
                                handPoint.add(new Point3f(x, y, 0));
                            }
                        }
                        if (handPoint.size() == HANDPOINTNUM) {
                            for (int[] index : handLineIndex) {
                                canvas.drawLine(handPoint.get(index[0]).x, handPoint.get(index[0]).y,
                                        handPoint.get(index[1]).x, handPoint.get(index[1]).y, linePaint);
                            }
                        }
                        if (handInfo.rect != null) {
                            float x1 = handInfo.rect.x;
                            float y1 = handInfo.rect.y;
                            float x2 = x1 + handInfo.rect.width;
                            float y2 = y1 + handInfo.rect.height;
                            canvas.drawRect(new Rect((int) x1, (int) y1, (int) x2, (int) y2), rectPaint);
                        }
                    }
                }
                break;
            case SINGLEPOSE:
            case PERSONPOSE:
                if (poseInfos != null) {
                    for (int j = 0; j < poseInfos.length; j++) {
//                        if (!showPoseArrayIndexMap.contains(j)) continue;
                        circlePaint.setColor(paintColors[j % paintColors.length]);
                        linePaint.setColor(paintColors[j % paintColors.length]);
                        PoseInfo poseInfo = poseInfos[j];
                        if (posePoint != null) {
                            posePoint.clear();
                        }
                        posePoint = new ArrayList<>();
                        Point2f[] landmarks = poseInfo.landmarks;
                        if (landmarks != null) {
                            for (int i = 0; i < landmarks.length; i++) {
                                float x = landmarks[i].x;
                                float y = landmarks[i].y;
                                posePoint.add(new Point3f(x, y, 0));
                                if (showPosePointMap.contains(i)) {
                                    canvas.drawCircle(x, y, pointRadius, circlePaint);
                                }
                            }
                        }

                        if (showLine) {
                            if (posePoint.size() == POSEPOINTNUM1) {
                                int count = 0;
                                for (int[] index : pose17Index) {
                                    count++;
                                    if (!showHeadPoint && count <= 4) continue;
                                    canvas.drawLine(posePoint.get(index[0]).x, posePoint.get(index[0]).y,
                                            posePoint.get(index[1]).x, posePoint.get(index[1]).y, linePaint);

                                }
                            } else if (posePoint.size() == POSEPOINTNUM2) {
                                int count = 0;
                                for (int[] index : pose29Index) {
                                    count++;
                                    if (!showHeadPoint && count <= 4) continue;
                                    canvas.drawLine(posePoint.get(index[0]).x, posePoint.get(index[0]).y,
                                            posePoint.get(index[1]).x, posePoint.get(index[1]).y, linePaint);
                                }
                            } else if (posePoint.size() == POSEPOINTNUM3) {
                                for (int[] index : pose33Index) {
                                    canvas.drawLine(posePoint.get(index[0]).x, posePoint.get(index[0]).y,
                                            posePoint.get(index[1]).x, posePoint.get(index[1]).y, linePaint);
                                }
                            }
                        }
                    }
                }
                break;
            case RECT:
                int right = checkRect.right / 3;
                int left = checkRect.left / 3;
                int top = checkRect.top / 3;
                int bottom = checkRect.bottom / 3;
                canvas.drawRect(new Rect(left, top, right, bottom), rectPaint);
                break;
            case CLEAR:
                Paint paint = new Paint();
                paint.setColor(Color.TRANSPARENT);
                paint.setStrokeWidth(4.0f);
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawRect(checkRect, paint);
                break;
            case PROGRESS:
                drawProgress(canvas);
                break;
            case HOLISTIC:
                if (holisticInfo != null) {
                    //身体骨骼关键点
                    {
                        if (holisticInfo.poseInfo != null && holisticInfo.poseInfo.landmarks != null && holisticInfo.poseInfo.landmarks.length > 0) {
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
                            if (posePoint.size() == POSEPOINTNUM1) {
                                for (int[] index : pose17Index) {
                                    canvas.drawLine(posePoint.get(index[0]).x, posePoint.get(index[0]).y,
                                            posePoint.get(index[1]).x, posePoint.get(index[1]).y, linePaint);
                                }
                            } else if (posePoint.size() == POSEPOINTNUM2) {
                                for (int[] index : pose29Index) {
                                    canvas.drawLine(posePoint.get(index[0]).x, posePoint.get(index[0]).y,
                                            posePoint.get(index[1]).x, posePoint.get(index[1]).y, linePaint);
                                }
                            } else if (posePoint.size() == POSEPOINTNUM3) {
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
                        if (holisticInfo.faceInfo != null && holisticInfo.faceInfo.landmarks != null && holisticInfo.faceInfo.landmarks.length > 0) {
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
                        if (holisticInfo.leftHandInfo != null && holisticInfo.leftHandInfo.landmarks != null && holisticInfo.leftHandInfo.landmarks.length > 0)   //由于初始化可选择不进行手部关键点检测，因此需要校验该landmarks
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
                        if (holisticInfo.rightHandInfo != null && holisticInfo.rightHandInfo.landmarks != null && holisticInfo.rightHandInfo.landmarks.length > 0)   //由于初始化可选择不进行手部关键点检测，因此需要校验该landmarks
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
            case SHOULDER:
                if (poseInfos != null) {
                    for (int j = 0; j < poseInfos.length; j++) {
                        circlePaint.setColor(paintColors[j % paintColors.length]);
                        PoseInfo poseInfo = poseInfos[j];
                        Point2f[] landmarks = poseInfo.landmarks;
                        if (landmarks != null) {
                            float x = landmarks[5].x;
                            float y = landmarks[5].y;
                            canvas.drawCircle(x, y, pointRadius, circlePaint);
                            x = landmarks[6].x;
                            y = landmarks[6].y;
                            canvas.drawCircle(x, y, pointRadius, circlePaint);
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

    private void drawProgress(Canvas canvas) {
        canvas.drawRect(checkRect, rectPaint);
        int point1X = checkRect.left;
        int point1Y = checkRect.top;
        int point2X = checkRect.right;
        int point2Y = checkRect.top;
        int point3X = checkRect.right;
        int point3Y = checkRect.bottom;
        int point4X = checkRect.left;
        int point4Y = checkRect.bottom;
        float allLength = 2 * (checkRect.width() + checkRect.height());
        float curPercent = (float) curProgress / maxProgress;
        float dotCX, dotCY;
        proPath = new Path();
        proPath.moveTo(point1X, point1Y);
        if (curPercent > 0) {
            if (curPercent < checkRect.width() / allLength) {
                dotCX = point1X + allLength * curProgress / maxProgress;
                dotCY = point1Y;
                proPath.lineTo(dotCX, dotCY);
            } else if (curPercent < (checkRect.height() + checkRect.width()) / allLength) {
                dotCX = point2X;
                dotCY = point1Y + allLength * curProgress / maxProgress - checkRect.width();
                proPath.lineTo(point2X, point2Y);
                proPath.lineTo(dotCX, dotCY);
            } else if (curPercent < (2 * checkRect.width() + checkRect.height()) / allLength) {
                dotCX = point1X + allLength - checkRect.height() - allLength * curProgress / maxProgress;
                dotCY = point4Y;
                proPath.lineTo(point2X, point2Y);
                proPath.lineTo(point3X, point3Y);
                proPath.lineTo(dotCX, dotCY);
            } else if (curPercent < 1) {
                dotCX = point1X;
                dotCY = point1Y + allLength - allLength * curProgress / maxProgress;
                Log.e("wang", "drawProgress: " + dotCY);
                proPath.lineTo(point2X, point2Y);
                proPath.lineTo(point3X, point3Y);
                proPath.lineTo(point4X, point4Y);
                proPath.lineTo(dotCX, dotCY);
            } else {
                proPath.lineTo(point2X, point2Y);
                proPath.lineTo(point3X, point3Y);
                proPath.lineTo(point4X, point4Y);
                proPath.close();
            }
            canvas.drawPath(proPath, linePaint);
        }

    }

    public void drawFacePoint(Rect rect) {
        location = LocationEnum.FACE;
        this.checkRect = null;
        this.handInfos = null;
        this.faceInfos = faceInfos;
        this.invalidate();
    }

    public void drawRect(Rect rect, int color) {
        location = LocationEnum.RECT;
        this.checkRect = rect;
        this.rectPaint.setColor(color);
        this.invalidate();
    }

    public void drawProgress(int progress) {
        location = LocationEnum.PROGRESS;
        this.curProgress = progress;
        this.invalidate();
    }

    public void clearRect() {
        location = LocationEnum.CLEAR;
        this.invalidate();
    }


    public void drawFacePoint(FaceInfo[] faceInfos) {
        location = LocationEnum.FACE;
        this.poseInfos = null;
        this.handInfos = null;
        this.faceInfos = faceInfos;
        this.invalidate();
    }

    public void drawHandPoint(HandInfo[] handInfos) {
        location = LocationEnum.HAND;
        this.poseInfos = null;
        this.faceInfos = null;
        this.handInfos = handInfos;
        this.invalidate();
    }

    public void drawPosePoint(PoseInfo[] poseInfos) {
        location = LocationEnum.PERSONPOSE;
        this.faceInfos = null;
        this.handInfos = null;
        this.poseInfos = poseInfos;
        this.invalidate();
    }

    public void drawHolisticPoint(HolisticInfo holisticInfo, float scale) {
        this.holisticInfo = holisticInfo;
        this.scale = scale;
        this.invalidate();
    }

    public void drawShoulderPoint(PoseInfo[] poseInfos) {
        location = LocationEnum.SHOULDER;
        this.faceInfos = null;
        this.handInfos = null;
        this.poseInfos = poseInfos;
        isDraw = true;
        this.invalidate();
    }


    public void drawPose3D2DPoint(Pose3D2DInfo[] pose3D2DInfos) {
        location = LocationEnum.PERSONPOSE;
        this.faceInfos = null;
        this.handInfos = null;
        if (pose3D2DInfos != null) {
            PoseInfo[] poseInfos = new PoseInfo[pose3D2DInfos.length];
            for (int i = 0; i < pose3D2DInfos.length; i++) {
                Pose3D2DInfo pose3D2DInfo = pose3D2DInfos[i];
                poseInfos[i] = new PoseInfo(pose3D2DInfo.rect, pose3D2DInfo.trackId, pose3D2DInfo.landmarks2D, pose3D2DInfo.scores2D);
            }
            this.poseInfos = poseInfos;
            this.invalidate();
        }
    }

    public void setPaintStyle(float pointRadius, int[] paintColors, float paintStrokeWidth, boolean showLine) {
        circlePaint.setColor(paintColors[0]);
        circlePaint.setStrokeWidth(paintStrokeWidth);
        this.pointRadius = pointRadius;
        this.showLine = showLine;

        rectPaint.setColor(paintColors[0]);
        rectPaint.setStrokeWidth(paintStrokeWidth);

        linePaint.setColor(paintColors[0]);
        linePaint.setStrokeWidth(paintStrokeWidth);
        this.paintColors = paintColors;
    }

    public void setShowHeadPoint(boolean showHeadPoint) {
        this.showHeadPoint = showHeadPoint;
    }

    public void clear() {
        if (isDraw) {
            this.poseInfos = null;
            this.faceInfos = null;
            this.handInfos = null;
            isDraw = false;
            this.invalidate();
        }
    }

    public void release() {
        faceInfos = null;
        handInfos = null;
        poseInfos = null;
        if (handPoint != null) {
            handPoint.clear();
        }
    }


}