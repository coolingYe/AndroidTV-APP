package com.zee.launcher.home.ui.list;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.zee.launcher.home.data.DataRepository;

public class AppListViewModelFactory implements ViewModelProvider.Factory {

    private final DataRepository dataRepository;

    public AppListViewModelFactory(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new AppListViewModel(dataRepository);
    }
}

