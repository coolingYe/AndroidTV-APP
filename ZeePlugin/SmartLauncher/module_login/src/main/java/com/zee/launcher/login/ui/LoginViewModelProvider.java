package com.zee.launcher.login.ui;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.zee.launcher.login.data.UserRepository;


public class LoginViewModelProvider implements ViewModelProvider.Factory {

    private final UserRepository mUserRepository;

    public LoginViewModelProvider(UserRepository userRepository) {
        mUserRepository = userRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new LoginViewModel(mUserRepository);
    }
}
