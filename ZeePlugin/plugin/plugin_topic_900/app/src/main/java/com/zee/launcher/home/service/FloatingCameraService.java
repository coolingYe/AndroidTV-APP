package com.zee.launcher.home.service;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.GestureEstimate.Hand;
import com.GestureEstimate.HandList;
import com.GestureEstimate.Point3f;
import com.GestureEstimate.Point3fList;
import com.GestureEstimate.SimpleStaticGesture;
import com.zee.launcher.home.R;
import com.zee.launcher.home.data.protocol.response.AkSkResp;
import com.zee.launcher.home.gesture.zeewainpose.Config;
import com.zee.launcher.home.utils.GestureConfigUtils;
import com.zeewain.base.BuildConfig;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.widgets.FloatProgressView;
import com.zeewain.zeepose.HolisticInfo;

import com.zeewain.zeepose.ZeewainPose;
import com.zeewain.zeepose.ZwnConfig;
import com.zeewain.zeepose.base.Point2f;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class FloatingCameraService extends Service {
    private View mFloatView;
    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;
    private static int ret = -1;
    public static boolean isStart = false;
    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the camera.
     */
    private final Semaphore mCameraOpenCloseLock = new Semaphore(1);

    static {
        System.loadLibrary("GestureEstimate");

    }

    private static final String TAG = "CameraService";

    private static final int MSG_CAMERA_ERR_CHECK_REOPEN = 2001;
    private static final int MSG_CAMERA_LEFT_PROGRESS = 2002;
    private static final int MSG_CAMERA_RIGHT_PROGRESS = 2003;
    private final int upHandTime = 1200;
    private int handType = 0;  // 0 未举起  1 举起左手 2 举起右手
    private long holderTime = 0;
    private TextureView mTextureView;
    private CameraManager mCameraManager;
    private CameraDevice mCameraDevice;
    private String mCameraId = "0";
    private Size previewSize; // 用于设置预览的宽高
    String[] models = new ZeewainPose().getModelNameLists();
    private ZeewainPose zeewainPose = new ZeewainPose();
    private int imgWidth = 640;
    private int imgHeight = 480;
    private Bitmap mBitmap = null;
    private boolean runClassifier = false;
    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    private SimpleStaticGesture simpleStaticGesture = new SimpleStaticGesture();
    private Point3fList bodyPointList = new Point3fList();
    private Point3f bodyPoint3f = new Point3f();
    private Handler handler = new Handler();
    private Button leftButton;
    private Button rightButton;
    private FloatProgressView leftProgress;
    private FloatProgressView rightProgress;
    private Handler serviceHandler;
    private int maxHeight;
    private int careCameraErrorCount = 0;

    private final Runnable cameraErrorRunnable = () -> {
        Log.i(TAG, "onError(), execute cameraErrorRunnable==========>>>>>>>>>");
        mCameraDevice = null;
        handleCameraError(new CameraErrorException("onError occurred", 4));
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        initUi();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showUi();
        return super.onStartCommand(intent, flags, startId);
    }

    private void initUi() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        layoutParams = new WindowManager.LayoutParams();
        layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.gravity = Gravity.TOP;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        DisplayMetrics dm = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(dm);
        layoutParams.width = dm.widthPixels;
        layoutParams.height = 380;
        maxHeight = dm.heightPixels - 280;
        layoutParams.x = 0;
        layoutParams.y = maxHeight;
        serviceHandler = new Handler(getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message message) {
                if (message.what == MSG_CAMERA_RIGHT_PROGRESS) {
                    rightProgress.setProgress((int) message.obj);
                } else if (message.what == MSG_CAMERA_LEFT_PROGRESS) {
                    leftProgress.setProgress((int) message.obj);
                } else if (message.what == MSG_CAMERA_ERR_CHECK_REOPEN) {
                    if (getCameraIdAndPreviewSizeByFacing() && mTextureView != null && mTextureView.isAvailable()) {
                        reOpenCamera();
                    } else {
                        sendErrorMessage(Config.ShowCode.CODE_CAMERA_ERROR);
                        stopSelf();
                    }
                }
                return true;
            }
        });
    }

    private void showUi() {
        if (!Settings.canDrawOverlays(FloatingCameraService.this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + FloatingCameraService.this.getPackageName()));
            this.startActivity(intent);
        } else {
            showWindow();
            Log.d(TAG, "浮窗权限已经授予");
        }
    }

    private void showWindow() {
        LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        mFloatView = inflater.inflate(R.layout.activity_ai, null);
        windowManager.addView(mFloatView, layoutParams);
        intiView(mFloatView);
        isStart = true;
        copyModelFromAssetsToData();
        releaseCamera();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (ret != 0) {
            setAuthAlgorithm();
        }
        if (ret == 0) {
            Log.d(TAG, "初始化TextureView");
            initCameraManager();
            initTextureView(mFloatView);
        }
    }


    private void setAuthAlgorithm() {
        //授权成功返回0,失败返回其他
        //在线授权激活接口
        if (ret != 0) {
            if (BuildConfig.FLAVOR == "plugin") {
                AkSkResp akSkCode = GestureConfigUtils.getAkSkCode(this);
                String ak = "";
                String sk = "";
                String url = "";
                if (akSkCode != null) {
                    if (!TextUtils.isEmpty(akSkCode.akCode)) {
                        ak = akSkCode.akCode;
                    }
                    if (!TextUtils.isEmpty(akSkCode.skCode)) {
                        sk = akSkCode.skCode;
                    }
                }
                if (!TextUtils.isEmpty(GestureConfigUtils.getAuthUrl(this))) {
                    url = GestureConfigUtils.getAuthUrl(this);
                }
                Log.d(TAG, "ak sk url" + ak + " " + sk + " " + url);
                try {
                    ret = zeewainPose.setAuthOnline(ak, sk, url, BaseConstants.LICENSE_V2_FILE_PATH);
                } catch (Exception e) {
                    Log.e("wang", "setAuthAlgorithm: " + e.toString());
                    // 发送签名失败信息
                    Intent intent = new Intent();
                    intent.putExtra("state", "authError");
                    intent.setAction("floatCamera");
                    sendBroadcast(intent);
                    handler.postDelayed(() -> {
                        onDestroy();
                        stopSelf();
                    }, 1000);
                    return;
                }
            } else {
                try {
                    ret = zeewainPose.setAuthOnline(Config.ak, Config.sk, Config.url, BaseConstants.LICENSE_V2_FILE_PATH);
                } catch (Exception e) {
                    Intent intent = new Intent();
                    intent.putExtra("state", "authError");
                    intent.setAction("floatCamera");
                    sendBroadcast(intent);
                    handler.postDelayed(() -> {
                        onDestroy();
                        stopSelf();
                    }, 1000);
                    return;
                }
            }
        }
        if (ret != 0) {
            Toast.makeText(getApplicationContext(), "在线授权设置失败,手势操作开启失败", Toast.LENGTH_SHORT).show();
            stopSelf();
            onDestroy();
            return;
        }
        Log.d(TAG, "授权设置成功");
        // 打开图像裁剪功能
        zeewainPose.setDynamicCropStatus(true);
        Log.d(TAG, "开始进行算法初始化...");
        initAlgorithm();
        Log.d(TAG, "算法初始化完成...");
    }

    private void initAlgorithm() {
        String modelDir = this.getFilesDir() + File.separator;
        int deviceType = 1;
        int threadNum = 2;
        int modelType = 1;
        ZwnConfig zwnConfig = new ZwnConfig(modelDir, deviceType, threadNum, modelType);
        ret = zeewainPose.initHolistic(zwnConfig);
        if (ret != 0) {
            Log.e(TAG, "initAlgorithm: " + "failed to init zeepose");
        } else {
            Log.e(TAG, "initAlgorithm: " + "success to init zeepose");
        }
    }


    private void intiView(View view) {
        leftButton = (Button) view.findViewById(R.id.btn_app_left);
        rightButton = (Button) view.findViewById(R.id.btn_app_right);
        leftProgress = (FloatProgressView) view.findViewById(R.id.fl_progress_left);
        rightProgress = (FloatProgressView) view.findViewById(R.id.fl_progress_right);
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                leftButton.setEnabled(false);
                isStart = false;
                Intent intent = new Intent();
                intent.putExtra("state", "close");
                intent.putExtra("skuIdType", "left");
                intent.setAction("floatCamera");
                sendBroadcast(intent);

            }
        });

        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rightButton.setEnabled(false);
                isStart = false;
                Intent intent = new Intent();
                intent.putExtra("state", "close");
                intent.putExtra("skuIdType", "right");
                intent.setAction("floatCamera");
                sendBroadcast(intent);
            }
        });


        // 设置触摸事件处理拖动
        view.setOnTouchListener(new View.OnTouchListener() {
            private int lastY;
            private float mTouchStartY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastY = layoutParams.y;
                        mTouchStartY = event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        layoutParams.y = (int) (lastY + event.getRawY() - mTouchStartY);
                        if (layoutParams.y < 0) layoutParams.y = 0;
                        if (layoutParams.y > maxHeight) layoutParams.y = maxHeight;
                        windowManager.updateViewLayout(v, layoutParams);
                        break;
                    case MotionEvent.ACTION_UP:
                        // 处理点击事件或其他逻辑
                        break;
                }
                return true;
            }
        });

    }

    private void initCameraManager() {
        mCameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
    }

    private void initTextureView(View view) {

        mTextureView = (TextureView) view.findViewById(R.id.textureView);

        // 对预览View的状态监听
        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                Log.i(TAG, "onSurfaceTextureAvailable width = " + width + ",height = " + height);
                //1、当SurefaceTexture可用的时候，设置相机参数并打开相机
                initCamera();
            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
                Log.i(TAG, "onSurfaceTextureSizeChanged width = " + width + ",height = " + height);
            }

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
                Log.i(TAG, "onSurfaceTextureDestroyed！");
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
                //正常预览的时候，会一直打印
                //Log.i(TAG, "onSurfaceTextureUpdated！");
            }
        });
    }


    private void initCamera() {
        startBackgroundThread();
        Log.i(TAG, "initCamera");
        // 2.配置前置相机，获取尺寸及id
        boolean hasCamera = getCameraIdAndPreviewSizeByFacing();// 0为前置摄像头,Camera api1里面定义0为后置
        if (hasCamera) {
            openCamera();
        } else {
            handleCameraError(new InvalidException("No support camera!"));
        }
    }


    /*获取cameraId及相机预览的最佳尺寸*/
    private boolean getCameraIdAndPreviewSizeByFacing() {
        try {
            String[] cameraIdList = mCameraManager.getCameraIdList(); //如果设备节点不可用，会阻塞在这里
            if (cameraIdList.length == 0) return false;
            for (String cameraId : cameraIdList) {
                CameraCharacteristics cameraCharacteristics = mCameraManager.getCameraCharacteristics(cameraId);
                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT)
                    continue;
                StreamConfigurationMap streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                Size[] outputSizes = streamConfigurationMap.getOutputSizes(SurfaceTexture.class);
                mCameraId = cameraId;
                for (Size sizes : outputSizes) {
                    Log.e(TAG, "size: " + sizes.getWidth() + " " + sizes.getHeight());
                }
                previewSize = setOptimalPreviewSize(outputSizes, mTextureView.getMeasuredWidth(), mTextureView.getMeasuredHeight());
                Log.e(TAG, "最佳预览尺寸（w-h）：" + previewSize.getWidth() + "-" + previewSize.getHeight() + ",相机id：" + mCameraId);
            }
        } catch (CameraAccessException e) {
            Log.e(TAG, "getCameraIdAndPreviewSizeByFacing error = " + e.getMessage());
        }
        return true;
    }


    /**
     * 打开相机，预览是在回调里面执行的。
     */
    private void openCamera() {
        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            Log.i(TAG, "openCamera");
            mCameraManager.openCamera(mCameraId, mStateCallback, null);
        } catch (CameraAccessException e) {
            Log.e(TAG, "openCamera error = " + e.getMessage());
            handleCameraError(e);
        }
    }


    public void reOpenCamera() {
        Log.i(TAG, "reOpenCamera() ");
        openCamera();
    }


    /**
     * 根据相机可用的预览尺寸和用户给定的TextureView的显示尺寸选择最接近的预览尺寸
     */
    private Size setOptimalPreviewSize(Size[] sizes, int previewViewWidth, int previewViewHeight) {
        List<Size> bigEnoughSizes = new ArrayList<>();
        List<Size> notBigEnoughSizes = new ArrayList<>();
        for (Size size : sizes) {
            if (size.getWidth() >= previewViewWidth && size.getHeight() >= previewViewHeight) {
                bigEnoughSizes.add(size);
            } else {
                notBigEnoughSizes.add(size);
            }
        }
        if (bigEnoughSizes.size() > 0) {
            return Collections.min(bigEnoughSizes, new Comparator<Size>() {
                @Override
                public int compare(Size lhs, Size rhs) {
                    return Long.signum((long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
                }
            });
        } else if (notBigEnoughSizes.size() > 0) {
            return Collections.max(notBigEnoughSizes, new Comparator<Size>() {
                @Override
                public int compare(Size lhs, Size rhs) {
                    return Long.signum((long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
                }
            });
        } else {
            Log.e(TAG, "未找到合适的预览尺寸");
            return sizes[0];
        }
    }

    //关闭相机，释放对象
    private void releaseCamera() {
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
        stopBackgroundThread();
    }

    /**
     * 相机状态监听对象
     */
    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            Log.i(TAG, "StateCallback！ onOpened");
            mCameraOpenCloseLock.release();
            mCameraDevice = camera; // 打开成功，保存代表相机的CameraDevice实例
            careCameraErrorCount = 0;
            SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(640, 480);
            Surface surface = new Surface(surfaceTexture);
            ArrayList<Surface> previewList = new ArrayList<>();
            previewList.add(surface);
            try {
                // 6.将TextureView的surface传递给CameraDevice
                mCameraDevice.createCaptureSession(previewList, new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession session) {
                        if (null == mCameraDevice) return;
                        try {
                            CaptureRequest.Builder builder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                            builder.addTarget(surface); // 必须设置才能正常预览
                            CaptureRequest captureRequest = builder.build();
                            session.setRepeatingRequest(captureRequest, mSessionCaptureCallback, mBackgroundHandler);
                            sendStartAdv();
                        } catch (CameraAccessException e) {
                            Log.e(TAG, "createCaptureRequest error = " + e.getMessage());
                            handleCameraError(e);
                        }
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                        Log.e(TAG, "onConfigureFailed");
                        handleCameraError(new Exception("onConfigureFailed"));
                    }
                }, null);
            } catch (CameraAccessException e) {
                Log.e(TAG, "createCaptureSession error = " + e.getMessage());
                handleCameraError(e);
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            Log.e(TAG, "StateCallback！ onDisconnected camera.getId() = " + cameraDevice.getId());
            mCameraOpenCloseLock.release();
            Log.i(TAG, "onDisconnected(), execute cameraDevice close()");
            handleCameraError(new CameraErrorException("cameraDevice disconnected", 0));
            cameraDevice.close();
            Log.i(TAG, "onDisconnected(), execute cameraDevice close() done");
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            Log.e(TAG, "StateCallback camera.getId() = " + cameraDevice.getId() + " , error = " + error);
            mCameraOpenCloseLock.release();
            if (cameraDevice == null) {
                Log.i(TAG, "onError(), cameraDevice is null.");
                mCameraDevice = null;
                handleCameraError(new CameraErrorException("onError occurred", error));
            } else {
                Log.i(TAG, "onError(), execute cameraDevice close()");
                mTextureView.postDelayed(cameraErrorRunnable, 4000);
                cameraDevice.close();//sometimes cost 4s
                Log.i(TAG, "onError(), execute cameraDevice close() done");
                mTextureView.removeCallbacks(cameraErrorRunnable);
                mCameraDevice = null;
                handleCameraError(new CameraErrorException("onError occurred", error));
            }
        }
    };

    //预览情况回调
    private CameraCaptureSession.CaptureCallback mSessionCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
            // 正常预览会一直刷新
            //Log.i(TAG, "mSessionCaptureCallback onCaptureStarted frameNumber =" + frameNumber);
        }

        @Override
        public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
            //  Log.i(TAG, "mSessionCaptureCallback onCaptureProgressed request =" + request);
        }

        @Override
        public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
            //  Log.e(TAG, "mSessionCaptureCallback onCaptureFailed request =" + request);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            // 正常预览时会一直刷新
            //Log.i(TAG, "mSessionCaptureCallback onCaptureCompleted request =" + request);
        }
    };


    /****************************zeepose**********************************/
    protected void copyModelFromAssetsToData() {
        // assets目录下的模型文件名
        try {
            for (String model : models) {
                copyAssetFileToFiles(this, model);
            }
            Log.d(TAG, "copyModelFromAssetsToData: " + "Copy model Success");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "copyModelFromAssetsToData: " + e.toString());
        }
    }

    public void copyAssetFileToFiles(Context context, String filename) throws IOException {
        File of = new File(context.getFilesDir() + File.separator + filename);
        if (!of.exists()) {
            InputStream is = context.getAssets().open(filename);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            of.createNewFile();
            FileOutputStream os = new FileOutputStream(of);
            os.write(buffer);
            os.close();
            is.close();
        }
    }

    private void holisticMethod(Bitmap bitmap) {
        long startTime = System.currentTimeMillis();
        zeewainPose.setHolisticBodyLandmarkStatus(true);
        zeewainPose.setHolisticHandLandmarkStatus(true);
        zeewainPose.setHolisticHandLandmarkNum(2);
        HolisticInfo holisticInfo = zeewainPose.GetHolisticInfo(bitmap);
        long endTime = System.currentTimeMillis();
        long costTime = endTime - startTime;
//        Log.i(TAG, "holistic costTime:" + costTime + " ms.");
        if (holisticInfo != null && holisticInfo.poseInfo != null && holisticInfo.poseInfo.landmarks != null) {
            bodyPointList.clear();
            for (int i = 0; i < holisticInfo.poseInfo.landmarks.length; ++i) {
                Point2f pt = holisticInfo.poseInfo.landmarks[i];
                bodyPoint3f.setX(pt.x);
                bodyPoint3f.setY(pt.y);
                bodyPoint3f.setZ(0);
                bodyPointList.add(bodyPoint3f);
            }
            if (handType == 0) {
                HandList hands = simpleStaticGesture.OnHandUpV2(bodyPointList);
                if (hands != null && hands.size() > 0) {
                    if (hands.get(0).equals(Hand.LeftHand)) {
                        Log.i(TAG, "leftHandUp");
                        handType = 1;

                    } else if (hands.get(0).equals(Hand.RightHand)) {
                        Log.i(TAG, "rightHandUp");
                        handType = 2;
                    }
                }
            } else if (handType == 1) {
                boolean holder = simpleStaticGesture.OnLeftHandUpV2(bodyPointList);
                if (holder) {
                    holderTime = holderTime + costTime;
                    Log.i(TAG, "leftHolder holderTime:" + holderTime);
                    if (holderTime >= upHandTime) {
                        runClassifier = false;
                        handType = 0;
                        Log.i(TAG, "sendLeft");
                        // 镜像 发反方向消息
                        postDataToService("right", Hand.LeftHand.toString());
                    }
                } else {
                    holderTime = holderTime - costTime;
                    if (holderTime <= 0) {
                        Log.i(TAG, "resetLeft");
                        handType = 0;
                        holderTime = 0;
                    }
                }
                sendProgress(MSG_CAMERA_RIGHT_PROGRESS);
            } else if (handType == 2) {
                boolean holder = simpleStaticGesture.OnRightHandUpV2(bodyPointList);
                if (holder) {
                    holderTime = holderTime + costTime;
                    Log.i(TAG, "rightHolder holderTime:" + holderTime);
                    if (holderTime >= upHandTime) {
                        runClassifier = false;
                        handType = 0;
                        Log.i(TAG, "sendRight");
                        // 镜像 发反方向消息
                        postDataToService("left", Hand.RightHand.toString());
                    }
                } else {
                    holderTime = holderTime - costTime;
                    if (holderTime <= 0) {
                        Log.i(TAG, "rightLeft");
                        handType = 0;
                        holderTime = 0;
                    }
                }
                sendProgress(MSG_CAMERA_LEFT_PROGRESS);
            }
        }
    }

    private void postDataToService(String name, String data) {
        Intent intent = new Intent();
        intent.putExtra("state", "close");
        intent.putExtra("skuIdType", name);
        intent.setAction("floatCamera");
        sendBroadcast(intent);
    }


    private void handleCameraError(Exception e) {
        if (e instanceof InvalidException) {
            sendErrorMessage(Config.ShowCode.CODE_CAMERA_INVALID);
            stopSelf();
        } else {
            careCameraErrorCount++;
            if (careCameraErrorCount == 1) {
                serviceHandler.removeMessages(MSG_CAMERA_ERR_CHECK_REOPEN);
                serviceHandler.sendEmptyMessageDelayed(MSG_CAMERA_ERR_CHECK_REOPEN, 1500);
            } else {
                sendErrorMessage(Config.ShowCode.CODE_CAMERA_ERROR);
                stopSelf();
            }
        }
    }

    private void sendErrorMessage(int errorType) {
        Intent intent = new Intent();
        intent.putExtra("state", "error");
        intent.putExtra("errorType", errorType);
        intent.setAction("floatCamera");
        sendBroadcast(intent);
    }

    private void sendStartAdv() {
        Intent intent = new Intent();
        intent.putExtra("state", "start");
        intent.setAction("floatCamera");
        sendBroadcast(intent);
    }

    /****************************gesture**********************************/


    private void transformImage(int viewWidth, int viewHeight) {
        int rotation = windowManager.getDefaultDisplay().getRotation();

        if (null == mTextureView) {
            return;
        }
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, previewSize.getWidth(), previewSize.getHeight());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max((float) viewHeight / previewSize.getHeight(), (float) viewWidth / previewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate((90 * (rotation - 2)) % 360, centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
//        Log.i(TAG, "configureTransform: " + getCameraOri(rotation, mCameraId) + "  " + rotation * 90 + "  " + matrix);
        mTextureView.setTransform(matrix);
    }


    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
        runClassifier = true;
        mBackgroundHandler.post(periodicClassify);
    }


    private void classifyFrame() {
        mBitmap = mTextureView.getBitmap(imgWidth, imgHeight);
        if (mBitmap != null) {
            Bitmap adjustBitmap = adjustBitmapRotation(mBitmap);
            if (adjustBitmap != null) {
                holisticMethod(adjustBitmap);
                adjustBitmap.recycle();
            }
            mBitmap.recycle();
        }
        mBackgroundHandler.post(periodicClassify);


    }

    private Runnable periodicClassify = () -> {
        if (runClassifier) {
            classifyFrame();
        }
    };


    private void stopBackgroundThread() {
        if (mBackgroundThread != null) {
            runClassifier = false;
            mBackgroundThread.quitSafely();
            try {
                mBackgroundThread.join();
                mBackgroundThread = null;
                mBackgroundHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }


    @Override
    public void onDestroy() {
        if (mFloatView != null) {
            boolean attachedToWindow = mFloatView.isAttachedToWindow();
            if (attachedToWindow) windowManager.removeView(mFloatView);
        }
        releaseCamera();
        handler.removeCallbacksAndMessages(null);
        serviceHandler.removeCallbacksAndMessages(null);
        isStart = false;
        super.onDestroy();
    }

    private Bitmap adjustBitmapRotation(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.setRotate(270, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);
        try {
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            return bitmap;
        } catch (OutOfMemoryError ex) {
            Log.e(TAG, "OutOfMemoryError " + ex);
        }
        return null;
    }

    private void sendProgress(int type) {
        Message obtain = Message.obtain();
        obtain.what = type;
        obtain.obj = (int) (holderTime * 100 / upHandTime);
        serviceHandler.sendMessage(obtain);
    }
}
