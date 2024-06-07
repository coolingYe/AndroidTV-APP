package com.zwn.user.ui.login;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.zwn.user.data.UserRepository;

public class QrcodeLoginViewModelFactory implements ViewModelProvider.Factory {

    private final UserRepository mUserRepository;

    public QrcodeLoginViewModelFactory(UserRepository userRepository) {
        mUserRepository = userRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new QrcodeLoginViewModel(mUserRepository);
    }
}
