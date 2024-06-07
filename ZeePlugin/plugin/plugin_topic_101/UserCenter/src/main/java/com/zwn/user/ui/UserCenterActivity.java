package com.zwn.user.ui;


import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zee.manager.IZeeManager;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.ui.BaseActivity;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.DensityUtils;
import com.zwn.launcher.host.HostManager;
import com.zwn.user.R;
import com.zwn.user.adapter.UserCenterCategoryAdapter;
import com.zwn.user.data.UserRepository;
import com.zwn.user.data.model.UserCenterCategory;
import com.zwn.user.ui.user.InteractiveRecordFragment;
import com.zwn.user.ui.user.MineDownloadFragment;
import com.zwn.user.ui.user.MineFavoritesFragment;
import com.zwn.user.ui.user.UserInfoFragment;

import java.util.ArrayList;
import java.util.List;

public class UserCenterActivity extends BaseActivity implements UserCenterCategoryAdapter.OnCategorySelectedListener, View.OnFocusChangeListener, ViewTreeObserver.OnGlobalFocusChangeListener {
    private UserCenterViewModel viewModel;
    private RecyclerView recyclerViewUserCenterCategory;
    private View backView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UserCenterViewModelFactory factory = new UserCenterViewModelFactory(UserRepository.getInstance());
        viewModel = new ViewModelProvider(this, factory).get(UserCenterViewModel.class);

        bindManagerService();

        DensityUtils.autoWidth(getApplication(), this);
        setContentView(R.layout.activity_user_center);

        initData();
        initListener();
        initViewObservable();

        getWindow().getDecorView().getViewTreeObserver().addOnGlobalFocusChangeListener(this);
    }

    private void initData() {
        backView = findViewById(R.id.user_back_view);
        backView.setBackgroundResource(R.mipmap.ic_user_back);
        backView.setOnFocusChangeListener(this);

        List<UserCenterCategory> categoryList = new ArrayList<>();
        categoryList.add(new UserCenterCategory("我的信息", R.mipmap.icon_user_info));
        categoryList.add(new UserCenterCategory("历史体验", R.mipmap.icon_user_history));
        categoryList.add(new UserCenterCategory("我的下载", R.mipmap.icon_user_download));
        categoryList.add(new UserCenterCategory("我的收藏", R.mipmap.icon_user_favorite));
        UserCenterCategoryAdapter userCenterCategoryAdapter = new UserCenterCategoryAdapter(categoryList);
        userCenterCategoryAdapter.setOnCategorySelectedListener(this);
        onCategorySelected(0);
        userCenterCategoryAdapter.setOnSelectedListener(position -> {
            userCenterCategoryAdapter.setSelectedPosition(position);
            onCategorySelected(position);
        });
        userCenterCategoryAdapter.setHasStableIds(true);

        recyclerViewUserCenterCategory = findViewById(R.id.recycler_view_user_center_category);
        recyclerViewUserCenterCategory.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        recyclerViewUserCenterCategory.setFocusable(true);
        recyclerViewUserCenterCategory.setAdapter(userCenterCategoryAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerViewUserCenterCategory.setLayoutManager(linearLayoutManager);
    }

    private void initListener() {
        backView.setOnClickListener(v -> finish());
    }

    private void initViewObservable() {
        viewModel.pToast.observe(this, msg -> {
            if (msg != null && msg.length() > 0) {
                showToast(msg);
            }
        });

        viewModel.userInfo.observe(this, userInfoResp -> {
            Intent intent = new Intent();
            intent.putExtra(BaseConstants.EXTRA_USER_NAME, userInfoResp.getUserName());
            setResult(RESULT_OK, intent);
        });

        viewModel.mldDeletePackageName.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String packageName) {
                deletePackage(packageName);
            }
        });
    }

    public void onRecycleViewCallback() {
        recyclerViewUserCenterCategory.requestFocus();
    }

    @Override
    public void onCategorySelected(int position) {
        if (position == 0) {
            replaceFragment(UserInfoFragment.newInstance());
        } else if (position == 1) {
            replaceFragment(InteractiveRecordFragment.newInstance());
        } else if (position == 2) {
            replaceFragment(MineDownloadFragment.newInstance());
        } else if (position == 3) {
            replaceFragment(MineFavoritesFragment.newInstance());
        }
    }

    private void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_user_center_content, fragment).commit();
    }

    @Override
    protected void onDestroy() {
        if (zeeManager != null) {
            unbindManagerService();
            zeeManager = null;
        }
        super.onDestroy();
    }

    private IZeeManager zeeManager = null;

    public void bindManagerService() {
        Intent bindIntent = new Intent(BaseConstants.MANAGER_SERVICE_ACTION);
        bindIntent.setPackage(BaseConstants.MANAGER_PACKAGE_NAME);
        HostManager.getUseContext(this).bindService(bindIntent, managerServiceConnection, BIND_AUTO_CREATE);
    }

    public void unbindManagerService() {
        HostManager.getUseContext(this).unbindService(managerServiceConnection);
    }

    private final ServiceConnection managerServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            zeeManager = IZeeManager.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            zeeManager = null;
        }
    };

    public void deletePackage(String packageName) {
        if (zeeManager != null) {
            try {
                zeeManager.deletePackage(packageName);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            if (v.getId() == backView.getId()) {
                backView.setBackgroundResource(R.mipmap.ic_user_back_selected);
            }
        } else {
            if (v.getId() == backView.getId()) {
                backView.setBackgroundResource(R.mipmap.ic_user_back);
            }
        }
    }

    @Override
    public void onGlobalFocusChanged(View oldFocus, View newFocus) {
        Log.d("test", "onGlobalFocusChanged newFocus: " + newFocus);
        Log.d("test", "onGlobalFocusChanged oldFocus: " + oldFocus);
    }

    @Override
    protected void onResume() {
        super.onResume();
        recyclerViewUserCenterCategory.requestFocus();
    }
}