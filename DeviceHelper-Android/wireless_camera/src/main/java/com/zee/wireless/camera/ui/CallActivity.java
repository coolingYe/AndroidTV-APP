package com.zee.wireless.camera.ui;


import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.card.MaterialCardView;
import com.zee.device.base.config.BaseConstants;
import com.zee.device.base.utils.DensityUtils;
import com.zee.device.base.utils.Logger;
import com.zee.device.base.utils.NetworkUtil;
import com.zee.wireless.camera.R;
import com.zee.wireless.camera.service.CallService;


import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class CallActivity extends AppCompatActivity {
    private static final String TAG = "CallActivity";
    private SignalServer mServer;
    private WebSocket webSocketClient;

    /**
     * ---------和webrtc相关-----------
     */
    // 视频信息
    private static final int VIDEO_RESOLUTION_WIDTH = 960;
    private static final int VIDEO_RESOLUTION_HEIGHT = 540;
    private static final int VIDEO_FPS = 30;

    // 打印log
    private TextView mLogcatView;

    // Opengl es
    private EglBase mRootEglBase;
    // 纹理渲染
    private SurfaceTextureHelper mSurfaceTextureHelper;

    private SurfaceViewRenderer mLocalSurfaceView;
    private SurfaceViewRenderer mRemoteSurfaceView;

    // 音视频数据
    public static final String VIDEO_TRACK_ID = "1";//"ARDAMSv0";
    public static final String AUDIO_TRACK_ID = "2";//"ARDAMSa0";
    private VideoTrack mVideoTrack;
    //private AudioTrack mAudioTrack;

    // 视频采集
    private VideoCapturer mVideoCapturer;

    //用于数据传输
    private PeerConnection mPeerConnection;
    private PeerConnectionFactory mPeerConnectionFactory;
    private boolean isFrontFacing = false;

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

    private void switchCamera() {
        if (mVideoCapturer != null) {
            CameraVideoCapturer cameraVideoCapturer = (CameraVideoCapturer) mVideoCapturer;
            if (isFrontFacing) {
                cameraVideoCapturer.switchCamera(cameraSwitchHandler);
            } else {
                cameraVideoCapturer.switchCamera(cameraSwitchHandler, frontCameraId);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        hideSystemUI();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        DensityUtils.autoWidth(getApplication(), this);
        setContentView(R.layout.activity_call);

        ConstraintLayout back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        MaterialCardView cardCameraSwitchView = findViewById(R.id.card_camera_switch);
        cardCameraSwitchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchCamera();
            }
        });

        // 用户打印信息
        mLogcatView = findViewById(R.id.LogcatView);

        mRootEglBase = EglBase.create();

        // 用于展示本地和远端视频
        mLocalSurfaceView = findViewById(R.id.localSurfaceView);
        mRemoteSurfaceView = findViewById(R.id.remoteSurfaceView);

        mLocalSurfaceView.init(mRootEglBase.getEglBaseContext(), null);
        //mLocalSurfaceView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        mLocalSurfaceView.setMirror(true);
        //mLocalSurfaceView.setEnableHardwareScaler(false /* enabled */);

        mRemoteSurfaceView.init(mRootEglBase.getEglBaseContext(), null);
        /*mRemoteSurfaceView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        mRemoteSurfaceView.setMirror(true);
        mRemoteSurfaceView.setEnableHardwareScaler(true *//* enabled *//*);*/
        mRemoteSurfaceView.setZOrderMediaOverlay(true); // 注意这句，因为2个surfaceview是叠加的

        // 创建PC factory , PC就是从factory里面获取的
        mPeerConnectionFactory = createPeerConnectionFactory(getApplicationContext());

        // NOTE: this _must_ happen while PeerConnectionFactory is alive!
        Logging.enableLogToDebugOutput(Logging.Severity.LS_VERBOSE);

        // 创建视频采集器
        mVideoCapturer = createVideoCapturer();

        if (frontCameraId == null) {
            cardCameraSwitchView.setVisibility(View.GONE);
        }

        mSurfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", mRootEglBase.getEglBaseContext());
        VideoSource videoSource = mPeerConnectionFactory.createVideoSource(false);
        if (mVideoCapturer != null)
            mVideoCapturer.initialize(mSurfaceTextureHelper, getApplicationContext(), videoSource.getCapturerObserver());

        mVideoTrack = mPeerConnectionFactory.createVideoTrack(VIDEO_TRACK_ID, videoSource);
        mVideoTrack.setEnabled(true);
        mVideoTrack.addSink(mLocalSurfaceView); // 设置渲染到本地surfaceview上

        //AudioSource 和 AudioTrack 与VideoSource和VideoTrack相似，只是不需要AudioCapturer 来获取麦克风，
        /*AudioSource audioSource = mPeerConnectionFactory.createAudioSource(new MediaConstraints());
        mAudioTrack = mPeerConnectionFactory.createAudioTrack(AUDIO_TRACK_ID, audioSource);
        mAudioTrack.setEnabled(false);*/

        /** ---------开始启动信令服务----------- */

        mServer = new SignalServer(BaseConstants.WEBRTC_SIGNAL_SERVER_PORT);
        mServer.setReuseAddr(true);
        mServer.start();

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 开始采集并本地显示
        if (mVideoCapturer != null)
            mVideoCapturer.startCapture(VIDEO_RESOLUTION_WIDTH, VIDEO_RESOLUTION_HEIGHT, VIDEO_FPS);
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            // 停止采集
            if (mVideoCapturer != null)
                mVideoCapturer.stopCapture();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 注意这里退出时的销毁动作
    @Override
    protected void onDestroy() {
        Logger.d(TAG, "onDestroy()");
        doLeave();
        if (mVideoTrack != null) {
            mVideoTrack.removeSink(mLocalSurfaceView);
        }
        mLocalSurfaceView.release();
        mRemoteSurfaceView.release();

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
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Logger.d(TAG, "onDestroy() done");
        super.onDestroy();
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

    private void updateCallState(boolean idle) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (idle) {
                    mRemoteSurfaceView.setVisibility(View.GONE);
                } else {
                    mRemoteSurfaceView.setVisibility(View.VISIBLE);
                }
            }
        });
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

    private String frontCameraId = null;

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
                remoteVideoTrack.addSink(mRemoteSurfaceView);
            }
        }
    };

    private void sendMessage(JSONObject message) {
        mServer.broadcast(message.toString());
    }

    private void sendMessage(String message) {
        mServer.broadcast(message);
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

    private void onRemoteHangup() {
        printInfoOnScreen("Receive Remote Hangup Event ...");
        doLeave();
    }

    private void printInfoOnScreen(String msg) {
        Logger.d(msg);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String output = mLogcatView.getText() + "\n" + msg;
                mLogcatView.setText(output);
            }
        });
    }


    /*private void showToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }*/

    class SignalServer extends WebSocketServer {

        public SignalServer(int port) {
            super(new InetSocketAddress(port));
        }

        @Override
        public void onOpen(WebSocket conn, ClientHandshake handshake) {
            Logger.d("=== SignalServer onOpen()");
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
                Logger.d("=== SignalServer onMessage() message=" + message + " " + conn.getRemoteSocketAddress().getHostString() );
            } else {
                Logger.d("=== SignalServer onMessage() message=" + message );
            }
            try {
                JSONObject jsonMessage = new JSONObject(message);

                String type = jsonMessage.getString("type");
                if (type.equals("doStartCall")) {
                    if (!TextUtils.isEmpty(CallService.selectedRemoteIp) && conn.getRemoteSocketAddress() != null && !TextUtils.isEmpty(conn.getRemoteSocketAddress().getHostString())) {
                        if (CallService.selectedRemoteIp.equals(conn.getRemoteSocketAddress().getHostString())) {
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
}