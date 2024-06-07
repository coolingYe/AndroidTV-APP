package com.zee.setting.fragment;


import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.zee.setting.R;
import com.zee.setting.activity.GestureExplainActivity;
import com.zee.setting.base.BaseConstants;
import com.zee.setting.cache.SPUtils;
import com.zee.setting.cache.SharePrefer;
import com.zee.setting.utils.ApkUtil;
import com.zee.setting.utils.RemoteManage;
import com.zee.setting.utils.ToastUtils;
import com.zee.setting.views.BaseDialog;

import net.sunniwell.aar.focuscontrol.layout.FocusControlConstraintLayout;


public class FragmentAI extends Fragment {
    public static final String TAG = "FragmentAI";
    private View rootView;
    private FocusControlConstraintLayout gestureSet;
    private FocusControlConstraintLayout voiceSet;
    private FocusControlConstraintLayout aiHelperSet;
    private boolean isActive = false;
    private ImageView back;
    private FocusControlConstraintLayout gestureActive;
    private boolean installed;
    private String[] cameraIdList;
    private boolean isHaveCamera;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_ai, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
        initListener();
    }

    private void initView() {
        back = rootView.findViewById(R.id.img_back);
        gestureSet = rootView.findViewById(R.id.gesture_set);
        voiceSet = rootView.findViewById(R.id.voice_set);
        aiHelperSet = rootView.findViewById(R.id.ai_helper_set);
    }

    private void initListener() {
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawerLayout drawerLayout = getActivity().findViewById(R.id.drawer_layout);
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });
        gestureSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //showGestureDialog();
                SPUtils.getInstance().put(SharePrefer.Gesture, "close");
                RemoteManage.getInstance().sendMessage("close");
                ToastUtils.showToast(getActivity(), "敬请期待");
            }
        });
        voiceSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtils.showToast(getActivity(), "敬请期待");
            }
        });
        aiHelperSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtils.showToast(getActivity(), "敬请期待");
            }
        });
    }

    public void showGestureDialog() {
        getCameraList();
        BaseDialog normalDialog = new BaseDialog(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_gesture_control, null);
        normalDialog.setContentView(view);
        normalDialog.show();
        TextView title = view.findViewById(R.id.title);
        ImageView back = view.findViewById(R.id.img_back);
        Switch gestureActiveSwitch = view.findViewById(R.id.gesture_active_switch);
        gestureActive = view.findViewById(R.id.gesture_active);
        ConstraintLayout gestureDes = view.findViewById(R.id.gesture_des);
        gestureActive.requestFocus();
        //默认关闭
//        isActive=false;
//        gestureActiveSwitch.setChecked(false);
//        SPUtils.getInstance().put(SharePrefer.Gesture,"close");
        //读取开关数据
        readCache(gestureActiveSwitch);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                normalDialog.dismiss();


            }
        });
        gestureActive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dealOpenAI(gestureActiveSwitch);

            }
        });
        gestureActiveSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dealOpenAI(gestureActiveSwitch);
            }
        });


        gestureActive.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    if (gestureActiveSwitch.isChecked()){
                        gestureActiveSwitch.setTrackDrawable(getResources().getDrawable(R.mipmap.img_selected_open));
                    }else {
                        gestureActiveSwitch.setTrackDrawable(getResources().getDrawable(R.mipmap.img_selected_close));
                    }
                }else {
                    if (gestureActiveSwitch.isChecked()){
                        gestureActiveSwitch.setTrackDrawable(getResources().getDrawable(R.mipmap.img_open));
                    }else {
                        gestureActiveSwitch.setTrackDrawable(getResources().getDrawable(R.mipmap.img_close));
                    }
                }

            }
        });
        gestureActiveSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    if (gestureActive.isFocused()){
                        gestureActiveSwitch.setTrackDrawable(getResources().getDrawable(R.mipmap.img_selected_open));
                    }else {
                        gestureActiveSwitch.setTrackDrawable(getResources().getDrawable(R.mipmap.img_open));
                    }
                }else {
                    if (gestureActive.isFocused()){
                        gestureActiveSwitch.setTrackDrawable(getResources().getDrawable(R.mipmap.img_selected_close));
                    }else {
                        gestureActiveSwitch.setTrackDrawable(getResources().getDrawable(R.mipmap.img_close));
                    }
                }
            }
        });
      /*  gestureDes.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    sendKeyCode(KeyEvent.KEYCODE_ENTER);
                }
            }
        });*/
        gestureDes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  sendKeyCode(KeyEvent.KEYCODE_BACK);
                Intent intent = new Intent(getActivity(), GestureExplainActivity.class);
                startActivity(intent);
            }
        });

    }

    private void dealOpenAI(Switch gestureActiveSwitch) {
        if (!isHaveCamera){
            ToastUtils.showToast(getActivity(), "请先接入摄像头");
            gestureActiveSwitch.setChecked(false);
            return;
        }
        if (!installed) {
            ToastUtils.showToast(getActivity(), "请先安装手势识别app");
            gestureActiveSwitch.setChecked(false);
            return;
        }
        if (!isActive) {
            isActive = true;
            gestureActiveSwitch.setChecked(true);
            RemoteManage.getInstance().sendMessage("start");
            SPUtils.getInstance().put(SharePrefer.Gesture, "open");
            Log.i(TAG, "open");

        } else {
            isActive = false;
            gestureActiveSwitch.setChecked(false);
            SPUtils.getInstance().put(SharePrefer.Gesture, "close");
            RemoteManage.getInstance().sendMessage("close");
            Log.i(TAG, "close");
        }
    }

    private void readCache(Switch gestureActiveSwitch) {
        installed = ApkUtil.isAppInstalled(getActivity(), BaseConstants.ZEE_GESTURE_PACKAGE_NAME);
        if (installed && (isHaveCamera)) {
            String gestureSwitch = SPUtils.getInstance().getString(SharePrefer.Gesture);
            if (gestureSwitch.equals("open")) {
                isActive = true;
                gestureActiveSwitch.setChecked(true);
                if (gestureActive.isFocused()){
                    gestureActiveSwitch.setTrackDrawable(getResources().getDrawable(R.mipmap.img_selected_open));
                }else {
                    gestureActiveSwitch.setTrackDrawable(getResources().getDrawable(R.mipmap.img_open));
                }

                RemoteManage.getInstance().sendMessage("start");
            } else  {
                isActive = false;
                gestureActiveSwitch.setChecked(false);
                if (gestureActive.isFocused()){
                    gestureActiveSwitch.setTrackDrawable(getResources().getDrawable(R.mipmap.img_selected_close));
                }else {
                    gestureActiveSwitch.setTrackDrawable(getResources().getDrawable(R.mipmap.img_close));
                }

            }
        } else {
            isActive = false;
            gestureActiveSwitch.setChecked(false);
            if (gestureActive.isFocused()){
                gestureActiveSwitch.setTrackDrawable(getResources().getDrawable(R.mipmap.img_selected_close));
            }else {
                gestureActiveSwitch.setTrackDrawable(getResources().getDrawable(R.mipmap.img_close));
            }
        }
    }

    private void getCameraList() {
        try {
            CameraManager mCameraManager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
            cameraIdList = mCameraManager.getCameraIdList();
            if (cameraIdList!=null){
                if (cameraIdList.length>0){
                    isHaveCamera = true;
                }else {
                    isHaveCamera = false;
                }
            }else {
                isHaveCamera = false;
            }


        } catch (Exception ex) {

        }
    }

}
