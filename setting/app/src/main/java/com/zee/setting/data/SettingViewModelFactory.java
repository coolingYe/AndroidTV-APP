package com.zee.setting.data;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;



public class SettingViewModelFactory implements ViewModelProvider.Factory {

    private final SettingRepository dataRepository;

    public SettingViewModelFactory(SettingRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new SettingViewModel(dataRepository);
    }
}
