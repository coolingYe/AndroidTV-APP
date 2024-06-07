package com.zwn.user.data;

import com.zeewain.base.data.protocol.response.BaseResp;
import com.zeewain.base.utils.RetrofitClient;
import com.zeewain.base.utils.SPUtils;
import com.zwn.user.data.protocol.request.AkSkReq;
import com.zwn.user.data.protocol.request.DelFavoritesReq;
import com.zwn.user.data.protocol.request.DelHistoryReq;
import com.zwn.user.data.protocol.request.FavoritePagedReq;
import com.zwn.user.data.protocol.request.FavoritesReq;
import com.zwn.user.data.protocol.request.HistoryReq;
import com.zwn.user.data.protocol.request.ImageCaptchaReq;
import com.zwn.user.data.protocol.request.MsgCodeReq;
import com.zwn.user.data.protocol.request.MsgLoginReq;
import com.zwn.user.data.protocol.request.PwdLoginReq;
import com.zwn.user.data.protocol.request.ResetPasswordReq;
import com.zwn.user.data.protocol.request.UserActivateReq;
import com.zwn.user.data.protocol.request.UserPwdLoginReq;
import com.zwn.user.data.protocol.response.AboutUsInfoResp;
import com.zwn.user.data.protocol.response.AkSkResp;
import com.zwn.user.data.protocol.response.DelFavoritesResp;
import com.zwn.user.data.protocol.response.FavoritePagedResp;
import com.zwn.user.data.protocol.response.FavoriteStateResp;
import com.zwn.user.data.protocol.response.FavoritesResp;
import com.zwn.user.data.protocol.response.HistoryResp;
import com.zwn.user.data.protocol.response.ImageCaptchaResp;
import com.zwn.user.data.protocol.response.MsgCodeResp;
import com.zwn.user.data.protocol.response.LoginResp;
import com.zwn.user.data.source.http.service.UserService;

import java.util.List;

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
