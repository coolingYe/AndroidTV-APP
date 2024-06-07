package com.zee.launcher.home.ui.product.rule;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.lifecycle.ViewModelProvider;

import com.zee.launcher.home.R;
import com.zeewain.base.ui.BaseActivity;
import com.zeewain.base.utils.DensityUtils;

public class RuleActivity extends BaseActivity implements ViewTreeObserver.OnGlobalFocusChangeListener {

    private RuleViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DensityUtils.autoWidth(getApplication(), this);
        setContentView(R.layout.activity_rule);
        mViewModel = new ViewModelProvider(this).get(RuleViewModel.class);
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_rule, new RuleFragment(),"").commit();
        getWindow().getDecorView().getViewTreeObserver().addOnGlobalFocusChangeListener(this);
    }

    @Override
    public void onGlobalFocusChanged(View oldFocus, View newFocus) {
        Log.d("test", "onGlobalFocusChanged newFocus: " + newFocus);
        Log.d("test", "onGlobalFocusChanged oldFocus: " + oldFocus);
    }
}
