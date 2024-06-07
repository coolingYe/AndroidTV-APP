package com.zwn.user.ui.login;


import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.zee.zxing.encode.CodeCreator;
import com.zeewain.base.BaseApplication;
import com.zeewain.base.config.SharePrefer;
import com.zeewain.base.model.DataLoadState;
import com.zeewain.base.model.LoadState;
import com.zeewain.base.ui.BaseActivity;
import com.zeewain.base.utils.DensityUtils;
import com.zeewain.base.utils.DisplayUtil;
import com.zeewain.base.utils.SPUtils;
import com.zwn.user.R;
import com.zwn.user.data.UserRepository;
import com.zwn.user.data.model.SessionInfoMo;
import com.zwn.user.data.model.SessionResultLoadState;
import com.zwn.user.data.protocol.response.CreateSessionResp;

public class QrcodeLoginActivity extends BaseActivity {

    private static final String TAG = "QrcodeLoginActivity";
    private QrcodeLoginViewModel viewModel;
    private ImageView ivQrcodeLoginQrcode;
    private ImageView ivQrcodeLoginRefresh;
    private Animation refreshAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Theme_CustomMyDialog);
        setFinishOnTouchOutside(false);
        QrcodeLoginViewModelFactory factory = new QrcodeLoginViewModelFactory(UserRepository.getInstance());
        viewModel = new ViewModelProvider(this, factory).get(QrcodeLoginViewModel.class);

        DensityUtils.autoWidth(getApplication(), this);
        setContentView(R.layout.activity_qrcode_login);

        ivQrcodeLoginQrcode = findViewById(R.id.iv_qrcode_login_qrcode);
        ivQrcodeLoginRefresh = findViewById(R.id.iv_qrcode_login_refresh);
        refreshAnimation = AnimationUtils.loadAnimation(ivQrcodeLoginRefresh.getContext(), R.anim.rotate_loading_anim);
        LinearInterpolator interpolator = new LinearInterpolator();
        refreshAnimation.setInterpolator(interpolator);

        initViewObservable();

        viewModel.createLoginSession();
    }

    private void initViewObservable(){
        viewModel.mldCreateLoginSessionLoadState.observe(this, new Observer<DataLoadState<CreateSessionResp>>() {
            @Override
            public void onChanged(DataLoadState<CreateSessionResp> dataLoadState) {
                if(LoadState.Success == dataLoadState.loadState){
                    stopRefreshAnim();
                    ivQrcodeLoginRefresh.setVisibility(View.GONE);
                    ivQrcodeLoginQrcode.setVisibility(View.VISIBLE);
                    ivQrcodeLoginQrcode.setImageBitmap(CodeCreator.createQRCode(dataLoadState.data.loginPageUrl,
                            DisplayUtil.dip2px(QrcodeLoginActivity.this, 120),
                            DisplayUtil.dip2px(QrcodeLoginActivity.this, 120), null));
                    ivQrcodeLoginQrcode.getHandler().removeCallbacksAndMessages(null);
                    ivQrcodeLoginQrcode.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            viewModel.createLoginSession();
                        }
                    }, 10 * 60 * 1000);

                    long createSessionTime = System.currentTimeMillis();
                    viewModel.addReqSessionResult(dataLoadState.data.sessionId, createSessionTime);
                    Log.i(TAG, "start reqSessionResult() " + viewModel.reqSessionResultMap.size());
                    viewModel.reqSessionResult(new SessionInfoMo(dataLoadState.data.sessionId,
                            dataLoadState.data.loginPageUrl,
                            dataLoadState.data.sessionSecretKey, createSessionTime));
                }else if(LoadState.Failed == dataLoadState.loadState){
                    stopRefreshAnim();
                    ivQrcodeLoginQrcode.getHandler().removeCallbacksAndMessages(null);
                    ivQrcodeLoginQrcode.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            viewModel.createLoginSession();
                        }
                    }, 1000);
                }else{
                    ivQrcodeLoginRefresh.setVisibility(View.VISIBLE);
                    startRefreshAnim();
                    ivQrcodeLoginQrcode.setVisibility(View.GONE);
                }
            }
        });

        viewModel.mldReqSessionResultLoadState.observe(this, new Observer<SessionResultLoadState>() {
            @Override
            public void onChanged(SessionResultLoadState sessionResultLoadState) {
                if(LoadState.Success == sessionResultLoadState.loadState){
                    SPUtils.getInstance().put(SharePrefer.userToken, sessionResultLoadState.token);
                    BaseApplication.userToken = sessionResultLoadState.token;
                    finish();
                }else if(LoadState.Failed == sessionResultLoadState.loadState){//一个是10011=等待超时（等待超过60s），一个是10012=会话失效
                    if(10012 == sessionResultLoadState.errCode){//session invalid
                        ivQrcodeLoginQrcode.getHandler().removeCallbacksAndMessages(null);
                        viewModel.createLoginSession();
                    }else {
                        if(System.currentTimeMillis() - sessionResultLoadState.sessionInfoMo.createTime < 10 * 60 * 1000){
                            if(viewModel.reqSessionResultMap.containsKey(sessionResultLoadState.sessionInfoMo.sessionId))
                                Log.i(TAG, "restart reqSessionResult() " + viewModel.reqSessionResultMap.size());
                                viewModel.reqSessionResult(sessionResultLoadState.sessionInfoMo);
                        }else{
                            viewModel.reqSessionResultMap.remove(sessionResultLoadState.sessionInfoMo.sessionId);
                        }
                    }
                }
            }
        });

        viewModel.mldToastMsg.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String msg) {
                showToast(msg);
            }
        });
    }

    public void startRefreshAnim(){
        ivQrcodeLoginRefresh.clearAnimation();
        ivQrcodeLoginRefresh.setAnimation(refreshAnimation);
        refreshAnimation.start();
    }

    public void stopRefreshAnim(){
        refreshAnimation.cancel();
        ivQrcodeLoginRefresh.clearAnimation();
    }

    @Override
    protected void onDestroy() {
        ivQrcodeLoginQrcode.getHandler().removeCallbacksAndMessages(null);
        ivQrcodeLoginRefresh.clearAnimation();
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}