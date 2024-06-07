package com.zee.launcher.home.ui.detail;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.danikula.videocache.HttpProxyCacheServer;
import com.danikula.videocache.file.Md5FileNameGenerator;
import com.google.gson.Gson;
import com.zee.launcher.home.BuildConfig;
import com.zee.launcher.home.HomeApplication;
import com.zee.launcher.home.R;
import com.zee.launcher.home.data.DataRepository;
import com.zee.launcher.home.data.protocol.request.CollectReq;
import com.zee.launcher.home.data.protocol.request.PublishReq;
import com.zee.launcher.home.data.protocol.request.RemoveCollectReq;
import com.zee.launcher.home.data.protocol.request.UpgradeReq;
import com.zee.launcher.home.data.protocol.response.AkSkResp;
import com.zee.launcher.home.data.protocol.response.AlgorithmInfoResp;
import com.zee.launcher.home.data.protocol.response.ModelInfoResp;
import com.zee.launcher.home.data.protocol.response.ProDetailResp;
import com.zee.launcher.home.data.protocol.response.PublishResp;
import com.zee.launcher.home.dialog.UpgradeTipDialog;
import com.zee.launcher.home.ui.loading.LoadingPluginActivity;
import com.zee.launcher.home.utils.DownloadHelper;
import com.zee.launcher.home.widgets.MyVideoView;
import com.zee.manager.IZeeManager;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.config.SharePrefer;
import com.zeewain.base.model.DataLoadState;
import com.zeewain.base.model.LoadState;
import com.zeewain.base.ui.BaseActivity;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.DateTimeUtils;
import com.zeewain.base.utils.DensityUtils;
import com.zeewain.base.utils.FileUtils;
import com.zeewain.base.utils.GlideApp;
import com.zeewain.base.utils.NetworkUtil;
import com.zeewain.base.widgets.CustomerFlowLayout;
import com.zeewain.base.widgets.GradientProgressView;
import com.zeewain.base.widgets.LoadingView;
import com.zeewain.base.widgets.NetworkErrView;
import com.zwn.launcher.host.HostManager;
import com.zwn.lib_download.DownloadListener;
import com.zwn.lib_download.DownloadService;
import com.zwn.lib_download.db.CareController;
import com.zwn.lib_download.model.DownloadInfo;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

public class DetailActivity extends BaseActivity implements View.OnFocusChangeListener {
    private static final String TAG = "DetailActivity";
    private final static int REQUEST_CODE_LOGIN = 1000;
    private DetailViewModel detailViewModel;
    private FrameLayout layoutDownloadDetail;
    private ImageView imgProductDetail;
    private LinearLayout llProductDescFrame;
    private ImageView imgCollectDetail, imgLikeDetail;
    private NetworkErrView networkErrViewDetail;
    private LoadingView loadingViewDetail;
    private ConstraintLayout clDetailLayout;
    private ConstraintLayout clCollectShareLayout;
    private TextView txtOffTheShelf;
    private ImageView downloadIcon;
    private TextView downloadPro;
    private GradientProgressView gradientProgressViewDetail;
    private CustomerFlowLayout layoutAttributes;

    private MyVideoView videoView;
    private String videoUrl;
    private ImageButton btnPlayOrPause;
    private AppCompatSeekBar timeSeekBar;
    private TextView tvPlayTime;
    private ProgressBar loadProgress;
    private RelativeLayout rlVideoRoot;

    private String skuId;
    private DownloadService.DownloadBinder downloadBinder = null;
    private boolean isProductRelease = true;
    private String currentFileId;
    private String lastVersion;
    private boolean isClickEnable = false;
    private boolean isAddCollected;
    private boolean isRequest = false;
    private boolean isLastGestureAIActive = false;
    private static final int MSG_DOWNLOAD_ON_PROGRESS = 1;
    private static final int MSG_DOWNLOAD_ON_FAILED = 2;
    private static final int MSG_DOWNLOAD_ON_UPDATE = 3;
    private static final String KEY_PROGRESS = "Progress";
    private boolean isVideoInited = false;
    private final MyHandler handler = new MyHandler(Looper.myLooper(), this);
    private final Runnable runnable = new Runnable() {
        public void run() {
            if (videoView.isPlaying()) {
                int current = videoView.getCurrentPosition();
                timeSeekBar.setProgress(current);
                tvPlayTime.setText(DateTimeUtils.formatToTimeString(videoView.getCurrentPosition()));
            }
            handler.postDelayed(runnable, 500);
        }
    };

    private final DownloadListener downloadListener = new DownloadListener() {
        @Override
        public void onProgress(String fileId, int progress, long loadedSize, long fileSize) {
            if (fileId.equals(currentFileId)) {
                Message message = Message.obtain(handler);
                message.what = MSG_DOWNLOAD_ON_PROGRESS;
                Bundle bundle = new Bundle();
                bundle.putInt(KEY_PROGRESS, progress);
                message.setData(bundle);
                handler.sendMessage(message);
            }
        }

        @Override
        public void onSuccess(String fileId, int type, File file) {
            if (fileId.equals(currentFileId)) {
                handler.sendEmptyMessage(MSG_DOWNLOAD_ON_UPDATE);
            }
        }

        @Override
        public void onFailed(String fileId, int type, int code) {
            if (fileId.equals(currentFileId)) {
                handler.sendEmptyMessage(MSG_DOWNLOAD_ON_FAILED);
                runOnUiThread(() -> {
                    if (!NetworkUtil.isNetworkAvailable(DetailActivity.this)) {
                        showToast("网络连接异常！");
                    }
                });
            }
        }

        @Override
        public void onPaused(String fileId) {
        }

        @Override
        public void onCancelled(String fileId) {
        }

        @Override
        public void onUpdate(String fileId) {
            if (fileId.equals(currentFileId)) {
                handler.sendEmptyMessage(MSG_DOWNLOAD_ON_UPDATE);
            }
        }
    };

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadBinder = (DownloadService.DownloadBinder) service;
            if (downloadBinder != null) {
                downloadBinder.registerDownloadListener(downloadListener);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DensityUtils.autoWidth(getApplication(), this);
        setContentView(R.layout.activity_detail);

        skuId = getIntent().getStringExtra("skuId");
        if (TextUtils.isEmpty(skuId)) {
            finish();
            return;
        }

        bindService();
        bindManagerService();

        DetailViewModelFactory factory = new DetailViewModelFactory(DataRepository.getInstance());
        detailViewModel = new ViewModelProvider(this, factory).get(DetailViewModel.class);

        imgLikeDetail = findViewById(R.id.img_like_detail);
        imgCollectDetail = findViewById(R.id.img_collect_detail);
        llProductDescFrame = findViewById(R.id.layout_detail_desc);
        layoutDownloadDetail = findViewById(R.id.layout_download_detail);
        layoutDownloadDetail.setOnFocusChangeListener(this);
        networkErrViewDetail = findViewById(R.id.networkErrView_detail);
        loadingViewDetail = findViewById(R.id.loadingView_detail);
        clDetailLayout = findViewById(R.id.cl_detail_layout);
        clCollectShareLayout = findViewById(R.id.cl_collect_share_layout);
        txtOffTheShelf = findViewById(R.id.txt_off_the_shelf);

        layoutAttributes = findViewById(R.id.layout_attributes);

        downloadIcon = findViewById(R.id.download_icon);
        downloadPro = findViewById(R.id.download_pro);
        gradientProgressViewDetail = findViewById(R.id.gradient_progress_view_detail);

        imgProductDetail = findViewById(R.id.img_product_detail);
        videoView = findViewById(R.id.video_view);
        rlVideoRoot = findViewById(R.id.rl_video_root);
        btnPlayOrPause = findViewById(R.id.btn_play_or_pause);
        timeSeekBar = findViewById(R.id.time_seekBar);
        tvPlayTime = findViewById(R.id.tv_play_time);
        loadProgress = findViewById(R.id.load_progress);

        imgProductDetail.setVisibility(View.VISIBLE);
        rlVideoRoot.setVisibility(View.GONE);

        initClickListener();
        initViewObservable();

        detailViewModel.initDataReq(skuId);

        if (BuildConfig.FLAVOR == "plugin") {
            if (HostManager.isGestureAiEnable()) {
                isLastGestureAIActive = HostManager.isGestureAIActive();
            }
        }
    }

    private void initClickListener() {
        View backView = findViewById(R.id.view_detail_back);
        backView.setOnClickListener(v -> finish());
        backView.setNextFocusRightId(backView.getId());
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

        layoutDownloadDetail.setOnClickListener(v -> {
            if (!CommonUtils.isUserLogin()) {
                showToast("请先登录才能下载");
                HostManager.gotoLoginPage(v.getContext());
                return;
            }

            if (!isProductRelease) {
                showToast("没有发布版本所以不能下载");
                return;
            }

            if (!isClickEnable) {
                return;
            }

            if (handleAlgorithmLib()) {
                if (detailViewModel.upgradeResp != null) {//处理升级
                    handleUpgrade();
                } else {
                    handleDownload();
                }
            }
        });

        imgLikeDetail.setOnClickListener(v -> showToast(R.string.not_support_now));

        imgCollectDetail.setOnClickListener(v -> {
            if (!CommonUtils.isUserLogin()) {
                showToast("请先登录才能收藏");
                HostManager.gotoLoginPage(v.getContext());
                return;
            }
            //防止暴力点击问题
            if (isRequest) {
                return;
            }

            CollectReq collectReq = new CollectReq(skuId, detailViewModel.proDetailResp.getProductTitle(), detailViewModel.proDetailResp.getUseImgUrl());
            if (!isAddCollected) {
                isRequest = true;
                detailViewModel.addFavorites(collectReq);
            } else if (!TextUtils.isEmpty(skuId)) {
                isRequest = true;
                List<String> tidList = new ArrayList<>();
                tidList.add(skuId);
                RemoveCollectReq removeCollectReq = new RemoveCollectReq(tidList);
                detailViewModel.removeFavorites(removeCollectReq);
            }
        });

        networkErrViewDetail.setRetryClickListener(() -> detailViewModel.initDataReq(skuId));
        rlVideoRoot.setOnClickListener(v -> isVideoPlay(videoView.isPlaying(), 0));
        btnPlayOrPause.setOnClickListener(v -> isVideoPlay(videoView.isPlaying(), 0));
        findViewById(R.id.view_detail_back).setNextFocusDownId(R.id.layout_download_detail);
        imgLikeDetail.setNextFocusUpId(R.id.view_detail_back);
        layoutDownloadDetail.setNextFocusUpId(R.id.view_detail_back);
        layoutDownloadDetail.setNextFocusLeftId(layoutDownloadDetail.getId());
        imgCollectDetail.setNextFocusUpId(R.id.view_detail_back);
    }

    private void initViewObservable() {
        detailViewModel.mldInitLoadState.observe(this, loadState -> {
            if (LoadState.Loading == loadState) {
                networkErrViewDetail.setVisibility(View.GONE);
                clDetailLayout.setVisibility(View.GONE);
                loadingViewDetail.setVisibility(View.VISIBLE);
                loadingViewDetail.startAnim();
            } else if (LoadState.Success == loadState) {
                if (detailViewModel.proDetailResp.getPutawayStatus() == 1 && CommonUtils.isUserLogin()) {
                    detailViewModel.getPublishVersionInfo(new PublishReq(detailViewModel.proDetailResp.getSoftwareCode()));
                    layoutDownloadDetail.setVisibility(View.VISIBLE);
                    detailViewModel.reqFavoriteState(skuId);
                } else {
                    if (detailViewModel.proDetailResp.getPutawayStatus() == 2) {
                        layoutDownloadDetail.setVisibility(View.GONE);
                        clCollectShareLayout.setVisibility(View.GONE);
                        txtOffTheShelf.setVisibility(View.VISIBLE);
                    } else {
                        layoutDownloadDetail.setVisibility(View.VISIBLE);
                        layoutDownloadDetail.requestFocus();
                        clCollectShareLayout.setVisibility(View.VISIBLE);
                        txtOffTheShelf.setVisibility(View.GONE);

                    }
                    clDetailLayout.setVisibility(View.VISIBLE);
                    handleDetailUiUpdate(detailViewModel.proDetailResp);
                    loadingViewDetail.stopAnim();
                    loadingViewDetail.setVisibility(View.GONE);
                }
            } else if (LoadState.Failed == loadState) {
                loadingViewDetail.stopAnim();
                loadingViewDetail.setVisibility(View.GONE);
                networkErrViewDetail.setVisibility(View.VISIBLE);
                clCollectShareLayout.setVisibility(View.GONE);
                layoutDownloadDetail.setVisibility(View.GONE);
            }
        });

        detailViewModel.mPublishState.observe(this, loadState -> {
            if (LoadState.Loading == loadState) {
                loadingViewDetail.setVisibility(View.VISIBLE);
            } else if (LoadState.Success == loadState) {
                clDetailLayout.setVisibility(View.VISIBLE);
                layoutDownloadDetail.requestFocus();
                clCollectShareLayout.setVisibility(View.VISIBLE);
                txtOffTheShelf.setVisibility(View.GONE);

                handleDetailUiUpdate(detailViewModel.proDetailResp);
                loadingViewDetail.stopAnim();
                loadingViewDetail.setVisibility(View.GONE);

                PublishResp publishResp = detailViewModel.publishResp;
                if ((publishResp != null) && (publishResp.getSoftwareInfo() != null)) {
                    isProductRelease = true;
                    currentFileId = publishResp.getSoftwareInfo().getSoftwareCode();
                    lastVersion = publishResp.getSoftwareVersion();

                    String fileSize = FileUtils.FormatFileSizeNoUnit(Long.parseLong(publishResp.getPackageSize()));
                    downloadPro.setText(String.format("下载(%sM)", fileSize));
                    downloadIcon.setImageResource(R.mipmap.icon_download);
                    downloadIcon.setVisibility(View.VISIBLE);
                    gradientProgressViewDetail.setProgress(0);
                    layoutDownloadDetail.setBackgroundResource(R.mipmap.ic_detail_player_loaded_bg);

                    DownloadInfo dbDownloadInfo = CareController.instance.getDownloadInfoByFileId(currentFileId);
                    if (dbDownloadInfo != null) {
                        downloadIcon.setVisibility(View.VISIBLE);
                        if (dbDownloadInfo.status == DownloadInfo.STATUS_SUCCESS) {
                            downloadIcon.setImageResource(R.mipmap.icon_play);
                            downloadIcon.setVisibility(View.VISIBLE);
                            downloadPro.setText("开始体验");
                            gradientProgressViewDetail.setVisibility(View.GONE);
                            layoutDownloadDetail.setBackgroundResource(R.mipmap.ic_detail_player_loaded_bg);
                            if (!dbDownloadInfo.version.equals(lastVersion)) {
                                detailViewModel.getUpgradeVersionInfo(new UpgradeReq(dbDownloadInfo.version, publishResp.getSoftwareInfo().getSoftwareCode()));
                            }
                        } else if (dbDownloadInfo.status == DownloadInfo.STATUS_STOPPED) {
                            downloadIcon.setImageResource(R.mipmap.icon_download_stop);
                            downloadPro.setText("继续下载");
                            layoutDownloadDetail.setBackgroundResource(R.mipmap.ic_detail_player_unload_bg);
                            if (!dbDownloadInfo.version.equals(lastVersion)) {
                                detailViewModel.getUpgradeVersionInfo(new UpgradeReq(dbDownloadInfo.version, publishResp.getSoftwareInfo().getSoftwareCode()));
                            }
                        } else if (dbDownloadInfo.status == DownloadInfo.STATUS_LOADING) {
                            downloadIcon.setImageResource(R.mipmap.icon_download_loading);
                            downloadPro.setText("下载中");
                            layoutDownloadDetail.setBackgroundResource(R.mipmap.ic_detail_player_unload_bg);
                        } else if (dbDownloadInfo.status == DownloadInfo.STATUS_PENDING) {
                            downloadIcon.setImageResource(R.mipmap.icon_download_waiting);
                            downloadPro.setText("等待中");
                            layoutDownloadDetail.setBackgroundResource(R.mipmap.ic_detail_player_unload_bg);
                        }
                        if (dbDownloadInfo.loadedSize > 0) {
                            int progress = (int) ((dbDownloadInfo.loadedSize * 1.0f / dbDownloadInfo.fileSize) * 100);
                            gradientProgressViewDetail.setProgress(progress);
                        }
                    }
                } else {
                    isProductRelease = false;
                    downloadPro.setText("未发布");
                    downloadIcon.setImageResource(R.mipmap.icon_download);
                    downloadIcon.setVisibility(View.GONE);
                    gradientProgressViewDetail.setProgress(0);
                }
            } else if (LoadState.Failed == loadState) {
                loadingViewDetail.stopAnim();
                loadingViewDetail.setVisibility(View.GONE);
                networkErrViewDetail.setVisibility(View.VISIBLE);
            }
        });

        detailViewModel.mUpgradeState.observe(this, loadState -> {
            if (LoadState.Success == loadState) {
                if (detailViewModel.upgradeResp != null) {
                    if (detailViewModel.upgradeResp.isForcible()) {
                        String fileSize = FileUtils.FormatFileSizeNoUnit(Long.parseLong(detailViewModel.upgradeResp.getPackageSize()));
                        downloadIcon.setImageResource(R.mipmap.icon_download);
                        downloadIcon.setVisibility(View.VISIBLE);
                        downloadPro.setText(String.format("更新(%sM)", fileSize));
                    }
                }
            }
        });

        detailViewModel.mCollectListState.observe(this, new Observer<DataLoadState<Boolean>>() {
            @Override
            public void onChanged(DataLoadState<Boolean> dataLoadState) {
                if (LoadState.Success == dataLoadState.loadState) {
                    if (dataLoadState.data) {
                        isAddCollected = true;
                        imgCollectDetail.setBackgroundResource(R.mipmap.ic_detail_collect_selected);
                    } else {
                        isAddCollected = false;
                        imgCollectDetail.setBackgroundResource(R.mipmap.ic_detail_collect);
                    }
                } else if (LoadState.Failed == dataLoadState.loadState) {
                    isAddCollected = false;
                    imgCollectDetail.setBackgroundResource(R.mipmap.ic_detail_collect);
                }
                isRequest = false;
            }
        });

        detailViewModel.mAddCollectState.observe(this, loadState -> {
            if (LoadState.Success == loadState) {
                isAddCollected = true;
                imgCollectDetail.setBackgroundResource(R.mipmap.ic_detail_collect_selected);
                showToast("收藏成功");
            } else if (LoadState.Failed == loadState) {
                isAddCollected = false;
                showToast("收藏失败");
            }
            isRequest = false;
        });

        detailViewModel.mRemoveCollectState.observe(this, loadState -> {
            if (LoadState.Success == loadState) {
                isAddCollected = false;
                imgCollectDetail.setBackgroundResource(R.mipmap.ic_detail_collect);
                showToast("收藏取消");
            } else if (LoadState.Failed == loadState) {
                showToast("收藏取消失败");
            }
            isRequest = false;
        });

    }

    private Context getUseContext() {
        return HostManager.getUseContext(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_CODE_LOGIN == requestCode) {
            detailViewModel.initDataReq(skuId);
        }
    }

    private void handleDetailUiUpdate(ProDetailResp proDetailResp) {
        GlideApp.with(this)
                .load(proDetailResp.getUseImgUrl())
                .into(imgProductDetail);

        TextView txtProductTitleDetail = findViewById(R.id.txt_product_title_detail);
        txtProductTitleDetail.setText(proDetailResp.getProductTitle());

        TextView txtProductSummaryDetail = findViewById(R.id.txt_product_desc);
        if (proDetailResp.getDescription().isEmpty()) {
            findViewById(R.id.layout_detail_desc).setVisibility(View.INVISIBLE);
        } else txtProductSummaryDetail.setText(Html.fromHtml(proDetailResp.getDescription()));

        List<ProDetailResp.SpecificationsInfo> specifications = proDetailResp.getSpecifications();
        List<String> attributesValue = new ArrayList<>();
        for (int j = 0; j < specifications.size(); j++) {
            List<ProDetailResp.SpecificationsInfo.AttributesInfo> attributes = specifications.get(j).getAttributes();
            for (int i = 0; i < attributes.size(); i++) {
                String value = attributes.get(i).getAttributeValue();
                attributesValue.add(value);
            }
        }
        if (attributesValue.size() > 0) {
            layoutAttributes.removeAllViews();
            layoutAttributes.setTags(attributesValue);
        }

        videoUrl = proDetailResp.getTutorial().getVideoUrl();

        readyPlayVideo();
    }

    private void readyPlayVideo() {
        if (!TextUtils.isEmpty(videoUrl)) {
            HttpProxyCacheServer proxy = HomeApplication.getProxy(this);
            if(!NetworkUtil.isNetworkAvailable(this) && !proxy.isCached(videoUrl)) {
                return;
            }
            rlVideoRoot.setVisibility(View.VISIBLE);
            loadProgress.setVisibility(View.VISIBLE);

            if(proxy.isCached(videoUrl)){//for file damage by outage;
                File cacheDir = new File(getExternalCacheDir(), "video-cache");
                String fileName = new Md5FileNameGenerator().generate(videoUrl);
                File file = new File(cacheDir, fileName);
                if(file.exists() && file.length() == 0){
                    file.delete();
                }
            }

            String proxyUrl = proxy.getProxyUrl(videoUrl);

            rlVideoRoot.setVisibility(View.VISIBLE);
            loadProgress.setVisibility(View.VISIBLE);
            initVideo(proxyUrl);
        }
    }

    private void initVideo(final String url) {
        timeSeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        videoView.setOnCompletionListener(mp -> { });

        videoView.setOnErrorListener((mp, what, extra) -> {
            showToast("播放视频出错");
            HttpProxyCacheServer proxy = HomeApplication.getProxy(this);
            if(videoUrl != null && proxy.isCached(videoUrl)){
                File cacheDir = new File(getExternalCacheDir(), "video-cache");
                String fileName = new Md5FileNameGenerator().generate(videoUrl);
                File file = new File(cacheDir, fileName);
                if(file.exists()){
                    file.delete();
                }
            }
            loadProgress.setVisibility(View.GONE);
            return true;
        });

        videoView.setVideoPath(url);
        videoView.setDrawingCacheEnabled(true);
        isVideoInited = true;
        TextView tvTotalTime = findViewById(R.id.tv_total_time);
        videoView.setOnPreparedListener(mp -> {
            if(isWindowOnFocus) {
                mp.setLooping(true);
                int totalTime = videoView.getDuration();//获取视频的总时长
                tvTotalTime.setText(stringForTime(totalTime));
                // 开始线程，更新进度条的刻度
                handler.removeCallbacks(runnable);
                handler.postDelayed(runnable, 0);
                timeSeekBar.setMax(videoView.getDuration());
                //视频加载完成,准备好播放视频的回调
                videoView.start();
                if(btnPlayOrPause.getVisibility() == View.VISIBLE) {
                    timeGone();
                }
                mp.setOnInfoListener((mp1, what, extra) -> {
                    //第一帧正式渲染
                    if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                        videoView.setBackgroundColor(Color.TRANSPARENT);
                        loadProgress.setVisibility(View.GONE);
                        imgProductDetail.setVisibility(View.GONE);
                    }
                    return true;
                });
            }
        });
    }

    private void timeGone() {
        handler.postDelayed(() -> btnPlayOrPause.setVisibility(View.GONE), 1500);
    }

    private final SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int progress = seekBar.getProgress();
            if (videoView.isPlaying()) {
                videoView.seekTo(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) { }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { }
    };

    private String stringForTime(int timeMs) {
        StringBuilder mFormatBuilder = new StringBuilder();
        Formatter mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());
        int totalSeconds = timeMs / 1000;
        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;
        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    private void isVideoPlay(boolean isPlay, int keys) {
        if (keys == 0) {
            if (isPlay) {//暂停
                btnPlayOrPause.setBackgroundResource(R.mipmap.icon_player);
                ((ImageView)findViewById(R.id.btn_play_small)).setImageResource(R.mipmap.icon_player_small);
                btnPlayOrPause.setVisibility(View.VISIBLE);
                videoView.pause();
            } else {//继续播放
                btnPlayOrPause.setBackgroundResource(R.mipmap.icon_pause);
                ((ImageView)findViewById(R.id.btn_play_small)).setImageResource(R.mipmap.icon_pause);
                btnPlayOrPause.setVisibility(View.VISIBLE);
                // 开始线程，更新进度条的刻度
                handler.removeCallbacks(runnable);
                handler.postDelayed(runnable, 0);
                videoView.start();
                timeSeekBar.setMax(videoView.getDuration());
                timeGone();
            }
        }
    }

    private boolean handleAlgorithmLib() {
        if (downloadBinder != null) {
            List<AlgorithmInfoResp> algorithmInfoList = detailViewModel.publishResp.getRelevancyAlgorithmVersions();
            if (detailViewModel.upgradeResp != null) {
                algorithmInfoList = detailViewModel.upgradeResp.getRelevancyAlgorithmVersions();//是否应该并集？
            }
            if (algorithmInfoList != null) {
                for (int i = 0; i < algorithmInfoList.size(); i++) {
                    AlgorithmInfoResp algorithmInfoResp = algorithmInfoList.get(i);
                    boolean successHandle = handleModelBin(algorithmInfoResp.relevancyModelVersions);
                    if (!successHandle) {
                        return false;
                    }
                    DownloadInfo downloadAlgorithm = CareController.instance.getDownloadInfoByFileId(algorithmInfoResp.versionId);
                    if (downloadAlgorithm == null) {
                        boolean addOk = downloadBinder.startDownload(DownloadHelper.buildAlgorithmDownloadInfo(getUseContext(), algorithmInfoResp));
                        if (!addOk) {
                            showToast("添加失败");
                            return false;
                        }
                    } else if (downloadAlgorithm.status == DownloadInfo.STATUS_STOPPED) {
                        downloadBinder.startDownload(downloadAlgorithm.fileId);
                    }
                }
            }
            return true;
        }
        return false;
    }

    private boolean handleModelBin(List<ModelInfoResp> relatedModelList) {
        if (relatedModelList != null) {
            for (int i = 0; i < relatedModelList.size(); i++) {
                ModelInfoResp modelInfoResp = relatedModelList.get(i);
                DownloadInfo downloadModel = CareController.instance.getDownloadInfoByFileId(modelInfoResp.versionId);
                if (downloadModel == null) {
                    boolean addOk = downloadBinder.startDownload(DownloadHelper.buildModelDownloadInfo(getUseContext(), modelInfoResp));
                    if (!addOk) {
                        showToast("添加失败");
                        return false;
                    }
                } else if (downloadModel.status == DownloadInfo.STATUS_STOPPED) {
                    downloadBinder.startDownload(downloadModel.fileId);
                }
            }
        }
        return true;
    }

    private void handleUpgrade() {
        if (detailViewModel.upgradeResp.isForcible()) {
            DownloadInfo downloadInfo = DownloadHelper.buildUpgradeDownloadInfo(getUseContext(), detailViewModel.proDetailResp, detailViewModel.publishResp, detailViewModel.upgradeResp);
            boolean success = downloadBinder.startDownload(downloadInfo);
            if (success) {
                detailViewModel.upgradeResp = null;
            }
        } else {
            showUpgradeDialog();
        }
    }

    private void handleDownload() {
        if (downloadBinder != null) {
            DownloadInfo dbDownloadInfo = CareController.instance.getDownloadInfoByFileId(currentFileId);
            if (dbDownloadInfo == null) {
                DownloadInfo downloadInfo = DownloadHelper.buildDownloadInfo(getUseContext(), detailViewModel.proDetailResp, detailViewModel.publishResp);
                downloadBinder.startDownload(downloadInfo);
            } else {
                if (dbDownloadInfo.status == DownloadInfo.STATUS_LOADING || dbDownloadInfo.status == DownloadInfo.STATUS_PENDING) {
                    downloadBinder.pauseDownload(dbDownloadInfo.fileId);
                } else if (dbDownloadInfo.status == DownloadInfo.STATUS_STOPPED) {
                    downloadBinder.startDownload(dbDownloadInfo);
                } else {
                    String lastPluginPackageName = HostManager.getLastPluginPackageName();
                    if (null != lastPluginPackageName
                            && !dbDownloadInfo.mainClassPath.equals(lastPluginPackageName)) {
                        removeRecentTask(lastPluginPackageName);
                    }
                    startLoadingApplication();
                }
            }
        }
    }

    private void updateDownloadTip() {
        DownloadInfo dbDownloadInfo = CareController.instance.getDownloadInfoByFileId(currentFileId);
        if (dbDownloadInfo == null) {
            downloadIcon.setVisibility(View.VISIBLE);
            downloadIcon.setImageResource(R.mipmap.icon_download);
            downloadPro.setText("下载");
            layoutDownloadDetail.setBackgroundResource(R.mipmap.ic_detail_player_unload_bg);
        } else {
            if (dbDownloadInfo.status == DownloadInfo.STATUS_LOADING) {
                downloadIcon.setVisibility(View.VISIBLE);
                downloadIcon.setImageResource(R.mipmap.icon_download_loading);
                downloadPro.setText("下载中");
                layoutDownloadDetail.setBackgroundResource(R.mipmap.ic_detail_player_unload_bg);
            } else if (dbDownloadInfo.status == DownloadInfo.STATUS_PENDING) {
                downloadIcon.setVisibility(View.VISIBLE);
                downloadIcon.setImageResource(R.mipmap.icon_download_waiting);
                downloadPro.setText("等待中");
                layoutDownloadDetail.setBackgroundResource(R.mipmap.ic_detail_player_unload_bg);
            } else if (dbDownloadInfo.status == DownloadInfo.STATUS_STOPPED) {
                downloadIcon.setVisibility(View.VISIBLE);
                downloadIcon.setImageResource(R.mipmap.icon_download_stop);
                downloadPro.setText("继续下载");
                layoutDownloadDetail.setBackgroundResource(R.mipmap.ic_detail_player_unload_bg);
            } else if (dbDownloadInfo.status == DownloadInfo.STATUS_SUCCESS) {
                downloadIcon.setImageResource(R.mipmap.icon_play);
                downloadPro.setText("开始体验");
                gradientProgressViewDetail.setVisibility(View.GONE);
                layoutDownloadDetail.setBackgroundResource(R.mipmap.ic_detail_player_loaded_bg);
            }
        }
    }

    private void updateDownloadTipOnFailed() {
        downloadIcon.setImageResource(R.mipmap.icon_download_stop);
        downloadPro.setText("继续下载");
    }

    private void updateDownloadTip(int progress) {
        downloadIcon.setVisibility(View.VISIBLE);
        downloadIcon.setImageResource(R.mipmap.icon_download_loading);
        downloadPro.setText(String.format("下载中(%s%%)", progress));
        gradientProgressViewDetail.setProgress(progress);
    }

    private void showUpgradeDialog() {
        final UpgradeTipDialog upgradeDialog = new UpgradeTipDialog(this);
        upgradeDialog.show();
        upgradeDialog.setMessageText("检测到新版本");
        upgradeDialog.setMessageText("V" + detailViewModel.upgradeResp.getSoftwareVersion());
        upgradeDialog.setPositiveText("立即更新");
        upgradeDialog.setCancelText("继续");
        upgradeDialog.setOnClickListener(new UpgradeTipDialog.OnClickListener() {
            @Override
            public void onConfirm(View v) {
            }

            @Override
            public void onPositive(View v) {
                upgradeDialog.cancel();
                DownloadInfo downloadInfo = DownloadHelper.buildUpgradeDownloadInfo(getUseContext(), detailViewModel.proDetailResp,
                        detailViewModel.publishResp, detailViewModel.upgradeResp);
                boolean success = downloadBinder.startDownload(downloadInfo);
                if (success) {
                    detailViewModel.upgradeResp = null;
                }
            }

            @Override
            public void onCancel(View v) {
                upgradeDialog.cancel();
                handleDownload();
            }
        });
    }

    @Override
    protected void onPause() {
        isClickEnable = false;
        if (videoView.isPlaying()){
            videoView.pause();
            imgProductDetail.setVisibility(View.VISIBLE);
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        isClickEnable = true;
        super.onResume();
        //handle come back from user center, user may delete the apk
        PublishResp publishResp = detailViewModel.publishResp;
        if ((publishResp != null) && (publishResp.getSoftwareInfo() != null) && currentFileId != null) {
            DownloadInfo dbDownloadInfo = CareController.instance.getDownloadInfoByFileId(currentFileId);
            if (dbDownloadInfo == null) {
                String fileSize = FileUtils.FormatFileSizeNoUnit(Long.parseLong(publishResp.getPackageSize()));
                downloadPro.setText(String.format("下载(%sM)", fileSize));
                downloadIcon.setImageResource(R.mipmap.icon_download);
                downloadIcon.setVisibility(View.VISIBLE);
                gradientProgressViewDetail.setProgress(0);
            }
        }

        if(rlVideoRoot.getVisibility() == View.VISIBLE && isVideoInited){
            videoView.resume();
        }

        if (HostManager.isGestureAiEnable()) {
            HostManager.startGestureAi(isLastGestureAIActive);
        }
    }

    boolean isWindowOnFocus = true;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        isWindowOnFocus = hasFocus;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String newSkuId = intent.getStringExtra("skuId");
        if (newSkuId != null && !newSkuId.isEmpty()) {
            currentFileId = null;
            handler.removeCallbacksAndMessages(null);
            handler.removeCallbacksAndMessages(null);

            if (videoView.isPlaying()){
                videoView.pause();
                imgProductDetail.setVisibility(View.VISIBLE);
                videoView.setOnPreparedListener(null);
                videoView.setOnCompletionListener(null);
                videoView.setOnErrorListener(null);
                videoView.destroyDrawingCache();
            }

            skuId = newSkuId;
            videoUrl = null;
            lastVersion = null;

            detailViewModel.initDataReq(skuId);
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);

        if (downloadBinder != null) {
            downloadBinder.unRegisterDownloadListener(downloadListener);
            HostManager.getUseContext(this).unbindService(serviceConnection);
            downloadBinder = null;
        }

        if (zeeManager != null) {
            unbindManagerService();
            zeeManager = null;
        }

        videoView.setOnPreparedListener(null);
        videoView.setOnCompletionListener(null);
        videoView.setOnErrorListener(null);
        videoView.destroyDrawingCache();
        rlVideoRoot.removeAllViews();

        super.onDestroy();
    }

    private void startLoadingApplication() {
        String akSkInfoString = HostManager.getHostSpString(SharePrefer.akSkInfo, null);
        if (akSkInfoString != null && !akSkInfoString.isEmpty()) {
            Gson gson = new Gson();
            AkSkResp akSkResp = gson.fromJson(akSkInfoString, AkSkResp.class);
            if (akSkResp != null) {
                if (HostManager.isGestureAiEnable()) {
                    isLastGestureAIActive = HostManager.isGestureAIActive();
                }
                Intent intent = new Intent();
                intent.putExtra(BaseConstants.EXTRA_AUTH_AK_CODE, akSkResp.akCode);
                intent.putExtra(BaseConstants.EXTRA_AUTH_SK_CODE, akSkResp.skCode);
                intent.putExtra(BaseConstants.EXTRA_SKU_NAME, detailViewModel.proDetailResp.getProductTitle());
                intent.putExtra(BaseConstants.EXTRA_SKU_URL, detailViewModel.proDetailResp.getUseImgUrl());
                intent.putExtra(BaseConstants.EXTRA_PLUGIN_NAME, currentFileId);
                intent.setClass(this, LoadingPluginActivity.class);
                startActivity(intent);
            }
        }
    }

    private void bindService() {
        /*Intent bindIntent = new Intent(this.getApplicationContext(), DownloadService.class);
        bindService(bindIntent, serviceConnection, BIND_AUTO_CREATE);*/

        Intent bindIntent = new Intent();
        bindIntent.setComponent(new ComponentName(HostManager.getUseContext(this).getPackageName(), DownloadService.class.getName()));
        HostManager.getUseContext(this).bindService(bindIntent, serviceConnection, BIND_AUTO_CREATE);

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
            Log.i(TAG, "onServiceConnected()");
            zeeManager = IZeeManager.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "onServiceDisconnected()");
            zeeManager = null;
        }
    };

    public void removeRecentTask(String packageName) {
        if (zeeManager != null) {
            try {
                zeeManager.removeRecentTask(packageName);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("ResourceType")
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (v.getId() == R.id.layout_download_detail) {
            if (hasFocus) {
                findViewById(R.id.view_player_background).setVisibility(View.VISIBLE);
            } else findViewById(R.id.view_player_background).setVisibility(View.GONE);
        }

    }

    public static class MyHandler extends Handler {
        private final WeakReference<Activity> mActivity;

        public MyHandler(Looper looper, Activity activity) {
            super(looper);
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Activity activity = mActivity.get();
            if (activity != null) {
                if (msg.what == MSG_DOWNLOAD_ON_PROGRESS) {
                    int progress = msg.getData().getInt(KEY_PROGRESS);
                    ((DetailActivity) activity).updateDownloadTip(progress);
                } else if (msg.what == MSG_DOWNLOAD_ON_FAILED) {
                    ((DetailActivity) activity).updateDownloadTipOnFailed();
                } else if (msg.what == MSG_DOWNLOAD_ON_UPDATE) {
                    ((DetailActivity) activity).updateDownloadTip();
                }
            }
        }
    }
}
