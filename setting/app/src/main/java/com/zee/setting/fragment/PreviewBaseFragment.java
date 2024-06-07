package com.zee.setting.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import com.zee.setting.R;
import com.zee.setting.config.Config;
import com.zee.setting.utils.Logger;
import com.zee.setting.utils.SystemProperties;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.DataChannel;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.MediaStreamTrack;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RendererCommon;
import org.webrtc.RtpReceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoDecoderFactory;
import org.webrtc.VideoEncoderFactory;
import org.webrtc.VideoFrame;
import org.webrtc.VideoSink;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;

public class PreviewBaseFragment extends Fragment {

    private static final String TAG = "PreviewBaseFragment";
    public TextureView textureView;
    private Context context;
    public boolean isCameraOpened = false; // 是否有相机已打开
    //===================webrtc============================
    private SignalClient mClient;
    // Opengl es
    private EglBase mRootEglBase;
    private SurfaceViewRenderer mRemoteSurfaceView;
    private SurfaceTextureHelper mSurfaceTextureHelper;
    // 音视频数据
    public static final String VIDEO_TRACK_ID = "1";
    public static final String AUDIO_TRACK_ID = "2";
    private VideoTrack mVideoTrack;
    private AudioTrack mAudioTrack;

    // 视频采集
    private VideoCapturer mVideoCapturer;

    //用于数据传输
    private PeerConnection mPeerConnection;
    private PeerConnectionFactory mPeerConnectionFactory;

    private boolean isRemoteFrontFacing = true;
    private int remoteDevOrientation = Configuration.ORIENTATION_PORTRAIT;
    //===================webrtc============================


    public PreviewBaseFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.context = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_preview, container, false);
        textureView = view.findViewById(R.id.texture_view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        int rotation = windowManager.getDefaultDisplay().getRotation();
        int previewWidth = 480;
        int previewHeight = 270;

        if(Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation){
            previewWidth = 270;
            previewHeight = 480;

            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) textureView.getLayoutParams();
            layoutParams.width = previewWidth;
            layoutParams.height = previewHeight;
            layoutParams.gravity = Gravity.CENTER;
            textureView.setLayoutParams(layoutParams);
        }

        //-------webrtc----------
        mRemoteSurfaceView = view.findViewById(R.id.remoteSurfaceView);
        mRemoteSurfaceView.setAlpha(1);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mRemoteSurfaceView.getLayoutParams();
        layoutParams.width = previewWidth;
        layoutParams.height = previewHeight;
        layoutParams.gravity = Gravity.CENTER;
        mRemoteSurfaceView.setLayoutParams(layoutParams);
        //-------webrtc----------

        initWebrtc();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(mPeerConnection != null){
            mPeerConnection.dispose();
            mPeerConnection = null;
        }
        if (mVideoCapturer != null)
            mVideoCapturer.dispose();

        if (mSurfaceTextureHelper != null)
            mSurfaceTextureHelper.dispose();

        if (mRemoteSurfaceView != null)
            mRemoteSurfaceView.release();

        if(mVideoTrack != null)
            mVideoTrack.dispose();

        if(mAudioTrack != null)
            mAudioTrack.dispose();

        PeerConnectionFactory.stopInternalTracingCapture();
        PeerConnectionFactory.shutdownInternalTracer();
        if(mPeerConnectionFactory != null)
            mPeerConnectionFactory.dispose();

        if(mClient != null){
            mClient.close();
        }
    }

    private void initWebrtc(){
        mRootEglBase = EglBase.create();
        mRemoteSurfaceView.setVisibility(View.INVISIBLE);
        mRemoteSurfaceView.init(mRootEglBase.getEglBaseContext(), null);
        mRemoteSurfaceView.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        mRemoteSurfaceView.setMirror(true);
        mRemoteSurfaceView.setEnableHardwareScaler(true);
        mRemoteSurfaceView.setZOrderMediaOverlay(true);

        // 创建PC factory , PC就是从factory里面获取的
        mPeerConnectionFactory = createPeerConnectionFactory(context.getApplicationContext());

        // NOTE: this _must_ happen while PeerConnectionFactory is alive!
        //Logging.enableLogToDebugOutput(Logging.Severity.LS_VERBOSE);

        // 创建视频采集器
        mVideoCapturer = null;//createVideoCapturer();

        // 纹理渲染
        mSurfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", mRootEglBase.getEglBaseContext());
        VideoSource videoSource = mPeerConnectionFactory.createVideoSource(false);
        if(mVideoCapturer != null)
            mVideoCapturer.initialize(mSurfaceTextureHelper, context.getApplicationContext(), videoSource.getCapturerObserver());

        mVideoTrack = mPeerConnectionFactory.createVideoTrack(VIDEO_TRACK_ID, videoSource);
        mVideoTrack.setEnabled(false);
        //mVideoTrack.addSink(mLocalSurfaceView); // 设置渲染到本地surfaceview上

        //AudioSource 和 AudioTrack 与VideoSource和VideoTrack相似，只是不需要AudioCapturer 来获取麦克风，
        AudioSource audioSource = mPeerConnectionFactory.createAudioSource(new MediaConstraints());
        mAudioTrack = mPeerConnectionFactory.createAudioTrack(AUDIO_TRACK_ID, audioSource);
        mAudioTrack.setEnabled(false);
    }

    private boolean isOnWebrtcConnecting = false;
    public synchronized void doWebrtcConnect(){
        if (isOnWebrtcConnecting) return;
        isOnWebrtcConnecting = true;
        try {
            if (mClient != null && mClient.isOpen()) {
                isOnWebrtcConnecting = false;
                return;
            }

            String webrtcServerInfo = SystemProperties.get(Config.PERSIST_DATA_WEBRTC_SERVER_INFO);
            String[] infoArray = webrtcServerInfo.split(";");
            if(infoArray.length >= 2) {
                mClient = new SignalClient(new URI("ws://" + infoArray[0] + ":" + infoArray[1]));
                webrtcConnectCostTime = System.currentTimeMillis();
                mClient.connect();
            } else {
                onRemoteCameraOpenFailed();
            }
        } catch (URISyntaxException e) {
            onRemoteCameraOpenFailed();
        }
    }

    private void onRemoteCameraOpenFailed() {
        isOnWebrtcConnecting = false;
        if (mClient != null && mClient.handleClosed) {
            mClient.handleClosed = false;
        }
    }

    private static long webrtcConnectCostTime = 0;
    private static final int CODE_WEBSOCKET_CLOSE_NOT_LEAVE= 1000;
    public synchronized void doWebrtcDisconnect(){
        if(mClient != null && mClient.isOpen()){
            mClient.close(CODE_WEBSOCKET_CLOSE_NOT_LEAVE);
        }
    }

    public void onRemoteCameraOpened() {}

    private void onIceConnected(){
        isCameraOpened = true;
        textureView.setAlpha(0);
        mRemoteSurfaceView.post(() -> mRemoteSurfaceView.setVisibility(View.VISIBLE));
        onRemoteCameraOpened();
    }

    private void onSignalDisconnected(){
        isCameraOpened = false;
        mRemoteSurfaceView.post(() -> {
            mRemoteSurfaceView.setVisibility(View.INVISIBLE);
            int hashCode = mClient.hashCode();
            mRemoteSurfaceView.postDelayed(() -> {
                if (mClient != null && mClient.hashCode() == hashCode && mClient.isOpen()) {
                    Logger.i(TAG, "onSignalDisconnected() call mClient.close()");
                    mClient.close();
                }
            }, 1500);
        });
    }

    public void doAnswerCall() {
        Logger.d("Answer Call, Wait ...");

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
        Logger.d("doLeave() Leave room!");
        Logger.d("Hangup Call, Wait ...");

        if (mPeerConnection != null) {
            mPeerConnection.dispose();
            mPeerConnection = null;

        }
        Logger.d("doLeave() Hangup Done.");
        //updateCallState(true);
    }

    public PeerConnection createPeerConnection() {
        Logger.d("Create PeerConnection ...");

        LinkedList<PeerConnection.IceServer> iceServers = new LinkedList<>();

        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);
        // TCP candidates are only useful when connecting to a server that supports
        // ICE-TCP.

        rtcConfig.enableCpuOveruseDetection = false;
        rtcConfig.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_ONCE;
        rtcConfig.sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN;
        PeerConnection connection =
                mPeerConnectionFactory.createPeerConnection(rtcConfig,
                        mPeerConnectionObserver); // PC的observer
        if (connection == null) {
            Logger.d( "Failed to createPeerConnection !");
            return null;
        }
        return connection;
    }

    public PeerConnectionFactory createPeerConnectionFactory(Context context) {
        final VideoEncoderFactory encoderFactory;
        final VideoDecoderFactory decoderFactory;

        encoderFactory = new DefaultVideoEncoderFactory(
                mRootEglBase.getEglBaseContext(),
                true,
                true);
        decoderFactory = new DefaultVideoDecoderFactory(mRootEglBase.getEglBaseContext());

        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        options.networkIgnoreMask = 32;

        PeerConnectionFactory.initialize(PeerConnectionFactory.InitializationOptions.builder(context)
                .setFieldTrials("IncludeWifiDirect/Enabled/")
                .createInitializationOptions());

        PeerConnectionFactory.Builder builder = PeerConnectionFactory.builder()
                .setVideoEncoderFactory(encoderFactory)
                .setVideoDecoderFactory(decoderFactory);
        builder.setOptions(options);


        return builder.createPeerConnectionFactory();
    }

    private final PeerConnection.Observer mPeerConnectionObserver = new PeerConnection.Observer() {
        @Override
        public void onSignalingChange(PeerConnection.SignalingState signalingState) {
            Logger.d("onSignalingChange: " + signalingState);
        }

        @Override
        public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
            Logger.d("onIceConnectionChange: " + iceConnectionState + ", costTime=" + (System.currentTimeMillis() - webrtcConnectCostTime));
            if (PeerConnection.IceConnectionState.CONNECTED == iceConnectionState) {
                onIceConnected();
            }  else if (PeerConnection.IceConnectionState.CLOSED == iceConnectionState || PeerConnection.IceConnectionState.DISCONNECTED == iceConnectionState) {
                if (mClient != null && mClient.handleClosed) {
                    mClient.handleClosed = false;
                    onSignalDisconnected();
                }
            }
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
                //remoteVideoTrack.addSink(mRemoteSurfaceView);
                remoteVideoTrack.addSink(new VideoSink() {
                    @Override
                    public void onFrame(VideoFrame videoFrame) {
                        if(Configuration.ORIENTATION_PORTRAIT == remoteDevOrientation) {
                            if (mRemoteSurfaceView.getWidth() > mRemoteSurfaceView.getHeight()) {
                                mRemoteSurfaceView.onFrame(new VideoFrame(videoFrame.getBuffer(), 0, videoFrame.getTimestampNs()));
                            } else {
                                if (isRemoteFrontFacing) {
                                    mRemoteSurfaceView.onFrame(new VideoFrame(videoFrame.getBuffer(), 270, videoFrame.getTimestampNs()));
                                } else {
                                    mRemoteSurfaceView.onFrame(new VideoFrame(videoFrame.getBuffer(), 90, videoFrame.getTimestampNs()));
                                }
                            }
                        } else { //remote is landscape
                            if(mRemoteSurfaceView.getWidth() > mRemoteSurfaceView.getHeight()){
                                mRemoteSurfaceView.onFrame(videoFrame);
                            }else{
                                if(isRemoteFrontFacing) {
                                    mRemoteSurfaceView.onFrame(new VideoFrame(videoFrame.getBuffer(), 270, videoFrame.getTimestampNs()));
                                }else{
                                    mRemoteSurfaceView.onFrame(new VideoFrame(videoFrame.getBuffer(), 90, videoFrame.getTimestampNs()));
                                }
                            }
                        }
                    }
                });
            }
        }
    };


    private void updateCallState(boolean idle) {
        mRemoteSurfaceView.post(new Runnable() {
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

    public static class SimpleSdpObserver implements SdpObserver {
        @Override
        public void onCreateSuccess(SessionDescription sessionDescription) {
            Logger.d( "SdpObserver: onCreateSuccess !");
        }

        @Override
        public void onSetSuccess() {
            Logger.d("SdpObserver: onSetSuccess");
        }

        @Override
        public void onCreateFailure(String msg) {
            Logger.d( "SdpObserver onCreateFailure: " + msg);
        }

        @Override
        public void onSetFailure(String msg) {
            Logger.d("SdpObserver onSetFailure: " + msg);
        }
    }

    private void sendMessage(JSONObject message) {
        if(mClient != null){
            mClient.send(message.toString());
        }
    }

    private void sendMessage(String message) {
        if(mClient != null){
            mClient.send(message);
        }
    }

    class SignalClient extends WebSocketClient {
        public boolean handleClosed = true;

        public SignalClient(URI serverUri) {
            super(serverUri, (Draft)(new Draft_6455()), null, 5000);
        }

        @Override
        public void onOpen(ServerHandshake handShakeData) {
            Logger.d("=== SignalClient onOpen() " + ", costTime=" + (System.currentTimeMillis() - webrtcConnectCostTime));
            Logger.d("连接服务端成功...创建PC");
            isOnWebrtcConnecting = false;
            //这里应该创建PeerConnection
            if (mPeerConnection == null) {
                mPeerConnection = createPeerConnection();
            }

            try {
                JSONObject jsonMessage = new JSONObject();
                jsonMessage.put("type", "doStartCall");
                sendMessage(jsonMessage);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

        }

        @Override
        public void onMessage(final String message) {
            Logger.d("=== SignalClient onMessage(): message="+message);
            isOnWebrtcConnecting = false;
            try {
                JSONObject jsonMessage = new JSONObject(message);

                String type = jsonMessage.getString("type");
                if (type.equals("offer")) {
                    onRemoteOfferReceived(jsonMessage);
                }else if(type.equals("answer")) {
                    onRemoteAnswerReceived(jsonMessage);
                }else if(type.equals("candidate")) {
                    onRemoteCandidateReceived(jsonMessage);
                }else if(type.equals("previewInfo")) {
                    onPreviewInfoReceived(jsonMessage);
                }else{
                    Logger.e("the type is invalid: " + type);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            Logger.d("=== SignalClient onClose(): reason="+reason+", remote="+remote + ", code=" + code);
            Logger.d("和服务端断开...调用doLeave " + ", costTime=" + (System.currentTimeMillis() - webrtcConnectCostTime));
            isOnWebrtcConnecting = false;
            if (mPeerConnection != null) {
                mPeerConnection.dispose();
                mPeerConnection = null;
            }

            if (handleClosed) {
                handleClosed = false;
                onSignalDisconnected();
            }
        }

        @Override
        public void onError(Exception ex) {
            ex.printStackTrace();
            Logger.d("=== SignalClient onError() ex="+ex.getMessage());
        }
    }

    // 接听方，收到offer
    private void onRemoteOfferReceived(JSONObject message) {
        Logger.d("Receive Remote Call ...");

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
            Logger.d("收到offer...调用doAnswerCall");
            doAnswerCall();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 发送方，收到answer
    private void onRemoteAnswerReceived(JSONObject message) {
        Logger.d("Receive Remote Answer ...");
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
        updateCallState(false);
    }

    // 收到对端发过来的candidate
    private void onRemoteCandidateReceived(JSONObject message) {
        Logger.d("Receive Remote Candidate ...");
        try {
            // candidate 候选者描述信息
            // sdpMid 与候选者相关的媒体流的识别标签
            // sdpMLineIndex 在SDP中m=的索引值
            // usernameFragment 包括了远端的唯一识别
            IceCandidate remoteIceCandidate =
                    new IceCandidate(message.getString("id"),
                            message.getInt("label"),
                            message.getString("candidate"));

            mPeerConnection.addIceCandidate(remoteIceCandidate);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void onPreviewInfoReceived(JSONObject message) {
        Logger.d("Receive Remote PreviewInfo ...");
        try {
            isRemoteFrontFacing = message.getBoolean("isFrontFacing");
            remoteDevOrientation = message.getInt("orientation");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}