package com.zwn.user.ui;

import static com.zeewain.base.utils.CommonUtils.getThemeName;

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
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.zee.manager.IZeeManager;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.config.SharePrefer;
import com.zeewain.base.ui.BaseActivity;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.DensityUtils;
import com.zeewain.base.utils.DisplayUtil;
import com.zeewain.base.utils.FontUtils;
import com.zeewain.base.utils.GlideApp;
import com.zeewain.base.utils.SPUtils;
import com.zeewain.base.widgets.GradientBorderLayout;
import com.zwn.launcher.host.HostManager;
import com.zwn.user.R;
import com.zwn.user.adapter.UserCenterCategoryAdapter;
import com.zwn.user.data.UserRepository;
import com.zwn.user.data.model.UserCenterCategory;
import com.zwn.user.ui.user.BaseUserCenterFragment;
import com.zwn.user.ui.user.InteractiveRecordFragment;
import com.zwn.user.ui.user.MineDownloadFragment;
import com.zwn.user.ui.user.MineFavoritesFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class UserCenterActivity extends BaseActivity implements UserCenterCategoryAdapter.OnCategorySelectedListener, View.OnFocusChangeListener, ViewTreeObserver.OnGlobalFocusChangeListener {
    private UserCenterViewModel viewModel;
    private RecyclerView recyclerViewUserCenterCategory;
    private BaseUserCenterFragment currentFragment;

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        savedInstanceState.setClassLoader(getClass().getClassLoader());
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            savedInstanceState.setClassLoader(getClass().getClassLoader());
            Bundle bundle = savedInstanceState.getBundle("androidx.lifecycle.BundlableSavedStateRegistry.key");
            if (bundle != null) {
                Set<String> keySet = bundle.keySet();
                if (keySet != null) {
                    for (String key : keySet) {
                        Object object = bundle.get(key);
                        if (object instanceof Bundle) {
                            ((Bundle) object).setClassLoader(getClass().getClassLoader());
                        }
                    }
                }
            }
        }
        super.onCreate(savedInstanceState);
        UserCenterViewModelFactory factory = new UserCenterViewModelFactory(UserRepository.getInstance());
        viewModel = new ViewModelProvider(this, factory).get(UserCenterViewModel.class);

        bindManagerService();

        DensityUtils.autoWidth(getApplication(), this);
        setContentView(R.layout.activity_user_center);

        topBarView = findViewById(R.id.top_bar_view);
        topBarView.setBackEnable(true);
        topBarView.disappearImgUser();
        topBarView.setBackBtnNextFocusDownId(R.id.layout_user_center_log);
        topBarView.setSettingsBtnNextFocusDownId(R.id.tv_user_comm_page_del_all);

        TextView textView = findViewById(R.id.txt_user_center_log);
        textView.setTypeface(FontUtils.typefaceMedium);

        initListener();
        initData();
        initViewObservable();

        getWindow().getDecorView().getViewTreeObserver().addOnGlobalFocusChangeListener(this);
    }

    private void initData() {
        String logoUrl = CommonUtils.getLogoUrl();
        if (!logoUrl.isEmpty()) {
            ImageView ivLogo = findViewById(R.id.img_user_center_head);
            GlideApp.with(ivLogo)
                    .load(logoUrl)
                    .transform(new RoundedCorners(DisplayUtil.dip2px(ivLogo.getContext(), 24)))
                    .into(ivLogo);
        }

        List<UserCenterCategory> categoryList = new ArrayList<>();
        categoryList.add(new UserCenterCategory("", R.mipmap.uc_history, R.mipmap.uc_history_focused));
        categoryList.add(new UserCenterCategory("", R.mipmap.uc_download, R.mipmap.uc_download_focused));
        categoryList.add(new UserCenterCategory("", R.mipmap.uc_collection, R.mipmap.uc_collection_focused));
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

        TextView txtUserCenterUid = findViewById(R.id.txt_user_center_uid);
        txtUserCenterUid.setText(getThemeName(SPUtils.getInstance().getString(SharePrefer.themeName,""), HostManager.getHostSpString(SharePrefer.userAccount, "用户ID")));
    }

    private void initListener(){
        GradientBorderLayout gradientBorderLayout = findViewById(R.id.layout_user_center_log);
        gradientBorderLayout.setOnFocusChangeListener(this);
        gradientBorderLayout.setOnClickListener(v -> {
            HostManager.logoutClear();
            HostManager.gotoLoginPage(v.getContext());
            finish();
        });
    }

    private void initViewObservable() {
        viewModel.pToast.observe(this, msg -> showToast(msg));

        viewModel.mldDeletePackageName.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String packageName) {
                deletePackage(packageName);
            }
        });
    }

    @Override
    public void onCategorySelected(int position) {
        if(position == 0){
            replaceFragment(InteractiveRecordFragment.newInstance());
        } else if(position == 1){
            replaceFragment(MineDownloadFragment.newInstance());
        } else if(position == 2){
            replaceFragment(MineFavoritesFragment.newInstance());
        }
    }

    private void replaceFragment(Fragment fragment){
        currentFragment = (BaseUserCenterFragment)fragment;
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

    public void deletePackage(String packageName){
        if(zeeManager != null){
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
            CommonUtils.scaleView(v, 1.1f);
        } else {
            v.clearAnimation();
            CommonUtils.scaleView(v, 1f);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        recyclerViewUserCenterCategory.requestFocus();
    }

    @Override
    public void onGlobalFocusChanged(View oldFocus, View newFocus) {
        Log.d("test", "onGlobalFocusChanged newFocus: " + newFocus);
        Log.d("test", "onGlobalFocusChanged oldFocus: " + oldFocus);
    }

    @Override
    public void onBackPressed() {
        if(currentFragment != null){
            if(currentFragment.mDelMode){
                currentFragment.backToNormalMode();
                return;
            }
        }
        super.onBackPressed();
    }
}