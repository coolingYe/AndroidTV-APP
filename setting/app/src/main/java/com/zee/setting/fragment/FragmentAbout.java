package com.zee.setting.fragment;


import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.zee.setting.R;
import com.zee.setting.activity.AgreementActivity;
import com.zee.setting.base.BaseConstants;
import com.zee.setting.utils.DeviceUtils;
import com.zee.setting.utils.EthernetUtils;
import com.zee.setting.utils.IPUtils;
import com.zee.setting.utils.NetworkUtil;
import com.zee.setting.views.MarqueeText;

import net.sunniwell.aar.focuscontrol.layout.FocusControlConstraintLayout;


public class FragmentAbout extends Fragment {

    private TextView deviceNameTv;
    private TextView deviceModelNameTV;
    private MarqueeText identifyCodeNameTv;
    private TextView deviceNumberTv;
    private TextView deviceMacTv;
    private TextView ipAddressTv;
    private TextView wirelessMacTv;
    private TextView androidVersionTv;
    private FocusControlConstraintLayout identifyCode;
    private FocusControlConstraintLayout privacyLayout;
    public static final String TAG = "FragmentAbout";
    private FocusControlConstraintLayout userLayout;
    private FocusControlConstraintLayout deviceNameSet;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        deviceNameTv = view.findViewById(R.id.device_name);
        deviceModelNameTV = view.findViewById(R.id.device_model_name);
        identifyCodeNameTv = view.findViewById(R.id.identify_code_name);
        deviceNumberTv = view.findViewById(R.id.device_number_tv);
        deviceMacTv = view.findViewById(R.id.device_mac_tv);
        ipAddressTv = view.findViewById(R.id.ip_address_tv);
        wirelessMacTv = view.findViewById(R.id.wireless_mac_tv);
        androidVersionTv = view.findViewById(R.id.android_version_tv);
        identifyCode = view.findViewById(R.id.identify_code);
        privacyLayout = view.findViewById(R.id.privacy_layout);
        userLayout = view.findViewById(R.id.user_layout);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getData();
        initListener();
    }

    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {// 不在最前端界面显示
             Log.i(TAG,"不在最前端界面显示");
        } else {// 重新显示到最前端中
            Log.i(TAG,"重新显示到最前端中");
            getData();
        }
    }


    private void getData() {
        String deviceName = DeviceUtils.getDeviceName(getActivity());
        String model = DeviceUtils.getModelName();
        String androidId = DeviceUtils.getModelIdentificationCode(getActivity());
        String serialNum = DeviceUtils.getSerialNum();
        String macAddress = DeviceUtils.getMacAddress();
        String ipAddress = IPUtils.getIpAddress(getActivity());
        String androidVersion = DeviceUtils.getAndroidVersion();


        Log.i(TAG, "deviceName11=" + deviceName);
        Log.i(TAG, "model=" + model);
        Log.i(TAG, "androidId=" + androidId);
        Log.i(TAG, "serialNum=" + serialNum);
        Log.i(TAG, "macAddress=" + macAddress);
        Log.i(TAG, "ipAddress=" + ipAddress);
        Log.i(TAG, "androidVersion=" + androidVersion);


        deviceNameTv.setText(deviceName);
        deviceModelNameTV.setText(model);
        //  identifyCodeNameTv.requestFocus();
        identifyCodeNameTv.setText(androidId);
        deviceNumberTv.setText(serialNum);

        androidVersionTv.setText("Android " + androidVersion);
        int netWorkType = NetworkUtil.getNetWorkType(getActivity());
        if (netWorkType == NetworkUtil.NETWORK_ETHERNET) {
            Log.i(TAG, "有线网络");
            macAddress = EthernetUtils.getLocalMacAddress();
            deviceMacTv.setText(macAddress);
            wirelessMacTv.setText("");
            ipAddress = EthernetUtils.getIpAddressForInterfaces();
            ipAddressTv.setText(ipAddress);
        } else if (netWorkType == NetworkUtil.NETWORK_WIFI) {
            Log.i(TAG, "无线网络");
            deviceMacTv.setText("");
            ipAddressTv.setText(ipAddress);
            wirelessMacTv.setText(macAddress);
        } else {
            Log.i(TAG, "没有网络");
            deviceMacTv.setText("");
            wirelessMacTv.setText("");
            ipAddressTv.setText("");
        }


    }

    private void initListener() {

        identifyCode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    identifyCodeNameTv.setTextColor(getResources().getColor(R.color.white));
                } else {
                    identifyCodeNameTv.setTextColor(getResources().getColor(R.color.src_c99));
                }
            }
        });

        privacyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              //ToastUtils.showToast(getActivity(), "敬请期待");
                Intent intent=new Intent(getActivity(), AgreementActivity.class);
                intent.putExtra("agreementCode", BaseConstants.ZEE_PRIVACY_AGREEMENT);
                startActivity(intent);

            }
        });

        userLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(), AgreementActivity.class);
                intent.putExtra("agreementCode", BaseConstants.ZEE_USER_AGREEMENT);
                startActivity(intent);
            }
        });
    }
}
