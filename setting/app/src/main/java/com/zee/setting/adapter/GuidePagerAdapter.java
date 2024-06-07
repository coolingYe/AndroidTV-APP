package com.zee.setting.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.zee.setting.fragment.FragmentGuide;
import com.zee.setting.fragment.FragmentGuide2;

public class GuidePagerAdapter extends FragmentStateAdapter {

    public GuidePagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 2 || position == 3) {
            return FragmentGuide2.newInstance(position);
        }
        return FragmentGuide.newInstance(position);
    }

    @Override
    public int getItemCount() {
        return 4;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
