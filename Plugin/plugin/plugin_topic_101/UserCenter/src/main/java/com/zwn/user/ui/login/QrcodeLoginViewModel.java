package com.zwn.user.ui.login;



import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.data.protocol.response.BaseResp;
import com.zeewain.base.model.DataLoadState;
import com.zeewain.base.model.LoadState;
import com.zeewain.base.ui.BaseViewModel;
import com.zeewain.base.utils.CommonUtils;
import com.zwn.user.data.UserRepository;
import com.zwn.user.data.model.SessionInfoMo;
import com.zwn.user.data.model.SessionResultLoadState;
import com.zwn.user.data.protocol.request.DeviceSNReq;
import com.zwn.user.data.protocol.request.SessionResultReq;
import com.zwn.user.data.protocol.response.CreateSessionResp;
import com.zwn.user.data.protocol.response.LoginResp;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class QrcodeLoginViewModel extends BaseViewModel {
    private final UserRepository mUserRepository;
    public MutableLiveData<String> mldToastMsg = new MutableLiveData<>();
    public MutableLiveData<DataLoadState<CreateSessionResp>> mldCreateLoginSessionLoadState = new MutableLiveData<>();
    public MutableLiveData<SessionResultLoadState> mldReqSessionResultLoadState = new MutableLiveData<>();
    public final ConcurrentHashMap<String, Long> reqSessionResultMap = new ConcurrentHashMap<>();

    public QrcodeLoginViewModel(UserRepository userRepository) {
        mUserRepository = userRepository;
    }

    public synchronized void addReqSessionResult(String session, long createTime){
        if(reqSessionResultMap.size() < 5){
            reqSessionResultMap.put(session, createTime);
        }else{
            String earliestSession = null;
            long earliestTime = System.currentTimeMillis();
            for (Map.Entry<String, Long> entry: reqSessionResultMap.entrySet()) {
                if(entry.getValue() < earliestTime){
                    earliestTime = entry.getValue();
                    earliestSession = entry.getKey();
                }
            }

            if(earliestSession != null){
                reqSessionResultMap.remove(earliestSession);
            }

            reqSessionResultMap.put(session, createTime);
        }
    }

    public void createLoginSession() {
        mldCreateLoginSessionLoadState.setValue(new DataLoadState<>(LoadState.Loading));
        mUserRepository.createSession(new DeviceSNReq(CommonUtils.getDeviceSn()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(QrcodeLoginViewModel.this)
                .subscribe(new DisposableObserver<BaseResp<CreateSessionResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<CreateSessionResp> response) {
                        if(BaseConstants.API_HANDLE_SUCCESS == response.code){
                            if(response.data != null && response.data.sessionId != null && !response.data.sessionId.isEmpty()){
                                mldCreateLoginSessionLoadState.setValue(new DataLoadState<>(LoadState.Success, response.data));
                            }else{
                                mldToastMsg.setValue("会话创建失败！");
                                mldCreateLoginSessionLoadState.setValue(new DataLoadState<>(LoadState.Failed));
                            }
                        }else{
                            mldToastMsg.setValue(response.message);
                            mldCreateLoginSessionLoadState.setValue(new DataLoadState<>(LoadState.Failed));
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mldCreateLoginSessionLoadState.setValue(new DataLoadState<>(LoadState.Failed));
                    }

                    @Override
                    public void onComplete() {}
                });
    }

    public void reqSessionResult(final SessionInfoMo sessionInfoMo) {
        mldReqSessionResultLoadState.setValue(new SessionResultLoadState(LoadState.Loading, sessionInfoMo));
        mUserRepository.getSessionResult(new SessionResultReq(sessionInfoMo.sessionId, sessionInfoMo.sessionSecretKey))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(QrcodeLoginViewModel.this)
                .subscribe(new DisposableObserver<BaseResp<LoginResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<LoginResp> response) {
                        if(BaseConstants.API_HANDLE_SUCCESS == response.code){
                            if(response.data != null && response.data.token != null && !response.data.token.isEmpty()){
                                mldReqSessionResultLoadState.setValue(new SessionResultLoadState(LoadState.Success, sessionInfoMo, response.data.token));
                            }else{
                                mldToastMsg.setValue("会话结果返回异常！");
                                mldReqSessionResultLoadState.setValue(new SessionResultLoadState(LoadState.Failed, sessionInfoMo));
                            }
                        }else if(10011 == response.code || 10012 == response.code){
                            mldReqSessionResultLoadState.setValue(new SessionResultLoadState(LoadState.Failed, sessionInfoMo, response.code));
                        }else{
                            mldToastMsg.setValue(response.message);
                            mldReqSessionResultLoadState.setValue(new SessionResultLoadState(LoadState.Failed, sessionInfoMo));
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mldReqSessionResultLoadState.setValue(new SessionResultLoadState(LoadState.Failed, sessionInfoMo));
                    }

                    @Override
                    public void onComplete() {}
                });
    }

}