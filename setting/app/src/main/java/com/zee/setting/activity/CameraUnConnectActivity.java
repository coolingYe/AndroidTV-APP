package com.zee.setting.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.android.material.card.MaterialCardView;
import com.zee.setting.R;
import com.zee.setting.base.BaseActivity;
import com.zee.setting.base.BaseConstants;
import com.zee.setting.utils.DensityUtils;
import com.zee.setting.utils.DisplayUtil;
import com.zee.setting.utils.SystemProperties;

public class CameraUnConnectActivity extends BaseActivity implements View.OnFocusChangeListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_unconnect);
        DensityUtils.autoWidth(getApplication(), this);

        MaterialCardView btnNext = findViewById(R.id.card_next);
        MaterialCardView btnCancel = findViewById(R.id.card_cancel);
        btnCancel.setOnFocusChangeListener(this);
        btnNext.setOnFocusChangeListener(this);
        btnNext.setOnClickListener(v -> {
            boolean hasCommonUpdate = SystemProperties.getBoolean(BaseConstants.SP_KEY_CAMERA_UPDATE_STATE, true);
            if (hasCommonUpdate) {
                startActivity(new Intent(this, CameraDescriptionActivity.class));
            } else startActivity(new Intent(this, GuideCameraActivity.class));
            finish();
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        MaterialCardView cardView = (view instanceof MaterialCardView) ? (MaterialCardView) view : null;
        if (cardView == null) return;
        if (hasFocus) {
            cardView.setStrokeColor(0xFF4D79FF);
            cardView.setStrokeWidth(DisplayUtil.dip2px(this, 1f));
        } else {
            cardView.setStrokeColor(0x00FFFFFF);
            cardView.setStrokeWidth(0);
        }
    }
}
