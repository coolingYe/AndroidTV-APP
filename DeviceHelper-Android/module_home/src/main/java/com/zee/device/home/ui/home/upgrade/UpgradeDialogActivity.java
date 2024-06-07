package com.zee.device.home.ui.home.upgrade;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.zee.device.base.config.BaseConstants;
import com.zee.device.base.dialog.UpgradeTipDialog;
import com.zee.device.base.ui.BaseActivity;
import com.zee.device.base.utils.ApkUtil;
import com.zee.device.base.utils.DensityUtils;
import com.zee.device.base.utils.FileUtils;
import com.zee.device.base.utils.ToastUtils;
import com.zee.device.base.widgets.GradientProgressView;
import com.zee.device.home.data.protocol.response.UpgradeResp;
import com.zee.device.home.ui.home.utils.DownloadHelper;
import com.zwn.lib_download.DownloadListener;
import com.zwn.lib_download.DownloadService;
import com.zwn.lib_download.db.CareController;
import com.zwn.lib_download.model.DownloadInfo;

import java.io.File;

public class UpgradeDialogActivity extends BaseActivity {
    private TextView titleTextView;
    private TextView progressTextView;
    private LinearLayout positiveCancelLayout;
    private GradientProgressView gradientProgressView;
    public DownloadService.DownloadBinder downloadBinder = null;


    private final DownloadListener downloadListener = new DownloadListener() {
        @Override
        public void onProgress(String fileId, int progress, long loadedSize, long fileSize) {
            if (fileId.equals(BaseConstants.ZEE_HELP_APP_SOFTWARE_CODE)) {
                runOnUiThread(() -> {
                    if (positiveCancelLayout != null && positiveCancelLayout.getVisibility() == View.VISIBLE) {
                        positiveCancelLayout.setVisibility(View.GONE);
                    }
                    if (progress < 100) {
                        setTitleText("正在更新软件");
                        setLoadingProgress(progress);
                    }
                });
            }
        }

        @Override
        public void onSuccess(String fileId, int type, File file) {
            if (fileId.equals(BaseConstants.ZEE_HELP_APP_SOFTWARE_CODE)) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startInstall();
                        finish();
                    }
                });
            }
        }

        @Override
        public void onFailed(String fileId, int type, int code) {
            if (fileId.equals(BaseConstants.ZEE_HELP_APP_SOFTWARE_CODE)) {
                runOnUiThread(() -> {
                    setTitleText("网络异常");
                    if (positiveCancelLayout != null) {
                        positiveCancelLayout.setVisibility(View.VISIBLE);
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
        }
    };

    private void startInstall() {
        DownloadInfo downloadInfo = CareController.instance.getDownloadInfoByFileId(BaseConstants.ZEE_HELP_APP_SOFTWARE_CODE);
        ToastUtils.showShort("安装apk");
        installApk(downloadInfo.filePath);
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadBinder = (DownloadService.DownloadBinder) service;
            if (downloadBinder != null) {
                downloadBinder.registerDownloadListener(downloadListener);
                Bundle bundle = getIntent().getBundleExtra(BaseConstants.EXTRA_UPGRADE_INFO);
                UpgradeResp upgradeResp = (UpgradeResp) bundle.getSerializable(BaseConstants.EXTRA_UPGRADE_INFO);
                handleUpgrade(upgradeResp);

            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DensityUtils.autoWidth(getApplication(), this);
        setContentView(com.zee.device.base.R.layout.activity_upgrade_dialog);

        titleTextView = findViewById(com.zee.device.base.R.id.txt_title_progress);
        progressTextView = findViewById(com.zee.device.base.R.id.txt_value_progress);

        TextView positiveView = findViewById(com.zee.device.base.R.id.txt_positive_progress);
        positiveView.setOnClickListener(v -> {
            if (downloadBinder != null) {
                setTitleText("正在更新软件");
                downloadBinder.startDownload(BaseConstants.ZEE_HELP_APP_SOFTWARE_CODE);
            }
        });

        TextView cancelView = findViewById(com.zee.device.base.R.id.txt_cancel_progress);
        cancelView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
            }
        });

        positiveCancelLayout = findViewById(com.zee.device.base.R.id.ll_positive_cancel_progress);

        gradientProgressView = findViewById(com.zee.device.base.R.id.gradient_progress_view);

        setTitleText("正在更新软件");
        bindService();

//        Bundle bundle = getIntent().getBundleExtra(BaseConstants.EXTRA_UPGRADE_INFO);
//        UpgradeResp upgradeResp = (UpgradeResp) bundle.getSerializable(BaseConstants.EXTRA_UPGRADE_INFO);
//        handleUpgrade(upgradeResp);
    }

    /**
     * 绑定服务
     */
    private void bindService() {
        Intent bindIntent = new Intent(this.getApplicationContext(), DownloadService.class);
        bindService(bindIntent, serviceConnection, BIND_AUTO_CREATE);
    }

    @SuppressLint("DefaultLocale")
    public void setLoadingProgress(int progress) {
        if (gradientProgressView != null) {
            gradientProgressView.setProgress(progress);
            progressTextView.setText(String.format("%d/100", progress));
        }
    }

    public void setTitleText(String titleText) {
        if (titleTextView != null)
            titleTextView.setText(titleText);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        if ((downloadBinder != null) && (serviceConnection != null)) {
            downloadBinder.unRegisterDownloadListener(downloadListener);
            unbindService(serviceConnection);
        }
        super.onDestroy();
    }

    public void handleUpgrade(final UpgradeResp upgradeResp) {
        DownloadInfo downloadInfo = CareController.instance.getDownloadInfoByFileId(BaseConstants.ZEE_HELP_APP_SOFTWARE_CODE);
        if (downloadInfo != null) {
            if (downloadInfo.version.equals(upgradeResp.getSoftwareVersion())) {//mean already add
                if (downloadInfo.status == DownloadInfo.STATUS_SUCCESS) {
                    File file = new File(downloadInfo.filePath);
                    if (file.exists()) {
                        if (downloadInfo.packageMd5.equals(FileUtils.file2MD5(file))) {
                            setLoadingProgress(100);
                            startInstall();
                            finish();
                        } else {
                            if (file.delete() && CareController.instance.deleteDownloadInfo(downloadInfo.fileId) > 0) {
                                downloadBinder.startDownload(DownloadHelper.buildHostUpgradeDownloadInfo(this, upgradeResp));
                            } else {
                                showToast("删除损坏的升级包失败！");
                                finish();
                            }
                        }
                    } else {//something wrong?
                        downloadBinder.startDownload(downloadInfo);
                    }
                } else if (downloadInfo.status == DownloadInfo.STATUS_STOPPED) {
                    downloadBinder.startDownload(downloadInfo);
                }
            } else {//old version in db
                downloadBinder.startDownload(DownloadHelper.buildHostUpgradeDownloadInfo(this, upgradeResp));
            }
        } else {
            boolean download = downloadBinder.startDownload(DownloadHelper.buildHostUpgradeDownloadInfo(this, upgradeResp));
        }
    }

    public static void showUpgradeDialog(Context context, final UpgradeResp upgradeResp) {
        Intent intent = new Intent();
        intent.setClass(context, UpgradeDialogActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(BaseConstants.EXTRA_UPGRADE_INFO, upgradeResp);
        intent.putExtra(BaseConstants.EXTRA_UPGRADE_INFO, bundle);
        context.startActivity(intent);

    }

    private void installApk(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            ApkUtil.installApk(UpgradeDialogActivity.this, UpgradeDialogActivity.this.getPackageName() + ".fileProvider", file);
        }
    }

    public static void showUpgradeTipDialog(final Context context, final UpgradeResp upgradeResp) {
        final UpgradeTipDialog upgradeTipDialog = new UpgradeTipDialog(context);
        upgradeTipDialog.setTitleText("检测到新版本");
        upgradeTipDialog.setMessageText("V" + upgradeResp.getSoftwareVersion());
        upgradeTipDialog.showConfirmButton(upgradeResp.isForcible());
        upgradeTipDialog.setOnClickListener(new UpgradeTipDialog.OnClickListener() {
            @Override
            public void onConfirm(View v) {
                upgradeTipDialog.cancel();
                UpgradeDialogActivity.showUpgradeDialog(context, upgradeResp);
            }

            @Override
            public void onPositive(View v) {
                upgradeTipDialog.cancel();
                UpgradeDialogActivity.showUpgradeDialog(context, upgradeResp);
            }

            @Override
            public void onCancel(View v) {
                upgradeTipDialog.cancel();
            }
        });
        upgradeTipDialog.show();
    }
}