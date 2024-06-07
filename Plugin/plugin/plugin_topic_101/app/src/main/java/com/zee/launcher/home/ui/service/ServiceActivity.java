package com.zee.launcher.home.ui.service;

import android.os.Bundle;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zee.launcher.home.R;
import com.zee.launcher.home.adapter.CenterLayoutManager;
import com.zee.launcher.home.adapter.ProductListAdapter;
import com.zee.paged.HorizontalRecyclerView;
import com.zeewain.base.ui.BaseActivity;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.DensityUtils;
import com.zeewain.base.widgets.CenterGridLayoutManager;
import com.zeewain.base.widgets.LoadingView;
import com.zeewain.base.widgets.NetworkErrView;

import java.util.ArrayList;

public class ServiceActivity extends BaseActivity {

    private ServiceViewModel mViewModel;
    private ProductListAdapter productListAdapter;
    private NetworkErrView networkErrViewServiceClassic;
    private LoadingView loadingViewServiceClassic;
    private ArrayList<String> skuIds = new ArrayList<>();
    private ConstraintLayout clServiceLayout;
    private CenterGridLayoutManager centerGridLayoutManager;
    private HorizontalRecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DensityUtils.autoWidth(getApplication(), this);
        setContentView(R.layout.activity_service);
        skuIds = getIntent().getStringArrayListExtra("skuIds");
        if (!(skuIds.size() > 0)) {
            finish();
            return;
        }

        mViewModel = new ViewModelProvider(this).get(ServiceViewModel.class);
        initView();
        initListener();
        initData();
        initObService();
    }

    private void initView() {
        View backView = findViewById(R.id.view_service_back);
        backView.setOnClickListener(v -> finish());
        backView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                if (v.getId() == backView.getId()) {
                    backView.setBackgroundResource(com.zwn.user.R.mipmap.ic_back_selected_bg);
                    CommonUtils.scaleView(v, 1.1f);
                }
            } else {
                if (v.getId() == backView.getId()) {
                    backView.setBackgroundResource(com.zwn.user.R.mipmap.ic_back_bg);
                    v.clearAnimation();
                    CommonUtils.scaleView(v, 1f);
                }
            }
        });
        clServiceLayout = findViewById(R.id.cl_service_layout);
        loadingViewServiceClassic = findViewById(R.id.loadingView_product_classic);
        networkErrViewServiceClassic = findViewById(R.id.networkErrView_product_classic);
        recyclerView = findViewById(R.id.rv_service_category);
        centerGridLayoutManager = new CenterGridLayoutManager(this, 3);
        recyclerView.setLayoutManager(centerGridLayoutManager);
        productListAdapter = new ProductListAdapter(new ArrayList<>(), ProductListAdapter.TYPE_PERSON_THREE);
        recyclerView.setAdapter(productListAdapter);
    }

    private void initData() {
        mViewModel.reqProductListBySkuIds(skuIds);
    }

    private void initListener() {
        networkErrViewServiceClassic.setRetryClickListener(() -> mViewModel.reqProductListBySkuIds(skuIds));
        productListAdapter.setOnItemFocusedListens(integer -> centerGridLayoutManager.smoothScrollToPosition(recyclerView, new RecyclerView.State(), integer));
    }

    private void initObService() {
        mViewModel.mldProductRecodeListLoadState.observe(this, loadState -> {
            switch (loadState) {
                case Loading:
                    loadingViewServiceClassic.setVisibility(View.VISIBLE);
                    networkErrViewServiceClassic.setVisibility(View.GONE);
                    clServiceLayout.setVisibility(View.GONE);
                    loadingViewServiceClassic.startAnim();
                    break;
                case Success:
                    loadingViewServiceClassic.stopAnim();
                    loadingViewServiceClassic.setVisibility(View.GONE);
                    networkErrViewServiceClassic.setVisibility(View.GONE);
                    clServiceLayout.setVisibility(View.VISIBLE);
                    break;
                case Failed:
                    loadingViewServiceClassic.stopAnim();
                    loadingViewServiceClassic.setVisibility(View.GONE);
                    clServiceLayout.setVisibility(View.GONE);
                    networkErrViewServiceClassic.setVisibility(View.VISIBLE);
                    break;
            }
        });
        mViewModel.productRecodeList.observe(this, records ->
                recyclerView.post(() -> {
                    productListAdapter.updateList(records);
                    recyclerView.requestFocus();
                }));
    }
}
