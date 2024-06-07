package com.zee.setting.fragment;

import static com.zee.setting.base.BaseConstants.SP_KEY_CAMERA_QR_URL;
import static com.zee.setting.base.BaseConstants.ZEE_SETTINGS_CAMERA_STATE;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
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
import com.zee.setting.base.LoadState;
import com.zee.setting.cache.SPUtils;
import com.zee.setting.data.SettingRepository;
import com.zee.setting.data.SettingViewModel;
import com.zee.setting.data.SettingViewModelFactory;
import com.zee.setting.utils.CodeCreator;
import com.zee.setting.utils.DisplayUtil;
import com.zee.setting.utils.NetworkUtil;

public class FragmentGuide extends Fragment {

    private static final String ARG_POSITION = "position";

    private ConstraintLayout clGuide;
    private LinearLayout llGuideAppQr;
    private TextView tvDescTitle, tvTipTitle;

    private int currentIndex;
    private SettingViewModel settingViewModel;

    private MobileChangeReceiver mobileChangeReceiver;

    private LinearLayout llGuideMobileTip;

    private ImageView ivGuideMobileTip;
    private TextView tvGuideMobileTip;

    public static Fragment newInstance(int position) {
        FragmentGuide fragment = new FragmentGuide();
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_guide, container, false);
        clGuide = rootView.findViewById(R.id.cl_guide_1);
        llGuideAppQr = rootView.findViewById(R.id.ll_guide_app_qr);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() == null) return;
        SettingViewModelFactory factory = new SettingViewModelFactory(SettingRepository.getInstance());
        settingViewModel = new ViewModelProvider(requireActivity(), factory).get(SettingViewModel.class);
        currentIndex = getArguments().getInt(ARG_POSITION, 0);
        tvDescTitle = view.findViewById(R.id.tv_guide_desc);
        tvTipTitle = view.findViewById(R.id.tv_guide_tip);
        if (currentIndex == 0) {
            clGuide.setVisibility(View.GONE);
            llGuideAppQr.setVisibility(View.VISIBLE);
            SpannableString spannableString = new SpannableString(getString(R.string.guide_desc_1));
            StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
            ForegroundColorSpan colorSpan = new ForegroundColorSpan(0XFF5A39FF);
            spannableString.setSpan(colorSpan, 8, getString(R.string.guide_desc_1).length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            spannableString.setSpan(boldSpan, 8, getString(R.string.guide_desc_1).length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            tvDescTitle.setText(spannableString);
            tvTipTitle.setVisibility(View.GONE);
            String url = SPUtils.getInstance().getString(SP_KEY_CAMERA_QR_URL, "");
            if (NetworkUtil.isNetworkAvailable(view.getContext())) {
                settingViewModel.getPublishVersionInfo("ZWN_SW_ANDROID_AIIP_020");
            } else if (url.length() > 0) {
                setupQR(view, url);
            }
        }

        if (currentIndex == 1) {
            llGuideAppQr.setVisibility(View.GONE);
            clGuide.setVisibility(View.VISIBLE);
            llGuideMobileTip = view.findViewById(R.id.ll_guide_mobile_state_tip);
            ivGuideMobileTip = view.findViewById(R.id.iv_guide_mobile_state_tip);
            tvGuideMobileTip = view.findViewById(R.id.tv_guide_mobile_state_tip);
            registerBroadCast();
        }

        settingViewModel.guideInfo.observe(requireActivity(), guideInfo -> {
            if (currentIndex == 1) {
                SpannableString spannableString = new SpannableString(getString(R.string.guide_desc_2));
                StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
                ForegroundColorSpan redSpan = new ForegroundColorSpan(0XFF5A39FF);
                spannableString.setSpan(boldSpan, 14, getString(R.string.guide_desc_2).length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                spannableString.setSpan(redSpan, 14, getString(R.string.guide_desc_2).length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                tvDescTitle.setText(spannableString);
                tvTipTitle.setText(getString(R.string.guide_tip_2));

                int width = DisplayUtil.dip2px(view.getContext(), 147);
                int height = DisplayUtil.dip2px(view.getContext(), 147);
                ImageView ivConnectQR = view.findViewById(R.id.iv_desc_qr);
                ivConnectQR.setImageBitmap(CodeCreator.createQRCode(guideInfo.getConnectQRInfo(), width, height, null));
                TextView tvNetWorkName = view.findViewById(R.id.tv_guide_network_value);
                TextView tvDeviceSN = view.findViewById(R.id.tv_guide_device_sn_value);
                TextView tvIPAddress = view.findViewById(R.id.tv_guide_ip_address_value);
                tvNetWorkName.setText(guideInfo.getDeviceInfo().getNetworkName());
                tvDeviceSN.setText(guideInfo.getDeviceInfo().getDeviceSN());
                tvIPAddress.setText(guideInfo.getDeviceInfo().getIpAddress());
            }
        });

        settingViewModel.mPublishState.observe(requireActivity(), loadState -> {
            if (loadState == LoadState.Success) {
                String url = settingViewModel.publishResp.getPackageUrl();
                SPUtils.getInstance().put(SP_KEY_CAMERA_QR_URL, url);
                setupQR(view, url);
            }
        });

    }

    private void setupQR(View view, String url) {
        int width = DisplayUtil.dip2px(view.getContext(), 300);
        int height = DisplayUtil.dip2px(view.getContext(), 300);
        ImageView imageView = view.findViewById(R.id.iv_app_qr);
        imageView.setImageBitmap(CodeCreator.createQRCode(url, width, height, null));
    }

    class MobileChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (currentIndex != 1) return;
            String type = intent.getStringExtra("type");
            if (type == null) return;
            if (!type.equals("Mobile")) return;
            int action = intent.getIntExtra("action", 0);
            switch (action) {
                case 10001:
                    llGuideMobileTip.setVisibility(View.VISIBLE);
                    ivGuideMobileTip.setImageResource(R.drawable.icon_erroe);
                    tvGuideMobileTip.setTextColor(0xfff10007);
                    tvGuideMobileTip.setText("当前APP授权摄像权限异常，未启用摄像头请重新授权APP摄像头权限");
                    break;
                case 10002:
                    llGuideMobileTip.setVisibility(View.VISIBLE);
                    ivGuideMobileTip.setImageResource(R.mipmap.icon_success);
                    tvGuideMobileTip.setTextColor(0xff07ac4e);
                    tvGuideMobileTip.setText("当前APP绑定设备成功，请点击下一步 ");
                    break;
                default:
                    llGuideMobileTip.setVisibility(View.GONE);
            }
        }

    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void registerBroadCast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ZEE_SETTINGS_CAMERA_STATE);
        mobileChangeReceiver = new MobileChangeReceiver();
        requireActivity().registerReceiver(mobileChangeReceiver, intentFilter);
    }

    private void unRegisterBroadCast() {
        requireActivity().unregisterReceiver(mobileChangeReceiver);
    }

    @Override
    public void onDestroy() {
        if (mobileChangeReceiver != null) {
            unRegisterBroadCast();
        }
        super.onDestroy();
    }
}
