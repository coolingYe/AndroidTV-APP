package com.zee.launcher.login.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.zee.launcher.login.R;
import com.zee.launcher.login.data.UserRepository;
import com.zee.launcher.login.ui.login.LoginFragment;
import com.zee.launcher.login.ui.login.SelectUserFragment;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.config.SharePrefer;
import com.zeewain.base.model.LoadState;
import com.zeewain.base.ui.BaseActivity;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.DensityUtils;
import com.zeewain.base.utils.SPUtils;
import com.zeewain.base.widgets.LoadingView;
import com.zeewain.base.widgets.NetworkErrView;


public class LoginActivity extends BaseActivity {
    private LoginViewModel viewModel;
    private LoadingView loadingViewLogin;
    private NetworkErrView networkErrViewLogin;
    private ConstraintLayout contentLayout;
    private static final String FRAGMENT_TAG_LOGIN = "Login";
    private static final String FRAGMENT_TAG_USER_SELECT = "UserSelect";
    private Fragment currentFragment;
    private LoginFragment loginFragment;
    private SelectUserFragment selectUserFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LoginViewModelProvider factory = new LoginViewModelProvider(UserRepository.getInstance());
        viewModel = new ViewModelProvider(this, factory).get(LoginViewModel.class);

        DensityUtils.autoWidth(getApplication(), this);
        setContentView(R.layout.activity_login);

        intiView();
        initViewObservable();

        boolean isRegister = getIntent().getBooleanExtra(BaseConstants.EXTRA_REGISTER, false);
        if (isRegister) {
            viewModel.isDeviceActivated = false;
            showFragment(FRAGMENT_TAG_LOGIN);
        } else {
            viewModel.isDeviceActivated = true;

            if (isTokenExistButAkSkLost()) {
                viewModel.reqUserInfo();
            } else {
                viewModel.reqDeviceInfo();
            }
        }
    }

    private boolean isTokenExistButAkSkLost() {
        String userToken = SPUtils.getInstance().getString(SharePrefer.userToken);
        if (userToken != null && !userToken.isEmpty()) {
            String akSkInfo = SPUtils.getInstance().getString(SharePrefer.akSkInfo);
            if (akSkInfo == null || akSkInfo.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private synchronized void showFragment(String tag) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();

        if (currentFragment != null) {
            transaction.hide(currentFragment);
        }

        if (FRAGMENT_TAG_LOGIN.equals(tag)) {
            if (loginFragment == null) {
                loginFragment = LoginFragment.newInstance();
                if (currentFragment == null) {
                    transaction.replace(R.id.fl_login_root, loginFragment);
                } else {
                    transaction.add(R.id.fl_login_root, loginFragment);
                }
            } else {
                transaction.show(loginFragment);
            }
            currentFragment = loginFragment;
        } else if (FRAGMENT_TAG_USER_SELECT.equals(tag)) {
            if (selectUserFragment == null) {
                selectUserFragment = SelectUserFragment.newInstance();
                if (currentFragment == null) {
                    transaction.replace(R.id.fl_login_root, selectUserFragment);
                } else {
                    transaction.add(R.id.fl_login_root, selectUserFragment);
                }
            } else {
                transaction.show(selectUserFragment);
            }
            currentFragment = selectUserFragment;
        }

        transaction.commit();
    }

    private void intiView() {
        loadingViewLogin = findViewById(R.id.loadingView_login);
        networkErrViewLogin = findViewById(R.id.networkErrView_login);
        contentLayout = findViewById(R.id.cl_login_layout);

        networkErrViewLogin.setRetryClickListener(() -> {
            if (LoadState.Failed == viewModel.mldInitUserDataState.getValue()) {
                viewModel.reqUserInfo();
            } else {
                viewModel.reqDeviceInfo();
            }
        });
    }

    private void initViewObservable() {
        viewModel.mldToastMsg.observe(this, msg -> showToast(msg));

        viewModel.mldDeviceInfoLoadState.observe(this, dataLoadState -> {
            if (LoadState.Success == dataLoadState.loadState) {
                loadingViewLogin.setVisibility(View.GONE);
                loadingViewLogin.stopAnim();
                networkErrViewLogin.setVisibility(View.GONE);
                contentLayout.setVisibility(View.VISIBLE);
                if (dataLoadState.data == BaseConstants.DeviceStatus.UNACTIVATED) {
                    SPUtils.getInstance().clear();
                    CommonUtils.startGuideActivity(this, true);
                    finish();
                } else {
                    showFragment(FRAGMENT_TAG_LOGIN);
                }
            } else if (LoadState.Failed == dataLoadState.loadState) {
                loadingViewLogin.setVisibility(View.GONE);
                loadingViewLogin.stopAnim();
                networkErrViewLogin.setVisibility(View.VISIBLE);
            } else {
                loadingViewLogin.setVisibility(View.VISIBLE);
                loadingViewLogin.startAnim();
                networkErrViewLogin.setVisibility(View.GONE);
                contentLayout.setVisibility(View.GONE);
            }
        });

        viewModel.mldUserBindingOrLoginState.observe(this, new Observer<LoadState>() {
            @Override
            public void onChanged(LoadState loadState) {
                if (LoadState.Success == loadState) {
                    hideLoadingDialog();
                    viewModel.isDeviceActivated = true;
                    viewModel.reqUserInfo();
                } else if (LoadState.Loading == loadState) {
                    showLoadingDialog();
                } else {
                    hideLoadingDialog();
                }
            }
        });

        viewModel.mldInitUserDataState.observe(this, loadState -> {
            if (LoadState.Success == loadState) {
                loadingViewLogin.setVisibility(View.GONE);
                loadingViewLogin.stopAnim();
                toMainActivity();
            } else if (LoadState.Loading == loadState) {
                if (loadingViewLogin.getVisibility() != View.VISIBLE) {
                    loadingViewLogin.setVisibility(View.VISIBLE);
                    loadingViewLogin.startAnim();
                }
                networkErrViewLogin.setVisibility(View.GONE);
                contentLayout.setVisibility(View.GONE);
            } else {
                loadingViewLogin.setVisibility(View.GONE);
                loadingViewLogin.stopAnim();
                networkErrViewLogin.setVisibility(View.VISIBLE);
            }
        });

        viewModel.mldReqSmsCodeLoadState.observe(this, loadState -> {
            if (LoadState.Loading == loadState) {
                showLoadingDialog();
            } else {
                hideLoadingDialog();
            }
        });

        viewModel.mldUserOptionInfoListSelect.observe(this, userOptionInfoList -> {
            hideLoadingDialog();
            showFragment(FRAGMENT_TAG_USER_SELECT);
        });

        viewModel.mldUserSelectBack.observe(this, aBoolean -> {
            if (aBoolean) {
                showFragment(FRAGMENT_TAG_LOGIN);
            }
        });
    }

    private void toMainActivity() {
        if (CommonUtils.startMainActivity(this)) {
            finish();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (currentFragment != null) {
                if (!(currentFragment instanceof LoginFragment)) {
                    showFragment(FRAGMENT_TAG_LOGIN);
                    return true;
                }

                boolean isGuideDone = SPUtils.getInstance().getBoolean(SharePrefer.GuideDone);
                if (isGuideDone && (currentFragment instanceof LoginFragment)) {
                    return true;
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onDestroy() {
        viewModel.mCountDownTimer.cancel();
        super.onDestroy();
    }
}