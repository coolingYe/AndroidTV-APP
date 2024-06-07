package com.zee.setting.activity;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.zee.setting.R;
import com.zee.setting.base.BaseActivity;
import com.zee.setting.utils.AppUtils;
import com.zee.setting.utils.DensityUtils;
import com.zee.setting.utils.NetSpeed;
import com.zee.setting.utils.ToastUtils;
import com.zee.setting.views.CurveView;
import com.zee.setting.views.DashboardView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NetSpeedActivity extends BaseActivity {
    private DashboardView dashboardView;
    private CurveView curveView;
    private int num;
    private DownloadManager downloadManager;
    private long downloadId;
    private File file;
    //广播监听下载的各个状态
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                if (file != null && file.exists()) {
                    //下载完成删除它
                    file.delete();
                }
            }
        }
    };
    private LinearLayout layoutMeasurement;
    private LinearLayout layoutCurve;
    private TextView downloadSpeedTv;
    private TextView videoQualityTv;
    private TextView networkOptimization;
    private TextView networkMeasure;
    private List<String> xList;
    private List<String> yList;
    private Timer timer;
    private float speed;
    private float maxSpeed;
    private int status;
    private CheckBox checkboxInit;
    private CheckBox checkboxUnlock;
    private CheckBox checkboxSelect;
    private ProgressBar progressInit;
    private ProgressBar progressUnlock;
    private ProgressBar progressSelect;
    private View lineFirst;
    private View lineSecond;
    private Timer timerOptimize;
    private TextView title;
    private ConstraintLayout layoutOptimization;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DensityUtils.autoWidth(getApplication(), this);
        setContentView(R.layout.activity_net_speed);
        initView();
      //  measureSpeed();

    }

    private void initView() {
        title = findViewById(R.id.title);
        layoutOptimization = findViewById(R.id.layout_optimization);
        //刻度盘
        dashboardView = findViewById(R.id.dashboard_view);
        //曲线图
        curveView = findViewById(R.id.curveView);
        curveView.setContentWidth(32);
        layoutMeasurement = findViewById(R.id.layout_measurement);
        layoutCurve = findViewById(R.id.layout_curve);
        downloadSpeedTv = findViewById(R.id.download_speed_tv);
        videoQualityTv = findViewById(R.id.video_quality_tv);
        networkOptimization = findViewById(R.id.network_optimization);
        networkMeasure = findViewById(R.id.network_measure);
        //默认
        startMeasureSpeed();
        networkMeasure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMeasureSpeed();
            }
        });
        networkOptimization.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              //  startNetOptimization();
                ToastUtils.showToast(NetSpeedActivity.this,"敬请期待");
            }
        });

        //网络优化
        checkboxInit = findViewById(R.id.checkbox_init);
        checkboxUnlock = findViewById(R.id.checkbox_unlock);
        checkboxSelect = findViewById(R.id.checkbox_select);
        progressInit = findViewById(R.id.progress_init);
        progressUnlock = findViewById(R.id.progress_unlock);
        progressSelect = findViewById(R.id.progress_select);
        lineFirst = findViewById(R.id.line_first);
        lineSecond = findViewById(R.id.line_second);
        //startNetOptimization();


    }

    private void startNetOptimization() {
        title.setText("网络优化");
        dashboardView.setVisibility(View.GONE);
        layoutCurve.setVisibility(View.GONE);
        layoutMeasurement.setVisibility(View.GONE);
        layoutOptimization.setVisibility(View.VISIBLE);
        status=0;
        lineFirst.setSelected(false);
        lineSecond.setSelected(false);
        checkboxInit.setChecked(false);
        checkboxUnlock.setChecked(false);
        checkboxSelect.setChecked(false);
        progressInit.setVisibility(View.VISIBLE);
        progressUnlock.setVisibility(View.GONE);
        progressSelect.setVisibility(View.GONE);


        timerOptimize = new Timer();
        timerOptimize.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (status==0){
                            checkboxInit.setVisibility(View.GONE);
                            progressInit.setVisibility(View.VISIBLE);
                        }else if (status==1){
                            checkboxInit.setVisibility(View.VISIBLE);
                            progressInit.setVisibility(View.GONE);
                            checkboxInit.setChecked(true);
                            lineFirst.setSelected(true);

                            checkboxUnlock.setVisibility(View.GONE);
                            progressUnlock.setVisibility(View.VISIBLE);
                        }else if (status==2){
                            checkboxUnlock.setVisibility(View.VISIBLE);
                            progressUnlock.setVisibility(View.GONE);
                            checkboxUnlock.setChecked(true);
                            lineSecond.setSelected(true);

                            checkboxSelect.setVisibility(View.GONE);
                            progressSelect.setVisibility(View.VISIBLE);
                        }else if (status==3){
                            checkboxSelect.setVisibility(View.VISIBLE);
                            progressSelect.setVisibility(View.GONE);
                            checkboxSelect.setChecked(true);
                            timerOptimize.cancel();
                            dashboardView.setVisibility(View.VISIBLE);
                            layoutCurve.setVisibility(View.GONE);
                            layoutMeasurement.setVisibility(View.VISIBLE);
                            layoutOptimization.setVisibility(View.GONE);


                        }
                        status =status+1;
                    }
                });



            }
        },0,3000);
    }

    private void startMeasureSpeed() {
        title.setText("网络测速");
        layoutMeasurement.setVisibility(View.GONE);
        layoutCurve.setVisibility(View.VISIBLE);
        measureSpeed();
    }

    private void measureSpeed() {
        String packageName = AppUtils.getPackageName(this);
        int uid = AppUtils.getUid(this, packageName);
        downloadFile();
        showSpeed(uid);
    }

    private void showSpeed(int uid) {
        num=0;
        if (xList==null){
            xList = new ArrayList<>();
            yList = new ArrayList<>();
        }else {
            xList.clear();
            yList.clear();
        }


        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if (num<=10){
                    String netSpeed = NetSpeed.getNetSpeed(uid);
                    float temp =  Float.parseFloat(netSpeed);
                    speed = (float)(Math.round(temp*100))/100;
                    xList.add(num+"");
                    yList.add(speed +"");
                    num+=1;
                    //设置speed显示的极限速度
                    if (speed>300){
                        speed=300;
                    }
                    if (speed>maxSpeed){
                        maxSpeed=speed;
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            curveView.setData(xList, yList);
                            dashboardView.setRealTimeValue(speed);
                            dashboardView.setSelect(speed / 300f);
                        }
                    });

                }else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            layoutMeasurement.setVisibility(View.VISIBLE);
                            layoutCurve.setVisibility(View.GONE);
                            networkMeasure.requestFocus();
                            xList.clear();
                            yList.clear();
                            curveView.setData(xList, yList);
                            timer.cancel();
                            downloadSpeedTv.setText(maxSpeed+"Mbps");
                            if (maxSpeed>10){
                                videoQualityTv.setText("蓝光视频");
                            }else if (maxSpeed>2){
                                videoQualityTv.setText("高清视频");
                            }else if (maxSpeed>1){
                                videoQualityTv.setText("标清视频");
                            }else {
                                videoQualityTv.setText("网速不好，无法观看视频");
                            }
                            maxSpeed=0;

                        }
                    });

                }
            }
        };
        timer.schedule(task, 1000, 200);
    }

    public void downloadFile() {
//        DownloadManager.Request request = new DownloadManager.Request(Uri.parse("https://sjws.ssl.qihucdn.com/mobile/shouji360/360safesis/20220803-1852/360MobileSafe_8.9.5.1016_jg.apk"));
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse("https://oss-gz.zeewain.com/rk-net-test/SYSTEM/1ac8394a3dff4f7ea84f4d53ab878bf2.apk"));
        //允许移动网络与WIFI下载
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        //是否在通知栏显示下载进度
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        //设置通知栏标题
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        request.setTitle("下载");
        request.setDescription("今日头条正在下载");
        request.setAllowedOverRoaming(false);
        file = new File(this.getExternalFilesDir(""),"appCode.apk");
        //  file = new File(this.getExternalFilesDir(""), "appCode.apk");
        request.setDestinationUri(Uri.fromFile(file));
        //设置文件存放目录
        if (downloadManager == null) {
            downloadManager = (DownloadManager) NetSpeedActivity.this.getSystemService(Context.DOWNLOAD_SERVICE);
        }
        if (downloadManager != null) {
            downloadId = downloadManager.enqueue(request);
        }
        //注册广播监测下载情况
        registerReceiver(receiver,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver!=null){
            unregisterReceiver(receiver);
        }
        if (file != null && file.exists()) {
            file.delete();
        }
    }
}
