package com.zee.launcher.home.widgets.banner;

import android.view.View;

public interface OnBannerClickListener<T> {
    void onBannerClick(View view, T data, int position);
}
