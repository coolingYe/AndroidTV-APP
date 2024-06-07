package com.zee.launcher.home.ui.loading;

import androidx.annotation.NonNull;

import com.zee.launcher.home.data.DataRepository;
import com.zee.launcher.home.data.protocol.request.CoursewareStartReq;
import com.zeewain.base.data.protocol.response.BaseResp;
import com.zeewain.base.ui.BaseViewModel;


import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class LoadingViewModel extends BaseViewModel {

    private final DataRepository dataRepository;

    public LoadingViewModel(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    public void reqStartCourseware(String skuId, String skuName, String skuUrl){
        CoursewareStartReq coursewareStartReq = new CoursewareStartReq(skuId, skuName, skuUrl);
        dataRepository.startCourseware(coursewareStartReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<String>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<String> response) {
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }


}
