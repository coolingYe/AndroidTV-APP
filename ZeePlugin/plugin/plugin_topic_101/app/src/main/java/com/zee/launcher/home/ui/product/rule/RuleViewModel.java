package com.zee.launcher.home.ui.product.rule;

import androidx.lifecycle.MutableLiveData;

import com.google.gson.JsonObject;
import com.zee.launcher.home.data.DataRepository;
import com.zee.launcher.home.data.protocol.response.RankingResp;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.data.protocol.response.BaseResp;
import com.zeewain.base.model.LoadState;
import com.zeewain.base.ui.BaseViewModel;
import com.zwn.user.data.protocol.response.UserInfoResp;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class RuleViewModel extends BaseViewModel {

    public MutableLiveData<RankingResp> rankingResp = new MutableLiveData<>();
    public MutableLiveData<LoadState> mldInitDataLoadState = new MutableLiveData<>();
    public MutableLiveData<LoadState> userInfoReqState = new MutableLiveData<>();
    public String userOrganizeName;

    public void reqUserInfo() {
        userInfoReqState.setValue(LoadState.Loading);
        DataRepository.getInstance().getUserInfo()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<UserInfoResp>>() {
                    @Override
                    public void onNext(BaseResp<UserInfoResp> t) {
                        if (t.code == BaseConstants.API_HANDLE_SUCCESS) {
                            if (t.data != null) {
                                userOrganizeName = t.data.getOrganizeName();
                            }
                            userInfoReqState.setValue(LoadState.Success);
                        } else userInfoReqState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onError(Throwable e) {
                        userInfoReqState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    void getRanking() {
        mldInitDataLoadState.setValue(LoadState.Loading);
        JsonObject req = new JsonObject();
        req.addProperty("activityId", -1);
        req.addProperty("topN", 30);
        DataRepository.getInstance().getRankingInfo(req)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(RuleViewModel.this)
                .subscribe(new DisposableObserver<BaseResp<RankingResp>>() {
                    @Override
                    public void onNext(BaseResp<RankingResp> t) {
                        if (t.code == BaseConstants.API_HANDLE_SUCCESS) {
                            rankingResp.setValue(t.data);
                            mldInitDataLoadState.setValue(LoadState.Success);
                        } else mldInitDataLoadState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mldInitDataLoadState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}
