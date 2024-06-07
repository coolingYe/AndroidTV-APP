package com.zeewain.ai;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class ZeeAiManager {
    private static final String TAG = "ZeeAiManager";
    private static final String ZEE_GESTURE_AI_PACKAGE_NAME = "com.zeewain.ai";
    private static final String ZEE_GESTURE_AI_SERVICE_ACTION = "com.zeewain.aidlserver";
    private static volatile ZeeAiManager instance;
    private static IMyAidlInterface aiService;
    private ZeeAiManager(){}
    private OnServiceStateListener onServiceStateListener;

    public static ZeeAiManager getInstance(){
        if(instance == null){
            synchronized (ZeeAiManager.class){
                if(instance == null){
                    instance = new ZeeAiManager();
                }
            }
        }
        return instance;
    }

    public synchronized void bindAiService(Context context) {
        if(aiService == null) {
            Intent bindIntent = new Intent();
            bindIntent.setAction(ZEE_GESTURE_AI_SERVICE_ACTION);
            bindIntent.setPackage(ZEE_GESTURE_AI_PACKAGE_NAME);
            context.bindService(bindIntent, aiServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    public synchronized void unbindAiService(Context context) {
        Log.i(TAG, "unbindAiService() aiService=" + aiService);
        if(aiService != null) {
            try {
                aiService.unRegister(iClientCallBack);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            context.unbindService(aiServiceConnection);
            aiService = null;
        }
    }

    private final IClientCallBack iClientCallBack = new IClientCallBack.Stub() {
        @Override
        public void updateLeft(String left) throws RemoteException {
        }

        @Override
        public void updateRight(String right) throws RemoteException {
        }
    };

    private final ServiceConnection aiServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "onServiceConnected()");
            aiService = IMyAidlInterface.Stub.asInterface(service);
            if(aiService != null){
                try {
                    aiService.register(iClientCallBack);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "onServiceDisconnected()");
            aiService = null;
            if(onServiceStateListener != null){
                onServiceStateListener.onServiceDisconnected();
            }
        }
    };

    public void sendMsg(String action){
        Log.i(TAG, "sendMsg() action=" + action + ", aiService=" + aiService);
        if(aiService != null){
            try {
                aiService.sendMessage(action);
            } catch (RemoteException e) {
                e.printStackTrace();
                Log.e(TAG, "sendMsg() err " + e);
            }
        }
    }

    public void startGestureAI(boolean withActive){
        if(withActive) {
            sendMsg("startAndActive");
        }else{
            sendMsg("start");
        }
    }

    public void stopGestureAI(){
        sendMsg("close");
    }

    public boolean isGestureAIActive(){
        if(aiService != null){
            try {
                boolean isActive = aiService.getActiveStatus();
                Log.i(TAG, "getActiveStatus() isActive=" + isActive);
                return isActive;
            } catch (RemoteException e) {
                e.printStackTrace();
                Log.e(TAG, "getActiveStatus() err " + e);
            }
        }
        return false;
    }

    public void setOnServiceStateListener(OnServiceStateListener onServiceStateListener) {
        this.onServiceStateListener = onServiceStateListener;
    }

    public interface OnServiceStateListener{
        void onServiceConnected();
        void onServiceDisconnected();
    }
}
