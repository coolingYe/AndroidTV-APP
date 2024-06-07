package com.zee.device.helper.ui.main;


import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.zee.device.base.config.BaseConstants;
import com.zee.device.base.model.DeviceInfo;
import com.zee.device.base.utils.Logger;
import com.zee.device.base.utils.NetworkUtil;
import com.zee.device.helper.ZeeApplication;
import com.zee.device.helper.data.model.ConnectionState;
import com.zee.device.helper.data.model.ConnectionType;
import com.zee.device.helper.data.model.DataConnectionState;
import com.zee.wireless.camera.service.CallService;
import com.zee.wireless.camera.ui.CallActivity;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;


public class MainWebrtcActivity extends MainBaseActivity {
    private static final String TAG = "MainWebrtcActivity";
    private MessageClient mMessageClient;
    public DeviceInfo selectedDeviceInfo;
    public DataConnectionState dataConnectionState = new DataConnectionState(ConnectionState.Connecting, ConnectionType.WiFiP2P);

    private final ActivityResultLauncher<Intent> callActivityLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        onExistCallActivity();
    });

    public void onExistCallActivity() {

    }

    public void onEnterCallActivity() {

    }

    public void gotoCallActivity() {
        Intent intent = new Intent();
        intent.setClass(this, CallActivity.class);
        callActivityLauncher.launch(intent);
        onEnterCallActivity();
    }

    public void startCallService() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!Settings.canDrawOverlays(MainWebrtcActivity.this)) {
                    startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())), 0);
                } else {
                    try {
                        ZeeApplication.getInstance().mServiceConnection = new ServiceConnection() {
                            @Override
                            public void onServiceConnected(ComponentName name, IBinder service) {
                                if (service != null) {
                                    CallService.ServiceBinder binder = (CallService.ServiceBinder) service;
                                    ZeeApplication.getInstance().mService = binder.getService();
                                    binder.getService().setCloseCallback(o -> {
                                        ZeeApplication.getInstance().unBindService();
                                    });
                                }
                            }

                            @Override
                            public void onServiceDisconnected(ComponentName name) {
                                Log.d(TAG, "onServiceDisconnected: ");
                            }
                        };
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    ZeeApplication.getInstance().bindGService(MainWebrtcActivity.this);
                    onEnterCallActivity();
                }
            }
        });

    }


    public void sendMsgForStartCall(String hostAddress, int port) {
        try {
            Logger.i(TAG, "sendMsgForStartCall() getHostAddress=" + hostAddress);
            @SuppressLint("DefaultLocale")
            String uriString = String.format("ws://%s:%d", hostAddress, port);
            URI uri = new URI(uriString);
            if (mMessageClient != null) {
                mMessageClient.close();
            }
            mMessageClient = new MessageClient(uri);
            mMessageClient.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            Logger.e(TAG, e.toString());

            dataConnectionState.updateState(ConnectionState.ConnectFailed, "连接失败！");
            viewModel.mldConnectionState.setValue(dataConnectionState);
        }
    }

    public void onRemoteMessageDone() {
        dataConnectionState.updateState(ConnectionState.Connected, "");
        viewModel.mldConnectionState.postValue(dataConnectionState);
        startCallService();
    }

    class MessageClient extends WebSocketClient {

        public MessageClient(URI serverUri) {
            super(serverUri, new Draft_6455(), null, 2000);
        }

        @Override
        public void onOpen(ServerHandshake handshake) {
            try {
                String localIp = null;
                if (mMessageClient.getLocalSocketAddress() != null && mMessageClient.getLocalSocketAddress().getAddress() != null) {
                    localIp = mMessageClient.getLocalSocketAddress().getAddress().getHostAddress();
                }
                if (localIp == null) {
                    localIp = NetworkUtil.getLocalIpAddress();
                }

                if (mMessageClient.getRemoteSocketAddress() != null) {
                    Logger.d("=== MessageClient onOpen() localIp=" + localIp + ", remoteIp=" + mMessageClient.getRemoteSocketAddress().getHostString());
                } else {
                    Logger.d("=== MessageClient onOpen() localIp=" + localIp);
                }

                if (localIp != null) {
                    JSONObject message = new JSONObject();
                    message.put("type", "WebrtcServerInfo");
                    message.put("ip", localIp);
                    message.put("port", BaseConstants.WEBRTC_SIGNAL_SERVER_PORT);
                    mMessageClient.send(message.toString());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onMessage(final String message) {
            Logger.d("=== MessageClient onMessage(): message=" + message);
            try {
                JSONObject jsonMessage = new JSONObject(message);
                String type = jsonMessage.getString("type");
                if (type.equals("WebrtcServerInfo")) {

                    if (mMessageClient.getRemoteSocketAddress() != null) {
                        CallService.selectedRemoteIp = mMessageClient.getRemoteSocketAddress().getHostString();
                        CallService.selectedRemotePort = mMessageClient.uri.getPort();
                        if (selectedDeviceInfo != null) {
                            CallService.backupRemoteIp = selectedDeviceInfo.ip;
                        } else {
                            CallService.backupRemoteIp = "";
                        }
                    }

                    mMessageClient.close();
                    onRemoteMessageDone();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            Logger.d("=== MessageClient onClose(): reason=" + reason + ", remote=" + remote);
        }

        @Override
        public void onError(Exception ex) {
            ex.printStackTrace();
            Logger.d("=== MessageClient onError() ex=" + ex.getMessage());
            dataConnectionState.updateState(ConnectionState.ConnectFailed, "连接失败！");
            viewModel.mldConnectionState.postValue(dataConnectionState);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            startCallService();
        }
    }
}