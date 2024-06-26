package com.zee.launcher.home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.gson.Gson;
import com.zee.launcher.home.adapter.CenterLayoutManager;
import com.zee.launcher.home.adapter.HMainTabItemDecoration;
import com.zee.launcher.home.adapter.MainTabAdapter;
import com.zee.launcher.home.adapter.MainViewPager2Adapter;
import com.zee.launcher.home.data.DataRepository;
import com.zee.launcher.home.data.layout.GlobalLayout;
import com.zee.launcher.home.data.protocol.response.AkSkResp;
import com.zee.launcher.home.ui.cache.CacheManagerActivity;
import com.zee.launcher.home.ui.loading.LoadingLocationPluginActivity;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.config.SharePrefer;
import com.zeewain.base.model.LoadState;
import com.zeewain.base.ui.BaseActivity;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.DensityUtils;
import com.zeewain.base.utils.DisplayUtil;
import com.zeewain.base.widgets.LoadingView;
import com.zeewain.base.widgets.NetworkErrView;
import com.zeewain.base.widgets.TopBarView;
import com.zwn.launcher.host.HostManager;

import java.io.Serializable;
import java.util.Set;

public class MainActivity extends BaseActivity {

    private MainViewModel viewModel;
    private LoadingView loadingViewHomeClassic;
    private NetworkErrView networkErrViewHomeClassic;
    private LinearLayout llMainContent;
    private ViewPager2 viewPageMain;
    private ConstraintLayout clMainTabRoot;
    private RecyclerView recyclerViewMainTab;
    private MainTabAdapter mainTabAdapter;
    private MainViewPager2Adapter mainViewPager2Adapter;
    private CenterLayoutManager centerLayoutManager;
    private View mainTabView;
    private TopBarView topBarView;
    private StartAppReceiver startReceiver;
    private MainReceiver mainReceiver;

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        savedInstanceState.setClassLoader(getClass().getClassLoader());
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(savedInstanceState != null) {
            savedInstanceState.setClassLoader(getClass().getClassLoader());
            Bundle bundle = savedInstanceState.getBundle("androidx.lifecycle.BundlableSavedStateRegistry.key");
            if(bundle != null) {
                Set<String> keySet = bundle.keySet();
                if (keySet != null) {
                    for(String key: keySet){
                        Object object = bundle.get(key);
                        if(object instanceof Bundle){
                            ((Bundle)object).setClassLoader(getClass().getClassLoader());
                        }
                    }
                }
            }
        }
        super.onCreate(savedInstanceState);
        MainViewModelFactory factory = new MainViewModelFactory(DataRepository.getInstance());
        viewModel = new ViewModelProvider(this, factory).get(MainViewModel.class);

        DensityUtils.autoWidth(getApplication(), this);
        setContentView(R.layout.activity_main);

        loadingViewHomeClassic = findViewById(R.id.loadingView_home_classic);
        networkErrViewHomeClassic = findViewById(R.id.networkErrView_home_classic);
        llMainContent = findViewById(R.id.ll_main_content);
        viewPageMain = findViewById(R.id.view_page_main);
        viewPageMain.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        viewPageMain.setFocusable(true);
        viewPageMain.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    RecyclerView recyclerView = (RecyclerView) viewPageMain.getChildAt(0);
                    if (recyclerView.getLayoutManager() != null) {
                        int position = viewPageMain.getCurrentItem();
                        View view = recyclerView.getLayoutManager().findViewByPosition(position);
                        if (view != null) {
                            boolean canFocus = view.requestFocus();
                            if (!canFocus) {
                                recyclerViewMainTab.requestFocus();
                            }
                        }
                    }
                }
            }
        });

        mainTabView = LayoutInflater.from(this).inflate(R.layout.layout_main_tab, null, false);
        clMainTabRoot = mainTabView.findViewById(R.id.cl_main_tab_root);
        recyclerViewMainTab = mainTabView.findViewById(R.id.recycler_view_main_tab);
        recyclerViewMainTab.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        recyclerViewMainTab.setFocusable(true);
        recyclerViewMainTab.requestFocus();
        recyclerViewMainTab.setNextFocusDownId(R.id.view_page_main);
        topBarView = findViewById(R.id.top_bar_view);
        topBarView.addCenterView(mainTabView);
        topBarView.updateUserImg(getUserName(HostManager.getHostSpString(SharePrefer.userAccount, "用户ID")));

        initListener();
        initViewObservable();

        HomeApplication.initHostData();

        viewModel.reqServicePackInfo();

        startReceiver();
        registerMainReceiver();
    }

    private String getUserName(String name) {
        if (name != null) {
            if (name.length() > 14) {
                return name.substring(0, 14) + "...";
            }
            return name;
        }
        return "";
    }

    private void initView(GlobalLayout globalLayout) {
        if ("vertical".equals(globalLayout.layout.basic.config.direction)) {
            initVerticalLayout();
        } else {
            initHorizontalLayout();
        }

        mainTabAdapter = new MainTabAdapter(globalLayout.layout.pages);
        mainTabAdapter.setHasStableIds(true);
        mainTabAdapter.setOnSelectedListener(position -> {
            mainTabAdapter.setSelectedPosition(position);
            centerLayoutManager.smoothScrollToPosition(recyclerViewMainTab, new RecyclerView.State(), position);
            viewPageMain.setCurrentItem(position);
        });
        recyclerViewMainTab.setAdapter(mainTabAdapter);
        if (globalLayout.layout.pages.size() == 1) {
            clMainTabRoot.setVisibility(View.GONE);
        } else {
            recyclerViewMainTab.requestFocus();
        }

        if (globalLayout.layout.basic.pageHeaderLayout != null && globalLayout.layout.basic.pageHeaderLayout.config != null) {
            if (globalLayout.layout.basic.pageHeaderLayout.config.showLogo) {
                TopBarView topBarView = findViewById(R.id.top_bar_view);
                topBarView.setShowTxtLogo(globalLayout.layout.basic.pageHeaderLayout.config.showLogo);
            }
        }

        mainViewPager2Adapter = new MainViewPager2Adapter(globalLayout.layout.pages, getSupportFragmentManager(), getLifecycle());
        viewPageMain.setAdapter(mainViewPager2Adapter);
    }

    private void initChannel(int position) {
        View mainTabViewBg = findViewById(R.id.v_main_tab_bg);
        if (CommonUtils.isTopic003Enable()) {
            if (position == 1) {
                mainTabViewBg.setBackgroundResource(R.mipmap.img_main_tab_cmcc1_tab2_bg);
                topBarView.setTabResourceIds(
                        new int[]{R.mipmap.top_bar_user_login_tab2, R.drawable.selector_btn_tab2},
                        new int[]{R.mipmap.top_bar_settings_tab2, R.drawable.selector_btn_frame},
                        new int[]{R.mipmap.top_bar_wifi_err_tab2, R.mipmap.top_bar_wifi_tab2, R.drawable.selector_btn_frame},
                        new int[]{R.drawable.selector_top_bar_back_cmcc1, 0xFFFFFFFF});
                getWindow().setBackgroundDrawableResource(R.mipmap.img_main_home_tab2_bg);
                topBarView.updateWifiInfo();
                return;
            }
        }
        mainTabViewBg.setBackgroundResource(R.mipmap.img_main_tab_cmcc1_bg);
        topBarView.setTabResourceIds(
                new int[]{R.mipmap.top_bar_user_login, R.drawable.selector_btn},
                new int[]{R.mipmap.top_bar_settings, R.drawable.selector_btn_frame},
                new int[]{R.mipmap.top_bar_wifi_err, R.mipmap.top_bar_wifi, R.drawable.selector_btn_frame},
                new int[]{R.drawable.selector_top_bar_back_cmcc1, 0xFFFFFFFF});
        getWindow().setBackgroundDrawableResource(R.mipmap.img_main_home_bg);
        topBarView.updateWifiInfo();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        if (hasFocus) {
            WindowManager.LayoutParams params = getWindow().getAttributes();
            params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            getWindow().setAttributes(params);
        }
    }

    private void initVerticalLayout() {
        llMainContent.setOrientation(LinearLayout.VERTICAL);
        ViewGroup.LayoutParams layoutParams = recyclerViewMainTab.getLayoutParams();
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        recyclerViewMainTab.setLayoutParams(layoutParams);
        //binding.recyclerViewMainTab.setVisibility(View.GONE);

        layoutParams = viewPageMain.getLayoutParams();
        layoutParams.height = 0;
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        viewPageMain.setLayoutParams(layoutParams);

        centerLayoutManager = new CenterLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerViewMainTab.setLayoutManager(centerLayoutManager);
        recyclerViewMainTab.addItemDecoration(new HMainTabItemDecoration(DisplayUtil.dip2px(this, 20)));
    }

    private void initHorizontalLayout() {
        llMainContent.setOrientation(LinearLayout.HORIZONTAL);
        ViewGroup.LayoutParams layoutParams = recyclerViewMainTab.getLayoutParams();
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        recyclerViewMainTab.setLayoutParams(layoutParams);
        //binding.recyclerViewTab.setVisibility(View.GONE);

        layoutParams = viewPageMain.getLayoutParams();
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        viewPageMain.setLayoutParams(layoutParams);
        viewPageMain.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
        viewPageMain.setUserInputEnabled(false);

        centerLayoutManager = new CenterLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerViewMainTab.setLayoutManager(centerLayoutManager);
    }

    private void initListener() {
        networkErrViewHomeClassic.setRetryClickListener(() -> {
            viewModel.reqServicePackInfo();
        });

        viewPageMain.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (mainTabAdapter != null) {
                    mainTabAdapter.setSelectedPosition(position);
                    centerLayoutManager.smoothScrollToPosition(recyclerViewMainTab, new RecyclerView.State(), position);
                    View view = centerLayoutManager.findViewByPosition(position);
                    if (view != null) {
                        view.requestFocus();
                    }
                }
                initChannel(position);
            }
        });
    }

    private void initViewObservable() {
        viewModel.mldServicePackInfoLoadState.observe(this, loadState -> {
            if (LoadState.Loading == loadState) {
                loadingViewHomeClassic.setVisibility(View.VISIBLE);
                networkErrViewHomeClassic.setVisibility(View.GONE);
                llMainContent.setVisibility(View.GONE);
                loadingViewHomeClassic.startAnim();
            } else if (LoadState.Success == loadState) {
                loadingViewHomeClassic.stopAnim();
                loadingViewHomeClassic.setVisibility(View.GONE);
                networkErrViewHomeClassic.setVisibility(View.GONE);
                llMainContent.setVisibility(View.VISIBLE);
                initView(viewModel.globalLayout);
            } else {
                loadingViewHomeClassic.stopAnim();
                loadingViewHomeClassic.setVisibility(View.GONE);
                networkErrViewHomeClassic.setVisibility(View.VISIBLE);
            }
        });

        viewModel.mldToastMsg.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String msg) {
                showToast(msg);
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK
                || keyCode == KeyEvent.KEYCODE_HOME)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }




    private void startReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("startApp");
        startReceiver = new StartAppReceiver();
        registerReceiver(startReceiver, filter);
    }

    private void stopReceiver() {
        if (startReceiver != null) {
            unregisterReceiver(startReceiver);
        }
    }

    @Override
    protected void onDestroy() {
        stopReceiver();
        unregisterMainReceiver();
        super.onDestroy();
    }

    public static class StartAppReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String softwareCode = intent.getStringExtra("softwareCode");
            Log.i("MainActivity", "onReceive softwareCode: " + softwareCode);
            String akSkInfoString = HostManager.getHostSpString(SharePrefer.akSkInfo, null);
            if (akSkInfoString != null && !akSkInfoString.isEmpty() && !softwareCode.isEmpty()) {
                Gson gson = new Gson();
                AkSkResp akSkResp = gson.fromJson(akSkInfoString, AkSkResp.class);
                if (akSkResp != null) {
                    Intent intents = new Intent(context, LoadingLocationPluginActivity.class);
                    intents.putExtra(BaseConstants.EXTRA_PLUGIN_NAME, softwareCode);
                    intents.putExtra(BaseConstants.EXTRA_AUTH_AK_CODE, akSkResp.akCode);
                    intents.putExtra(BaseConstants.EXTRA_AUTH_SK_CODE, akSkResp.skCode);
                    context.startActivity(intents);
                }
            }
        }
    }

    private void registerMainReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BaseConstants.START_CACHE_MANAGER_ACTION);
        mainReceiver = new MainReceiver();
        registerReceiver(mainReceiver, filter);
    }

    private void unregisterMainReceiver() {
        if (mainReceiver != null) {
            unregisterReceiver(mainReceiver);
        }
    }

    private class MainReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (BaseConstants.START_CACHE_MANAGER_ACTION.equals(intent.getAction())) {
                if (viewModel.globalLayout != null && LoadState.Success == viewModel.mldServicePackInfoLoadState.getValue()) {
                    Intent cacheManagerIntent = new Intent();
                    cacheManagerIntent.setClass(context, CacheManagerActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(BaseConstants.EXTRA_HOME_PAGE_CACHE_INFO, (Serializable) viewModel.getProductSkuIdListTypeList());
                    cacheManagerIntent.putExtra(BaseConstants.EXTRA_HOME_PAGE_CACHE_INFO, bundle);
                    context.startActivity(cacheManagerIntent);
                }
            }
        }
    }
}