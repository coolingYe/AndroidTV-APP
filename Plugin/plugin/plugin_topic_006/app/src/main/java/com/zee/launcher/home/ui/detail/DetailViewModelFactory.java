package com.zee.launcher.home.ui.detail;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.zee.launcher.home.data.DataRepository;

public class DetailViewModelFactory implements ViewModelProvider.Factory {

    private final DataRepository dataRepository;

    public DetailViewModelFactory(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new DetailViewModel(dataRepository);
    }
}
