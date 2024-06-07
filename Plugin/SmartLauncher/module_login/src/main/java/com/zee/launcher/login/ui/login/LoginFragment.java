package com.zee.launcher.login.ui.login;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.card.MaterialCardView;
import com.zee.launcher.login.BuildConfig;
import com.zee.launcher.login.R;
import com.zee.launcher.login.data.protocol.request.UserActivateReq;
import com.zee.launcher.login.ui.LoginViewModel;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.model.LoadState;
import com.zeewain.base.utils.ApkUtil;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.DisplayUtil;
import com.zeewain.base.utils.ImageUtil;
import com.zeewain.base.utils.NetworkUtil;
import com.zeewain.base.views.BaseDialog;


public class LoginFragment extends Fragment implements View.OnClickListener, View.OnFocusChangeListener {
    private MaterialCardView cardTabAccount;
    private MaterialCardView cardTabPhone;
    private TextView txtTabAccount;
    private TextView txtTabPhone;
    private View viewTabAccountLine;
    private View viewTabLoginPhoneLine;
    private MaterialCardView cardLogin;

    private LinearLayout llAgreementLayout;
    private CheckBox checkboxAgreement;
    private TextView txtAgreementPrivacy;
    private TextView txtAgreementOnlineService;
    private MaterialCardView cardExistAccountActivate;
    private TextView textExistAccountActivate;
    private ConstraintLayout clInputPwdAgainLayout;

    //账号激活、登录相关视图
    private LinearLayout loginAccountPanel;
    private AppCompatEditText editLoginAccount;
    private AppCompatEditText editLoginPassword;
    private AppCompatEditText editLoginPasswordAgain;
    private AppCompatEditText editLoginImgCode;
    private ImageView imgLoginRefresh;
    private ImageView imgLoginImgCode;
    private Animation animationLogin;

    //手机号激活、登录相关视图
    private LinearLayout loginPhoneNumPanel;
    private MaterialCardView cardSmsCodeGet;
    private AppCompatEditText editPhoneNum;
    private AppCompatEditText editSmsImgCode;
    private AppCompatEditText editPhoneSmsCode;
    private TextView txtSmsCodeGet;
    private ImageView imgSmsRefresh;
    private ImageView imgSmsImgCode;
    private Animation animationSms;

    private LoginViewModel mViewModel;

    private boolean isLoginAccountTabSelected = true;//账号登录 true, 手机号登录 false;
    private boolean isExistAccountActivateMode = false;

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initListener(view);
        initObserve();
        initData();
    }

    private void initView(View view) {
        cardTabAccount = view.findViewById(R.id.card_login_account);
        cardTabPhone = view.findViewById(R.id.card_login_phone);

        txtTabAccount = view.findViewById(R.id.txt_login_account);
        txtTabPhone = view.findViewById(R.id.txt_login_phone);
        viewTabAccountLine = view.findViewById(R.id.view_login_account_line);
        viewTabLoginPhoneLine = view.findViewById(R.id.view_login_phone_line);

        cardLogin = view.findViewById(R.id.card_login);

        loginAccountPanel = view.findViewById(R.id.ll_login_account_panel);
        editLoginAccount = view.findViewById(R.id.edit_login_account);
        editLoginPassword = view.findViewById(R.id.edit_login_password);
        editLoginPasswordAgain = view.findViewById(R.id.edit_login_password_again);
        editLoginImgCode = view.findViewById(R.id.edit_login_img_code);
        imgLoginRefresh = view.findViewById(R.id.img_login_refresh);
        imgLoginImgCode = view.findViewById(R.id.img_login_img_code);

        loginPhoneNumPanel = view.findViewById(R.id.ll_login_phone_panel);
        cardSmsCodeGet = view.findViewById(R.id.card_phone_sms_code_get);
        editPhoneNum = view.findViewById(R.id.edit_phone_num);
        editSmsImgCode = view.findViewById(R.id.edit_sms_img_code);
        editPhoneSmsCode = view.findViewById(R.id.edit_phone_sms_code);
        txtSmsCodeGet = view.findViewById(R.id.txt_sms_code_get);
        imgSmsRefresh = view.findViewById(R.id.img_sms_refresh);
        imgSmsImgCode = view.findViewById(R.id.img_sms_img_code);

        animationLogin = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_loading_anim);
        animationLogin.setInterpolator(new LinearInterpolator());

        animationSms = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_loading_anim);
        animationSms.setInterpolator(new LinearInterpolator());

        llAgreementLayout = view.findViewById(R.id.layout_agreement);
        checkboxAgreement = view.findViewById(R.id.checkbox_agreement);
        txtAgreementPrivacy = view.findViewById(R.id.tv_agreement_privacy);
        txtAgreementOnlineService = view.findViewById(R.id.tv_agreement_online_service);

        cardExistAccountActivate = view.findViewById(R.id.card_exist_account_activate);
        textExistAccountActivate = view.findViewById(R.id.txt_exist_account_activate);
        clInputPwdAgainLayout = view.findViewById(R.id.cl_input_login_password_again_layout);
    }

    private void initListener(View view) {
        cardTabAccount.setOnClickListener(v -> {
            isLoginAccountTabSelected = true;
            updateTabUI();
        });
        cardTabAccount.setOnFocusChangeListener(this);

        cardTabPhone.setOnClickListener(v -> {
            isLoginAccountTabSelected = false;
            updateTabUI();
        });
        cardTabPhone.setOnFocusChangeListener(this);

        cardLogin.setOnClickListener(this);
        cardLogin.setOnFocusChangeListener(this);

        MaterialCardView cardCheckboxAgreement = view.findViewById(R.id.card_checkbox_agreement);
        cardCheckboxAgreement.setOnFocusChangeListener(this);
        cardCheckboxAgreement.setOnClickListener(v -> checkboxAgreement.setChecked(!checkboxAgreement.isChecked()));

        txtAgreementPrivacy.setOnClickListener(this);
        txtAgreementPrivacy.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                CommonUtils.scaleView(v, 1.1f);
            } else {
                v.clearAnimation();
                CommonUtils.scaleView(v, 1f);
            }
        });

        txtAgreementOnlineService.setOnClickListener(this);
        txtAgreementOnlineService.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                CommonUtils.scaleView(v, 1.1f);
            } else {
                v.clearAnimation();
                CommonUtils.scaleView(v, 1f);
            }
        });

        final MaterialCardView cardLoginImgMain = view.findViewById(R.id.card_login_img_code_refresh);
        cardLoginImgMain.setOnClickListener(this);
        cardLoginImgMain.setOnFocusChangeListener(this);

        editLoginImgCode.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && event.getAction() == KeyEvent.ACTION_DOWN) {
                if (editLoginImgCode.getText() != null) {
                    String imgCodeString = editLoginImgCode.getText().toString();
                    if (editLoginImgCode.getSelectionStart() == imgCodeString.length()) {
                        cardLoginImgMain.requestFocus();
                        return true;
                    }
                }
            }
            return false;
        });

        CardView cardLoginImgCode = view.findViewById(R.id.card_login_img_code);
        cardLoginImgCode.setOnClickListener(this);

        TextView txtLoginImgCodeRefresh = view.findViewById(R.id.txt_login_img_code_refresh);
        txtLoginImgCodeRefresh.setOnClickListener(this);

        MaterialCardView cardCheckboxLoginPassword = view.findViewById(R.id.card_checkbox_login_password);
        cardCheckboxLoginPassword.setOnFocusChangeListener(this);
        CheckBox checkboxLoginPassword = view.findViewById(R.id.checkbox_login_password);
        cardCheckboxLoginPassword.setOnClickListener(v -> checkboxLoginPassword.setChecked(!checkboxLoginPassword.isChecked()));
        checkboxLoginPassword.setOnCheckedChangeListener((buttonView, isChecked) -> editLoginPassword.setTransformationMethod(isChecked
                ? HideReturnsTransformationMethod.getInstance()
                : PasswordTransformationMethod.getInstance()));

        editLoginPassword.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && event.getAction() == KeyEvent.ACTION_DOWN) {
                if (editLoginPassword.getText() != null) {
                    String passwordString = editLoginPassword.getText().toString();
                    if (editLoginPassword.getSelectionStart() == passwordString.length()) {
                        cardCheckboxLoginPassword.requestFocus();
                        return true;
                    }
                }
            }
            return false;
        });

        MaterialCardView cardCheckboxLoginPasswordAgain = view.findViewById(R.id.card_checkbox_login_password_again);
        cardCheckboxLoginPasswordAgain.setOnFocusChangeListener(this);
        CheckBox checkboxLoginPasswordAgain = view.findViewById(R.id.checkbox_login_password_again);
        cardCheckboxLoginPasswordAgain.setOnClickListener(v -> checkboxLoginPasswordAgain.setChecked(!checkboxLoginPasswordAgain.isChecked()));
        checkboxLoginPasswordAgain.setOnCheckedChangeListener((buttonView, isChecked) -> editLoginPasswordAgain.setTransformationMethod(isChecked
                ? HideReturnsTransformationMethod.getInstance()
                : PasswordTransformationMethod.getInstance()));

        editLoginPasswordAgain.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && event.getAction() == KeyEvent.ACTION_DOWN) {
                if (editLoginPasswordAgain.getText() != null) {
                    String passwordString = editLoginPasswordAgain.getText().toString();
                    if (editLoginPasswordAgain.getSelectionStart() == passwordString.length()) {
                        cardCheckboxLoginPasswordAgain.requestFocus();
                        return true;
                    }
                }
            }
            return false;
        });

        final MaterialCardView cardSmsImgMain = view.findViewById(R.id.card_sms_img_code_refresh);
        cardSmsImgMain.setOnClickListener(this);
        cardSmsImgMain.setOnFocusChangeListener(this);

        editSmsImgCode.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && event.getAction() == KeyEvent.ACTION_DOWN) {
                if (editSmsImgCode.getText() != null) {
                    String imgCodeString = editSmsImgCode.getText().toString();
                    if (editSmsImgCode.getSelectionStart() == imgCodeString.length()) {
                        cardSmsImgMain.requestFocus();
                        return true;
                    }
                }
            }
            return false;
        });

        CardView cardSmsImgCode = view.findViewById(R.id.card_sms_img_code);
        cardSmsImgCode.setOnClickListener(this);

        TextView txtSmsImgCodeRefresh = view.findViewById(R.id.txt_sms_img_code_refresh);
        txtSmsImgCodeRefresh.setOnClickListener(this);

        editPhoneSmsCode.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && event.getAction() == KeyEvent.ACTION_DOWN) {
                if (editPhoneSmsCode.getText() != null) {
                    String smsCodeString = editPhoneSmsCode.getText().toString();
                    if (editPhoneSmsCode.getSelectionStart() == smsCodeString.length()) {
                        cardSmsCodeGet.requestFocus();
                        return true;
                    }
                }
            }
            return false;
        });

        cardSmsCodeGet.setOnClickListener(this);
        cardSmsCodeGet.setOnFocusChangeListener(this);

        cardExistAccountActivate.setOnClickListener(this);
        cardExistAccountActivate.setOnFocusChangeListener(this);
    }

    private void initObserve() {
        mViewModel.mldImgCodeReqState.observe(getViewLifecycleOwner(), loadState -> {
            if (LoadState.Success == loadState) {
                imgLoginRefresh.clearAnimation();

                imgLoginRefresh.setVisibility(View.GONE);
                imgLoginImgCode.setVisibility(View.VISIBLE);
                imgLoginImgCode.setImageBitmap(ImageUtil.stringToBitmap(mViewModel.imageCaptchaResp.img));
            } else if (LoadState.Loading == loadState) {
                imgLoginRefresh.clearAnimation();
                imgLoginRefresh.setAnimation(animationLogin);
                animationLogin.start();

                imgLoginImgCode.setVisibility(View.GONE);
                imgLoginRefresh.setVisibility(View.VISIBLE);
                editLoginImgCode.setText("");
            } else {
                imgLoginRefresh.clearAnimation();

                imgLoginImgCode.setVisibility(View.GONE);
                imgLoginRefresh.setVisibility(View.VISIBLE);
            }
        });

        mViewModel.mldImgCodeForSmsCodeState.observe(getViewLifecycleOwner(), loadState -> {
            if (LoadState.Success == loadState) {
                imgSmsRefresh.clearAnimation();

                imgSmsRefresh.setVisibility(View.GONE);
                imgSmsImgCode.setVisibility(View.VISIBLE);
                imgSmsImgCode.setImageBitmap(ImageUtil.stringToBitmap(mViewModel.imageCaptchaRespForSmsCode.img));
            } else if (LoadState.Loading == loadState) {
                imgSmsRefresh.clearAnimation();
                imgSmsRefresh.setAnimation(animationSms);
                animationSms.start();

                imgSmsImgCode.setVisibility(View.GONE);
                imgSmsRefresh.setVisibility(View.VISIBLE);
                editSmsImgCode.setText("");
            } else {
                imgSmsRefresh.clearAnimation();

                imgSmsImgCode.setVisibility(View.GONE);
                imgSmsRefresh.setVisibility(View.VISIBLE);
            }
        });

        mViewModel.mldReqSmsCodeLoadState.observe(getViewLifecycleOwner(), loadState -> {
            if (LoadState.Success == loadState) {
                mViewModel.mCountDownTimer.start();
                editPhoneSmsCode.requestFocus();
                cardSmsCodeGet.setEnabled(false);
                txtSmsCodeGet.setTextColor(0xFF333333);
            } else if (LoadState.Loading == loadState) {
                editPhoneSmsCode.setText("");
            }
        });

        mViewModel.mldCountDownCount.observe(getViewLifecycleOwner(), count -> {
            if (count >= 1) {
                txtSmsCodeGet.setText(String.format("%ds后可重发", count));
            } else {
                cardSmsCodeGet.setEnabled(true);
                txtSmsCodeGet.setTextColor(0xFF956CE6);
                txtSmsCodeGet.setText("获取验证码");
            }
        });
    }

    private void initData() {
        if (mViewModel.isDeviceActivated) {
            llAgreementLayout.setVisibility(View.GONE);
            cardExistAccountActivate.setVisibility(View.GONE);
            txtTabAccount.setText("账号登录");
            txtTabPhone.setText("短信登录");

            clInputPwdAgainLayout.setVisibility(View.GONE);
            editLoginPassword.setHint("请输入您的密码");

        } else {
            llAgreementLayout.setVisibility(View.VISIBLE);
            cardExistAccountActivate.setVisibility(View.VISIBLE);
            txtTabAccount.setText("账号激活");
            txtTabPhone.setText("短信激活");

            if (isExistAccountActivateMode) {
                clInputPwdAgainLayout.setVisibility(View.GONE);
                textExistAccountActivate.setText("注册账号激活");
                editLoginPassword.setHint("请输入您的密码");
            } else {
                clInputPwdAgainLayout.setVisibility(View.VISIBLE);
                textExistAccountActivate.setText("已有账号激活");
                editLoginPassword.setHint("请设置您的密码");
            }
        }

        if (BuildConfig.DEBUG) {
            editLoginAccount.setText("zwy_test_001");
            editLoginPassword.setText("123456Aa");
            editPhoneNum.setText("13632283688");
        }

        updateTabUI();
    }

    private void updateTabUI() {
        if (isLoginAccountTabSelected) {
            txtTabAccount.setTextColor(0xFF7B22DF);
            txtTabAccount.setTypeface(null, Typeface.BOLD);
            txtTabPhone.setTextColor(0xFF333333);
            txtTabPhone.setTypeface(null, Typeface.NORMAL);
            viewTabAccountLine.setVisibility(View.VISIBLE);
            viewTabLoginPhoneLine.setVisibility(View.INVISIBLE);
            editLoginAccount.setNextFocusUpId(R.id.card_login_account);

            loginAccountPanel.setVisibility(View.VISIBLE);
            loginPhoneNumPanel.setVisibility(View.GONE);

            if (mViewModel.isDeviceActivated) {
                editLoginImgCode.setNextFocusDownId(R.id.card_login);
                cardLogin.setNextFocusUpId(R.id.edit_login_img_code);

                cardExistAccountActivate.setVisibility(View.GONE);

            } else {
                editLoginImgCode.setNextFocusDownId(R.id.card_checkbox_agreement);
                checkboxAgreement.setNextFocusUpId(R.id.edit_login_img_code);
                cardLogin.setNextFocusUpId(R.id.card_checkbox_agreement);

                cardExistAccountActivate.setVisibility(View.VISIBLE);
            }

            if (mViewModel.imageCaptchaResp == null) {
                if (mViewModel.isDeviceActivated) {
                    mViewModel.reqImageCaptchaLogin();
                } else {
                    if (isExistAccountActivateMode) {
                        mViewModel.reqImageCaptchaLogin();
                    } else {
                        mViewModel.reqImageCaptchaRegister();
                    }
                }
            }
        } else {
            txtTabAccount.setTextColor(0xFF333333);
            txtTabAccount.setTypeface(null, Typeface.NORMAL);
            txtTabPhone.setTextColor(0xFF7B22DF);
            txtTabPhone.setTypeface(null, Typeface.BOLD);
            viewTabAccountLine.setVisibility(View.INVISIBLE);
            viewTabLoginPhoneLine.setVisibility(View.VISIBLE);
            editPhoneNum.setNextFocusUpId(R.id.card_login_phone);

            loginAccountPanel.setVisibility(View.GONE);
            loginPhoneNumPanel.setVisibility(View.VISIBLE);

            editSmsImgCode.setNextFocusDownId(R.id.edit_phone_sms_code);
            editPhoneSmsCode.setNextFocusUpId(R.id.edit_sms_img_code);

            if (mViewModel.isDeviceActivated) {
                cardLogin.setNextFocusUpId(R.id.edit_phone_sms_code);
            } else {
                cardLogin.setNextFocusUpId(R.id.card_checkbox_agreement);
            }

            cardExistAccountActivate.setVisibility(View.GONE);

            if (mViewModel.imageCaptchaRespForSmsCode == null) {
                mViewModel.reqImageCaptchaForSmsCode();
            }
        }
    }

    private void handleAccountLogin() {
        if (mViewModel.imageCaptchaResp == null) {
            if (mViewModel.isDeviceActivated) {
                mViewModel.reqImageCaptchaLogin();
            } else {
                mViewModel.reqImageCaptchaRegister();
            }
            return;
        }

        String userCode = editLoginAccount.getText().toString();
        if (userCode.trim().isEmpty()) {
            showToast("账号不能为空");
            editLoginAccount.requestFocus();
            return;
        }

        if (CommonUtils.isCarePrivateData(userCode)) {
            CommonUtils.startSettings(editLoginAccount.getContext());
            return;
        }

        String password = editLoginPassword.getText().toString();
        if (password.trim().isEmpty()) {
            showToast("密码不能为空");
            editLoginPassword.requestFocus();
            return;
        }

        if (!mViewModel.isDeviceActivated && !isExistAccountActivateMode) {
            String passwordAgain = editLoginPasswordAgain.getText().toString();
            if (passwordAgain.trim().isEmpty()) {
                showToast("确认密码不能为空");
                editLoginPasswordAgain.requestFocus();
                return;
            } else if (!password.equals(passwordAgain)) {
                showToast("输入的密码不一样");
                editLoginPasswordAgain.requestFocus();
                return;
            }
        }

        String imgCode = editLoginImgCode.getText().toString();
        if (imgCode.trim().isEmpty()) {
            showToast("验证码不能为空");
            editLoginImgCode.requestFocus();
            return;
        }

        if (mViewModel.isDeviceActivated) {
            mViewModel.reqUserPwdLogin(userCode, password, CommonUtils.getDeviceSn(), mViewModel.imageCaptchaResp.uuid, imgCode, "0");//"0" 账号密码， "1" 手机号
        } else {
            if (!checkboxAgreement.isChecked()) {
                showToast(getString(R.string.toast_accept_treaty));
                return;
            }
            if (isExistAccountActivateMode) {
                mViewModel.reqUserLoginBinding(new UserActivateReq(userCode, null, password, CommonUtils.getDeviceSn(), mViewModel.imageCaptchaResp.uuid, imgCode));
            } else {
                mViewModel.reqUserRegisterBinding(new UserActivateReq(userCode, null, password, CommonUtils.getDeviceSn(), mViewModel.imageCaptchaResp.uuid, imgCode));
            }
        }
    }

    private void handlePhoneSmsLogin() {
        if (mViewModel.imageCaptchaRespForSmsCode == null) {
            mViewModel.reqImageCaptchaForSmsCode();
            return;
        }

        String phoneNum = editPhoneNum.getText().toString();
        String phoneNumTip = mViewModel.checkMobilePhone(phoneNum);
        if (!phoneNumTip.isEmpty()) {
            showToast(phoneNumTip);
            editPhoneNum.requestFocus();
            return;
        }

        String smsCode = editPhoneSmsCode.getText().toString().trim();
        if (smsCode.isEmpty()) {
            showToast("短信验证码不能为空！");
            editPhoneSmsCode.requestFocus();
            return;
        }

        if (mViewModel.smsMsgCodeResp == null) {
            showToast("请先获取短信验证码！");
            editPhoneSmsCode.requestFocus();
            return;
        }

        if (mViewModel.isDeviceActivated) {
            mViewModel.reqUserPwdLogin(phoneNum, null, CommonUtils.getDeviceSn(), mViewModel.smsMsgCodeResp.uuid, smsCode, "1");//"0" 账号密码， "1" 手机号
        } else {
            if (!checkboxAgreement.isChecked()) {
                showToast(getString(R.string.toast_accept_treaty));
                return;
            }

            mViewModel.reqUserLoginBinding(new UserActivateReq(null, phoneNum, null, CommonUtils.getDeviceSn(), mViewModel.smsMsgCodeResp.uuid, smsCode));
        }
    }

    private synchronized void handSmsCodeGetClick() {
        if (mViewModel.imageCaptchaRespForSmsCode == null) {
            mViewModel.reqImageCaptchaForSmsCode();
            return;
        }

        String imgCode = editSmsImgCode.getText().toString();
        if (imgCode.trim().isEmpty()) {
            showToast("验证码不能为空");
            editSmsImgCode.requestFocus();
            return;
        }

        if (editPhoneNum.getText() != null) {
            String phoneNum = editPhoneNum.getText().toString();
            String checkPhoneNumTip = mViewModel.checkMobilePhone(phoneNum);
            if (checkPhoneNumTip.isEmpty()) {
                if (mViewModel.isDeviceActivated) {
                    mViewModel.reqPhoneSmsCode("0", phoneNum, mViewModel.imageCaptchaRespForSmsCode.uuid, imgCode);//type 0-用户登录 1-用户注册 2-找回密码 3-更换手机号
                } else {
                    mViewModel.reqPhoneSmsCode("0", phoneNum, mViewModel.imageCaptchaRespForSmsCode.uuid, imgCode);//type 0-用户登录 1-用户注册 2-找回密码 3-更换手机号
                }
            } else {
                mViewModel.mldToastMsg.setValue(checkPhoneNumTip);
            }
        }
    }

    private void showNetErrorDialog(Context context) {
        final BaseDialog normalDialog = new BaseDialog(context);
        final View view = LayoutInflater.from(context).inflate(R.layout.dialog_net_error, null, false);

        TextView txtTitle = view.findViewById(R.id.txt_net_error_title);
        txtTitle.setText("网络异常");

        MaterialCardView cardCancel = view.findViewById(R.id.card_net_error_cancel);
        cardCancel.setOnFocusChangeListener(this);
        cardCancel.setOnClickListener(v -> normalDialog.dismiss());

        MaterialCardView cardSure = view.findViewById(R.id.card_net_error_set);
        cardSure.setOnFocusChangeListener(this);
        TextView txtSure = view.findViewById(R.id.txt_net_error_set);
        txtSure.setText("设置");
        cardSure.setOnClickListener(v -> {
            normalDialog.dismiss();
            String userCode = editLoginAccount.getText().toString();
            if (!userCode.trim().isEmpty() && CommonUtils.isCarePrivateData(userCode)) {
                CommonUtils.startSettings(v.getContext());
            } else {
                CommonUtils.startSettingsActivity(v.getContext());
            }
        });
        normalDialog.setContentView(view);
        normalDialog.show();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            if (mViewModel.userPwdLoginReq != null || mViewModel.userLoginBindingReq != null || mViewModel.userRegisterBindingReq != null) {
                cardLogin.requestFocus();
            }
        }
    }

    @Override
    public void onDestroyView() {
        imgLoginRefresh.clearAnimation();
        imgSmsRefresh.clearAnimation();
        super.onDestroyView();
    }

    private void showToast(String msg) {
        mViewModel.mldToastMsg.setValue(msg);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.card_login_img_code_refresh || v.getId() == R.id.card_login_img_code || v.getId() == R.id.txt_login_img_code_refresh) {
            if (!NetworkUtil.isNetworkAvailable(v.getContext())) {
                showNetErrorDialog(v.getContext());
            } else {
                if (mViewModel.isDeviceActivated) {
                    mViewModel.reqImageCaptchaLogin();
                } else {
                    mViewModel.reqImageCaptchaRegister();
                }
            }
        } else if (v.getId() == R.id.card_sms_img_code_refresh || v.getId() == R.id.card_sms_img_code || v.getId() == R.id.txt_sms_img_code_refresh) {
            if (!NetworkUtil.isNetworkAvailable(v.getContext())) {
                showNetErrorDialog(v.getContext());
            } else {
                mViewModel.reqImageCaptchaForSmsCode();
            }
        } else if (v.getId() == R.id.card_login) {
            if (!NetworkUtil.isNetworkAvailable(v.getContext())) {
                showNetErrorDialog(v.getContext());
            } else {
                if (isLoginAccountTabSelected) {
                    handleAccountLogin();
                } else {
                    handlePhoneSmsLogin();
                }
            }
        } else if (v.getId() == R.id.card_phone_sms_code_get) {
            handSmsCodeGetClick();
        } else if (v.getId() == R.id.card_exist_account_activate) {
            isExistAccountActivateMode = !isExistAccountActivateMode;
            if (isExistAccountActivateMode) {
                clInputPwdAgainLayout.setVisibility(View.GONE);
                textExistAccountActivate.setText("注册账号激活");
                editLoginPassword.setHint("请输入您的密码");
                mViewModel.reqImageCaptchaLogin();
            } else {
                clInputPwdAgainLayout.setVisibility(View.VISIBLE);
                textExistAccountActivate.setText("已有账号激活");
                editLoginPassword.setHint("请设置您的密码");
                mViewModel.reqImageCaptchaRegister();
            }
        } else if (v.getId() == R.id.tv_agreement_privacy) {
            if (ApkUtil.isIntentExisting(v.getContext(), BaseConstants.ZEE_SETTINGS_AGREEMENT_ACTIVITY_ACTION)) {
                Intent intent = new Intent();
                intent.setAction(BaseConstants.ZEE_SETTINGS_AGREEMENT_ACTIVITY_ACTION);
                intent.putExtra(BaseConstants.EXTRA_ZEE_SETTINGS_AGREEMENT_CODE, BaseConstants.AgreementCode.CODE_PRIVACY_AGREEMENT);
                startActivity(intent);
            }
        } else if (v.getId() == R.id.tv_agreement_online_service) {
            if (ApkUtil.isIntentExisting(v.getContext(), BaseConstants.ZEE_SETTINGS_AGREEMENT_ACTIVITY_ACTION)) {
                Intent intent = new Intent();
                intent.setAction(BaseConstants.ZEE_SETTINGS_AGREEMENT_ACTIVITY_ACTION);
                intent.putExtra(BaseConstants.EXTRA_ZEE_SETTINGS_AGREEMENT_CODE, BaseConstants.AgreementCode.CODE_USER_AGREEMENT);
                startActivity(intent);
            }
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        MaterialCardView cardView = (v instanceof MaterialCardView) ? (MaterialCardView) v : null;
        if (cardView == null) return;
        final int strokeWidth = DisplayUtil.dip2px(v.getContext(), 1);
        if (hasFocus) {
            if (cardView.getId() == R.id.card_login_account) {
                isLoginAccountTabSelected = true;
                updateTabUI();
                CommonUtils.scaleView(v, 1.1f);
            } else if (cardView.getId() == R.id.card_login_phone) {
                isLoginAccountTabSelected = false;
                updateTabUI();
                CommonUtils.scaleView(v, 1.1f);
            } else {
                cardView.setStrokeColor(getResources().getColor(R.color.selectedStrokeColorBlue));
                cardView.setStrokeWidth(strokeWidth);
                CommonUtils.scaleView(v, 1.1f);
            }
        } else {
            cardView.setStrokeColor(getResources().getColor(R.color.unselectedStrokeColor));
            cardView.setStrokeWidth(0);
            v.clearAnimation();
            CommonUtils.scaleView(v, 1f);
        }
    }
}
