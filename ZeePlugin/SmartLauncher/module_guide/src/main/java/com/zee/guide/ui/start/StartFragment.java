package com.zee.guide.ui.start;

import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.google.android.material.card.MaterialCardView;
import com.zee.guide.R;
import com.zee.guide.ui.GuideViewModel;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.config.SharePrefer;
import com.zeewain.base.utils.ApkUtil;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.DensityUtils;
import com.zeewain.base.utils.DisplayUtil;
import com.zeewain.base.utils.NetworkUtil;
import com.zeewain.base.utils.SPUtils;
import com.zeewain.base.views.BaseDialog;


public class StartFragment extends Fragment {
    private GuideViewModel viewModel;

    public StartFragment() {}


    public static StartFragment newInstance() {
        return new StartFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        DensityUtils.autoWidth(requireActivity().getApplication(), requireActivity());
        View view = inflater.inflate(R.layout.fragment_start, container, false);
        MaterialCardView cardGuideStartEnter = view.findViewById(R.id.card_guide_start_enter);
        cardGuideStartEnter.setOnClickListener(v -> {
            CommonUtils.savePluginCareInfo();
            if(CommonUtils.createPrivateDir()){
                if(NetworkUtil.isNetworkAvailable(v.getContext()) && !NetworkUtil.isWifiConnected(v.getContext())){
                    if(viewModel.isUserDiscardUpgrade){
                        viewModel.reqDeviceInfo(CommonUtils.getDeviceSn());
                    }else{
                        viewModel.reqManagerAppUpgrade(ApkUtil.getAppVersionName(v.getContext(), BaseConstants.MANAGER_PACKAGE_NAME));
                    }
                }else {
                    NavOptions navOptions = new NavOptions.Builder()
                            .setLaunchSingleTop(true)
                            .build();
                    Navigation.findNavController(v).navigate(R.id.nav_guide_wifi, null, navOptions);
                }
            }else{
                viewModel.mldToastMsg.setValue("创建目录失败！");
            }
        });
        cardGuideStartEnter.setOnFocusChangeListener((v, hasFocus) -> {
            final int strokeWidth = DisplayUtil.dip2px(v.getContext(), 1);
            if (hasFocus) {
                cardGuideStartEnter.setStrokeColor(getResources().getColor(R.color.selectedStrokeColorBlue));
                cardGuideStartEnter.setStrokeWidth(strokeWidth);
                CommonUtils.scaleView(v, 1.1f);
            } else {
                cardGuideStartEnter.setStrokeColor(getResources().getColor(R.color.unselectedStrokeColor));
                cardGuideStartEnter.setStrokeWidth(0);
                v.clearAnimation();
                CommonUtils.scaleView(v, 1f);
            }
        });

        ImageView imageView = view.findViewById(R.id.img_guide_start_logo);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //showInputDialog(v.getContext());
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(GuideViewModel.class);
    }

    private void showInputDialog(Context context){
        final BaseDialog normalDialog = new BaseDialog(context);
        final View view = LayoutInflater.from(context).inflate(R.layout.dialog_wifi_password, null, false);

        TextView txtWifiPasswordTitle = view.findViewById(R.id.txt_wifi_password_title);
        txtWifiPasswordTitle.setText("输入设备序列号");

        CheckBox checkboxWifiPassword = view.findViewById(R.id.checkbox_wifi_password);
        checkboxWifiPassword.setVisibility(View.GONE);

        final EditText editWifiPassword = view.findViewById(R.id.edit_wifi_password);
        editWifiPassword.setInputType(InputType.TYPE_CLASS_TEXT);

        TextView txtWifiPasswordMsg = view.findViewById(R.id.txt_wifi_password_msg);
        txtWifiPasswordMsg.setText("设备序列号");

        TextView txtWifiPasswordCancel = view.findViewById(R.id.txt_wifi_password_cancel);
        txtWifiPasswordCancel.setOnClickListener(v -> normalDialog.dismiss());

        TextView txtWifiPasswordSure = view.findViewById(R.id.txt_wifi_password_sure);
        txtWifiPasswordSure.setText("确定");
        txtWifiPasswordSure.setOnClickListener(v -> {
            String deviceSn = editWifiPassword.getText().toString();
            SPUtils.getInstance().put(SharePrefer.DeviceSN, deviceSn);
            if(deviceSn.equals(CommonUtils.getDeviceSn())){
                normalDialog.dismiss();
            }
        });
        normalDialog.setContentView(view);
        normalDialog.show();
    }
}