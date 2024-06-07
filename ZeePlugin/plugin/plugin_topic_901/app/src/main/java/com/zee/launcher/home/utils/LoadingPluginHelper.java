package com.zee.launcher.home.utils;

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
import com.zee.launcher.home.ui.direct.DirectLoadingActivity;
import com.zee.launcher.home.ui.loading.LoadingPluginActivity;
import com.zee.launcher.home.ui.loading.LoadingViewModel;
import com.zee.launcher.home.ui.loading.LoadingViewModelFactory;
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

public class LoadingPluginHelper {
    private static final String TAG = "LoadingPluginHelper";
    private String akCode;
    private String skCode;
    private String skuName;
    private String skuUrl;
    private String pluginName;
    private DownloadInfo downloadInfo;
    private LoadingView loadingView;
    private Handler handler;
    private long startTime = 0;
    private BaseActivity mContext;
    private final AtomicInteger pendingPrepareCount = new AtomicInteger();
    private static final List<File> unzipFiles = new ArrayList<>();
    private final List<String> modelFileList = new ArrayList<>();
    private final ExecutorService mFixedPool = Executors.newFixedThreadPool(2);
    private static final ConcurrentHashMap<String, String> downloadRelatedDataMap = new ConcurrentHashMap<>(5);
    public static volatile boolean isUnzipCalled = false;
    public static volatile boolean isPrepareStart = false;
    private int failedTryCount = 3;
    private LoadingViewModel loadingViewModel;


    MyBroadcastReceiver myBroadcastReceiver;

    private DownloadService.DownloadBinder downloadBinder;

    public LoadingPluginHelper(String akCode, String skCode, String skuName, String skuUrl, String pluginName, Handler handler, BaseActivity context) {
        this.akCode = akCode;
        this.skCode = skCode;
        this.skuName = skuName;
        this.skuUrl = skuUrl;
        this.pluginName = pluginName;
        this.handler = handler;
        this.mContext = context;
    }


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
                    mContext.showToast("加载资源失败！");
                    mContext.finish();
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
        bindIntent.setComponent(new ComponentName(HostManager.getUseContext(mContext).getPackageName(), DownloadService.class.getName()));
        HostManager.getUseContext(mContext).bindService(bindIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void registerBroadCast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BaseConstants.PACKAGE_INSTALLED_ACTION);
        myBroadcastReceiver = new MyBroadcastReceiver();
        mContext.registerReceiver(myBroadcastReceiver, intentFilter);
    }

    public void startLoad() {
        handler.sendEmptyMessage(DirectLoadingActivity.MSG_LOADING_OPEN);
        if (pluginName == null || pluginName.isEmpty()) {
            mContext.finish();
            return;
        }

        downloadInfo = CareController.instance.getDownloadInfoByFileId(pluginName);
        Log.i(TAG, "onCreate() downloadInfo " + downloadInfo);
        if (downloadInfo == null) {
            mContext.finish();
            return;
        }

        LoadingViewModelFactory factory = new LoadingViewModelFactory(DataRepository.getInstance());
        loadingViewModel = new ViewModelProvider(mContext, factory).get(LoadingViewModel.class);

        registerBroadCast();

        startTime = System.currentTimeMillis();
        isPrepareStart = false;
        isUnzipCalled = false;
        pendingPrepareCount.set(1);
        initDownloadRelatedDataMap(downloadInfo.relyIds);
        bindDownloadService();
        handler.postDelayed(new Runnable() {
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
                mContext.showToast("应用文件损坏！");
                delayFinish();
            }
        } else {
            if (ApkUtil.isAppInstalled(mContext, downloadInfo.mainClassPath)) {
                Log.e(TAG, "isAppInstalled" + downloadInfo.mainClassPath);
                prepareStartPlugin();
            } else {
                Log.e(TAG, "app file not exists and app not installed");
                mContext.showToast("应用不存在！");
                CareController.instance.deleteDownloadInfo(downloadInfo.fileId);
                delayFinish();
            }
        }
    }

    private void delayFinish() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mContext.finish();
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
                    mContext.finish();
                    return;
                } else {
                    String relyModelIds = downloadLib.relyIds;
                    if (relyModelIds != null && !relyModelIds.isEmpty()) {
                        String[] relyModelIdArray = relyModelIds.split(",");
                        for (String relyModelId : relyModelIdArray) {
                            DownloadInfo downloadModel = CareController.instance.getDownloadInfoByFileId(relyModelId);
                            if (downloadModel == null) {
                                Log.e(TAG, "downloadModel null, relyModelId=" + relyModelId);
                                mContext.finish();
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
                    PackageManager packageManager = mContext.getPackageManager();
                    PackageInfo packageInfo = packageManager.getPackageInfo(downloadInfo.mainClassPath, PackageManager.GET_PERMISSIONS);

                    String desDir = BaseConstants.PLUGIN_MODEL_PATH + "/";

                    if (modelFileList.size() > 0) {
                        boolean copyModelSuccess = FileUtils.copyFilesTo(modelFileList, desDir);
                        if (!copyModelSuccess) {
                            mContext.finish();
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


    private void decrementCountAndCheck() {
        int newPendingCount = pendingPrepareCount.decrementAndGet();
        if (newPendingCount <= 0) {
            Log.i(TAG, "decrementCountAndCheck() Done");
            sendMsgToStartPlugin();
        }
    }

    private void sendMsgToStartPlugin() {
        handler.sendEmptyMessageDelayed(DirectLoadingActivity.MSG_START_PLUGIN, 100);
    }

    public void startPluginActivity() {
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
            packageinfo = mContext.getPackageManager().getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageinfo == null) {
            Log.e(TAG, "getPackageInfo() " + packageName + ", null");
            mContext.showToast("获取应用信息失败！");
            mContext.finish();
            return;
        }

        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        //resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(packageinfo.packageName);

        // 通过getPackageManager()的queryIntentActivities方法遍历
        List<ResolveInfo> resolveInfoList = mContext.getPackageManager().queryIntentActivities(resolveIntent, 0);
        if (resolveInfoList == null || resolveInfoList.size() == 0) {
            mContext.showToast("获取应用入口失败！");
            mContext.finish();
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
            if (!mContext.isFinishing()&&!mContext.isDestroyed()&&((DirectLoadingActivity)mContext).isWindowOnFocus)
            {
                HostManager.getUseContext(mContext).startActivity(intent);
                handler.sendEmptyMessage(DirectLoadingActivity.MSG_LOADING_CLOSE);
            }
        }
    }


    public void onDestroy() {
        if (downloadBinder != null) {
            downloadBinder.unRegisterDownloadListener(downloadListener);
            HostManager.getUseContext(mContext).unbindService(serviceConnection);
        }
        if (myBroadcastReceiver != null) {
            mContext.unregisterReceiver(myBroadcastReceiver);
        }
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
                            mContext.startActivity(confirmIntent);
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
                            mContext.showToast("安装应用失败！ 状态码：" + status);
                            mContext.finish();
                            break;
                        default:
                            mContext.showToast("安装应用失败！ 未知状态码：" + status);
                            mContext.finish();
                    }
                }
            }
        }
    }


}
