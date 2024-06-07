package com.zwn.user.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.zeewain.base.data.protocol.response.BaseResp;
import com.zeewain.base.utils.RetrofitClient;
import com.zeewain.base.utils.SPUtils;
import com.zwn.user.data.protocol.request.AkSkReq;
import com.zwn.user.data.protocol.request.DelFavoritesReq;
import com.zwn.user.data.protocol.request.DelHistoryReq;
import com.zwn.user.data.protocol.request.DeviceSNReq;
import com.zwn.user.data.protocol.request.FavoritePagedReq;
import com.zwn.user.data.protocol.request.ImageCaptchaReq;
import com.zwn.user.data.protocol.request.MsgCodeReq;
import com.zwn.user.data.protocol.request.MsgLoginReq;
import com.zwn.user.data.protocol.request.PwdLoginReq;
import com.zwn.user.data.protocol.request.ResetPasswordReq;
import com.zwn.user.data.protocol.request.SessionResultReq;
import com.zwn.user.data.protocol.request.UserActivateReq;
import com.zwn.user.data.protocol.request.UserPwdLoginReq;
import com.zwn.user.data.protocol.response.AboutUsInfoResp;
import com.zwn.user.data.protocol.response.AkSkResp;
import com.zwn.user.data.protocol.response.CreateSessionForUserInfoResp;
import com.zwn.user.data.protocol.response.CreateSessionResp;
import com.zwn.user.data.protocol.response.DelFavoritesResp;
import com.zwn.user.data.protocol.response.FavoritePagedResp;
import com.zwn.user.data.protocol.response.FavoriteStateResp;
import com.zwn.user.data.protocol.response.HistoryResp;
import com.zwn.user.data.protocol.response.ImageCaptchaResp;
import com.zwn.user.data.protocol.response.MsgCodeResp;
import com.zwn.user.data.protocol.response.LoginResp;
import com.zwn.user.data.protocol.response.UserInfoResp;
import com.zwn.user.data.source.http.service.UserService;

import io.reactivex.Observable;

public class UserRepository implements UserService{

    private static volatile UserRepository instance;
    private final UserService userService;

    private UserRepository(UserService userService){
        this.userService = userService;
    }

    public static UserRepository getInstance(){
        if(instance == null){
            synchronized (UserRepository.class) {
                if (instance == null){
                    instance = new UserRepository(RetrofitClient.getInstance().create(UserService.class));
                }
            }
        }
        return instance;
    }

    @Override
    public Observable<BaseResp<UserInfoResp>> getUserInfo() {
        return userService.getUserInfo();
    }

    public Observable<BaseResp<AkSkResp>> getAkSkInfo(AkSkReq akSkReq){
        return userService.getAkSkInfo(akSkReq);
    }

    public Observable<BaseResp<MsgCodeResp>> getMsgCode(MsgCodeReq msgCodeReq) {
        return userService.getMsgCode(msgCodeReq);
    }

    public Observable<BaseResp<LoginResp>> msgLogin(MsgLoginReq msgLoginReq) {
        return userService.msgLogin(msgLoginReq);
    }

    public Observable<BaseResp<LoginResp>> pwdLogin(PwdLoginReq pwdLoginReq) {
        return userService.pwdLogin(pwdLoginReq);
    }

    public Observable<BaseResp<FavoritePagedResp>> getUserFavorites(FavoritePagedReq favoritePagedReq) {
        return userService.getUserFavorites(favoritePagedReq);
    }

    public Observable<BaseResp<DelFavoritesResp>> delFavorites(DelFavoritesReq delFavoritesReq) {
        return userService.delFavorites(delFavoritesReq);
    }

    public Observable<BaseResp<HistoryResp>> getUserHistory() {
        return userService.getUserHistory();
    }

    public Observable<BaseResp<String>> delUserHistory(DelHistoryReq delHistoryReq) {
        return userService.delUserHistory(delHistoryReq);
    }

    public Observable<BaseResp<String>> clearUserHistory() {
        return userService.clearUserHistory();
    }

    public Observable<BaseResp<AboutUsInfoResp>> getAboutUsInfo() {
        return userService.getAboutUsInfo();
    }

    @Override
    public Observable<BaseResp<String>> resetPassword(ResetPasswordReq resetPasswordReq) {
        return userService.resetPassword(resetPasswordReq);
    }

    public Observable<BaseResp<FavoriteStateResp>> getFavoriteState(String objId) {
        return userService.getFavoriteState(objId);
    }

    @Override
    public Observable<BaseResp<String>> userActivateReq(UserActivateReq userActivateReq) {
        return userService.userActivateReq(userActivateReq);
    }

    @Override
    public Observable<BaseResp<LoginResp>> userPwdLoginReq(UserPwdLoginReq userPwdLoginReq) {
        return userService.userPwdLoginReq(userPwdLoginReq);
    }

    @Override
    public Observable<BaseResp<ImageCaptchaResp>> imageCaptchaReq(ImageCaptchaReq imageCaptchaReq) {
        return userService.imageCaptchaReq(imageCaptchaReq);
    }

    @Override
    public Observable<BaseResp<CreateSessionResp>> createSession(DeviceSNReq deviceSNReq) {
        return userService.createSession(deviceSNReq);
    }

    @Override
    public Observable<BaseResp<LoginResp>> getSessionResult(SessionResultReq sessionIdReq) {
        return userService.getSessionResult(sessionIdReq);
    }

    @Override
    public Observable<BaseResp<CreateSessionForUserInfoResp>> createSessionForUserInfo() {
        return userService.createSessionForUserInfo();
    }

    @Override
    public Observable<BaseResp<JsonElement>> getSessionResultForUserInfo(JsonObject jsonObject) {
        return userService.getSessionResultForUserInfo(jsonObject);
    }

    public void putValue(String key, String value) {
        SPUtils.getInstance().put(key, value);
    }

    public String getString(String key) {
        return SPUtils.getInstance().getString(key);
    }

    public void removeValue(String key) {
        SPUtils.getInstance().remove(key);
    }
}
