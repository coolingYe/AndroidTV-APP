package com.zeewain.ai;
import com.zeewain.ai.IClientCallBack;
interface IMyAidlInterface {
        void sendMessage(in String data);
        void register(IClientCallBack callback);
        void unRegister(IClientCallBack callback);
        boolean getActiveStatus();
}