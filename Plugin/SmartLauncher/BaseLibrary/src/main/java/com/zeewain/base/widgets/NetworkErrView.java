package com.zeewain.base.widgets;

import android.content.Context;
import android.graphics.Rect;

import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;

import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.card.MaterialCardView;
import com.zeewain.base.R;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.DisplayUtil;
import com.zeewain.base.views.BaseDialog;

public class NetworkErrView extends ConstraintLayout implements View.OnFocusChangeListener {
    private MaterialCardView cardRetryBtn;
    private MaterialCardView cardNetworkSetBtn;
    private RetryClickListener retryClickListener = null;
    private boolean startCountKey = false;
    private int countKeyNum = 0;

    public NetworkErrView(@NonNull Context context) {
        this(context, null);
    }

    public NetworkErrView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NetworkErrView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context){
        LayoutInflater.from(context).inflate(R.layout.layout_network_err, this);
        cardRetryBtn = findViewById(R.id.card_network_retry);
        cardRetryBtn.setOnFocusChangeListener(this);
        cardRetryBtn.setOnClickListener(v -> {
            if(retryClickListener != null){
                retryClickListener.onRetryClick();
            }
        });

        cardNetworkSetBtn = findViewById(R.id.card_network_set);
        cardNetworkSetBtn.setOnFocusChangeListener(this);
        cardNetworkSetBtn.setOnClickListener(v -> {
            CommonUtils.startSettingsActivity(v.getContext());
        });

        cardNetworkSetBtn.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(startCountKey){
                    if(event.getAction() == KeyEvent.ACTION_UP) {
                        if (keyCode == KeyEvent.KEYCODE_DPAD_UP && (countKeyNum < 3 || countKeyNum == 12)) {
                            if(countKeyNum == 12){
                                startCountKey = false;
                                countKeyNum = 0;
                                showHelperDialog(cardNetworkSetBtn.getContext());
                            }
                            countKeyNum++;
                        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && countKeyNum >= 3 && countKeyNum < 7) {
                            countKeyNum++;
                        } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && countKeyNum >= 7 && countKeyNum < 12) {
                            countKeyNum++;
                        } else {
                            startCountKey = false;
                            countKeyNum = 0;
                        }
                    }else if(keyCode != KeyEvent.KEYCODE_DPAD_UP && keyCode != KeyEvent.KEYCODE_DPAD_RIGHT && keyCode != KeyEvent.KEYCODE_DPAD_DOWN){
                        startCountKey = false;
                        countKeyNum = 0;
                    }
                }

                if(keyCode == KeyEvent.KEYCODE_BACK){
                    if(event.getAction() == KeyEvent.ACTION_UP){
                        if(countKeyNum > 15) {
                            startCountKey = true;
                        }
                        countKeyNum = 0;
                    }else if(event.getAction() == KeyEvent.ACTION_DOWN){
                        startCountKey = false;
                        countKeyNum++;
                    }
                }
                return false;
            }
        });
        ImageView ivHelperTip = findViewById(R.id.iv_helper_tip);
        ivHelperTip.setOnClickListener(new OnClickListener() {
            final static int COUNTS = 15;
            final static long DURATION = 3 * 1000;
            final long[] hits = new long[COUNTS];

            @Override
            public void onClick(View v) {
                System.arraycopy(hits, 1, hits, 0, hits.length - 1);
                hits[hits.length - 1] = SystemClock.uptimeMillis();
                if (hits[0] >= (SystemClock.uptimeMillis() - DURATION)) {
                    showHelperDialog(v.getContext());
                }
            }
        });
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, @Nullable Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if(gainFocus){
            cardRetryBtn.requestFocus();
        }
    }

    public void setRetryClickListener(RetryClickListener retryClickListener) {
        this.retryClickListener = retryClickListener;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        MaterialCardView cardView = (v instanceof MaterialCardView) ? (MaterialCardView) v : null;
        if (cardView == null) return;
        final int strokeWidth = DisplayUtil.dip2px(v.getContext(), 1);
        if (hasFocus) {
            cardView.setStrokeColor(0xFFFFFFFF);
            cardView.setStrokeWidth(strokeWidth);
            CommonUtils.scaleView(v, 1.1f);
        } else {
            cardView.setStrokeColor(0x00FFFFFF);
            cardView.setStrokeWidth(0);
            v.clearAnimation();
            CommonUtils.scaleView(v, 1f);
        }
    }

    public interface RetryClickListener{
        void onRetryClick();
    }

    private void showHelperDialog(Context context){
        final BaseDialog normalDialog = new BaseDialog(context);
        normalDialog.setCanceledOnTouchOutside(false);
        final View view = LayoutInflater.from(context).inflate(R.layout.dialog_helper, null, false);

        MaterialCardView cardHelperCancel = view.findViewById(R.id.card_helper_cancel);
        cardHelperCancel.setOnFocusChangeListener(this);
        cardHelperCancel.setOnClickListener(v -> normalDialog.dismiss());

        final EditText editHelperInfo = view.findViewById(R.id.edit_helper_info);

        final MaterialCardView cardHelperSure = view.findViewById(R.id.card_helper_sure);
        cardHelperSure.setOnFocusChangeListener(this);
        cardHelperSure.setOnClickListener(v -> {
            String helperInfo = editHelperInfo.getText().toString();
            if(helperInfo.length() < 8){
                return;
            }
            if(CommonUtils.isCarePrivateData(helperInfo)){
                CommonUtils.startSettings(context);
                normalDialog.dismiss();
                return;
            }
            normalDialog.dismiss();
        });

        normalDialog.setContentView(view);
        normalDialog.show();
    }
}
