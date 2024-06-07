package com.zwn.user.ui;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.zwn.user.data.UserRepository;

public class UserCenterViewModelFactory implements ViewModelProvider.Factory {

    private final UserRepository mUserRepository;

    public UserCenterViewModelFactory(UserRepository userRepository) {
        mUserRepository = userRepository;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new UserCenterViewModel(mUserRepository);
    }
}
