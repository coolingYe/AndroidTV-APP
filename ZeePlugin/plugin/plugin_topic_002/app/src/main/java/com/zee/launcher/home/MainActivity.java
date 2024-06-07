package com.zee.launcher.home;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

import android.annotation.SuppressLint;
import android.app.Instrumentation;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;
import com.zee.launcher.home.adapter.CenterLayoutManager;
import com.zee.launcher.home.adapter.ProductListAdapter;
import com.zee.launcher.home.data.DataRepository;
import com.zee.launcher.home.data.layout.GlobalLayout;
import com.zee.launcher.home.data.model.ProductListMo;
import com.zee.paged.HorizontalRecyclerView;
import com.zeewain.base.config.SharePrefer;
import com.zeewain.base.model.LoadState;
import com.zeewain.base.ui.BaseActivity;
import com.zeewain.base.utils.DensityUtils;
import com.zeewain.base.utils.GlideApp;
import com.zeewain.base.widgets.LoadingView;
import com.zeewain.base.widgets.NetworkErrView;
import com.zeewain.base.widgets.TopBarView;
import com.zwn.launcher.host.HostManager;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MainActivity extends BaseActivity {

    private MainViewModel viewModel;
    private LoadingView loadingViewHomeClassic;
    private NetworkErrView networkErrViewHomeClassic;
    private HorizontalRecyclerView recyclerView;
    private ProductListAdapter productListAdapter;
    private ImageView ivProductBackground, ivProductLogo;
    private TextView tvProductDetail, tvProductDesc;
    private CenterLayoutManager centerLayoutManager;
    private ConstraintLayout clMainLayout;
    private Drawable previousImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainViewModelFactory factory = new MainViewModelFactory(DataRepository.getInstance());
        viewModel = new ViewModelProvider(this, factory).get(MainViewModel.class);

        DensityUtils.autoWidth(getApplication(), this);
        setContentView(R.layout.activity_main);

        loadingViewHomeClassic = findViewById(R.id.loadingView_home_classic);
        networkErrViewHomeClassic = findViewById(R.id.networkErrView_home_classic);
        TopBarView topBarView = findViewById(R.id.top_bar_view);
        topBarView.updateUserImg(HostManager.getHostSpString(SharePrefer.userAccount, "用户ID"));

        clMainLayout = findViewById(R.id.cl_main_content);
        ivProductBackground = findViewById(R.id.iv_main_product_background);

        recyclerView = findViewById(R.id.rv_main_product);
        centerLayoutManager = new CenterLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(centerLayoutManager);
        productListAdapter = new ProductListAdapter(new ArrayList<>());
        recyclerView.setAdapter(productListAdapter);

        tvProductDetail = findViewById(R.id.tv_main_product_detail);

        tvProductDesc = findViewById(R.id.tv_main_product_desc);
        ivProductLogo = findViewById(R.id.iv_main_product_logo);
        previousImage = getDrawable(R.drawable.bg_default);
        initListener();
        initViewObservable();

        HomeApplication.initHostData();

        viewModel.reqServicePackInfo();
    }

    private void initView(GlobalLayout globalLayout) {
        if (globalLayout != null) {
            if (globalLayout.layout.pages.size() > 0) {
                if (globalLayout.layout.pages.get(0).content.size() > 0) {
                    if (globalLayout.layout.pages.get(0).content.get(0).config != null) {
                        if (globalLayout.layout.pages.get(0).content.get(0).config.appSkus.size() > 0) {
                            viewModel.reqProductListBySkuIds(globalLayout.layout.pages.get(0).content.get(0).config.appSkus);
                        }
                    }
                }
            }
        }
    }

    @SuppressLint({"ResourceType", "SetTextI18n"})
    private void initListener() {
        networkErrViewHomeClassic.setRetryClickListener(() -> {
            viewModel.reqServicePackInfo();
        });
        productListAdapter.setCallback(this::getProductCardInfo);
        productListAdapter.setOnItemFocusedListens(integer -> {
            centerLayoutManager.smoothScrollToPosition(recyclerView, new RecyclerView.State(), integer);
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void initViewObservable() {
        viewModel.mldServicePackInfoLoadState.observe(this, loadState -> {
            if (LoadState.Loading == loadState) {
                loadingViewHomeClassic.setVisibility(View.VISIBLE);
                networkErrViewHomeClassic.setVisibility(View.GONE);
                clMainLayout.setVisibility(View.GONE);
                loadingViewHomeClassic.startAnim();
            } else if (LoadState.Success == loadState) {
                loadingViewHomeClassic.stopAnim();
                loadingViewHomeClassic.setVisibility(View.GONE);
                networkErrViewHomeClassic.setVisibility(View.GONE);
                clMainLayout.setVisibility(View.VISIBLE);
                initView(viewModel.globalLayout);
            } else {
                loadingViewHomeClassic.stopAnim();
                loadingViewHomeClassic.setVisibility(View.GONE);
                clMainLayout.setVisibility(View.GONE);
                networkErrViewHomeClassic.setVisibility(View.VISIBLE);
            }
        });

        viewModel.mldProductRecodeListLoadState.observe(this, loadState -> {
                if (LoadState.Success == loadState) {
                    productListAdapter.updateList(viewModel.productRecodeList);
                    if (viewModel.productRecodeList != null) { // fix not data for main background when touch click
                        if (viewModel.productRecodeList.size() > 0) {
                            getProductCardInfo(viewModel.productRecodeList.get(0));
                        }
                    }
                    recyclerView.post(() -> {
                        new Thread(() -> {
                            try {
                                Instrumentation inst= new Instrumentation();
                                inst.sendKeyDownUpSync(KeyEvent. KEYCODE_DPAD_DOWN);
                                inst.sendKeyDownUpSync(KeyEvent. KEYCODE_DPAD_DOWN);
                            } catch(Exception e) {
                                e.printStackTrace();
                            }
                        }).start();
                    });
                }
        });
    }

    private void getProductCardInfo(ProductListMo.Record record) {
        String backgroundUrl = "";
        String logoImageUrl = "";
        if (record.getExtendInfo() != null) {
            if (record.getExtendInfo().getBackgroundLogo() != null && !record.getExtendInfo().getBackgroundLogo().isEmpty()) {
                logoImageUrl = record.getExtendInfo().getBackgroundLogo();
            }
        }

        if (record.getExtendInfo() != null) {
            if (record.getExtendInfo().getBackgroundImages() != null) {
                if (!record.getExtendInfo().getBackgroundImages().getH().isEmpty()) {
                    backgroundUrl = record.getExtendInfo().getBackgroundImages().getH();
                } else backgroundUrl = record.getProductImg();
            } else backgroundUrl = record.getProductImg();
        } else backgroundUrl = record.getProductImg();

        if (!logoImageUrl.isEmpty()) {
            ivProductLogo.setVisibility(View.VISIBLE);
            GlideApp.with(ivProductLogo.getContext())
                    .load(logoImageUrl).transition(withCrossFade()).placeholder(0).override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                    .into(ivProductLogo);
        } else ivProductLogo.setVisibility(View.GONE);

        GlideApp.with(ivProductBackground.getContext())
                .load(backgroundUrl).placeholder(previousImage).transition(withCrossFade()).fitCenter()
                .into(ivProductBackground);

        String playNum = String.valueOf(record.getHeat());
        String score = "  ·  " + record.getScore() + "分";

        tvProductDetail.setVisibility(View.VISIBLE);
        tvProductDetail.setText(playNum + score);
        tvProductDesc.setText(record.getSimplerIntroduce());

        new Thread(() -> {
            previousImage = getDrawableGlide(record.getProductImg(), this);
        }).start();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK
                || keyCode == KeyEvent.KEYCODE_HOME)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public static Drawable getDrawableGlide(String url, Context context) {
        try {
            return Glide.with(context)
                    .load(url)
                    .submit()
                    .get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}