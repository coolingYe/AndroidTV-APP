package com.zee.launcher.home.ui.loading;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.zee.launcher.home.R;
import com.zee.launcher.home.data.DataRepository;
import com.zeewain.base.BaseApplication;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.config.SharePrefer;
import com.zeewain.base.ui.BaseActivity;
import com.zeewain.base.utils.ApkUtil;
import com.zeewain.base.utils.DensityUtils;
import com.zeewain.base.utils.FileUtils;
import com.zeewain.base.utils.ZipUtils;
import com.zeewain.base.widgets.LoadingView;
import com.zwn.launcher.host.HostManager;
import com.zwn.lib_download.DownloadListener;
import com.zwn.lib_download.DownloadService;
import com.zwn.lib_download.db.CareController;
import com.zwn.lib_download.model.DownloadInfo;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class LoadingPluginActivity extends BaseActivity {

    private static final String TAG = "Zee";
    private static final int MSG_START_PLUGIN = 1;
    private String akCode;
    private String skCode;
    private String skuName;
    private String skuUrl;
    private DownloadInfo downloadInfo;
    private LoadingView loadingView;
    private long startTime = 0;
    private final MyHandler mHandler = new MyHandler(Looper.myLooper(), this);
    private final AtomicInteger pendingPrepareCount = new AtomicInteger();
    private static final List<File> unzipFiles = new ArrayList<>();
    private final List<String> modelFileList = new ArrayList<>();
    private final ExecutorService mFixedPool = Executors.newFixedThreadPool(2);
    private static final ConcurrentHashMap<String, String> downloadRelatedDataMap = new ConcurrentHashMap<>(5);
    private static volatile boolean isUnzipCalled = false;
    private static volatile boolean isPrepareStart = false;
    private int failedTryCount = 3;
    private LoadingViewModel loadingViewModel;


    MyBroadcastReceiver myBroadcastReceiver;

    private DownloadService.DownloadBinder downloadBinder;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            downloadBinder = (DownloadService.DownloadBinder) iBinder;
            if (downloadRelatedDataMap.size() > 0) {
                downloadBinder.registerDownloadListener(downloadListener);
                checkRelyDownloadData();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    private final DownloadListener downloadListener = new DownloadListener() {

        @Override
        public void onProgress(String fileId, int progress, long loadedSize, long fileSize) {

        }

        @Override
        public void onSuccess(String fileId, int type, File file) {
            if (downloadRelatedDataMap.containsKey(fileId)) {
                downloadRelatedDataMap.remove(fileId);
                checkToUnzip();
            }
        }

        @Override
        public void onFailed(String fileId, int type, int code) {
            if (downloadRelatedDataMap.containsKey(fileId)) {
                failedTryCount--;
                if (failedTryCount > 0) {
                    downloadBinder.startDownload(fileId);
                } else {
                    Log.e(TAG, "onFailed() " + fileId);
                    showToast("加载资源失败！");
                    finish();
                }
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
            //checkRelyDownloadData();
        }
    };

    void bindDownloadService() {
        /*Intent intent = new Intent();
        intent.setClass(this, DownloadService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);*/
        Intent bindIntent = new Intent();
        bindIntent.setComponent(new ComponentName(HostManager.getUseContext(this).getPackageName(), "com.zwn.lib_download.DownloadService"));
        HostManager.getUseContext(this).bindService(bindIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void registerBroadCast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BaseConstants.PACKAGE_INSTALLED_ACTION);
        myBroadcastReceiver = new MyBroadcastReceiver();
        registerReceiver(myBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DensityUtils.autoWidth(getApplication(), this);
        setContentView(R.layout.activity_loading_plugin);

        loadingView = findViewById(R.id.loadingView_loading);
        loadingView.setText("努力加载中...");
        loadingView.startAnim();

        String pluginName = getIntent().getStringExtra(BaseConstants.EXTRA_PLUGIN_NAME);
        akCode = getIntent().getStringExtra(BaseConstants.EXTRA_AUTH_AK_CODE);
        skCode = getIntent().getStringExtra(BaseConstants.EXTRA_AUTH_SK_CODE);
        skuName = getIntent().getStringExtra(BaseConstants.EXTRA_SKU_NAME);
        skuUrl = getIntent().getStringExtra(BaseConstants.EXTRA_SKU_URL);

        if (pluginName == null || pluginName.isEmpty()) {
            finish();
            return;
        }

        downloadInfo = CareController.instance.getDownloadInfoByFileId(pluginName);
        Log.i(TAG, "onCreate() downloadInfo " + downloadInfo);
        if (downloadInfo == null) {
            finish();
            return;
        }

        LoadingViewModelFactory factory = new LoadingViewModelFactory(DataRepository.getInstance());
        loadingViewModel = new ViewModelProvider(this, factory).get(LoadingViewModel.class);

        registerBroadCast();

        startTime = System.currentTimeMillis();
        isPrepareStart = false;
        isUnzipCalled = false;
        pendingPrepareCount.set(1);
        initDownloadRelatedDataMap(downloadInfo.relyIds);
        bindDownloadService();

        loadingView.postDelayed(new Runnable() {
            @Override
            public void run() {
                nextToDo(pluginName);
            }
        }, 500);
    }

    private void nextToDo(String pluginName) {
        File pluginFile = new File(downloadInfo.filePath);
        if (pluginName.equals(HostManager.getInstallingFileId())) {
            Log.i(TAG, "app installing");
        } else if (pluginFile.exists()) {//new version
            if (downloadInfo.packageMd5.equals(FileUtils.file2MD5(pluginFile))) {
                HostManager.installPlugin(downloadInfo.fileId);
            } else {
                pluginFile.delete();
                CareController.instance.deleteDownloadInfo(downloadInfo.fileId);
                Log.e(TAG, "app file damage！ packageName=" + downloadInfo.mainClassPath);
                showToast("应用文件损坏！");
                delayFinish();
            }
        } else {
            if (ApkUtil.isAppInstalled(this, downloadInfo.mainClassPath)) {
                prepareStartPlugin();
            } else {
                Log.e(TAG, "app file not exists and app not installed");
                showToast("应用不存在！");
                CareController.instance.deleteDownloadInfo(downloadInfo.fileId);
                delayFinish();
            }
        }
    }

    private void delayFinish() {
        loadingView.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 500);
    }

    private void initDownloadRelatedDataMap(String relyIds) {
        if (relyIds != null && !relyIds.isEmpty()) {
            String[] relyIdArray = relyIds.split(",");
            for (String relyId : relyIdArray) {
                DownloadInfo downloadLib = CareController.instance.getDownloadInfoByFileId(relyId);
                if (downloadLib == null) {
                    Log.e(TAG, "downloadLib null, relyId=" + relyId);
                    finish();
                    return;
                } else {
                    String relyModelIds = downloadLib.relyIds;
                    if (relyModelIds != null && !relyModelIds.isEmpty()) {
                        String[] relyModelIdArray = relyModelIds.split(",");
                        for (String relyModelId : relyModelIdArray) {
                            DownloadInfo downloadModel = CareController.instance.getDownloadInfoByFileId(relyModelId);
                            if (downloadModel == null) {
                                Log.e(TAG, "downloadModel null, relyModelId=" + relyModelId);
                                finish();
                                return;
                            } else {
                                modelFileList.add(downloadModel.filePath);
                                if (downloadModel.status != DownloadInfo.STATUS_SUCCESS) {
                                    downloadRelatedDataMap.put(downloadModel.fileId, downloadModel.fileId);
                                }
                            }
                        }
                    }
                    if (downloadLib.status != DownloadInfo.STATUS_SUCCESS) {
                        downloadRelatedDataMap.put(downloadLib.fileId, downloadLib.fileId);
                    }
                }
            }
        }
    }

    private void checkRelyDownloadData() {
        Iterator<Map.Entry<String, String>> iterator = downloadRelatedDataMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            String fileId = entry.getKey();
            DownloadInfo downloadLib = CareController.instance.getDownloadInfoByFileId(fileId);
            if (downloadLib.status == DownloadInfo.STATUS_SUCCESS) {
                iterator.remove();
            } else if (downloadLib.status == DownloadInfo.STATUS_STOPPED) {
                downloadBinder.startDownload(fileId);
            }
        }
        checkToUnzip();
    }

    private void checkToUnzip() {
        if (downloadRelatedDataMap.size() == 0 && isPrepareStart) {
            unzipShareLib();
        }
    }

    private void prepareStartPlugin() {
        isPrepareStart = true;
        loadingViewModel.reqStartCourseware(downloadInfo.extraId, skuName, skuUrl);
        checkToUnzip();
    }

    private synchronized void unzipShareLib() {
        if (!isUnzipCalled) {
            isUnzipCalled = true;
            mFixedPool.execute(() -> {
                try {
                    long currentTime = System.currentTimeMillis();
                    PackageManager packageManager = getPackageManager();
                    PackageInfo packageInfo = packageManager.getPackageInfo(downloadInfo.mainClassPath, 0);

                    String desDir = BaseConstants.PLUGIN_MODEL_PATH + "/";

                    if (modelFileList.size() > 0) {
                        boolean copyModelSuccess = FileUtils.copyFilesTo(modelFileList, desDir);
                        if (!copyModelSuccess) {
                            finish();
                            //showToast("拷贝模型失败！");
                            Log.e(TAG, "copyModel() failed !");
                            return;
                        }
                    }
                    Log.i(TAG, "copyModel() cost time=" + (System.currentTimeMillis() - currentTime));


                    /*String [] fileName = new File(packageInfo.applicationInfo.nativeLibraryDir).list();
                    if(fileName != null) {
                        for (String s : fileName) {
                            Log.i(TAG, "pluginInfo.getNativeLibsDir() file=" + s);
                        }
                        if(fileName.length <= 3){
                            lastUnzipDonePlugin = null;
                        }
                    }*/

                    if (!downloadInfo.fileId.equals(HostManager.getLastUnzipDonePlugin())) {
                        Log.e(TAG, "unzipShareLib() start==>");
                        HostManager.setLastUnzipDonePlugin(null);
                        if (unzipFiles.size() > 0) {
                            for (int i = 0; i < unzipFiles.size(); i++) {
                                File file = unzipFiles.get(i);
                                if (file.exists()) {
                                    boolean delResult = file.delete();
                                    if (!delResult) Log.e(TAG, "file.delete err " + file.getPath());
                                }
                            }
                        }
                        unzipFiles.clear();
                        if (downloadInfo.relyIds != null && !downloadInfo.relyIds.isEmpty()) {
                            String[] relyIdArray = downloadInfo.relyIds.split(",");
                            for (String relyId : relyIdArray) {
                                DownloadInfo downloadLib = CareController.instance.getDownloadInfoByFileId(relyId);
                                if (downloadLib.status == DownloadInfo.STATUS_SUCCESS) {
                                    unzipFiles.addAll(ZipUtils.unzipFile(downloadLib.filePath, packageInfo.applicationInfo.nativeLibraryDir));
                                } else {
                                    Log.e(TAG, "something wrong!!! unzipShareLib() downloadLib not ready " + downloadLib);
                                }
                            }
                        }
                        HostManager.setLastUnzipDonePlugin(downloadInfo.fileId);
                        Log.e(TAG, "unzipShareLib() cost time=" + (System.currentTimeMillis() - currentTime) + "<<<<-done-");
                    } else {
                        Log.e(TAG, "no need unzipShareLib() <-----");
                    }


                    /*for(int i=0; i<unzipFiles.size(); i++){
                        Log.i(TAG, "unzipShareLib file=" + unzipFiles.get(i).getPath());
                    }*/
                } catch (Exception exception) {
                    exception.printStackTrace();
                    Log.e(TAG, "unzipShareLib() failed! " + exception);
                } finally {
                    isUnzipCalled = false;
                    decrementCountAndCheck();
                }
            });
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (isUnzipCalled) {
                return !isUnzipCalled;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mHandler.removeMessages(MSG_START_PLUGIN);
    }

    private void decrementCountAndCheck() {
        int newPendingCount = pendingPrepareCount.decrementAndGet();
        if (newPendingCount <= 0) {
            Log.i(TAG, "decrementCountAndCheck() Done");
            sendMsgToStartPlugin();
        }
    }

    private void sendMsgToStartPlugin() {
        mHandler.sendEmptyMessageDelayed(MSG_START_PLUGIN, 100);
    }

    private void startPluginActivity() {
        doStartApplicationWithPackageName(downloadInfo.mainClassPath);
    }

    private boolean isUseAuthV2() {
        Log.i(TAG, "isUseAuthV2()" + downloadInfo.relyIds);
        String relyIds = downloadInfo.relyIds;
        if (relyIds != null && !relyIds.isEmpty()) {
            List<DownloadInfo> downloadList = CareController.instance.getAllDownloadInfo("fileId in (" + relyIds + ") and type=" + BaseConstants.DownloadFileType.SHARE_LIB);
            for (DownloadInfo downloadInfo : downloadList) {
                Log.i(TAG, "rely downloadLib ==>" + downloadInfo);
                if (downloadInfo.version.compareToIgnoreCase("0.6.7") > 0) {
                    Log.i(TAG, "rely downloadLib is over 0.6.7");
                    return true;
                }
            }
        }
        return false;
    }

    private void doStartApplicationWithPackageName(String packageName) {
        PackageInfo packageinfo = null;
        try {
            packageinfo = getPackageManager().getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageinfo == null) {
            Log.e(TAG, "getPackageInfo() " + packageName + ", null");
            showToast("获取应用信息失败！");
            finish();
            return;
        }

        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        //resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(packageinfo.packageName);

        // 通过getPackageManager()的queryIntentActivities方法遍历
        List<ResolveInfo> resolveInfoList = getPackageManager().queryIntentActivities(resolveIntent, 0);
        if (resolveInfoList == null || resolveInfoList.size() == 0) {
            showToast("获取应用入口失败！");
            finish();
            return;
        }
        ResolveInfo resolveInfo = resolveInfoList.iterator().next();
        if (resolveInfo != null) {
            //String packageName = resolveinfo.activityInfo.packageName;
            // 这个就是我们要找的该APP的LAUNCHER的Activity[组织形式：packageName.mainActivityName]
            String className = resolveInfo.activityInfo.name;

            Intent intent = new Intent(Intent.ACTION_MAIN);
            //intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.putExtra(BaseConstants.EXTRA_AUTH_AK_CODE, akCode);
            intent.putExtra(BaseConstants.EXTRA_AUTH_SK_CODE, skCode);
            intent.putExtra(BaseConstants.EXTRA_HOST_PKG, packageName);
            if (isUseAuthV2()) {
                intent.putExtra(BaseConstants.EXTRA_AUTH_URI, (BaseApplication.baseUrl + BaseApplication.basePath + "/auth"));
                intent.putExtra(BaseConstants.EXTRA_LICENSE_PATH, BaseConstants.LICENSE_V2_FILE_PATH);
            } else {
                intent.putExtra(BaseConstants.EXTRA_AUTH_URI, (BaseApplication.baseUrl + BaseApplication.basePath + "/auth/client/get-license"));
                intent.putExtra(BaseConstants.EXTRA_LICENSE_PATH, BaseConstants.LICENSE_FILE_PATH);
            }
            intent.putExtra(BaseConstants.EXTRA_MODELS_DIR_PATH, BaseConstants.PLUGIN_MODEL_PATH + "/");

            String userToken = HostManager.getHostSpString(SharePrefer.userToken, "");
            intent.putExtra(BaseConstants.EXTRA_AUTH_TOKEN, userToken);

            HostManager.setLastPluginPackageName(packageName);
            if (HostManager.isGestureAiEnable()) {
                HostManager.stopGestureAi();
            }

            ComponentName cn = new ComponentName(packageName, className);
            intent.setComponent(cn);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            HostManager.getUseContext(this).startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        loadingView.stopAnim();
        if (downloadBinder != null) {
            downloadBinder.unRegisterDownloadListener(downloadListener);
            HostManager.getUseContext(this).unbindService(serviceConnection);
        }
        if (myBroadcastReceiver != null) {
            unregisterReceiver(myBroadcastReceiver);
        }
        super.onDestroy();
    }

    class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive() intent=" + intent);
            Bundle extras = intent.getExtras();
            if (BaseConstants.PACKAGE_INSTALLED_ACTION.equals(intent.getAction())) {
                int status = extras.getInt(PackageInstaller.EXTRA_STATUS);
                String message = extras.getString(PackageInstaller.EXTRA_STATUS_MESSAGE);
                String pluginName = extras.getString(BaseConstants.EXTRA_PLUGIN_NAME);
                Log.i(TAG, "PACKAGE_INSTALLED_ACTION status=" + status + ", message=" + message + ", pluginName=" + pluginName);
                if (downloadInfo.fileId.equals(pluginName)) {
                    switch (status) {
                        case PackageInstaller.STATUS_PENDING_USER_ACTION:
                            // This test app isn't privileged, so the user has to confirm the install.
                            Intent confirmIntent = (Intent) extras.get(Intent.EXTRA_INTENT);
                            startActivity(confirmIntent);
                            break;

                        case PackageInstaller.STATUS_SUCCESS:
                            prepareStartPlugin();
                            break;

                        case PackageInstaller.STATUS_FAILURE:
                        case PackageInstaller.STATUS_FAILURE_ABORTED:
                        case PackageInstaller.STATUS_FAILURE_BLOCKED:
                        case PackageInstaller.STATUS_FAILURE_CONFLICT:
                        case PackageInstaller.STATUS_FAILURE_INCOMPATIBLE:
                        case PackageInstaller.STATUS_FAILURE_INVALID:
                        case PackageInstaller.STATUS_FAILURE_STORAGE:
                            showToast("安装应用失败！ 状态码：" + status);
                            finish();
                            break;
                        default:
                            showToast("安装应用失败！ 未知状态码：" + status);
                            finish();
                    }
                }
            }
        }
    }

    public static class MyHandler extends Handler {
        private final WeakReference<Activity> mActivity;

        public MyHandler(Looper looper, Activity activity) {
            super(looper);
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Activity activity = mActivity.get();
            if (activity != null) {
                if (msg.what == MSG_START_PLUGIN) {
                    ((LoadingPluginActivity) activity).startPluginActivity();
                }
            }
        }
    }
}