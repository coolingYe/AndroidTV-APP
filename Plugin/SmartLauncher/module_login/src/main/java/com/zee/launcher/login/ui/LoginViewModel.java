package com.zee.launcher.login.ui;

import android.os.CountDownTimer;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.zee.launcher.login.data.UserRepository;
import com.zee.launcher.login.data.protocol.request.AkSkReq;
import com.zee.launcher.login.data.protocol.request.ImageCaptchaReq;
import com.zee.launcher.login.data.protocol.request.MsgCodeReq;
import com.zee.launcher.login.data.protocol.request.UserActivateReq;
import com.zee.launcher.login.data.protocol.request.UserPwdLoginReq;
import com.zee.launcher.login.data.protocol.response.AkSkResp;
import com.zee.launcher.login.data.protocol.response.ImageCaptchaResp;
import com.zee.launcher.login.data.protocol.response.LoginResp;
import com.zee.launcher.login.data.protocol.response.MsgCodeResp;
import com.zee.launcher.login.data.protocol.response.UserInfoResp;
import com.zee.launcher.login.utils.AndroidHelper;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.config.SharePrefer;
import com.zeewain.base.data.protocol.response.BaseResp;
import com.zeewain.base.data.protocol.response.DeviceInfoResp;
import com.zeewain.base.model.DataLoadState;
import com.zeewain.base.model.LoadState;
import com.zeewain.base.ui.BaseViewModel;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.SPUtils;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class LoginViewModel extends BaseViewModel {

    private final UserRepository mUserRepository;
    public MutableLiveData<String> mldToastMsg = new MutableLiveData<>();
    public MutableLiveData<LoadState> mldUserBindingOrLoginState = new MutableLiveData<>();
    public MutableLiveData<DataLoadState<Integer>> mldDeviceInfoLoadState = new MutableLiveData<>();
    public MutableLiveData<LoadState> mldReqSmsCodeLoadState = new MutableLiveData<>();

    public MutableLiveData<Integer> mldCountDownCount = new MutableLiveData<>();

    public MutableLiveData<LoadState> mldImgCodeReqState = new MutableLiveData<>();
    public MutableLiveData<LoadState> mldImgCodeForSmsCodeState = new MutableLiveData<>();

    public MutableLiveData<List<UserInfoResp>> mldUserOptionInfoListSelect = new MutableLiveData<>();
    public MutableLiveData<Boolean> mldUserSelectBack = new MutableLiveData<>();

    public MutableLiveData<LoadState> mldInitUserDataState = new MutableLiveData<>();

    public MsgCodeResp smsMsgCodeResp;
    public ImageCaptchaResp imageCaptchaResp;
    public ImageCaptchaResp imageCaptchaRespForSmsCode;

    public UserPwdLoginReq userPwdLoginReq;
    public UserActivateReq userRegisterBindingReq;
    public UserActivateReq userLoginBindingReq;
    public boolean isDeviceActivated = false;

    public LoginViewModel(UserRepository userRepository) {
        mUserRepository = userRepository;
    }

    public final CountDownTimer mCountDownTimer = new CountDownTimer(60 * 1000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            int counter = (int) (millisUntilFinished / 1000);
            mldCountDownCount.setValue(counter);
        }

        @Override
        public void onFinish() {
            mldCountDownCount.setValue(0);
        }
    };

    public String checkMobilePhone(String phoneNumber) {
        if (phoneNumber.trim().isEmpty()) return "手机号不能为空";
        if (phoneNumber.length() < 11) return "手机号位数错误";
        if (!AndroidHelper.isPhone(phoneNumber)) return "请输入正确的手机号";
        return "";
    }

    public String checkPassword(String password) {
        if (password.trim().isEmpty()) return "密码不能为空";
        if (password.length() < 8) return "密码不能小于8位";
        if (password.length() > 16) return "密码不能小于16位";
        if (!AndroidHelper.isCompliantPassword(password)) return "请输入符合要求的密码";
        return "";
    }

    public String checkConfirmPassword(String password, String confirmPassword) {
        if (!confirmPassword.equals(password)) return "两次密码输入不一致";
        return checkPassword(confirmPassword);
    }

    public String checkCaptcha(String captcha) {
        if (captcha.trim().isEmpty()) return "验证码不能为空";
        if (!AndroidHelper.isNumeric(captcha)) return "请输入正确的验证码";
        return "";
    }

    private void saveLoginToken(String token) {
        mUserRepository.putValue(SharePrefer.userToken, token);
        SPUtils.getInstance().put(SharePrefer.GuideDone, true);
    }

    public void reqUserLoginBinding(UserActivateReq userActivateReq) {
        mldUserBindingOrLoginState.setValue(LoadState.Loading);
        userLoginBindingReq = userActivateReq;
        mUserRepository.userLoginBindingReq(userActivateReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<LoginResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<LoginResp> resp) {
                        if (resp.code == BaseConstants.API_HANDLE_SUCCESS) {
                            saveLoginToken(resp.data.token);
                            mldUserBindingOrLoginState.setValue(LoadState.Success);
                        } else if (resp.code == 1105) {
                            userRegisterBindingReq = null;
                            mldUserOptionInfoListSelect.setValue(resp.data.userOptionList);
                        } else {
                            mldUserBindingOrLoginState.setValue(LoadState.Failed);
                            mldToastMsg.setValue(resp.message);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mldUserBindingOrLoginState.setValue(LoadState.Failed);
                        mldToastMsg.setValue("网络异常，请检查网络状态");
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void reqUserRegisterBinding(UserActivateReq userActivateReq) {
        mldUserBindingOrLoginState.setValue(LoadState.Loading);
        userRegisterBindingReq = userActivateReq;
        mUserRepository.userRegisterBindingReq(userActivateReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<LoginResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<LoginResp> resp) {
                        if (resp.code == BaseConstants.API_HANDLE_SUCCESS) {
                            saveLoginToken(resp.data.token);
                            mldUserBindingOrLoginState.setValue(LoadState.Success);
                        } else if (resp.code == 1105) {
                            userLoginBindingReq = null;
                            mldUserOptionInfoListSelect.setValue(resp.data.userOptionList);
                        } else {
                            mldUserBindingOrLoginState.setValue(LoadState.Failed);
                            mldToastMsg.setValue(resp.message);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mldUserBindingOrLoginState.setValue(LoadState.Failed);
                        mldToastMsg.setValue("网络异常，请检查网络状态");
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void reqUserPwdLogin(String userCode, String password, String deviceSn, String uuid, String code, String type) {
        userPwdLoginReq = new UserPwdLoginReq(userCode, password, deviceSn, uuid, code, type);
        reqUserPwdLogin(userPwdLoginReq);
    }

    public void reqUserPwdLogin(UserPwdLoginReq userPwdLoginReq) {
        mldUserBindingOrLoginState.setValue(LoadState.Loading);
        mUserRepository.userPwdLoginReq(userPwdLoginReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<LoginResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<LoginResp> resp) {
                        if (resp.code == BaseConstants.API_HANDLE_SUCCESS) {
                            saveLoginToken(resp.data.token);
                            mldUserBindingOrLoginState.setValue(LoadState.Success);
                        } else if (resp.code == 1105) {
                            mldUserOptionInfoListSelect.setValue(resp.data.userOptionList);
                        } else {
                            mldUserBindingOrLoginState.setValue(LoadState.Failed);
                            mldToastMsg.setValue(resp.message);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mldUserBindingOrLoginState.setValue(LoadState.Failed);
                        mldToastMsg.setValue("登录失败，请检查网络状态");
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void reqUserInfo() {
        mldInitUserDataState.setValue(LoadState.Loading);
        mUserRepository.getUserInfo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<UserInfoResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<UserInfoResp> resp) {
                        if (resp.code == BaseConstants.API_HANDLE_SUCCESS) {
                            UserInfoResp userInfoResp = resp.data;
                            if (userInfoResp != null && userInfoResp.userCode != null && !userInfoResp.userCode.isEmpty()) {
                                mUserRepository.putValue(SharePrefer.userAccount, userInfoResp.userCode);
                                reqAkSkInfo();
                            } else {
                                mldToastMsg.setValue("获取用户信息接口返回数据异常！");
                                mldInitUserDataState.setValue(LoadState.Failed);
                            }
                        } else {
                            mldToastMsg.setValue(resp.message);
                            mldInitUserDataState.setValue(LoadState.Failed);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mldToastMsg.setValue("网络异常，请检查网络状态");
                        mldInitUserDataState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void reqAkSkInfo() {
        mldInitUserDataState.setValue(LoadState.Loading);
        mUserRepository.getAkSkInfo(new AkSkReq(BaseConstants.AUTH_SYSTEM_CODE))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<AkSkResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<AkSkResp> resp) {
                        if (resp.code == BaseConstants.API_HANDLE_SUCCESS) {
                            AkSkResp akSkResp = resp.data;
                            if (akSkResp != null && akSkResp.akCode != null && !akSkResp.akCode.isEmpty()) {
                                Gson gson = new Gson();
                                String akSkString = gson.toJson(akSkResp);
                                mUserRepository.putValue(SharePrefer.akSkInfo, akSkString);
                                mldInitUserDataState.setValue(LoadState.Success);
                            } else {
                                mldToastMsg.setValue("获取AK码接口返回数据异常！");
                                mldInitUserDataState.setValue(LoadState.Failed);
                            }
                        } else {
                            mldToastMsg.setValue(resp.message);
                            mldInitUserDataState.setValue(LoadState.Failed);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mldToastMsg.setValue("网络异常，请检查网络状态");
                        mldInitUserDataState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


    public void reqDeviceInfo() {
        mldDeviceInfoLoadState.setValue(new DataLoadState<>(LoadState.Loading));
        mUserRepository.getDeviceInfo(CommonUtils.getDeviceSn())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<DeviceInfoResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<DeviceInfoResp> resp) {
                        if (resp.code == BaseConstants.API_HANDLE_SUCCESS) {
                            if (resp.data != null)
                                mldDeviceInfoLoadState.setValue(new DataLoadState<>(LoadState.Success, resp.data.activateStatus));
                            else
                                mldDeviceInfoLoadState.setValue(new DataLoadState<>(LoadState.Success, BaseConstants.DeviceStatus.UNACTIVATED));
                        } else {
                            mldDeviceInfoLoadState.setValue(new DataLoadState<>(LoadState.Failed));
                            mldToastMsg.setValue(resp.message);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mldToastMsg.setValue("网络异常，请检查网络设置！");
                        mldDeviceInfoLoadState.setValue(new DataLoadState<>(LoadState.Failed));
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void reqPhoneSmsCode(String type, String phoneNum, String uuid, String imageCode) {//type 0-用户登录 1-用户注册 2-找回密码 3-更换手机号
        mldReqSmsCodeLoadState.setValue(LoadState.Loading);
        mUserRepository.getMsgCode(new MsgCodeReq(type, phoneNum, uuid, imageCode))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<MsgCodeResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<MsgCodeResp> resp) {
                        if (resp.code == BaseConstants.API_HANDLE_SUCCESS) {
                            if (resp.data != null && resp.data.uuid != null) {
                                smsMsgCodeResp = resp.data;
                                mldReqSmsCodeLoadState.setValue(LoadState.Success);
                            } else {
                                mldReqSmsCodeLoadState.setValue(LoadState.Failed);
                                mldToastMsg.setValue("短信验证码接口返回数据异常！");
                            }
                        } else {
                            mldReqSmsCodeLoadState.setValue(LoadState.Failed);
                            mldToastMsg.setValue(resp.message);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mldToastMsg.setValue("网络异常，请检查网络设置！");
                        mldReqSmsCodeLoadState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void reqImageCaptchaLogin() {
        ImageCaptchaReq imageCaptchaReq = new ImageCaptchaReq("1");
        reqImageCaptcha(imageCaptchaReq);
    }

    public void reqImageCaptchaRegister() {
        List<String> typeList = new ArrayList<>();
        typeList.add("1");
        typeList.add("3");
        ImageCaptchaReq imageCaptchaReq = new ImageCaptchaReq(typeList);
        reqImageCaptcha(imageCaptchaReq);
    }

    public void reqImageCaptcha(ImageCaptchaReq imageCaptchaReq) {
        mldImgCodeReqState.setValue(LoadState.Loading);
        imageCaptchaResp = null;
        mUserRepository.imageCaptchaReq(imageCaptchaReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<ImageCaptchaResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<ImageCaptchaResp> resp) {
                        if (resp.code == BaseConstants.API_HANDLE_SUCCESS) {
                            imageCaptchaResp = resp.data;
                            mldImgCodeReqState.setValue(LoadState.Success);
                        } else {
                            mldImgCodeReqState.setValue(LoadState.Failed);
                            mldToastMsg.setValue(resp.message);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mldImgCodeReqState.setValue(LoadState.Failed);
                        mldToastMsg.setValue("网络异常，请检查网络状态！");
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void reqImageCaptchaForSmsCode() {
        mldImgCodeForSmsCodeState.setValue(LoadState.Loading);
        mUserRepository.imageCaptchaReq(new ImageCaptchaReq("0"))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<ImageCaptchaResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<ImageCaptchaResp> resp) {
                        if (resp.code == BaseConstants.API_HANDLE_SUCCESS) {
                            imageCaptchaRespForSmsCode = resp.data;
                            mldImgCodeForSmsCodeState.setValue(LoadState.Success);
                        } else {
                            mldImgCodeForSmsCodeState.setValue(LoadState.Failed);
                            mldToastMsg.setValue(resp.message);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mldImgCodeForSmsCodeState.setValue(LoadState.Failed);
                        mldToastMsg.setValue("网络异常，请检查网络状态！");
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    /**
     * 短信激活
     * 1. 图片：短信发送类型
     * 2. 短信：登录类型
     *
     * 账号激活
     * 图片：登录类型、注册类型
     */
}