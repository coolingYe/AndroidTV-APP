package com.zee.device.helper.ui.settings;


import android.os.Bundle;
import android.view.View;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.zee.device.base.ui.BaseActivity;
import com.zee.device.base.utils.ApkUtil;
import com.zee.device.base.utils.DensityUtils;
import com.zee.device.helper.R;
import com.zee.device.helper.databinding.ActivitySettingsBinding;
import com.zee.device.home.ui.home.model.LoadState;
import com.zee.device.home.ui.home.upgrade.UpgradeDialogActivity;

public class SettingsActivity extends BaseActivity {

    private ActivitySettingsBinding binding;
    private SettingsViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DensityUtils.autoWidth(getApplication(), this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings);

        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);

        String version = ApkUtil.getAppVersionName(this);
        binding.tvSettingsAppVersion.setText("V" + version);

        binding.llSettingsCheckUpdate.setOnClickListener(v -> {
            if (LoadState.Loading != viewModel.mldHostAppUpgradeState.getValue()) {
                viewModel.reqHostAppUpgrade(ApkUtil.getAppVersionName(v.getContext()));
            }
        });

        binding.llSettingsUserPrivate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast("暂不支持！");
            }
        });

        binding.llSettingsUserManual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast("暂不支持！");
            }
        });

        initObserver();
    }

    private void initObserver() {
        viewModel.mldHostAppUpgradeState.observe(this, loadState -> {
            if (LoadState.Success == loadState) {
                if (viewModel.hostAppUpgradeResp != null) {
                    UpgradeDialogActivity.showUpgradeTipDialog(this, viewModel.hostAppUpgradeResp);
                }else{
                    showToast("已是最新版本了！");
                }
            }
        });

        viewModel.mldToastMsg.observe(this, msg -> showToast(msg));
    }

    private void checkAppUpdate() {
        viewModel.reqHostAppUpgrade(ApkUtil.getAppVersionName(this));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}