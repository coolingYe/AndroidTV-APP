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

public class RemoteManageBack {
    private int num = 0;
    private IMyAidlInterface mIService;
    private ExecutorService cachedThreadPool;
    private Handler handler = new Handler();
    private final Context context;
    private static RemoteManageBack instance;

    private CustomPopupWindow customPopupWindow;
    private boolean isActivated=false;
    private Timer timer;
    private long lastTime;
    public static  boolean isOpen=false;
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
        if (!data.equals("active")){
            postData(data);
        }
        showActiveUI(data);
        dealActive();
    }

    private void dealActive() {
        if (timer!=null){
            timer.cancel();
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (isActivated && (System.currentTimeMillis()-lastTime)>30000){
                            isActivated=false;
                            SPUtils.getInstance().put(SharePrefer.Active,false);
                            postData("inactivation");
                            sendMessage("close_key_control");
                            ToastUtils.showToast(context,"手势功能已关闭");
                        }
                    }
                });


            }
        },0,30000);
    }


    private void showActiveUI(String data) {
        if (data.equals("active")) {
            if (isActivated){
                ToastUtils.showToast(context,"手势功能已激活");
                return;
            }
            postData("active");
            handler.removeCallbacksAndMessages(null);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Activity currentActivity = ApkUtil.getCurrentActivity();
                    customPopupWindow = new CustomPopupWindow(currentActivity);
                    customPopupWindow.showPopMenu();
                    isActivated = true;
                    SPUtils.getInstance().put(SharePrefer.Active,true);
                    if (currentActivity instanceof GestureExplainActivity){
                        Log.i("ssshhh","当前界面是手势说明界面,这个界面不驱动按键动作");
                    }else {
                        sendMessage("open_key_control");
                    }

                    handler.removeCallbacksAndMessages(null);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            customPopupWindow.dismissPopupWindow();
                        }
                    }, 10000);
                }
            }, 2000);
        }
    }

    private void postData(String data) {
        Intent intent = new Intent();
        intent.putExtra("gesture", data);
        intent.setAction("gesture_action");
        context.sendBroadcast(intent);
        lastTime = System.currentTimeMillis();
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

    public RemoteManageBack(Context context) {
        this.context = context;
    }

    public static RemoteManageBack initialize(Context context) {
        if (instance == null) {
            synchronized (RemoteManageBack.class) {
                if (instance == null) {
                    instance = new RemoteManageBack(context.getApplicationContext());
                    instance.init();
                }
            }
        }
        return instance;
    }

    public static RemoteManageBack getInstance() {
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
        Log.i("ssshhh", "bindService=" + bindService);
    }

    private void onDestroy() {
        context.unbindService(mServiceConnection);
        handler.removeCallbacksAndMessages(null);
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

    public void sendMessage(String data) {
        Log.i("ggghhh","sendMessage="+data);
        try {
            dealData(data);
            mIService.sendMessage(data);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void dealData(String data) {
        if (data.equals("start")){
            isOpen=true;
        }else if (data.equals("close")){
            isOpen=false;
            //关闭之后如果原来手势是激活的那么也重置为false状态
            isActivated=false;
            SPUtils.getInstance().put(SharePrefer.Active,false);
            postData("inactivation");
            sendMessage("close_key_control");
        }
    }


    public boolean getIsActivated(){
        return  isActivated;

  }




}
