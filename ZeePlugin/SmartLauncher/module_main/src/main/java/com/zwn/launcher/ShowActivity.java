package com.zwn.launcher;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.card.MaterialCardView;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.ui.BaseActivity;
import com.zeewain.base.utils.DensityUtils;
import com.zeewain.base.utils.DisplayUtil;
import com.zeewain.base.utils.SystemProperties;

public class ShowActivity extends BaseActivity implements View.OnFocusChangeListener {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DensityUtils.autoWidth(getApplication(), this);
        if (!SystemProperties.getBoolean("persist.sys.zee.camera.function.switch", false)) {
            int showActionCode = getIntent().getIntExtra(BaseConstants.EXTRA_SHOW_ACTION, -1);
            if (showActionCode > 0) {
                setContentView(R.layout.activity_show);
                ConstraintLayout clShowOld = findViewById(R.id.cl_show_old);
                clShowOld.setVisibility(View.VISIBLE);
                TextView textView = findViewById(R.id.txt_show_tip);
                if (showActionCode == BaseConstants.ShowCode.CODE_CAMERA_ERROR) {
                    textView.setText("摄像头异常，请重新插拔USB摄像头或者重启设备！");
                } else if (showActionCode == BaseConstants.ShowCode.CODE_CAMERA_INVALID) {
                    textView.setText("未找到可使用的摄像头！");
                } else {
                    textView.setText("未知错误！");
                }
                ConstraintLayout constraintLayout = findViewById(R.id.scl_confirm);
                constraintLayout.requestFocus();
                constraintLayout.setOnClickListener(v -> finish());
            } else {
                finish();
            }
        } else {
            boolean isUseRemoteCamera = getIntent().getBooleanExtra(BaseConstants.EXTRA_USE_REMOTE_CAMERA, false);
            setContentView(R.layout.activity_show);
            ConstraintLayout clShowNew = findViewById(R.id.cl_show_new);
            clShowNew.setVisibility(View.VISIBLE);
            TextView tvTips1 = findViewById(R.id.tv_cam_exception_tips_1);
            TextView tvTips2 = findViewById(R.id.tv_cam_exception_tips_2);
            TextView tvNext = findViewById(R.id.tv_camera_exception);
            MaterialCardView cardNext = findViewById(R.id.card_next);
            MaterialCardView cardCancel = findViewById(R.id.card_cancel);
            cardNext.setOnFocusChangeListener(this);
            cardCancel.setOnFocusChangeListener(this);
            if (!isUseRemoteCamera) {
                tvTips1.setText("USB摄像头连接中断");
                tvTips2.setText("请重新拔插USB摄像头或重启设备，确保正常体验");
                tvNext.setText("切换为手机连接");
            } else {
                tvTips1.setText("手机摄像头连接中断");
                tvTips2.setText("请重新连接摄像头，确保正常体验");
                tvNext.setText("前往连接指引");
            }

            cardNext.setOnClickListener(v -> {
                Intent intent = new Intent();
                intent.setAction("com.zee.setting.SHOW_GUIDE_ACTION");
                startActivity(intent);
                finish();
            });
            cardCancel.setOnClickListener(v -> finish());
        }
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