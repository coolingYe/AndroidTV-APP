package com.zee.launcher.home.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.danikula.videocache.CacheListener;
import com.danikula.videocache.HttpProxyCacheServer;
import com.zee.launcher.home.HomeApplication;
import com.zee.launcher.home.data.DataRepository;
import com.zee.launcher.home.data.model.ProductListMo;
import com.zee.launcher.home.data.protocol.request.ProDetailReq;
import com.zee.launcher.home.data.protocol.request.ProductRecommendReq;
import com.zee.launcher.home.data.protocol.request.PublishReq;
import com.zee.launcher.home.data.protocol.request.UpgradeReq;
import com.zee.launcher.home.data.protocol.response.AlgorithmInfoResp;
import com.zee.launcher.home.data.protocol.response.FavoriteStateResp;
import com.zee.launcher.home.data.protocol.response.ModelInfoResp;
import com.zee.launcher.home.data.protocol.response.ProDetailResp;
import com.zee.launcher.home.data.protocol.response.PublishResp;
import com.zee.launcher.home.data.protocol.response.UpgradeResp;
import com.zee.launcher.home.utils.DownloadHelper;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.data.protocol.response.BaseResp;
import com.zeewain.base.utils.CareLog;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.GlideApp;
import com.zwn.launcher.host.HostManager;
import com.zwn.lib_download.DownloadService;
import com.zwn.lib_download.db.CareController;
import com.zwn.lib_download.model.DownloadInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class TopicService extends Service {
    private static final String TAG = TopicService.class.getSimpleName();
    private static TopicService instance = null;
    private static Context appContext;
    private DownloadService.DownloadBinder downloadBinder = null;
    private final TopicServiceBinder topicServiceBinder = new TopicServiceBinder();
    private static final ConcurrentLinkedQueue<String> skuIdCacheQueue = new ConcurrentLinkedQueue<>();
    private static final ConcurrentLinkedQueue<String> failedSkuIdCacheQueue = new ConcurrentLinkedQueue<>();
    private static final ConcurrentLinkedQueue<String> detailVideoUrlCacheQueue = new ConcurrentLinkedQueue<>();
    private static final ConcurrentLinkedQueue<String> failedDetailVideoUrlCacheQueue = new ConcurrentLinkedQueue<>();
    private static final ConcurrentLinkedQueue<String> detailImageUrlCacheQueue = new ConcurrentLinkedQueue<>();
    private static final ConcurrentLinkedQueue<String> failedDetailImageUrlCacheQueue = new ConcurrentLinkedQueue<>();
    private final List<String> currentSkuIdList = new ArrayList<>(1);
    private List<String> extraImageList = new ArrayList<>(1);
    private List<String> extraVideoList = new ArrayList<>(1);
    private final HashMap<String, String> detailVideoUrlMap = new HashMap<>();
    private final HashMap<String, String> detailImageUrlMap = new HashMap<>();
    private String processingSkuId = null;
    private String processingVideoUrl = null;
    private String processingImageUrl = null;
    private final HashMap<String, String> relayLibFileIdMap = new HashMap<>();
    private final HashMap<String, String> relayModelBinFileIdMap = new HashMap<>();

    private MediaPlayer mediaPlayer;
    private HttpProxyCacheServer httpProxyCacheServer;

    private static final int MSG_VIDEO_CACHED_DONE = 1000;
    private static final int MSG_VIDEO_CACHING_PENDING = 1001;
    private static final String KEY_CACHING_VIDEO_URL = "VideoUrl";
    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_VIDEO_CACHED_DONE) {
                if (httpProxyCacheServer != null && mediaPlayer != null) {
                    mediaPlayer.reset();
                    httpProxyCacheServer.unregisterCacheListener(videoCacheListener, processingVideoUrl);
                    processingVideoUrl = null;
                    preloadProductDetailVideo();
                }
            } else if (msg.what == MSG_VIDEO_CACHING_PENDING) {
                if (processingVideoUrl != null) {
                    String pendingUrl = msg.getData().getString(KEY_CACHING_VIDEO_URL);
                    CareLog.e(TAG, "MSG_VIDEO_CACHING_PENDING pendingUrl=" + pendingUrl);
                    if (processingVideoUrl.equals(pendingUrl)) {
                        if (mediaPlayer != null) {
                            mediaPlayer.reset();
                        }
                        failedDetailVideoUrlCacheQueue.addAll(detailVideoUrlCacheQueue);
                        detailVideoUrlCacheQueue.clear();
                        if (!failedDetailVideoUrlCacheQueue.contains(processingVideoUrl)) {
                            failedDetailVideoUrlCacheQueue.add(processingVideoUrl);
                        }
                        processingVideoUrl = null;
                    }
                }
            }
        }
    };

    private final ServiceConnection downloadServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadBinder = (DownloadService.DownloadBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            downloadBinder = null;
        }
    };

    private void bindDownloadService() {
        Intent bindIntent = new Intent();
        bindIntent.setComponent(new ComponentName(HostManager.getUseContext(this).getPackageName(), DownloadService.class.getName()));
        HostManager.getUseContext(this).bindService(bindIntent, downloadServiceConnection, BIND_AUTO_CREATE);
    }

    private void unBindDownloadService() {
        if (downloadBinder != null) {
            HostManager.getUseContext(this).unbindService(downloadServiceConnection);
            downloadBinder = null;
        }
    }

    public static class TopicServiceBinder extends Binder {

    }

    public TopicService() {
        CareLog.i(TAG, "TopicService(), TopicService in construction!");
    }

    public static TopicService getInstance() {
        return instance;
    }

    public static void initTopicService(Context context) {
        CareLog.i(TAG, "initTopicService()");
        appContext = context.getApplicationContext();
        Intent intent = new Intent(appContext, TopicService.class);
        appContext.getApplicationContext().bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {

            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        }, BIND_AUTO_CREATE);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        CareLog.i(TAG, "onCreate()");
        instance = this;
        bindDownloadService();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return topicServiceBinder;
    }

    @Override
    public void onDestroy() {
        CareLog.i(TAG, "onDestroy()");
        unBindDownloadService();
        instance = null;
        appContext = null;

        handler.removeCallbacksAndMessages(null);

        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        currentSkuIdList.clear();
        detailVideoUrlMap.clear();
        detailImageUrlMap.clear();
        detailVideoUrlCacheQueue.clear();
        failedDetailVideoUrlCacheQueue.clear();
        detailImageUrlCacheQueue.clear();
        failedDetailImageUrlCacheQueue.clear();
        relayLibFileIdMap.clear();
        relayModelBinFileIdMap.clear();

        super.onDestroy();
    }

    public DownloadService.DownloadBinder getDownloadBinder() {
        return downloadBinder;
    }

    public void initSkuIdCacheQueue(List<String> skuIdList, List<String> extraImageList, List<String> extraVideoList) {
        if ((skuIdCacheQueue.size() == 0 && failedSkuIdCacheQueue.size() == 0 && processingSkuId == null)
                && (detailVideoUrlCacheQueue.size() == 0 && failedDetailVideoUrlCacheQueue.size() == 0 && processingVideoUrl == null)
                && (detailImageUrlCacheQueue.size() == 0 && failedDetailImageUrlCacheQueue.size() == 0 && processingImageUrl == null)) {
            currentSkuIdList.clear();
            detailVideoUrlMap.clear();
            detailImageUrlMap.clear();
            relayLibFileIdMap.clear();
            relayModelBinFileIdMap.clear();
            for (String skuId : skuIdList) {
                if (!skuIdCacheQueue.contains(skuId)) {
                    currentSkuIdList.add(skuId);
                    skuIdCacheQueue.offer(skuId);
                }
            }

            this.extraImageList = extraImageList;
            this.extraVideoList = extraVideoList;

            new Thread(() -> {
                processingSkuId = skuIdCacheQueue.poll();
                if (processingSkuId != null) {
                    reqFavoriteState(processingSkuId);
                }
            }).start();
        }
    }

    public void clearQueueAndMapData() {
        skuIdCacheQueue.clear();
        failedSkuIdCacheQueue.clear();
        processingSkuId = null;
        detailVideoUrlCacheQueue.clear();
        failedDetailVideoUrlCacheQueue.clear();
        processingVideoUrl = null;
        detailImageUrlCacheQueue.clear();
        failedDetailImageUrlCacheQueue.clear();
        processingImageUrl = null;

        detailVideoUrlMap.clear();
        detailImageUrlMap.clear();
    }

    public void handStopDownloadList(final List<DownloadInfo> stopDownloadInfoList) {
        new Thread(() -> {
            for (DownloadInfo downloadInfo : stopDownloadInfoList) {
                for (String skuId : currentSkuIdList) {
                    if (skuId.equals(downloadInfo.extraId)) {
                        getDownloadBinder().startDownload(downloadInfo.fileId);
                    }
                }
            }
        }).start();
    }

    public void handNetDisconnect() {
        if (processingSkuId != null) {
            failedSkuIdCacheQueue.addAll(skuIdCacheQueue);
            skuIdCacheQueue.clear();
            if (!failedSkuIdCacheQueue.contains(processingSkuId)) {
                failedSkuIdCacheQueue.add(processingSkuId);
            }
            processingSkuId = null;
        }

        if (processingImageUrl != null) {
            failedDetailImageUrlCacheQueue.addAll(detailImageUrlCacheQueue);
            detailImageUrlCacheQueue.clear();
            if (!failedDetailImageUrlCacheQueue.contains(processingImageUrl)) {
                failedDetailImageUrlCacheQueue.add(processingImageUrl);
            }
            processingImageUrl = null;
        }

        if (processingVideoUrl != null) {
            if (mediaPlayer != null) {
                mediaPlayer.reset();
            }
            failedDetailVideoUrlCacheQueue.addAll(detailVideoUrlCacheQueue);
            detailVideoUrlCacheQueue.clear();
            if (!failedDetailVideoUrlCacheQueue.contains(processingVideoUrl)) {
                failedDetailVideoUrlCacheQueue.add(processingVideoUrl);
            }
            processingVideoUrl = null;
        }
    }

    public void retryFailedData() {
        if (failedSkuIdCacheQueue.size() > 0) {
            skuIdCacheQueue.addAll(failedSkuIdCacheQueue);
            failedSkuIdCacheQueue.clear();
        }

        if (failedDetailImageUrlCacheQueue.size() > 0) {
            detailImageUrlCacheQueue.addAll(failedDetailImageUrlCacheQueue);
            failedDetailImageUrlCacheQueue.clear();
        }

        if (failedDetailVideoUrlCacheQueue.size() > 0) {
            detailVideoUrlCacheQueue.addAll(failedDetailVideoUrlCacheQueue);
            failedDetailVideoUrlCacheQueue.clear();
        }

        processingSkuId = skuIdCacheQueue.poll();
        if (processingSkuId != null) {
            reqFavoriteState(processingSkuId);
        } else {
            processingImageUrl = null;
            preloadProductDetailImage();
        }
    }

    @SuppressLint("DefaultLocale")
    public String getCachingString() {
        if (processingSkuId != null || skuIdCacheQueue.size() > 0) {
            return String.format("正在缓存数据（%d/%d）", (currentSkuIdList.size() - skuIdCacheQueue.size()), currentSkuIdList.size());
        } else if (processingImageUrl != null || detailImageUrlCacheQueue.size() > 0) {
            return String.format("正在缓存图片资源（%d/%d）", (detailImageUrlMap.size() - detailImageUrlCacheQueue.size()), detailImageUrlMap.size());
        } else if (processingVideoUrl != null || detailVideoUrlCacheQueue.size() > 0) {
            return String.format("正在缓存视频资源（%d/%d）", (detailVideoUrlMap.size() - detailVideoUrlCacheQueue.size()), detailVideoUrlMap.size());
        } else {
            return "";
        }
    }

    public boolean hasCachingFailedData() {
        return failedSkuIdCacheQueue.size() > 0 || failedDetailImageUrlCacheQueue.size() > 0 || failedDetailVideoUrlCacheQueue.size() > 0;
    }

    private void sendStartDownloadMsg(String uid, String softwareCode, int status, String errMsg) {
        CareLog.i(TAG, "sendStartDownloadMsg() uid=" + uid + ", softwareCode=" + softwareCode + ", status=" + status + ", errMsg=" + errMsg);
        if (StartDownloadStatus.FAILED == status) {
            failedSkuIdCacheQueue.offer(uid);
        }

        processingSkuId = skuIdCacheQueue.poll();
        if (processingSkuId != null) {
            reqFavoriteState(processingSkuId);
        } else {
            if (extraImageList != null) {
                for (String imageUrl : extraImageList) {
                    String useSkuId = String.valueOf(System.currentTimeMillis());
                    if (!isImageUrlContainsInQueue(imageUrl) && !detailImageUrlMap.containsKey(useSkuId)) {
                        detailImageUrlMap.put(useSkuId, imageUrl);
                        detailImageUrlCacheQueue.offer(imageUrl);
                    }
                }
            }
            processingImageUrl = null;
            preloadProductDetailImage();
        }
    }

    private boolean isVideoUrlContainsInQueue(String url) {
        return url.equals(processingVideoUrl) || detailVideoUrlCacheQueue.contains(url) || failedDetailVideoUrlCacheQueue.contains(url);
    }

    private boolean isImageUrlContainsInQueue(String url) {
        return url.equals(processingImageUrl) || detailImageUrlCacheQueue.contains(url) || failedDetailImageUrlCacheQueue.contains(url);
    }

    private final CacheListener videoCacheListener = (cacheFile, url, percentsAvailable) -> {
        if (percentsAvailable % 5 == 0) {
            CareLog.i(TAG, "onCacheAvailable url=" + url + ", percentsAvailable=" + percentsAvailable);
            handler.removeMessages(MSG_VIDEO_CACHING_PENDING);
            Message message = Message.obtain(handler);
            message.what = MSG_VIDEO_CACHING_PENDING;
            Bundle bundle = new Bundle();
            bundle.putString(KEY_CACHING_VIDEO_URL, url);
            message.setData(bundle);
            handler.sendEmptyMessageDelayed(MSG_VIDEO_CACHING_PENDING, 120 * 1000);
        }
        if (percentsAvailable == 100) {
            handler.removeMessages(MSG_VIDEO_CACHING_PENDING);
            handler.removeMessages(MSG_VIDEO_CACHED_DONE);
            handler.sendEmptyMessageDelayed(MSG_VIDEO_CACHED_DONE, 200);
        }
    };

    private synchronized void preloadProductDetailVideo() {
        if (processingVideoUrl != null) return;

        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnPreparedListener(mp -> {
                CareLog.i(TAG, "preloadProductDetailVideo onPrepared() processingVideoUrl=" + processingVideoUrl);
            });

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                CareLog.e(TAG, "preloadProductDetailVideo onError() what=" + what + ", processingVideoUrl=" + processingVideoUrl);
                if (processingVideoUrl != null && !failedDetailVideoUrlCacheQueue.contains(processingVideoUrl)) {
                    failedDetailVideoUrlCacheQueue.offer(processingVideoUrl);
                }
                handler.removeMessages(MSG_VIDEO_CACHED_DONE);
                handler.sendEmptyMessageDelayed(MSG_VIDEO_CACHED_DONE, 200);
                return false;
            });
        }

        if (httpProxyCacheServer == null) {
            httpProxyCacheServer = HomeApplication.getProxy(this);
        }

        processingVideoUrl = detailVideoUrlCacheQueue.poll();

        CareLog.i(TAG, "preloadProductDetailVideo() processingVideoUrl=" + processingVideoUrl);

        if (processingVideoUrl != null) {
            if (!httpProxyCacheServer.isCached(processingVideoUrl)) {
                try {
                    httpProxyCacheServer.registerCacheListener(videoCacheListener, processingVideoUrl);
                    mediaPlayer.setDataSource(httpProxyCacheServer.getProxyUrl(processingVideoUrl));
                    mediaPlayer.prepareAsync();
                } catch (Exception e) {
                    CareLog.e(TAG, "preloadProductDetailVideo error() e=" + e + ", processingVideoUrl=" + processingVideoUrl);
                    if (processingVideoUrl != null && !failedDetailVideoUrlCacheQueue.contains(processingVideoUrl)) {
                        failedDetailVideoUrlCacheQueue.offer(processingVideoUrl);
                    }
                    handler.removeMessages(MSG_VIDEO_CACHED_DONE);
                    handler.sendEmptyMessageDelayed(MSG_VIDEO_CACHED_DONE, 200);
                }
            } else {
                processingVideoUrl = null;
                preloadProductDetailVideo();
            }
        } else {
            handler.removeMessages(MSG_VIDEO_CACHING_PENDING);
            CareLog.i(TAG, "preloadProductDetailVideo() Done");
        }
    }

    private synchronized void preloadProductDetailImage() {
        if (processingImageUrl != null) return;

        processingImageUrl = detailImageUrlCacheQueue.poll();
        if (processingImageUrl == null) {//all loaded
            CareLog.i(TAG, "preloadProductDetailImage() Done " + detailImageUrlCacheQueue.size() + ", failedDetailImageUrlCacheQueue=" + failedDetailImageUrlCacheQueue.size());
            if (extraVideoList != null) {
                for (String videoUrl : extraVideoList) {
                    String useSkuId = String.valueOf(System.currentTimeMillis());
                    if (!isVideoUrlContainsInQueue(videoUrl) && !detailVideoUrlMap.containsKey(useSkuId)) {
                        detailVideoUrlMap.put(useSkuId, videoUrl);
                        detailVideoUrlCacheQueue.offer(videoUrl);
                    }
                }
            }
            processingVideoUrl = null;
            preloadProductDetailVideo();
            return;
        }

        CareLog.i(TAG, "preloadProductDetailImage() processingImageUrl=" + processingImageUrl);

        GlideApp.with(this).load(processingImageUrl).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                CareLog.e(TAG, "preloadProductDetailImage() onLoadFailed processingImageUrl=" + processingImageUrl + ", err=" + e);
                if (processingImageUrl != null && !failedDetailImageUrlCacheQueue.contains(processingImageUrl)) {
                    failedDetailImageUrlCacheQueue.offer(processingImageUrl);
                }
                processingImageUrl = null;
                preloadProductDetailImage();
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                processingImageUrl = null;
                preloadProductDetailImage();
                return true;
            }
        }).preload();
    }

    private void reqFavoriteState(final String skuId) {
        DataRepository.getInstance().getFavoriteState(skuId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<BaseResp<FavoriteStateResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<FavoriteStateResp> resp) {
                        reqProductRecommendList(skuId);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        reqProductRecommendList(skuId);
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    private void reqProductRecommendList(final String skuId) {
        DataRepository.getInstance().getProductRecommend(new ProductRecommendReq(skuId, CommonUtils.getDeviceSn(), 12))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<BaseResp<List<ProductListMo.Record>>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<List<ProductListMo.Record>> response) {
                        if (response.code == BaseConstants.API_HANDLE_SUCCESS) {
                            if (response.isCache) {
                                failedSkuIdCacheQueue.add(skuId);
                                failedSkuIdCacheQueue.addAll(skuIdCacheQueue);
                                skuIdCacheQueue.clear();
                                detailImageUrlCacheQueue.clear();
                                detailVideoUrlCacheQueue.clear();
                                processingSkuId = null;
                                processingImageUrl = null;
                                processingVideoUrl = null;
                            } else {
                                reqProductDetailInfo(skuId);
                            }
                        } else {
                            sendStartDownloadMsg(skuId, null, StartDownloadStatus.FAILED, response.message);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        sendStartDownloadMsg(skuId, null, StartDownloadStatus.FAILED, "网络异常！");
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void handleProductDetailResInQueue(String skuId, ProDetailResp proDetailResp) {
        if (proDetailResp.getTutorial() != null && !TextUtils.isEmpty(proDetailResp.getTutorial().getVideoUrl())) {
            String videoUrl = proDetailResp.getTutorial().getVideoUrl();
            if (!detailVideoUrlMap.containsKey(skuId)) {
                detailVideoUrlMap.put(skuId, videoUrl);
                if (!isVideoUrlContainsInQueue(videoUrl)) {
                    detailVideoUrlCacheQueue.offer(videoUrl);
                }
            }
        }
        String imageUrl = proDetailResp.getUseImgUrl();
        if (!TextUtils.isEmpty(imageUrl)) {
            if (!detailImageUrlMap.containsKey(skuId)) {
                detailImageUrlMap.put(skuId, imageUrl);
                if (!isImageUrlContainsInQueue(imageUrl)) {
                    detailImageUrlCacheQueue.offer(imageUrl);
                }
            }
        }
    }

    public void reqProductDetailInfo(final String skuId) {
        DataRepository.getInstance().getProDetailInfo(new ProDetailReq(skuId, CommonUtils.getDeviceSn()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<BaseResp<ProDetailResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<ProDetailResp> response) {
                        if (response.code == BaseConstants.API_HANDLE_SUCCESS && !response.isCache) {
                            if (response.data != null && response.data.getSkuId() != null) {
                                if (response.data.getPutawayStatus() == 1) {
                                    handleProductDetailResInQueue(skuId, response.data);
                                    reqPublishVersionInfo(skuId, response.data);
                                } else {
                                    sendStartDownloadMsg(skuId, null, StartDownloadStatus.READY, ""); //PutawayStatus
                                }
                            } else {
                                sendStartDownloadMsg(skuId, null, StartDownloadStatus.FAILED, "接口返回数据异常！");
                            }
                        } else {
                            sendStartDownloadMsg(skuId, null, StartDownloadStatus.FAILED, response.message);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        sendStartDownloadMsg(skuId, null, StartDownloadStatus.FAILED, "网络异常！");
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    private void reqPublishVersionInfo(final String skuId, ProDetailResp proDetailResp) {
        DataRepository.getInstance().getPublishedVersionInfo(new PublishReq(proDetailResp.getSoftwareCode()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<BaseResp<PublishResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<PublishResp> response) {
                        if (response.code == BaseConstants.API_HANDLE_SUCCESS && !response.isCache) {
                            PublishResp publishResp = response.data;
                            if (publishResp != null && publishResp.getSoftwareInfo() != null) {
                                handlePluginAppPublishResp(skuId, proDetailResp, response.data, true);
                            } else {
                                sendStartDownloadMsg(skuId, null, StartDownloadStatus.FAILED, "接口返回数据异常！");
                            }
                        } else {
                            if ("该软件不存在或已删除".equals(response.message)) {
                                sendStartDownloadMsg(skuId, null, StartDownloadStatus.READY, response.message);
                            } else {
                                sendStartDownloadMsg(skuId, null, StartDownloadStatus.FAILED, response.message);
                            }
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        sendStartDownloadMsg(skuId, null, StartDownloadStatus.FAILED, "网络异常！");
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    private void handlePluginAppPublishResp(final String uid, ProDetailResp proDetailResp, PublishResp publishResp, boolean isCareUpgrade) {
        final String currentFileId = publishResp.getSoftwareInfo().getSoftwareCode();
        final String lastVersion = publishResp.getSoftwareVersion();
        DownloadInfo dbDownloadInfo = CareController.instance.getDownloadInfoByFileId(currentFileId);
        if (dbDownloadInfo != null) {
            if (dbDownloadInfo.status == DownloadInfo.STATUS_SUCCESS) {
                if (isCareUpgrade && !dbDownloadInfo.version.equals(lastVersion)) {
                    getPluginAppLatestVersion(uid, dbDownloadInfo.version, proDetailResp, publishResp);
                } else {//版本相同，监测算法依赖、模型依赖更新
                    if (!handleAlgorithmLib(publishResp, null)) {
                        sendStartDownloadMsg(uid, currentFileId, StartDownloadStatus.FAILED, "启动算法或者模型下载失败！");
                    } else {//已下载，依赖已添加成功；
                        CareLog.i(TAG, "reqStartDownloadPluginApp() " + dbDownloadInfo.fileId + " downloaded! relies added!");
                        sendStartDownloadMsg(uid, currentFileId, StartDownloadStatus.READY, "课件已下载！依赖已添加");
                    }
                }
            } else if (dbDownloadInfo.status == DownloadInfo.STATUS_STOPPED) {
                if (isCareUpgrade && !dbDownloadInfo.version.equals(lastVersion)) {
                    getPluginAppLatestVersion(uid, dbDownloadInfo.version, proDetailResp, publishResp);
                } else {
                    if (!handleAlgorithmLib(publishResp, null)) {
                        sendStartDownloadMsg(uid, currentFileId, StartDownloadStatus.FAILED, "启动算法或者模型下载失败！");
                    } else {
                        if (!getDownloadBinder().startDownload(currentFileId)) {
                            sendStartDownloadMsg(uid, currentFileId, StartDownloadStatus.FAILED, "启动下载失败！");
                        } else {//进入下载中...
                            sendStartDownloadMsg(uid, currentFileId, StartDownloadStatus.LOADING, "正在下载中！");
                        }
                    }
                }
            } else {//正在下载
                sendStartDownloadMsg(uid, currentFileId, StartDownloadStatus.LOADING, "正在下载中！");
            }
        } else {//未有下载
            if (!handleAlgorithmLib(publishResp, null)) {
                sendStartDownloadMsg(uid, currentFileId, StartDownloadStatus.FAILED, "启动算法或者模型下载失败！");
            } else {//依赖已添加成功，开始下载课件
                if (!getDownloadBinder().startDownload(DownloadHelper.buildDownloadInfo(appContext, proDetailResp, publishResp))) {
                    sendStartDownloadMsg(uid, currentFileId, StartDownloadStatus.FAILED, "启动下载失败！");
                } else {//开始下载成功，进入下载中...
                    sendStartDownloadMsg(uid, currentFileId, StartDownloadStatus.LOADING, "正在下载中！");
                }
            }
        }
    }

    private void getPluginAppLatestVersion(final String uid, String originalSoftwareVersion, ProDetailResp proDetailResp, PublishResp publishResp) {
        final String currentFileId = publishResp.getSoftwareInfo().getSoftwareCode();
        DataRepository.getInstance().getUpgradeVersionInfo(new UpgradeReq(originalSoftwareVersion, publishResp.getSoftwareInfo().getSoftwareCode()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<BaseResp<UpgradeResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<UpgradeResp> response) {
                        if (response.code == BaseConstants.API_HANDLE_SUCCESS && !response.isCache) {
                            UpgradeResp upgradeResp = response.data;
                            if (upgradeResp != null && upgradeResp.getVersionId() != null && !upgradeResp.getVersionId().isEmpty()) {
                                if (upgradeResp.isForcible()) {
                                    if (handleAlgorithmLib(publishResp, upgradeResp)) {
                                        DownloadInfo downloadInfo = DownloadHelper.buildUpgradeDownloadInfo(appContext, proDetailResp, publishResp, upgradeResp);
                                        if (!getDownloadBinder().startDownload(downloadInfo)) {
                                            sendStartDownloadMsg(uid, currentFileId, StartDownloadStatus.FAILED, "启动下载失败！");
                                        } else {
                                            sendStartDownloadMsg(uid, currentFileId, StartDownloadStatus.LOADING, "正在下载中！");
                                        }
                                    } else {
                                        sendStartDownloadMsg(uid, currentFileId, StartDownloadStatus.FAILED, "启动算法或者模型下载失败！");
                                    }
                                } else {
                                    handlePluginAppPublishResp(uid, proDetailResp, publishResp, false);
                                }
                            } else {
                                sendStartDownloadMsg(uid, currentFileId, StartDownloadStatus.FAILED, "接口返回数据异常！");
                            }
                        } else {
                            sendStartDownloadMsg(uid, currentFileId, StartDownloadStatus.FAILED, response.message);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        sendStartDownloadMsg(uid, currentFileId, StartDownloadStatus.FAILED, "网络异常！");
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private boolean handleAlgorithmLib(PublishResp publishResp, UpgradeResp upgradeResp) {
        List<AlgorithmInfoResp> algorithmInfoList = publishResp.getRelevancyAlgorithmVersions();
        if (upgradeResp != null) {
            algorithmInfoList = upgradeResp.getRelevancyAlgorithmVersions();//是否应该并集？
        }
        if (algorithmInfoList != null) {
            for (int i = 0; i < algorithmInfoList.size(); i++) {
                AlgorithmInfoResp algorithmInfoResp = algorithmInfoList.get(i);
                relayLibFileIdMap.put(algorithmInfoResp.versionId, algorithmInfoResp.versionId);
                boolean successHandle = handleModelBin(algorithmInfoResp.relevancyModelVersions);
                if (!successHandle) {
                    return false;
                }
                DownloadInfo downloadAlgorithm = CareController.instance.getDownloadInfoByFileId(algorithmInfoResp.versionId);
                if (downloadAlgorithm == null) {
                    boolean addOk = getDownloadBinder().startDownload(DownloadHelper.buildAlgorithmDownloadInfo(appContext, algorithmInfoResp));
                    if (!addOk) {
                        return false;
                    }
                } else if (downloadAlgorithm.status == DownloadInfo.STATUS_STOPPED) {
                    getDownloadBinder().startDownload(downloadAlgorithm.fileId);
                }
            }
        }
        return true;
    }

    private boolean handleModelBin(List<ModelInfoResp> relatedModelList) {
        if (relatedModelList != null) {
            for (int i = 0; i < relatedModelList.size(); i++) {
                ModelInfoResp modelInfoResp = relatedModelList.get(i);
                relayModelBinFileIdMap.put(modelInfoResp.versionId, modelInfoResp.versionId);
                DownloadInfo downloadModel = CareController.instance.getDownloadInfoByFileId(modelInfoResp.versionId);
                if (downloadModel == null) {
                    boolean addOk = getDownloadBinder().startDownload(DownloadHelper.buildModelDownloadInfo(appContext, modelInfoResp));
                    if (!addOk) {
                        return false;
                    }
                } else if (downloadModel.status == DownloadInfo.STATUS_STOPPED) {
                    getDownloadBinder().startDownload(downloadModel.fileId);
                }
            }
        }
        return true;
    }

    public void clearRelayLibFileIdMap() {
        relayLibFileIdMap.clear();
    }

    public HashMap<String, String> getRelayLibFileIdMap() {
        return relayLibFileIdMap;
    }

    public void clearRelayModelBinFileIdMap() {
        relayModelBinFileIdMap.clear();
    }

    public HashMap<String, String> getRelayModelBinFileIdMap() {
        return relayModelBinFileIdMap;
    }

    private static boolean isAlgorithmLibReady(PublishResp publishResp, UpgradeResp upgradeResp) {
        List<AlgorithmInfoResp> algorithmInfoList = publishResp.getRelevancyAlgorithmVersions();
        if (upgradeResp != null) {
            algorithmInfoList = upgradeResp.getRelevancyAlgorithmVersions();//是否应该并集？
        }
        if (algorithmInfoList != null) {
            for (int i = 0; i < algorithmInfoList.size(); i++) {
                AlgorithmInfoResp algorithmInfoResp = algorithmInfoList.get(i);
                if (!isModelBinReady(algorithmInfoResp.relevancyModelVersions)) {
                    return false;
                }
                DownloadInfo downloadAlgorithm = CareController.instance.getDownloadInfoByFileId(algorithmInfoResp.versionId);
                if (downloadAlgorithm == null) {
                    return false;
                } else if (downloadAlgorithm.status != DownloadInfo.STATUS_SUCCESS) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isModelBinReady(List<ModelInfoResp> relatedModelList) {
        if (relatedModelList != null) {
            for (int i = 0; i < relatedModelList.size(); i++) {
                ModelInfoResp modelInfoResp = relatedModelList.get(i);
                DownloadInfo downloadModel = CareController.instance.getDownloadInfoByFileId(modelInfoResp.versionId);
                if (downloadModel == null) {
                    return false;
                } else if (downloadModel.status != DownloadInfo.STATUS_SUCCESS) {
                    return false;
                }
            }
        }
        return true;
    }

    static class StartDownloadStatus {
        public static final int PROCESSING = -2;        //上一次的请求还在处理中
        public static final int FAILED = -1;            //处理失败，网络异常、接口异常、启动下载失败、下载异常
        public static final int READY = 0;              //已下载，已安装
        public static final int LOADING = 1;            //正在下载
        public static final int LOADING_RELIES = 2;     //正在下载依赖内容
        public static final int INSTALLING = 10;        //正在安装
    }
}
