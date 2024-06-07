package com.zeewain.ai;
interface IClientCallBack {
    void updateLeft(in String left);//传递左手手势信号给第三方应用驱动相应展示效果
    void updateRight(in String right);//传递右手手势信号给第三方应用驱动相应展示效果
}