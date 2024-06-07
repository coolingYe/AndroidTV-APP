package com.zee.setting.fragment;


import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProviders;

import com.zee.setting.R;
import com.zee.setting.base.BaseConstants;
import com.zee.setting.base.LoadState;
import com.zee.setting.data.SettingRepository;
import com.zee.setting.data.SettingViewModel;
import com.zee.setting.data.SettingViewModelFactory;
import com.zee.setting.data.protocol.request.UpgradeReq;
import com.zee.setting.data.protocol.response.UpgradeResp;
import com.zee.setting.utils.ApkUtil;
import com.zee.setting.utils.DataCleanManagerUtils;
import com.zee.setting.utils.DensityUtils;
import com.zee.setting.utils.SystemUtils;
import com.zee.setting.utils.ToastUtils;
import com.zee.setting.views.BaseDialog;

import net.sunniwell.aar.focuscontrol.layout.FocusControlConstraintLayout;


public class FragmentSystem extends Fragment {

    private FocusControlConstraintLayout cleanSystem;
    private FocusControlConstraintLayout restore;
    private FocusControlConstraintLayout versionLayout;
    private SettingViewModel settingViewModel;
    private TextView description;
    private FocusControlConstraintLayout dataTime;
    private FocusControlConstraintLayout certification;
    public static final String TAG = "FragmentSystem";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        View view = inflater.inflate(R.layout.fragment_system, container, false);
        initView(view);
        DensityUtils.autoWidth(getActivity().getApplication(), getActivity());
        return view;
    }

    private void initView(View view) {
        cleanSystem = view.findViewById(R.id.clean_system);
        restore = view.findViewById(R.id.restore);
        versionLayout = view.findViewById(R.id.version_layout);
        description = view.findViewById(R.id.description);
        dataTime = view.findViewById(R.id.data_time);
        certification = view.findViewById(R.id.certification);
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
        initListener();
        initObserver();
    }

    public void showRecoveryDialog() {
        BaseDialog normalDialog = new BaseDialog(getActivity(), R.style.dialogStyle);
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_alert, null);
        normalDialog.setContentView(view);
        normalDialog.show();
        TextView confirm = view.findViewById(R.id.confirm);
        TextView cancel = view.findViewById(R.id.cancel);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SystemUtils.resetSystem(getActivity());
                normalDialog.dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                normalDialog.dismiss();
            }
        });
    }



    public void showCleanCacheDialog() {
        BaseDialog normalDialog = new BaseDialog(getActivity(), R.style.dialogStyle);
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_alert, null);
        normalDialog.setContentView(view);
        normalDialog.show();

        LinearLayout headRoot = view.findViewById(R.id.head_root);
        headRoot.setVisibility(View.GONE);
        TextView description = view.findViewById(R.id.description);
        description.setText("此操作将会清除设备内所有应用\n" +
                "的使用数据及缓存");


        TextView confirm = view.findViewById(R.id.confirm);
        TextView cancel = view.findViewById(R.id.cancel);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long cacheSize = DataCleanManagerUtils.getTotalCache(getActivity());
                Log.i(TAG, "cacheSize=" + cacheSize);
                if (cacheSize > 0) {
                    DataCleanManagerUtils.clearAllCache(getActivity());
                    //ToastUtils.showShort("清理缓存成功");
                    com.zee.setting.utils.ToastUtils.showToast(getActivity(), "清理完成");
                } else {
                    // ToastUtils.showShort("没有缓存需要清理");
                    com.zee.setting.utils.ToastUtils.showToast(getActivity(), "没有缓存需要清理");

                }
                normalDialog.dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                normalDialog.dismiss();
            }
        });


    }

    public void initData() {
       // description.setText(ApkUtil.getAppVersionName(getActivity()));
//        description.setText(ApkUtil.getAppVersionName(getActivity(),BaseConstants.ZEE_PACKAGE_NAME));
        SettingViewModelFactory factory = new SettingViewModelFactory(SettingRepository.getInstance());
        settingViewModel = ViewModelProviders.of((FragmentActivity) getActivity(), factory).get(SettingViewModel.class);
    }

    @Override
    public void onResume() {
        super.onResume();
        description.setText(ApkUtil.getAppVersionName(getActivity(),BaseConstants.ZEE_PACKAGE_NAME));
    }

    private void initObserver() {
        settingViewModel.mUpgradeState.observe((LifecycleOwner) getActivity(), loadState -> {
            if (LoadState.Success == loadState) {
                UpgradeResp upgradeResp = settingViewModel.upgradeResp;
                if (upgradeResp != null) {
                    Log.i(TAG, "弹出对话框");
                    try {
                       /* Class<?> upgrade = Class.forName("com.zwn.launcher.ui.upgrade.UpgradeDialogActivity");
                        Method method = upgrade.getMethod("showUpgradeTipDialog", Context.class, Object.class);
                        Log.i("ssshhh", "开启反射调用");
                        method.invoke(upgrade.newInstance(), getActivity(), upgradeResp);*/
                        ToastUtils.showToast(getActivity(), "跳转更新界面");


                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    // showToast("已是最新版本！");
                    // ToastUtils.showShort("已是最新版本");
                    com.zee.setting.utils.ToastUtils.showToast(getActivity(), "已是最新版本");
                }
            }
        });
    }

    public void initListener() {
        versionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // checkAppUpdate();
                startSettingsActivity(getActivity());
            }
        });
        dataTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                com.zee.setting.utils.ToastUtils.showToast(getActivity(), "敬请期待");
            }
        });
        certification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  ToastUtils.showShort("敬请期待");
                com.zee.setting.utils.ToastUtils.showToast(getActivity(), "敬请期待");
            }
        });
        restore.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.i(TAG, "恢复出厂设置");
                //SystemUtils.resetSystem(getActivity());
                //  ToastUtils.showToast(getActivity(),"已恢复出厂设置");
                showRecoveryDialog();
                //SystemUtils.rebootSystem(getActivity());

            }
        });

        cleanSystem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "cleanApplicationData");
                showCleanCacheDialog();


            }
        });


    }

    private void checkAppUpdate() {
        String version = ApkUtil.getAppVersionName(getActivity());
        //version="1.0.1";
        if ((version != null)) {
            settingViewModel.getUpgradeVersionInfo(new UpgradeReq(version, BaseConstants.HOST_APP_SOFTWARE_CODE));
            //   settingViewModel.getUpgradeVersionInfo(new UpgradeReq(version, "ZWN_SW_ANDROID_NATIVE_001"));
        }
    }

    public static void startSettingsActivity(Context context) {
        try {
            Intent intent = new Intent();
            intent.setAction(BaseConstants.ZEE_UPDATE_ACTIVITY_ACTION);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
