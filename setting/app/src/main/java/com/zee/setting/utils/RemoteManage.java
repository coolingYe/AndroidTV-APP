package com.zee.setting.utils;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.zee.setting.activity.GestureExplainActivity;
import com.zee.setting.cache.SPUtils;
import com.zee.setting.cache.SharePrefer;
import com.zee.setting.views.CustomPopupWindow;
import com.zeewain.ai.IClientCallBack;
import com.zeewain.ai.IMyAidlInterface;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RemoteManage {
    public static final String TAG = "RemoteManage";
    private int num = 0;
    private IMyAidlInterface mIService;
    private Handler handler = new Handler();
    private final Context context;
    private static RemoteManage instance;
    private boolean isActivated = false;
    public static boolean isOpen = false;
    private final IClientCallBack iClientCallBack = new IClientCallBack.Stub() {
        @Override
        public void updateLeft(String left) throws RemoteException {
            dealCallBack(left);
        }

        @Override
        public void updateRight(String right) throws RemoteException {
            dealCallBack(right);
        }
    };

    private void dealCallBack(String data) {
        if (data.equals("active")) {
            showActiveUI();
        } else if (data.equals("inactive")) {
            isActivated = false;
            SPUtils.getInstance().put(SharePrefer.Active, false);
            postData("inactive");
        } else {
            postData(data);
        }
    }


    private void showActiveUI() {
        if (isActivated) {
            return;
        }
        postData("active");
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Activity currentActivity = ApkUtil.getCurrentActivity();
                isActivated = true;
                SPUtils.getInstance().put(SharePrefer.Active, true);
                if (currentActivity instanceof GestureExplainActivity) {
                    sendMessage("close_key_control");
                } else {
                    sendMessage("open_key_control");
                }

            }
        }, 2000);

    }

    private void postData(String data) {
        Intent intent = new Intent();
        intent.putExtra("gesture", data);
        intent.setAction("gesture_action");
        context.sendBroadcast(intent);
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i("ssshhh", "service connected");
            mIService = IMyAidlInterface.Stub.asInterface(service);
            try {
                mIService.register(iClientCallBack);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            try {
                mIService.unRegister(iClientCallBack);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }
    };

    public RemoteManage(Context context) {
        this.context = context;
    }

    public static RemoteManage initialize(Context context) {
        if (instance == null) {
            synchronized (RemoteManage.class) {
                if (instance == null) {
                    instance = new RemoteManage(context.getApplicationContext());
                    instance.init();
                }
            }
        }
        return instance;
    }

    public static RemoteManage getInstance() {
        return instance;
    }

    private void init() {
        initAidl();
    }

    private void initAidl() {
        Intent intent = new Intent();
        intent.setAction("com.zeewain.aidlserver");
        intent.setPackage("com.zeewain.ai");
        boolean bindService = context.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        Log.i(TAG, "bindService=" + bindService);
    }

    private void onDestroy() {
        context.unbindService(mServiceConnection);
        handler.removeCallbacksAndMessages(null);
    }


    public void sendMessage(String data) {
        Log.i(TAG, "sendMessage=" + data);
        try {
            if (mIService!=null){
                dealData(data);
                mIService.sendMessage(data);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void dealData(String data) {
        if (data.equals("start")) {
            isOpen = true;
        } else if (data.equals("close")) {
            isOpen = false;
            //关闭之后如果原来手势是激活的那么也重置为false状态
            isActivated = false;
            SPUtils.getInstance().put(SharePrefer.Active, false);
            postData("inactive");
            sendMessage("close_key_control");
        }
    }


    public boolean getIsActivated() {
        return isActivated;

    }


    public boolean getActiveStatus() {
        if (mIService != null) {
            try {
                return mIService.getActiveStatus();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

}
