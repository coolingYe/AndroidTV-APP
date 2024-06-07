package com.zee.setting.fragment;


import static com.zee.setting.base.BaseConstants.SP_KEY_TIMER_PLAN;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.util.Consumer;
import androidx.core.widget.NestedScrollView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeUtils;
import com.google.gson.Gson;
import com.zee.setting.R;
import com.zee.setting.activity.CameraDescriptionActivity;
import com.zee.setting.activity.GuideCameraActivity;
import com.zee.setting.activity.SettingActivity;
import com.zee.setting.adapter.CameraInfoAdapter;
import com.zee.setting.adapter.DreamInfoAdapter;
import com.zee.setting.adapter.TimerAdapter;
import com.zee.setting.base.BaseConstants;
import com.zee.setting.bean.CameraBean;
import com.zee.setting.cache.SPUtils;
import com.zee.setting.cache.SharePrefer;
import com.zee.setting.receive.alarm.AlarmSetter;
import com.zee.setting.service.ConnectService;
import com.zee.setting.utils.CommonUtils;
import com.zee.setting.utils.Constant;
import com.zee.setting.utils.DensityUtils;
import com.zee.setting.utils.DreamUtils;
import com.zee.setting.utils.SystemProperties;
import com.zee.setting.utils.ToastUtils;
import com.zee.setting.views.TimerDialog;

import net.sunniwell.aar.focuscontrol.layout.FocusControlConstraintLayout;
import net.sunniwell.aar.focuscontrol.layout.FocusControlLinearLayout;
import net.sunniwell.aar.focuscontrol.layout.FocusControlRecyclerView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;


@SuppressLint("UseSwitchCompatOrMaterialCode")
public class FragmentCommon extends Fragment implements View.OnFocusChangeListener, View.OnClickListener {
    private NestedScrollView commonNestedScrollView;
    private FocusControlConstraintLayout volume;
    private FocusControlConstraintLayout microphone;
    private SeekBar volumeRate;
    private SeekBar microphoneRate;
    private AudioManager audioManager = null; //音频
    private int last_volume;
    private int streamMaxCallVolume;
    private TextView description;
    private DrawerLayout drawerLayout;
    private TextView volumeRateTv;
    private TextView microphoneRateTv;
    private int streamMaxVolume;
    private ImageView imgMicrophone;
    private ImageView imgVolume;
    private TextView volumeTv;
    private TextView microphoneTv;
    private boolean isSoundZero = false;
    private boolean isMicrophoneZero = false;
    private FocusControlConstraintLayout cameraSelectLayout;
    private TextView cameraDescription;
    private LinearLayout llCamera;
    private ImageView cameraArrow;
    private RecyclerView cameraListRecyclerView;
    private CameraInfoAdapter cameraInfoListAdapter;
    public static final String TAG = "FragmentCommon";
    private LinearLayout llShouDownDetails;
    private Switch shutdownSwitch;
    private FocusControlLinearLayout cameraRootView;
    private FocusControlRecyclerView rvShutdown;
    private FocusControlConstraintLayout screenOutTimeLayout;
    private TextView tvCameraDesc;
    private BadgeDrawable badgeCamera;
    private Consumer<Boolean> badgeCameraCallback;
    private RadioButton radioBtn5min;
    private RadioButton radioBtn15min;
    private RadioButton radioBtn30min;
    private RadioButton radioBtn1h;
    private RadioButton radioBtn2h;
    private RadioButton radioBtnNever;
    private LinearLayout llScreenOffTimeout;
    private TextView tvScreenOffTimeoutTitle;
    private ImageView ivScreenOffEnter;
    private List<DreamUtils.DreamInfo> dreamInfoList;
    private RecyclerView rvDreamActionList;
    private TextView tvDreamActionTitle;
    private ImageView ivDreamArrow;

    private boolean hasShowDreamActionList = false;

    private static final String screenOffTimeout5Min = "5分钟";
    private static final String screenOffTimeout15Min = "15分钟";
    private static final String screenOffTimeout30Min = "30分钟";
    private static final String screenOffTimeout1H = "1小时";
    private static final String screenOffTimeout2H = "2小时";
    private static final String screenOffTimeoutNever = "永不";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        View view = inflater.inflate(R.layout.fragment_common, container, false);
        commonNestedScrollView = view.findViewById(R.id.nsv_common);
        FocusControlConstraintLayout resolution = view.findViewById(R.id.resolution);
        volume = view.findViewById(R.id.volume);
        microphone = view.findViewById(R.id.microphone);
        volumeRate = view.findViewById(R.id.volume_rate);
        microphoneRate = view.findViewById(R.id.microphone_rate);
        description = view.findViewById(R.id.description);
        FocusControlConstraintLayout keySound = view.findViewById(R.id.key_sound);
        FocusControlConstraintLayout microphoneSelect = view.findViewById(R.id.microphone_select);
        volumeRateTv = view.findViewById(R.id.volume_rate_tv);
        microphoneRateTv = view.findViewById(R.id.microphone_rate_tv);
        drawerLayout = getActivity().findViewById(R.id.drawer_layout);
        imgMicrophone = view.findViewById(R.id.img_microphone);
        imgVolume = view.findViewById(R.id.img_volume);
        volumeTv = view.findViewById(R.id.volume_tv);
        microphoneTv = view.findViewById(R.id.microphone_tv);
        tvCameraDesc = view.findViewById(R.id.camera_wireless_description);
        llCamera = view.findViewById(R.id.ll_camera);
        cameraSelectLayout = view.findViewById(R.id.camera_select);
        cameraDescription = view.findViewById(R.id.camera_description);
        cameraArrow = view.findViewById(R.id.camera_arrow);
        FocusControlConstraintLayout cameraWirelessSelect = view.findViewById(R.id.camera_wireless_select);
        cameraListRecyclerView = view.findViewById(R.id.recycler_view_camera_list);
        FocusControlConstraintLayout shutdownView = view.findViewById(R.id.layout_timer_shutdown);
        llShouDownDetails = view.findViewById(R.id.ll_shutdown_details);
        shutdownSwitch = view.findViewById(R.id.shutdown_switch);
        FocusControlConstraintLayout addShutDownPlanView = view.findViewById(R.id.layout_add_shutdown_plan);
        rvShutdown = view.findViewById(R.id.rv_shutdown);
        cameraRootView = view.findViewById(R.id.camera_root);
        FocusControlConstraintLayout dreamLayout = view.findViewById(R.id.layout_dream);
        screenOutTimeLayout = view.findViewById(R.id.screen_timeout);
        FocusControlConstraintLayout screenOffTimeout5MinLayout = view.findViewById(R.id.screen_timeout_5min);
        FocusControlConstraintLayout screenOffTimeout15minLayout = view.findViewById(R.id.screen_timeout_15min);
        FocusControlConstraintLayout screenOffTimeout30minLayout = view.findViewById(R.id.screen_timeout_30min);
        FocusControlConstraintLayout screenOffTimeout1hLayout = view.findViewById(R.id.screen_timeout_1h);
        FocusControlConstraintLayout screenOffTimeout2hLayout = view.findViewById(R.id.screen_timeout_2h);
        FocusControlConstraintLayout screenOffTimeoutNeverLayout = view.findViewById(R.id.screen_timeout_never);
        radioBtn5min = view.findViewById(R.id.radio_btn_5min);
        radioBtn15min = view.findViewById(R.id.radio_btn_15min);
        radioBtn30min = view.findViewById(R.id.radio_btn_30min);
        radioBtn1h = view.findViewById(R.id.radio_btn_1h);
        radioBtn2h = view.findViewById(R.id.radio_btn_2h);
        radioBtnNever = view.findViewById(R.id.radio_btn_never);
        llScreenOffTimeout = view.findViewById(R.id.ll_screen_out_time);
        ivScreenOffEnter = view.findViewById(R.id.iv_screen_timeout_enter);
        rvDreamActionList = view.findViewById(R.id.recycler_view_dream_action_list);
        tvDreamActionTitle = view.findViewById(R.id.dream_description);
        ivDreamArrow = view.findViewById(R.id.dream_arrow);

        DensityUtils.autoWidth(getActivity().getApplication(), getActivity());

        resolution.setOnClickListener(this);
        volume.setOnClickListener(this);
        volumeRate.setOnClickListener(this);
        microphone.setOnClickListener(this);
        microphoneRate.setOnClickListener(this);
        keySound.setOnClickListener(this);
        microphoneSelect.setOnClickListener(this);
        cameraSelectLayout.setOnClickListener(this);
        cameraWirelessSelect.setOnClickListener(this);
        shutdownView.setOnClickListener(this);
        addShutDownPlanView.setOnClickListener(this);
        dreamLayout.setOnClickListener(this);

        cameraSelectLayout.setOnFocusChangeListener(this);
        resolution.setOnFocusChangeListener(this);
        volume.setOnFocusChangeListener(this);
        volumeRate.setOnFocusChangeListener(this);
        microphone.setOnFocusChangeListener(this);
        microphoneRate.setOnFocusChangeListener(this);
        keySound.setOnFocusChangeListener(this);
        microphoneSelect.setOnFocusChangeListener(this);

        resolution.setNextFocusDownId(R.id.volume);
        keySound.setNextFocusUpId(R.id.volume);
        keySound.setNextFocusDownId(R.id.camera_select);

        screenOffTimeout5MinLayout.setOnClickListener(this);
        screenOffTimeout15minLayout.setOnClickListener(this);
        screenOffTimeout30minLayout.setOnClickListener(this);
        screenOffTimeout1hLayout.setOnClickListener(this);
        screenOffTimeout2hLayout.setOnClickListener(this);
        screenOffTimeoutNeverLayout.setOnClickListener(this);

        radioBtn5min.setOnCheckedChangeListener(radioCheckedChangeListener);
        radioBtn15min.setOnCheckedChangeListener(radioCheckedChangeListener);
        radioBtn30min.setOnCheckedChangeListener(radioCheckedChangeListener);
        radioBtn1h.setOnCheckedChangeListener(radioCheckedChangeListener);
        radioBtn2h.setOnCheckedChangeListener(radioCheckedChangeListener);
        radioBtnNever.setOnCheckedChangeListener(radioCheckedChangeListener);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            initData();
        }
        initListener(view);
        initCameraFunction();
        initDream(view);
    }

    private void initCameraFunction() {
        if (SystemProperties.getBoolean(BaseConstants.SP_KEY_CAMERA_FUNCTION_SWITCH, false)) {
            cameraRootView.setVisibility(View.VISIBLE);
            initBadge();
            updateCameraInfo();
        }
    }

    private void initBadge() {
        boolean hasCommonUpdate = SystemProperties.getBoolean(BaseConstants.SP_KEY_CAMERA_UPDATE_STATE, true);
        llCamera.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressLint({"ResourceType", "UnsafeOptInUsageError"})
            @Override
            public void onGlobalLayout() {
                badgeCamera = BadgeDrawable.create(llCamera.getContext());
                badgeCamera.setBadgeGravity(BadgeDrawable.TOP_END);
                badgeCamera.setBackgroundColor(0xFFFF0715);
                badgeCamera.setVisible(hasCommonUpdate);
                BadgeUtils.attachBadgeDrawable(badgeCamera, llCamera);
                llCamera.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void initData() {
        //屏幕分辨率获取
        Display primaryDisplay = getActivity().getDisplay();
        Display.Mode[] modes = primaryDisplay.getSupportedModes();
        //Log.i("ssshhh", "modes.length=" + modes.length);
        if (modes != null && modes.length > 0) {
            Display.Mode mode = modes[0];
            int physicalWidth = mode.getPhysicalWidth();
            int physicalHeight = mode.getPhysicalHeight();
            int refreshRate = (int) mode.getRefreshRate();
            description.setText(physicalHeight + "-" + refreshRate + "hz");
            //Log.i("ssshhh", "physicalWidth=" + physicalWidth + "--physicalHeight=" + physicalHeight + "---refreshRate=" + refreshRate);
        }


        //系统音量
        audioManager = (AudioManager) getActivity().getSystemService(Service.AUDIO_SERVICE);
        streamMaxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        volumeRate.setMax(streamMaxVolume);
        int streamVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        volumeRate.setProgress(streamVolume);

        setSoundUI(streamVolume, 1);
        last_volume = streamVolume;
        //麦克风音量(也就是通话音量)
        streamMaxCallVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);

        microphoneRate.setMax(streamMaxCallVolume - 1);
        int streamCallVolume = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
        microphoneRate.setProgress(streamCallVolume - 1);

        setSoundUI(streamCallVolume, 2);
        //隐藏摄像头
        updateCameraSelectItem();

        //定时任务
        initTimer();

        dreamInfoList = DreamUtils.getDreamInfoList(getContext());
        dreamInfoList.removeIf(dreamInfo -> dreamInfo.caption.toString().equals("beeAdvert"));
    }

    private void initTimer() {
        boolean timerEnable = SPUtils.getInstance().getBoolean(BaseConstants.SP_KEY_TIMER_PLAN_ENABLE);
        shutdownSwitch.setChecked(timerEnable);
        llShouDownDetails.setVisibility(timerEnable ? View.VISIBLE : View.GONE);
        if (timerEnable) getTimerTask();
    }

    private void initDream(View view) {
        String dreamActionStr = dreamInfoList.stream().filter(dreamInfo -> dreamInfo.isActive).findFirst().map(dreamInfo -> String.valueOf(dreamInfo.caption)).orElse("");
        tvDreamActionTitle.setText(dreamActionStr);

        DreamInfoAdapter dreamInfoAdapter = new DreamInfoAdapter(dreamInfoList);
        dreamInfoList.forEach(dreamInfo -> {
            if (dreamInfo.caption.toString().equals(dreamActionStr)) {
                dreamInfoAdapter.setSelectedDream(dreamInfo.componentName);
            }
        });
        dreamInfoAdapter.setItemClick((view1, dreamInfo) -> {
            dreamInfoAdapter.setSelectedDream(dreamInfo.componentName);
            dreamInfoAdapter.notifyDataSetChanged();
            tvDreamActionTitle.setText(dreamInfo.caption);
            rvDreamActionList.setVisibility(View.GONE);
            ivDreamArrow.setRotation(0f);
            hasShowDreamActionList = false;
            setupDreamAction(dreamInfo);
        });
        rvDreamActionList.setAdapter(dreamInfoAdapter);

        String screenOutTime = getScreenOffTimeoutTitle();
        tvScreenOffTimeoutTitle = view.findViewById(R.id.screen_timeout_description);
        setScreenOffTimeOutTitle(screenOutTime);
        switch (screenOutTime) {
            case screenOffTimeout5Min:
                radioBtn5min.setChecked(true);
                break;
            case screenOffTimeout15Min:
                radioBtn15min.setChecked(true);
                break;
            case screenOffTimeout30Min:
                radioBtn30min.setChecked(true);
                break;
            case screenOffTimeout1H:
                radioBtn1h.setChecked(true);
                break;
            case screenOffTimeout2H:
                radioBtn2h.setChecked(true);
                break;
            case screenOffTimeoutNever:
                radioBtnNever.setChecked(true);
                break;
        }
    }

    private void setupDreamAction(DreamUtils.DreamInfo dreamInfo) {
        DreamUtils.setActiveDream(dreamInfo.componentName);
        switch (dreamInfo.caption.toString()) {
            case "照片桌面":
            case "相框":
                DreamUtils.launchSettings(getContext(), dreamInfo);
                break;
        }
    }

    private void getTimerTask() {
        Set<String> planSet = SPUtils.getInstance().getStringSet(SP_KEY_TIMER_PLAN);
        if (planSet.size() > 0) {
            List<String> dataList = new ArrayList<>(planSet);
            TimerAdapter timerAdapter = new TimerAdapter(dataList);
            timerAdapter.setSetOnClickListener((view, date) -> {
                TimerDialog timerDialog = new TimerDialog(getActivity(), TimerDialog.TYPE_FROM_EDIT, date);
                timerDialog.show();
                timerDialog.setSetDialogCallback(() -> {
                    getTimerTask();
                    return null;
                });
                return null;
            });
            rvShutdown.setVisibility(View.VISIBLE);
            rvShutdown.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
            rvShutdown.setAdapter(timerAdapter);
        } else rvShutdown.setVisibility(View.GONE);
    }

    private void updateCameraSelectItem() {
        List<CameraBean> cameraBeanList = getCameraBeanList();
        String cameraCache = SPUtils.getInstance().getString(SharePrefer.CameraSelected);
        CameraBean selectedCameraBean = null;
        if (!TextUtils.isEmpty(cameraCache)) {
            CameraBean cacheCameraBean = new Gson().fromJson(cameraCache, CameraBean.class);
            for (int i = 0; i < cameraBeanList.size(); i++) {
                CameraBean cameraBean = cameraBeanList.get(i);
                if (cacheCameraBean.getCameraId().equals(cameraBean.cameraId)) {
                    cameraDescription.setText(cameraBean.cameraName);
                    selectedCameraBean = cameraBean;
                    break;
                }
            }
        }

        if (selectedCameraBean == null) {
            for (int i = 0; i < cameraBeanList.size(); i++) {
                CameraBean cameraBean = cameraBeanList.get(i);
                if (cameraBean.getType() == CameraBean.TYPE_USB_CAMERA) {
                    SPUtils.getInstance().put(SharePrefer.CameraSelected, new Gson().toJson(cameraBean));
                    cameraDescription.setText(cameraBean.cameraName);
                    selectedCameraBean = cameraBean;
                    break;
                }
            }
        }

        if (selectedCameraBean == null) {
            for (int i = 0; i < cameraBeanList.size(); i++) {
                CameraBean cameraBean = cameraBeanList.get(i);
                if (cameraBean.getType() == CameraBean.TYPE_REMOTE_CAMERA) {
                    SPUtils.getInstance().put(SharePrefer.CameraSelected, new Gson().toJson(cameraBean));
                    cameraDescription.setText(cameraBean.cameraName);
                    selectedCameraBean = cameraBean;
                    break;
                }
            }
        }

        if (selectedCameraBean == null) {
            cameraDescription.setText("无摄像头");
            SPUtils.getInstance().put(SharePrefer.CameraSelected, "");
            CommonUtils.saveGlobalCameraId("");
        } else {
            CommonUtils.saveGlobalCameraId(selectedCameraBean.cameraId);
        }
    }


    private final CompoundButton.OnCheckedChangeListener radioCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                switch (buttonView.getId()) {
                    case R.id.radio_btn_5min:
                        removeRadioButtonChecked();
                        radioBtn5min.setChecked(true);
                        break;
                    case R.id.radio_btn_15min:
                        removeRadioButtonChecked();
                        radioBtn15min.setChecked(true);
                        break;
                    case R.id.radio_btn_30min:
                        removeRadioButtonChecked();
                        radioBtn30min.setChecked(true);
                        break;
                    case R.id.radio_btn_1h:
                        removeRadioButtonChecked();
                        radioBtn1h.setChecked(true);
                        break;
                    case R.id.radio_btn_2h:
                        removeRadioButtonChecked();
                        radioBtn2h.setChecked(true);
                        break;
                    case R.id.radio_btn_never:
                        removeRadioButtonChecked();
                        radioBtnNever.setChecked(true);
                        break;
                }
            }
        }
    };

    private void removeRadioButtonChecked() {
        radioBtn5min.setChecked(false);
        radioBtn15min.setChecked(false);
        radioBtn30min.setChecked(false);
        radioBtn1h.setChecked(false);
        radioBtn2h.setChecked(false);
        radioBtnNever.setChecked(false);
    }

    private void initListener(View view) {
        imgVolume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                volumeRate.setProgress(0);
                setSystemVolume(0);
                setSoundUI(0, 1);
            }
        });
        imgMicrophone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                microphoneRate.setProgress(0);
                setCallVolume(1);
                setSoundUI(1, 2);
            }
        });

        volumeRate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                setSystemVolume(progress);
                if (progress == 0) {
                    isSoundZero = true;
                    microphoneRate.setFocusable(false);
                }

                setSoundUI(progress, 1);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {


            }
        });

        microphoneRate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    isMicrophoneZero = true;
                }
                progress = progress + 1;
                setCallVolume((progress));
                if ((progress) == streamMaxCallVolume) {
                    volumeRate.setFocusable(false);
                }
                setSoundUI(progress, 2);


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //   volumeRate.setThumb(getResources().getDrawable(R.mipmap.img_thumb_white));
        volume.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                dealVolumeBack(hasFocus);
            }
        });
        volumeRate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                dealVolumeBack(hasFocus);
            }
        });

        microphone.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                dealMicrophoneBack(hasFocus);
            }
        });
        microphoneRate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                dealMicrophoneBack(hasFocus);
            }
        });

        screenOutTimeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                llScreenOffTimeout.setVisibility(llScreenOffTimeout.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
                if (llScreenOffTimeout.getVisibility() == View.VISIBLE) {
                    ivScreenOffEnter.setRotation(90f);
                } else ivScreenOffEnter.setRotation(0f);
            }
        });
    }

    private void dealMicrophoneBack(boolean hasFocus) {
        if (hasFocus) {
            if (isMicrophoneZero) {
                imgMicrophone.setImageDrawable(getResources().getDrawable(R.mipmap.img_microphone_closed_white));
            } else {
                imgMicrophone.setImageDrawable(getResources().getDrawable(R.mipmap.img_microphone_white));
            }
            microphoneRateTv.setTextColor(getResources().getColor(R.color.white));
            microphoneTv.setTextColor(getResources().getColor(R.color.white));
            microphone.setBackground(getResources().getDrawable(R.drawable.shape_rectange_gradient_6a6_ae8));
            microphoneRate.setThumb(getResources().getDrawable(R.mipmap.img_thumb_blue));
            microphoneRate.setProgressDrawable(getResources().getDrawable(R.drawable.bg_progress_selected));
        } else {
            if (isMicrophoneZero) {
                imgMicrophone.setImageDrawable(getResources().getDrawable(R.mipmap.img_microphone_closed));
            } else {
                imgMicrophone.setImageDrawable(getResources().getDrawable(R.mipmap.img_microphone));
            }
            microphoneRateTv.setTextColor(getResources().getColor(R.color.src_c99));
            microphoneTv.setTextColor(getResources().getColor(R.color.src_c33));
            microphone.setBackground(getResources().getDrawable(R.drawable.shape_rectangle_white));
            microphoneRate.setThumb(getResources().getDrawable(R.mipmap.img_thumb));
            microphoneRate.setProgressDrawable(getResources().getDrawable(R.drawable.bg_progress));
        }
    }

    private void dealVolumeBack(boolean hasFocus) {
        if (hasFocus) {
            if (isSoundZero) {
                imgVolume.setImageDrawable(getResources().getDrawable(R.mipmap.img_volume_closed_white));
            } else {
                imgVolume.setImageDrawable(getResources().getDrawable(R.mipmap.img_volume_white));
            }

            volumeRateTv.setTextColor(getResources().getColor(R.color.white));
            volumeTv.setTextColor(getResources().getColor(R.color.white));
            volume.setBackground(getResources().getDrawable(R.drawable.shape_rectange_gradient_6a6_ae8));
            volumeRate.setThumb(getResources().getDrawable(R.mipmap.img_thumb_blue));
            volumeRate.setProgressDrawable(getResources().getDrawable(R.drawable.bg_progress_selected));
        } else {
            if (isSoundZero) {
                imgVolume.setImageDrawable(getResources().getDrawable(R.mipmap.img_volume_closed));
            } else {
                imgVolume.setImageDrawable(getResources().getDrawable(R.mipmap.img_volume));
            }
            volumeRateTv.setTextColor(getResources().getColor(R.color.src_c99));
            volumeTv.setTextColor(getResources().getColor(R.color.src_c33));
            volume.setBackground(getResources().getDrawable(R.drawable.shape_rectangle_white));
            volumeRate.setThumb(getResources().getDrawable(R.mipmap.img_thumb));
            volumeRate.setProgressDrawable(getResources().getDrawable(R.drawable.bg_progress));
        }
    }

    //设置系统音量

    private void setSystemVolume(int progress) {
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, AudioManager.FLAG_PLAY_SOUND);
        //audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
    }

    private void setSoundUI(int progress, int type) {
        if (type == 1) {
            last_volume = progress;
            float rate = (((float) progress / streamMaxVolume));
            int percent = (int) (rate * 100);
            volumeRateTv.setText(percent + "");
            if (percent == 0) {
                if (volumeRate.isFocused() || volume.isFocused()) {
                    imgVolume.setImageDrawable(getResources().getDrawable(R.mipmap.img_volume_closed_white));
                } else {
                    imgVolume.setImageDrawable(getResources().getDrawable(R.mipmap.img_volume_closed));
                }
                isSoundZero = true;

            } else {
                if (volumeRate.isFocused() || volume.isFocused()) {
                    imgVolume.setImageDrawable(getResources().getDrawable(R.mipmap.img_volume_white));
                } else {
                    imgVolume.setImageDrawable(getResources().getDrawable(R.mipmap.img_volume));
                }
                isSoundZero = false;

            }
        } else if (type == 2) {
            float rate = (((float) (progress - 1) / (streamMaxCallVolume - 1)));
            int percent = (int) (rate * 100);
            microphoneRateTv.setText(percent + "");
            if (percent == 0) {
                if (microphoneRate.isFocused() || microphone.isFocused()) {
                    imgMicrophone.setImageDrawable(getResources().getDrawable(R.mipmap.img_microphone_closed_white));
                } else {
                    imgMicrophone.setImageDrawable(getResources().getDrawable(R.mipmap.img_microphone_closed));
                }
                isMicrophoneZero = true;
            } else {
                if (microphoneRate.isFocused() || microphone.isFocused()) {
                    imgMicrophone.setImageDrawable(getResources().getDrawable(R.mipmap.img_microphone_white));
                } else {
                    imgMicrophone.setImageDrawable(getResources().getDrawable(R.mipmap.img_microphone));
                }
                isMicrophoneZero = false;

            }
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.BACK_REFRESH) {

        }
    }

    //设置通话音量
    public void setCallVolume(int volume) {
        audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, volume, AudioManager.FLAG_PLAY_SOUND);
        // AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.volume:
            case R.id.volume_rate:
                volumeRate.setFocusable(true);
                volumeRate.requestFocus();
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
                break;
            case R.id.microphone:
            case R.id.microphone_rate:
                microphoneRate.setFocusable(true);
                microphoneRate.requestFocus();
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
                break;
            case R.id.resolution:
            case R.id.key_sound:
            case R.id.microphone_select:
                ToastUtils.showToast(getActivity(), "敬请期待");
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                break;
            case R.id.camera_select:
                if (isCameraClickValid()) {
                    if (isCameraSelectListOpened) {
                        cameraListRecyclerView.setVisibility(View.GONE);
                        isCameraSelectListOpened = false;
                        ((SettingActivity) getContext()).callCameraClose();
                        cameraArrow.setImageResource(R.drawable.bg_arrow_focus_selector);
                    } else {
                        cameraListRecyclerView.setVisibility(View.VISIBLE);
                        isCameraSelectListOpened = true;
                        if (getCameraBeanList().size() > 0) {
                            showCameraSelectList();
                        }
                        cameraArrow.setImageResource(R.drawable.bg_arrow_down_focus_selector);
                    }
                }
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                break;
            case R.id.camera_wireless_select:
                boolean hasCommonUpdate = SystemProperties.getBoolean(BaseConstants.SP_KEY_CAMERA_UPDATE_STATE, true);
                if (hasCommonUpdate) {
                    badgeCamera.setVisible(false);
                    badgeCameraCallback.accept(false);
                    startActivity(new Intent(v.getContext(), CameraDescriptionActivity.class));
                    getActivity().finish();
                } else {
                    startActivity(new Intent(v.getContext(), GuideCameraActivity.class));
                    getActivity().finish();
                }
                break;
            case R.id.layout_timer_shutdown:
                switchTimerShutDown();
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                break;
            case R.id.layout_add_shutdown_plan:
                TimerDialog timerDialog = new TimerDialog(getActivity(), TimerDialog.TYPE_FROM_ADD, AlarmSetter.Companion.getDateFormat().format(new Date()));
                timerDialog.show();
                timerDialog.setSetDialogCallback(() -> {
                    getTimerTask();
                    return null;
                });
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
                break;
            case R.id.layout_dream:
                hasShowDreamActionList = !hasShowDreamActionList;
                rvDreamActionList.setVisibility(hasShowDreamActionList ? View.VISIBLE : View.GONE);
                ivDreamArrow.setRotation(hasShowDreamActionList ? 90f : 0f);
                break;
            case R.id.screen_timeout_5min:
                setupScreenOffTimeout(300000);
                radioBtn5min.setChecked(true);
                setScreenOffTimeOutTitle(screenOffTimeout5Min);
                changeScreenOffTimeOut();
                break;
            case R.id.screen_timeout_15min:
                setupScreenOffTimeout(900000);
                radioBtn15min.setChecked(true);
                setScreenOffTimeOutTitle(screenOffTimeout15Min);
                changeScreenOffTimeOut();
                break;
            case R.id.screen_timeout_30min:
                setupScreenOffTimeout(1800000);
                radioBtn30min.setChecked(true);
                setScreenOffTimeOutTitle(screenOffTimeout30Min);
                changeScreenOffTimeOut();
                break;
            case R.id.screen_timeout_1h:
                setupScreenOffTimeout(3600000);
                radioBtn1h.setChecked(true);
                setScreenOffTimeOutTitle(screenOffTimeout1H);
                changeScreenOffTimeOut();
                break;
            case R.id.screen_timeout_2h:
                setupScreenOffTimeout(7200000);
                radioBtn2h.setChecked(true);
                setScreenOffTimeOutTitle(screenOffTimeout2H);
                changeScreenOffTimeOut();
                break;
            case R.id.screen_timeout_never:
                setupScreenOffTimeout(Integer.MAX_VALUE);
                radioBtnNever.setChecked(true);
                setScreenOffTimeOutTitle(screenOffTimeoutNever);
                changeScreenOffTimeOut();
                break;
            default:
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                break;
        }
    }

    private void changeScreenOffTimeOut() {
        screenOutTimeLayout.requestFocus();
        llScreenOffTimeout.setVisibility(View.GONE);
        ivScreenOffEnter.setRotation(0f);
    }

    @SuppressLint("SetTextI18n")
    private void setScreenOffTimeOutTitle(String content) {
        if (content.equals("永不")) {
            tvScreenOffTimeoutTitle.setText(content);
            return;
        }
        tvScreenOffTimeoutTitle.setText("闲置" + content + "后");
    }

    private void setupScreenOffTimeout(int value) {
        boolean retVal = true;
        retVal = Settings.System.canWrite(getContext());
        if (retVal) {
            Settings.System.putInt(getContext().getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, value);
        }
    }

    private String getScreenOffTimeoutTitle() {
        try {
            int screenOutTime = Settings.System.getInt(getContext().getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT);
            String timeout;
            switch (screenOutTime) {
                case 300000:
                    timeout = screenOffTimeout5Min;
                    break;
                case 900000:
                    timeout = screenOffTimeout15Min;
                    break;
                case 1800000:
                    timeout = screenOffTimeout30Min;
                    break;
                case 3600000:
                    timeout = screenOffTimeout1H;
                    break;
                case 7200000:
                    timeout = screenOffTimeout2H;
                    break;
                case Integer.MAX_VALUE:
                    timeout = screenOffTimeoutNever;
                    break;
                default:
                    timeout = "";
            }
            return timeout;
        } catch (Settings.SettingNotFoundException ignored) {

        }
        return "";
    }

    private void switchTimerShutDown() {
        shutdownSwitch.setChecked(!shutdownSwitch.isChecked());
        if (shutdownSwitch.isChecked()) {
            SPUtils.getInstance().put(BaseConstants.SP_KEY_TIMER_PLAN_ENABLE, shutdownSwitch.isChecked());
            llShouDownDetails.setVisibility(View.VISIBLE);
            getTimerTask();
            AlarmSetter.AlarmUtil.INSTANCE.openAllAlarmTask(getActivity());
            return;
        }
        SPUtils.getInstance().put(BaseConstants.SP_KEY_TIMER_PLAN_ENABLE, shutdownSwitch.isChecked());
        llShouDownDetails.setVisibility(View.GONE);
        AlarmSetter.AlarmUtil.INSTANCE.stopAllAlarmTask(getActivity());
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        int id = v.getId();
        if (id == R.id.volume || id == R.id.microphone) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
        } else if (id == R.id.volume_rate || id == R.id.microphone_rate) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
        } else {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        }

    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden && isAdded()) {
            //显示
            Log.i(TAG, "显示通用界面");
            updateCameraSelectItem();
        } else {
            //隐藏
            Log.i(TAG, "隐藏通用界面");
            cameraListRecyclerView.setVisibility(View.GONE);
            isCameraSelectListOpened = false;
            ((SettingActivity) getContext()).callCameraClose();
            cameraArrow.setImageResource(R.drawable.bg_arrow_focus_selector);
        }
    }

    private List<CameraBean> getCameraBeanList() {
        if (ConnectService.getInstance() != null) {
            return ConnectService.getInstance().getCameraBeanList();
        }
        return new ArrayList<>();
    }

    public boolean isCameraSelectListOpened = false;

    public void openCameraSelectList() {
        isCameraSelectListOpened = true;
        cameraArrow.setImageResource(R.drawable.bg_arrow_down_focus_selector);
        cameraListRecyclerView.setVisibility(View.VISIBLE);
        showCameraSelectList();
    }

    private boolean isCameraClickValid() {
        if (cameraSelectLayout.getTag() != null) {
            long lastTime = (Long) (cameraSelectLayout.getTag());
            if (System.currentTimeMillis() - lastTime > 1200) {
                cameraSelectLayout.setTag(System.currentTimeMillis());
                return true;
            }
            return false;
        }
        cameraSelectLayout.setTag(System.currentTimeMillis());
        return true;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void showCameraSelectList() {
        List<CameraBean> cameraBeanList = getCameraBeanList();
        if (cameraInfoListAdapter == null) {
            cameraInfoListAdapter = new CameraInfoAdapter(cameraBeanList);
            cameraInfoListAdapter.setHasStableIds(true);
            cameraListRecyclerView.setAdapter(cameraInfoListAdapter);

            cameraInfoListAdapter.setItemClick((view, cameraBean) -> {
                if (isCameraClickValid()) {
                    SPUtils.getInstance().put(SharePrefer.CameraSelected, new Gson().toJson(cameraBean));
                    cameraInfoListAdapter.setSelectedCameraId(cameraBean.cameraId);
                    CommonUtils.saveGlobalCameraId(cameraBean.cameraId);
                    cameraInfoListAdapter.notifyDataSetChanged();
                    ((SettingActivity) view.getContext()).onCameraSelectChanged(cameraBean);
                    cameraDescription.setText(cameraBean.cameraName);
                }
            });

            cameraInfoListAdapter.setOnItemFocusedListener(v -> {
                if (cameraListRecyclerView.getVisibility() == View.GONE) {
                    cameraSelectLayout.requestFocus();
                }
            });
        } else {
            cameraInfoListAdapter.updateDataList(cameraBeanList);
        }

        cameraSelectLayout.requestFocus();

        //读取缓存进行选中
        cameraListRecyclerView.postDelayed(() -> {
            String cameraCache = SPUtils.getInstance().getString(SharePrefer.CameraSelected);
            CameraBean selectedCameraBean = null;
            if (!TextUtils.isEmpty(cameraCache)) {
                CameraBean cacheCameraBean = new Gson().fromJson(cameraCache, CameraBean.class);
                for (int i = 0; i < cameraBeanList.size(); i++) {
                    CameraBean cameraBean = cameraBeanList.get(i);
                    if (cacheCameraBean.getCameraId().equals(cameraBean.cameraId)) {
                        cameraInfoListAdapter.setSelectedCameraId(cameraBean.cameraId);
                        selectedCameraBean = cameraBean;
                        break;
                    }
                }
            }

            if (selectedCameraBean == null) {
                for (int i = 0; i < cameraBeanList.size(); i++) {
                    CameraBean cameraBean = cameraBeanList.get(i);
                    if (cameraBean.getType() == CameraBean.TYPE_USB_CAMERA) {
                        SPUtils.getInstance().put(SharePrefer.CameraSelected, new Gson().toJson(cameraBean));
                        cameraInfoListAdapter.setSelectedCameraId(cameraBean.cameraId);
                        selectedCameraBean = cameraBean;
                        break;
                    }
                }
            }

            if (selectedCameraBean == null) {
                for (int i = 0; i < cameraBeanList.size(); i++) {
                    CameraBean cameraBean = cameraBeanList.get(i);
                    if (cameraBean.getType() == CameraBean.TYPE_REMOTE_CAMERA) {
                        SPUtils.getInstance().put(SharePrefer.CameraSelected, new Gson().toJson(cameraBean));
                        cameraInfoListAdapter.setSelectedCameraId(cameraBean.cameraId);
                        selectedCameraBean = cameraBean;
                        break;
                    }
                }
            }

            cameraInfoListAdapter.notifyDataSetChanged();

            cameraSelectLayout.requestFocus();

            if (selectedCameraBean != null) {
                CommonUtils.saveGlobalCameraId(selectedCameraBean.cameraId);
                ((SettingActivity) cameraListRecyclerView.getContext()).onCameraSelectChanged(selectedCameraBean);
                cameraDescription.setText(selectedCameraBean.cameraName);
            } else {
                CommonUtils.saveGlobalCameraId("");
                cameraDescription.setText("无摄像头");
            }
        }, 20);

        commonNestedScrollView.smoothScrollBy(0, 300);
    }

    public void updateCameraInfo() {
        if (ConnectService.getInstance() != null) {
            if (ConnectService.getInstance().isExistRemoteCamera()) {
                tvCameraDesc.setText("已连接");
            } else {
                tvCameraDesc.setText("未连接");
            }
        }
    }

    public void setBadgeCameraCallback(Consumer<Boolean> badgeCameraCallback) {
        this.badgeCameraCallback = badgeCameraCallback;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
