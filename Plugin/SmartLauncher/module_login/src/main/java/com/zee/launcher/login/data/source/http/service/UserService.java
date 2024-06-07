package com.zee.launcher.login.data.source.http.service;

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
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.data.protocol.response.BaseResp;
import com.zeewain.base.data.protocol.response.DeviceInfoResp;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface UserService {

    @POST(BaseConstants.basePath + BaseConstants.ApiPath.CLIENT_GET_AK_SK)
    Observable<BaseResp<AkSkResp>> getAkSkInfo(@Body AkSkReq akSkReq);

    @GET(BaseConstants.basePath + BaseConstants.ApiPath.CLIENT_GET_USER_INFO)
    Observable<BaseResp<UserInfoResp>> getUserInfo();

    @POST(BaseConstants.basePath + BaseConstants.ApiPath.DEVICE_LOGIN_BINDING)
    Observable<BaseResp<LoginResp>> userLoginBindingReq(@Body UserActivateReq userActivateReq);

    @POST(BaseConstants.basePath + BaseConstants.ApiPath.DEVICE_REGISTER_BINDING)
    Observable<BaseResp<LoginResp>> userRegisterBindingReq(@Body UserActivateReq userActivateReq);

    @POST(BaseConstants.basePath + BaseConstants.ApiPath.DEVICE_ACTIVATE)
    Observable<BaseResp<LoginResp>> userActivateReq(@Body UserActivateReq userActivateReq);

    @POST(BaseConstants.basePath + BaseConstants.ApiPath.SSO_DO_LOGIN)
    Observable<BaseResp<LoginResp>> userPwdLoginReq(@Body UserPwdLoginReq userPwdLoginReq);

    @POST(BaseConstants.basePath + BaseConstants.ApiPath.CAPTCHA_IMAGE)
    Observable<BaseResp<ImageCaptchaResp>> imageCaptchaReq(@Body ImageCaptchaReq imageCaptchaReq);

    @GET(BaseConstants.basePath + BaseConstants.ApiPath.PURCHASE_DEVICE_INFO)
    Observable<BaseResp<DeviceInfoResp>> getDeviceInfo(@Query("deviceSn") String deviceSn);

    @POST(BaseConstants.basePath + BaseConstants.ApiPath.CAPTCHA_SMS_CODE)
    Observable<BaseResp<MsgCodeResp>> getMsgCode(@Body MsgCodeReq msgCodeReq);
}
