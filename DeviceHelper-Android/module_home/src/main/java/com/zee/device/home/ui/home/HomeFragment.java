package com.zee.device.home.ui.home;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.youth.banner.indicator.CircleIndicator;
import com.zee.device.base.config.BaseConstants;
import com.zee.device.base.config.SharePrefer;
import com.zee.device.base.db.DatabaseController;
import com.zee.device.base.dialog.UpgradeTipDialog;
import com.zee.device.base.model.DeviceInfo;
import com.zee.device.base.utils.ApkUtil;
import com.zee.device.base.utils.SPUtils;
import com.zee.device.base.utils.ToastUtils;
import com.zee.device.home.databinding.FragmentHomeBinding;
import com.zee.device.home.ui.device.DeviceListActivity;
import com.zee.device.home.ui.home.adapter.CourseEntryAdapter;
import com.zee.device.home.ui.home.adapter.ImageAdapter;
import com.zee.device.home.ui.home.adapter.QuickEntryAdapter;
import com.zee.device.home.data.DataRepository;
import com.zee.device.home.data.protocol.response.UpgradeResp;
import com.zee.device.home.ui.home.model.CourseEntryItem;
import com.zee.device.home.ui.home.model.LoadState;
import com.zee.device.home.ui.home.model.QuickEntryItem;
import com.zee.device.home.ui.home.upgrade.UpgradeDialogActivity;
import com.zee.wireless.camera.ui.WirelessCameraActivity;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements QuickEntryAdapter.QuickEntryItemClickListener,CourseEntryAdapter.CourseEntryItemClickListener {
    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;
    private ActivityResultLauncher<Intent> qrcodeScanLauncher;
    private DeviceInfo selectedDeviceInfo;
    private final ActivityResultLauncher<Intent> deviceListLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        updateSelectedDeviceInfo();
    });


    private List<String> images;
    public static final String TAG = "HomeFragment";

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        /*qrcodeScanLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                if(data != null){
                    String scanResult = data.getStringExtra(Intents.Scan.RESULT);
                    handleScanResult(scanResult);
                }
            }
        });*/
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        HomeViewModelFactory factory = new HomeViewModelFactory(DataRepository.getInstance());
        homeViewModel = new ViewModelProvider(this, factory).get(HomeViewModel.class);
        checkAppUpdate();
        initListener();
        initObserver();
        initData();

    }


    private void initListener() {
        binding.tvHomeSelectedDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(v.getContext(), DeviceListActivity.class);
                deviceListLauncher.launch(intent);
            }
        });
        binding.ivHomeQrcodeScan.setOnClickListener(v -> requestPermissions());
    }

    private void updateSelectedDeviceInfo(){
        String deviceSn = SPUtils.getInstance().getString(SharePrefer.SELECTED_DEVICE_SN);
        if(!TextUtils.isEmpty(deviceSn)){
            selectedDeviceInfo = DatabaseController.instance.getDeviceInfoBySN(deviceSn);
        }
        if(selectedDeviceInfo == null){
            List<DeviceInfo> deviceInfoList = DatabaseController.instance.getAllDeviceInfo("");
            if(deviceInfoList.size() > 0){
                selectedDeviceInfo = deviceInfoList.get(0);
            }
        }

        if(selectedDeviceInfo != null) {
            SPUtils.getInstance().put(SharePrefer.SELECTED_DEVICE_SN, selectedDeviceInfo.sn);
            binding.tvHomeSelectedDevice.setText("紫星PRO " + selectedDeviceInfo.sn);
        }else{
            SPUtils.getInstance().put(SharePrefer.SELECTED_DEVICE_SN, "");
            binding.tvHomeSelectedDevice.setText("您还未添加设备呢");
        }
    }

    private void initData() {
        updateSelectedDeviceInfo();

        List<QuickEntryItem> dataList = new ArrayList<>();
        dataList.add(new QuickEntryItem(com.zee.resource.R.mipmap.icon_add_device, "添加设备", 0));
        dataList.add(new QuickEntryItem(com.zee.resource.R.mipmap.icon_connect_camera, "摄像头连接", 1));

        dataList.add(new QuickEntryItem(com.zee.resource.R.mipmap.icon_ai_helper, "AI助手", 2));
        dataList.add(new QuickEntryItem(com.zee.resource.R.mipmap.icon_session_record, "对话记录", 3));
        dataList.add(new QuickEntryItem(com.zee.resource.R.mipmap.icon_wifi_set, "WiFi设置", 4));
        dataList.add(new QuickEntryItem(com.zee.resource.R.mipmap.icon_bluetooth_set, "蓝牙设置", 5));
        dataList.add(new QuickEntryItem(com.zee.resource.R.mipmap.icon_sleep_set, "休眠设置", 6));
        dataList.add(new QuickEntryItem(com.zee.resource.R.mipmap.icon_more, "更多", 7));

        QuickEntryAdapter quickEntryAdapter = new QuickEntryAdapter(dataList);
        binding.recyclerViewHomeQuickEntry.setAdapter(quickEntryAdapter);
        quickEntryAdapter.setQuickEntryItemClickListener(this);

        images = new ArrayList<>();
        images.add("https://img.zcool.cn/community/013de756fb63036ac7257948747896.jpg");
        images.add("https://img.zcool.cn/community/01639a56fb62ff6ac725794891960d.jpg");
        images.add("https://img.zcool.cn/community/01270156fb62fd6ac72579485aa893.jpg");
        images.add("https://img.zcool.cn/community/01233056fb62fe32f875a9447400e1.jpg");
        images.add("https://img.zcool.cn/community/016a2256fb63006ac7257948f83349.jpg");

        binding.banner.addBannerLifecycleObserver(this)//添加生命周期观察者
                .setAdapter(new ImageAdapter(images))
                .setIndicator(new CircleIndicator(getActivity()));

      //  binding.banner.setBannerGalleryMZ(60,0.8f);

        List<CourseEntryItem> courseEntryItemList=new ArrayList<>();
        courseEntryItemList.add(new CourseEntryItem(com.zee.resource.R.mipmap.icon_digital_human,"数字人播报"));
        courseEntryItemList.add(new CourseEntryItem(com.zee.resource.R.mipmap.icon_popularize_law,"普法竞答"));
        courseEntryItemList.add(new CourseEntryItem(com.zee.resource.R.mipmap.icon_digital_human,"数字人播报"));
        courseEntryItemList.add(new CourseEntryItem(com.zee.resource.R.mipmap.icon_popularize_law,"普法竞答"));

        CourseEntryAdapter courseEntryAdapter=new CourseEntryAdapter(courseEntryItemList);

        binding.recyclerCourse.setAdapter(courseEntryAdapter);
        courseEntryAdapter.setCourseEntryItemClickListener(this);
        binding.tvMoreCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtils.showShort("敬请期待");
            }
        });

    }

    @Override
    public void onItemClick(View view, QuickEntryItem quickEntryItem) {
        if(quickEntryItem.uid == 0){
            requestPermissions();
        }else if(quickEntryItem.uid == 1){
            if(selectedDeviceInfo != null) {
                Intent intent = new Intent();
                intent.setClass(view.getContext(), WirelessCameraActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(BaseConstants.EXTRA_DEVICE_INFO, selectedDeviceInfo);
                intent.putExtra(BaseConstants.EXTRA_DEVICE_INFO, bundle);
                startActivity(intent);
            }else{
                showToast("请先扫码添加设备！");
            }
        }else{
            showToast("敬请期待！");
        }
    }

    private void requestPermissions(){
        requestPermissions(new  String[]{Manifest.permission.CAMERA}, 1000);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000) {
            boolean allPermissionsGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (allPermissionsGranted) {
                onPermissionsGrantedDone();
            } else {
                showToast("请开启权限！");
            }
        }
    }

    private void onPermissionsGrantedDone(){
        gotoScanActivity();
    }

    private void gotoScanActivity(){
        /*Intent intent = new Intent();
        intent.setAction(Intents.Scan.ACTION);
        intent.setClass(requireContext(), CaptureActivity.class);
        qrcodeScanLauncher.launch(intent);*/
    }

    private synchronized void handleScanResult(String scanResult){
        if (!TextUtils.isEmpty(scanResult) && scanResult.startsWith(BaseConstants.QRCODE_CONTENT_PREFIX)) {
            String[] strArray = scanResult.split(";");
            if (strArray.length >= 6) {
                try {
                    DeviceInfo deviceInfo = new DeviceInfo();
                    deviceInfo.sn = strArray[1];
                    deviceInfo.ip = strArray[2];
                    deviceInfo.port = Integer.parseInt(strArray[3]);
                    deviceInfo.mac = strArray[4];
                    deviceInfo.name = strArray[5];

                    DeviceInfo existDeviceInfo = DatabaseController.instance.getDeviceInfoBySN(deviceInfo.sn);
                    if (existDeviceInfo != null) {
                        DatabaseController.instance.updateDeviceInfo(deviceInfo);
                    } else {
                        DatabaseController.instance.addDeviceInfo(deviceInfo);
                    }

                    if(selectedDeviceInfo == null){
                        selectedDeviceInfo = DatabaseController.instance.getDeviceInfoBySN(deviceInfo.sn);
                        if(selectedDeviceInfo != null){
                            SPUtils.getInstance().put(SharePrefer.SELECTED_DEVICE_SN, selectedDeviceInfo.sn);
                            binding.tvHomeSelectedDevice.setText("紫星PRO " + selectedDeviceInfo.sn);
                        }
                    }else if(selectedDeviceInfo.sn.equals(deviceInfo.sn)){
                        selectedDeviceInfo = DatabaseController.instance.getDeviceInfoBySN(deviceInfo.sn);
                    }
                    showToast("添加成功！");
                }catch (NumberFormatException ignored){
                    showToast("不支持该二维码！");
                }
            }
        }
    }

    private void showToast(String msg){
        ToastUtils.showShort(msg);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        qrcodeScanLauncher.unregister();
    }


    @Override
    public void onItemClick(View view, CourseEntryItem courseEntryItem) {
        ToastUtils.showShort(courseEntryItem.title);

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void initObserver() {
        homeViewModel.mldHostAppUpgradeState.observe(this, loadState -> {
            if (LoadState.Success == loadState) {
                if (homeViewModel.hostAppUpgradeResp != null) {
                    showUpgradeTipDialog(getActivity(), homeViewModel.hostAppUpgradeResp);
                }
            }else if(LoadState.Failed == loadState){
                ToastUtils.showShort("获取更新数据失败");
            }
        });
    }

    private void checkAppUpdate() {
        String version = ApkUtil.getAppVersionName(getActivity());
        if ((version != null) ) {
            homeViewModel.reqHostAppUpgrade(version);
        }
    }

    private UpgradeTipDialog upgradeTipDialog;
    public void showUpgradeTipDialog(final Context context, final UpgradeResp upgradeResp) {
        if(upgradeTipDialog == null){
            upgradeTipDialog = new UpgradeTipDialog(context);
            upgradeTipDialog.setTitleText("检测到新版本");
            upgradeTipDialog.setMessageText("V" + upgradeResp.getSoftwareVersion());
            upgradeTipDialog.showConfirmButton(upgradeResp.isForcible());
            upgradeTipDialog.setOnClickListener(new UpgradeTipDialog.OnClickListener() {
                @Override
                public void onConfirm(View v) {
                    upgradeTipDialog.cancel();
                    upgradeTipDialog = null;
                    UpgradeDialogActivity.showUpgradeDialog(context, upgradeResp);
                }

                @Override
                public void onPositive(View v) {
                    upgradeTipDialog.cancel();
                    upgradeTipDialog = null;
                    UpgradeDialogActivity.showUpgradeDialog(context, upgradeResp);
                }

                @Override
                public void onCancel(View v) {
                    upgradeTipDialog.cancel();
                    upgradeTipDialog = null;
                  //  checkLoginAndReqTheme();
                }
            });
        }
        if(!upgradeTipDialog.isShowing())
            upgradeTipDialog.show();
    }

    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {// 不在最前端界面显示
            Log.i(TAG,"不在最前端界面显示");
        } else {// 重新显示到最前端中
            Log.i(TAG,"重新显示到最前端中");
            checkAppUpdate();
        }


    }


}