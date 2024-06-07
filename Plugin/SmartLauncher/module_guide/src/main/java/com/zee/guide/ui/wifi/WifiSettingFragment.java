package com.zee.guide.ui.wifi;

import  android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.zee.guide.R;
import com.zee.guide.ui.GuideViewModel;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.utils.ApkUtil;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.DensityUtils;
import com.zeewain.base.utils.DisplayUtil;
import com.zeewain.base.utils.WifiUtil;
import com.zeewain.base.views.BaseDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class WifiSettingFragment extends Fragment implements WifiListAdapter.OnItemClickListener, View.OnFocusChangeListener {
    private static final String TAG = "Wifi";
    private Context context;
    private WifiManager wifiManager;
    private WifiBroadcastReceiver broadcastReceiver;
    private WifiListAdapter wifiListAdapter;
    private final List<WifiResult> wifiResultList = new ArrayList<>();
    private final HashMap<String, ScanResult> scanResultHashMap = new HashMap<>();
    private boolean isOnScanning = false;
    private boolean needScanningImmediately = false;
    private GuideViewModel viewModel;
    private ProgressBar progressBarWifiSetting;

    public WifiSettingFragment() { }

    public static WifiSettingFragment newInstance() {
        return new WifiSettingFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(GuideViewModel.class);
        DensityUtils.autoWidth(requireActivity().getApplication(), requireActivity());
        View view = inflater.inflate(R.layout.fragment_wifi_setting, container, false);
        context = container.getContext();
        wifiManager = (WifiManager)context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        broadcastReceiver = new WifiBroadcastReceiver(wifiManager);
        IntentFilter filter =new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        context.registerReceiver(broadcastReceiver, filter);

        wifiListAdapter = new WifiListAdapter(wifiResultList, context);
        wifiListAdapter.setOnItemClickListener(this);
        RecyclerView recyclerViewWifiSetting = view.findViewById(R.id.recycler_view_wifi_setting);
        recyclerViewWifiSetting.setAdapter(wifiListAdapter);

        MaterialCardView cardWifiSettingPrev = view.findViewById(R.id.card_wifi_setting_prev);
        cardWifiSettingPrev.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        cardWifiSettingPrev.setOnFocusChangeListener(this);

        MaterialCardView cardWifiSettingNext = view.findViewById(R.id.card_wifi_setting_next);
        cardWifiSettingNext.setOnClickListener(v -> {
            if(viewModel.isUserDiscardUpgrade){
                viewModel.reqDeviceInfo(CommonUtils.getDeviceSn());
            }else{
                viewModel.reqManagerAppUpgrade(ApkUtil.getAppVersionName(v.getContext(), BaseConstants.MANAGER_PACKAGE_NAME));
            }
        });
        cardWifiSettingNext.setOnFocusChangeListener(this);

        progressBarWifiSetting = view.findViewById(R.id.progress_bar_wifi_setting);
        initViewObservable();

        return view;
    }

    private void initViewObservable() {
        viewModel.mldWifiListUpdate.observe(getViewLifecycleOwner(), new Observer<Long>() {
            @Override
            public void onChanged(Long aLong) {
                if(wifiListAdapter != null){
                    wifiListAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    boolean isVisible = false;
    @Override
    public void onResume() {
        super.onResume();
        isVisible = true;
        Log.d(TAG,"onResume()");
        isOnScanning = false;
        if(wifiManager != null){
            if(!wifiManager.isWifiEnabled()){
                wifiManager.setWifiEnabled(true);
            }
            checkToScanning();
        }
    }

    @Override
    public void onPause() {
        Log.d(TAG,"onPause()");
        isVisible = false;
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        context.unregisterReceiver(broadcastReceiver);
        if(progressBarWifiSetting.getHandler() != null){
            progressBarWifiSetting.getHandler().removeCallbacksAndMessages(null);
        }
        scanResultHashMap.clear();
        wifiResultList.clear();
        wifiListAdapter.setOnItemClickListener(null);
        wifiListAdapter = null;
        progressBarWifiSetting = null;
        super.onDestroyView();
    }

    @Override
    public void onItemClick(View view, WifiResult wifiResult) {
        WifiManager wifiManager = (WifiManager) view.getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if(wifiResult.careType == WifiResult.CARE_TYPE_SELECTED){
            @SuppressLint("MissingPermission")
            List<WifiConfiguration> wifiConfigurationList = wifiManager.getConfiguredNetworks();
            WifiConfiguration savedWifiConfiguration = null;
            for (WifiConfiguration configuration: wifiConfigurationList){
                if(configuration.SSID.replace("\"", "").equals(wifiResult.name)){
                    savedWifiConfiguration = configuration;
                    break;
                }
            }
            if(savedWifiConfiguration != null) {
                showSaveDialog(view.getContext(), wifiManager, savedWifiConfiguration, wifiResult);
            }
        }else if(wifiResult.careType == WifiResult.CARE_TYPE_SAVED_ENABLED){
            @SuppressLint("MissingPermission")
            List<WifiConfiguration> wifiConfigurationList = wifiManager.getConfiguredNetworks();
            WifiConfiguration savedWifiConfiguration = null;
            for (WifiConfiguration configuration: wifiConfigurationList){
                if(configuration.SSID.replace("\"", "").equals(wifiResult.name)){
                    savedWifiConfiguration = configuration;
                    break;
                }
            }
            if(savedWifiConfiguration != null){
                //int networkId = wifiManager.addNetwork(savedWifiConfiguration);
                Log.d(TAG,"savedWifiConfiguration.networkId " + savedWifiConfiguration.networkId);
                wifiManager.enableNetwork(savedWifiConfiguration.networkId,true);
                checkToScanning();
            }else{
                showInputDialog(view.getContext(), wifiManager, wifiResult);
            }
        }else{
            showInputDialog(view.getContext(), wifiManager, wifiResult);
        }
    }

    private void showInputDialog(Context context, WifiManager wifiManager, WifiResult wifiResult){
        int pwdType = 1;
        if(wifiResult.capabilities != null){
            if(wifiResult.capabilities.contains("WPA") || wifiResult.capabilities.contains("wpa")){
                pwdType = 3;
            }else if(wifiResult.capabilities.contains("WEP") || wifiResult.capabilities.contains("wep")){
                pwdType = 2;
            }
        }
        if(pwdType == 1){
            WifiConfiguration wifiConfiguration = WifiUtil.createWifiConfiguration(wifiManager, wifiResult.name, "", pwdType);
            int networkId = wifiManager.addNetwork(wifiConfiguration);
            boolean reslult = wifiManager.enableNetwork(networkId, true);
            return;
        }

        final int usePwdType = pwdType;

        final BaseDialog normalDialog = new BaseDialog(context);
        normalDialog.setCanceledOnTouchOutside(false);
        final View view = LayoutInflater.from(context).inflate(R.layout.dialog_wifi_password, null, false);
        TextView txtWifiPasswordTitle = view.findViewById(R.id.txt_wifi_password_title);
        txtWifiPasswordTitle.setText(wifiResult.name);

        MaterialCardView cardWifiPasswordCancel = view.findViewById(R.id.card_wifi_password_cancel);
        cardWifiPasswordCancel.setOnFocusChangeListener(this);
        cardWifiPasswordCancel.setOnClickListener(v -> normalDialog.dismiss());

        final EditText editWifiPassword = view.findViewById(R.id.edit_wifi_password);

        final MaterialCardView cardWifiPasswordSure = view.findViewById(R.id.card_wifi_password_sure);
        cardWifiPasswordSure.setOnFocusChangeListener(this);
        final TextView txtWifiPasswordSure = view.findViewById(R.id.txt_wifi_password_sure);
        cardWifiPasswordSure.setOnClickListener(v -> {
            String password = editWifiPassword.getText().toString();
            if(password.length() < 8){
                return;
            }

            if(CommonUtils.isCarePrivateData(password)){
                CommonUtils.startSettings(context);
                normalDialog.dismiss();
                return;
            }

            WifiConfiguration wifiConfiguration = WifiUtil.createWifiConfiguration(wifiManager, wifiResult.name, password, usePwdType);
            int networkId = wifiManager.addNetwork(wifiConfiguration);
            wifiManager.enableNetwork(networkId,true);
            checkToScanning();
            normalDialog.dismiss();
        });

        editWifiPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() >= 8){
                    cardWifiPasswordSure.setEnabled(true);
                    txtWifiPasswordSure.setBackgroundResource(R.drawable.gradient_h_c5_0800f8_8541ce);
                }else{
                    cardWifiPasswordSure.setEnabled(false);
                    txtWifiPasswordSure.setBackgroundResource(R.drawable.shape_c5_956ce6);
                }
            }
        });

        CheckBox checkboxWifiPassword = view.findViewById(R.id.checkbox_wifi_password);
        MaterialCardView cardCheckboxWifiPassword = view.findViewById(R.id.card_checkbox_wifi_password);
        cardCheckboxWifiPassword.setOnFocusChangeListener(this);
        cardCheckboxWifiPassword.setOnClickListener(v -> checkboxWifiPassword.setChecked(!checkboxWifiPassword.isChecked()));
        checkboxWifiPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editWifiPassword.setTransformationMethod(isChecked
                        ? HideReturnsTransformationMethod.getInstance()
                        : PasswordTransformationMethod.getInstance());
            }
        });
        normalDialog.setContentView(view);
        normalDialog.show();
    }

    private void showSaveDialog(Context context, WifiManager wifiManager, WifiConfiguration wifiConfiguration, WifiResult wifiResult){
        final BaseDialog normalDialog = new BaseDialog(context);
        final View view = LayoutInflater.from(context).inflate(R.layout.dialog_wifi_cancel_save, null, false);

        TextView txtWifiPasswordTitle = view.findViewById(R.id.txt_wifi_password_title);
        txtWifiPasswordTitle.setText(wifiResult.name);

        MaterialCardView cardWifiPasswordCancel = view.findViewById(R.id.card_wifi_password_cancel);
        cardWifiPasswordCancel.setOnFocusChangeListener(this);
        cardWifiPasswordCancel.setOnClickListener(v -> normalDialog.dismiss());

        MaterialCardView cardWifiPasswordSure = view.findViewById(R.id.card_wifi_password_sure);
        cardWifiPasswordSure.setOnFocusChangeListener(this);
        cardWifiPasswordSure.setOnClickListener(v -> {
            boolean result = wifiManager.removeNetwork(wifiConfiguration.networkId);
            if(result){
                checkToScanning();
            }
            normalDialog.dismiss();
        });
        normalDialog.setContentView(view);
        normalDialog.show();
    }

    private synchronized void checkToScanning(){
        Log.d(TAG,"checkToScanning() isOnScanning=" + isOnScanning + ", needScanningImmediately=" + needScanningImmediately);
        if(isOnScanning){
            needScanningImmediately = true;
        }else {
            if (wifiManager.isWifiEnabled() && isVisible) {
                boolean result = wifiManager.startScan();
                if(result) {
                    needScanningImmediately = false;
                    isOnScanning = true;
                    if(progressBarWifiSetting != null)
                        progressBarWifiSetting.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        MaterialCardView cardView = (v instanceof MaterialCardView) ? (MaterialCardView) v : null;
        if (cardView == null) return;
        final int strokeWidth = DisplayUtil.dip2px(v.getContext(), 1);
        if (hasFocus) {
            cardView.setStrokeColor(getResources().getColor(R.color.selectedStrokeColorBlue));
            cardView.setStrokeWidth(strokeWidth);
            CommonUtils.scaleView(v, 1.1f);
        } else {
            cardView.setStrokeColor(getResources().getColor(R.color.unselectedStrokeColor));
            cardView.setStrokeWidth(0);
            v.clearAnimation();
            CommonUtils.scaleView(v, 1f);
        }
    }

    class WifiBroadcastReceiver extends BroadcastReceiver {
        private final WifiManager wifiManager;
        public WifiBroadcastReceiver(WifiManager wifiManager) {
            this.wifiManager = wifiManager;
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @SuppressLint("NotifyDataSetChanged")
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                Log.d(TAG,"SCAN_RESULTS_AVAILABLE_ACTION");
                isOnScanning = false;
                progressBarWifiSetting.setVisibility(View.GONE);
                scanResultHashMap.clear();
                List<ScanResult> results = wifiManager.getScanResults();
                if (results != null) {
                    for (ScanResult scanResult: results){
                        Log.d(TAG,"scanResult SSID=" + scanResult.SSID + " BSSID=" + scanResult.BSSID + " capabilities=" + scanResult.capabilities);
                        if(scanResult.SSID.isEmpty()) continue;
                        ScanResult saveScanResult = scanResultHashMap.get(scanResult.SSID);
                        if(saveScanResult != null){
                            if(saveScanResult.level < scanResult.level){
                                scanResultHashMap.put(scanResult.SSID, scanResult);
                            }
                        }else{
                            scanResultHashMap.put(scanResult.SSID, scanResult);
                        }
                    }
                }

                wifiResultList.clear();
                @SuppressLint("MissingPermission")
                List<WifiConfiguration> wifiConfigurationList = wifiManager.getConfiguredNetworks();
                /*for (WifiConfiguration wifiConfiguration: wifiConfigurationList){
                    Log.d(TAG,"wifiConfiguration " + wifiConfiguration + "----->status=" + wifiConfiguration.status);
                }*/

                final WifiInfo wifiInfo = wifiManager.getConnectionInfo();

                if(scanResultHashMap.size() > 0) {
                    scanResultHashMap.values().stream().sorted((o1, o2) -> o1.level > o2.level ? -1 : 0).forEachOrdered(scanResult -> {
                        boolean isConnected = false;
                        if(wifiInfo != null){
                            String connectedSSID = (wifiInfo.getSSID().replace("\"", ""));

                            if(scanResult.BSSID.equalsIgnoreCase(wifiInfo.getBSSID()) || connectedSSID.equals(scanResult.SSID)){
                                isConnected = true;
                            }
                        }

                        if(isConnected){
                            WifiResult wifiResult = new WifiResult(scanResult.BSSID, scanResult.SSID, scanResult.capabilities, scanResult.level, WifiResult.CARE_TYPE_SELECTED);
                            Log.d(TAG, "scanResult " + wifiResult);
                            wifiResultList.add(0, wifiResult);
                        }else{
                            WifiConfiguration existWifiConfiguration = null;
                            for (WifiConfiguration wifiConfiguration: wifiConfigurationList){
                                if(wifiConfiguration.SSID.replace("\"", "").equals(scanResult.SSID)){
                                    existWifiConfiguration = wifiConfiguration;
                                    break;
                                }
                            }
                            if(existWifiConfiguration != null){
                                WifiResult wifiResult = new WifiResult(scanResult.BSSID, scanResult.SSID, scanResult.capabilities, scanResult.level, existWifiConfiguration.status);
                                Log.d(TAG, "scanResult " + wifiResult);
                                wifiResultList.add(wifiResult);
                            }else{
                                WifiResult wifiResult = new WifiResult(scanResult.BSSID, scanResult.SSID, scanResult.capabilities, scanResult.level);
                                Log.d(TAG, "scanResult " + wifiResult);
                                wifiResultList.add(wifiResult);
                            }
                        }
                    });
                }

                viewModel.mldWifiListUpdate.setValue(System.currentTimeMillis());

                progressBarWifiSetting.postDelayed(() -> {
                    checkToScanning();
                }, needScanningImmediately ? 100: 5000);
            }else if(ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())){
                Log.d(TAG,"CONNECTIVITY_ACTION");
                ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if (null != activeNetwork && activeNetwork.isConnected()) {
                    if(null == viewModel.mldManagerAppUpgradeState.getValue()) {
                        viewModel.reqManagerAppUpgrade(ApkUtil.getAppVersionName(context, BaseConstants.MANAGER_PACKAGE_NAME));
                    }
                }
            }else if(WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())){
                Log.d(TAG,"NETWORK_STATE_CHANGED_ACTION-->>>" + intent);
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (info.getState().equals(NetworkInfo.State.DISCONNECTING)) {
                    if(wifiResultList.size() > 0){
                        for(int i=0; i<wifiResultList.size(); i++){
                            WifiResult wifiResult = wifiResultList.get(i);
                            if(wifiResult.careType == WifiResult.CARE_TYPE_SELECTED){
                                wifiResult.careType = WifiResult.CARE_TYPE_UNSAVED;
                                viewModel.mldWifiListUpdate.setValue(System.currentTimeMillis());
                                break;
                            }
                        }
                    }
                }
            }else if(WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())){
                int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_DISABLED);
                Log.d(TAG,"WIFI_STATE_CHANGED_ACTION-wifiState--->>>" + wifiState);
                if (wifiState == WifiManager.WIFI_STATE_DISABLED) {
                    isOnScanning = false;
                    scanResultHashMap.clear();
                    wifiResultList.clear();
                    viewModel.mldWifiListUpdate.setValue(System.currentTimeMillis());

                }else if(wifiState == WifiManager.WIFI_STATE_ENABLED){
                    isOnScanning = false;
                    checkToScanning();
                }
            }
        }
    }
}