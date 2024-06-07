package com.zwn.launcher.ui.upgrade;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.zwn.launcher.data.DataRepository;

public class UpgradeTipViewModelFactory implements ViewModelProvider.Factory {

    private final DataRepository dataRepository;

    public UpgradeTipViewModelFactory(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new UpgradeTipViewModel(dataRepository);
    }
}
