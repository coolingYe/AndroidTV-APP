package com.zee.launcher.home.ui.home;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.alibaba.fastjson.JSONArray;
import com.google.gson.Gson;
import com.youth.banner.Banner;
import com.youth.banner.listener.OnPageChangeListener;
import com.youth.banner.transformer.ScaleInTransformer;
import com.zee.launcher.home.MainViewModel;
import com.zee.launcher.home.R;
import com.zee.launcher.home.data.DataRepository;
import com.zee.launcher.home.data.layout.PageLayoutDTO;
import com.zee.launcher.home.data.model.ProductListMo;
import com.zee.launcher.home.data.protocol.response.ShowBannerBean;
import com.zee.launcher.home.data.protocol.response.ShowRoomBean;
import com.zee.launcher.home.ui.detail.DetailActivity;
import com.zee.launcher.home.ui.detail.DetailViewModelFactory;
import com.zee.launcher.home.ui.home.adapter.ImageAdapter;
import com.zee.launcher.home.widgets.MyVideoView;
import com.zeewain.base.model.LoadState;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.DensityUtils;
import com.zeewain.base.utils.DisplayUtil;
import com.zeewain.base.widgets.LoadingView;
import com.zeewain.base.widgets.NetworkErrView;
import com.zeewain.base.widgets.TopBarView;

import java.util.ArrayList;
import java.util.List;

import static android.media.MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START;


public class HomeShowRoomFragment extends Fragment implements View.OnFocusChangeListener, View.OnKeyListener {

    private MainViewModel mViewModel;
    private LoadingView loadingViewHomeClassic;
    private NetworkErrView networkErrViewHomeClassic;
    private LinearLayout llNoData;
    private PageLayoutDTO pageLayoutDTO;
    private int mCategoryIndex = 0;
    private List<ProductListMo.Record> productRecodeLists;
    private List<ShowBannerBean> bannerBeanList;
    private ConstraintLayout bannerLayout;
    private Banner banner;
    private ImageView leftArrow;
    private ImageView rightArrow;
    private ImageView enterDetail;
    private TextView appTitle;
    private Handler handler = new Handler();
    private ImageAdapter bannerMixAdapter;
    private MyVideoView videoView;
    private String videoUrl;
    private ProgressBar progressBar;
    private ImageView appTitleBg;
    private ImageView videoRightFrame;
    private ImageView videoLeftFrame;

    public static HomeShowRoomFragment newInstance() {
        return new HomeShowRoomFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        DensityUtils.autoWidth(requireActivity().getApplication(), requireActivity());
        View view = inflater.inflate(R.layout.fragment_home_show_room, container, false);
        initView(view);
        initReqData();
        initListener();
        initViewObserve();
        return view;
    }

    private void playVideo(String url) {

        videoView.setAlpha(0);
        videoView.setVisibility(View.VISIBLE);
        // 停止当前视频的播放
        videoView.stopPlayback();
        // 设置新的视频源
        videoView.setVideoURI(Uri.parse(url));
        // 准备视频，异步操作
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                // 视频准备好了，可以开始播放
                videoView.start();
                mp.setOnInfoListener((mp1, what, extra) -> {
                    if (what == MEDIA_INFO_VIDEO_RENDERING_START) {
                        videoView.setAlpha(1);
                        return true;
                    }
                    return false;
                });

            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                videoView.start();

            }
        });


    }

    private void initView(View view) {
        bannerLayout = view.findViewById(R.id.banner_layout);
        banner = view.findViewById(R.id.banner);
        leftArrow = view.findViewById(R.id.left_arrow_banner);
        rightArrow = view.findViewById(R.id.right_arrow_banner);
        videoRightFrame = view.findViewById(R.id.video_right_frame);
        videoLeftFrame = view.findViewById(R.id.video_left_frame);
        enterDetail = view.findViewById(R.id.enter_detail);
        appTitleBg = view.findViewById(R.id.app_title_bg);
        appTitle = view.findViewById(R.id.app_title);
        loadingViewHomeClassic = view.findViewById(R.id.loadingView_home_classic);
        networkErrViewHomeClassic = view.findViewById(R.id.networkErrView_home_classic);
        llNoData = view.findViewById(R.id.ll_no_data);

        videoView = view.findViewById(R.id.video);
        progressBar = view.findViewById(R.id.img_progress_loading);
        leftArrow.setOnFocusChangeListener(this);
        rightArrow.setOnFocusChangeListener(this);
        enterDetail.setOnFocusChangeListener(this);
        leftArrow.setOnKeyListener(this);
        rightArrow.setOnKeyListener(this);
        dealFocus();


    }

    private void dealFocus() {
        traverseViews(banner);
        TopBarView topBarView = getActivity().findViewById(R.id.top_bar_view);
        LinearLayout llUserTopBar = topBarView.findViewById(R.id.ll_user_top_bar);
        ImageView imgSettingsTopBar = topBarView.findViewById(R.id.img_settings_top_bar);
        ImageView imgWifiTopBar = topBarView.findViewById(R.id.img_wifi_top_bar);
        llUserTopBar.setNextFocusDownId(R.id.left_arrow_banner);
        imgSettingsTopBar.setNextFocusDownId(R.id.right_arrow_banner);
        imgWifiTopBar.setNextFocusDownId(R.id.right_arrow_banner);
        leftArrow.setNextFocusRightId(R.id.right_arrow_banner);
        rightArrow.setNextFocusLeftId(R.id.left_arrow_banner);
        leftArrow.setNextFocusLeftId(R.id.left_arrow_banner);
        rightArrow.setNextFocusRightId(R.id.right_arrow_banner);
    }

    private void traverseViews(ViewGroup parentView) {
        for (int i = 0; i < parentView.getChildCount(); i++) {
            View childView = parentView.getChildAt(i);
            // 在这里处理子 View，例如对每个子 View 进行操作
            childView.setDefaultFocusHighlightEnabled(false);
            childView.setFocusable(false);
            // 如果子 View 也是 ViewGroup，递归遍历其子 View
            if (childView instanceof ViewGroup) {
                traverseViews((ViewGroup) childView);
            }
        }
    }

    private void initReqData() {
        DetailViewModelFactory factory = new DetailViewModelFactory(DataRepository.getInstance());
        bannerBeanList = new ArrayList<>();
        pageLayoutDTO = mViewModel.globalLayout.layout.pages.get(mCategoryIndex);
        JSONArray content = pageLayoutDTO.content;
        if (content != null && content.size() > 0) {
            ShowRoomBean showRoomBean = new Gson().fromJson(String.valueOf(content.getJSONObject(0)), ShowRoomBean.class);
            List<ShowRoomBean.ItemDTO> items = showRoomBean.config.items;
            List<String> skuIdList = new ArrayList<>();
            for (int i = 0; i < items.size(); i++) {
                ShowRoomBean.ItemDTO itemDTO = items.get(i);
                String kind = itemDTO.kind;
                String url = itemDTO.url;
                String skuId = itemDTO.skuId;
                if (kind.equals("app")) {
                    skuIdList.add(skuId);
                } else {
                    bannerBeanList.add(new ShowBannerBean(kind, url));
                }
            }
            mViewModel.reqProductListBySkuIds(skuIdList, mCategoryIndex + "");
        }

    }

    private void initListener() {
        networkErrViewHomeClassic.setRetryClickListener(() -> initReqData());
    }

    //    @SuppressLint("NotifyDataSetChanged")
    private void initViewObserve() {
        mViewModel.mldProductRecodeListLoadState.observe(getViewLifecycleOwner(), productRecordLoadState -> {
            if (LoadState.Loading == productRecordLoadState.loadState) {
                if (loadingViewHomeClassic.getVisibility() != View.VISIBLE) {
                    loadingViewHomeClassic.setVisibility(View.VISIBLE);
                    loadingViewHomeClassic.startAnim();
                    networkErrViewHomeClassic.setVisibility(View.GONE);
                    bannerLayout.setVisibility(View.GONE);
                }
            } else if (LoadState.Success == productRecordLoadState.loadState) {
                showData();
            } else {
                loadingViewHomeClassic.stopAnim();
                loadingViewHomeClassic.setVisibility(View.GONE);
                networkErrViewHomeClassic.setVisibility(View.VISIBLE);
                bannerLayout.setVisibility(View.GONE);
            }
        });


    }

    public void showData() {
        loadingViewHomeClassic.stopAnim();
        loadingViewHomeClassic.setVisibility(View.GONE);
        networkErrViewHomeClassic.setVisibility(View.GONE);
        bannerLayout.setVisibility(View.VISIBLE);

        productRecodeLists = mViewModel.getProductRecodeListFromCache(mCategoryIndex + "");
        if (productRecodeLists != null && productRecodeLists.size() > 0) {
            for (int i = 0; i < productRecodeLists.size(); i++) {
                ProductListMo.Record record = productRecodeLists.get(i);
                String productImg = record.getProductImg();
                String productTitle = record.getProductTitle();
                String skuId = record.getSkuId();
                bannerBeanList.add(new ShowBannerBean("app", productImg, productTitle, skuId));
            }
            showBanner();
        } else {
            llNoData.setVisibility(View.VISIBLE);
        }


    }

    public void setBannerGalleryEffect(Context context, int leftItemWidth, int rightItemWidth, int pageMargin, float scale) {
        if (pageMargin > 0) {
            banner.addPageTransformer(new MarginPageTransformer(DisplayUtil.dip2px(context, pageMargin)));
        }
        if (scale < 1 && scale > 0) {
            banner.addPageTransformer(new ScaleInTransformer(scale));
        }
        setRecyclerViewPadding(leftItemWidth > 0 ? DisplayUtil.dip2px(context, leftItemWidth + pageMargin) : 0,
                rightItemWidth > 0 ? DisplayUtil.dip2px(context, rightItemWidth + pageMargin) : 0);
    }

    private void setRecyclerViewPadding(int leftItemPadding, int rightItemPadding) {
        RecyclerView recyclerView = (RecyclerView) banner.getViewPager2().getChildAt(0);
        if (banner.getViewPager2().getOrientation() == ViewPager2.ORIENTATION_VERTICAL) {
            recyclerView.setPadding(0, leftItemPadding, 0, rightItemPadding);
        } else {
            recyclerView.setPadding(leftItemPadding, 0, rightItemPadding, 0);
        }
        recyclerView.setClipToPadding(false);
    }

    private void showBanner() {
        bannerMixAdapter = new ImageAdapter(bannerBeanList);
        banner.addBannerLifecycleObserver(this)
                .setCurrentItem(0)
                .setAdapter(bannerMixAdapter, false);
        ShowBannerBean firstBannerBean = bannerBeanList.get(0);
        initShowBannerStatus(firstBannerBean);
        handleShowBean(firstBannerBean);
        //当大于1个条目时候才需要显示左右边框
        if (bannerBeanList.size() > 1) {
            handleLeftAndRightFrame(0);
        }

        setBannerGalleryEffect(banner.getContext(),160, 160, 85, 0.9f);

        banner.setUserInputEnabled(false);//开启或者禁止手动滑动
        if (!leftArrow.isFocused()) {
            leftArrow.setOnClickListener(v -> {
                banner.setCurrentItem(banner.getCurrentItem() - 1);
            });
        }
        if (!rightArrow.isFocused()) {
            rightArrow.setOnClickListener(v -> {
                banner.setCurrentItem(banner.getCurrentItem() + 1);
            });
        }


        banner.addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                ShowBannerBean showBannerBean = bannerBeanList.get(position);
                initShowBannerStatus(showBannerBean);
                // handleShowBean(showBannerBean);
                handleLeftAndRightFrame(position);
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        handleShowBean(showBannerBean);
                        if ((position != 0) && (position != (bannerBeanList.size() - 1))) {
                            CommonUtils.scaleView(leftArrow, 1f);
                            CommonUtils.scaleView(rightArrow, 1f);
                        }

                    }
                }, 1000);


            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void handleLeftAndRightFrame(int position) {
        if (position == 0) {
            videoLeftFrame.setVisibility(View.GONE);
            videoRightFrame.setVisibility(View.VISIBLE);
            leftArrow.setVisibility(View.GONE);
            rightArrow.requestFocus();
        } else if (position == (bannerBeanList.size() - 1)) {
            videoLeftFrame.setVisibility(View.VISIBLE);
            videoRightFrame.setVisibility(View.GONE);
            rightArrow.setVisibility(View.GONE);
            leftArrow.requestFocus();
        } else {
            leftArrow.setVisibility(View.VISIBLE);
            rightArrow.setVisibility(View.VISIBLE);
            videoLeftFrame.setVisibility(View.VISIBLE);
            videoRightFrame.setVisibility(View.VISIBLE);
        }
    }


    private void initShowBannerStatus(ShowBannerBean showBannerBean) {
        videoUrl = "";
        videoView.setVisibility(View.GONE);
        appTitleBg.setVisibility(View.GONE);
        appTitle.setVisibility(View.GONE);
        enterDetail.setFocusable(false);
        enterDetail.setClickable(false);
        enterDetail.setBackgroundResource(R.mipmap.icon_enter_grey);
        if (showBannerBean.getKind().equals("video")) {
            videoUrl = showBannerBean.url;
        }
    }

    private void handleShowBean(ShowBannerBean showBannerBean) {
        if (showBannerBean.getKind().equals("app")) {
            appTitleBg.setVisibility(View.VISIBLE);
            appTitle.setVisibility(View.VISIBLE);
            appTitle.setText(showBannerBean.title);
            enterDetail.setBackgroundResource(R.mipmap.icon_enter);
            enterDetail.setFocusable(true);
            enterDetail.setClickable(true);
            enterDetail.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra("skuId", showBannerBean.skuId);
                startActivity(intent);
            });
        } else {
            enterDetail.setFocusable(false);
            enterDetail.setClickable(false);
            appTitleBg.setVisibility(View.GONE);
            appTitle.setVisibility(View.GONE);
            enterDetail.setBackgroundResource(R.mipmap.icon_enter_grey);
            if (!TextUtils.isEmpty(videoUrl)) {
                playVideo(videoUrl);
            }

        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onPause() {
        super.onPause();
        mViewModel.mldOnPause.setValue(mCategoryIndex);

    }

    @Override
    public void onResume() {
        super.onResume();
        mViewModel.mldOnResume.setValue(mCategoryIndex);


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            if (v.getId() == R.id.enter_detail) {
                CommonUtils.scaleView(v, 1.1f);
            } else {
                CommonUtils.scaleView(v, 1.3f);
            }

        } else {
            CommonUtils.scaleView(v, 1f);
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (v.getId() == R.id.left_arrow_banner && event.getAction() == KeyEvent.ACTION_UP) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
                CommonUtils.scaleView(leftArrow, 1.3f);
                banner.setCurrentItem(banner.getCurrentItem() - 1);
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER && enterDetail.isFocusable()) {
                //enterDetail.isFocusable()加上这个条件是为了限定在轮播图切换播放动画的1s中不允许操作跟以前点击进入按钮保持同步
                handleEnterDetail();
            }
            return true;

        } else if (v.getId() == R.id.right_arrow_banner && event.getAction() == KeyEvent.ACTION_UP) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                CommonUtils.scaleView(rightArrow, 1.3f);
                banner.setCurrentItem(banner.getCurrentItem() + 1);
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER && enterDetail.isFocusable()) {
                handleEnterDetail();
            }

            return true;
        }
        return false;
    }

    private void handleEnterDetail() {
        int position = banner.getCurrentItem();
        if (bannerBeanList.get(position).getKind().equals("app")) {
            Intent intent = new Intent(getActivity(), DetailActivity.class);
            intent.putExtra("skuId", bannerBeanList.get(position).getSkuId());
            startActivity(intent);
        }
    }
}
