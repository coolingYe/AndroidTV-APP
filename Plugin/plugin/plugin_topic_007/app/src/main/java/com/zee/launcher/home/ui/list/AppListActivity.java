package com.zee.launcher.home.ui.list;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zee.launcher.home.R;
import com.zee.launcher.home.adapter.ListAppAdapter;
import com.zee.launcher.home.data.DataRepository;
import com.zee.launcher.home.data.model.ProductListMo;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.model.LoadState;
import com.zeewain.base.ui.BaseActivity;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.DensityUtils;
import com.zeewain.base.widgets.LoadingView;
import com.zeewain.base.widgets.NetworkErrView;
import com.zwn.lib_download.db.CareSettings;
import com.zwn.lib_download.model.DownloadInfo;

import java.util.ArrayList;
import java.util.List;

public class AppListActivity extends BaseActivity implements View.OnFocusChangeListener {

    private ImageView imgBack;

    private RecyclerView rvAppList;

    private LinearLayout llAppListEmpty;
    private NetworkErrView networkErrViewDetail;
    private LoadingView loadingViewAppList;
    private AppListViewModel appListViewModel;
    private final List<ProductListMo.Record> records = new ArrayList<>();
    private ListAppAdapter listAppAdapter;
    private String softwareCode;
    private String pageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DensityUtils.autoWidth(getApplication(), this);
        setTheme(R.style.Transparent);
        setContentView(R.layout.activity_list);
        AppListViewModelFactory factory = new AppListViewModelFactory(DataRepository.getInstance());
        appListViewModel = new ViewModelProvider(this, factory).get(AppListViewModel.class);
        pageName = getIntent().getStringExtra(BaseConstants.EXTRA_PACKAGE_NAME);
        initView();
        initListener();
        initViewObservable();
        // 查询软件编码
        checkSoftwareCode();

    }

    private void checkSoftwareCode() {
        DownloadInfo downloadInfo = getDownloadInfoByMainClassPath(pageName);
        if (downloadInfo != null) {
            Log.d("AppListActivity", "checkSoftwareCode: " + downloadInfo.fileId);
            softwareCode = downloadInfo.fileId;
            appListViewModel.reqAppList(CommonUtils.getDeviceSn(), softwareCode);
        } else {
            loadingViewAppList.setVisibility(View.GONE);
            networkErrViewDetail.setVisibility(View.GONE);
            llAppListEmpty.setVisibility(View.VISIBLE);
            finish();
        }
    }

    public DownloadInfo getDownloadInfoByMainClassPath(String mainClassPath) {
        DownloadInfo downloadInfo = null;
        Cursor cursor = getContentResolver().query(CareSettings.DownloadInfo.CONTENT_URI,
                CareSettings.DownloadInfo.DOWNLOAD_INFO_QUERY_COLUMNS, "mainClassPath='" + mainClassPath + "'", null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                downloadInfo = new DownloadInfo(cursor);
            }
            cursor.close();
        }
        return downloadInfo;
    }


    private void initListener() {
        networkErrViewDetail.setRetryClickListener(() -> appListViewModel.reqAppList(CommonUtils.getDeviceSn(), softwareCode));
        imgBack.setOnFocusChangeListener(this);
    }

    private void initView() {
        imgBack = findViewById(com.zeewain.base.R.id.img_top_bar_back);
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getContext() instanceof AppCompatActivity) {
                    ((AppCompatActivity) v.getContext()).finish();
                }
            }
        });
        imgBack.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && event.getAction() == KeyEvent.ACTION_DOWN) {
                v.clearAnimation();
                v.startAnimation(AnimationUtils.loadAnimation(AppListActivity.this, com.zeewain.base.R.anim.host_shake));
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP && event.getAction() == KeyEvent.ACTION_DOWN) {
                v.clearAnimation();
                v.startAnimation(AnimationUtils.loadAnimation(AppListActivity.this, com.zeewain.base.R.anim.host_shake_y));
            }
            return false;
        });

        rvAppList = findViewById(R.id.rv_app_list);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 5);
        rvAppList.setLayoutManager(gridLayoutManager);
        rvAppList.setAdapter(listAppAdapter = new ListAppAdapter(records, pageName));
        networkErrViewDetail = findViewById(R.id.networkErrView_app_list);
        loadingViewAppList = findViewById(R.id.loadingView_app_list);
        llAppListEmpty = findViewById(R.id.ll_app_list_empty);
    }


    private void initViewObservable() {
        appListViewModel.mldInitLoadState.observe(this, loadState -> {
            if (LoadState.Loading == loadState) {
                networkErrViewDetail.setVisibility(View.GONE);
                rvAppList.setVisibility(View.GONE);
                llAppListEmpty.setVisibility(View.GONE);
                loadingViewAppList.setVisibility(View.VISIBLE);
                loadingViewAppList.startAnim();
            } else if (LoadState.Success == loadState) {
                networkErrViewDetail.setVisibility(View.GONE);
                llAppListEmpty.setVisibility(View.GONE);
                loadingViewAppList.stopAnim();
                loadingViewAppList.setVisibility(View.GONE);
                rvAppList.setVisibility(View.VISIBLE);
                loadAppListData();
            } else if (LoadState.Failed == loadState) {
                loadingViewAppList.stopAnim();
                llAppListEmpty.setVisibility(View.GONE);
                loadingViewAppList.setVisibility(View.GONE);
                networkErrViewDetail.setVisibility(View.VISIBLE);
            }
        });
    }

    // 加载包拉包数据
    private void loadAppListData() {
        if (appListViewModel.appRequestList != null && appListViewModel.appRequestList.size() > 0) {
            records.addAll(appListViewModel.appRequestList);
            listAppAdapter.notifyDataSetChanged();
        } else {
            llAppListEmpty.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (hasFocus) {
            CommonUtils.scaleView(view, 1.1f);
        } else {
            view.clearAnimation();
            CommonUtils.scaleView(view, 1f);
        }
    }
}