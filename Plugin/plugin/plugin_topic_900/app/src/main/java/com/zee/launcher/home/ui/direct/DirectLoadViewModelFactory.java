package com.zee.launcher.home.ui.direct;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.zee.launcher.home.data.DataRepository;
import com.zee.launcher.home.ui.detail.DetailViewModel;

public class DirectLoadViewModelFactory implements ViewModelProvider.Factory {

    private final DataRepository dataRepository;

    public DirectLoadViewModelFactory(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new DirectLoadViewModel(dataRepository);
    }
}
