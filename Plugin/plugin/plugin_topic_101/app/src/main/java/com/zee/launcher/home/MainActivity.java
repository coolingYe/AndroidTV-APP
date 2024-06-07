package com.zee.launcher.home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zee.launcher.home.adapter.MainItemAdapter;
import com.zee.launcher.home.adapter.UnitBannerAdapter;
import com.zee.launcher.home.data.DataRepository;
import com.zee.launcher.home.data.model.MainItemMo;
import com.zee.launcher.home.data.model.UnitMo;
import com.zee.launcher.home.ui.detail.DetailActivity;
import com.zee.launcher.home.ui.product.ProductActivity;
import com.zee.launcher.home.ui.service.ServiceActivity;
import com.zee.launcher.home.widgets.MyVideoView;
import com.zee.launcher.home.widgets.banner.Banner;
import com.zee.zxing.encode.CodeCreator;
import com.zeewain.base.BaseApplication;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.config.SharePrefer;
import com.zeewain.base.model.LoadState;
import com.zeewain.base.ui.BaseActivity;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.DensityUtils;
import com.zeewain.base.utils.DisplayUtil;
import com.zeewain.base.utils.FileUtils;
import com.zeewain.base.utils.SPUtils;
import com.zeewain.base.widgets.LoadingView;
import com.zeewain.base.widgets.NetworkErrView;
import com.zwn.launcher.host.HostManager;
import com.zwn.user.data.model.SessionInfoMo;
import com.zwn.user.ui.UserCenterActivity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";
    private static final int USER_CENTER_CODE = 1234;
    private MainViewModel viewModel;
    private LoadingView loadingViewHomeClassic;
    private NetworkErrView networkErrViewHomeClassic;
    private ConstraintLayout clMainLayout;
    private RecyclerView recyclerViewHomeClassic;
    private Banner<List<UnitMo>, UnitBannerAdapter> banner;
    private ConstraintLayout clTopUser;
    private TextView tvTopUsername;
    private ConstraintLayout clTopSettings;
    private FrameLayout layoutQrcodeLoginRoot;
    private CardView cardViewQrcodeLogin;
    private ImageView ivQrcodeLoginQrcode;
    private ImageView ivQrcodeLoginRefresh;
    private Animation refreshAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainViewModelFactory factory = new MainViewModelFactory(DataRepository.getInstance());
        viewModel = new ViewModelProvider(this, factory).get(MainViewModel.class);

        DensityUtils.autoWidth(getApplication(), this);
        setContentView(R.layout.activity_main);

        initView();
        initListener();
        initViewObservable();

        if(!HomeApplication.isVidePlayed) {
            File videoFile = new File(getCacheDir(), "meta_video.mp4");
            try {
                FileUtils.copyAssetFileToPath(this, "meta_video.mp4", videoFile.getAbsolutePath());
                //todo md5check
                initVideo(videoFile.getAbsolutePath());
                if(CommonUtils.isUserLogin()) {
                    clMainLayout.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (isAkSkExist()) {
                                viewModel.reqUserInfo();
                            } else {
                                viewModel.reqAkSkInfo();
                            }
                        }
                    }, 500);
                }
            } catch (IOException e) {
                MyVideoView myVideoView = findViewById(R.id.player_view_home_classic);
                myVideoView.setVisibility(View.GONE);
                initData();
            }
        }else{
            MyVideoView myVideoView = findViewById(R.id.player_view_home_classic);
            myVideoView.setVisibility(View.GONE);
            initData();
        }
    }

    private void initView(){
        loadingViewHomeClassic = findViewById(R.id.loadingView_home_classic);
        networkErrViewHomeClassic = findViewById(R.id.networkErrView_home_classic);
        clMainLayout = findViewById(R.id.cl_main_content);
        clTopUser = findViewById(R.id.cl_top_user);
        tvTopUsername = findViewById(R.id.tv_top_username);
        clTopSettings = findViewById(R.id.cl_top_settings);

        recyclerViewHomeClassic = findViewById(R.id.recycler_view_home_classic);
        recyclerViewHomeClassic.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        recyclerViewHomeClassic.setFocusable(true);
        recyclerViewHomeClassic.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewHomeClassic.setNextFocusUpId(R.id.cl_top_user);

        banner =  findViewById(R.id.banner_home_classic);
        banner.setOrientation(Banner.VERTICAL);
        banner.setScrollTime(2000);
        banner.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);

        layoutQrcodeLoginRoot = findViewById(R.id.layout_qrcode_login_root);
        cardViewQrcodeLogin = findViewById(R.id.card_qrcode_login_qrcode);
        cardViewQrcodeLogin.setNextFocusUpId(R.id.card_qrcode_login_qrcode);
        cardViewQrcodeLogin.setNextFocusDownId(R.id.card_qrcode_login_qrcode);
        cardViewQrcodeLogin.setNextFocusLeftId(R.id.card_qrcode_login_qrcode);
        cardViewQrcodeLogin.setNextFocusRightId(R.id.card_qrcode_login_qrcode);
        ivQrcodeLoginQrcode = findViewById(R.id.iv_qrcode_login_qrcode);
        ivQrcodeLoginRefresh = findViewById(R.id.iv_qrcode_login_refresh);
        refreshAnimation = AnimationUtils.loadAnimation(ivQrcodeLoginRefresh.getContext(), com.zwn.user.R.anim.rotate_loading_anim);
        LinearInterpolator interpolator = new LinearInterpolator();
        refreshAnimation.setInterpolator(interpolator);
    }

    private void initData(){
        if(CommonUtils.isUserLogin()) {
            if (isAkSkExist()) {
                viewModel.reqUserInfo();
            } else {
                viewModel.reqAkSkInfo();
            }
        }else{
            showQrcodeLoginPage();
        }
    }

    private boolean isAkSkExist(){
        String akSkInfoString = HostManager.getHostSpString(SharePrefer.akSkInfo, null);
        if (akSkInfoString != null && !akSkInfoString.isEmpty()) {
            return true;
        }
        return false;
    }

    private void onLoginSuccess(){
        viewModel.tryReqOrganizerInfoListTimes = 3;
        viewModel.tryReqServicePackInfoTimes = 3;
        viewModel.tryReqAkSkInfoTimes = 3;
        viewModel.tryReqUserInfoTimes = 3;
        viewModel.reqAkSkInfo();
    }

    private void showQrcodeLoginPage(){
        tvTopUsername.setText("未登录");
        layoutQrcodeLoginRoot.setVisibility(View.VISIBLE);
        cardViewQrcodeLogin.requestFocus();
        viewModel.checkValidSessionAndReq();
        viewModel.createLoginSession();
    }

    private void onVideoPlayDone(){
        HomeApplication.isVidePlayed = true;
        HomeApplication.isPlayingVideo = false;
        if(!CommonUtils.isUserLogin()){
            showQrcodeLoginPage();
        }else{
            recyclerViewHomeClassic.requestFocus();
        }
    }

    private void initVideo(String videoPath){
        HomeApplication.isPlayingVideo = true;
        final MyVideoView myVideoView = findViewById(R.id.player_view_home_classic);
        myVideoView.requestFocus();
        myVideoView.setOnClickListener(v -> {});
        myVideoView.setOnErrorListener((mp, what, extra) -> {
            myVideoView.setOnPreparedListener(null);
            myVideoView.setOnCompletionListener(null);
            myVideoView.setOnErrorListener(null);
            myVideoView.destroyDrawingCache();
            myVideoView.setVisibility(View.GONE);
            if(mp != null)
                mp.release();
            onVideoPlayDone();
            return false;
        });
        myVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                myVideoView.setOnPreparedListener(null);
                myVideoView.setOnCompletionListener(null);
                myVideoView.setOnErrorListener(null);
                myVideoView.destroyDrawingCache();
                myVideoView.setVisibility(View.GONE);
                if(mp != null)
                    mp.release();
                onVideoPlayDone();
            }
        });

        myVideoView.setVideoPath(videoPath);
        myVideoView.setDrawingCacheEnabled(true);
        myVideoView.setOnPreparedListener(mp -> {
            mp.setLooping(false);
            myVideoView.start();
        });
    }

    private void updateView() {

        List<MainItemMo> mainItemList = new ArrayList<>();
        mainItemList.add(new MainItemMo(R.mipmap.img_main_item_1_gate, R.mipmap.img_main_item_1_gate_selected, R.mipmap.img_main_item_1_title, R.mipmap.img_main_item_1_title_selected));
        mainItemList.add(new MainItemMo(R.mipmap.img_main_item_2_gate, R.mipmap.img_main_item_2_gate_selected, R.mipmap.img_main_item_2_title, R.mipmap.img_main_item_2_title_selected));
        mainItemList.add(new MainItemMo(R.mipmap.img_main_item_3_gate, R.mipmap.img_main_item_3_gate_selected, R.mipmap.img_main_item_3_title, R.mipmap.img_main_item_3_title_selected));

        MainItemAdapter mainItemAdapter = new MainItemAdapter(mainItemList);
        recyclerViewHomeClassic.setAdapter(mainItemAdapter);

        mainItemAdapter.setOnItemClickListener(new MainItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (position == 0) {
                    Intent intent = new Intent(MainActivity.this, ProductActivity.class);
                    intent.putExtra("skuIds", (ArrayList<String>) viewModel.getSkuIdListByPageName("赛事活动"));
                    startActivity(intent);
                } else if (position == 1) {
                    Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                    intent.putExtra("skuId", viewModel.baiYunLakeSkuId);
                    startActivity(intent);
                } else if (position == 2) {
                    Intent intent = new Intent(MainActivity.this, ServiceActivity.class);
                    intent.putExtra("skuIds", (ArrayList<String>) viewModel.getSkuIdListByPageName("企业服务"));
                    startActivity(intent);
                }
            }
        });

        if(!HomeApplication.isPlayingVideo)
            recyclerViewHomeClassic.requestFocus();

        banner.setAdapter(new UnitBannerAdapter(viewModel.getUnitMoList()));
        banner.startAutoLoop(true);

        if(viewModel.userName != null){
            tvTopUsername.setText(getUserName(viewModel.userName));
        }
    }

    private String getUserName(String name) {
        if (name.length() > 5) {
            return name.substring(0, 5) + "...";
        }
        return name;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == USER_CENTER_CODE){
            if(resultCode == RESULT_OK && data != null){
                String userName = data.getStringExtra(BaseConstants.EXTRA_USER_NAME);
                if(userName != null){
                    tvTopUsername.setText(getUserName(userName));
                }
            }
        }
    }

    @SuppressLint({"ResourceType", "SetTextI18n"})
    private void initListener() {
        networkErrViewHomeClassic.setRetryClickListener(() -> {
            if(LoadState.Failed == viewModel.mldAkSkInfoLoadState.getValue()){
                viewModel.tryReqAkSkInfoTimes = 3;
                viewModel.reqAkSkInfo();
            }else if(LoadState.Failed == viewModel.mldUserInfoLoadState.getValue()){
                viewModel.tryReqUserInfoTimes = 3;
                viewModel.reqUserInfo();
            }else if(LoadState.Failed == viewModel.mldOrganizerInfoListLoadState.getValue()){
                viewModel.tryReqOrganizerInfoListTimes = 3;
                viewModel.reqOrganizerInfoList();
            }else{
                viewModel.tryReqServicePackInfoTimes = 3;
                viewModel.reqServicePackInfo();
            }
        });

        clTopUser.setOnClickListener(v -> {
            if(CommonUtils.isUserLogin()){
                if(LoadState.Success == viewModel.mldUserInfoLoadState.getValue()) {
                    Intent intent = new Intent();
                    intent.setClass(v.getContext(), UserCenterActivity.class);
                    startActivityForResult(intent, USER_CENTER_CODE);
                }else{
                    viewModel.tryReqUserInfoTimes = 3;
                    viewModel.reqUserInfo();
                }
            }
        });

        clTopUser.setOnFocusChangeListener((v, hasFocus) -> {
            if(hasFocus){
                if(!CommonUtils.isUserLogin()){
                    cardViewQrcodeLogin.requestFocus();
                    return;
                }
                CommonUtils.scaleView(v, 1.2f);
            }else{
                v.clearAnimation();
                CommonUtils.scaleView(v, 1f);
            }
        });
        clTopUser.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && event.getAction() == KeyEvent.ACTION_DOWN) {
                v.clearAnimation();
                v.startAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.host_shake));
            }else if(keyCode == KeyEvent.KEYCODE_DPAD_UP && event.getAction() == KeyEvent.ACTION_DOWN){
                v.clearAnimation();
                v.startAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.host_shake_y));
            }
            return false;
        });

        clTopSettings.setOnClickListener(v -> CommonUtils.startSettingsActivity(v.getContext()));

        clTopSettings.setOnFocusChangeListener((v, hasFocus) -> {
            if(hasFocus){
                CommonUtils.scaleView(v, 1.2f);
            }else{
                v.clearAnimation();
                CommonUtils.scaleView(v, 1f);
            }
        });
        clTopSettings.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && event.getAction() == KeyEvent.ACTION_DOWN) {
                v.clearAnimation();
                v.startAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.host_shake));
            }else if(keyCode == KeyEvent.KEYCODE_DPAD_UP && event.getAction() == KeyEvent.ACTION_DOWN){
                v.clearAnimation();
                v.startAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.host_shake_y));
            }
            return false;
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void initViewObservable() {
        viewModel.mldAkSkInfoLoadState.observe(this, loadState -> {
            if (LoadState.Loading == loadState) {
                loadingViewHomeClassic.setVisibility(View.VISIBLE);
                networkErrViewHomeClassic.setVisibility(View.GONE);
                clMainLayout.setVisibility(View.GONE);
                loadingViewHomeClassic.startAnim();
            } else if (LoadState.Success == loadState) {
                viewModel.reqUserInfo();
            } else {
                if(viewModel.tryReqAkSkInfoTimes > 0) {
                    viewModel.reqAkSkInfo();
                }else{
                    loadingViewHomeClassic.stopAnim();
                    loadingViewHomeClassic.setVisibility(View.GONE);
                    clMainLayout.setVisibility(View.GONE);
                    networkErrViewHomeClassic.setVisibility(View.VISIBLE);
                }
            }
        });

        viewModel.mldUserInfoLoadState.observe(this, loadState -> {
            if (LoadState.Loading == loadState) {
                if(View.VISIBLE != loadingViewHomeClassic.getVisibility()) {
                    loadingViewHomeClassic.setVisibility(View.VISIBLE);
                    networkErrViewHomeClassic.setVisibility(View.GONE);
                    clMainLayout.setVisibility(View.GONE);
                    loadingViewHomeClassic.startAnim();
                }
            } else if (LoadState.Success == loadState) {
                viewModel.reqOrganizerInfoList();
            } else {
                if(viewModel.tryReqUserInfoTimes > 0) {
                    viewModel.reqUserInfo();
                }else{
                    loadingViewHomeClassic.stopAnim();
                    loadingViewHomeClassic.setVisibility(View.GONE);
                    clMainLayout.setVisibility(View.GONE);
                    networkErrViewHomeClassic.setVisibility(View.VISIBLE);
                }
            }
        });

        viewModel.mldOrganizerInfoListLoadState.observe(this, loadState -> {
            if (LoadState.Loading == loadState) {
                if(View.VISIBLE != loadingViewHomeClassic.getVisibility()) {
                    loadingViewHomeClassic.setVisibility(View.VISIBLE);
                    networkErrViewHomeClassic.setVisibility(View.GONE);
                    clMainLayout.setVisibility(View.GONE);
                    loadingViewHomeClassic.startAnim();
                }
            } else if (LoadState.Success == loadState) {
                viewModel.reqServicePackInfo();
            } else {
                if(viewModel.tryReqOrganizerInfoListTimes > 0) {
                    viewModel.reqOrganizerInfoList();
                }else{
                    loadingViewHomeClassic.stopAnim();
                    loadingViewHomeClassic.setVisibility(View.GONE);
                    clMainLayout.setVisibility(View.GONE);
                    networkErrViewHomeClassic.setVisibility(View.VISIBLE);
                }
            }
        });

        viewModel.mldServicePackInfoLoadState.observe(this, loadState -> {
            if (LoadState.Loading == loadState) {
                if(View.VISIBLE != loadingViewHomeClassic.getVisibility()){
                    loadingViewHomeClassic.setVisibility(View.VISIBLE);
                    networkErrViewHomeClassic.setVisibility(View.GONE);
                    clMainLayout.setVisibility(View.GONE);
                    loadingViewHomeClassic.startAnim();
                }
            } else if (LoadState.Success == loadState) {
                loadingViewHomeClassic.stopAnim();
                loadingViewHomeClassic.setVisibility(View.GONE);
                networkErrViewHomeClassic.setVisibility(View.GONE);
                clMainLayout.setVisibility(View.VISIBLE);
                updateView();
            } else {
                if(viewModel.tryReqServicePackInfoTimes > 0) {
                    viewModel.reqServicePackInfo();
                }else{
                    loadingViewHomeClassic.stopAnim();
                    loadingViewHomeClassic.setVisibility(View.GONE);
                    clMainLayout.setVisibility(View.GONE);
                    networkErrViewHomeClassic.setVisibility(View.VISIBLE);
                }
            }
        });

        viewModel.mldCreateLoginSessionLoadState.observe(this, dataLoadState -> {
            if (View.VISIBLE == layoutQrcodeLoginRoot.getVisibility()) {
                if(cardViewQrcodeLogin != null)
                    cardViewQrcodeLogin.requestFocus();
                if (LoadState.Success == dataLoadState.loadState) {
                    stopRefreshAnim();
                    ivQrcodeLoginRefresh.setVisibility(View.GONE);
                    ivQrcodeLoginQrcode.setVisibility(View.VISIBLE);
                    ivQrcodeLoginQrcode.setImageBitmap(CodeCreator.createQRCode(dataLoadState.data.loginPageUrl,
                            DisplayUtil.dip2px(MainActivity.this, 120),
                            DisplayUtil.dip2px(MainActivity.this, 120), null));
                    ivQrcodeLoginQrcode.getHandler().removeCallbacksAndMessages(null);
                    ivQrcodeLoginQrcode.postDelayed(() -> viewModel.createLoginSession(), 10 * 60 * 1000);

                    long createSessionTime = System.currentTimeMillis();
                    final SessionInfoMo sessionInfoMo = new SessionInfoMo(dataLoadState.data.sessionId,
                            dataLoadState.data.loginPageUrl,
                            dataLoadState.data.sessionSecretKey, createSessionTime);
                    viewModel.addReqSessionResult(sessionInfoMo);
                    Log.i(TAG, "start reqSessionResult() " + MainViewModel.reqSessionResultMap.size());
                    viewModel.reqSessionResult(sessionInfoMo);
                } else if (LoadState.Failed == dataLoadState.loadState) {
                    stopRefreshAnim();
                    ivQrcodeLoginQrcode.getHandler().removeCallbacksAndMessages(null);
                    ivQrcodeLoginQrcode.postDelayed(() -> viewModel.createLoginSession(), 1000);
                } else {
                    ivQrcodeLoginRefresh.setVisibility(View.VISIBLE);
                    startRefreshAnim();
                    ivQrcodeLoginQrcode.setVisibility(View.GONE);
                }
            }
        });

        viewModel.mldReqSessionResultLoadState.observe(this, sessionResultLoadState -> {
            if (View.VISIBLE == layoutQrcodeLoginRoot.getVisibility()){
                if (LoadState.Success == sessionResultLoadState.loadState) {
                    layoutQrcodeLoginRoot.setVisibility(View.GONE);
                    MainViewModel.reqSessionResultMap.clear();

                    SPUtils.getInstance().put(SharePrefer.userToken, sessionResultLoadState.token);
                    BaseApplication.userToken = sessionResultLoadState.token;

                    ivQrcodeLoginQrcode.getHandler().removeCallbacksAndMessages(null);
                    ivQrcodeLoginRefresh.clearAnimation();

                    onLoginSuccess();

                } else if (LoadState.Failed == sessionResultLoadState.loadState) {//一个是10011=等待超时（等待超过60s），一个是10012=会话失效
                    if (10012 == sessionResultLoadState.errCode) {//session invalid
                        ivQrcodeLoginQrcode.getHandler().removeCallbacksAndMessages(null);
                        viewModel.createLoginSession();
                    } else {
                        if (System.currentTimeMillis() - sessionResultLoadState.sessionInfoMo.createTime < 10 * 60 * 1000) {
                            if (MainViewModel.reqSessionResultMap.containsKey(sessionResultLoadState.sessionInfoMo.sessionId))
                                Log.i(TAG, "restart reqSessionResult() " + MainViewModel.reqSessionResultMap.size());
                            viewModel.reqSessionResult(sessionResultLoadState.sessionInfoMo);
                        } else {
                            MainViewModel.reqSessionResultMap.remove(sessionResultLoadState.sessionInfoMo.sessionId);
                        }
                    }
                }
            }
        });

        viewModel.mldToastMsg.observe(this, msg -> showToast(msg));
    }

    public void startRefreshAnim(){
        ivQrcodeLoginRefresh.clearAnimation();
        ivQrcodeLoginRefresh.setAnimation(refreshAnimation);
        refreshAnimation.start();
    }

    public void stopRefreshAnim(){
        refreshAnimation.cancel();
        ivQrcodeLoginRefresh.clearAnimation();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(!CommonUtils.isUserLogin() && !HomeApplication.isPlayingVideo){
            showQrcodeLoginPage();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK
                || keyCode == KeyEvent.KEYCODE_HOME)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(banner.getItemCount() > 0) {
            banner.startAutoLoop(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(banner.getItemCount() > 0){
            banner.stop();
            banner.setAutoLoop(false);
        }
    }

    @Override
    protected void onDestroy() {
        banner.destroy();
        if(ivQrcodeLoginQrcode != null && ivQrcodeLoginQrcode.getHandler() != null){
            ivQrcodeLoginQrcode.getHandler().removeCallbacksAndMessages(null);
            ivQrcodeLoginRefresh.clearAnimation();
        }
        super.onDestroy();
    }
}