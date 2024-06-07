// IMyAidlInterface.aidl
package com.zeewain.ai;

// Declare any non-default types here with import statements
import com.zeewain.ai.IClientCallBack;
interface IMyAidlInterface {
       void sendMessage(in String data);//start:启动小窗口;hide:隐藏小窗口;show:显示小窗口;close:关闭小窗口;startAndActive:启动并激活
       void register(IClientCallBack callback);
       void unRegister(IClientCallBack callback);
       boolean getActiveStatus();//获取是否激活状态
}
