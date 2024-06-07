package com.zee.setting.activity;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;

import com.google.gson.Gson;
import com.zee.setting.R;
import com.zee.setting.base.BaseActivity;
import com.zee.setting.bean.WifiDhcp;
import com.zee.setting.bean.WifiResult;
import com.zee.setting.cache.SPUtils;
import com.zee.setting.cache.SharePrefer;
import com.zee.setting.config.Config;
import com.zee.setting.utils.CommonUtils;
import com.zee.setting.utils.Constant;
import com.zee.setting.utils.DensityUtils;
import com.zee.setting.utils.DpUtil;
import com.zee.setting.utils.SystemProperties;
import com.zee.setting.utils.ToastUtils;
import com.zee.setting.utils.WifiUtil;
import com.zee.setting.views.BaseDialog;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class WifiActivity extends BaseActivity {

    private boolean isShowPwd;
    private WifiManager wifiManager;
    private WifiResult scanResult;
    private boolean isEditWifi;
    private boolean isRecordVideo;
    private boolean isAlgorithmDebug;

    private boolean isSafeMode = true;
    private static final String TAG = "wifiActivity";
    private BaseDialog normalDialog;
    private Handler handler = new Handler();
    private ExecutorService cachedThreadPool;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);
        DensityUtils.autoWidth(getApplication(), this);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        getData();


    }


    public void getData() {
        String connect = getIntent().getStringExtra("connect");
        scanResult = new Gson().fromJson(connect, WifiResult.class);
        Log.i(TAG, "scanResult_SSID=" + connect);
        String status = getIntent().getStringExtra("status");
        if (status.equals("connected")) {
            showWiFiDetailDialog();
        } else if (status.equals("un_connected")) {
            showWiFiConnectDialog();
        } else if (status.equals("add")) {
            showAddWiFiDialog();
        }
    }


    public void showWiFiConnectDialog() {
        normalDialog = new BaseDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_new_connect_wifi, null);
        normalDialog.setContentView(view);
        normalDialog.show();
        //  normalDialog.setCanceledOnTouchOutside(false);

      /*  Window dialogWindow = normalDialog.getWindow();
        WindowManager m = getWindowManager();
        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
        WindowManager.LayoutParams p = dialogWindow.getAttributes(); // 获取对话框当前的参数值
        p.height = (int) (d.getHeight() * 0.6); // 高度设置为屏幕的0.6
        p.width = (int) (d.getWidth() * 1); // 宽度设置为屏幕的0.65
        dialogWindow.setAttributes(p);*/

        TextView wifiName = view.findViewById(R.id.wifi_name);
        ImageView showPwd = view.findViewById(R.id.show_pwd);
        EditText inputPwd = view.findViewById(R.id.input_pwd);
        TextView connectWifi = view.findViewById(R.id.connect_wifi);
        TextView cancel = view.findViewById(R.id.cancel);
        ImageView imgBack = view.findViewById(R.id.img_back);
        inputPwd.requestFocus();
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                normalDialog.dismiss();
                finish();
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                normalDialog.dismiss();
                finish();
            }
        });

        wifiName.setText(scanResult.name);
        exitDialog(normalDialog);

        showPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isShowPwd) {
                    isShowPwd = true;
                    showPwd.setSelected(true);
                    //如果选中，显示密码
                    inputPwd.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    isShowPwd = false;
                    showPwd.setSelected(false);
                    //否则隐藏密码
                    inputPwd.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }

            }
        });

        connectWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pwdType = 1;
                if (scanResult.capabilities != null) {
                    if (scanResult.capabilities.contains("WPA") || scanResult.capabilities.contains("wpa")) {
                        pwdType = 3;
                    } else if (scanResult.capabilities.contains("WEP") || scanResult.capabilities.contains("wep")) {
                        pwdType = 2;
                    }
                }
                final int usePwdType = pwdType;
                Log.i(TAG, "usePwdType=" + usePwdType);
                String password = inputPwd.getText().toString().trim();
                if (TextUtils.isEmpty(password)) {
                    ToastUtils.showToast(WifiActivity.this, "密码不能为空");
                    return;
                } else if (password.length() < 8) {
                    ToastUtils.showToast(WifiActivity.this, "密码不能少于8位");
                    return;
                }

                if (CommonUtils.isCarePrivateData(password)) {
                    CommonUtils.startSettings(v.getContext());
                    normalDialog.dismiss();
                    finish();
                    return;
                }

                if (CommonUtils.isEnablePrivateFun(password)) {
                    normalDialog.dismiss();
                    showDevelopDialog();
                    return;
                }

                WifiConfiguration wifiConfiguration = WifiUtil.createWifiConfiguration(wifiManager, scanResult.name, password, usePwdType);
                int networkId = wifiManager.addNetwork(wifiConfiguration);

                Intent intent = new Intent();
                intent.putExtra("name", scanResult.name);
                setResult(Constant.BACK_REFRESH, intent);
                if (cachedThreadPool == null) {
                    cachedThreadPool = Executors.newCachedThreadPool();
                }
                cachedThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            boolean enableNetwork = wifiManager.enableNetwork(networkId, true);
                            Log.i(TAG, "enableNetwork=" + enableNetwork);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                normalDialog.dismiss();
                finish();


            }
        });


    }


    public void showWiFiDetailDialog() {
        normalDialog = new BaseDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_detail_wifi, null);
        normalDialog.setContentView(view);
        normalDialog.show();
        // normalDialog.setCanceledOnTouchOutside(false);
        TextView title = view.findViewById(R.id.title);
        ConstraintLayout ignoreWifi = view.findViewById(R.id.ignore_wifi);
        ConstraintLayout manualSet = view.findViewById(R.id.manual_set);
        Switch manualSetSwitch = view.findViewById(R.id.manual_set_switch);
        EditText ipv4Edit = view.findViewById(R.id.ipv4_edit);
        EditText subnetMaskEdit = view.findViewById(R.id.subnet_mask_edit);
        EditText defaultGatewayEdit = view.findViewById(R.id.default_gateway_edit);
        EditText dnsServerEdit = view.findViewById(R.id.dns_server_edit);
        LinearLayout wifiDetailLayout = view.findViewById(R.id.wifi_detail_layout);
        ConstraintLayout confirmCancelLayout = view.findViewById(R.id.confirm_cancel_layout);
        TextView confirm = view.findViewById(R.id.confirm);
        TextView cancel = view.findViewById(R.id.cancel);
        ConstraintLayout ipv4 = view.findViewById(R.id.ipv4);
        ConstraintLayout subnetMask = view.findViewById(R.id.subnet_mask);
        ConstraintLayout defaultGateway = view.findViewById(R.id.default_gateway);
        ConstraintLayout dnsServer = view.findViewById(R.id.dns_server);

        TextView ipv4Title = view.findViewById(R.id.ipv4_title);
        TextView netMaskTitle = view.findViewById(R.id.netMask_title);
        TextView gateWayTitle = view.findViewById(R.id.gateWay_title);
        TextView dnsTitle = view.findViewById(R.id.dns_title);
        ImageView imgBack = view.findViewById(R.id.img_back);
        exitDialog(normalDialog);
        String wifiSetting = WifiUtil.getWifiSetting(WifiActivity.this);
        // ToastUtils.showToast(WifiActivity.this,wifiSetting);
        if (wifiSetting.equals("StaticIP")) {
            // ToastUtils.showToast(WifiActivity.this,"当前是静态ip");
            manualSetSwitch.setChecked(true);
        } else if (wifiSetting.equals("DHCP")) {
            // ToastUtils.showToast(WifiActivity.this,"当前是动态ip");
            manualSetSwitch.setChecked(false);
        }
        switchEdit(manualSetSwitch.isChecked(), ipv4Edit, subnetMaskEdit, defaultGatewayEdit, dnsServerEdit, wifiDetailLayout, confirmCancelLayout, manualSet);
        //默认选中
        ignoreWifi.requestFocus();
        dealFocusEffect(manualSet, manualSetSwitch);

        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                normalDialog.dismiss();
                finish();
            }
        });

        ignoreWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                int networkId = wifiInfo.getNetworkId();
                wifiManager.removeNetwork(networkId);
                wifiManager.saveConfiguration();
                finish();
            }
        });
        manualSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isEditWifi) {
                    manualSetSwitch.setChecked(true);
                } else {
                    manualSetSwitch.setChecked(false);

                    // WifiUtil.setWifiDHCP(WifiActivity.this);
                }


            }
        });

        manualSetSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    String wifiSetting = WifiUtil.getWifiSetting(WifiActivity.this);
                    //打开开关但是没有设置那么仍然是dhcp.这时候在关闭也就不用设置dhcp模式，只有当前是静态模式关闭才设置
                    if (wifiSetting.equals("StaticIP")) {
                        WifiUtil.setWifiDHCP(WifiActivity.this.getApplicationContext());
                        String dhcp = SPUtils.getInstance().getString(SharePrefer.WifiDhcp);
                        WifiDhcp wifiDhcp = new Gson().fromJson(dhcp, WifiDhcp.class);
                        // Log.i("ssshhh","wifiDhcp="+wifiDhcp.toString());
                        ipv4Edit.setText(wifiDhcp.ipv4);
                        subnetMaskEdit.setText(wifiDhcp.netmask);
                        defaultGatewayEdit.setText(wifiDhcp.gateway);
                        dnsServerEdit.setText(wifiDhcp.dns);
                    }

                    if (manualSet.isFocused() || manualSetSwitch.isFocused()) {
                        manualSetSwitch.setTrackDrawable(getResources().getDrawable(R.mipmap.img_selected_close));
                    } else {
                        manualSetSwitch.setTrackDrawable(getResources().getDrawable(R.mipmap.img_close));
                    }
                } else {
                    if (manualSet.isFocused() || manualSetSwitch.isFocused()) {
                        manualSetSwitch.setTrackDrawable(getResources().getDrawable(R.mipmap.img_selected_open));
                    } else {
                        manualSetSwitch.setTrackDrawable(getResources().getDrawable(R.mipmap.img_open));
                    }
                }
                switchEdit(isChecked, ipv4Edit, subnetMaskEdit, defaultGatewayEdit, dnsServerEdit, wifiDetailLayout, confirmCancelLayout, manualSet);
            }
        });


        ipv4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEditWifi) {
                    ipv4Edit.requestFocus();
                    ipv4Title.setTextColor(getResources().getColor(R.color.src_c63d));
                    ipv4Edit.setTextColor(getResources().getColor(R.color.src_c63d));
                }
            }
        });
        ipv4Edit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    ipv4Title.setTextColor(getResources().getColor(R.color.src_c33));
                    ipv4Edit.setTextColor(getResources().getColor(R.color.src_c33));

                } else {
                    ipv4Title.setTextColor(getResources().getColor(R.color.src_c63d));
                    ipv4Edit.setTextColor(getResources().getColor(R.color.src_c63d));
                }
            }
        });
        ipv4.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    ipv4Title.setTextColor(getResources().getColor(R.color.white));
                    ipv4Edit.setTextColor(getResources().getColor(R.color.white));
                } else {
                    ipv4Title.setTextColor(getResources().getColor(R.color.src_c33));
                    ipv4Edit.setTextColor(getResources().getColor(R.color.src_c33));
                }
            }
        });

        subnetMask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEditWifi) {
                    subnetMaskEdit.requestFocus();
                    netMaskTitle.setTextColor(getResources().getColor(R.color.src_c63d));
                    subnetMaskEdit.setTextColor(getResources().getColor(R.color.src_c63d));
                }
            }
        });
        subnetMaskEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    netMaskTitle.setTextColor(getResources().getColor(R.color.src_c33));
                    subnetMaskEdit.setTextColor(getResources().getColor(R.color.src_c33));

                } else {
                    netMaskTitle.setTextColor(getResources().getColor(R.color.src_c63d));
                    subnetMaskEdit.setTextColor(getResources().getColor(R.color.src_c63d));
                }
            }
        });
        subnetMask.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    netMaskTitle.setTextColor(getResources().getColor(R.color.white));
                    subnetMaskEdit.setTextColor(getResources().getColor(R.color.white));
                } else {
                    netMaskTitle.setTextColor(getResources().getColor(R.color.src_c33));
                    subnetMaskEdit.setTextColor(getResources().getColor(R.color.src_c33));
                }
            }
        });


        defaultGateway.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEditWifi) {
                    defaultGatewayEdit.requestFocus();
                    gateWayTitle.setTextColor(getResources().getColor(R.color.src_c63d));
                    defaultGatewayEdit.setTextColor(getResources().getColor(R.color.src_c63d));
                }
            }
        });
        defaultGatewayEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    gateWayTitle.setTextColor(getResources().getColor(R.color.src_c33));
                    defaultGatewayEdit.setTextColor(getResources().getColor(R.color.src_c33));

                } else {
                    gateWayTitle.setTextColor(getResources().getColor(R.color.src_c63d));
                    defaultGatewayEdit.setTextColor(getResources().getColor(R.color.src_c63d));
                }
            }
        });
        defaultGateway.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    gateWayTitle.setTextColor(getResources().getColor(R.color.white));
                    defaultGatewayEdit.setTextColor(getResources().getColor(R.color.white));
                } else {
                    gateWayTitle.setTextColor(getResources().getColor(R.color.src_c33));
                    defaultGatewayEdit.setTextColor(getResources().getColor(R.color.src_c33));
                }
            }
        });


        dnsServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEditWifi) {
                    dnsServerEdit.requestFocus();
                    dnsTitle.setTextColor(getResources().getColor(R.color.src_c63d));
                    dnsServerEdit.setTextColor(getResources().getColor(R.color.src_c63d));
                }
            }
        });
        dnsServerEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    dnsTitle.setTextColor(getResources().getColor(R.color.src_c33));
                    dnsServerEdit.setTextColor(getResources().getColor(R.color.src_c33));

                } else {
                    dnsTitle.setTextColor(getResources().getColor(R.color.src_c63d));
                    dnsServerEdit.setTextColor(getResources().getColor(R.color.src_c63d));
                }
            }
        });
        dnsServer.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    dnsTitle.setTextColor(getResources().getColor(R.color.white));
                    dnsServerEdit.setTextColor(getResources().getColor(R.color.white));
                } else {
                    dnsTitle.setTextColor(getResources().getColor(R.color.src_c33));
                    dnsServerEdit.setTextColor(getResources().getColor(R.color.src_c33));
                }
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ssid = title.getText().toString().trim();
                String ipv4 = ipv4Edit.getText().toString().trim();
                String netMask = subnetMaskEdit.getText().toString().trim();
                String gateWay = defaultGatewayEdit.getText().toString().trim();
                String dns = dnsServerEdit.getText().toString().trim();
                //判断配置是否相同
                if (ipv4.equals(scanResult.ipv4) && netMask.equals(scanResult.netmask)
                        && gateWay.equals(scanResult.gateway) && dns.equals(scanResult.dns)) {
                    // showToast("网络配置相同");
                    ToastUtils.showToast(WifiActivity.this, "网络配置相同");
                } else {
                    boolean isSuccess = WifiUtil.changeWifiConfiguration(WifiActivity.this.getApplicationContext(), ssid, ipv4, netMask, gateWay, dns, "202.96.128.166");
                    if (isSuccess) {
                        //showToast("设置成功");
                        ToastUtils.showToast(WifiActivity.this, "设置成功");
                        //  SPUtils.getInstance().put(SharePrefer.WirelessSwitch,true);
                    } else {
                        //showToast("设置失败");
                        ToastUtils.showToast(WifiActivity.this, "设置失败");

                    }

                    normalDialog.dismiss();
                    finish();
                }


            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                normalDialog.dismiss();
                finish();
            }
        });

        title.setText(scanResult.name);
        ipv4Edit.setText(scanResult.ipv4);
        subnetMaskEdit.setText(scanResult.netmask);
        defaultGatewayEdit.setText(scanResult.gateway);
        dnsServerEdit.setText(scanResult.dns);


    }

    private void switchEdit(boolean isChecked, EditText ipv4Edit, EditText subnetMaskEdit, EditText defaultGatewayEdit, EditText dnsServerEdit, LinearLayout wifiDetailLayout, ConstraintLayout confirmCancelLayout, ConstraintLayout manualSet) {
        if (isChecked) {
            isEditWifi = true;
            ipv4Edit.setEnabled(true);
            subnetMaskEdit.setEnabled(true);
            defaultGatewayEdit.setEnabled(true);
            dnsServerEdit.setEnabled(true);
            wifiDetailLayout.setBackground(getResources().getDrawable(R.drawable.shape_rectangle_dialog_grey2));
            confirmCancelLayout.setBackground(getResources().getDrawable(R.drawable.shape_rectangle_dialog_grey));

        } else {
            isEditWifi = false;
            ipv4Edit.setEnabled(false);
            subnetMaskEdit.setEnabled(false);
            defaultGatewayEdit.setEnabled(false);
            dnsServerEdit.setEnabled(false);
            wifiDetailLayout.setBackground(getResources().getDrawable(R.drawable.shape_rectangle_dialog_grey));
            confirmCancelLayout.setBackground(getResources().getDrawable(R.drawable.shape_rectangle_dialog_grey2));
            manualSet.requestFocus();


        }
        Display defaultDisplay = getWindowManager().getDefaultDisplay();
        float scaleY = defaultDisplay.getHeight() / DensityUtils.HEIGHT;

        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) wifiDetailLayout.getLayoutParams();
        if (isEditWifi) {
            //lp.bottomMargin = (int) (DpUtil.dp2px(70) * scaleY);
            lp.bottomMargin = (int) (DpUtil.dp2px(60));
            confirmCancelLayout.setVisibility(View.VISIBLE);
        } else {
            lp.bottomMargin = DpUtil.dp2px(0);
            confirmCancelLayout.setVisibility(View.GONE);
        }
        wifiDetailLayout.setLayoutParams(lp);
    }


    private void exitDialog(BaseDialog normalDialog) {
        normalDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                normalDialog.dismiss();
                finish();
            }
        });
        /*normalDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if ((keyCode == KeyEvent.KEYCODE_BACK)) {
                    normalDialog.dismiss();
                    finish();
                }
                return false;
            }
        });*/
    }


    public void showAddWiFiDialog() {
        normalDialog = new BaseDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_wifi, null);
        normalDialog.setContentView(view);
        normalDialog.show();
        // normalDialog.setCanceledOnTouchOutside(false);
        NestedScrollView scrollView = view.findViewById(R.id.scrollView);
        LinearLayout wifiAddLayout = view.findViewById(R.id.wifi_add_layout);
        ConstraintLayout confirmCancelLayout = view.findViewById(R.id.confirm_cancel_layout);
        ConstraintLayout wifiName = view.findViewById(R.id.wifi_name);
        ConstraintLayout wifiPwd = view.findViewById(R.id.wifi_pwd);
        ConstraintLayout safeMode = view.findViewById(R.id.safe_mode);
        ConstraintLayout nothing = view.findViewById(R.id.nothing);
        EditText wifiNameEdit = view.findViewById(R.id.wifi_name_edit);
        EditText wifiPwdEdit = view.findViewById(R.id.wifi_pwd_edit);
        ImageView safeModeCheck = view.findViewById(R.id.safe_mode_check);
        ImageView nothingCheck = view.findViewById(R.id.nothing_check);
        TextView wifiNameTitle = view.findViewById(R.id.wifi_name_title);
        TextView wifiPwdTitle = view.findViewById(R.id.wifi_pwd_title);
        TextView safeModeTitle = view.findViewById(R.id.safe_mode_title);
        TextView nothingTitle = view.findViewById(R.id.nothing_title);

        TextView confirm = view.findViewById(R.id.confirm);
        TextView cancel = view.findViewById(R.id.cancel);
        ImageView imgBack = view.findViewById(R.id.img_back);
        //默认选中
        wifiNameEdit.requestFocus();
        wifiNameTitle.setTextColor(getResources().getColor(R.color.src_c63d));
        wifiNameEdit.setTextColor(getResources().getColor(R.color.src_c63d));
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                normalDialog.dismiss();
                finish();
            }
        });


        safeModeCheck.setImageResource(R.mipmap.img_checked_true_grey);
        nothingCheck.setImageResource(R.mipmap.img_checked_false_grey);
        exitDialog(normalDialog);

        wifiName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wifiNameEdit.requestFocus();
                wifiNameTitle.setTextColor(getResources().getColor(R.color.src_c63d));
                wifiNameEdit.setTextColor(getResources().getColor(R.color.src_c63d));
            }
        });

        wifiNameEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    wifiNameTitle.setTextColor(getResources().getColor(R.color.src_c33));
                    wifiNameEdit.setTextColor(getResources().getColor(R.color.src_c33));

                }
            }
        });

        wifiName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    wifiNameTitle.setTextColor(getResources().getColor(R.color.white));
                    wifiNameEdit.setTextColor(getResources().getColor(R.color.white));
                } else {
                    wifiNameTitle.setTextColor(getResources().getColor(R.color.src_c33));
                    wifiNameEdit.setTextColor(getResources().getColor(R.color.src_c33));
                }
            }
        });


        wifiPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wifiPwdEdit.requestFocus();
                wifiPwdTitle.setTextColor(getResources().getColor(R.color.src_c63d));
                wifiPwdEdit.setTextColor(getResources().getColor(R.color.src_c63d));
            }
        });

        wifiPwdEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    wifiPwdTitle.setTextColor(getResources().getColor(R.color.src_c33));
                    wifiPwdEdit.setTextColor(getResources().getColor(R.color.src_c33));

                }
            }
        });

        wifiPwd.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    wifiPwdTitle.setTextColor(getResources().getColor(R.color.white));
                    wifiPwdEdit.setTextColor(getResources().getColor(R.color.white));
                } else {
                    wifiPwdTitle.setTextColor(getResources().getColor(R.color.src_c33));
                    wifiPwdEdit.setTextColor(getResources().getColor(R.color.src_c33));
                }
            }
        });

        safeMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectSafeMode(safeMode, safeModeCheck, nothingCheck);


            }
        });
        safeModeCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectSafeMode(safeMode, safeModeCheck, nothingCheck);
            }
        });
        safeMode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (isSafeMode) {
                        safeModeCheck.setImageResource(R.mipmap.img_checked_true_white);
                    } else {
                        safeModeCheck.setImageResource(R.mipmap.img_checked_false_white);
                    }
                } else {
                    if (isSafeMode) {
                        safeModeCheck.setImageResource(R.mipmap.img_checked_true_grey);
                    } else {
                        safeModeCheck.setImageResource(R.mipmap.img_checked_false_grey);
                    }
                }


            }
        });


        nothing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectNothing(nothing, safeModeCheck, nothingCheck);
            }
        });
        nothing.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (isSafeMode) {
                        safeModeCheck.setImageResource(R.mipmap.img_checked_true_grey);
                        nothingCheck.setImageResource(R.mipmap.img_checked_false_white);
                    } else {
                        safeModeCheck.setImageResource(R.mipmap.img_checked_false_grey);
                        nothingCheck.setImageResource(R.mipmap.img_checked_true_white);
                    }
                } else {
                    if (isSafeMode) {
                        safeModeCheck.setImageResource(R.mipmap.img_checked_true_grey);
                        nothingCheck.setImageResource(R.mipmap.img_checked_false_grey);
                    } else {
                        safeModeCheck.setImageResource(R.mipmap.img_checked_false_grey);
                        nothingCheck.setImageResource(R.mipmap.img_checked_true_grey);
                    }
                }

            }
        });
        nothingCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("ppphhh", "nothingCheck");
                setSelectNothing(nothing, safeModeCheck, nothingCheck);
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = wifiNameEdit.getText().toString().trim();
                String pwd = wifiPwdEdit.getText().toString().trim();
                int type = 0;
                if (isSafeMode) {
                    type = 3;
                } else {
                    type = 1;
                }
                if (TextUtils.isEmpty(name)) {
                    ToastUtils.showToast(WifiActivity.this, "名称不能为空");
                    return;
                }
                if (TextUtils.isEmpty(pwd) && (type == 3)) {
                    ToastUtils.showToast(WifiActivity.this, "密码不能为空");
                    return;

                }


                Intent intent = new Intent();
                intent.putExtra("name", name);
                setResult(Constant.BACK_REFRESH, intent);
                int finalType = type;
                if (cachedThreadPool == null) {
                    cachedThreadPool = Executors.newCachedThreadPool();
                }
                cachedThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            boolean addWifi = WifiUtil.addWifi(wifiManager, name, pwd, finalType);
                            Log.i(TAG, "addWifi=" + addWifi);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        normalDialog.dismiss();
                        finish();
                    }
                }, 1000);


            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                normalDialog.dismiss();
                finish();
            }
        });


    }


    public void showDevelopDialog() {
        normalDialog = new BaseDialog(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_develop_switch, null);
        normalDialog.setContentView(view);
        normalDialog.show();
        ConstraintLayout videoRecordLayout = view.findViewById(R.id.video_record_layout);
        Switch videoRecordSwitch = view.findViewById(R.id.video_record_switch);
        ConstraintLayout algorithmDebugLayout = view.findViewById(R.id.algorithm_debug_layout);
        Switch algorithmDebugSwitch = view.findViewById(R.id.algorithm_debug_switch);
        ImageView imgBack = view.findViewById(R.id.img_back);
        exitDialog(normalDialog);
        imgBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                normalDialog.dismiss();
                finish();
            }
        });
        dealFocusEffect(videoRecordLayout, videoRecordSwitch);
        dealFocusEffect(algorithmDebugLayout, algorithmDebugSwitch);
        dealDevelopSwitch(videoRecordLayout, videoRecordSwitch,0);
        dealDevelopSwitch(algorithmDebugLayout, algorithmDebugSwitch,1);
        boolean isRecordVideo=SPUtils.getInstance().getBoolean(SharePrefer.RecordVideo);
        boolean algorithmDebug=SPUtils.getInstance().getBoolean(SharePrefer.AlgorithmDebug);
        videoRecordSwitch.setChecked(isRecordVideo);
        algorithmDebugSwitch.setChecked(algorithmDebug);
        videoRecordLayout.requestFocus();

    }

    private void dealDevelopSwitch(ConstraintLayout rootLayout, Switch switchButton,int type) {
        rootLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switchButton.isChecked()){
                    switchButton.setChecked(false);
                }else {
                    switchButton.setChecked(true);
                }

            }
        });
        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (type==0){
                    SystemProperties.set(Config.PERSIST_DATA_RECORD_VIDEO, isChecked+"");
                    SPUtils.getInstance().put(SharePrefer.RecordVideo,isChecked);
                }else if (type==1){
                    SystemProperties.set(Config.PERSIST_DATA_POINT_DEBUG, isChecked+"");
                    SPUtils.getInstance().put(SharePrefer.AlgorithmDebug,isChecked);
                }
                if (!isChecked) {
                    if (rootLayout.isFocused() || switchButton.isFocused()) {
                        switchButton.setTrackDrawable(getResources().getDrawable(R.mipmap.img_selected_close));
                    } else {
                        switchButton.setTrackDrawable(getResources().getDrawable(R.mipmap.img_close));
                    }
                } else {
                    if (rootLayout.isFocused() || switchButton.isFocused()) {
                        switchButton.setTrackDrawable(getResources().getDrawable(R.mipmap.img_selected_open));
                    } else {
                        switchButton.setTrackDrawable(getResources().getDrawable(R.mipmap.img_open));
                    }
                }
            }
        });
    }

    private void dealFocusEffect(ConstraintLayout rootLayout, Switch switchButton) {
        rootLayout.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (switchButton.isChecked()) {
                        switchButton.setTrackDrawable(getResources().getDrawable(R.mipmap.img_selected_open));
                    } else {
                        switchButton.setTrackDrawable(getResources().getDrawable(R.mipmap.img_selected_close));
                    }
                } else {
                    if (switchButton.isChecked()) {
                        switchButton.setTrackDrawable(getResources().getDrawable(R.mipmap.img_open));
                    } else {
                        switchButton.setTrackDrawable(getResources().getDrawable(R.mipmap.img_close));
                    }
                }
            }
        });
    }

    private void setSelectSafeMode(ConstraintLayout safeMode, ImageView safeModeCheck, ImageView nothingCheck) {
        isSafeMode = true;
        if (safeMode.isFocused()) {
            safeModeCheck.setImageResource(R.mipmap.img_checked_true_white);
            nothingCheck.setImageResource(R.mipmap.img_checked_false_grey);
        } else {
            safeModeCheck.setImageResource(R.mipmap.img_checked_true_grey);
            nothingCheck.setImageResource(R.mipmap.img_checked_false_grey);
        }
    }

    private void setSelectNothing(ConstraintLayout nothing, ImageView safeModeCheck, ImageView nothingCheck) {
        isSafeMode = false;
        if (nothing.isFocused()) {
            safeModeCheck.setImageResource(R.mipmap.img_checked_false_grey);
            nothingCheck.setImageResource(R.mipmap.img_checked_true_white);
        } else {
            safeModeCheck.setImageResource(R.mipmap.img_checked_false_grey);
            nothingCheck.setImageResource(R.mipmap.img_checked_true_grey);
        }
    }

    @Override
    protected void onDestroy() {
        if (normalDialog.isShowing()) {
            normalDialog.dismiss();
        }
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();

    }
}
