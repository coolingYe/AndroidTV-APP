package com.zee.launcher.login.ui.login;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.text.Editable;
import android.text.TextWatcher;
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
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.card.MaterialCardView;
import com.zee.launcher.login.R;
import com.zee.launcher.login.ui.LoginViewModel;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.model.LoadState;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.DisplayUtil;
import com.zeewain.base.utils.ImageUtil;

import java.util.List;


public class RegisterFragment extends Fragment implements View.OnFocusChangeListener {
    private AppCompatEditText editRegisterAccount;
    private AppCompatEditText editRegisterPassword;
    private AppCompatEditText editRegisterPasswordConfirm;
    private AppCompatEditText editRegisterImgCode;
    private TextView txtRegisterAccountTip;
    private TextView txtRegisterPasswordTip;
    private TextView txtRegisterPasswordConfirmTip;
    private TextView txtAgreementPrivacy;
    private TextView txtAgreementOnlineService;
    private ImageView imgRegisterRefresh;
    private ImageView imgRegisterImgCode;
    private CheckBox checkboxAgreement;
    private LoginViewModel viewModel;
    private Animation animation;

    public RegisterFragment() { }


    public static RegisterFragment newInstance() {
        return new RegisterFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(LoginViewModel.class);
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView(view);
        initListener(view);
        initViewObservable();
        viewModel.reqImageCaptchaRegister();
    }

    private void initView(View view){
        editRegisterAccount = view.findViewById(R.id.edit_register_account);
        editRegisterPassword = view.findViewById(R.id.edit_register_password);
        editRegisterPasswordConfirm = view.findViewById(R.id.edit_register_password_confirm);
        editRegisterImgCode = view.findViewById(R.id.edit_register_img_code);

        txtRegisterAccountTip = view.findViewById(R.id.txt_register_account_tip);
        txtRegisterPasswordTip = view.findViewById(R.id.txt_register_password_tip);
        txtRegisterPasswordConfirmTip = view.findViewById(R.id.txt_register_password_confirm_tip);

        imgRegisterRefresh = view.findViewById(R.id.img_register_refresh);
        imgRegisterImgCode = view.findViewById(R.id.img_register_img_code);

        checkboxAgreement = view.findViewById(R.id.checkbox_agreement);
        txtAgreementPrivacy = view.findViewById(R.id.tv_agreement_privacy);
        txtAgreementOnlineService = view.findViewById(R.id.tv_agreement_online_service);

        animation = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_loading_anim);
        LinearInterpolator interpolator = new LinearInterpolator();
        animation.setInterpolator(interpolator);
    }

    private void initListener(View view){
        MaterialCardView cardRegister = view.findViewById(R.id.card_register);
        cardRegister.setOnClickListener(v -> handleUserActivate());
        cardRegister.setOnFocusChangeListener(this);
        cardRegister.setNextFocusDownId(R.id.card_checkbox_agreement);

        CheckBox checkboxRegisterPassword = view.findViewById(R.id.checkbox_register_password);
        final MaterialCardView cardCheckboxRegisterPassword = view.findViewById(R.id.card_checkbox_register_password);
        cardCheckboxRegisterPassword.setOnFocusChangeListener(this);
        cardCheckboxRegisterPassword.setOnClickListener(v -> checkboxRegisterPassword.setChecked(!checkboxRegisterPassword.isChecked()));
        checkboxRegisterPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editRegisterPassword.setTransformationMethod(isChecked
                        ? HideReturnsTransformationMethod.getInstance()
                        : PasswordTransformationMethod.getInstance());
            }
        });

        CheckBox checkboxRegisterPasswordConfirm = view.findViewById(R.id.checkbox_register_password_confirm);
        final MaterialCardView cardCheckboxRegisterPasswordConfirm = view.findViewById(R.id.card_checkbox_register_password_confirm);
        cardCheckboxRegisterPasswordConfirm.setOnFocusChangeListener(this);
        cardCheckboxRegisterPasswordConfirm.setOnClickListener(v -> checkboxRegisterPasswordConfirm.setChecked(!checkboxRegisterPasswordConfirm.isChecked()));
        checkboxRegisterPasswordConfirm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editRegisterPasswordConfirm.setTransformationMethod(isChecked
                        ? HideReturnsTransformationMethod.getInstance()
                        : PasswordTransformationMethod.getInstance());
            }
        });

        CardView cardRegisterImgCode = view.findViewById(R.id.card_register_img_code);
        cardRegisterImgCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.reqImageCaptchaRegister();
            }
        });

        MaterialCardView cardCheckboxAgreement = view.findViewById(R.id.card_checkbox_agreement);
        cardCheckboxAgreement.setOnFocusChangeListener(this);
        cardCheckboxAgreement.setOnClickListener(v -> checkboxAgreement.setChecked(!checkboxAgreement.isChecked()));

        MaterialCardView txtRegisterImgCodeRefresh = view.findViewById(R.id.card_register_img_code_refresh);
        txtRegisterImgCodeRefresh.setOnFocusChangeListener(this);
        txtRegisterImgCodeRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModel.reqImageCaptchaRegister();
            }
        });

        editRegisterImgCode.setOnKeyListener((v, keyCode, event) -> {
            if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && event.getAction() == KeyEvent.ACTION_DOWN){
                if(editRegisterImgCode.getText() != null){
                    String imgCodeString = editRegisterImgCode.getText().toString();
                    if(editRegisterImgCode.getSelectionStart() == imgCodeString.length()){
                        txtRegisterImgCodeRefresh.requestFocus();
                        return true;
                    }
                }
            }
            return false;
        });

        editRegisterAccount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() > 0){
                    txtRegisterAccountTip.setText("");
                }
            }
        });

        editRegisterPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                txtRegisterPasswordTip.setText("");
                txtRegisterPasswordTip.setTextColor(0x7F333333);
                txtRegisterPasswordConfirmTip.setText("");
                txtRegisterPasswordConfirmTip.setTextColor(0x7F333333);
            }
        });

        editRegisterPassword.setOnKeyListener((v, keyCode, event) -> {
            if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && event.getAction() == KeyEvent.ACTION_DOWN){
                if(editRegisterPassword.getText() != null){
                    String passwordString = editRegisterPassword.getText().toString();
                    if(editRegisterPassword.getSelectionStart() == passwordString.length()){
                        cardCheckboxRegisterPassword.requestFocus();
                        return true;
                    }
                }
            }
            return false;
        });

        editRegisterPasswordConfirm.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                txtRegisterPasswordConfirmTip.setText("");
                txtRegisterPasswordConfirmTip.setTextColor(0x7F333333);
            }
        });

        editRegisterPasswordConfirm.setOnKeyListener((v, keyCode, event) -> {
            if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && event.getAction() == KeyEvent.ACTION_DOWN){
                if(editRegisterPasswordConfirm.getText() != null){
                    String passwordString = editRegisterPasswordConfirm.getText().toString();
                    if(editRegisterPasswordConfirm.getSelectionStart() == passwordString.length()){
                        cardCheckboxRegisterPasswordConfirm.requestFocus();
                        return true;
                    }
                }
            }
            return false;
        });

        txtAgreementPrivacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isIntentExisting(v.getContext(), BaseConstants.ZEE_SETTINGS_AGREEMENT_ACTIVITY_ACTION)) {
                    Intent intent = new Intent();
                    intent.setAction(BaseConstants.ZEE_SETTINGS_AGREEMENT_ACTIVITY_ACTION);
                    intent.putExtra(BaseConstants.EXTRA_ZEE_SETTINGS_AGREEMENT_CODE, BaseConstants.AgreementCode.CODE_PRIVACY_AGREEMENT);
                    startActivity(intent);
                }
            }
        });
        txtAgreementPrivacy.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    CommonUtils.scaleView(v, 1.1f);
                } else {
                    v.clearAnimation();
                    CommonUtils.scaleView(v, 1f);
                }
            }
        });

        txtAgreementOnlineService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isIntentExisting(v.getContext(), BaseConstants.ZEE_SETTINGS_AGREEMENT_ACTIVITY_ACTION)) {
                    Intent intent = new Intent();
                    intent.setAction(BaseConstants.ZEE_SETTINGS_AGREEMENT_ACTIVITY_ACTION);
                    intent.putExtra(BaseConstants.EXTRA_ZEE_SETTINGS_AGREEMENT_CODE, BaseConstants.AgreementCode.CODE_USER_AGREEMENT);
                    startActivity(intent);
                }
            }
        });
        txtAgreementOnlineService.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    CommonUtils.scaleView(v, 1.1f);
                } else {
                    v.clearAnimation();
                    CommonUtils.scaleView(v, 1f);
                }
            }
        });

        //binding.txtRegisterPrev.setOnClickListener(v -> requireActivity().finish());
    }

    public boolean isIntentExisting(Context context, String action) {
        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent(action);
        List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (resolveInfo.size() > 0) {
            return true;
        }
        return false;
    }

    private void initViewObservable(){
        viewModel.mldImgCodeReqState.observe(getViewLifecycleOwner(), loadState -> {
            if(LoadState.Success == loadState){
                stopAnim();
                imgRegisterRefresh.setVisibility(View.GONE);
                imgRegisterImgCode.setVisibility(View.VISIBLE);
                imgRegisterImgCode.setImageBitmap(ImageUtil.stringToBitmap(viewModel.imageCaptchaResp.img));
            }else if(LoadState.Loading == loadState){
                startAnim();
                imgRegisterImgCode.setVisibility(View.GONE);
                imgRegisterRefresh.setVisibility(View.VISIBLE);
            }else{
                stopAnim();
                imgRegisterImgCode.setVisibility(View.GONE);
                imgRegisterRefresh.setVisibility(View.VISIBLE);
            }
        });
    }

    public void startAnim(){
        imgRegisterRefresh.clearAnimation();
        imgRegisterRefresh.setAnimation(animation);
        animation.start();
    }

    public void stopAnim(){
        imgRegisterRefresh.clearAnimation();
    }

    private void handleUserActivate(){
        if(viewModel.imageCaptchaResp == null){
            viewModel.reqImageCaptchaRegister();
            return;
        }

        if (!checkboxAgreement.isChecked()) {
            showToast(getString(R.string.toast_accept_treaty));
            return;
        }

        String userCode = editRegisterAccount.getText().toString();
        if(userCode.trim().isEmpty()){
            txtRegisterAccountTip.setText("账号不能为空");
            editRegisterAccount.requestFocus();
            return;
        }

        String password = editRegisterPassword.getText().toString();
        if(password.trim().isEmpty()){
            txtRegisterPasswordTip.setText("密码不能为空");
            txtRegisterPasswordTip.setTextColor(0xFFFF5B5B);
            editRegisterPassword.requestFocus();
            return;
        }
        /*String errTip = viewModel.checkPassword(password);
        if(errTip.length() > 0){
            txtRegisterPasswordTip.setText(errTip);
            txtRegisterPasswordTip.setTextColor(0xFFFF5B5B);
            editRegisterPassword.requestFocus();
            return;
        }*/

        String passwordConfirm = editRegisterPasswordConfirm.getText().toString();
        if(!password.equals(passwordConfirm)){
            txtRegisterPasswordConfirmTip.setText("两次密码输入不一致");
            txtRegisterPasswordConfirmTip.setTextColor(0xFFFF5B5B);
            editRegisterPasswordConfirm.requestFocus();
            return;
        }

        String imgCode = editRegisterImgCode.getText().toString();
        if(imgCode.trim().isEmpty()){
            showToast("验证码不能为空");
            editRegisterImgCode.requestFocus();
            return;
        }

        //viewModel.reqUserActivate(userCode, "", password, CommonUtils.getDeviceSn(), viewModel.imageCaptchaResp.uuid, imgCode);
    }

    private void showToast(String msg){
        viewModel.mldToastMsg.setValue(msg);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        MaterialCardView cardView = (v instanceof MaterialCardView) ? (MaterialCardView) v : null;
        if (cardView == null) return;
        final int strokeWidth = DisplayUtil.dip2px(v.getContext(), 1);
        if (hasFocus) {
            cardView.setStrokeColor(getResources().getColor(R.color.selectedStrokeColorBlue));
            cardView.setStrokeWidth(strokeWidth);
            CommonUtils.scaleView(v, 1.1f);
        } else {
            cardView.setStrokeColor(getResources().getColor(R.color.unselectedStrokeColor));
            cardView.setStrokeWidth(0);
            v.clearAnimation();
            CommonUtils.scaleView(v, 1f);
        }
    }
}