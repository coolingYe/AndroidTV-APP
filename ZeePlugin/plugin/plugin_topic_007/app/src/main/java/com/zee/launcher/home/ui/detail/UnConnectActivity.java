package com.zee.launcher.home.ui.detail;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.material.card.MaterialCardView;
import com.zee.launcher.home.R;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.ui.BaseActivity;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.DensityUtils;
import com.zeewain.base.utils.DisplayUtil;
import com.zeewain.base.utils.SystemProperties;

public class UnConnectActivity extends BaseActivity implements View.OnFocusChangeListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_unconnect);
        DensityUtils.autoWidth(getApplication(), this);

        MaterialCardView btnCancel = findViewById(R.id.card_cancel);
        btnCancel.setOnFocusChangeListener(this);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        LinearLayout llModel1 = findViewById(R.id.ll_model1);
        LinearLayout llModel2 = findViewById(R.id.ll_model2);
        LinearLayout llModel3 = findViewById(R.id.ll_model3);

        llModel1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(1,null);
                finish();
            }
        });
        llModel1.requestFocus();

        llModel2.setOnClickListener(v -> {
            Toast.makeText(this, "请将摄像头插入终端USB接口", Toast.LENGTH_LONG).show();
            finish();
        });

        llModel3.setOnClickListener(v -> {
            boolean hasCommonUpdate = SystemProperties.getBoolean(BaseConstants.SP_KEY_CAMERA_UPDATE_STATE, true);
            if (hasCommonUpdate) {
                CommonUtils.startSettingsActivityForCameraDescription(UnConnectActivity.this);
            } else CommonUtils.startSettingsActivityForGuideCamera(UnConnectActivity.this);
            finish();
        });
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        MaterialCardView cardView = (view instanceof MaterialCardView) ? (MaterialCardView) view : null;
        if (cardView == null) return;
        if (hasFocus) {
            cardView.setStrokeColor(0xFF754AFF);
            cardView.setStrokeWidth(DisplayUtil.dip2px(this, 1f));
        } else {
            cardView.setStrokeColor(0x00FFFFFF);
            cardView.setStrokeWidth(0);
        }
    }
}
