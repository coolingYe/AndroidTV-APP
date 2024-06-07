package com.zee.setting.base;

import androidx.lifecycle.ViewModel;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class BaseViewModel extends ViewModel implements Consumer<Disposable> {

    private CompositeDisposable mCompositeDisposable;

    public BaseViewModel(){
        mCompositeDisposable = new CompositeDisposable();
    }

    @Override
    public void accept(Disposable disposable) throws Exception {
        addSubscribe(disposable);
    }

    protected void addSubscribe(Disposable disposable) {
        if (mCompositeDisposable == null) {
            mCompositeDisposable = new CompositeDisposable();
        }
        mCompositeDisposable.add(disposable);
    }

    @Override
    protected void onCleared() {
        if (mCompositeDisposable != null) {
            mCompositeDisposable.clear();
        }
        super.onCleared();
    }
}
