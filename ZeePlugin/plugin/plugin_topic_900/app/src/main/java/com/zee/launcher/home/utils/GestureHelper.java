package com.zee.launcher.home.utils;

import static android.content.Context.WINDOW_SERVICE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
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
import android.hardware.camera2.params.OutputConfiguration;
import android.hardware.camera2.params.SessionConfiguration;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.GestureEstimate.Point3f;
import com.GestureEstimate.Point3fList;
import com.GestureEstimate.SimpleDynamicGesture;
import com.GestureEstimate.SlipEnum;
import com.zee.launcher.home.MainActivity;
import com.zee.launcher.home.gesture.zeewainpose.Config;
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

public class GestureHelper {
    private static final String TAG = "Camera2Test";
    private TextureView mTextureView;
    private CameraManager mCameraManager;
    private CameraDevice mCameraDevice;
    private String mCameraId = "0";
    private MainActivity mContext;
    private Size previewSize; // 用于设置预览的宽高
    String[] models = new ZeewainPose().getModelNameLists();
    private ZeewainPose zeewainPose = new ZeewainPose();
    private int imgWidth = 720;
    private int imgHeight = 960;
    private Bitmap mBitmap = null;
    private float scaleX = 0.3f;
    private float scaleY = 0.3f;
    private boolean runClassifier = false;
    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    private SimpleDynamicGesture simpleDynamicGesture = new SimpleDynamicGesture();
    private Point3fList handLeftPointList = new Point3fList();
    private Point3f handLeftPoint3f = new Point3f();
    private Point3fList handRightPointList = new Point3fList();
    private Point3f handRightPoint3f = new Point3f();
    private Point3fList bodyPointList = new Point3fList();
    private Point3f bodyPoint3f = new Point3f();
    private Handler handler = new Handler();


    public GestureHelper(MainActivity context, TextureView textureView) {
        mContext = context;
        intiView(textureView);
        Log.e(TAG, "onInit");
        copyModelFromAssetsToData();
        releaseCamera();
        Log.i("wang", "onCreat_releaseCamera");
        initData();
    }

    public void startGesture() {
        setAuthAlgorithm();
        initEvent();
        if (mTextureView.isAvailable()) {
            initCamera();
        }
    }


    private void setAuthAlgorithm() {
        //授权成功返回0,失败返回其他
        int ret = zeewainPose.setAuthOnline(Config.ak, Config.sk, Config.url, Config.licensePath);
        if (ret != 0) {
            Log.d(TAG, "在线授权设置失败...");
            return;
        }
        // 打开图像裁剪功能
        zeewainPose.setDynamicCropStatus(true);
        Log.d(TAG, "开始进行算法初始化...");
        initAlgorithm();
        Log.d(TAG, "算法初始化完成...");
    }

    private void initAlgorithm() {
        String modelDir = mContext.getFilesDir() + File.separator;
        int deviceType = 1;  //0:CPU 1:OpenCL 2:metal 3:OpenGl 4：Vulkan
        int threadNum = 2;
        int modelType = 1; //0表示17个骨骼关键点， 1表示29个骨骼关键点, 2表示33个骨骼关键点

        ZwnConfig zwnConfig = new ZwnConfig(modelDir, deviceType, threadNum, modelType);
        int ret = zeewainPose.initHolistic(zwnConfig);
        if (ret != 0) {
            Toast.makeText(mContext, "failed to init zeepose", Toast.LENGTH_LONG).show();
            mContext.finish();
        } else {
            Toast.makeText(mContext, "success to init zeepose", Toast.LENGTH_LONG).show();
        }
    }


    private void intiView(TextureView textureView) {
        mTextureView = textureView;
    }

    private void initData() {
        mCameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        if (mCameraManager == null) {
            Toast.makeText(mContext, "获取不到CameraService对象！", Toast.LENGTH_LONG).show();
            mContext.finish();
        }
    }

    public void initEvent() {
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
        getCameraIdAndPreviewSizeByFacing(CameraCharacteristics.LENS_FACING_FRONT); // 0为前置摄像头,Camera api1里面定义0为后置
        // 3.打开相机
        openCamera();
    }


    /*获取cameraId及相机预览的最佳尺寸*/
    private void getCameraIdAndPreviewSizeByFacing(int lensFacingFront) {
        Log.i(TAG, "getCameraIdAndPreviewSizeByFacing");
        try {
            String[] cameraIdList = mCameraManager.getCameraIdList(); //如果设备节点不可用，会阻塞在这里
            Log.i(TAG, "getCameraIdAndPreviewSizeByFacing cameraIdList = " + Arrays.toString(cameraIdList));
            for (String cameraId : cameraIdList) {
                CameraCharacteristics cameraCharacteristics = mCameraManager.getCameraCharacteristics(cameraId);
                // 默认打开后置摄像头 - 忽略前置摄像头
                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT)
                    continue;
                int deviceLevel = cameraCharacteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL); //硬件与api2的契合度，0-4
                Log.i(TAG, "deviceLevel = " + deviceLevel);
                StreamConfigurationMap streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                Size[] outputSizes = streamConfigurationMap.getOutputSizes(SurfaceTexture.class);
                mCameraId = cameraId;
                previewSize = setOptimalPreviewSize(outputSizes, mTextureView.getMeasuredWidth(), mTextureView.getMeasuredHeight());
                Log.i(TAG, "最佳预览尺寸（w-h）：" + previewSize.getWidth() + "-" + previewSize.getHeight() + ",相机id：" + mCameraId);

            }
        } catch (CameraAccessException e) {
            Log.e(TAG, "getCameraIdAndPreviewSizeByFacing error = " + e.getMessage());
        }
    }


    /**
     * 打开相机，预览是在回调里面执行的。
     */
    @SuppressLint("MissingPermission")
    private void openCamera() {
        Log.i(TAG, "openCamera");
        transformImage(mTextureView.getWidth(), mTextureView.getHeight());
        try {
            mCameraManager.openCamera(mCameraId, mStateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
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
                    return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                            (long) rhs.getWidth() * rhs.getHeight());
                }
            });
        } else if (notBigEnoughSizes.size() > 0) {
            return Collections.max(notBigEnoughSizes, new Comparator<Size>() {
                @Override
                public int compare(Size lhs, Size rhs) {
                    return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                            (long) rhs.getWidth() * rhs.getHeight());
                }
            });
        } else {
            Log.e(TAG, "未找到合适的预览尺寸");
            return sizes[0];
        }
    }

    //关闭相机，释放对象
    private void releaseCamera() {
        Log.e("wang", "releaseCamera: ");
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
        @RequiresApi(api = Build.VERSION_CODES.P)
        @Override
        public void onOpened(CameraDevice camera) {
            Log.i(TAG, "StateCallback！ onOpened");
            mCameraDevice = camera; // 打开成功，保存代表相机的CameraDevice实例
            SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(mTextureView.getWidth(), mTextureView.getHeight());
            Surface surface = new Surface(surfaceTexture);
            ArrayList<Surface> previewList = new ArrayList<>();
            previewList.add(surface);
            //将预览控件和预览尺寸比例保持一致，避免拉伸
            DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
            int width = dm.widthPixels;
            int height = dm.heightPixels;
            ViewGroup.LayoutParams layoutParams = mTextureView.getLayoutParams();
            layoutParams.width = (int) (width * scaleX);
            layoutParams.height = (int) (height * scaleY);
            mTextureView.setLayoutParams(layoutParams);
            try {
                // 6.将TextureView的surface传递给CameraDevice
                SessionConfiguration sessionConfiguration = new SessionConfiguration(SessionConfiguration.SESSION_REGULAR,
                        Collections.singletonList(new OutputConfiguration(surface)),
                        mContext.getMainExecutor(),
                        new CameraCaptureSession.StateCallback() {
                            @Override
                            public void onConfigured(@NonNull CameraCaptureSession session) {
                                try {
                                    CaptureRequest.Builder builder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                                    builder.addTarget(surface); // 必须设置才能正常预览
                                    CaptureRequest captureRequest = builder.build();
                                    session.setRepeatingRequest(captureRequest, mSessionCaptureCallback, mBackgroundHandler);
                                } catch (CameraAccessException e) {
                                    Log.e(TAG, "createCaptureRequest error = " + e.getMessage());
                                }
                            }

                            @Override
                            public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                                Log.e(TAG, "onConfigureFailed");
                            }
                        });
                mCameraDevice.createCaptureSession(sessionConfiguration);
            } catch (CameraAccessException e) {
                Log.e(TAG, "createCaptureSession error = " + e.getMessage());
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            Log.e(TAG, "StateCallback！ onDisconnected camera.getId() = " + camera.getId());
            releaseCamera();
            Log.i("wang", "onDisconnected_releaseCamera");
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.e(TAG, "StateCallback camera.getId() = " + camera.getId() + " , error = " + error);
            Log.i("wang", "onError_releaseCamera");
            releaseCamera();
        }

        @Override
        public void onClosed(@NonNull CameraDevice camera) {

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


    public void destroy() {
        releaseCamera();
        handler.removeCallbacksAndMessages(null);
        Log.i("wang", "onDestroy_releaseCamera");
    }

    public void close() {
        releaseCamera();
        handler.removeCallbacksAndMessages(null);
        Log.i("wang", "CameraClose");
    }

    public void reStart() {
        initCamera();
    }

    /****************************zeepose**********************************/
    protected void copyModelFromAssetsToData() {
        // assets目录下的模型文件名
        try {
            for (String model : models) {
                copyAssetFileToFiles(mContext, model);
            }
            Toast.makeText(mContext, "Copy model Success", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mContext, e.toString(), Toast.LENGTH_SHORT).show();
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
        Log.i(TAG, "holistic costTime:" + costTime + " ms.");
        if (holisticInfo != null && holisticInfo.poseInfo != null && holisticInfo.poseInfo.landmarks != null) {
            bodyPointList.clear();
            for (int i = 0; i < holisticInfo.poseInfo.landmarks.length; ++i) {
                Point2f pt = holisticInfo.poseInfo.landmarks[i];
                bodyPoint3f.setX(pt.x);
                bodyPoint3f.setY(pt.y);
                bodyPoint3f.setZ(0);
                bodyPointList.add(bodyPoint3f);
            }
        }

        if (holisticInfo != null && holisticInfo.leftHandInfo != null && holisticInfo.leftHandInfo.landmarks != null) {
            handLeftPointList.clear();
            Log.i(TAG, "holisticInfo.leftHandInfo.landmarks.length:" + holisticInfo.leftHandInfo.landmarks.length);
            for (int i = 0; i < holisticInfo.leftHandInfo.landmarks.length; ++i) {
                Point2f pt = holisticInfo.leftHandInfo.landmarks[i];
                handLeftPoint3f.setX(pt.x);
                handLeftPoint3f.setY(pt.y);
                handLeftPoint3f.setZ(0);
                handLeftPointList.add(handLeftPoint3f);
            }
            if (bodyPointList.size() > 0 && handLeftPointList.size() > 0) {
                SlipEnum slipEnum = simpleDynamicGesture.OnSlipV2LeftHand(handLeftPointList, bodyPointList);
                Log.e("wang", "left_slipEnum=" + slipEnum);
                if (slipEnum.equals(SlipEnum.SlipUp)) {
                    String data = slipEnum.toString();
                    Log.e("wang", "left_slipEnum=" + data);
                    runClassifier = false;
                    postDataToService("left");
                    return;
                }
            }
        }
        if (holisticInfo != null && holisticInfo.rightHandInfo != null && holisticInfo.rightHandInfo.landmarks != null) {
            handRightPointList.clear();
            Log.i(TAG, "holisticInfo.rightHandInfo.landmarks.length:" + holisticInfo.rightHandInfo.landmarks.length);
            for (int i = 0; i < holisticInfo.rightHandInfo.landmarks.length; ++i) {
                Point2f pt = holisticInfo.rightHandInfo.landmarks[i];
                handRightPoint3f.setX(pt.x);
                handRightPoint3f.setY(pt.y);
                handRightPoint3f.setZ(0);
                handRightPointList.add(handRightPoint3f);
            }
            if (bodyPointList.size() > 0 && handRightPointList.size() > 0) {
                SlipEnum slipEnum = simpleDynamicGesture.OnSlipV2RightHand(handRightPointList, bodyPointList);
                Log.e("wang", "right_slipEnum=" + slipEnum);
                if (slipEnum.equals(SlipEnum.SlipUp)) {
                    String data = slipEnum.toString();
                    Log.e("wang", "right_slipEnum=" + slipEnum);
                    runClassifier = false;
                    postDataToService("right");
                }
            }
        }

    }

    private void postDataToService(String data) {
        Log.e("wang", "postDataToService: " + data);
    }

    /****************************gesture**********************************/


    private void transformImage(int width, int height) {

        if (mTextureView == null) {

            return;

        } else try {
            {
                Matrix matrix = new Matrix();

                WindowManager windowManager = (WindowManager) mContext.getSystemService(WINDOW_SERVICE);

                int rotation = windowManager.getDefaultDisplay().getRotation();

                RectF textureRectF = new RectF(0, 0, width, height);

                RectF previewRectF = new RectF(0, 0, mTextureView.getHeight(), mTextureView.getWidth());

                float centerX = textureRectF.centerX();

                float centerY = textureRectF.centerY();

                if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {

                    previewRectF.offset(centerX - previewRectF.centerX(), centerY - previewRectF.centerY());

                    matrix.setRectToRect(textureRectF, previewRectF, Matrix.ScaleToFit.FILL);

                    float scale = Math.max((float) width / width, (float) height / width);

                    matrix.postScale(scale, scale, centerX, centerY);

                    matrix.postRotate(90 * (rotation - 2), centerX, centerY);

                }

                mTextureView.setTransform(matrix);

            }

        } catch (Exception e) {

            e.printStackTrace();

        }

    }


    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
        runClassifier = true;
        mBackgroundHandler.post(periodicClassify);
    }


    private void classifyFrame() {
        mBitmap = null;
        mBitmap = mTextureView.getBitmap(imgWidth, imgHeight);
        if (mBitmap != null) {
            holisticMethod(mBitmap);
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

}
