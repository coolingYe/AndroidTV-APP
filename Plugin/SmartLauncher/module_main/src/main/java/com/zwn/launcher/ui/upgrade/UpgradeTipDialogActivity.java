package com.zwn.launcher.ui.upgrade;



import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.card.MaterialCardView;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.data.protocol.response.UpgradeResp;
import com.zeewain.base.model.LoadState;
import com.zeewain.base.ui.BaseActivity;
import com.zeewain.base.utils.ApkUtil;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.DensityUtils;
import com.zeewain.base.utils.DisplayUtil;
import com.zeewain.base.widgets.LoadingView;
import com.zwn.launcher.R;
import com.zwn.launcher.data.DataRepository;


public class UpgradeTipDialogActivity extends BaseActivity implements View.OnFocusChangeListener {
    private TextView titleView;
    private TextView messageView;
    private TextView confirmTextView;
    private MaterialCardView confirmView;
    private ConstraintLayout positiveCancelLayout;
    private ConstraintLayout clUpgradeTipRoot;
    private LoadingView loadingViewUpgradeTip;
    private UpgradeTipViewModel viewModel;
    private int countDown = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DensityUtils.autoWidth(getApplication(), this);
        setContentView(R.layout.activity_upgrade_tip);
        setFinishOnTouchOutside(false);

        UpgradeTipViewModelFactory factory = new UpgradeTipViewModelFactory(DataRepository.getInstance());
        viewModel = new ViewModelProvider(this, factory).get(UpgradeTipViewModel.class);

        titleView = findViewById(R.id.txt_title_dialog);
        messageView = findViewById(R.id.txt_message_dialog);
        MaterialCardView positiveView = findViewById(R.id.card_positive_dialog);
        MaterialCardView cancelView = findViewById(R.id.card_cancel_dialog);
        confirmView = findViewById(R.id.card_confirm_dialog);
        confirmTextView = findViewById(R.id.txt_confirm_dialog);

        positiveCancelLayout = findViewById(R.id.layout_positive_cancel_dialog);

        clUpgradeTipRoot = findViewById(R.id.cl_upgrade_tip_root);
        loadingViewUpgradeTip = findViewById(R.id.loadingView_upgrade_tip);

        positiveView.setOnFocusChangeListener(this);
        cancelView.setOnFocusChangeListener(this);
        confirmView.setOnFocusChangeListener(this);

        positiveView.setOnClickListener(v -> {
            handleUpgrade();
        });

        cancelView.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });

        confirmView.setOnClickListener(v -> {
            handleUpgrade();
        });

        initViewObservable();

        Bundle bundle = getIntent().getBundleExtra(BaseConstants.EXTRA_UPGRADE_INFO);
        if(bundle != null) {
            viewModel.hostAppUpgradeResp = (UpgradeResp) bundle.getSerializable(BaseConstants.EXTRA_UPGRADE_INFO);
            viewModel.mldHostAppUpgradeState.setValue(LoadState.Success);
        }else {
            viewModel.reqHostAppUpgrade(ApkUtil.getAppVersionName(this));
        }
    }

    private void handleUpgrade(){
        UpgradeDialogActivity.showUpgradeDialog(this, viewModel.hostAppUpgradeResp);
        setResult(RESULT_OK);
        finish();
    }

    private void countDownToUpgrade(){
        confirmTextView.setText("升级(" + countDown + "s)");
        confirmView.postDelayed(() -> {
            countDown--;
            if (countDown == 0) {
                handleUpgrade();
            } else if (countDown > 0) {
                countDownToUpgrade();
            }
        }, 1000);
    }

    private void initViewObservable() {
        viewModel.mldHostAppUpgradeState.observe(this, loadState -> {
            if (LoadState.Success == loadState) {
                loadingViewUpgradeTip.stopAnim();
                loadingViewUpgradeTip.setVisibility(View.GONE);
                if(viewModel.hostAppUpgradeResp != null){
                    clUpgradeTipRoot.setVisibility(View.VISIBLE);
                    if(viewModel.hostAppUpgradeResp.isForcible()){
                        positiveCancelLayout.setVisibility(View.GONE);
                        confirmView.setVisibility(View.VISIBLE);
                        if (confirmView.getHandler() != null) {
                            confirmView.getHandler().removeCallbacksAndMessages(null);
                        }
                        countDown = 10;
                        countDownToUpgrade();
                    }else{
                        positiveCancelLayout.setVisibility(View.VISIBLE);
                        confirmView.setVisibility(View.GONE);
                    }
                    titleView.setText("检测到新版本");
                    messageView.setText("V" + viewModel.hostAppUpgradeResp.getSoftwareVersion());
                }else{
                    showToast("已是最新版本了！");
                    delayFinish();
                }
            }else if(LoadState.Failed == loadState){
                loadingViewUpgradeTip.stopAnim();
                loadingViewUpgradeTip.setVisibility(View.GONE);
                showToast("网络异常！");
                delayFinish();
            }else{
                loadingViewUpgradeTip.setVisibility(View.VISIBLE);
                loadingViewUpgradeTip.startAnim();
            }
        });
    }

    @Override
    protected void onDestroy() {
        countDown = -1;
        if (confirmView.getHandler() != null) {
            confirmView.getHandler().removeCallbacksAndMessages(null);
        }
        super.onDestroy();
    }

    private void delayFinish(){
        loadingViewUpgradeTip.postDelayed(() -> finish(), 600);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle bundle =intent.getBundleExtra(BaseConstants.EXTRA_UPGRADE_INFO);
        if(bundle != null) {
            viewModel.hostAppUpgradeResp = (UpgradeResp) bundle.getSerializable(BaseConstants.EXTRA_UPGRADE_INFO);
            viewModel.mldHostAppUpgradeState.setValue(LoadState.Success);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) || (keyCode == KeyEvent.KEYCODE_HOME)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        MaterialCardView cardView = (view instanceof MaterialCardView) ? (MaterialCardView) view : null;
        if (cardView == null) return;
        final int strokeWidth = DisplayUtil.dip2px(view.getContext(), 1);
        if (hasFocus) {
            cardView.setStrokeColor(getResources().getColor(R.color.selectedStrokeColorPurple));
            cardView.setStrokeWidth(strokeWidth);
            CommonUtils.scaleView(view, 1.1f);
        } else {
            cardView.setStrokeColor(getResources().getColor(R.color.unselectedStrokeColor));
            cardView.setStrokeWidth(0);
            view.clearAnimation();
            CommonUtils.scaleView(view, 1f);
        }
    }
}