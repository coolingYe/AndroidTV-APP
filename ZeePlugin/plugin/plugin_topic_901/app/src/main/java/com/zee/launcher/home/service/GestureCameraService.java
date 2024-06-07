package com.zee.launcher.home.service;

import android.app.Instrumentation;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.GestureEstimate.GestureEstimate;
import com.GestureEstimate.Hand;
import com.GestureEstimate.HandList;
import com.GestureEstimate.HandPoseEnum;
import com.GestureEstimate.KptVersion;
import com.GestureEstimate.OptionalPoint2f;
import com.GestureEstimate.Point3f;
import com.GestureEstimate.Point3fList;
import com.GestureEstimate.SimpleDynamicGesture;
import com.GestureEstimate.SimpleStaticGesture;
import com.GestureEstimate.SlipEnum;
import com.GestureEstimate.ZwnHorizontalWaveConfig;
import com.GestureEstimate.ZwnVerticalWaveConfig;
import com.GestureEstimate.ZwnWaveCommonConfig;
import com.google.gson.Gson;
import com.zee.launcher.home.R;
import com.zee.launcher.home.gesture.HardwareInfo;
import com.zee.launcher.home.gesture.LocationEnum;
import com.zee.launcher.home.gesture.Overlay;
import com.zee.launcher.home.gesture.camera2.Camera2Helper;
import com.zee.launcher.home.gesture.camera2.Camera2Listener;
import com.zee.launcher.home.gesture.camera2.InvalidException;
import com.zee.launcher.home.gesture.config.Config;
import com.zee.launcher.home.gesture.model.AkSkResp;
import com.zee.launcher.home.gesture.model.ZeePoseWrapper;
import com.zee.launcher.home.gesture.utils.CameraBean;
import com.zee.launcher.home.gesture.utils.CommonUtils;
import com.zee.launcher.home.gesture.utils.DensityUtils;
import com.zee.launcher.home.gesture.utils.MediaManager;
import com.zeewain.base.utils.ToastUtils;
import com.zeewain.zeepose.HolisticInfo;
import com.zeewain.zeepose.PoseInfo;
import com.zeewain.zeepose.ZeewainPose;
import com.zeewain.zeepose.ZwnConfig;
import com.zeewain.zeepose.base.Point2f;
import com.zwn.launcher.host.BuildConfig;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GestureCameraService extends Service implements Camera2Listener {
    static {
        System.loadLibrary("GestureEstimate");
    }

    public static final int classAndPluginProgress = 29;
    public static final int backProgress = 14;
    public boolean isHaveCamera;
    private View mFloatView;
    private WindowManager windowManager;
    private GestureCameraService.ServiceBinder serviceBinder = new GestureCameraService.ServiceBinder();
    private GestureListener gestureListener;
    public final String TAG = "GestureCameraService";
    private LayoutInflater inflater;
    public boolean isDrawOverlayPoint = true;
    private String[] cameraIdList;
    private Handler handler = new Handler(Looper.myLooper());
    private WindowManager.LayoutParams layoutParams;
    private int rotation = 0;     // 屏幕旋转方向
    private int screenWidth = 0;
    private int screenHeight = 0;
    private float scaleX = 0.375f;
    private float scaleY = 0.375f;
    private int initZeePoseResult = -1;
    public final ZeewainPose zeewainPose = new ZeewainPose();
    private String modelsDirPath;


    private int initZeeHolisticResult;
    private HolisticInfo holisticInfo;
    private final List<LocationEnum> locationEnumList = new ArrayList<>();

    public Camera2Helper camera2Helper;

    private TextureView textureView;
    private Overlay overlay;
    private Hand poseHandType = Hand.Unkonwn;
    private HandPoseEnum leftHandType = HandPoseEnum.Unkonwn;
    private HandPoseEnum rightHandType = HandPoseEnum.Unkonwn;
    private SlipEnum lastLeftSlipEnum = SlipEnum.Unkonwn;
    private SlipEnum lastRightSlipEnum = SlipEnum.Unkonwn;
    private SlipEnum leftSlip = SlipEnum.Unkonwn;
    private SlipEnum rightSlip = SlipEnum.Unkonwn;

    private String lastUsedCameraId;

    private int checkState = 0;  // 0 没有人  1 鹤龙街道 2 健身
    private long holderTime = 0;
    private long mainHolderTime = 0;
    private long pageTurnHolderTime = 0;
    private long fitnessHolderTime = 0;
    private Point3f bodyPoint3f = new Point3f();
    private Point3fList bodyPointList = new Point3fList();
    private Point3fList handLeftPointList = new Point3fList();
    private Point3fList handRightPointList = new Point3fList();
    private Point3f handLeftPoint3f = new Point3f();
    private Point3f handRightPoint3f = new Point3f();
    private SimpleStaticGesture staticHandPoseDetector = new SimpleStaticGesture();
    private SimpleDynamicGesture simpleDynamicGesture = new SimpleDynamicGesture();
    private SimpleStaticGesture simpleStaticGesture = new SimpleStaticGesture();
    private PoseInfo[] poseInfoOverlayArray;
    private PoseInfo[] poseInfoOrgArray;
    private PoseInfo[] poseInfoArray;
    private PoseInfo shoulderPoseInfo;
    private boolean hasPersonInRect = false;
    private boolean hasPersonHand = false;
    private int synTrackId = 0;
    private boolean isLeftSlipIng = false;
    private boolean isRightSlipIng = false;
    private ExecutorService cachedThreadPool;


    private boolean isRightDynamicGesture = true;
    private boolean isUpDynamicGesture = true;
    private boolean isDownDynamicGesture = true;


    private boolean isLeftHandDynamicGesture = true;
    private boolean isRightHandDynamicGesture = true;


    private long lastLeftHandGestureTime = 0;
    private long lastRightHandGestureTime = 0;
    private long currentGestureTime = 0;


    private long mainGestureTime = 1000;  // 静态手势持续时间 ms
    private long otherGestureTime = 1000;  // 静态手势持续时间 ms
    private long outGestureTime = 800;  // 离开区域丢失持续时间 ms

    private long isDynamicGestureTime = 1000;  // 动态手势单次暂停时间 ms

    private long EnableLeftGestureTime = 1000;   // 反向手势控制时间 ms
    private long EnableRightGestureTime = 1000;
    public int cameraState = Config.CameraError_UnKNOW;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "onBind: ");
        getCameraList();
        if (isHaveCamera) {
            initUi();
        } else {
            Log.e(TAG, "没有找到摄像头: ");
        }
        return this.serviceBinder;
    }

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate: ");
        DensityUtils.autoWidth(getApplication(), this);
        initLayoutPar();
        super.onCreate();

    }

    private void initLayoutPar() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        layoutParams = new WindowManager.LayoutParams();
        inflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.gravity = Gravity.TOP;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        int width = getResources().getDisplayMetrics().widthPixels;
        layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        layoutParams.width = (int) (640 * scaleX);
        layoutParams.height = (int) (360 * scaleY);
        Log.e(TAG, "initLayoutPar: width " + layoutParams.width + " height" + layoutParams.height);
        layoutParams.x = width - layoutParams.width;
        layoutParams.y = 0;
        layoutParams.alpha = 1;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand: ");
        return super.onStartCommand(intent, flags, startId);
    }


    private void getCameraList() {
        try {
            CameraManager mCameraManager = (CameraManager) GestureCameraService.this.getSystemService(Context.CAMERA_SERVICE);
            cameraIdList = mCameraManager.getCameraIdList();
            if (cameraIdList != null) {
                if (cameraIdList.length > 0) {
                    isHaveCamera = true;
                } else {
                    isHaveCamera = false;
                }
            } else {
                isHaveCamera = false;
            }
        } catch (Exception ex) {

        }
    }

    private void initUi() {
        if (!Settings.canDrawOverlays(GestureCameraService.this)) {
            Toast.makeText(this, "can not DrawOverlays", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + GestureCameraService.this.getPackageName()));
            this.startActivity(intent);
        } else {
            showWindow();
        }
    }

    private void showWindow() {
        mFloatView = inflater.inflate(R.layout.service_gesture_camera, null);
        windowManager.addView(mFloatView, layoutParams);
        initView(mFloatView);
        initService();
    }


    private void initService() {
        initSize();
        HardwareInfo.init();
        Log.e(TAG, "onCreate: " + rotation + " " + screenWidth + " " + screenHeight + " " + HardwareInfo.careHardware);
        copyModel();
        boolean isAuth = authZeeWainPose();
        if (isAuth) {
            Log.i(TAG, "zeewainPose version " + zeewainPose.getVersionInfo());
            initPersonAndHolisticPose(0, 0, 2);
            initEvent();
        } else {
            stopSelf();
        }
    }

    private void initView(View view) {
        textureView = view.findViewById(R.id.textureView);
        overlay = view.findViewById(R.id.overlay);
        overlay.initOverlay(LocationEnum.PERSONPOSE);
        rotation = windowManager.getDefaultDisplay().getRotation();
    }

    /**
     * @param modelType  0表示17个骨骼关键点， 1表示29个骨骼关键点, 2表示33个骨骼关键点
     * @param deviceType 0:CPU 1:OpenCL 2:metal 3:OpenGl 4：Vulkan
     * @param threadNum  线程数
     * @return initPersonPose result
     */
    public void initPersonAndHolisticPose(int modelType, int deviceType, int threadNum) {
        GestureEstimate.set_kpt_version(KptVersion.KPT_17);
        staticHandPoseDetector.SetStaicHandPoseUpWardAngle(0, 45);

        ZwnWaveCommonConfig zwnWaveCommonConfig = new ZwnWaveCommonConfig();
        zwnWaveCommonConfig.setFrame_count(25);
        simpleDynamicGesture.SetSlipV3WristCommonWaveParam(zwnWaveCommonConfig);

        ZwnVerticalWaveConfig zwnVerticalWaveConfig = new ZwnVerticalWaveConfig();
        zwnVerticalWaveConfig.setMove_vertical_thres(0.4f);
        zwnVerticalWaveConfig.setMove_horizontal_slope_thres(0.5f);
        zwnVerticalWaveConfig.setUp_recognition_wrist_shoulder_thres(0.1f);
        zwnVerticalWaveConfig.setDown_recognition_wrist_shoulder_thres(0.6f);
        zwnVerticalWaveConfig.setUp_down_need_move_pass(true);
        zwnVerticalWaveConfig.setUp_down_no_move_delay_count(5);
        zwnVerticalWaveConfig.setUp_down_action_no_move_thres(0.5f);
        simpleDynamicGesture.SetSlipV3WristVerticalWaveParam(zwnVerticalWaveConfig);

        ZwnHorizontalWaveConfig zwnHorizontalWaveConfig = new ZwnHorizontalWaveConfig();
        zwnHorizontalWaveConfig.setMove_horizontal_thres(0.2f);
        zwnHorizontalWaveConfig.setMove_vertical_slope_thres(0.4f);
        zwnHorizontalWaveConfig.setMove_pass_inside_wrist_shoulder_thres(0.5f);
        zwnHorizontalWaveConfig.setMove_pass_outside_wrist_shoulder_thres(0.5f);
        simpleDynamicGesture.SetSlipV3WristHorizontalWaveParam(zwnHorizontalWaveConfig);


        ZwnConfig zwnConfig = new ZwnConfig(modelsDirPath, deviceType, threadNum, modelType);
        initZeePoseResult = zeewainPose.initPose(zwnConfig);
        initZeeHolisticResult = zeewainPose.initHolistic(zwnConfig);
        Log.e(TAG, "initPersonPose: state" + initZeePoseResult);
        Log.e(TAG, "initZeeHolistic: state" + initZeeHolisticResult);
        // 关闭脸部检测
        if (initZeeHolisticResult == 0) zeewainPose.setHolisticFaceLandmarkStatus(false);
        addLocationEnum(LocationEnum.PERSONPOSE);
        addLocationEnum(LocationEnum.HOLISTIC);
        zeewainPose.setDynamicCropStatus(true);
    }

    private void addLocationEnum(LocationEnum locationEnum) {
        if (initZeePoseResult == 0 && locationEnum == LocationEnum.PERSONPOSE) {
            for (int i = 0; i < locationEnumList.size(); i++) {
                if (locationEnumList.get(i) == locationEnum) {
                    locationEnumList.remove(i);
                    break;
                }
            }
            locationEnumList.add(locationEnum);
        } else if (initZeeHolisticResult == 0 && locationEnum == LocationEnum.HOLISTIC) {
            for (int i = 0; i < locationEnumList.size(); i++) {
                if (locationEnumList.get(i) == locationEnum) {
                    locationEnumList.remove(i);
                    break;
                }
            }
            locationEnumList.add(locationEnum);
        }
    }

    /**
     * 拷贝模型
     */
    private void copyModel() {
        if (modelsDirPath == null || modelsDirPath.isEmpty()) {
            modelsDirPath = getFilesDir() + File.separator;
            boolean isDone = CommonUtils.copyFilesFromAssetsTo(this, zeewainPose.getModelNameLists(), modelsDirPath);
            if (!isDone) {
                Log.e(TAG, "拷贝模型失败！");
//                showToast("拷贝模型失败");
                stopSelf();
            }
        }
    }


    /**
     * 算法在线授权
     */
    public boolean authZeeWainPose() {
        Log.e(TAG, "use default config info auth");
        String ak = "";
        String sk = "";
        String url = "";
        if (BuildConfig.FLAVOR == "plugin") {
            AkSkResp akSkCode = CommonUtils.getAkSkCode(this);
            if (akSkCode != null) {
                if (!TextUtils.isEmpty(akSkCode.akCode)) {
                    ak = akSkCode.akCode;
                }
                if (!TextUtils.isEmpty(akSkCode.skCode)) {
                    sk = akSkCode.skCode;
                }
            }
            if (!TextUtils.isEmpty(CommonUtils.getAuthUrl(this))) {
                url = CommonUtils.getAuthUrl(this);
            }
        } else {
            ak = Config.ak;
            sk = Config.sk;
            url = Config.url;
        }
        int isDone = -1;
        String licenseFile = getExternalFilesDir("").getAbsolutePath() + File.separator + "zeewain";
        isDone = zeewainPose.setAuthOnline(ak, sk, url, licenseFile);
        if (isDone != 0) {
            showToast("在线授权失败！");
            return false;
        } else {
            return true;
        }
    }

    /**
     * 获取屏幕真实宽高
     */
    void initSize() {
        DisplayMetrics metrics = getApplicationContext().getResources().getDisplayMetrics();
        screenHeight = metrics.heightPixels;
        screenWidth = metrics.widthPixels;
    }

    private void initEvent() {
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
                Log.i(TAG, "onSurfaceTextureAvailable width = " + width + ",height = " + height);
                //1、当SurefaceTexture可用的时候，设置相机参数并打开相机
                if (isHaveCamera) {
                    initCamera();
                }
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
        Log.i(TAG, "initCamera");
        camera2Helper = new Camera2Helper.Builder().cameraListener(this).context(getApplicationContext()).previewOn(textureView).rotation(rotation).build();
        lastUsedCameraId = cameraIdList[0];//默认选中第一个摄像头
        String cameraCache = CommonUtils.getCameraId(this);
        if (!TextUtils.isEmpty(cameraCache)) {
            CameraBean cameraBean = new Gson().fromJson(cameraCache, CameraBean.class);
            String cameraIdCache = cameraBean.getCameraId();
            for (String cameraId : cameraIdList) {
                if (cameraIdCache.equals(cameraId)) {
                    lastUsedCameraId = cameraCache;
                    break;
                }
            }
        }
        if (lastUsedCameraId != null && camera2Helper != null && camera2Helper.getCameraDevice() == null && textureView.isAvailable()) {
            camera2Helper.start(lastUsedCameraId);
        }
    }


    @Override
    public void onCameraOpened(CameraDevice cameraDevice, String cameraId, Size previewSize, int displayOrientation, boolean isMirror) {
        lastUsedCameraId = cameraId;
        cameraState = Config.CameraError_NORMAL;
        Log.i(TAG, "onCameraOpened() previewSize width=" + previewSize.getWidth() + ", height=" + previewSize.getHeight() + ", textureView width=" + textureView.getWidth() + ", height=" + textureView.getHeight() + ", algorithm analysis width=" + camera2Helper.getImgWidth() + ", height=" + camera2Helper.getImgHeight());
    }

    @Override
    public void onCameraClosed() {
        Log.i(TAG, "onCameraClosed() *** Camera Closed *** ");
    }

    @Override
    public void onCameraDisconnect() {
        Log.i(TAG, "onCameraDisconnect() *** Camera Disconnect *** ");
        handleCameraErrExit(Config.CameraError_INVALID);
    }

    @Override
    public void onCameraError(Exception e) {
        Log.i(TAG, "onCameraError() *** Camera Error ***");
        if (e instanceof InvalidException) {
//                showToast("未找到可使用的摄像头！");
            handleCameraErrExit(Config.CameraError_INVALID);
        } else {
//                showToast("摄像头异常，请重新插拔USB摄像头或者重启设备！");
            handleCameraErrExit(Config.CameraError_ERROR);
        }

    }

    @Override
    public void method(Bitmap bitmap) {
        personPoseMethod(bitmap);
    }

    @Override
    public void setRotAngle(int result) {

    }

    @Override
    public void textureAvailable() {

    }

    @Override
    public void textureSizeChanged(int width, int height) {

    }

    MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            Log.e(TAG, "onCompletion: " + "音频播放完成了");
        }
    };

    private void personPoseMethod(Bitmap bitmap) {
        if (checkState == 0)  // 首页
        {
            long startTime = System.currentTimeMillis();
            poseInfoOrgArray = zeewainPose.getPoseInfo(bitmap);
//            long endTime = System.currentTimeMillis();
//            long costTime = endTime - startTime;
//            Log.i(TAG, "checkState: " + checkState + "  costTime:" + costTime + " ms.");
            if (poseInfoOrgArray != null) {
                checkMainHandUp(startTime);
            } else {
                overlay.clear();
                shoulderPoseInfo = null;
                synTrackId = 0;
            }
        } else if (checkState == 1) {
            long startTime = System.currentTimeMillis();
            holisticInfo = zeewainPose.GetTrackIDHolisticInfo(bitmap, synTrackId);
            long endTime = System.currentTimeMillis();
            if (holisticInfo != null && holisticInfo.getPoseInfo().getTrackId() == synTrackId) {
                // 跟踪成功
                PoseInfo poseInfo = holisticInfo.getPoseInfo();
                if (isDrawOverlayPoint) {
                    handleTransformPoseInfoResult(transformPose(new PoseInfo[]{poseInfo}));
                }
//                Log.e(TAG, "跟踪成功 CheckState : " + checkState + "  holderTime : " + holderTime + " synTrackId: " + synTrackId);
                holderTime = 0;
                checkTurnPageGesture(holisticInfo, startTime);
            } else {
                long costTime = endTime - startTime;
                // 跟踪失败
                holderTime = holderTime + costTime;
//                Log.e(TAG, "跟踪失败 CheckState : " + checkState + "  holderTime : " + holderTime + " synTrackId: " + synTrackId + "");
                if (holderTime > outGestureTime) {
                    // 重新锁定人 有人的情况
                    if (holisticInfo != null) {
                        synTrackId = holisticInfo.poseInfo.trackId;
                        holderTime = 0;
                        Log.e("synTrackId", "重新锁定成功: ");
                    } else  // 没有人的情况
                    {
                        Log.e("synTrackId", "操作内没有人: ");
                        holderTime = outGestureTime;
                    }
                }
            }
        } else if (checkState == 2) {
            long startTime = System.currentTimeMillis();
            holisticInfo = zeewainPose.GetTrackIDHolisticInfo(bitmap, synTrackId);
            long endTime = System.currentTimeMillis();

            if (holisticInfo != null && holisticInfo.getPoseInfo().getTrackId() == synTrackId) {
                // 跟踪成功
                PoseInfo poseInfo = holisticInfo.getPoseInfo();
                if (isDrawOverlayPoint) {
                    handleTransformPoseInfoResult(transformPose(new PoseInfo[]{poseInfo}));
                }
//                Log.e(TAG, "跟踪成功 CheckState : " + checkState + "  holderTime : " + holderTime + " synTrackId: " + synTrackId);
                holderTime = 0;
                checkFitnessGesture(holisticInfo, startTime);
            } else {
                long costTime = endTime - startTime;
                // 跟踪失败
                holderTime = holderTime + costTime;
                Log.e(TAG, "跟踪失败 CheckState : " + checkState + "  holderTime : " + holderTime + " synTrackId: " + synTrackId + "");
                if (holderTime > outGestureTime) {
                    // 重新锁定人 有人的情况
                    if (holisticInfo != null) {
                        synTrackId = holisticInfo.poseInfo.trackId;
                        holderTime = 0;
                        Log.e(TAG, "重新锁定成功: ");
                    } else  // 没有人的情况
                    {
                        Log.e(TAG, "操作内没有人: ");
                        holderTime = outGestureTime;
                    }
                }
            }
        }
    }

    private void checkMainHandUp(long startTime) {
        boolean holder = false;
        int tempSynTrackId = -1;
        boolean hasLastPose = false;
        for (PoseInfo poseInfo : poseInfoOrgArray) {
            // 判读有没有举手
            bodyPointList.clear();
            for (int j = 0; j < poseInfo.landmarks.length; ++j) {
                Point2f pt = poseInfo.landmarks[j];
                bodyPoint3f.setX(pt.x);
                bodyPoint3f.setY(pt.y);
                bodyPoint3f.setZ(0);
                bodyPointList.add(bodyPoint3f);
            }
            // 初始状态
            if (poseHandType.equals(Hand.Unkonwn)) {
                // 检测是否在伸展
                Hand raiseHandType = simpleStaticGesture.OnLateralRaise(bodyPointList);
                if (raiseHandType.equals(Hand.BothHand)) {
                    poseHandType = Hand.BothHand;
                    shoulderPoseInfo = poseInfo;
                    synTrackId = poseInfo.getTrackId();
                    holder = true;
                    Log.i(TAG, poseHandType.toString());
                    break;
                } else {
                    // 检测是否在举手
                    HandList upHandType = simpleStaticGesture.OnHandUpV2(bodyPointList);
                    if (upHandType.size() > 0) {
                        if (upHandType.get(0).equals(Hand.LeftHand)) {
                            poseHandType = Hand.LeftHand;
                            shoulderPoseInfo = poseInfo;
                            synTrackId = poseInfo.getTrackId();
                            holder = true;
                            Log.i(TAG, poseHandType.toString());
                            break;
                        } else if (upHandType.get(0).equals(Hand.RightHand)) {
                            poseHandType = Hand.RightHand;
                            shoulderPoseInfo = poseInfo;
                            synTrackId = poseInfo.getTrackId();
                            holder = true;
                            Log.i(TAG, poseHandType.toString());
                            break;
                        }
                    } else {
                        if (poseInfo.getTrackId() == synTrackId) {
                            shoulderPoseInfo = poseInfo;
                            hasLastPose = true;
                        }
                    }
                }
            } else {
                if (poseHandType.equals(Hand.LeftHand)) {
                    // 是否持续举起左手
                    holder = simpleStaticGesture.OnLeftHandUpV2(bodyPointList);
                    if (holder) {
                        tempSynTrackId = poseInfo.getTrackId();
                        shoulderPoseInfo = poseInfo;
                        synTrackId = poseInfo.getTrackId();
                        break;
                    } else if (shoulderPoseInfo != null && poseInfo.getTrackId() == shoulderPoseInfo.getTrackId()) {
//                        Log.e(TAG, "shoulderPoseInfo: " + poseInfo.getTrackId());
                        shoulderPoseInfo = poseInfo;
                        synTrackId = poseInfo.getTrackId();
                        break;
                    }
                } else if (poseHandType.equals(Hand.RightHand)) {
                    // 是否持续举起右手
                    holder = simpleStaticGesture.OnRightHandUpV2(bodyPointList);
                    if (holder) {
                        tempSynTrackId = poseInfo.getTrackId();
                        shoulderPoseInfo = poseInfo;
                        synTrackId = poseInfo.getTrackId();
                        break;
                    } else if (shoulderPoseInfo != null && poseInfo.getTrackId() == shoulderPoseInfo.getTrackId()) {
//                        Log.e(TAG, "shoulderPoseInfo: " + poseInfo.getTrackId());
                        shoulderPoseInfo = poseInfo;
                        synTrackId = poseInfo.getTrackId();
                        break;
                    }
                } else if (poseHandType.equals(Hand.BothHand)) {
                    // 是否持续伸展
                    holder = simpleStaticGesture.OnLateralRaise(bodyPointList).equals(Hand.BothHand);
                    if (holder) {
                        tempSynTrackId = poseInfo.getTrackId();
                        shoulderPoseInfo = poseInfo;
                        synTrackId = poseInfo.getTrackId();
                        break;
                    } else if (shoulderPoseInfo != null && poseInfo.getTrackId() == shoulderPoseInfo.getTrackId()) {
//                        Log.e(TAG, "shoulderPoseInfo: " + poseInfo.getTrackId());
                        shoulderPoseInfo = poseInfo;
                        synTrackId = poseInfo.getTrackId();
                        break;
                    }
                }
            }
        }
//        Log.i(TAG, poseHandType + "");
        if (!poseHandType.equals(Hand.Unkonwn)) {
            long currentTime = System.currentTimeMillis();
            long costTime = currentTime - startTime;
            if (holder) {
                mainHolderTime = mainHolderTime + costTime;
//                Log.i(TAG, poseHandType + " mainHolderTime:" + mainHolderTime);
                sendMainProgress(poseHandType, tempSynTrackId);
                if (shoulderPoseInfo != null)
                    handleTransformPoseInfoResult(transformPose(new PoseInfo[]{shoulderPoseInfo}));
                if (mainHolderTime >= mainGestureTime) {
                    poseHandType = Hand.Unkonwn;
                    mainHolderTime = 0;
                }
            } else {
                mainHolderTime = mainHolderTime - costTime;
//                Log.i(TAG, poseHandType + " mainHolderTime: " + mainHolderTime);
                sendMainProgress(poseHandType, tempSynTrackId);
                if (shoulderPoseInfo != null)
                    handleTransformPoseInfoResult(transformPose(new PoseInfo[]{shoulderPoseInfo}));
                if (mainHolderTime <= 0) {
                    poseHandType = Hand.Unkonwn;
                    mainHolderTime = 0;
                }
            }
        } else {
            if (hasLastPose) {
                if (shoulderPoseInfo != null)
                    handleTransformPoseInfoResult(transformPose(new PoseInfo[]{shoulderPoseInfo}));
            } else {
                handleTransformPoseInfoResult(transformPose(new PoseInfo[]{poseInfoOrgArray[0]}));
                shoulderPoseInfo = null;
                synTrackId = 0;
            }
        }
    }


    private void checkTurnPageGesture(HolisticInfo holisticInfo, long startTime) {
        if (holisticInfo != null && holisticInfo.poseInfo != null && holisticInfo.poseInfo.landmarks != null) {
            bodyPointList.clear();
            for (int i = 0; i < holisticInfo.poseInfo.landmarks.length; ++i) {
                Point2f pt = holisticInfo.poseInfo.landmarks[i];
                bodyPoint3f.setX(pt.x);
                bodyPoint3f.setY(pt.y);
                bodyPoint3f.setZ(0);
                bodyPointList.add(bodyPoint3f);
            }
            currentGestureTime = System.currentTimeMillis();

            checkTurnPageHandUp(startTime);

            if (holisticInfo.leftHandInfo != null && holisticInfo.leftHandInfo.landmarks != null) {
                handLeftPointList.clear();
                for (int i = 0; i < holisticInfo.leftHandInfo.landmarks.length; ++i) {
                    Point2f pt = holisticInfo.leftHandInfo.landmarks[i];
                    handLeftPoint3f.setX(pt.x);
                    handLeftPoint3f.setY(pt.y);
                    handLeftPoint3f.setZ(0);
                    handLeftPointList.add(handLeftPoint3f);
                }
                checkLeftDynamicGesture();
            }

            if (holisticInfo.rightHandInfo != null && holisticInfo.rightHandInfo.landmarks != null) {
                handRightPointList.clear();
                for (int i = 0; i < holisticInfo.rightHandInfo.landmarks.length; ++i) {
                    Point2f pt = holisticInfo.rightHandInfo.landmarks[i];
                    handRightPoint3f.setX(pt.x);
                    handRightPoint3f.setY(pt.y);
                    handRightPoint3f.setZ(0);
                    handRightPointList.add(handRightPoint3f);
                }
                checkRightDynamicGesture();
            }
        }

    }


    private void checkFitnessGesture(HolisticInfo holisticInfo, long startTime) {
        if (holisticInfo != null && holisticInfo.poseInfo != null && holisticInfo.poseInfo.landmarks != null) {
            bodyPointList.clear();
            for (int i = 0; i < holisticInfo.poseInfo.landmarks.length; ++i) {
                Point2f pt = holisticInfo.poseInfo.landmarks[i];
                bodyPoint3f.setX(pt.x);
                bodyPoint3f.setY(pt.y);
                bodyPoint3f.setZ(0);
                bodyPointList.add(bodyPoint3f);
            }
            checkFitnessHandUp(startTime);
        }

    }


    private void checkTurnPageHandUp(long startTime) {
        OptionalPoint2f optionalPoint2f = simpleStaticGesture.OnArmCross(bodyPointList);
        boolean armCross = optionalPoint2f.is_specified();
        long currentTime = System.currentTimeMillis();
        long costTime = currentTime - startTime;
        if (armCross) {
            pageTurnHolderTime = pageTurnHolderTime + costTime;
            Log.e(TAG, "checkTurnPageHandUp: holderTime " + pageTurnHolderTime);
            sendTurnPageProgress();
            if (pageTurnHolderTime >= otherGestureTime) {
                pageTurnHolderTime = 0;
            }
        } else {
            pageTurnHolderTime = pageTurnHolderTime - costTime;
            sendTurnPageProgress();
            if (pageTurnHolderTime <= 0) {
                pageTurnHolderTime = 0;
            }
        }
    }

    private void checkFitnessHandUp(long startTime) {
        boolean holder = false;
        // 初始状态
        if (poseHandType.equals(Hand.Unkonwn)) {
            // 检测交叉
            OptionalPoint2f optionalPoint2f = simpleStaticGesture.OnArmCross(bodyPointList);
            boolean armCross = optionalPoint2f.is_specified();
            if (armCross) {
                poseHandType = Hand.BothHand;
                holder = true;
                Log.i(TAG, poseHandType.toString());
            } else {
                // 检测是否在举手
                HandList upHandType = simpleStaticGesture.OnHandUpV2(bodyPointList);
                if (upHandType.size() > 0) {
                    if (upHandType.get(0).equals(Hand.LeftHand)) {
                        poseHandType = Hand.LeftHand;
                        holder = true;
                        Log.i(TAG, poseHandType.toString());

                    } else if (upHandType.get(0).equals(Hand.RightHand)) {
                        poseHandType = Hand.RightHand;
                        holder = true;
                        Log.i(TAG, poseHandType.toString());

                    }
                }
            }
        } else {
            if (poseHandType.equals(Hand.LeftHand)) {
                // 是否持续举起左手
                holder = simpleStaticGesture.OnLeftHandUpV2(bodyPointList);
            } else if (poseHandType.equals(Hand.RightHand)) {
                // 是否持续举起右手
                holder = simpleStaticGesture.OnRightHandUpV2(bodyPointList);
            } else if (poseHandType.equals(Hand.BothHand)) {
                // 是否持续伸展
                OptionalPoint2f optionalPoint2f = simpleStaticGesture.OnArmCross(bodyPointList);
                holder = optionalPoint2f.is_specified();
            }
        }

        Log.i(TAG, poseHandType + " ");
        if (!poseHandType.equals(Hand.Unkonwn)) {
            long currentTime = System.currentTimeMillis();
            long costTime = currentTime - startTime;
            if (holder) {
                fitnessHolderTime = fitnessHolderTime + costTime;
                Log.i(TAG, poseHandType + " fitnessHolderTime: " + fitnessHolderTime);
                sendFitnessProgress(poseHandType);
                if (fitnessHolderTime >= otherGestureTime) {
                    poseHandType = Hand.Unkonwn;
                    fitnessHolderTime = 0;
                }
            } else {
                fitnessHolderTime = fitnessHolderTime - costTime;
                Log.i(TAG, poseHandType + " fitnessHolderTime: " + fitnessHolderTime);
                sendFitnessProgress(poseHandType);
                if (fitnessHolderTime <= 0) {
                    poseHandType = Hand.Unkonwn;
                    fitnessHolderTime = 0;
                }
            }
        }
    }


    private void checkLeftDynamicGesture() {
        leftSlip = SlipEnum.Unkonwn;
        // 表示时间够，可以检测手势
        isLeftHandDynamicGesture = currentGestureTime - lastLeftHandGestureTime > isDynamicGestureTime;
        if (isLeftHandDynamicGesture) {
            simpleDynamicGesture.InputSlipV3InfoParam(bodyPointList, handLeftPointList, handRightPointList);
            SlipEnum slipEnum = simpleDynamicGesture.OnSlipV3LeftHand();
            if (slipEnum.equals(SlipEnum.Unkonwn)) {
                isLeftSlipIng = false;
            } else if (slipEnum.equals(SlipEnum.SlipLeft)) {
                if (lastLeftSlipEnum.equals(SlipEnum.SlipRight)) {
                    if (currentGestureTime - lastLeftHandGestureTime > EnableLeftGestureTime) {
                        isLeftSlipIng = true;
                        leftSlip = SlipEnum.SlipLeft;
                    } else {
                        isLeftSlipIng = false;
                    }
                } else {
                    isLeftSlipIng = true;
                    leftSlip = SlipEnum.SlipLeft;
                }
            } else if (slipEnum.equals(SlipEnum.SlipRight)) {
                if (lastLeftSlipEnum.equals(SlipEnum.SlipLeft)) {
                    if (currentGestureTime - lastLeftHandGestureTime > EnableRightGestureTime) {
                        isLeftSlipIng = true;
                        leftSlip = SlipEnum.SlipRight;
                    } else {
                        isLeftSlipIng = false;
                    }
                } else {
                    isLeftSlipIng = true;
                    leftSlip = SlipEnum.SlipRight;
                }
            } else {
                isLeftSlipIng = false;
            }
        } else {
            isLeftSlipIng = false;
        }
        // 主线程动作

        if (isLeftSlipIng) {
            if (leftSlip.equals(SlipEnum.SlipLeft)) {
                lastLeftHandGestureTime = System.currentTimeMillis();
                lastLeftSlipEnum = SlipEnum.SlipLeft;
                handler.post(() -> {
//                    gestureListener.onSlipLeft();
                    gestureListener.onSlipRight();
                });
            } else {
//                lastLeftHandGestureTime = System.currentTimeMillis();
//                lastLeftSlipEnum = SlipEnum.SlipRight;
//                handler.post(() -> {
//                    gestureListener.onSlipLeft();
//                    gestureListener.onSlipRight();
//                });
            }
        }

    }

    private void checkRightDynamicGesture() {
        rightSlip = SlipEnum.Unkonwn;
        isRightHandDynamicGesture = currentGestureTime - lastRightHandGestureTime > isDynamicGestureTime;
        if (isRightHandDynamicGesture) {
            simpleDynamicGesture.InputSlipV3InfoParam(bodyPointList, handLeftPointList, handRightPointList);
            SlipEnum slipEnum = simpleDynamicGesture.OnSlipV3RightHand();
            if (slipEnum.equals(SlipEnum.Unkonwn)) {
                isRightSlipIng = false;
            } else {
                if (slipEnum.equals(SlipEnum.SlipLeft)) {
                    if (lastRightSlipEnum.equals(SlipEnum.SlipRight)) {
                        if (currentGestureTime - lastRightHandGestureTime > EnableLeftGestureTime) {
                            rightSlip = SlipEnum.SlipLeft;
                            isRightSlipIng = true;
                        } else {
                            isRightSlipIng = false;
                        }
                    } else {
                        rightSlip = SlipEnum.SlipLeft;
                        isRightSlipIng = true;
                    }
                } else if (slipEnum.equals(SlipEnum.SlipRight)) {
                    if (lastRightSlipEnum.equals(SlipEnum.SlipLeft)) {
                        if (currentGestureTime - lastRightHandGestureTime > EnableRightGestureTime) {
                            rightSlip = SlipEnum.SlipRight;
                            isRightSlipIng = true;
                        } else {
                            isRightSlipIng = false;
                        }
                    } else {
                        rightSlip = SlipEnum.SlipRight;
                        isRightSlipIng = true;
                    }
                } else {
                    isRightSlipIng = false;
                }
            }
        } else {
            isRightSlipIng = false;
        }

        if (isRightSlipIng) {
            if (rightSlip.equals(SlipEnum.SlipLeft)) {
//                lastRightHandGestureTime = System.currentTimeMillis();
//                lastRightSlipEnum = SlipEnum.SlipLeft;
//                handler.post(() -> {
//                    gestureListener.onSlipLeft();
//                    gestureListener.onSlipRight();
//                });
            } else {
                lastRightHandGestureTime = System.currentTimeMillis();
                lastRightSlipEnum = SlipEnum.SlipRight;
                handler.post(() -> {
                    gestureListener.onSlipLeft();
//                    gestureListener.onSlipRight();
                });
            }
        }
    }


    private void playSound(int type) {
        MediaManager.stop();
        AssetFileDescriptor afd = null;
        try {
            if (type == 0 && !hasPersonHand) {
                afd = getAssets().openFd("checkUpHand.mp3");
                Log.e(TAG, "playSound: checkUpHand.mp3");
//                showToast("播放举手提示音频");
                MediaManager.playSoundAssets(afd, onCompletionListener);
            } else if (type == 1) {
                afd = getAssets().openFd("gestureOk.mp3");
                Log.e(TAG, "playSound: gestureOk.mp3");
//                showToast("播放锁定成功音频");
                MediaManager.playSoundAssets(afd, onCompletionListener);

            } else if (type == 2) {
                afd = getAssets().openFd("gestureCancel.mp3");
                Log.e(TAG, "playSound: gestureCancel.mp3");
//                showToast("播放离开手势区域");
                MediaManager.playSoundAssets(afd, onCompletionListener);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void sendKeyCode(final int keyCode) {
        if (cachedThreadPool == null) {
            cachedThreadPool = Executors.newCachedThreadPool();
        }
        cachedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Instrumentation inst = new Instrumentation();
                    inst.sendKeyDownUpSync(keyCode);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void releaseAllZee() {
        Log.i(TAG, "releaseAllZee()");
        for (int i = 0; i < locationEnumList.size(); i++) {
            LocationEnum locationEnum = locationEnumList.get(i);
            if (LocationEnum.PERSONPOSE == locationEnum) {
                zeewainPose.releasePose();
            } else if (LocationEnum.SINGLEPOSE == locationEnum) {
                zeewainPose.releaseSinglePose();
            } else if (LocationEnum.FACE == locationEnum) {
                zeewainPose.releaseFace();
            } else if (LocationEnum.HAND == locationEnum) {
                zeewainPose.releaseHand();
            } else if (LocationEnum.HOLISTIC == locationEnum) {
                zeewainPose.releaseHolistic();
            } else if (LocationEnum.SEGMENT == locationEnum) {
                zeewainPose.releaseSegment();
            } else if (LocationEnum.POSE3D2D == locationEnum) {
                zeewainPose.releasePose3D2D();
            } else if (LocationEnum.HOLISTIC3D2D == locationEnum) {
                zeewainPose.releaseHolistic3D2D();
            }
        }
        locationEnumList.clear();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy()");
        if (handler != null) handler.removeCallbacksAndMessages(null);
        if (mFloatView != null) {
            windowManager.removeView(mFloatView);
        }
        if (camera2Helper != null) {
            camera2Helper.release();
        }

        releaseAllZee();
        if (overlay != null) overlay.release();
        super.onDestroy();
    }


    private void showToast(String toast) {
        ToastUtils.showShort(toast);
    }


    private List<PoseInfo[]> transformPose(PoseInfo[] poseInfoOrgArray) {
        if (poseInfoOrgArray != null && poseInfoOrgArray.length > 0) {
            List<PoseInfo[]> dataList = new ArrayList<>(2);
            PoseInfo[] poseInfoArray = new PoseInfo[poseInfoOrgArray.length];
            PoseInfo[] poseInfoOverlayArray = new PoseInfo[poseInfoOrgArray.length];

            for (int j = 0; j < poseInfoOrgArray.length; j++) {
                poseInfoArray[j] = ZeePoseWrapper.clonePoseInfo(poseInfoOrgArray[j]);
                poseInfoOverlayArray[j] = ZeePoseWrapper.clonePoseInfo(poseInfoOrgArray[j]);

                if (poseInfoArray[j].landmarks != null) {
                    for (int i = 0; i < poseInfoArray[j].landmarks.length; i++) {
                        poseInfoOverlayArray[j].landmarks[i].x = poseInfoArray[j].landmarks[i].x * scaleX;
                        poseInfoOverlayArray[j].landmarks[i].y = poseInfoArray[j].landmarks[i].y * scaleY;

                    }
                }

                if (poseInfoArray[j].rect != null) {
                    float rectX = poseInfoArray[j].rect.x * scaleX;
                    float rectY = poseInfoArray[j].rect.y * scaleY;
                    poseInfoOverlayArray[j].rect.x = (int) rectX;
                    poseInfoOverlayArray[j].rect.y = (int) rectY;
                    poseInfoOverlayArray[j].rect.width = (int) (poseInfoOverlayArray[j].rect.width * scaleX);
                    poseInfoOverlayArray[j].rect.height = (int) (poseInfoOverlayArray[j].rect.height * scaleY);
                }
            }
            dataList.add(poseInfoArray);
            dataList.add(poseInfoOverlayArray);
            return dataList;
        }
        return null;
    }

    private void handleTransformPoseInfoResult(List<PoseInfo[]> dataList) {
        if (dataList != null && dataList.size() == 2) {
//            poseInfoArray = dataList.get(0);
            poseInfoOverlayArray = dataList.get(1);
            if (isDrawOverlayPoint) {
                handler.post(() -> overlay.drawShoulderPoint(poseInfoOverlayArray));
            }
        } else {
//            poseInfoArray = null;
            poseInfoOverlayArray = null;
            if (isDrawOverlayPoint) {
                handler.post(() -> overlay.clear());
            }
        }
    }


    private void sendMainProgress(Hand type, int synTrackId) {
        int progress = (int) (mainHolderTime * classAndPluginProgress / mainGestureTime);
        if (progress == classAndPluginProgress) {
            if (type.equals(Hand.BothHand)) {
                checkState = 2;
                this.synTrackId = synTrackId;
                camera2Helper.pauseRunClassifier();
            } else if (type.equals(Hand.LeftHand)) {
                camera2Helper.pauseRunClassifier();
            } else if (type.equals(Hand.RightHand)) {
                checkState = 1;
                this.synTrackId = synTrackId;
            }
        }

        if (type.equals(Hand.BothHand)) {
            handler.post(() -> {
                if (gestureListener != null) {
                    gestureListener.onExpandProgress(progress);
                }
            });
        } else if (type.equals(Hand.LeftHand)) {
            handler.post(() -> {
                if (gestureListener != null) {
                    gestureListener.onLeftHandUpProgress(progress);
                }
            });
        } else if (type.equals(Hand.RightHand)) {
            handler.post(() -> {
                if (gestureListener != null) {
                    gestureListener.onRightHandUpProgress(progress);
                }
            });

        }
    }

    private void sendTurnPageProgress() {
        int progress = (int) (pageTurnHolderTime * backProgress / otherGestureTime);
        if (progress == backProgress) {
            checkState = 0;
            camera2Helper.pauseRunClassifier();
        }
        handler.post(() -> {
            if (gestureListener != null) {
                gestureListener.onThrowbackProgress(progress);
            }
        });
    }


    private void sendFitnessProgress(Hand type) {
        if (type.equals(Hand.BothHand)) {
            int progress = (int) (fitnessHolderTime * backProgress / otherGestureTime);
            if (progress == backProgress) checkState = 0;
            handler.post(() -> {
                if (gestureListener != null) {
                    gestureListener.onThrowbackProgress(progress);
                }
            });
        } else if (type.equals(Hand.LeftHand)) {
            int progress = (int) (fitnessHolderTime * classAndPluginProgress / otherGestureTime);
            if (progress == classAndPluginProgress) camera2Helper.pauseRunClassifier();
            handler.post(() -> {
                if (gestureListener != null) {
                    gestureListener.onLeftHandUpProgress(progress);
                }
            });
        } else if (type.equals(Hand.RightHand)) {
            int progress = (int) (fitnessHolderTime * classAndPluginProgress / otherGestureTime);
            if (progress == classAndPluginProgress) camera2Helper.pauseRunClassifier();
            handler.post(() -> {
                if (gestureListener != null) {
                    gestureListener.onRightHandUpProgress(progress);
                }
            });
        }
    }


    public final class ServiceBinder extends Binder {
        public GestureCameraService getService() {
            return GestureCameraService.this;
        }
    }


    public void setGestureListener(GestureListener listener, int checkState) {
        this.gestureListener = listener;
        this.checkState = checkState;
        if (camera2Helper != null && !camera2Helper.runClassifier) {
            camera2Helper.startRunClassifier();
        }
    }

    public void setResumeGestureListener(GestureListener listener, int checkState) {
        this.gestureListener = listener;
        this.checkState = checkState;
        if (camera2Helper != null && !camera2Helper.runClassifier) {
            resumeAndStartGesture();
        }
    }

    public void clearGestureListener() {
        camera2Helper.runClassifier = false;
        gestureListener = null;
        handler.removeCallbacksAndMessages(null);
    }


    public void pauseGesture() {
        if (camera2Helper != null) {
            mFloatView.setAlpha(0);
            camera2Helper.stop();
        }

    }

    public void resumeGesture() {
        if (camera2Helper != null && lastUsedCameraId != null) {
            poseHandType = Hand.Unkonwn;
            holderTime = 0;
//            camera2Helper.start(lastUsedCameraId);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    camera2Helper.start(lastUsedCameraId);
                    mFloatView.setAlpha(1);
                }
            }, 500);
        }
    }


    public void resumeAndStartGesture() {
        if (camera2Helper != null && lastUsedCameraId != null) {
            poseHandType = Hand.Unkonwn;
            holderTime = 0;
//            camera2Helper.start(lastUsedCameraId);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    camera2Helper.start(lastUsedCameraId);
                    mFloatView.setAlpha(1);
                    camera2Helper.startRunClassifier();
                }
            }, 500);
        }
    }


    public void showGestureView(boolean isShow) {
        if (isShow) {
            mFloatView.setAlpha(1);
        } else {
            mFloatView.setAlpha(0);
        }
    }


    public void resumeHideGesture() {
        if (camera2Helper != null && lastUsedCameraId != null) {
            poseHandType = Hand.Unkonwn;
            holderTime = 0;
            camera2Helper.start(lastUsedCameraId);
        }
    }


    public interface GestureListener {

        void onLeftHandUpProgress(int progress);

        void onRightHandUpProgress(int progress);

        void onExpandProgress(int progress);

        void onThrowbackProgress(int progress);

        void onSlipLeft();

        void onSlipRight();

        void onError(int errType);
    }


    private void handleCameraErrExit(int err) {
        cameraState = err;
        gestureListener.onError(err);
        camera2Helper.stopBackgroundThread();
    }
}
