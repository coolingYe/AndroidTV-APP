package com.zee.launcher.home.ui.cache;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.zee.launcher.home.R;
import com.zee.launcher.home.data.model.ProductListMo;
import com.zee.launcher.home.data.model.ProductSkuIdListType;
import com.zee.launcher.home.service.TopicService;
import com.zee.launcher.home.ui.cache.adapter.CacheItemAdapter;
import com.zee.launcher.home.ui.detail.DetailActivity;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.model.LoadState;
import com.zeewain.base.ui.BaseActivity;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.DensityUtils;
import com.zeewain.base.utils.DisplayUtil;
import com.zeewain.base.utils.NetworkUtil;
import com.zeewain.base.widgets.CenterGridLayoutManager;
import com.zeewain.base.widgets.LoadingView;
import com.zeewain.base.widgets.NetworkErrView;
import com.zeewain.base.widgets.TopBarView;
import com.zwn.lib_download.db.CareController;
import com.zwn.lib_download.model.DownloadInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CacheManagerActivity extends BaseActivity {
    private NetworkErrView networkErrViewDetail;
    private LoadingView loadingViewDetail;
    private LinearLayout layoutCacheContent;
    private RecyclerView recyclerViewCache;
    private ImageView ivCacheActionLoading;
    private TextView tvCacheTip;
    private MaterialCardView cardUserAction;
    private Animation animationCacheActionLoading;
    private CacheManagerViewModel viewModel;
    private List<ProductSkuIdListType> dataList;
    private List<String> imageList;
    private List<String> videoList;
    private final ConcurrentLinkedQueue<ProductSkuIdListType> productSkuIdListTypeQueue = new ConcurrentLinkedQueue<>();
    private ProductSkuIdListType currentReqProductSkuIdListType = null;
    private CacheItemAdapter cacheItemAdapter;
    private CenterGridLayoutManager centerGridLayoutManager;
    private CacheReceiver cacheReceiver;

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
        DensityUtils.autoWidth(getApplication(), this);
        setContentView(R.layout.activity_cache_manager);

        viewModel = new ViewModelProvider(this).get(CacheManagerViewModel.class);

        Bundle bundle = getIntent().getBundleExtra(BaseConstants.EXTRA_HOME_PAGE_CACHE_INFO);
        if (bundle != null) {
            Serializable serializableObject = bundle.getSerializable(BaseConstants.EXTRA_HOME_PAGE_CACHE_INFO);
            if (serializableObject != null) {
                dataList = (List<ProductSkuIdListType>) serializableObject;
            }

            imageList = bundle.getStringArrayList(BaseConstants.EXTRA_HOME_PAGE_IMAGE_CACHE_INFO);
            videoList = bundle.getStringArrayList(BaseConstants.EXTRA_HOME_PAGE_VIDEO_CACHE_INFO);
        }

        if (dataList == null || dataList.size() == 0) {
            finish();
            return;
        }

        for (ProductSkuIdListType productSkuIdListType : dataList) {
            productSkuIdListTypeQueue.offer(productSkuIdListType);
        }

        initView();
        initViewObservable();
        registerCacheReceiver();

        viewModel.reqUserCenterData();
    }

    private void initView() {
        TopBarView topBarView = findViewById(com.zwn.user.R.id.top_bar_view);
        topBarView.setBackEnable(true);
        topBarView.disappearImgUser();

        networkErrViewDetail = findViewById(R.id.networkErrView_cache);
        networkErrViewDetail.setRetryClickListener(() -> {
            if (LoadState.Failed == viewModel.mldUserDataLoadState.getValue()) {
                TopicService.getInstance().clearQueueAndMapData();
                viewModel.reqUserCenterData();
            } else if (viewModel.mldProductRecodeListLoadState.getValue() != null
                    && LoadState.Failed == viewModel.mldProductRecodeListLoadState.getValue().loadState
                    && currentReqProductSkuIdListType != null) {
                TopicService.getInstance().clearQueueAndMapData();
                viewModel.reqProductListBySkuIds(currentReqProductSkuIdListType.skuIdList, currentReqProductSkuIdListType.careKey);
            } else if (viewModel.mldAllProductRecodeListLoadState.getValue() != null
                    && LoadState.Failed == viewModel.mldAllProductRecodeListLoadState.getValue().loadState) {
                List<String> skuIdList = new ArrayList<>();
                for (ProductSkuIdListType productSkuIdListType : dataList) {
                    skuIdList.addAll(productSkuIdListType.skuIdList);
                }
                viewModel.reqAllProductListBySkuIds(skuIdList);
            }
        });

        loadingViewDetail = findViewById(R.id.loadingView_cache);
        layoutCacheContent = findViewById(R.id.ll_cache_content);
        recyclerViewCache = findViewById(R.id.recycler_view_cache_manager);

        ivCacheActionLoading = findViewById(R.id.iv_cache_action_loading);
        tvCacheTip = findViewById(R.id.tv_cache_tip);
        cardUserAction = findViewById(R.id.card_user_action);
        cardUserAction.setOnClickListener(v -> {
            if (!NetworkUtil.isNetworkAvailable(v.getContext())) {
                showToast("网络异常，请检查网络状态！");
                return;
            }
            tvCacheTip.setText("正在加载中...");
            if (ivCacheActionLoading.getVisibility() != View.VISIBLE) {
                ivCacheActionLoading.setVisibility(View.VISIBLE);
                ivCacheActionLoading.clearAnimation();
                ivCacheActionLoading.setAnimation(animationCacheActionLoading);
                animationCacheActionLoading.start();
            }
            cardUserAction.setVisibility(View.GONE);

            TopicService.getInstance().clearQueueAndMapData();
            currentReqProductSkuIdListType = null;
            productSkuIdListTypeQueue.clear();
            for (ProductSkuIdListType productSkuIdListType : dataList) {
                productSkuIdListTypeQueue.offer(productSkuIdListType);
            }
            viewModel.reqUserCenterData();
        });

        cardUserAction.setOnFocusChangeListener((view, hasFocus) -> {
            final int strokeWidth = DisplayUtil.dip2px(view.getContext(), 1);
            if (hasFocus) {
                cardUserAction.setStrokeColor(0xFFFA701F);
                cardUserAction.setStrokeWidth(strokeWidth);
                CommonUtils.scaleView(view, 1.1f);
            } else {
                cardUserAction.setStrokeColor(0x00FFFFFF);
                cardUserAction.setStrokeWidth(0);
                view.clearAnimation();
                CommonUtils.scaleView(view, 1f);
            }
        });

        animationCacheActionLoading = AnimationUtils.loadAnimation(ivCacheActionLoading.getContext(), com.zeewain.base.R.anim.rotate_loading_anim);
        LinearInterpolator interpolator = new LinearInterpolator();
        animationCacheActionLoading.setInterpolator(interpolator);
    }

    private void initViewObservable() {
        viewModel.mldUserDataLoadState.observe(this, loadState -> {
            if (LoadState.Loading == loadState) {
                networkErrViewDetail.setVisibility(View.GONE);
                layoutCacheContent.setVisibility(View.GONE);
                if (loadingViewDetail.getVisibility() != View.VISIBLE) {
                    loadingViewDetail.setVisibility(View.VISIBLE);
                    loadingViewDetail.startAnim();
                }
                loadingViewDetail.setText("加载中");
            } else if (LoadState.Success == loadState) {
                currentReqProductSkuIdListType = productSkuIdListTypeQueue.poll();
                if (currentReqProductSkuIdListType != null) {
                    viewModel.reqProductListBySkuIds(currentReqProductSkuIdListType.skuIdList, currentReqProductSkuIdListType.careKey);
                }
            } else {
                loadingViewDetail.stopAnim();
                loadingViewDetail.setVisibility(View.GONE);
                layoutCacheContent.setVisibility(View.GONE);
                networkErrViewDetail.setVisibility(View.VISIBLE);
            }
        });

        viewModel.mldProductRecodeListLoadState.observe(this, dataLoadState -> {
            if (LoadState.Loading == dataLoadState.loadState) {
                networkErrViewDetail.setVisibility(View.GONE);
                layoutCacheContent.setVisibility(View.GONE);
                if (loadingViewDetail.getVisibility() != View.VISIBLE) {
                    loadingViewDetail.setVisibility(View.VISIBLE);
                    loadingViewDetail.startAnim();
                }
                loadingViewDetail.setText(String.format("正在加载首页列表数据%d/%d", (dataList.size() - productSkuIdListTypeQueue.size()), dataList.size()));
            } else if (LoadState.Success == dataLoadState.loadState) {
                currentReqProductSkuIdListType = productSkuIdListTypeQueue.poll();
                if (currentReqProductSkuIdListType != null) {
                    viewModel.reqProductListBySkuIds(currentReqProductSkuIdListType.skuIdList, currentReqProductSkuIdListType.careKey);
                } else {
                    List<String> skuIdList = new ArrayList<>();
                    for (ProductSkuIdListType productSkuIdListType : dataList) {
                        skuIdList.addAll(productSkuIdListType.skuIdList);
                    }
                    viewModel.reqAllProductListBySkuIds(skuIdList);
                }
            } else {
                loadingViewDetail.stopAnim();
                loadingViewDetail.setVisibility(View.GONE);
                layoutCacheContent.setVisibility(View.GONE);
                networkErrViewDetail.setVisibility(View.VISIBLE);
            }
        });

        viewModel.mldAllProductRecodeListLoadState.observe(this, dataLoadState -> {
            if (LoadState.Loading == dataLoadState.loadState) {
                networkErrViewDetail.setVisibility(View.GONE);
                layoutCacheContent.setVisibility(View.GONE);
                if (loadingViewDetail.getVisibility() != View.VISIBLE) {
                    loadingViewDetail.setVisibility(View.VISIBLE);
                    loadingViewDetail.startAnim();
                }
                loadingViewDetail.setText("正在加载中...");
            } else if (LoadState.Success == dataLoadState.loadState) {
                loadingViewDetail.stopAnim();
                loadingViewDetail.setVisibility(View.GONE);
                layoutCacheContent.setVisibility(View.VISIBLE);
                handAllSkuIdCache(dataLoadState.data);
            } else {
                loadingViewDetail.stopAnim();
                loadingViewDetail.setVisibility(View.GONE);
                layoutCacheContent.setVisibility(View.GONE);
                networkErrViewDetail.setVisibility(View.VISIBLE);
            }
        });

        viewModel.mldDownloadInfoListUpdate.observe(this, downloadInfoList -> {
            if (cacheItemAdapter != null) {
                Collections.reverse(downloadInfoList);
                cacheItemAdapter.updateDataList(downloadInfoList);
            }
            handleUpdateOnTimerTick(downloadInfoList);
        });
    }

    private void handleUpdateOnTimerTick(List<DownloadInfo> downloadInfoList) {
        String cachingString = TopicService.getInstance().getCachingString();
        if (cachingString.isEmpty()) {//caching done
            if (TopicService.getInstance().hasCachingFailedData()) {
                tvCacheTip.setText("部分资源加载失败！");
                animationCacheActionLoading.cancel();
                ivCacheActionLoading.clearAnimation();
                ivCacheActionLoading.setVisibility(View.GONE);
                cardUserAction.setVisibility(View.VISIBLE);
            } else {
                if (!NetworkUtil.isNetworkAvailable(this)) {
                    tvCacheTip.setText("网络异常，请检查网络状态！");
                    return;
                }
                int successCount = 0;
                int loadingCount = 0;
                List<DownloadInfo> stopDownloadInfoList = new ArrayList<>(1);
                for (DownloadInfo downloadInfo : downloadInfoList) {
                    if (downloadInfo.status == DownloadInfo.STATUS_SUCCESS) {
                        successCount++;
                    } else if (downloadInfo.status == DownloadInfo.STATUS_STOPPED) {
                        stopDownloadInfoList.add(downloadInfo);
                    } else {
                        loadingCount++;
                    }
                }

                if (stopDownloadInfoList.size() > 0 && loadingCount == 0) {
                    TopicService.getInstance().handStopDownloadList(stopDownloadInfoList);
                }

                if (successCount < downloadInfoList.size()) {
                    tvCacheTip.setText(String.format("正在下载课件（%d/%d）", successCount, downloadInfoList.size()));
                } else {// is relay done?
                    if (TopicService.getInstance().getRelayLibFileIdMap().size() == 0
                            && TopicService.getInstance().getRelayModelBinFileIdMap().size() == 0) {
                        tvCacheTip.setText("已全部缓存成功！");
                        viewModel.countDownTimer.cancel();

                        animationCacheActionLoading.cancel();
                        ivCacheActionLoading.clearAnimation();
                        ivCacheActionLoading.setVisibility(View.GONE);
                    } else {
                        tvCacheTip.setText("正在检查依赖下载...");
                        checkAllRelayData();
                    }
                }
            }
        } else {//in caching...
            tvCacheTip.setText(cachingString);
            if (ivCacheActionLoading.getVisibility() != View.VISIBLE) {
                ivCacheActionLoading.setVisibility(View.VISIBLE);
                ivCacheActionLoading.clearAnimation();
                ivCacheActionLoading.setAnimation(animationCacheActionLoading);
                animationCacheActionLoading.start();
            }
            cardUserAction.setVisibility(View.GONE);
        }
    }

    private void checkAllRelayData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                StringBuilder sbLibRelays = new StringBuilder();
                for (String key : TopicService.getInstance().getRelayLibFileIdMap().keySet()) {
                    sbLibRelays.append(",").append(key);
                }
                if (sbLibRelays.length() > 0) {
                    sbLibRelays.deleteCharAt(0);
                    List<DownloadInfo> downloadList = CareController.instance.getAllDownloadInfo("type=" + BaseConstants.DownloadFileType.SHARE_LIB + " and status<>3 and fileId in (" + sbLibRelays + ")");
                    if (downloadList.size() > 0) {
                        for (DownloadInfo downloadInfo : downloadList) {
                            if (downloadInfo.status == DownloadInfo.STATUS_STOPPED && TopicService.getInstance().getDownloadBinder() != null) {
                                TopicService.getInstance().getDownloadBinder().startDownload(downloadInfo.fileId);
                                return;
                            }
                        }
                        return;
                    } else {
                        TopicService.getInstance().clearRelayLibFileIdMap();
                    }
                }

                StringBuilder sbModelRelays = new StringBuilder();
                for (String key : TopicService.getInstance().getRelayModelBinFileIdMap().keySet()) {
                    sbModelRelays.append(",").append(key);
                }
                if (sbModelRelays.length() > 0) {
                    sbModelRelays.deleteCharAt(0);
                    List<DownloadInfo> downloadList = CareController.instance.getAllDownloadInfo("type=" + BaseConstants.DownloadFileType.MODEL_BIN + " and status<>3 and fileId in (" + sbModelRelays + ")");
                    if (downloadList.size() > 0) {
                        for (DownloadInfo downloadInfo : downloadList) {
                            if (downloadInfo.status == DownloadInfo.STATUS_STOPPED && TopicService.getInstance().getDownloadBinder() != null) {
                                TopicService.getInstance().getDownloadBinder().startDownload(downloadInfo.fileId);
                                return;
                            }
                        }
                        return;
                    } else {
                        TopicService.getInstance().clearRelayModelBinFileIdMap();
                    }
                }
                //all done
            }
        }).start();
    }

    private void handAllSkuIdCache(List<ProductListMo.Record> productRecordList) {
        List<String> skuIdList = new ArrayList<>();
        for (ProductListMo.Record record : productRecordList) {
            skuIdList.add(record.getSkuId());
        }
        TopicService.getInstance().initSkuIdCacheQueue(skuIdList, imageList, videoList);

        if (cacheItemAdapter == null) {
            cacheItemAdapter = new CacheItemAdapter();
            cacheItemAdapter.setOnItemClickListener((view, position, downloadInfo) -> {
                Intent intent = new Intent(view.getContext(), DetailActivity.class);
                intent.putExtra("skuId", downloadInfo.extraId);
                startActivity(intent);
            });
            cacheItemAdapter.setOnItemFocusedListener(integer -> centerGridLayoutManager.smoothScrollToPosition(recyclerViewCache, new RecyclerView.State(), integer));
            cacheItemAdapter.setHasStableIds(true);
            centerGridLayoutManager = new CenterGridLayoutManager(this, 6);
            recyclerViewCache.setLayoutManager(centerGridLayoutManager);
            recyclerViewCache.setAdapter(cacheItemAdapter);
        }

        viewModel.countDownTimer.start();
    }

    @Override
    protected void onDestroy() {
        viewModel.countDownTimer.cancel();
        animationCacheActionLoading.cancel();
        ivCacheActionLoading.clearAnimation();
        unregisterCacheReceiver();
        super.onDestroy();
    }

    private void registerCacheReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        cacheReceiver = new CacheReceiver();
        registerReceiver(cacheReceiver, filter);
    }

    private void unregisterCacheReceiver() {
        if (cacheReceiver != null) {
            unregisterReceiver(cacheReceiver);
        }
    }

    private static class CacheReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                if (!NetworkUtil.isNetworkAvailable(context)) {
                    TopicService.getInstance().handNetDisconnect();
                }
            }
        }
    }
}
