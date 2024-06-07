package com.zee.setting.fragment;


import android.Manifest;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.zee.setting.R;
import com.zee.setting.adapter.BondBluetoothAdapter;
import com.zee.setting.adapter.UnBondBluetoothAdapter;
import com.zee.setting.bean.BlueResult;
import com.zee.setting.cache.SPUtils;
import com.zee.setting.cache.SharePrefer;
import com.zee.setting.utils.ToastUtils;
import com.zee.setting.views.BaseDialog;

import net.sunniwell.aar.focuscontrol.layout.FocusControlRecyclerView;
import net.sunniwell.aar.focuscontrol.manager.FocusControlLinearLayoutManager;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class FragmentBluetooth extends Fragment {

    private View rootView;
    public static final String TAG = "Bluetooth";
    public final static int RC_BLUE = 10;
    public final static int ENABLE_BT = 1;
    private boolean isPermissionGranted = false;
    private HashMap<String, BlueResult> scanUnBondResultHashMap = new HashMap<>();
    private HashMap<String, BlueResult> scanBondResultHashMap = new HashMap<>();
    private List<BlueResult> bondResultList = new ArrayList<>();
    private List<BlueResult> unBondResultList = new ArrayList<>();
    private ImageView back;
    private FocusControlRecyclerView bondDeviceList;
    private FocusControlRecyclerView unBondDeviceList;
    private UnBondBluetoothAdapter unBondBluetoothAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private BondBluetoothAdapter bondBluetoothAdapter;
    private Handler handler = new Handler();
    private boolean isHide;
    private ExecutorService cachedThreadPool;
    private boolean isRemovePaired;
    private boolean isRetryConnect = false;
    //    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static UUID MY_UUID;
    public static int HEADSET = 1;
    public static int PHONE = 2;
    private Boolean isDelayRefresh = false;
    private ProgressBar progressBarBlu;
    private String macControl;
    private boolean isOtherDevice=false;
    //定义广播接收
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                //过滤遥控器
                if ((!TextUtils.isEmpty(device.getAddress())) && (device.getAddress().equals(macControl))) {
                    return;
                }
                //BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                short rssi = intent.getExtras().getShort(BluetoothDevice.EXTRA_RSSI);
                Log.i(TAG, "getName=" + device.getName() + "---getAddress=" + device.getAddress()
                        + "getBondState=" + device.getBondState() + "getType=" + device.getType() + "----rssi" + rssi);
                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    //显示已配对设备
                    String name = device.getName();
                    String mac = device.getAddress();
                    boolean connected = isConnected(device.getAddress());
                    Log.i(TAG, "connected=" + connected);
                    BlueResult result = null;
                    if (connected) {
                        result = new BlueResult(device, rssi, true, true, HEADSET);
                    } else {
                        result = new BlueResult(device, rssi, true, PHONE);
                    }
                    //采用mac作为唯一标志，因为有些不同类型或者同种类型设备名字相同
                    if ((!TextUtils.isEmpty(name)) && (!TextUtils.isEmpty(mac))) {
                        BlueResult blueResult = scanBondResultHashMap.get(mac);
                        if (blueResult != null) {
                            if (blueResult.rssi < rssi) {
                                bondResultList.remove(blueResult);
                                scanBondResultHashMap.put(mac, result);
                                bondResultList.add(result);
                                bondBluetoothAdapter.notifyDataSetChanged();
                            }
                        } else {
                            scanBondResultHashMap.put(mac, result);
                            bondResultList.add(result);
                            bondBluetoothAdapter.notifyDataSetChanged();

                        }

                    }


                } else if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    // ToastUtils.showToast(getActivity(),"搜索到未配对设备");
                    String mac = device.getAddress();
                    String name = device.getName();
                    int type = 0;
                    if (TextUtils.isEmpty(getBluetoothType(device))) {
                        type = PHONE;
                    } else {
                        type = HEADSET;
                    }
                    BlueResult result = new BlueResult(device, rssi, false, type);
                    if (!TextUtils.isEmpty(name)) {
                        BlueResult blueResult = scanUnBondResultHashMap.get(mac);
                        if (blueResult != null) {
                            if (blueResult.rssi < rssi) {
                                unBondResultList.remove(blueResult);
                                scanUnBondResultHashMap.put(mac, result);
                                unBondResultList.add(result);
                                unBondBluetoothAdapter.notifyDataSetChanged();
                            }
                        } else {
                            scanUnBondResultHashMap.put(mac, result);
                            unBondResultList.add(result);
                            unBondBluetoothAdapter.notifyDataSetChanged();
                        }
                        progressBarBlu.setVisibility(View.GONE);

                    }

                }


            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                //设备绑定状态改变
                //过滤遥控器
                if (((!TextUtils.isEmpty(device.getAddress())) && (device.getAddress().equals(macControl)))||(TextUtils.isEmpty(device.getName()))) {
                    return;
                }
                //  BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                if (bondState == BluetoothDevice.BOND_BONDED) {
//                    ToastUtils.showToast(getActivity(), "与设备" + device.getName() + "配对成功");
                    for (int i = 0; i < unBondResultList.size(); i++) {
                        BlueResult result = unBondResultList.get(i);
                        BluetoothDevice bluetoothDevice = result.bluetoothDevice;
                        if (device.getAddress().equals(bluetoothDevice.getAddress())) {
                            result.isPaired = true;
                            unBondResultList.remove(result);
                            scanBondResultHashMap.put(device.getAddress(), result);
                            bondResultList.add(0, result);


                        }
                    }

                    dealRefresh(true);
                    unBondBluetoothAdapter.notifyDataSetChanged();
                    bondBluetoothAdapter.notifyDataSetChanged();
                    dealSelectFocus();
                    //音频设备才进行连接,手机不必连接
                    checkDeviceAndConnect(device);


                } else if (bondState == BluetoothDevice.BOND_NONE) {

                    // Toast.makeText(getActivity(), "已取消与设备" + device.getName() + "的配对", Toast.LENGTH_SHORT).show();
                    for (int i = 0; i < bondResultList.size(); i++) {
                        BlueResult result = bondResultList.get(i);
                        BluetoothDevice bluetoothDevice = result.bluetoothDevice;
                        if (device.getAddress().equals(bluetoothDevice.getAddress())) {
                            result.isPaired = false;
                            bondResultList.remove(result);
                            scanUnBondResultHashMap.put(device.getAddress(), result);
                            unBondResultList.add(result);


                        }
                    }
                    if (isRetryConnect) {
                        return;
                    }
                    unBondBluetoothAdapter.notifyDataSetChanged();
                    bondBluetoothAdapter.notifyDataSetChanged();
                    if ((!isRemovePaired)) {
                        ToastUtils.showToast(getActivity(), "无法与“" + device.getName() + "”进行通信");
                    } else {
                        if (bondResultList.size() == 0) {
                            unBondDeviceList.getChildAt(0).requestFocus();
                        }
                        isRemovePaired = false;
                    }


                }


            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.i(TAG, "ACTION_DISCOVERY_STARTED");
//               ToastUtils.showToast(getActivity(),"开始搜索");
//                scanUnBondResultHashMap.clear();
//                scanBondResultHashMap.clear();
//                unBondResultList.clear();
//                bondResultList.clear();


            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
//              ToastUtils.showToast(getActivity(),"搜索完毕");

            } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                switch (blueState) {
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.i(TAG, "onReceive---------STATE_TURNING_ON");
//                        Toast.makeText(getActivity(),"蓝牙正在开启",Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.i(TAG, "onReceive---------STATE_ON");
//                        Toast.makeText(getActivity(),"蓝牙开启",Toast.LENGTH_SHORT).show();
                        scanBluetooth();

                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.i(TAG, "onReceive---------STATE_TURNING_OFF");
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        Log.i(TAG, "onReceive---------STATE_OFF");
                        break;
                }
            } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                //连接成功
                if (isRetryConnect) {
                    isRetryConnect = false;
                }
                //BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.i(TAG, "ACTION_ACL_CONNECTED=" + device.getName());
                String bluetoothType = getBluetoothType(device);
                //如果不是耳机就不必执行下去
                if (TextUtils.isEmpty(bluetoothType)) {
                    return;
                }
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        rootView.post(new Runnable() {
                            @Override
                            public void run() {
                                for (int i = 0; i < bondResultList.size(); i++) {
                                    BlueResult result = bondResultList.get(i);
                                    BluetoothDevice bluetoothDevice = result.bluetoothDevice;
                                    if (device.getAddress().equals(bluetoothDevice.getAddress())) {
                                        bondResultList.get(i).setConnected(true);
                                        bondBluetoothAdapter.notifyDataSetChanged();
                                        timer.cancel();

                                    }
                                }
                            }
                        });

                    }
                }, 0, 1000);


            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                //连接失败
                if (isRetryConnect) {
                    isRetryConnect = false;
                }
                //BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.i(TAG, "ACTION_ACL_DISCONNECTED=" + device.getName());

            }

            /*else  if (action.equals(BluetoothDevice.ACTION_PAIRING_REQUEST)){
                Log.d(TAG, "ACTION_PAIRING_REQUEST 配对请求");
                //获得蓝牙设备
                BluetoothDevice btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Bundle extras = intent.getExtras();
                //"android.bluetooth.device.extra.PAIRING_KEY"
                Object pairkey = extras.get(BluetoothDevice.EXTRA_PAIRING_KEY);	//配对的pin码

                Log.d("zwt", "device-->"+String.valueOf(btDevice )+":::pairkey-->"+String.valueOf(pairkey));

                btDevice.setPairingConfirmation(true);
                //消费这个广播，不然这个广播传到底层就会又弹出配对界面，一闪而过
                abortBroadcast();
                //ret为true 表示设置成功，fales表示不成功
                boolean ret = btDevice.setPin(pairkey.toString().getBytes());
            }*/


        }
    };


    private void checkDeviceAndConnect(BluetoothDevice device) {
        String bluetoothType = getBluetoothType(device);
        if (!TextUtils.isEmpty(bluetoothType)) {
            connectDevice(device);
        }
    }

    private void connectDevice(BluetoothDevice device) {
        if (cachedThreadPool == null) {
            cachedThreadPool = Executors.newCachedThreadPool();
        }
        cachedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                //连接
                try {
                    MY_UUID = UUID.randomUUID();
                    BluetoothSocket rfcommSocketToServiceRecord = device.createRfcommSocketToServiceRecord(MY_UUID);
                    rfcommSocketToServiceRecord.connect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    //这个为了防止有时绑定设备列表数据刷新导致焦点飞出
    private void dealRefresh(boolean refresh) {
        if (refresh) {
            getActivity().findViewById(R.id.network).setFocusable(false);
            getActivity().findViewById(R.id.common).setFocusable(false);
            getActivity().findViewById(R.id.ai).setFocusable(false);
            getActivity().findViewById(R.id.system).setFocusable(false);
            getActivity().findViewById(R.id.about).setFocusable(false);
            getActivity().findViewById(R.id.bluetooth).setFocusable(false);
        } else {
            getActivity().findViewById(R.id.network).setFocusable(true);
            getActivity().findViewById(R.id.common).setFocusable(true);
            getActivity().findViewById(R.id.ai).setFocusable(true);
            getActivity().findViewById(R.id.system).setFocusable(true);
            getActivity().findViewById(R.id.about).setFocusable(true);
            getActivity().findViewById(R.id.bluetooth).setFocusable(true);
        }

    }


    private void dealSelectFocus() {

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                defaultSelectPaired();
            }
        }, 20);


    }

    private void defaultSelectPaired() {
        View childAt = bondDeviceList.getChildAt(0);
        if (childAt != null) {
            childAt.requestFocus();
            dealRefresh(false);
        }


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_bluetooth, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i("ssshhh", "onActivityCreated");
        requestPermission();
        initView();
        initData();
        initListener();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void initData() {
        // 1、得到蓝牙适配器
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //  2、判断蓝牙是否打开
        if (!mBluetoothAdapter.isEnabled()) {
            //若没打开则打开蓝牙
            mBluetoothAdapter.enable();
//            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(enableBtIntent, ENABLE_BT);

        }
        macControl = SPUtils.getInstance().getString(SharePrefer.RemoteControl, "");

        // 注册这个 BroadcastReceiver
        //IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        IntentFilter intent = new IntentFilter();
        intent.addAction(BluetoothDevice.ACTION_FOUND);
        intent.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intent.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        intent.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        //注册一个搜索结束时的广播
        intent.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intent.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intent.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intent.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        getActivity().registerReceiver(mReceiver, intent);

        //扫描
        scanBluetooth();
    }

    private void scanBluetooth() {
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //若是扫描不到记得手机手动开启位置服务，这个对于wifi扫描和蓝牙扫描都是一样的
                //搜索到的蓝牙设备通过广播接收
                scanUnBondResultHashMap.clear();
                scanBondResultHashMap.clear();
                unBondResultList.clear();
                bondResultList.clear();
                progressBarBlu.setVisibility(View.VISIBLE);
                String address = mBluetoothAdapter.getAddress();
                //Log.i("wwwhhh", "---address---=" + address);

                getPairBLEAndConnectBLE();
                mBluetoothAdapter.startDiscovery();


            }
        }, 1000);

    }


    private void initView() {
        back = rootView.findViewById(R.id.img_back);
        bondDeviceList = rootView.findViewById(R.id.bond_device_list);
        unBondDeviceList = rootView.findViewById(R.id.un_bond_device_list);
        progressBarBlu = rootView.findViewById(R.id.progress_bar_blu);

        FocusControlLinearLayoutManager bondManagerVertical = new FocusControlLinearLayoutManager(getActivity());
        bondManagerVertical.setOrientation(LinearLayoutManager.VERTICAL);
        bondDeviceList.setLayoutManager(bondManagerVertical);

        FocusControlLinearLayoutManager unBondManagerVertical = new FocusControlLinearLayoutManager(getActivity());
        unBondManagerVertical.setOrientation(LinearLayoutManager.VERTICAL);
        unBondDeviceList.setLayoutManager(unBondManagerVertical);

        bondBluetoothAdapter = new BondBluetoothAdapter(bondResultList);
        bondBluetoothAdapter.setHasStableIds(true);
        bondDeviceList.setAdapter(bondBluetoothAdapter);
        bondDeviceList.setItemAnimator(null);

        unBondBluetoothAdapter = new UnBondBluetoothAdapter(unBondResultList);
        unBondBluetoothAdapter.setHasStableIds(true);
        unBondDeviceList.setAdapter(unBondBluetoothAdapter);
        unBondDeviceList.setItemAnimator(null);


    }


    private void initListener() {
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawerLayout drawerLayout = getActivity().findViewById(R.id.drawer_layout);
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });
        unBondBluetoothAdapter.setItemClick(new UnBondBluetoothAdapter.OnItemClick() {
            @Override
            public void onItemClick(String name, BlueResult blueResult) {
                BluetoothDevice device = blueResult.bluetoothDevice;
                //   Toast.makeText(getActivity(), device.getName(), Toast.LENGTH_SHORT).show();
                ToastUtils.showToast(getActivity(), "正在请求配对");
                pairedDevice(device);

            }
        });

        bondBluetoothAdapter.setItemClick(new BondBluetoothAdapter.OnItemClick() {
            @Override
            public void onItemClick(String name, BlueResult blueResult) {
                if (blueResult != null) {
                    BluetoothDevice device = blueResult.bluetoothDevice;
                    showIgnoreDialog(device);

                } else {
                    if (bondResultList.size()>0){
                        showCleanBluetoothDialog();
                    }else {
                        ToastUtils.showToast(getActivity(),"没有蓝牙配对信息需要清除");
                    }

                }

            }
        });

    }

    private void pairedDevice(BluetoothDevice device) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //Android 4.4 API 19 以上才开放Bond接口
            device.createBond();
        } else {
            //API 19 以下用反射调用Bond接口
            try {
                device.getClass().getMethod("connect").invoke(device);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void requestPermission() {
        //1. 检查是否已经有该权限
        if (Build.VERSION.SDK_INT >= 23 && (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.BLUETOOTH)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)) {
            //2. 权限没有开启，请求权限
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.BLUETOOTH,
                            Manifest.permission.BLUETOOTH_ADMIN,
                            Manifest.permission.BLUETOOTH_CONNECT,
                            Manifest.permission.BLUETOOTH_SCAN,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION

                    }, RC_BLUE);
        } else {
            //权限已经开启，做相应事情
            isPermissionGranted = true;


        }
    }

    //3. 接收申请成功或者失败回调
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RC_BLUE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //权限被用户同意,做相应的事情
                isPermissionGranted = true;
            } else {
                //权限被用户拒绝
                ToastUtils.showToast(getActivity(), "请打开权限");
            }
        } else if (requestCode == ENABLE_BT) {
            if (requestCode == getActivity().RESULT_OK) {
                ToastUtils.showToast(getActivity(), "蓝牙已经开启");
            } else if (requestCode == getActivity().RESULT_CANCELED) {
                ToastUtils.showToast(getActivity(), "没有蓝牙权限");

            }
        }


    }

    //反射来调用BluetoothDevice.removeBond取消设备的配对
    private void unpairDevice(BluetoothDevice device) {
        try {
            Method m = device.getClass()
                    .getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
            isRemovePaired = true;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void removePairDevice() {
        if (mBluetoothAdapter != null) {
            for (int i = 0; i < bondResultList.size(); i++) {
                BluetoothDevice device = bondResultList.get(i).getBluetoothDevice();
//                Log.i("wwwhhh", "rm_name_aa=" + device.getName() + "mac=" + device.getAddress()+"macControl="+macControl);
                if ((!TextUtils.isEmpty(device.getAddress())) && (!device.getAddress().equals(macControl))) {
                    unpairDevice(device);
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }


            }
        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mReceiver);
        handler.removeCallbacksAndMessages(null);

    }

    public void showIgnoreDialog(BluetoothDevice device) {
        BaseDialog normalDialog = new BaseDialog(getActivity());
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_ignore_bluetooth, null);
        normalDialog.setContentView(view);
        normalDialog.show();
        TextView title = view.findViewById(R.id.title);
        ImageView back = view.findViewById(R.id.img_back);
        ConstraintLayout connectDevice = view.findViewById(R.id.connect_device);
        ConstraintLayout ignoreDevice = view.findViewById(R.id.ignore_device);
        title.setText(device.getName());
        ignoreDevice.requestFocus();
        String bluetoothType = getBluetoothType(device);
        boolean connected = isConnected(device.getAddress());
        if ((!TextUtils.isEmpty(bluetoothType)) && (connected == false)) {
            connectDevice.setVisibility(View.VISIBLE);
            connectDevice.requestFocus();
        } else {
            connectDevice.setVisibility(View.GONE);
            ignoreDevice.requestFocus();
        }
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                normalDialog.dismiss();
            }
        });
        ignoreDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                normalDialog.dismiss();
                unpairDevice(device);

            }
        });
        connectDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                normalDialog.dismiss();
                isRetryConnect = true;
                unpairDevice(device);
                bondDeviceList.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pairedDevice(device);
                    }
                }, 1000);


            }
        });
    }

    public void showCleanBluetoothDialog() {
        BaseDialog normalDialog = new BaseDialog(getActivity(), R.style.dialogStyle);
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_alert, null);
        normalDialog.setContentView(view);
        normalDialog.show();
        LinearLayout headRoot = view.findViewById(R.id.head_root);
        headRoot.setVisibility(View.GONE);
        TextView description = view.findViewById(R.id.description);
        description.setText("此操作将会清除您设备中所有\n" +
                "蓝牙配对信息");
        TextView confirm = view.findViewById(R.id.confirm);
        TextView cancel = view.findViewById(R.id.cancel);
        confirm.requestFocus();
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dealRefresh(true);
                normalDialog.dismiss();
              //  Log.i("wwwhhh","macControl="+macControl);
                removePairDevice();
                rootView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dealRefresh(false);
                    }
                }, 100);


            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                normalDialog.dismiss();
            }
        });
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden && isAdded()) {
            //显示
            isHide = false;
            if (isDelayRefresh) {
                isDelayRefresh = false;
                refreshBondList();
            }
        } else {
            //隐藏
            isHide = true;
        }
    }


    public String getBluetoothType(BluetoothDevice device) {
        isOtherDevice=false;
        BluetoothClass bluetoothClass = device.getBluetoothClass();
        final int deviceClass = bluetoothClass.getDeviceClass(); //设备类型（音频、手机、电脑、音箱等等）
        final int majorDeviceClass = bluetoothClass.getMajorDeviceClass();//具体的设备类型（例如音频设备又分为音箱、耳机、麦克风等等）
        if (deviceClass == BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET) {
            //音箱
            Log.i(TAG, "audio=" + device.getName());
            return "audio";

        } else if (deviceClass == BluetoothClass.Device.AUDIO_VIDEO_MICROPHONE) {
            //麦克风
            Log.i(TAG, "microphone=" + device.getName());
            return "microphone";

        } else if (deviceClass == BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES) {
            Log.i(TAG, "headset=" + device.getName());
            //耳机
            return "headset";

        } else if (majorDeviceClass == BluetoothClass.Device.Major.COMPUTER) {
            //电脑
            Log.i(TAG, "computer=" + device.getName());

        } else if (majorDeviceClass == BluetoothClass.Device.Major.PHONE) {
            //手机
            Log.i(TAG, "phone=" + device.getName());

        } else if (majorDeviceClass == BluetoothClass.Device.Major.HEALTH) {
            //健康类设备
            Log.i(TAG, "health=" + device.getName());

        } else {
            //其它蓝牙设备
            Log.i(TAG, "other=" + device.getName());
            isOtherDevice=true;

        }
        return "";
    }


    public boolean isConnected(String macAddress) {
        if (!BluetoothAdapter.checkBluetoothAddress(macAddress)) {
            return false;
        }
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(macAddress);

        Method isConnectedMethod = null;
        boolean isConnected;
        try {
            isConnectedMethod = BluetoothDevice.class.getDeclaredMethod("isConnected", (Class[]) null);
            isConnectedMethod.setAccessible(true);
            isConnected = (boolean) isConnectedMethod.invoke(device, (Object[]) null);
        } catch (NoSuchMethodException e) {
            isConnected = false;
        } catch (IllegalAccessException e) {
            isConnected = false;
        } catch (InvocationTargetException e) {
            isConnected = false;
        }
        return isConnected;
    }

    private void getPairBLEAndConnectBLE() {
        BluetoothAdapter defaultAdapter = BluetoothAdapter.getDefaultAdapter();
        if (defaultAdapter != null) {
            //得到已配对的设备列表
            Set<BluetoothDevice> devices = defaultAdapter.getBondedDevices();
            for (BluetoothDevice bluetoothDevice : devices) {
                boolean isConnect = false;
                try {
                    //获取当前连接的蓝牙信息
                    isConnect = (boolean) bluetoothDevice.getClass().getMethod("isConnected").invoke(bluetoothDevice);
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    e.printStackTrace();
                }

                if (isConnect) {
                    Log.d(TAG, "device2=" + bluetoothDevice.getAddress());
                  //  Log.d("wwwhhh", "device2=" + bluetoothDevice.getAddress());

                }
                Log.d(TAG, "device1=" + bluetoothDevice.getName());
              //  Log.d("wwwhhh", "device1=" + bluetoothDevice.getName());

                //显示已配对设备
                String name = bluetoothDevice.getName();
                String mac = bluetoothDevice.getAddress();
                boolean connected = isConnected(bluetoothDevice.getAddress());
                Log.i(TAG, "connected=" + connected);

                BlueResult result = null;
                if (connected) {
                    result = new BlueResult(bluetoothDevice, (short) 0, true, true, HEADSET);
                } else {
                    result = new BlueResult(bluetoothDevice, (short) 0, true, PHONE);
                }
                getBluetoothType(bluetoothDevice);
                BluetoothClass bluetoothClass = bluetoothDevice.getBluetoothClass();
                int deviceClass = bluetoothClass.getDeviceClass(); //设备类型（音频、手机、电脑、音箱等等）
                int majorDeviceClass = bluetoothClass.getMajorDeviceClass();
//                Log.i("wwwhhh","isOtherDevice="+isOtherDevice);
//                Log.i("wwwhhh", "deviceClass=" + deviceClass + "majorDeviceClass=" + majorDeviceClass + "name=" + bluetoothDevice.getName());
                if ((deviceClass == 1292 && majorDeviceClass == 1280)||isOtherDevice || TextUtils.isEmpty(name)) {
                    macControl = bluetoothDevice.getAddress();
                    SPUtils.getInstance().put(SharePrefer.RemoteControl, macControl);
                    continue;
                }
                //过滤遥控器
                scanBondResultHashMap.put(mac, result);
                bondResultList.add(result);
                if (isHide) {
                    isDelayRefresh = true;
                    return;
                }
                progressBarBlu.setVisibility(View.GONE);
                refreshBondList();


            }


        }
    }

    private void refreshBondList() {
        dealRefresh(true);
        bondBluetoothAdapter.notifyDataSetChanged();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                dealRefresh(false);
            }
        }, 100);
    }


}
