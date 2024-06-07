package com.zee.launcher.home.gesture.camera2;

import android.graphics.Bitmap;
import android.hardware.camera2.CameraDevice;
import android.util.Size;

public interface Camera2Listener {
    /**
     * 当打开时执行
     *
     * @param cameraDevice       相机实例
     * @param cameraId           相机ID
     * @param displayOrientation 相机预览旋转角度
     * @param isMirror           是否镜像显示
     */
    void onCameraOpened(CameraDevice cameraDevice, String cameraId, Size previewSize, int displayOrientation, boolean isMirror);
    /**
     * 当相机关闭时执行
     */
    void onCameraClosed();

    /**
     * 当相机关闭时执行
     */
    void onCameraDisconnect();

    /**
     * 当出现异常时执行
     *
     * @param e 相机相关异常
     */
    void onCameraError(Exception e);

    /**
     * 调用后端
     */
    void method(Bitmap bitmap);

    /**
     * 设置按钮名字
     */
    void setRotAngle(int result);

    /**
     * texture Available
     */
    void textureAvailable();

    void textureSizeChanged(int width, int height);
}