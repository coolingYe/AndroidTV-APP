package com.zee.device.course;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.zee.device.home.databinding.FragmentCourseBinding;
import com.zee.device.home.ui.home.HomeFragment;

public class CourseFragment extends Fragment {

    private FragmentCourseBinding binding;

    public static CourseFragment newInstance() {
        return new CourseFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = FragmentCourseBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}
