package com.zee.guide.ui;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.zee.guide.data.GuideRepository;

public class GuideViewModelFactory implements ViewModelProvider.Factory{
    private final GuideRepository guideRepository;

    public GuideViewModelFactory(GuideRepository guideRepository) {
        this.guideRepository = guideRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new GuideViewModel(guideRepository);
    }
}
