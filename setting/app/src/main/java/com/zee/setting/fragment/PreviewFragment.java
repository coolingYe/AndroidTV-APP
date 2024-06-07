package com.zee.setting.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraDevice;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;

import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import com.zee.setting.bean.CameraBean;
import com.zee.setting.camera2.Camera2Helper;
import com.zee.setting.camera2.Camera2Listener;
import com.zee.setting.utils.Logger;
import com.zee.setting.utils.ToastUtils;

public class PreviewFragment extends PreviewBaseFragment implements Camera2Listener {

    private static final String TAG = "PreviewFragment";
    private Camera2Helper camera2Helper;
    public CameraBean cameraBean;

    public PreviewFragment() { }

    public static PreviewFragment newInstance() {
        return new PreviewFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initCamera(view.getContext().getApplicationContext());
    }

    private void initCamera(Context context) {
        WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        int rotation = windowManager.getDefaultDisplay().getRotation();
        camera2Helper = new Camera2Helper.Builder()
                .cameraListener(this)
                .context(context)
                .previewOn(textureView)
                .rotation(rotation)
                .build();
    }

    public synchronized void openCamera(CameraBean cameraBean){
        Logger.i(TAG, "openCamera() new=" + cameraBean + ", old=" + this.cameraBean);
        this.cameraBean = cameraBean;
        if (CameraBean.TYPE_USB_CAMERA == cameraBean.getType()) {
            if (camera2Helper != null) {
                camera2Helper.start(cameraBean.cameraId);
            }
        } else {
            doWebrtcConnect();
        }
    }

    public synchronized void closeCamera(){
        Logger.i(TAG, "closeCamera() " + cameraBean);
        if (cameraBean != null) {
            if (CameraBean.TYPE_USB_CAMERA == cameraBean.getType()) {
                if (camera2Helper != null) {
                    camera2Helper.stop();
                }
            } else {
                doWebrtcDisconnect();
            }
        }
    }

    @Override
    public void onDestroyView() {
        if (camera2Helper != null) {
            camera2Helper.release();
        }
        super.onDestroyView();
    }

    @Override
    public void onRemoteCameraOpened() {
        super.onRemoteCameraOpened();
        textureView.postDelayed(() -> ToastUtils.showToast(textureView.getContext(), cameraBean.cameraName + "已打开"), 10);
    }

    @Override
    public void onCameraOpened(CameraDevice cameraDevice, String cameraId, Size previewSize, int displayOrientation, boolean isMirror) {
        isCameraOpened = true;
        textureView.postDelayed(() -> {
            textureView.setAlpha(1);
            ToastUtils.showToast(textureView.getContext(), cameraBean.cameraName + "已打开");
        }, 200);

    }

    @Override
    public void onCameraClosed() {
        isCameraOpened = false;
        textureView.postDelayed(() -> textureView.setAlpha(0), 200);
    }

    @Override
    public void onCameraError(Exception e) {
        isCameraOpened = false;
        textureView.postDelayed(() -> textureView.setAlpha(0), 200);
    }

    @Override
    public void onCameraInvalid() {
        isCameraOpened = false;
        textureView.postDelayed(() -> textureView.setAlpha(0), 200);
    }

    @Override
    public void method(Bitmap bitmap) {

    }

    @Override
    public void setRotAngle(int result) {

    }

    @Override
    public void textureAvailable() {
        if (cameraBean != null) {
            textureView.post(() -> {
                if (cameraBean != null) {
                    openCamera(cameraBean);
                }
            });
        }
    }

    @Override
    public void textureSizeChanged(int width, int height) {

    }
}