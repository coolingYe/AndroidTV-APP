package com.zee.setting.fragment;

import static com.zee.setting.base.BaseConstants.ZEE_SETTINGS_CAMERA_STATE;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.zee.setting.R;
import com.zee.setting.activity.GuideCameraActivity;
import com.zee.setting.data.SettingRepository;
import com.zee.setting.data.SettingViewModel;
import com.zee.setting.data.SettingViewModelFactory;
import com.zee.setting.service.ConnectService;

public class FragmentGuide2 extends Fragment {

    private static final String ARG_POSITION = "position";
    private View rootView;
    private ConstraintLayout clGuide;
    private ImageView ivSuccess;
    private TextView tvDescTitle;
    private ConnectChangeReceiver connectChangeReceiver;
    private SettingViewModel settingViewModel;
    private ConstraintLayout clDeviceInfo;

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Runnable mRunnable;
    private ImageView ivGuideDesc;
    private int currentIndex;

    public static Fragment newInstance(int position) {
        FragmentGuide2 fragment = new FragmentGuide2();
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_guide_2, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() == null) return;
        registerBroadCast();
        updateConnectState();
        SettingViewModelFactory factory = new SettingViewModelFactory(SettingRepository.getInstance());
        settingViewModel = new ViewModelProvider(requireActivity(), factory).get(SettingViewModel.class);
        currentIndex = getArguments().getInt(ARG_POSITION, 0);
        clGuide = view.findViewById(R.id.cl_guide);
        clDeviceInfo = view.findViewById(R.id.cl_device_info);
        tvDescTitle = view.findViewById(R.id.tv_guide_desc);
        ivGuideDesc = view.findViewById(R.id.iv_guide_desc);
        ivSuccess = view.findViewById(R.id.iv_guide_success);
        ivSuccess.setVisibility(View.GONE);

        if (currentIndex == 2) {
            clGuide.setBackgroundResource(R.mipmap.img_guide_2);
            ivGuideDesc.setImageResource(R.mipmap.img_guide_desc_2);
            SpannableString spannableString = new SpannableString(getString(R.string.guide_desc_3));
            StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
            ForegroundColorSpan redSpan = new ForegroundColorSpan(0XFF5A39FF);
            spannableString.setSpan(boldSpan, 16, 23, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            spannableString.setSpan(redSpan, 16, 23, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            tvDescTitle.setText(spannableString);

            TextView tvNetWorkName = view.findViewById(R.id.tv_guide_network_value);
            TextView tvDeviceSN = view.findViewById(R.id.tv_guide_device_sn_value);
            TextView tvIPAddress = view.findViewById(R.id.tv_guide_ip_address_value);

            settingViewModel.guideInfo.observe(requireActivity(), guideInfo -> {
                tvNetWorkName.setText(guideInfo.getDeviceInfo().getNetworkName());
                tvDeviceSN.setText(guideInfo.getDeviceInfo().getDeviceSN());
                tvIPAddress.setText(guideInfo.getDeviceInfo().getIpAddress());
            });
        }

        if (currentIndex == 3) {
            clGuide.setBackgroundResource(R.mipmap.img_guide_3);
            ivGuideDesc.setImageResource(R.mipmap.img_guide_desc_3);
            tvDescTitle.setText(getString(R.string.guide_desc_4));

            TextView tvNetWorkName = view.findViewById(R.id.tv_guide_network_value);
            TextView tvDeviceSN = view.findViewById(R.id.tv_guide_device_sn_value);
            TextView tvIPAddress = view.findViewById(R.id.tv_guide_ip_address_value);

            settingViewModel.guideInfo.observe(requireActivity(), guideInfo -> {
                tvNetWorkName.setText(guideInfo.getDeviceInfo().getNetworkName());
                tvDeviceSN.setText(guideInfo.getDeviceInfo().getDeviceSN());
                tvIPAddress.setText(guideInfo.getDeviceInfo().getIpAddress());
            });
        }
    }

    private void updateConnectState() {
        mRunnable = new Runnable() {
            @Override
            public void run() {
                if (ConnectService.getInstance() != null) {
                    ConnectService.getInstance().checkConnect();
                }
                mHandler.postDelayed(this, 1500);
            }
        };
        mHandler.postDelayed(mRunnable, 1500);
    }

    private boolean hasPermissions = true;
    class ConnectChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (currentIndex == 2) {
                boolean message = intent.getBooleanExtra("message", false);
                if (message) {
                    ivSuccess.setVisibility(View.VISIBLE);
                    ivSuccess.setImageResource(R.mipmap.img_guide_success);
                    clGuide.setBackgroundResource(R.mipmap.img_guide_3);
                } else {
                    ivSuccess.setVisibility(View.GONE);
                    clGuide.setBackgroundResource(R.mipmap.img_guide_2);
                }
            } else if (currentIndex == 3) {
                String type = intent.getStringExtra("type");
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                ivSuccess.setLayoutParams(params);

                boolean message = intent.getBooleanExtra("message", false);
                if (message) {
                    hasPermissions = true;
                    ivSuccess.setVisibility(View.VISIBLE);
                    ivSuccess.setImageResource(R.mipmap.img_guide_3_success);
                    ((GuideCameraActivity) requireActivity()).setConnected(true);
                } else if (type != null) {
                    if (type.equals("Mobile")) {
                        int action = intent.getIntExtra("action", 0);
                        if (action == 10001) {
                            hasPermissions = false;
                            ivSuccess.setVisibility(View.VISIBLE);
                            ivSuccess.setImageResource(R.mipmap.img_guide_3_permissions);
                        } else if (action == 10003) {
                            hasPermissions = true;
                            ivSuccess.setVisibility(View.GONE);
                        }
                    }
                } else if (hasPermissions) {
                    ivSuccess.setVisibility(View.GONE);
                } else {
                    ((GuideCameraActivity) requireActivity()).setConnected(false);
                }

            }
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void registerBroadCast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ZEE_SETTINGS_CAMERA_STATE);
        connectChangeReceiver = new ConnectChangeReceiver();
        requireActivity().registerReceiver(connectChangeReceiver, intentFilter);
    }

    private void unRegisterBroadCast() {
        requireActivity().unregisterReceiver(connectChangeReceiver);
    }

    @Override
    public void onDestroy() {
        if (connectChangeReceiver != null) {
            unRegisterBroadCast();
        }
        mHandler.removeCallbacks(mRunnable);
        super.onDestroy();
    }
}
