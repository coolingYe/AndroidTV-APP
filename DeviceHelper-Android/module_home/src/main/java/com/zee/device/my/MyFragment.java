package com.zee.device.my;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.zee.device.home.databinding.FragmentCreateHelpBinding;
import com.zee.device.home.databinding.FragmentMyBinding;

public class MyFragment extends Fragment {

    private FragmentMyBinding binding;

    public static MyFragment newInstance() {
        return new MyFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = FragmentMyBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}
