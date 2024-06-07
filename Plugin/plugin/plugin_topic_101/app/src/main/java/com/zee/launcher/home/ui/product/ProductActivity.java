package com.zee.launcher.home.ui.product;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zee.launcher.home.R;
import com.zee.launcher.home.adapter.CenterLayoutManager;
import com.zee.launcher.home.adapter.ProductListAdapter;
import com.zee.launcher.home.ui.product.rule.RuleActivity;
import com.zee.paged.HorizontalRecyclerView;
import com.zeewain.base.ui.BaseActivity;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.DensityUtils;
import com.zeewain.base.widgets.LoadingView;
import com.zeewain.base.widgets.NetworkErrView;

import java.util.ArrayList;
import java.util.List;

public class ProductActivity extends BaseActivity implements View.OnFocusChangeListener {

    private ProductViewModel mViewModel;
    private LoadingView loadingViewProductClassic;
    private NetworkErrView networkErrViewProductClassic;
    private HorizontalRecyclerView recyclerView;
    private ProductListAdapter productListAdapter;
    private List<String> skuIds = new ArrayList<>();
    private CenterLayoutManager centerLayoutManager;
    private ConstraintLayout clProductLayout;
    private View vEventsRule;
    private View backView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DensityUtils.autoWidth(getApplication(), this);
        setContentView(R.layout.activity_product);
        skuIds = getIntent().getStringArrayListExtra("skuIds");
        if (!(skuIds.size() > 0)) {
            finish();
            return;
        }
        mViewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        initView();
        initListener();
        initViewObservable();
        mViewModel.reqProductListBySkuIds(skuIds);
    }

    private void initView() {
        vEventsRule = findViewById(R.id.v_events_rule);
        vEventsRule.setOnFocusChangeListener(this);
        loadingViewProductClassic = findViewById(R.id.loadingView_product_classic);
        networkErrViewProductClassic = findViewById(R.id.networkErrView_product_classic);
        clProductLayout = findViewById(R.id.cl_product_layout);
        recyclerView = findViewById(R.id.rv_product_product);
        centerLayoutManager = new CenterLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(centerLayoutManager);
        productListAdapter = new ProductListAdapter(new ArrayList<>(), ProductListAdapter.TYPE_PERSON_ONE);
        recyclerView.setAdapter(productListAdapter);

        backView = findViewById(R.id.view_product_back);
        backView.setOnClickListener(v -> finish());
        backView.setOnFocusChangeListener(this);
    }

    private void initListener() {
        networkErrViewProductClassic.setRetryClickListener(() -> mViewModel.reqProductListBySkuIds(skuIds));
        productListAdapter.setOnItemFocusedListens(integer -> centerLayoutManager.smoothScrollToPosition(recyclerView, new RecyclerView.State(), integer));
        vEventsRule.setOnClickListener(v -> startActivity(new Intent(this, RuleActivity.class)));
    }

    private void initViewObservable() {
        mViewModel.mldProductRecodeListLoadState.observe(this, loadState -> {
            switch (loadState) {
                case Loading:
                    loadingViewProductClassic.setVisibility(View.VISIBLE);
                    networkErrViewProductClassic.setVisibility(View.GONE);
                    clProductLayout.setVisibility(View.GONE);
                    loadingViewProductClassic.startAnim();
                    break;
                case Success:
                    loadingViewProductClassic.stopAnim();
                    loadingViewProductClassic.setVisibility(View.GONE);
                    networkErrViewProductClassic.setVisibility(View.GONE);
                    clProductLayout.setVisibility(View.VISIBLE);
                    break;
                case Failed:
                    loadingViewProductClassic.stopAnim();
                    loadingViewProductClassic.setVisibility(View.GONE);
                    clProductLayout.setVisibility(View.GONE);
                    networkErrViewProductClassic.setVisibility(View.VISIBLE);
                    break;
            }
        });
        mViewModel.productRecodeList.observe(this, records ->
                recyclerView.post(() -> {
                    productListAdapter.updateList(records);
                    recyclerView.requestFocus();
                }));
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            if (v.getId() == backView.getId()) {
                backView.setBackgroundResource(R.mipmap.ic_back_selected_bg);
                CommonUtils.scaleView(v, 1.1f);
            }
            if (v.getId() == vEventsRule.getId()) {
                vEventsRule.setBackgroundResource(R.mipmap.ic_rule_enter_btn_selected_bg);
            }
        } else {
            if (v.getId() == backView.getId()) {
                backView.setBackgroundResource(R.mipmap.ic_back_bg);
                v.clearAnimation();
                CommonUtils.scaleView(v, 1f);
            }
            if (v.getId() == vEventsRule.getId()) {
                vEventsRule.setBackgroundResource(R.mipmap.ic_rule_enter_btn_bg);
            }
        }
    }
}
