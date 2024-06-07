package com.zee.setting.fragment;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.gson.Gson;
import com.zee.setting.R;
import com.zee.setting.activity.NetSpeedActivity;
import com.zee.setting.activity.WifiActivity;
import com.zee.setting.adapter.SavedWifiAdapter;
import com.zee.setting.adapter.WirelessWifiAdapter;
import com.zee.setting.bean.WifiDhcp;
import com.zee.setting.bean.WifiResult;
import com.zee.setting.cache.SPUtils;
import com.zee.setting.cache.SharePrefer;
import com.zee.setting.receive.NetworkChangeReceiver;
import com.zee.setting.receive.WifiBroadcastReceiver;
import com.zee.setting.utils.Constant;
import com.zee.setting.utils.DataCleanManagerUtils;
import com.zee.setting.utils.DensityUtils;
import com.zee.setting.utils.EthernetUtils;
import com.zee.setting.utils.IpGetUtil;
import com.zee.setting.utils.NetworkUtil;
import com.zee.setting.utils.ToastUtils;
import com.zee.setting.utils.WifiUtil;
import com.zee.setting.views.BaseDialog;

import net.sunniwell.aar.focuscontrol.layout.FocusControlConstraintLayout;
import net.sunniwell.aar.focuscontrol.layout.FocusControlLinearLayout;
import net.sunniwell.aar.focuscontrol.layout.FocusControlRecyclerView;
import net.sunniwell.aar.focuscontrol.manager.FocusControlLinearLayoutManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class FragmentNetwork extends Fragment {

    private boolean isOpenWired;
    private boolean isOpenWirelessSwitch;
    private boolean isOpenManualSet;
    private Switch wirelessSwitch;

    private Handler handler = new Handler();
    private WirelessWifiAdapter wirelessWifiAdapter;
    private WifiManager wifiManager;
    private Context context;
    private WifiBroadcastReceiver broadcastReceiver;
    private static final String TAG = "fragmentNetwork";
    private List<WifiResult> wifiResultList;
    private HashMap<String, ScanResult> scanResultHashMap = new HashMap<>();
    private String backWifi;
    private FocusControlConstraintLayout wiredNetwork;
    private FocusControlConstraintLayout wirelessNetwork;
    private FocusControlRecyclerView wifiList;

    private EditText ipv4Edit;
    private EditText subnetMaskEdit;
    private EditText defaultGatewayEdit;
    private EditText dnsServerEdit;
    private FocusControlLinearLayout wiredDetailLayout;
    private ImageView arrow;
    private Switch wiredSwitch;
    private FocusControlConstraintLayout manualSet;
    private FocusControlConstraintLayout ipv4Set;
    private TextView ipv4Title;
    private FocusControlConstraintLayout subnetMask;
    private TextView subnetMaskTitle;
    private FocusControlConstraintLayout defaultGateway;
    private TextView defaultGatewayTitle;
    private FocusControlConstraintLayout dnsServer;
    private TextView dnsServerTitle;
    private TextView confirm;
    private TextView cancel;
    private FocusControlConstraintLayout measureNetwork;
    private FocusControlConstraintLayout savedNetworkLayout;
    private FocusControlRecyclerView savedWifiListRecyclerView;
    private ImageView ivSavedNetworkArrow;
    private TextView tvSavedNetworkDesc;
    private NestedScrollView networkNestedScrollView;
    private TextView wiredStatus;
    private String originWiredIpv4;
    private String originWiredGateway;
    private String originWiredNetMask;
    private String originWiredDns1;
    private String originWiredDns2;
    private boolean isInitWifi;
    private NetworkChangeReceiver networkChangeReceiver;
    //    private String backStatus;
    private int clickPosition = -1;
    private boolean isCurrentFocused;
    private TextView wirelessTitle;
    private ProgressBar progressWifi;
    private boolean isHide;
    private boolean initEthenet = false;
    private boolean startScan;
    public static final String WIFI_AUTH_OPEN = "";
    public static final String WIFI_AUTH_ROAM = "[ESS]";
    private boolean isNetChange=false;

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
      /*  binding = FragmentNetworkBinding.inflate(inflater, container, false);
        context = container.getContext();
        return binding.getRoot();*/
        View view = inflater.inflate(R.layout.fragment_network, container, false);
        wiredNetwork = view.findViewById(R.id.wired_network);
        wirelessNetwork = view.findViewById(R.id.wireless_network);
        wirelessTitle = view.findViewById(R.id.wireless_title);
        measureNetwork = view.findViewById(R.id.measure_network);
        measureNetwork.setVisibility(View.VISIBLE);
        wifiList = view.findViewById(R.id.wifi_list);
        wirelessSwitch = view.findViewById(R.id.wireless_switch);

        savedNetworkLayout = view.findViewById(R.id.fccl_saved_network);
        savedWifiListRecyclerView = view.findViewById(R.id.saved_wifi_list);
        ivSavedNetworkArrow = view.findViewById(R.id.iv_saved_network_arrow);
        tvSavedNetworkDesc = view.findViewById(R.id.tv_saved_network_desc);
        networkNestedScrollView = view.findViewById(R.id.nsv_network);

        wiredDetailLayout = view.findViewById(R.id.wired_detail_layout);
        arrow = view.findViewById(R.id.arrow);
        wiredSwitch = view.findViewById(R.id.wired_switch);
        manualSet = view.findViewById(R.id.manual_set);
        ipv4Set = view.findViewById(R.id.ipv4_set);
        ipv4Title = view.findViewById(R.id.ipv4_title);
        ipv4Edit = view.findViewById(R.id.ipv4_edit);
        subnetMask = view.findViewById(R.id.subnet_mask);
        subnetMaskTitle = view.findViewById(R.id.subnet_mask_title);
        subnetMaskEdit = view.findViewById(R.id.subnet_mask_edit);
        defaultGateway = view.findViewById(R.id.default_gateway);
        defaultGatewayTitle = view.findViewById(R.id.default_gateway_title);
        defaultGatewayEdit = view.findViewById(R.id.default_gateway_edit);
        dnsServer = view.findViewById(R.id.dns_server);
        dnsServerTitle = view.findViewById(R.id.dns_server_title);
        dnsServerEdit = view.findViewById(R.id.dns_server_edit);
        confirm = view.findViewById(R.id.confirm);
        cancel = view.findViewById(R.id.cancel);
        wiredStatus = view.findViewById(R.id.wired_status);
        progressWifi = view.findViewById(R.id.progress_bar_wifi_setting);
        // nestedScrollView = view.findViewById(R.id.scrollView);


        context = container.getContext();

        DensityUtils.autoWidth(getActivity().getApplication(), getActivity());
        return view;
    }

    private void initWiFi() {
        isInitWifi = true;
     //   wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        broadcastReceiver = new WifiBroadcastReceiver(getActivity().getApplicationContext(), wifiManager);
        setWifiScanner();
        broadcastReceiver.setWifiStateChangeListener(new WifiBroadcastReceiver.WifiStateChangeListener() {
            @Override
            public void onWifiChange(Intent intent) {
                if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                    int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                    switch (state) {
                        /*
                         * WIFI_STATE_DISABLED    WLAN已经关闭
                         * WIFI_STATE_DISABLING   WLAN正在关闭
                         * WIFI_STATE_ENABLED     WLAN已经打开
                         * WIFI_STATE_ENABLING    WLAN正在打开
                         * WIFI_STATE_UNKNOWN     未知*/


                        case WifiManager.WIFI_STATE_DISABLED: {
                            Log.i(TAG, "已经关闭");
                            break;
                        }
                        case WifiManager.WIFI_STATE_DISABLING: {
                            Log.i(TAG, "正在关闭");
                            break;
                        }
                        case WifiManager.WIFI_STATE_ENABLED: {
                            Log.i(TAG, "已经打开");
                            break;
                        }
                        case WifiManager.WIFI_STATE_ENABLING: {
                            Log.i(TAG, "正在打开");
                            break;
                        }
                        case WifiManager.WIFI_STATE_UNKNOWN: {
                            Log.i(TAG, "未知状态");
                            break;
                        }
                    }
                } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                    NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                    Log.i(TAG, "--NetworkInfo--" + info.toString());
                    if (NetworkInfo.State.DISCONNECTED == info.getState()) {//wifi没连接上
                        Log.i(TAG, "wifi没连接上");
                    } else if (NetworkInfo.State.CONNECTED == info.getState()) {//wifi连接上了
                        Log.i(TAG, "wifi连接上了");
                        getScanResult();
                    } else if (NetworkInfo.State.CONNECTING == info.getState()) {//正在连接
                        Log.i(TAG, "wifi正在连接");
                    }
                } else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {
                    Log.i(TAG, "网络列表变化了");
                    //ToastUtils.showToast(getActivity(), "网络列表变化了");
                    // checkToScanning();
                    getScanResult();
                    progressWifi.setVisibility(View.GONE);


                }else if(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                    //密码监听
                 /*   int linkWifiResult = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, 123);
                    if (linkWifiResult == WifiManager.ERROR_AUTHENTICATING) {
                       Toast.makeText(getActivity(),"密码错误",Toast.LENGTH_SHORT).show();
                    }*/
                    SupplicantState supplicantState = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);

                    NetworkInfo.DetailedState state = WifiInfo.getDetailedStateOf(supplicantState);
                    int error = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1);
                    Log.e(TAG, "当前网络连接状态码：" + error + "；连接状态 --->>> " + state);

                    if (error == WifiManager.ERROR_AUTHENTICATING) {
                        String ssidName = wifiManager.getConnectionInfo().getSSID();
                      //  Log.e(TAG, ssidName + "：密码错误，连接状态 --->>>" + state +"，connectType：" + connectType);
                        //状态为 DISCONNECTED 断开的 时候才是真的密码错误
                        if(state == NetworkInfo.DetailedState.DISCONNECTED){
                            //用系统自己的方式忘记wifi
                          //  Toast.makeText(getActivity(),"密码错误="+ssidName,Toast.LENGTH_SHORT).show();
                            Log.i(TAG,"密码错误="+ssidName);
                          //  boolean b = WifiSupport.forgetWifiNetWork(ssidName, mContext);//连接失败后需要忘记密码，不然会因为重连机制导致反复广播
                            //密码错误时清空保持中的密码
                            //PreferenceHelper.write(mContext,ssidName,"");
                        }else if(state == NetworkInfo.DetailedState.SCANNING){
                            //密码正确的时，第一次会走密码错误的逻辑 进入 SCANNING 扫描的状态，或者进入 CONNECTING 连接的状态

                        }
                    }
                }
            }
        });
    }

    private void setWifiScanner() {
        //默认打开
        wiredStatus.setText("未连接");
        isOpenWirelessSwitch = true;
        wirelessSwitch.setChecked(true);
        wirelessSwitch.setTrackDrawable(getResources().getDrawable(R.mipmap.img_open));
        //Log.i("ssshhh","默认打开");
        initEthenet = false;
        wifiList.setVisibility(View.VISIBLE);
        measureNetwork.setVisibility(View.GONE);
        wirelessNetwork.setBackground(getActivity().getResources().getDrawable(R.drawable.bg_network_focus_selector));
        wirelessNetwork.setFocusable(true);
        wirelessTitle.setFocusable(true);
        wirelessSwitch.setFocusable(true);

//        wiredNetwork.setFocusable(false);
//        wiredNetwork.setEnabled(false);
        wirelessNetwork.setFocusable(true);
        wirelessNetwork.setEnabled(true);
        if (wifiManager != null) {
            checkToScanning();
        }
        closeWiredDetail();

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initAdapter();
        initData();
        initListener();
    }


    public void initAdapter() {
        wifiResultList = new ArrayList<>();
        FocusControlLinearLayoutManager managerVertical = new FocusControlLinearLayoutManager(getActivity());
        managerVertical.setOrientation(LinearLayoutManager.VERTICAL);
        wifiList.setLayoutManager(managerVertical);
        // wifiList.setNestedScrollingEnabled(false);
        wirelessWifiAdapter = new WirelessWifiAdapter(wifiResultList, getActivity());
        wirelessWifiAdapter.setHasStableIds(true);
        wifiList.setAdapter(wirelessWifiAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i(TAG, "onActivityResult");
        if (requestCode == Constant.BACK_REFRESH) {
            if (data != null) {
                backWifi = data.getStringExtra("name");
                //   Log.i("ssshhh", "backWifi=" + backWifi);
                startScan = false;
                checkToScanning();

            }
        }
    }

    private synchronized void checkToScanning() {
        if (wifiManager.isWifiEnabled()) {
            boolean result = wifiManager.startScan();
            if (result) {
                progressWifi.setVisibility(View.VISIBLE);
                getScanResult();
            }
        }
    }


    private void initData() {
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        networkChangeReceiver = new NetworkChangeReceiver(getActivity());
        networkChangeReceiver.setNetworkChangeListener(new NetworkChangeReceiver.NetworkChangeListener() {
            @Override
            public void onNetworkChange(int status) {
                isNetChange = true;
                if (status == NetworkUtil.NETWORK_ETHERNET) {
                    Log.i(TAG, "aa_当前是有线网");
                   // ToastUtils.showToast(getActivity(), "aa_当前是有线网");
                    getEthernetInfo();
                } else if (status == NetworkUtil.NETWORK_WIFI) {
                    Log.i(TAG, "aa_当前是Wifi");
                   // ToastUtils.showToast(getActivity(), "aa_当前是Wifi");
                    if (isInitWifi == false) {
                        initWiFi();
                    } else {
                        setWifiScanner();
                    }

                } else if (status == NetworkUtil.NETWORK_NO) {
                    Log.i(TAG, "aa_当前没用网络");
                    // ToastUtils.showToast(getActivity(),"aa_当前没有网络");
                    boolean wiFiActive = WifiUtil.isWiFiActive(getActivity());
                    if (wiFiActive || initEthenet) {
                      //  ToastUtils.showToast(getActivity(), "aa_当前网络意见打开没有连接");
                        setWifiScanner();
                    }

                }
            }
        });

        //刚开始进来网络没有变化只是打开未连接wifi状态
        if (WifiUtil.isWiFiActive(getActivity()) && (NetworkUtil.isNetworkAvailable(getActivity())==false) && (isNetChange==false)) {
            setWifiScanner();
        }

        updateSavedNetworkLayout();
    }


    private void getEthernetInfo() {
        int useDhcpOrStaticIp = EthernetUtils.getEthUseDhcpOrStaticIp(getActivity());
        if (useDhcpOrStaticIp == 1) {
            wiredSwitch.setChecked(true);
            // com.zee.setting.utils.ToastUtils.showToast(getActivity(), "以太网是静态的");
        } else if (useDhcpOrStaticIp == 2) {
            wiredSwitch.setChecked(false);
            //com.zee.setting.utils.ToastUtils.showToast(getActivity(), "以太网是动态的");
        }
        setEthernetSwitch(wiredSwitch.isChecked());

        wiredStatus.setText("已连接");
        wiredNetwork.setFocusable(true);
        wiredNetwork.setEnabled(true);
        wirelessNetwork.setFocusable(false);
        wirelessNetwork.setEnabled(false);
        //关闭wifi
        isOpenWirelessSwitch = false;
        wirelessSwitch.setChecked(false);
        // Log.i("ssshhh","默认关闭");
        wirelessSwitch.setTrackDrawable(getResources().getDrawable(R.mipmap.img_close));
        initEthenet = true;
        wifiList.setVisibility(View.GONE);
        measureNetwork.setVisibility(View.VISIBLE);
        wirelessNetwork.setBackground(getActivity().getResources().getDrawable(R.drawable.shape_rectangle_grey_999));
        wirelessNetwork.setFocusable(false);
        wirelessTitle.setFocusable(false);
        wirelessSwitch.setFocusable(false);
        originWiredIpv4 = EthernetUtils.getIpAddressForInterfaces();
        originWiredGateway = EthernetUtils.getGateWay();
        // originWiredNetMask = EthernetUtils.getEth0Mask();
        originWiredNetMask = EthernetUtils.getEthernetMask();

        originWiredDns1 = "";
        originWiredDns2 = "";
        List<String> dnsList = EthernetUtils.getEthernetDnsInfo(getActivity());
        if (dnsList != null && dnsList.size() > 0) {
            for (int i = 0; i < dnsList.size(); i++) {
                if (i == 0) {
                    originWiredDns1 = dnsList.get(i);
                } else if (i == 1) {
                    originWiredDns2 = dnsList.get(i);
                }
            }
        }
        ipv4Edit.setText(originWiredIpv4);
        subnetMaskEdit.setText(originWiredNetMask);
        defaultGatewayEdit.setText(originWiredGateway);
        dnsServerEdit.setText(originWiredDns1);
    }


    private synchronized void getScanResult() {
        if (startScan) {
            return;
        }
        startScan = true;
        List<ScanResult> results = wifiManager.getScanResults();
        if (results != null) {
            wifiResultList.clear();
            scanResultHashMap.clear();


            for (ScanResult scanResult : results) {
                //  Log.d("wwwhhh", "scanResult SSID=" + scanResult.SSID + " BSSID=" + scanResult.BSSID + " capabilities=" + scanResult.capabilities);
                if (scanResult.SSID.isEmpty()) continue;
                ScanResult saveScanResult = scanResultHashMap.get(scanResult.SSID);
                if (saveScanResult != null) {
                    if (saveScanResult.level < scanResult.level) {
                        scanResultHashMap.put(scanResult.SSID, scanResult);
                    }
                } else {
                    scanResultHashMap.put(scanResult.SSID, scanResult);
                }

            }

            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            List<WifiConfiguration> wifiConfigurationList = wifiManager.getConfiguredNetworks();


            for (String key : scanResultHashMap.keySet()) {
                //  wifiNameList.add(scanResultHashMap.get(key));
                ScanResult scanResult = scanResultHashMap.get(key);
                boolean isConnected = false;
                if ((wifiInfo.getSSID().replace("\"", "")).equals(key)) {
                    isConnected = true;
                    if (!TextUtils.isEmpty(backWifi)) {
                        if (key.equals(backWifi) && (!isHide)) {
                            // ToastUtils.showShort("连接" + backWifi + "成功");
                            wifiList.getChildAt(0).requestFocus();
                        } else {
                            // ToastUtils.showShort("连接" + backWifi + "失败");
                        }
                        backWifi = "";
                    }
                } else {
                    isConnected = false;
                }
                if (isConnected) {
                    DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
                    String ipv4 = WifiUtil.intToIp(dhcpInfo.ipAddress);
                    // String netmask = WifiUtil.intToIp(dhcpInfo.netmask);
                    String netmask = WifiUtil.getWifiMask();
                    String gateway = WifiUtil.intToIp(dhcpInfo.gateway);
                    String dns1 = WifiUtil.intToIp(dhcpInfo.dns1);
                    String wifiSetting = WifiUtil.getWifiSetting(getActivity());
                    if (wifiSetting.equals("DHCP")) {
                        WifiDhcp wifiDhcp = new WifiDhcp(ipv4, netmask, gateway, dns1);
                        String json = new Gson().toJson(wifiDhcp);
                        SPUtils.getInstance().put(SharePrefer.WifiDhcp, json);
                    }


                    WifiResult wifiResult = new WifiResult(scanResult.BSSID, scanResult.SSID, scanResult.capabilities, scanResult.level, WifiResult.CARE_TYPE_SELECTED, ipv4, netmask, gateway, dns1);
                    Log.d(TAG, "scanResult " + wifiResult);
                    wifiResultList.add(0, wifiResult);


                } else {
                    WifiConfiguration existWifiConfiguration = null;
                    for (WifiConfiguration wifiConfiguration : wifiConfigurationList) {
                        if (wifiConfiguration.SSID.replace("\"", "").equals(scanResult.SSID)) {
                            existWifiConfiguration = wifiConfiguration;
                            break;
                        }
                    }
                    if (existWifiConfiguration != null) {
                        WifiResult wifiResult = new WifiResult(scanResult.BSSID, scanResult.SSID, scanResult.capabilities, scanResult.level, existWifiConfiguration.status);
                        Log.d(TAG, "scanResult " + wifiResult);
                       // Log.i("ssshhh","name="+scanResult.SSID+"---careType="+existWifiConfiguration.status);
                        String capabilities = scanResult.capabilities.trim();
                        if (capabilities != null && (capabilities.equals(WIFI_AUTH_OPEN) || capabilities.equals(WIFI_AUTH_ROAM))) {
                            wifiResult.setHavePassword(false);
                        } else {
                            wifiResult.setHavePassword(true);
                        }
                        wifiResultList.add(wifiResult);
                    } else {
                        WifiResult wifiResult = new WifiResult(scanResult.BSSID, scanResult.SSID, scanResult.capabilities, scanResult.level);
                        Log.d(TAG, "scanResult " + wifiResult);
                        wifiResultList.add(wifiResult);

                    }
                }
            }

            if (wirelessWifiAdapter != null) {
                Log.d(TAG, "wifiResultList=" + wifiResultList.size());
                if (wifiManager.isWifiEnabled()) {

                    boolean isCurrentFocusedOnWifiList = false;
                    for (int i = 0; i < wifiList.getChildCount(); i++) {
                        View childAt = wifiList.getChildAt(i);
                        if (childAt.isFocused()) {
                            isCurrentFocusedOnWifiList = true;
                        }
                    }

                    wirelessWifiAdapter.notifyDataSetChanged();

                    if (isCurrentFocusedOnWifiList) {
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dealLoseFocused();
                            }
                        }, 20);
                    }
                }

            }

        }
        startScan = false;
    }

    //处理当数据刷新数据减少导致焦点丢失问题，比如以前有10条数据，选中第九条，刷新数据后只有五条数据，原先选中第九条的焦点自然不存在了,之后默认选中0
    private void dealLoseFocused() {
        isCurrentFocused = false;
        for (int i = 0; i < wifiList.getChildCount(); i++) {
            View childAt = wifiList.getChildAt(i);
            if (childAt.isFocused()) {
                isCurrentFocused = true;

            }
        }
        if ((!isCurrentFocused) && (wifiList.getChildCount() > 0) && (!isHide)) {
            wifiList.getChildAt(0).requestFocus();
        }
    }


    public void initListener() {
        /***********************************有线网络************************************/
        //默认不可编辑
      /*  ipv4Edit.setEnabled(false);
        subnetMaskEdit.setEnabled(false);
        defaultGatewayEdit.setEnabled(false);
        dnsServerEdit.setEnabled(false);*/
        wiredNetwork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((wiredStatus.getText().toString().trim()).equals("未连接")) {
                    // ToastUtils.showShort("请插入网线");
                    com.zee.setting.utils.ToastUtils.showToast(getActivity(), "请插入网线");
                    return;
                }
                if (!isOpenWired) {
                    isOpenWired = true;
                    wiredDetailLayout.setVisibility(View.VISIBLE);
                    arrow.setImageResource(R.drawable.bg_arrow_down_focus_selector);
                    getEthernetInfo();
                } else {
                    isOpenManualSet = false;
                    //  wiredSwitch.setChecked(false);

                    isOpenWired = false;
                    wiredDetailLayout.setVisibility(View.GONE);
                    arrow.setSelected(false);
                    arrow.setImageResource(R.drawable.bg_arrow_focus_selector);
                }

            }
        });
        manualSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isOpenManualSet) {
                    wiredSwitch.setChecked(true);
                } else {
                    wiredSwitch.setChecked(false);

                }
            }
        });

        wiredSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setEthernetSwitch(isChecked);
                if (isChecked == false) {
                    int useDhcpOrStaticIp = EthernetUtils.getEthUseDhcpOrStaticIp(getActivity());
                    if (useDhcpOrStaticIp == 1) {
                        // ToastUtils.showShort("DHCP");
                        EthernetUtils.setDhcpIpConfiguration(getActivity());
                    }
                }

            }
        });
        ipv4Set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOpenManualSet) {
                    ipv4Edit.requestFocus();
                    ipv4Title.setTextColor(getActivity().getResources().getColor(R.color.src_c63d));
                    ipv4Edit.setTextColor(getActivity().getResources().getColor(R.color.src_c63d));
                }
            }
        });
        ipv4Edit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    ipv4Title.setTextColor(getActivity().getResources().getColor(R.color.src_c33));
                    ipv4Edit.setTextColor(getActivity().getResources().getColor(R.color.src_c33));
                } else {
                    ipv4Title.setTextColor(getActivity().getResources().getColor(R.color.src_c63d));
                    ipv4Edit.setTextColor(getActivity().getResources().getColor(R.color.src_c63d));
                }
            }
        });
        ipv4Set.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if (hasFocus) {
                    ipv4Title.setTextColor(getActivity().getResources().getColor(R.color.white));
                    ipv4Edit.setTextColor(getActivity().getResources().getColor(R.color.white));
                } else {
                    ipv4Title.setTextColor(getActivity().getResources().getColor(R.color.src_c33));
                    ipv4Edit.setTextColor(getActivity().getResources().getColor(R.color.src_c33));
                }
            }
        });


        subnetMask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOpenManualSet) {
                    subnetMaskEdit.requestFocus();
                    subnetMaskTitle.setTextColor(getActivity().getResources().getColor(R.color.src_c63d));
                    subnetMaskEdit.setTextColor(getActivity().getResources().getColor(R.color.src_c63d));
                }
            }
        });
        subnetMaskEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    subnetMaskTitle.setTextColor(getActivity().getResources().getColor(R.color.src_c33));
                    subnetMaskEdit.setTextColor(getActivity().getResources().getColor(R.color.src_c33));

                } else {
                    subnetMaskTitle.setTextColor(getActivity().getResources().getColor(R.color.src_c63d));
                    subnetMaskEdit.setTextColor(getActivity().getResources().getColor(R.color.src_c63d));
                }
            }
        });
        subnetMask.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    subnetMaskTitle.setTextColor(getActivity().getResources().getColor(R.color.white));
                    subnetMaskEdit.setTextColor(getActivity().getResources().getColor(R.color.white));
                } else {
                    subnetMaskTitle.setTextColor(getActivity().getResources().getColor(R.color.src_c33));
                    subnetMaskEdit.setTextColor(getActivity().getResources().getColor(R.color.src_c33));
                }
            }
        });

        defaultGateway.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOpenManualSet) {
                    defaultGatewayEdit.requestFocus();
                    defaultGatewayTitle.setTextColor(getActivity().getResources().getColor(R.color.src_c63d));
                    defaultGatewayEdit.setTextColor(getActivity().getResources().getColor(R.color.src_c63d));
                }
            }
        });
        defaultGatewayEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    defaultGatewayTitle.setTextColor(getActivity().getResources().getColor(R.color.src_c33));
                    defaultGatewayEdit.setTextColor(getActivity().getResources().getColor(R.color.src_c33));

                } else {
                    defaultGatewayTitle.setTextColor(getActivity().getResources().getColor(R.color.src_c63d));
                    defaultGatewayEdit.setTextColor(getActivity().getResources().getColor(R.color.src_c63d));
                }
            }
        });
        defaultGateway.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    defaultGatewayTitle.setTextColor(getActivity().getResources().getColor(R.color.white));
                    defaultGatewayEdit.setTextColor(getActivity().getResources().getColor(R.color.white));
                } else {
                    defaultGatewayTitle.setTextColor(getActivity().getResources().getColor(R.color.src_c33));
                    defaultGatewayEdit.setTextColor(getActivity().getResources().getColor(R.color.src_c33));
                }
            }
        });

        dnsServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isOpenManualSet) {
                    dnsServerEdit.requestFocus();
                    dnsServerTitle.setTextColor(getActivity().getResources().getColor(R.color.src_c63d));
                    dnsServerEdit.setTextColor(getActivity().getResources().getColor(R.color.src_c63d));
                }
            }
        });
        dnsServerEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    dnsServerTitle.setTextColor(getActivity().getResources().getColor(R.color.src_c33));
                    dnsServerEdit.setTextColor(getActivity().getResources().getColor(R.color.src_c33));

                } else {
                    dnsServerTitle.setTextColor(getActivity().getResources().getColor(R.color.src_c63d));
                    dnsServerEdit.setTextColor(getActivity().getResources().getColor(R.color.src_c63d));
                }
            }
        });
        dnsServer.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    dnsServerTitle.setTextColor(getActivity().getResources().getColor(R.color.white));
                    dnsServerEdit.setTextColor(getActivity().getResources().getColor(R.color.white));
                } else {
                    dnsServerTitle.setTextColor(getActivity().getResources().getColor(R.color.src_c33));
                    dnsServerEdit.setTextColor(getActivity().getResources().getColor(R.color.src_c33));
                }
            }
        });


        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ipv4 = ipv4Edit.getText().toString().trim();
                String netMask = subnetMaskEdit.getText().toString().trim();
                String gateWay = defaultGatewayEdit.getText().toString().trim();
                String dns = dnsServerEdit.getText().toString().trim();
                if (ipv4.equals(originWiredIpv4) && netMask.equals(originWiredNetMask)
                        && gateWay.equals(originWiredGateway) && dns.equals(originWiredDns1)) {
                    ToastUtils.showToast(getActivity(), "网络配置相同");
                    return;

                }
                boolean success = IpGetUtil.setEthernetIP(getActivity(), "STATIC",
                        ipv4, netMask,
                        gateWay, dns, "");
                if (success) {
                    //  ToastUtils.showShort("设置成功");
                    com.zee.setting.utils.ToastUtils.showToast(getActivity(), "设置成功");
                } else {
                    // ToastUtils.showShort("设置失败");
                    com.zee.setting.utils.ToastUtils.showToast(getActivity(), "设置失败");
                }

                //收起来
                isOpenManualSet = false;
                //  wiredSwitch.setChecked(false);
                isOpenWired = false;
                wiredDetailLayout.setVisibility(View.GONE);
                arrow.setSelected(false);
                arrow.setImageResource(R.drawable.bg_arrow_focus_selector);

            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeWiredDetail();
            }
        });
        /***********************************无线网络************************************/
        wirelessNetwork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isOpenWirelessSwitch) {
                    wirelessSwitch.setChecked(true);
                } else {
                    wirelessSwitch.setChecked(false);
                }


            }
        });
        wirelessNetwork.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (wirelessSwitch.isChecked()) {
                        wirelessSwitch.setTrackDrawable(getResources().getDrawable(R.mipmap.img_selected_open));
                    } else {
                        wirelessSwitch.setTrackDrawable(getResources().getDrawable(R.mipmap.img_selected_close));
                    }
                } else {
                    if (wirelessSwitch.isChecked()) {
                        wirelessSwitch.setTrackDrawable(getResources().getDrawable(R.mipmap.img_open));
                    } else {
                        wirelessSwitch.setTrackDrawable(getResources().getDrawable(R.mipmap.img_close));
                    }
                }
            }
        });

        wirelessSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (!isInitWifi) {
                        initWiFi();
                    }
                    isOpenWirelessSwitch = true;
                    wifiList.setVisibility(View.VISIBLE);
                    progressWifi.setVisibility(View.VISIBLE);
                    measureNetwork.setVisibility(View.GONE);
                    wifiManager.setWifiEnabled(true);
                    if (wirelessNetwork.isFocused() || wirelessSwitch.isFocused()) {
                        wirelessSwitch.setTrackDrawable(getResources().getDrawable(R.mipmap.img_selected_open));
                    } else {
                        wirelessSwitch.setTrackDrawable(getResources().getDrawable(R.mipmap.img_open));
                    }
                } else {
                    isOpenWirelessSwitch = false;
                    wifiList.setVisibility(View.GONE);
                    progressWifi.setVisibility(View.GONE);
                    measureNetwork.setVisibility(View.VISIBLE);
                    wifiManager.setWifiEnabled(false);
                    if (wirelessNetwork.isFocused() || wirelessSwitch.isFocused()) {
                        wirelessSwitch.setTrackDrawable(getResources().getDrawable(R.mipmap.img_selected_close));
                    } else {
                        wirelessSwitch.setTrackDrawable(getResources().getDrawable(R.mipmap.img_close));
                    }
                    if (wirelessWifiAdapter != null) {
                        wifiResultList.clear();
                        scanResultHashMap.clear();
                        wirelessWifiAdapter.notifyDataSetChanged();

                    }

                }
            }
        });
        wirelessWifiAdapter.setOnItemClickListener(new WirelessWifiAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, WifiResult wifiResult, int position, String... footType) {
                if (isSavedWifListExpand) {
                    isSavedWifListExpand = false;
                }
                updateSavedNetworkLayout();

                if (wifiResult != null) {

                    //  Toast.makeText(getActivity(), wifiResult.name, Toast.LENGTH_SHORT).show();
                   // Log.i("ssshhh","capabilities="+wifiResult.capabilities);
                    String capabilities=wifiResult.capabilities;
                    if (capabilities != null && (capabilities.equals(WIFI_AUTH_OPEN) || capabilities.equals(WIFI_AUTH_ROAM)) && (wifiResult.careType!= WifiResult.CARE_TYPE_SELECTED)) {
                        boolean addWifi = WifiUtil.addWifi(wifiManager, wifiResult.name, "", 1);
                        return;
                    }

                    Intent intent = new Intent(getActivity(), WifiActivity.class);
                    String json = new Gson().toJson(wifiResult);
                    intent.putExtra("connect", json);
                    if (wifiResult.careType == WifiResult.CARE_TYPE_SELECTED) {
                        intent.putExtra("status", "connected");
                        if (wifiResult.ipv4.equals("0.0.0.0")) {
                            com.zee.setting.utils.ToastUtils.showToast(getActivity(), "正在配置ip地址");
                            return;
                        }
                    } else if (wifiResult.careType == WifiResult.CARE_TYPE_SAVED_ENABLED) {
                       // autoConnectSavedWifi(wifiResult);
                        //取消保存
                       // cancelSavedWifi(wifiResult);
                        showSavedWifiDialog(wifiResult);

                        return;
                    } else {
                        intent.putExtra("status", "un_connected");
                    }

                    //  clearFocusView(position);
                    startActivityForResult(intent, Constant.BACK_REFRESH);

                } else {
                    String type = footType[0];
                    if (!TextUtils.isEmpty(type)) {
                        if (type.equals("add")) {
                            Intent intent = new Intent(getActivity(), WifiActivity.class);
                            intent.putExtra("status", "add");
                            startActivityForResult(intent, Constant.BACK_REFRESH);
                        } else {
                             ToastUtils.showToast(getActivity(), "敬请期待");
                          /*  Intent intent = new Intent(getActivity(), NetSpeedActivity.class);
                            startActivity(intent);*/
                        }
                    }
                    // clearFocusView(position);

                }


            }
        });

        //add
        savedNetworkLayout.setOnClickListener(v -> {
            isSavedWifListExpand = !isSavedWifListExpand;
            updateSavedNetworkLayout();

            if (isSavedWifListExpand) {
                savedWifiListRecyclerView.postDelayed(() -> {
                    networkNestedScrollView.smoothScrollBy(0, 500);
                    }, 30);
            }
        });
        /***********************************网络测速************************************/
        measureNetwork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), NetSpeedActivity.class);
                startActivity(intent);
                //  ToastUtils.showShort("敬请期待");
                //  com.zee.setting.utils.ToastUtils.showToast(getActivity(), "敬请期待");
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void updateSavedNetworkLayout() {
        List<WifiConfiguration> wifiConfigurationList = wifiManager.getConfiguredNetworks();

        HashMap<String, WifiConfiguration> wifiConfigurationHashMap = new HashMap<>();

        for (WifiConfiguration wifiConfiguration: wifiConfigurationList) {
            if (wifiConfiguration.SSID.isEmpty()) continue;
            if (!wifiConfigurationHashMap.containsKey(wifiConfiguration.SSID)) {
                wifiConfigurationHashMap.put(wifiConfiguration.SSID, wifiConfiguration);
            }
        }
        List<WifiConfiguration> dataList = new ArrayList<>(wifiConfigurationHashMap.values());

        //tvSavedNetworkDesc.setText(String.valueOf(dataList.size()));
        if (isSavedWifListExpand) {
            ivSavedNetworkArrow.setImageResource(R.drawable.bg_arrow_down_focus_selector);
            savedWifiListRecyclerView.setVisibility(View.VISIBLE);

            if (savedWifiAdapter == null) {
                savedWifiAdapter = new SavedWifiAdapter(dataList);
                savedWifiAdapter.setOnItemClickListener(new SavedWifiAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, WifiConfiguration wifiConfiguration) {
                        showSavedWifiDialog(wifiConfiguration);
                    }
                });
                savedWifiListRecyclerView.setAdapter(savedWifiAdapter);
            } else {
                savedWifiAdapter.updateDataList(dataList);
            }

        } else {
            ivSavedNetworkArrow.setImageResource(R.drawable.bg_arrow_focus_selector);
            savedWifiListRecyclerView.setVisibility(View.GONE);
        }
    }

    private boolean isSavedWifListExpand = false;
    private SavedWifiAdapter savedWifiAdapter;

    private void setEthernetSwitch(boolean isChecked) {
        if (isChecked) {
            isOpenManualSet = true;
            ipv4Edit.setEnabled(true);
            subnetMaskEdit.setEnabled(true);
            defaultGatewayEdit.setEnabled(true);
            dnsServerEdit.setEnabled(true);
        } else {
            isOpenManualSet = false;
            ipv4Edit.setEnabled(false);
            subnetMaskEdit.setEnabled(false);
            defaultGatewayEdit.setEnabled(false);
            dnsServerEdit.setEnabled(false);
        }
    }

    private ExecutorService cachedThreadPool;

    private void autoConnectSavedWifi(WifiResult wifiResult) {
        if (cachedThreadPool == null) {
            cachedThreadPool = Executors.newCachedThreadPool();
        }
        cachedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    List<WifiConfiguration> wifiConfigurationList = wifiManager.getConfiguredNetworks();
                    WifiConfiguration savedWifiConfiguration = null;
                    for (WifiConfiguration configuration : wifiConfigurationList) {
                        if (configuration.SSID.replace("\"", "").equals(wifiResult.name)) {
                            savedWifiConfiguration = configuration;
                            break;
                        }
                    }
                    if (savedWifiConfiguration != null) {
                        //int networkId = wifiManager.addNetwork(savedWifiConfiguration);
                        Log.d(TAG, "savedWifiConfiguration.networkId " + savedWifiConfiguration.networkId);
                        wifiManager.enableNetwork(savedWifiConfiguration.networkId, true);
                        //为了让已连接wifi选中
                        backWifi = wifiResult.name;
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                checkToScanning();
                            }
                        });

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void clearFocusView(int position) {
        View curView = ((Activity) context).getCurrentFocus();
        if (curView != null) {
            curView.clearFocus();
        }
        clickPosition = position;
    }

    private void closeWiredDetail() {
        //收起来
        isOpenManualSet = false;
        wiredSwitch.setChecked(false);
        isOpenWired = false;
        wiredDetailLayout.setVisibility(View.GONE);
        arrow.setSelected(false);
        arrow.setImageResource(R.drawable.bg_arrow_focus_selector);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (wirelessWifiAdapter != null) {
            wirelessWifiAdapter.setOnItemClickListener(null);
        }
        if (savedWifiAdapter != null) {
            savedWifiAdapter.setOnItemClickListener(null);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (broadcastReceiver != null) {
            broadcastReceiver.unRegister();
        }
        if (networkChangeReceiver != null) {
            networkChangeReceiver.unRegister();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden && isAdded()) {
            //显示
            isHide = false;
        } else {
            //隐藏
            isHide = true;
        }
    }

    private void cancelSavedWifi(WifiResult wifiResult) {
        if (cachedThreadPool == null) {
            cachedThreadPool = Executors.newCachedThreadPool();
        }
        cachedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    List<WifiConfiguration> wifiConfigurationList = wifiManager.getConfiguredNetworks();
                    WifiConfiguration savedWifiConfiguration = null;
                    for (WifiConfiguration configuration : wifiConfigurationList) {
                        if (configuration.SSID.replace("\"", "").equals(wifiResult.name)) {
                            savedWifiConfiguration = configuration;
                            break;
                        }
                    }
                    if (savedWifiConfiguration != null) {
                        //int networkId = wifiManager.addNetwork(savedWifiConfiguration);
                        Log.d(TAG, "savedWifiConfiguration.networkId " + savedWifiConfiguration.networkId);
                        wifiManager.removeNetwork(savedWifiConfiguration.networkId);
                        wifiManager.saveConfiguration(); // 保存更改

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                checkToScanning();
                            }
                        });

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public void showSavedWifiDialog(WifiResult wifiResult) {
        BaseDialog normalDialog = new BaseDialog(getActivity(), R.style.dialogStyle);
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_alert, null);
        normalDialog.setContentView(view);
        normalDialog.show();

        LinearLayout headRoot = view.findViewById(R.id.head_root);
        headRoot.setVisibility(View.GONE);
        TextView description = view.findViewById(R.id.description);
        description.setText(wifiResult.name+"是已保存的网络");
        description.setTextSize(16.f);
        description.setMaxLines(1);
        description.setEllipsize(TextUtils.TruncateAt.END);


        TextView confirm = view.findViewById(R.id.confirm);
        TextView cancel = view.findViewById(R.id.cancel);
        confirm.setText("连接");
        cancel.setText("忽略");
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                autoConnectSavedWifi(wifiResult);
                normalDialog.dismiss();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelSavedWifi(wifiResult);
                normalDialog.dismiss();
            }
        });


    }

    public void showSavedWifiDialog(WifiConfiguration wifiConfiguration) {
        BaseDialog normalDialog = new BaseDialog(getActivity(), R.style.dialogStyle);
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_alert, null);
        normalDialog.setContentView(view);
        normalDialog.show();

        LinearLayout headRoot = view.findViewById(R.id.head_root);
        headRoot.setVisibility(View.GONE);
        TextView description = view.findViewById(R.id.description);
        String useSSID = wifiConfiguration.SSID.replace("\"", "");
        description.setText(useSSID + "是已保存的网络");
        description.setTextSize(16.f);
        description.setMaxLines(1);
        description.setEllipsize(TextUtils.TruncateAt.END);


        TextView confirm = view.findViewById(R.id.confirm);
        TextView cancel = view.findViewById(R.id.cancel);
        confirm.setText("忽略");
        cancel.setText("取消");
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wifiManager.removeNetwork(wifiConfiguration.networkId);
                wifiManager.saveConfiguration(); // 保存更改
                normalDialog.dismiss();
                savedNetworkLayout.requestFocus();
                checkToScanning();

                savedNetworkLayout.postDelayed(() -> {
                    updateSavedNetworkLayout();
                    savedNetworkLayout.requestFocus();
                }, 30);

                savedNetworkLayout.postDelayed(() -> {
                    if(savedWifiAdapter != null && savedWifiAdapter.getItemCount() <= 1 && !isHide) {
                        getScanResult();
                    }
                }, 3000);
            }
        });
        cancel.setOnClickListener(v -> normalDialog.dismiss());
    }
}
