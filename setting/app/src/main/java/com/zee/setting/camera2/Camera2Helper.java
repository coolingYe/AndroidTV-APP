package com.zee.setting.camera2;

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
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class Camera2Helper {

    private static final String TAG = "Camera2Helper";
    public static final String CAMERA_ID_FRONT = "1";
    public static final String CAMERA_ID_BACK = "0";
    private String mCameraId;
    private String specificCameraId;
    private Camera2Listener camera2Listener;
    private TextureView mTextureView;
    private int rotation;
    private boolean isMirror;
    private Context mContext;
    public boolean runClassifier = false;
    public boolean useTextureUpdate = false;
    private Bitmap mBitmap = null;
    private static final int ANALYSIS_IMG_DEF_LONG_SIDE = 640;
    private int mImgWidth = ANALYSIS_IMG_DEF_LONG_SIDE;
    private int mImgHeight = ANALYSIS_IMG_DEF_LONG_SIDE;
    public boolean reverseLandscapeKeepOriginalPreview = false;

    private static final String HANDLE_THREAD_NAME = "CameraBackground";

    /**
     * A {@link CameraCaptureSession } for camera preview.
     */
    private CameraCaptureSession mCaptureSession;

    private CameraCharacteristics cameraCharacteristics;

    /**
     * A reference to the opened {@link CameraDevice}.
     */
    private CameraDevice mCameraDevice;

    private Size mPreviewSize;

    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private HandlerThread mBackgroundThread;

    /**
     * A {@link Handler} for running tasks in the background.
     */
    private Handler mBackgroundHandler;

    /**
     * A {@link Semaphore} to prevent the app from exiting before closing the camera.
     */
    private final Semaphore mCameraOpenCloseLock = new Semaphore(1);

    /**
     * Orientation of the camera sensor
     */
    private int mSensorOrientation;
    private final WindowManager windowManager;

    private Camera2Helper(Builder builder) {
        mTextureView = builder.previewDisplayView;
        mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        camera2Listener = builder.camera2Listener;
        //rotation = builder.rotation;
        mContext = builder.context;

        windowManager = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
        rotation = windowManager.getDefaultDisplay().getRotation();

        if (isMirror) {
            mTextureView.setScaleX(-1);
        }
    }

    public int getCameraOri(int rotation, String cameraId) {
        int degrees = rotation * 90;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
            default:
                break;
        }
        int result;
        if (CAMERA_ID_FRONT.equals(cameraId)) {
            result = (mSensorOrientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (mSensorOrientation - degrees + 360) % 360;
        }
        //Log.i(TAG, "getCameraOri: " + rotation + " " + result + " " + mSensorOrientation);
        camera2Listener.setRotAngle(result);
        return result;
    }

    public final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            Log.d(TAG, "onSurfaceTextureAvailable: width=" + width + ", height=" + height);
            if(camera2Listener != null){
                camera2Listener.textureAvailable();
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            Log.d(TAG, "onSurfaceTextureSizeChanged: width=" + width + ", height=" + height);
            /*if (!HardwareInfo.careHardware) {
                createCameraPreviewSession();
            }else{
                if(mCameraDevice != null) {
                    rotation = windowManager.getDefaultDisplay().getRotation();
                    configureTransform(width, height);
                }
            }*/

            if(mCameraDevice != null) {
                rotation = windowManager.getDefaultDisplay().getRotation();
                configureTransform(width, height);
            }

            if(camera2Listener != null){
                camera2Listener.textureSizeChanged(width, height);
            }

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            Log.d(TAG, "onSurfaceTextureDestroyed: ");
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
            if (!HardwareInfo.isZeeBox) {
                int currentRotation = windowManager.getDefaultDisplay().getRotation();
                if (currentRotation != rotation) {
                    rotation = currentRotation;
                    if (mCameraDevice != null) {
                        configureTransform(mTextureView.getWidth(), mTextureView.getHeight());
                        return;
                    }
                }
            }
            if(useTextureUpdate) {
                if (!inClassifyFrameTime) {
                    inClassifyFrameTime = true;
                    mBackgroundHandler.post(periodicClassify);
                }
            }
        }
    };

    private final CameraDevice.StateCallback mDeviceStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened( CameraDevice cameraDevice) {
            Log.i(TAG, "onOpened: ");
            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            createCameraPreviewSession();
            if (camera2Listener != null) {
                camera2Listener.onCameraOpened(cameraDevice, mCameraId, mPreviewSize, getCameraOri(rotation, mCameraId), isMirror);
            }
        }

        @Override
        public void onDisconnected( CameraDevice cameraDevice) {
            Log.i(TAG, "onDisconnected(): ");
            mCameraOpenCloseLock.release();

            if (cameraDevice == null) {
                Log.i(TAG, "onDisconnected(), cameraDevice is null.");
            } else {
                Log.i(TAG, "onDisconnected(), execute cameraDevice close()");
                if (camera2Listener != null) {
                    camera2Listener.onCameraInvalid();
                }
                cameraDevice.close();
                Log.i(TAG, "onDisconnected(), execute cameraDevice close() done");
            }

            mCameraDevice = null;
            if (camera2Listener != null) {
                camera2Listener.onCameraClosed();
            }
        }

        @Override
        public void onError( CameraDevice cameraDevice, int error) {
            Log.i(TAG, "onError: error=" + error);
            mCameraOpenCloseLock.release();

            if (cameraDevice == null) {
                Log.i(TAG, "onError(), cameraDevice is null.");
                mCameraDevice = null;
                if (camera2Listener != null) {
                    camera2Listener.onCameraError(new CameraErrorException("onError occurred", error));
                }
            }else{
                Log.i(TAG, "onError(), execute cameraDevice close()");
                mTextureView.postDelayed(cameraErrorRunnable, 4000);
                if (camera2Listener != null) {
                    camera2Listener.onCameraInvalid();
                }
                cameraDevice.close();//sometimes cost 4s
                Log.i(TAG, "onError(), execute cameraDevice close() done");
                mTextureView.removeCallbacks(cameraErrorRunnable);
                mCameraDevice = null;
                if (camera2Listener != null) {
                    camera2Listener.onCameraError(new CameraErrorException("onError occurred", error));
                }
            }
        }
    };

    private final Runnable cameraErrorRunnable = () -> {
        Log.i(TAG, "onError(), execute cameraErrorRunnable==========>>>>>>>>>");
        mCameraDevice = null;
        if (camera2Listener != null) {
            camera2Listener.onCameraError(new CameraErrorException("onError occurred", 4));
        }
    };

    public void reOpenCamera(String cameraId) {
        Log.i(TAG, "reOpenCamera() cameraId=" + cameraId);
        specificCameraId = cameraId;
        openCamera();
    }

    public void start(String cameraId) {
        if (mCameraDevice != null) {
            return;
        }

        startBackgroundThread();

        specificCameraId = cameraId;
        // When the screen is turned off and turned back on, the SurfaceTexture is already
        // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
        // a camera and start preview from here (otherwise, we wait until the surface is ready in
        // the SurfaceTextureListener).
        if (mTextureView.isAvailable()) {
            openCamera();
        }else{
            Log.d(TAG, "start mTextureView isAvailable false");
        }
    }

    public void stop() {

        if (mCameraDevice != null) {
            closeCamera();
        }

        if(mBitmap != null) {
            mBitmap.recycle();
            mBitmap = null;
        }

        stopBackgroundThread();
        Log.i(TAG, "camera stop done!");
    }

    public void release() {
        stop();
        mTextureView = null;
        camera2Listener = null;
        mContext = null;
    }

    private boolean setUpCameraOutputs(CameraManager cameraManager) {
        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                if (cameraId.equals(specificCameraId) && configCameraParams(cameraManager, specificCameraId)) {
                    return true;
                }
            }
            for (String cameraId : cameraManager.getCameraIdList()) {
                if (configCameraParams(cameraManager, cameraId)) {
                    return true;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
        }

        return false;
    }

    @SuppressLint("WrongConstant")
    private boolean configCameraParams(CameraManager manager, String cameraId) throws CameraAccessException {
        CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);

        StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
        if (map == null) {
            return false;
        }

        mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        mCameraId = cameraId;
        cameraCharacteristics = characteristics;
        return true;
    }

    public void reCreateCameraPreviewSession(){
        try {
            createCameraPreviewSession();
        }catch (Exception e){
            e.printStackTrace();
            Log.e(TAG, "reCreateCameraPreviewSession() err " + e);
        }
    }

    /**
     * Opens the camera specified by {@link #mCameraId}.
     */
    @SuppressLint("MissingPermission")
    private void openCamera() {
        Log.i(TAG, "openCamera() call");
        CameraManager cameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        boolean setUpResult = setUpCameraOutputs(cameraManager);
        if(!setUpResult){
            if (camera2Listener != null) {
                camera2Listener.onCameraError(new InvalidException("No support camera!"));
            }
            return;
        }
        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }

            cameraManager.openCamera(mCameraId, mDeviceStateCallback, mBackgroundHandler);
        } catch (CameraAccessException | InterruptedException e) {
            if (camera2Listener != null) {
                camera2Listener.onCameraError(e);
            }
        }
    }

    /**
     * Closes the current {@link CameraDevice}.
     */
    private void closeCamera() {
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCaptureSession) {
                Log.d(TAG, "closeCamera() captureSession close");
                mCaptureSession.close();
                mCaptureSession = null;
            }
            if (null != mCameraDevice) {
                Log.d(TAG, "closeCamera() cameraDevice close");
                mCameraDevice.close();
                mCameraDevice = null;
            }
            if (camera2Listener != null) {
                camera2Listener.onCameraClosed();
            }
        } catch (InterruptedException e) {
            if (camera2Listener != null) {
                camera2Listener.onCameraError(e);
            }
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    public CameraDevice getCameraDevice() {
        return mCameraDevice;
    }

    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread(HANDLE_THREAD_NAME);
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
        runClassifier = true;
        mBackgroundHandler.post(periodicClassify);
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        try {
            runClassifier = false;
            if(mBackgroundThread != null) {
                mBackgroundHandler.removeCallbacksAndMessages(null);
                mBackgroundThread.quitSafely();
                mBackgroundThread.join();
                mBackgroundThread = null;
                mBackgroundHandler = null;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private final Runnable periodicClassify =
            () -> {
                if (runClassifier) {
                    classifyFrameByPost();
                }else if(useTextureUpdate){
                    classifyFrameByUpdate();
                }
            };

    long lastTimestamp = 0;
    private void classifyFrameByPost() {
        if(mTextureView !=null && mTextureView.isAvailable()) {
            long currentTimestamp = mTextureView.getSurfaceTexture().getTimestamp();
            if (lastTimestamp == currentTimestamp) {
                mBackgroundHandler.post(periodicClassify);
                return;
            }

            lastTimestamp = currentTimestamp;
            handleFrameAnalysis();
        }

        mBackgroundHandler.post(periodicClassify);
    }

    boolean inClassifyFrameTime = false;
    private void classifyFrameByUpdate() {
        handleFrameAnalysis();
        inClassifyFrameTime = false;
    }

    private void handleFrameAnalysis() {
        if (rotation == Surface.ROTATION_0) {//phone portrait, top camera on top
            if (HardwareInfo.isZeeBox && mTextureView.getWidth() < mTextureView.getHeight()) {//landscape screen to preview portrait
                mBitmap = mTextureView.getBitmap(mImgHeight, mImgWidth);
                if (mBitmap != null) {
                    mBitmap = adjustBitmapRotationForRotation_0(mBitmap);
                    camera2Listener.method(mBitmap);
                }
            } else {
                mBitmap = mTextureView.getBitmap(mImgWidth, mImgHeight);
                if (mBitmap != null) {
                    camera2Listener.method(mBitmap);
                }
            }
        } else if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {
            if (reverseLandscapeKeepOriginalPreview) {//for Virtual shooting phone version
                mBitmap = mTextureView.getBitmap(mImgWidth, mImgHeight);
                if (mBitmap != null) {
                    camera2Listener.method(mBitmap);
                }
            } else {
                mBitmap = mTextureView.getBitmap(mImgHeight, mImgWidth);
                if (mBitmap != null) {
                    mBitmap = adjustBitmapRotation(mBitmap, rotation);
                    camera2Listener.method(mBitmap);
                }
            }
        } else {//phone reversePortrait, top camera on bottom
            mBitmap = mTextureView.getBitmap(mImgWidth, mImgHeight);
            if (mBitmap != null) {
                mBitmap = adjustBitmapRotation(mBitmap, rotation);
                camera2Listener.method(mBitmap);
            }
        }
    }

    private Bitmap adjustBitmapRotationForRotation_0(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.postRotate(-90, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);

        try {
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            return bitmap;
        } catch (OutOfMemoryError ex) {
            Log.e(TAG, "OutOfMemoryError " + ex);
        }
        return null;
    }

    private Bitmap adjustBitmapRotation(Bitmap bitmap, int rotation) {
        Matrix matrix = new Matrix();
        if (Surface.ROTATION_90 == rotation){
            matrix.postRotate(-90, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);
        } else if (Surface.ROTATION_270 == rotation) {
            matrix.postRotate(90, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);
        }

        try {
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            return bitmap;
        } catch (OutOfMemoryError ex) {
            Log.e(TAG, "OutOfMemoryError " + ex);
        }
        return null;
    }

    public void setZeePoseEnable(boolean isEnable){
        runClassifier = isEnable;
        if(mBackgroundHandler != null) {
            if (isEnable) {
                mBackgroundHandler.removeCallbacksAndMessages(null);
                mBackgroundHandler.post(periodicClassify);
            } else {
                mBackgroundHandler.removeCallbacksAndMessages(null);
            }
        }
    }


    public void setUseTextureUpdate(boolean isEnable){
        if(isEnable){
            setZeePoseEnable(false);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            useTextureUpdate = true;
        }else{
            useTextureUpdate = false;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            setZeePoseEnable(true);
        }
    }

    /**
     * Creates a new {@link CameraCaptureSession} for camera preview.
     */
    public void createCameraPreviewSession() {
        try {
            if(mCaptureSession != null){
                mCaptureSession.close();
                mCaptureSession = null;
            }

            rotation = windowManager.getDefaultDisplay().getRotation();

            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;

            StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            Size[] sizeArray = map.getOutputSizes(SurfaceTexture.class);
            for (Size size : sizeArray) {
                Log.d(TAG, "camera support size.getWidth()=" + size.getWidth() + ", size.getHeight()=" + size.getHeight());
            }

            /*mPreviewSize = CameraUtil.getBestSupportedSize(new ArrayList<Size>(Arrays.asList(map.getOutputSizes(SurfaceTexture.class))),
                mTextureView.getLayoutParams().width,
                mTextureView.getLayoutParams().height);*/
            if(mTextureView.getLayoutParams().width > mTextureView.getLayoutParams().height) {
                mPreviewSize = CameraUtil.getBestSupportedSize(map.getOutputSizes(SurfaceTexture.class),
                        mTextureView.getLayoutParams().width,
                        mTextureView.getLayoutParams().height);
            }else{
                mPreviewSize = CameraUtil.getBestSupportedSize(map.getOutputSizes(SurfaceTexture.class),
                        mTextureView.getLayoutParams().height,
                        mTextureView.getLayoutParams().width
                );
            }

            configureTransform(mTextureView.getLayoutParams().width, mTextureView.getLayoutParams().height);

            // We configure the size of default buffer to be the size of camera preview we want.
            Log.d(TAG,"mPreviewSize: w: " + mPreviewSize.getWidth() + " h: " + mPreviewSize.getHeight()
                    +", texture.setDefaultBufferSize w: " + mPreviewSize.getWidth() + " h: " + mPreviewSize.getHeight()
                    +", use mTextureView.getBitmap w: " + mImgWidth + " h: " + mImgHeight);
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

            // This is the output Surface we need to start preview.
            final Surface previewSurface = new Surface(texture);
            List<Surface> surfaceList = new ArrayList<>(1);
            surfaceList.add(previewSurface);

            // Here, we create a CameraCaptureSession for camera preview.
            mCameraDevice.createCaptureSession(surfaceList,
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                            Log.i(TAG, "onConfigured: ");
                            // The camera is already closed
                            if (null == mCameraDevice) return;

                            // When the session is ready, we start displaying the preview.
                            mCaptureSession = cameraCaptureSession;

                            try {
                                // We set up a CaptureRequest.Builder with the output Surface.
                                CaptureRequest.Builder requestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                                requestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                requestBuilder.addTarget(surfaceList.get(0));
                                mCaptureSession.setRepeatingRequest(requestBuilder.build(), new CameraCaptureSession.CaptureCallback() {}, mBackgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                                Log.e(TAG, "onConfigured: err " + e);
                                if (camera2Listener != null) {
                                    camera2Listener.onCameraError(new Exception("onConfigured"));
                                }
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession session) {
                            Log.e(TAG, "onConfigureFailed: ");
                            if (camera2Listener != null) {
                                camera2Listener.onCameraError(new Exception("onConfigureFailed"));
                            }
                        }
                    },
                    mBackgroundHandler
            );
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "createCameraPreviewSession: err " + e);
            if (camera2Listener != null) {
                camera2Listener.onCameraError(new Exception("onConfigured"));
            }
        }
    }

    /**
     * Configures the necessary {@link Matrix} transformation to `mTextureView`.
     * This method should be called after the camera preview size is determined in
     * setUpCameraOutputs and also the size of `mTextureView` is fixed.
     *
     * @param viewWidth  The width of `mTextureView`
     * @param viewHeight The height of `mTextureView`
     */
    private void configureTransform(int viewWidth, int viewHeight) {
        if (null == mTextureView || null == mPreviewSize) {
            return;
        }

        initImageSize();
        Log.d(TAG,"configureTransform(), use mTextureView.getBitmap w: " + mImgWidth + " h: " + mImgHeight);

        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation) {//phone landscape, top camera on left side
            matrix.postScale((float) viewHeight / viewWidth,
                    (float) viewWidth / viewHeight, centerX, centerY);
            matrix.postRotate(-90, centerX, centerY);
        } else if ( Surface.ROTATION_270 == rotation) {//phone reverseLandscape, top camera on right side
            if (reverseLandscapeKeepOriginalPreview) {
                //for Virtual shooting phone version
            } else {
                matrix.postScale((float) viewHeight / viewWidth,
                        (float) viewWidth / viewHeight, centerX, centerY);
                matrix.postRotate(90, centerX, centerY);
            }
        } else if (Surface.ROTATION_180 == rotation) {//phone reversePortrait, top camera on bottom
            matrix.postRotate(180, centerX, centerY);
        } else {//phone portrait, top camera on top
            if (HardwareInfo.isZeeBox && viewWidth < viewHeight) {//landscape screen to preview portrait
                matrix.postScale((float) viewHeight / viewWidth,
                        (float) viewWidth / viewHeight, centerX, centerY);
                matrix.postRotate(-90, centerX, centerY);
            }
        }
        Log.i(TAG, "configureTransform: " + getCameraOri(rotation, mCameraId) + "  " + rotation * 90 + "  " + matrix);
        mTextureView.post(() -> {
            if(mTextureView != null) {
                mTextureView.setTransform(matrix);
            }
        });
    }

    public void changeRotation(int rotation) {this.rotation = rotation;}

    public void setImgHeight(int imgHeight) {
        this.mImgHeight = imgHeight;
    }

    public void setImgWidth(int imgWidth) {
        this.mImgWidth = imgWidth;
    }

    /**
     * 初始化Bitmap的Size
     */
    private void initImageSize() {
        if (mTextureView.getHeight() > mTextureView.getWidth()) {
            mImgHeight = ANALYSIS_IMG_DEF_LONG_SIDE;
            mImgWidth = (int) (mImgHeight * (mTextureView.getWidth() * 1f / mTextureView.getHeight()));
        } else {
            mImgWidth = ANALYSIS_IMG_DEF_LONG_SIDE;
            mImgHeight = (int) (mImgWidth * (mTextureView.getHeight() * 1f / mTextureView.getWidth()));
        }
    }

    public int getImgWidth() {
        return mImgWidth;
    }

    public int getImgHeight() {
        return mImgHeight;
    }

    public static final class Builder {

        /**
         * 预览显示的view，目前仅支持textureView
         */
        private TextureView previewDisplayView;

        /**
         * 事件回调
         */
        private Camera2Listener camera2Listener;

        /**
         * 传入getWindowManager().getDefaultDisplay().getRotation()的值即可
         */
        private int rotation;

        /**
         * 上下文，用于获取CameraManager
         */
        private Context context;

        public Builder() { }

        public Builder previewOn(TextureView val) {
            previewDisplayView = val;
            return this;
        }

        public Builder rotation(int val) {
            rotation = val;
            return this;
        }

        public Builder cameraListener(Camera2Listener val) {
            camera2Listener = val;
            return this;
        }

        public Builder context(Context val) {
            context = val;
            return this;
        }

        public Camera2Helper build() {
            if (camera2Listener == null) {
                Log.e(TAG, "camera2Listener is null, callback will not be called");
            }
            if (previewDisplayView == null) {
                throw new RuntimeException("you must preview on a textureView or a surfaceView");
            }
            return new Camera2Helper(this);
        }
    }

}