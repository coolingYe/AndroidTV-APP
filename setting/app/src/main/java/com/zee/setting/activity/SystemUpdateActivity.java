package com.zee.setting.activity;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.zee.setting.R;
import com.zee.setting.base.BaseActivity;
import com.zee.setting.utils.DensityUtils;
import com.zee.setting.views.BaseDialog;


public class SystemUpdateActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_update);
        DensityUtils.autoWidth(getApplication(), this);
        showUpdateDialog();

    }


    private void showUpdateDialog(){
        BaseDialog normalDialog = new BaseDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_system_update, null);
        normalDialog.setContentView(view);
        normalDialog.show();
    }
}
