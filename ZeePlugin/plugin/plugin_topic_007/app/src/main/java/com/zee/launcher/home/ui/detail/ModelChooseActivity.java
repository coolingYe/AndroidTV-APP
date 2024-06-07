package com.zee.launcher.home.ui.detail;

import android.os.Bundle;
import android.widget.LinearLayout;

import com.google.android.material.card.MaterialCardView;
import com.zee.launcher.home.R;
import com.zeewain.base.ui.BaseActivity;
import com.zeewain.base.utils.DensityUtils;
import com.zeewain.base.utils.DisplayUtil;

public class ModelChooseActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);
        DensityUtils.autoWidth(getApplication(), this);

        LinearLayout llModel1 = findViewById(R.id.ll_model1);
        LinearLayout llModel2 = findViewById(R.id.ll_model2);

        llModel1.setOnClickListener(v -> {
            setResult(1,null);
            finish();
        });

        llModel1.requestFocus();

        llModel2.setOnClickListener(v -> {
            setResult(2,null);
            finish();
        });

        MaterialCardView btnCancel = findViewById(R.id.card_cancel);
        btnCancel.setOnFocusChangeListener((v, hasFocus) -> {
            MaterialCardView cardView = (v instanceof MaterialCardView) ? (MaterialCardView) v : null;
            if (cardView == null) return;
            if (hasFocus) {
                cardView.setStrokeColor(0xFF754AFF);
                cardView.setStrokeWidth(DisplayUtil.dip2px(v.getContext(), 1f));
            } else {
                cardView.setStrokeColor(0x00FFFFFF);
                cardView.setStrokeWidth(0);
            }
        });
        btnCancel.setOnClickListener(v -> {
            finish();
        });
    }
}
