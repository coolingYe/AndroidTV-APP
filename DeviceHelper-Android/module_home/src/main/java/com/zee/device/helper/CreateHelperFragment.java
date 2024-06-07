package com.zee.device.helper;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.zee.device.home.databinding.FragmentCourseBinding;
import com.zee.device.home.databinding.FragmentCreateHelpBinding;

public class CreateHelperFragment extends Fragment {

    private FragmentCreateHelpBinding binding;

    public static CreateHelperFragment newInstance() {
        return new CreateHelperFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = FragmentCreateHelpBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}
