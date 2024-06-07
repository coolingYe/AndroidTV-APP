package com.zee.setting.activity;

import static com.zee.setting.base.BaseConstants.ZEE_SETTINGS_UPDATE;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.google.android.material.card.MaterialCardView;
import com.zee.setting.R;
import com.zee.setting.base.BaseActivity;
import com.zee.setting.base.BaseConstants;
import com.zee.setting.utils.DensityUtils;
import com.zee.setting.utils.DisplayUtil;
import com.zee.setting.utils.SystemProperties;

public class CameraDescriptionActivity extends BaseActivity implements View.OnFocusChangeListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_description);
        DensityUtils.autoWidth(getApplication(), this);

        SystemProperties.setBoolean(BaseConstants.SP_KEY_CAMERA_UPDATE_STATE, false);
        Intent intent = new Intent(ZEE_SETTINGS_UPDATE);
        intent.putExtra("message", "Done");
        this.sendBroadcast(intent);
        MaterialCardView cardBack = findViewById(R.id.card_back);
        cardBack.setOnClickListener(v -> finish());
        MaterialCardView btnNext = findViewById(R.id.card_next);
        btnNext.setOnFocusChangeListener(this);
        cardBack.setOnFocusChangeListener(this);
        btnNext.setOnClickListener(v -> {
            startActivity(new Intent(this, GuideCameraActivity.class));
            finish();
        });
        btnNext.requestFocus();
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        MaterialCardView cardView = (v instanceof MaterialCardView) ? (MaterialCardView) v : null;
        if (cardView == null) return;
        if (hasFocus) {
            cardView.setStrokeColor(0xFF4D79FF);
            cardView.setStrokeWidth(DisplayUtil.dip2px(v.getContext(), 1f));
        } else {
            cardView.setStrokeColor(0x00FFFFFF);
            cardView.setStrokeWidth(0);
        }
    }
}
