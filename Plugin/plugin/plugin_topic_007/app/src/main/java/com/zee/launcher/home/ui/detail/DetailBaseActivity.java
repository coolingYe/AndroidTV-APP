package com.zee.launcher.home.ui.detail;

import android.text.TextUtils;

import com.zeewain.base.ui.BaseActivity;
import com.zeewain.base.utils.CareLog;
import com.zeewain.base.utils.SystemProperties;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class DetailBaseActivity extends BaseActivity {
    private static final String TAG = "DetailBase";
    private static final String PERSIST_DATA_WEBRTC_SERVER_INFO = "persist.sys.zee.webrtc.server.info";
    private SignalClient mClient;
    private long startConnectTime = 0;
    private boolean isConnecting = false;

    @Override
    protected void onPause() {
        super.onPause();
        if (mClient != null && mClient.isOpen()) {
            mClient.close();
        }
        isConnecting = false;
    }

    public void onRemoteCameraCheckResp(boolean isExist) {
        isConnecting = false;
    }

    public synchronized void checkConnection() {
        if (isConnecting) {
            return;
        }
        CareLog.i(TAG, "doWebrtcConnect()");
        isConnecting = true;
        try {
            startConnectTime = System.currentTimeMillis();
            String webrtcServerInfo = SystemProperties.get(PERSIST_DATA_WEBRTC_SERVER_INFO);
            if (TextUtils.isEmpty(webrtcServerInfo)) {
                onRemoteCameraCheckResp(false);
                return;
            }
            String[] infoArray = webrtcServerInfo.split(";");
            if (infoArray.length >= 2) {
                URI useURI = new URI("ws://" + infoArray[0] + ":" + infoArray[1]);
                if (mClient != null && mClient.isOpen()) {
                    if (mClient.serverUri.toString().equals(useURI.toString())) {
                        onRemoteCameraCheckResp(true);
                        return;
                    }
                    mClient.close();
                }
                mClient = new SignalClient(useURI);
                CareLog.i(TAG, "mClient.connect()==>>");
                mClient.connect();
            } else {
                onRemoteCameraCheckResp(false);
            }
        } catch (URISyntaxException e) {
            CareLog.e(TAG, "doWebrtcConnect==>" + e);
            onRemoteCameraCheckResp(false);
        }
    }

    class SignalClient extends WebSocketClient {
        public URI serverUri;

        public SignalClient(URI serverUri) {
            super(serverUri, new Draft_6455(), null, 1500);
            this.serverUri = serverUri;
        }

        @Override
        public void onOpen(ServerHandshake handShakeData) {
            CareLog.i(TAG, "onOpen==> costTime=" + (System.currentTimeMillis() - startConnectTime));
        }

        @Override
        public void onMessage(final String message) {
            CareLog.i(TAG, "onMessage==> costTime=" + (System.currentTimeMillis() - startConnectTime));
            onRemoteCameraCheckResp(true);
            mClient.close();
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            CareLog.i(TAG, "onClose==>code=" + code + ", reason=" + reason + ", remote=" + remote);
        }

        @Override
        public void onError(Exception ex) {
            ex.printStackTrace();
            CareLog.e(TAG, "onError==>" + ex);
            onRemoteCameraCheckResp(false);
        }
    }
}

