package com.zee.device.home.ui.home;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.zee.device.home.data.DataRepository;


public class HomeViewModelFactory implements ViewModelProvider.Factory {

    private final DataRepository dataRepository;

    public HomeViewModelFactory(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new HomeViewModel(dataRepository);
    }
}
