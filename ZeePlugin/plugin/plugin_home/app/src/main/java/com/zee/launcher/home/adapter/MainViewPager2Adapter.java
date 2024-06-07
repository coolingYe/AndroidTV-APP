package com.zee.launcher.home.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.zee.launcher.home.data.layout.PageLayoutDTO;
import com.zee.launcher.home.ui.home.HomeClassicFragment;

import java.util.List;

public class MainViewPager2Adapter extends FragmentStateAdapter {
    private final List<PageLayoutDTO> dataList;

    public MainViewPager2Adapter(List<PageLayoutDTO> dataList, @NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
        this.dataList = dataList;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return HomeClassicFragment.newInstance(String.valueOf(position), position);
    }

    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
