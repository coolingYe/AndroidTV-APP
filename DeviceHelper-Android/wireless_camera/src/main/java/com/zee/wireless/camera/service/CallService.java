package com.zee.wireless.camera.service;

import static com.zee.device.base.utils.DensityUtils.getScreenSize;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Outline;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.util.Consumer;

import com.zee.device.base.config.BaseConstants;
import com.zee.device.base.dialog.CustomAlertDialog;
import com.zee.device.base.utils.CommonUtils;
import com.zee.device.base.utils.Logger;
import com.zee.device.base.utils.NetworkUtil;
import com.zee.device.base.utils.SPUtils;
import com.zee.wireless.camera.R;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.DataChannel;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.MediaStreamTrack;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpReceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class CallService extends Service {

    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;

    private View displayView;

    public static String selectedRemoteIp = null;
    public static String backupRemoteIp = null;
    public static int selectedRemotePort = 1213;
    private SignalServer mServer;
    private WebSocket webSocketClient;
    private static final String TAG = "FloatingService";
    /**
     * ---------和webrtc相关-----------
     */

    private int mCurrentWidth;

    private int mCurrentHeight;

    // Opengl es
    public EglBase mRootEglBase;
    // 纹理渲染
    private SurfaceTextureHelper mSurfaceTextureHelper;

    private SurfaceViewRenderer mLocalSurfaceView;
    // 音视频数据
    public static final String VIDEO_TRACK_ID = "1";//"ARDAMSv0";
    public static final String AUDIO_TRACK_ID = "2";//"ARDAMSa0";
    public VideoTrack mVideoTrack;
    //private AudioTrack mAudioTrack;

    // 视频采集
    private VideoCapturer mVideoCapturer;

    //用于数据传输
    private PeerConnection mPeerConnection;
    private PeerConnectionFactory mPeerConnectionFactory;
    private boolean isFrontFacing = false;
    public String frontCameraId = null;

    private Consumer<View> closeCallback = null;

    private long startTouchTime = 0;
    private long endTouchTime = 0;

    private final PeerConnection.Observer mPeerConnectionObserver = new PeerConnection.Observer() {
        @Override
        public void onSignalingChange(PeerConnection.SignalingState signalingState) {
            Logger.d("onSignalingChange: " + signalingState);
        }

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
            Logger.d("onIceConnectionChange: " + iceConnectionState);
        }

        @Override
        public void onIceConnectionReceivingChange(boolean b) {
            Logger.d("onIceConnectionChange: " + b);
        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
            Logger.d("onIceGatheringChange: " + iceGatheringState);
        }

        @Override
        public void onIceCandidate(IceCandidate iceCandidate) {
            Logger.d("onIceCandidate: " + iceCandidate);
            // 得到candidate，就发送给信令服务器
            try {
                JSONObject message = new JSONObject();
                //message.put("userId", RTCWebRTCSignalClient.getInstance().getUserId());
                message.put("type", "candidate");
                message.put("label", iceCandidate.sdpMLineIndex);
                message.put("id", iceCandidate.sdpMid);
                message.put("candidate", iceCandidate.sdp);
                sendMessage(message);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
            for (int i = 0; i < iceCandidates.length; i++) {
                Logger.d("onIceCandidatesRemoved: " + iceCandidates[i]);
            }
            mPeerConnection.removeIceCandidates(iceCandidates);
        }

        @Override
        public void onAddStream(MediaStream mediaStream) {
            Logger.d("onAddStream: " + mediaStream.videoTracks.size());
        }

        @Override
        public void onRemoveStream(MediaStream mediaStream) {
            Logger.d("onRemoveStream");
        }

        @Override
        public void onDataChannel(DataChannel dataChannel) {
            Logger.d("onDataChannel");
        }

        @Override
        public void onRenegotiationNeeded() {
            Logger.d("onRenegotiationNeeded");
        }

        // 收到了媒体流
        @Override
        public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {
            MediaStreamTrack track = rtpReceiver.track();
            if (track instanceof VideoTrack) {
                Logger.d("onAddVideoTrack");
                VideoTrack remoteVideoTrack = (VideoTrack) track;
                remoteVideoTrack.setEnabled(true);
//                remoteVideoTrack.addSink(mRemoteSurfaceView);
            }
        }
    };

    private final CameraVideoCapturer.CameraEventsHandler cameraEventsHandler = new CameraVideoCapturer.CameraEventsHandler() {
        @Override
        public void onCameraError(String err) {
            Logger.d("cameraEventsHandler===> onCameraError() err=" + err);
        }

        @Override
        public void onCameraDisconnected() {
            Logger.d("cameraEventsHandler===> onCameraDisconnected()");
        }

        @Override
        public void onCameraFreezed(String s) {
            Logger.d("cameraEventsHandler===> onCameraFreezed() s=" + s);
        }

        @Override
        public void onCameraOpening(String cameraId) {
            Logger.d("cameraEventsHandler===> onCameraOpening() cameraId=" + cameraId);

        }

        @Override
        public void onFirstFrameAvailable() {
            Logger.d("cameraEventsHandler===> onFirstFrameAvailable()");
            if (webSocketClient != null) {
                try {
                    JSONObject message = new JSONObject();
                    message.put("type", "previewInfo");
                    message.put("isFrontFacing", isFrontFacing);
                    message.put("orientation", getResources().getConfiguration().orientation);
                    sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onCameraClosed() {
            Logger.d("cameraEventsHandler===> onCameraClosed()");
        }
    };

    private final CallService.ServiceBinder serviceBinder = new CallService.ServiceBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        showFloatingWindow();
        return this.serviceBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate: ");
        initLayoutPar();
    }

    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = CallService.class.getSimpleName();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("相机服务")
                .setContentText("相机正在运行中...")
                .build();

        // 将服务设置为前台服务
        startForeground(NOTIFICATION_ID, notification);

        return super.onStartCommand(intent, flags, startId);
    }

    private void initLayoutPar() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        layoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.gravity = Gravity.TOP | Gravity.START;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.width = BaseConstants.CAMERA_WIDTH;
        layoutParams.height = BaseConstants.CAMERA_HEIGHT;
        layoutParams.x = getScreenSize(getApplication()).x - BaseConstants.CAMERA_WIDTH;
        layoutParams.y = getScreenSize(getApplication()).y - BaseConstants.CAMERA_HEIGHT;
    }

    private void showFloatingWindow() {
        if (Settings.canDrawOverlays(this)) {
            LayoutInflater layoutInflater = LayoutInflater.from(this);
            displayView = layoutInflater.inflate(R.layout.video_display, null);
            mLocalSurfaceView = displayView.findViewById(R.id.localSurfaceView);
            mLocalSurfaceView.setClipToOutline(true);
            mLocalSurfaceView.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    Rect rect = new Rect();
                    view.getGlobalVisibleRect(rect);
                    int leftMargin = 0;
                    int topMargin = 0;
                    Rect selfRect = new Rect(leftMargin, topMargin, rect.right - rect.left - leftMargin, rect.bottom - rect.top - topMargin);
                    outline.setRoundRect(selfRect, 22);
                }
            });
            displayView.setOnClickListener(mClickListener);
            displayView.setOnTouchListener(mTouchListener);
            initCamera();
            startActingRemoteCamera();
            windowManager.addView(displayView, layoutParams);
        }
    }

    public int getCurrentWidth() {
        return mCurrentWidth;
    }

    public void setCurrentWidth(int mCurrentWidth) {
        this.mCurrentWidth = mCurrentWidth;
    }

    public int getCurrentHeight() {
        return mCurrentHeight;
    }

    public void setCurrentHeight(int mCurrentHeight) {
        this.mCurrentHeight = mCurrentHeight;
    }

    public void setCloseCallback(Consumer closeCallback) {
        this.closeCallback = closeCallback;
    }

    public void stopAll() {
        stopActingRemoteCamera();
        mHandler.removeCallbacksAndMessages(null);
        try {
            // 停止采集
            if (mVideoCapturer != null)
                mVideoCapturer.stopCapture();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Logger.d(TAG, "onDestroy()");
        doLeave();
        if (mVideoTrack != null) {
            mVideoTrack.removeSink(mLocalSurfaceView);
        }
        mLocalSurfaceView.release();
        mSurfaceTextureHelper.dispose();
        if (mVideoCapturer != null)
            mVideoCapturer.dispose();
        PeerConnectionFactory.stopInternalTracingCapture();
        PeerConnectionFactory.shutdownInternalTracer();
        mPeerConnectionFactory.dispose();
        try {
            if (webSocketClient != null) {
                webSocketClient.close();
            }
            if (mServer != null) {
                mServer.stop();
            }

            if (remoteCameraClient != null) {
                remoteCameraClient.close();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        selectedRemoteIp = null;
        backupRemoteIp = null;
        Logger.d(TAG, "onDestroy() done");
        if (displayView != null) {
            windowManager.removeView(displayView);
        }
    }

    private final View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            hideAll();
            Intent intent = new Intent();
            try {
                intent.setClass(getApplicationContext(), Class.forName(BaseConstants.CALL_PKG_CLASS_NAME));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    };

    private final View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        private int initialX, initialY;
        private float initialTouchX, initialTouchY;

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_UP:
                    endTouchTime = System.currentTimeMillis();
                    if ((endTouchTime - startTouchTime) > 0.1 * 1000L) {
                        return true;
                    }
                    return false;

                case MotionEvent.ACTION_DOWN:
                    initialX = layoutParams.x;
                    initialY = layoutParams.y;
                    initialTouchX = event.getRawX();
                    initialTouchY = event.getRawY();
                    startTouchTime = System.currentTimeMillis();
                    return false;

                case MotionEvent.ACTION_MOVE:
                    layoutParams.x = initialX + (int) (event.getRawX() - initialTouchX);
                    layoutParams.y = initialY + (int) (event.getRawY() - initialTouchY);
                    windowManager.updateViewLayout(displayView, layoutParams);
                    return true;

                default:
                    return false;
            }
        }
    };

    public void hideAll() {
        displayView.setVisibility(View.GONE);
    }

    public void showAll() {
        displayView.setVisibility(View.VISIBLE);
    }

    private void initCamera() {
        mRootEglBase = EglBase.create();
        mLocalSurfaceView.init(mRootEglBase.getEglBaseContext(), null);

        //mLocalSurfaceView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        mLocalSurfaceView.setMirror(true);

        mPeerConnectionFactory = createPeerConnectionFactory(getApplicationContext());
        Logging.enableLogToDebugOutput(Logging.Severity.LS_VERBOSE);
        mVideoCapturer = createVideoCapturer();
        mSurfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", mRootEglBase.getEglBaseContext());
        VideoSource videoSource = mPeerConnectionFactory.createVideoSource(false);
        if (mVideoCapturer != null)
            mVideoCapturer.initialize(mSurfaceTextureHelper, getApplicationContext(), videoSource.getCapturerObserver());

        mVideoTrack = mPeerConnectionFactory.createVideoTrack(VIDEO_TRACK_ID, videoSource);
        mVideoTrack.setEnabled(true);
        mVideoTrack.addSink(mLocalSurfaceView); // 设置渲染到本地surfaceview上
        /** ---------开始启动信令服务----------- */
        mServer = new SignalServer(BaseConstants.WEBRTC_SIGNAL_SERVER_PORT);
        mServer.setReuseAddr(true);
        mServer.start();
        // 开始采集并本地显示
        mCurrentWidth = SPUtils.getInstance().getInt("VIDEO_CURRENT_WIDTH", BaseConstants.VIDEO_RESOLUTION_WIDTH_360);
        mCurrentHeight = SPUtils.getInstance().getInt("VIDEO_CURRENT_HEIGHT", BaseConstants.VIDEO_RESOLUTION_HEIGHT_600);
        if (mVideoCapturer != null)
            mVideoCapturer.startCapture(mCurrentWidth, mCurrentHeight, BaseConstants.VIDEO_FPS_DEFAULT);
    }

    public void changeCameraCaptureScale(int width, int height) {
        mVideoCapturer.changeCaptureFormat(width, height, 30);
    }

    /*
     * Read more about Camera2 here
     * https://developer.android.com/reference/android/hardware/camera2/package-summary.html
     **/
    private VideoCapturer createVideoCapturer() {
        if (Camera2Enumerator.isSupported(getApplicationContext())) {
            return createCameraCapturer(new Camera2Enumerator(getApplicationContext()));
        } else {
            return createCameraCapturer(new Camera1Enumerator(true));
        }
    }

    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();

        // Front facing camera not found, try something else
        Logger.d("Looking for other cameras.");
        for (String deviceName : deviceNames) {
            if (!enumerator.isBackFacing(deviceName)) {
                Logger.d("Creating other camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, cameraEventsHandler);
                if (videoCapturer != null) {
                    frontCameraId = deviceName;
                    isFrontFacing = true;
                    return videoCapturer;
                }
            }
        }

        // First, try to find front facing camera
        Logger.d("Looking for front facing cameras.");
        for (String deviceName : deviceNames) {
            if (enumerator.isBackFacing(deviceName)) {
                Logger.d("Creating front facing camera capturer.");
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, cameraEventsHandler);
                if (videoCapturer != null) {
                    isFrontFacing = false;
                    return videoCapturer;
                }
            }
        }

        return null;
    }

    private class FloatingOnTouchListener implements View.OnTouchListener {
        private int x;
        private int y;

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    x = (int) event.getRawX();
                    y = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    int nowX = (int) event.getRawX();
                    int nowY = (int) event.getRawY();
                    int movedX = nowX - x;
                    int movedY = nowY - y;
                    x = nowX;
                    y = nowY;
                    layoutParams.x = layoutParams.x + movedX;
                    layoutParams.y = layoutParams.y + movedY;
                    windowManager.updateViewLayout(view, layoutParams);
                    break;
                default:
                    break;
            }
            return false;
        }
    }

    private final CameraVideoCapturer.CameraSwitchHandler cameraSwitchHandler = new CameraVideoCapturer.CameraSwitchHandler() {
        @Override
        public void onCameraSwitchDone(boolean frontFacing) {
            isFrontFacing = frontFacing;
            if (isFrontFacing) {
                mLocalSurfaceView.setMirror(true);
            } else {
                mLocalSurfaceView.setMirror(false);
            }
            Logger.d("onCameraSwitchDone() frontFacing " + frontFacing);
        }

        @Override
        public void onCameraSwitchError(String err) {
            Logger.d("onCameraSwitchError() err " + err);
        }
    };


    public void switchCamera() {
        if (mVideoCapturer != null) {
            CameraVideoCapturer cameraVideoCapturer = (CameraVideoCapturer) mVideoCapturer;
            if (isFrontFacing) {
                cameraVideoCapturer.switchCamera(cameraSwitchHandler);
            } else {
                cameraVideoCapturer.switchCamera(cameraSwitchHandler, frontCameraId);
            }
        }
    }

    class SignalServer extends WebSocketServer {

        public SignalServer(int port) {
            super(new InetSocketAddress(port));
        }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            if (conn.getRemoteSocketAddress() != null) {
                Logger.d("=== SignalServer onOpen() conn ip=" + conn.getRemoteSocketAddress().getHostString());
            } else {
                Logger.d("=== SignalServer onOpen()");
            }
            printInfoOnScreen("onOpen有客户端连接上...调用start call");
            JSONObject welcomeMessage = new JSONObject();
            try {
                welcomeMessage.put("type", "Welcome");
                conn.send(welcomeMessage.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onClose(WebSocket conn, int code, String reason, boolean remote) {
            Logger.d("=== SignalServer onClose() reason=" + reason + ", remote=" + remote);
            printInfoOnScreen("onClose客户端断开...调用doLeave，reason=" + reason);
            doLeave();
        }

        @Override
        public void onMessage(WebSocket conn, String message) {
            if (conn.getRemoteSocketAddress() != null) {
                Logger.d("=== SignalServer onMessage() message=" + message + " " + conn.getRemoteSocketAddress().getHostString());
            } else {
                Logger.d("=== SignalServer onMessage() message=" + message);
            }

            try {
                JSONObject jsonMessage = new JSONObject(message);

                String type = jsonMessage.getString("type");
                if (type.equals("doStartCall")) {
                    if (!TextUtils.isEmpty(selectedRemoteIp) && conn.getRemoteSocketAddress() != null && !TextUtils.isEmpty(conn.getRemoteSocketAddress().getHostString())) {
                        if (selectedRemoteIp.equals(conn.getRemoteSocketAddress().getHostString())) {
                            if (webSocketClient != null) {
                                webSocketClient.close();
                            }
                            webSocketClient = conn;
                        } else {
                            conn.close();
                            return;
                        }
                    } else {
                        if (webSocketClient != null) {
                            webSocketClient.close();
                        }
                        webSocketClient = conn;
                    }
                    doStartCall(conn);
                } else if (type.equals("offer")) {
                    onRemoteOfferReceived(jsonMessage);
                } else if (type.equals("answer")) {
                    onRemoteAnswerReceived(jsonMessage);
                } else if (type.equals("candidate")) {
                    onRemoteCandidateReceived(jsonMessage);
                } else {
                    Logger.e("the type is invalid: " + type);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(WebSocket conn, Exception ex) {
            ex.printStackTrace();
            Logger.e("=== SignalServer onMessage() ex=" + ex.getMessage());
        }

        @Override
        public void onStart() {
            Logger.d("=== SignalServer onStart()");
            setConnectionLostTimeout(0);
            setConnectionLostTimeout(100);

            printInfoOnScreen("onStart服务端建立成功...创建PC " + NetworkUtil.getLocalIpAddress());
            //这里应该创建PeerConnection
            if (mPeerConnection == null) {
                mPeerConnection = createPeerConnection();
            }
        }
    }

    // 收到对端发过来的candidate
    private void onRemoteCandidateReceived(JSONObject message) {
        printInfoOnScreen("Receive Remote Candidate ...");
        try {
            // candidate 候选者描述信息
            // sdpMid 与候选者相关的媒体流的识别标签
            // sdpMLineIndex 在SDP中m=的索引值
            // usernameFragment 包括了远端的唯一识别
            IceCandidate remoteIceCandidate =
                    new IceCandidate(message.getString("id"),
                            message.getInt("label"),
                            message.getString("candidate"));

            printInfoOnScreen("收到Candidate.....");
            mPeerConnection.addIceCandidate(remoteIceCandidate);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 接听方，收到offer
    private void onRemoteOfferReceived(JSONObject message) {
        printInfoOnScreen("Receive Remote Call ...");

        if (mPeerConnection == null) {
            mPeerConnection = createPeerConnection();
        }

        try {
            String description = message.getString("sdp");
            mPeerConnection.setRemoteDescription(
                    new SimpleSdpObserver(),
                    new SessionDescription(
                            SessionDescription.Type.OFFER,
                            description));
            printInfoOnScreen("收到offer...调用doAnswerCall");
            doAnswerCall();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void doAnswerCall() {
        printInfoOnScreen("Answer Call, Wait ...");

        if (mPeerConnection == null) {
            mPeerConnection = createPeerConnection();
        }

        MediaConstraints sdpMediaConstraints = new MediaConstraints();
        Logger.d("Create answer ...");
        mPeerConnection.createAnswer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                Logger.d("Create answer success !");
                mPeerConnection.setLocalDescription(new SimpleSdpObserver(),
                        sessionDescription);

                JSONObject message = new JSONObject();
                try {
                    message.put("type", "answer");
                    message.put("sdp", sessionDescription.description);
                    sendMessage(message.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, sdpMediaConstraints);
        updateCallState(false);
    }

    private void sendMessage(JSONObject message) {
        mServer.broadcast(message.toString());
    }

    private void sendMessage(String message) {
        mServer.broadcast(message);
    }

    // 发送方，收到answer
    private void onRemoteAnswerReceived(JSONObject message) {
        printInfoOnScreen("Receive Remote Answer ...");
        try {
            String description = message.getString("sdp");
            mPeerConnection.setRemoteDescription(
                    new SimpleSdpObserver(),
                    new SessionDescription(
                            SessionDescription.Type.ANSWER,
                            description));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        printInfoOnScreen("收到answer.....");
        updateCallState(false);
    }

    public void doLeave() {
        printInfoOnScreen("Leave room, Wait ...");
        printInfoOnScreen("Hangup Call, Wait ...");
        if (mPeerConnection == null) {
            return;
        }
        mPeerConnection.dispose();
        mPeerConnection = null;
        printInfoOnScreen("Hangup Done.");
        updateCallState(true);
    }

    private void updateCallState(boolean idle) {

    }

    private void printInfoOnScreen(String msg) {
        Logger.d(msg);
    }

    /**
     * 有其他用户连进来，
     */
    public void doStartCall(WebSocket conn) {
        printInfoOnScreen("Start Call, Wait ...");
        if (mPeerConnection == null) {
            mPeerConnection = createPeerConnection();
        }
        MediaConstraints mediaConstraints = new MediaConstraints();
        mediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true")); // 接收远端音频
        mediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true")); // 接收远端视频
        mediaConstraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));
        mPeerConnection.createOffer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                Logger.d("Create local offer success: \n" + sessionDescription.description);
                mPeerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);
                JSONObject message = new JSONObject();
                try {
                    message.put("type", "offer");
                    message.put("sdp", sessionDescription.description);
                    conn.send(message.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, mediaConstraints);
    }

    public PeerConnection createPeerConnection() {
        Logger.d("Create PeerConnection ...");

        LinkedList<PeerConnection.IceServer> iceServers = new LinkedList<PeerConnection.IceServer>();

        // 设置ICE服务器
        /*PeerConnection.IceServer ice_server =
                PeerConnection.IceServer.builder("turn:xxxx:3478")
                        .setPassword("xxx")
                        .setUsername("xxx")
                        .createIceServer();

        iceServers.add(ice_server);*/

        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);
        // TCP candidates are only useful when connecting to a server that supports
        // ICE-TCP.

        rtcConfig.enableCpuOveruseDetection = false;
        rtcConfig.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_ONCE;
        rtcConfig.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN;

        /*rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED; // 不要使用TCP
        rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.MAXBUNDLE; // max-bundle表示音视频都绑定到同一个传输通道
        rtcConfig.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.REQUIRE; // 只收集RTCP和RTP复用的ICE候选者，如果RTCP不能复用，就失败
        rtcConfig.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY;*/
        //rtcConfig.iceCandidatePoolSize = 10;
        //rtcConfig.iceTransportsType = PeerConnection.IceTransportsType.ALL;


        // Use ECDSA encryption.
        //rtcConfig.keyType = PeerConnection.KeyType.ECDSA;
        // Enable DTLS for normal calls and disable for loopback calls.
        //rtcConfig.enableDtlsSrtp = true;
        //rtcConfig.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN;
        PeerConnection connection =
                mPeerConnectionFactory.createPeerConnection(rtcConfig,
                        mPeerConnectionObserver); // PC的observer
        if (connection == null) {
            Logger.d("Failed to createPeerConnection !");
            return null;
        }

        List<String> mediaStreamLabels = Collections.singletonList("ARDAMS");
        connection.addTrack(mVideoTrack, mediaStreamLabels);
        //connection.addTrack(mAudioTrack, mediaStreamLabels);

        return connection;
    }

    VideoEncoderFactory encoderFactory;
    VideoDecoderFactory decoderFactory;

    public PeerConnectionFactory createPeerConnectionFactory(Context context) {
        /*final VideoEncoderFactory encoderFactory;
        final VideoDecoderFactory decoderFactory;*/

        encoderFactory = new DefaultVideoEncoderFactory(
                mRootEglBase.getEglBaseContext(),
                true /* enableIntelVp8Encoder */,
                false);
        decoderFactory = new DefaultVideoDecoderFactory(mRootEglBase.getEglBaseContext());

        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        options.networkIgnoreMask = 32;

        PeerConnectionFactory.initialize(PeerConnectionFactory.InitializationOptions.builder(context)
                .setEnableInternalTracer(true)
                .setFieldTrials("IncludeWifiDirect/Enabled/")
                //.setFieldTrials("WebRTC-MediaTekH264/Enabled/")
                .createInitializationOptions());

        PeerConnectionFactory.Builder builder = PeerConnectionFactory.builder()
                .setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory);
        builder.setOptions(options);

        return builder.createPeerConnectionFactory();
    }

    public static class SimpleSdpObserver implements SdpObserver {
        @Override
        public void onCreateSuccess(SessionDescription sessionDescription) {
            Logger.d("SdpObserver: onCreateSuccess !");
        }

        @Override
        public void onSetSuccess() {
            Logger.d("SdpObserver: onSetSuccess");
        }

        @Override
        public void onCreateFailure(String msg) {
            Logger.d("SdpObserver onCreateFailure: " + msg);
        }

        @Override
        public void onSetFailure(String msg) {
            Logger.d("SdpObserver onSetFailure: " + msg);
        }
    }

    public final class ServiceBinder extends Binder {
        public CallService getService() {
            return CallService.this;
        }
    }


    //===================================================
    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_REMOTE_CAMERA_CONNECTING) {
                if (isRemoteCameraConnectingMode) {
                    Log.i(TAG, "doRemoteCameraConnect()");
                    doRemoteCameraConnect();
                }
            } else if (msg.what == MSG_EXIT_WEBRTC_PREVIEW) {
                String exitMSg = msg.getData().getString(KEY_EXIT_WEBRTC_PREVIEW_MSG);
                exitWebrtcPreview(exitMSg);
            }
        }
    };

    public static final int MSG_REMOTE_CAMERA_CONNECTING = 10000;
    public static final int MSG_EXIT_WEBRTC_PREVIEW = 10001;
    private static final String KEY_EXIT_WEBRTC_PREVIEW_MSG = "EXIT_WEBRTC_PREVIEW";
    private static final long REMOTE_CAMERA_CONNECTING_SPAN_TIME = 2000;
    private RemoteCameraClient remoteCameraClient;
    private boolean isRemoteCameraConnecting = false; //是否正在进行websocket连接
    public boolean isRemoteCameraConnectingMode = false;  //是否处于-连接模式

    private static final String REMOTE_CAMERA_TAG = "RemoteCamera";

    public void startActingRemoteCamera() {
        isRemoteCameraConnectingMode = true;
        countRemoteCameraConnectTimes = 0;
        mHandler.removeMessages(MSG_REMOTE_CAMERA_CONNECTING);
        mHandler.sendEmptyMessageDelayed(MSG_REMOTE_CAMERA_CONNECTING, REMOTE_CAMERA_CONNECTING_SPAN_TIME);
    }

    public void stopActingRemoteCamera() {
        isRemoteCameraConnectingMode = false;
        countRemoteCameraConnectTimes = 0;
        mHandler.removeMessages(MSG_REMOTE_CAMERA_CONNECTING);
        if (remoteCameraClient != null) {
            remoteCameraClient.isCareResp = false;
            remoteCameraClient.close();
        }
    }

    private void onRemoteCameraCheckResp(boolean isExist) {
        isRemoteCameraConnecting = false;
        if (!isExist) {
            mHandler.removeMessages(MSG_REMOTE_CAMERA_CONNECTING);
            mHandler.sendEmptyMessageDelayed(MSG_REMOTE_CAMERA_CONNECTING, REMOTE_CAMERA_CONNECTING_SPAN_TIME);
        }
    }

    private int countRemoteCameraConnectTimes = 0;

    private synchronized void doRemoteCameraConnect() {
        if (isRemoteCameraConnecting) {
            return;
        }
        isRemoteCameraConnecting = true;
        try {
            if (!TextUtils.isEmpty(selectedRemoteIp)) {
                URI useURI = new URI("ws://" + selectedRemoteIp + ":" + selectedRemotePort);
                if (remoteCameraClient != null && remoteCameraClient.isOpen()) {
                    if (remoteCameraClient.serverUri.toString().equals(useURI.toString())) {
                        onRemoteCameraCheckResp(true);
                        return;
                    }
                    remoteCameraClient.isCareResp = false;
                    remoteCameraClient.close();
                }
                remoteCameraClient = new RemoteCameraClient(useURI);
                Log.i(REMOTE_CAMERA_TAG, "doRemoteCameraConnect()==> selectedRemoteIp=" + selectedRemoteIp + ", selectedRemotePort=" + selectedRemotePort + ", backupRemoteIp=" + backupRemoteIp);
                countRemoteCameraConnectTimes++;
                remoteCameraClient.connect();
            } else {
                onRemoteCameraCheckResp(false);
            }
        } catch (URISyntaxException e) {
            Logger.e(REMOTE_CAMERA_TAG, "doRemoteCameraConnect==>" + e);
            onRemoteCameraCheckResp(false);
        }
    }

    private void exitWebrtcPreview(String msg) {
        Intent intent = new Intent();
        intent.setAction(BaseConstants.ACTION_EXIT_WEBRTC_PREVIEW);
        intent.setPackage(getPackageName());
        sendBroadcast(intent);

        showUnConnectDialog();
    }

    private void sendExitWebrtcPreviewMsg(String msg) {
        Message message = Message.obtain(mHandler);
        message.what = MSG_EXIT_WEBRTC_PREVIEW;
        Bundle bundle = new Bundle();
        bundle.putString(KEY_EXIT_WEBRTC_PREVIEW_MSG, msg);
        message.setData(bundle);
        mHandler.sendMessage(message);
    }

    class RemoteCameraClient extends WebSocketClient {
        public URI serverUri;
        public boolean isCareResp = true;

        public RemoteCameraClient(URI serverUri) {
            super(serverUri, new Draft_6455(), null, 1500);
            this.serverUri = serverUri;
        }

        @Override
        public void onOpen(ServerHandshake handShakeData) {
            Log.i(REMOTE_CAMERA_TAG, "onOpen==> ");
            countRemoteCameraConnectTimes = 0;
            if (isCareResp) {
                onRemoteCameraCheckResp(true);
            }

            try {
                JSONObject message = new JSONObject();
                message.put("type", "ZeeDevHelperClientInfo");
                String localIp = null;
                if (remoteCameraClient.getLocalSocketAddress() != null && remoteCameraClient.getLocalSocketAddress().getAddress() != null) {
                    localIp = remoteCameraClient.getLocalSocketAddress().getAddress().getHostAddress();
                }
                if (localIp == null) {
                    localIp = NetworkUtil.getLocalIpAddress();
                }

                message.put("ip", localIp);
                message.put("port", BaseConstants.WEBRTC_SIGNAL_SERVER_PORT);
                message.put("deviceName", CommonUtils.getDeviceName(CallService.this));
                remoteCameraClient.send(message.toString());
            } catch (JSONException ignored) {
            }
        }

        @Override
        public void onMessage(final String message) {
            Log.i(REMOTE_CAMERA_TAG, "onMessage==> costTime=");
            try {
                JSONObject jsonMessage = new JSONObject(message);
                String type = jsonMessage.getString("type");
                if (type.equals("KickOut")) {
                    sendExitWebrtcPreviewMsg("有新手机连入了！");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            Log.i(REMOTE_CAMERA_TAG, "onClose==>code=" + code + ", reason=" + reason
                    + ", remote=" + remote + ", countRemoteCameraConnectTimes=" + countRemoteCameraConnectTimes + ", backupRemoteIp=" + backupRemoteIp);

            if (isCareResp) {
                onRemoteCameraCheckResp(false);
            }

            if (!NetworkUtil.isWifiConnected(CallService.this)) {
                onRemoteCameraCheckResp(false);
                sendExitWebrtcPreviewMsg("连接断开了！");
            }

            if (countRemoteCameraConnectTimes == 3) {
                if (!TextUtils.isEmpty(backupRemoteIp) && "192.168.49.1".equals(selectedRemoteIp)) {
                    selectedRemoteIp = backupRemoteIp;
                }
            } else if (countRemoteCameraConnectTimes >= 5) {
                sendExitWebrtcPreviewMsg("连接断开了！");
            }
        }

        @Override
        public void onError(Exception ex) {
            ex.printStackTrace();
            Log.e(REMOTE_CAMERA_TAG, "onError==>" + ex);
        }
    }

    private void showUnConnectDialog() {
        final CustomAlertDialog customAlertDialog = new CustomAlertDialog(this);
        customAlertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        customAlertDialog.setMessageText("当前手机摄像头与盒子已断开连接，请重新连接");
        customAlertDialog.setConfirmText("确认");
        customAlertDialog.setOnClickListener(new CustomAlertDialog.OnClickListener() {
            @Override
            public void onConfirm(View v) {
                customAlertDialog.cancel();
            }

            @Override
            public void onPositive(View v) {

            }

            @Override
            public void onCancel(View v) {

            }
        });
        customAlertDialog.show();
    }
}
