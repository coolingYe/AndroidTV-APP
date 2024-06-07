package com.zee.device.base.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.zee.device.base.utils.DensityUtils;

public abstract class BaseFragment extends Fragment {

    private View mRootView;

    protected abstract int getLayoutId();

    protected abstract void initView(View view);

    protected abstract void initListener();

    protected abstract void initData();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (mRootView != null) return mRootView;
        DensityUtils.autoWidth(requireActivity().getApplication(), requireActivity());
        mRootView = inflater.inflate(getLayoutId(), container, false);
        return mRootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initListener();
        initData();
    }
}
