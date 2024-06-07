package com.zwn.launcher;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.zee.guide.data.protocol.response.ServicePkgInfoResp;
import com.zeewain.base.config.BaseConstants;
import com.zeewain.base.config.LogFileConfig;
import com.zeewain.base.config.SharePrefer;
import com.zeewain.base.data.protocol.response.BaseResp;
import com.zeewain.base.data.protocol.response.DeviceInfoResp;
import com.zeewain.base.model.LoadState;
import com.zeewain.base.ui.BaseViewModel;
import com.zeewain.base.utils.CareLog;
import com.zeewain.base.utils.CommonUtils;
import com.zeewain.base.utils.DateTimeUtils;
import com.zeewain.base.utils.FileUtils;
import com.zeewain.base.utils.SPUtils;
import com.zwn.launcher.data.DataRepository;
import com.zwn.launcher.data.model.UpgradeLoadState;
import com.zeewain.base.data.protocol.request.PublishReq;
import com.zwn.launcher.data.protocol.request.UploadLogReq;
import com.zeewain.base.data.protocol.request.UpgradeReq;
import com.zeewain.base.data.protocol.response.PublishResp;
import com.zwn.launcher.data.protocol.response.ThemeInfoResp;
import com.zeewain.base.data.protocol.response.UpgradeResp;
import com.zwn.lib_download.utils.LogHelper;

import java.io.File;
import java.util.Date;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.observers.ResourceObserver;
import io.reactivex.schedulers.Schedulers;


public class MainViewModel extends BaseViewModel {

    private static final String TAG = MainViewModel.class.getSimpleName();
    private final DataRepository dataRepository;
    public MutableLiveData<String> mldToastMsg = new MutableLiveData<>();
    public MutableLiveData<LoadState> mldHostAppUpgradeState = new MutableLiveData<>();
    public MutableLiveData<LoadState> mldManagerAppUpgradeState = new MutableLiveData<>();
    public MutableLiveData<UpgradeLoadState> mldCommonAppUpgradeState = new MutableLiveData<>();
    public MutableLiveData<LoadState> mldServicePackInfoLoadState = new MutableLiveData<>();
    public MutableLiveData<Boolean> mldDeviceUnActivated = new MutableLiveData<>();
    public MutableLiveData<LoadState> mldThemeInfoLoadState = new MutableLiveData<>();
    public MutableLiveData<LoadState> mldHostPluginPublishState = new MutableLiveData<>();
    public MutableLiveData<Long> mldTimeCheckState = new MutableLiveData<>();
    public UpgradeResp hostAppUpgradeResp;
    public UpgradeResp managerAppUpgradeResp;
    public PublishResp hostPluginPublishResp;
    public ThemeInfoResp themeInfoResp;
    public ServicePkgInfoResp servicePkgInfoResp;
    public boolean isFromCacheServicePkgInfoResp = false;
    public boolean isFromCacheManagerAppUpgrade = false;
    public boolean isFromCacheHostAppUpgrade = false;
    public boolean isFromCacheHostPluginPublish = false;
    public boolean isFromCacheThemeInfo = false;
    public long lastManagerAppUpgradeRespTime = 0;

    public MainViewModel(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    public void reqCommonAppUpgrade(String version, final String softwareCode) {
        mldCommonAppUpgradeState.setValue(new UpgradeLoadState(LoadState.Loading, softwareCode));
        UpgradeReq upgradeReq = new UpgradeReq(version, softwareCode);
        dataRepository.getUpgradeVersionInfo(upgradeReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<UpgradeResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<UpgradeResp> response) {
                        if(BaseConstants.API_HANDLE_SUCCESS == response.code) {
                            UpgradeResp commonAppUpgradeResp = response.data;
                            if (commonAppUpgradeResp != null) {
                                if (commonAppUpgradeResp.getVersionId() == null || commonAppUpgradeResp.getVersionId().isEmpty()) {
                                    commonAppUpgradeResp = null;
                                }
                            }
                            mldCommonAppUpgradeState.setValue(new UpgradeLoadState(LoadState.Success, softwareCode, commonAppUpgradeResp));
                        }else{
                            mldToastMsg.setValue(response.message);
                            mldCommonAppUpgradeState.setValue(new UpgradeLoadState(LoadState.Failed, softwareCode));
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mldCommonAppUpgradeState.setValue(new UpgradeLoadState(LoadState.Failed, softwareCode));
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void reqManagerAppUpgrade(String version) {
        mldManagerAppUpgradeState.setValue(LoadState.Loading);
        UpgradeReq upgradeReq = new UpgradeReq(version, BaseConstants.MANAGER_APP_SOFTWARE_CODE);
        dataRepository.getUpgradeVersionInfo(upgradeReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<UpgradeResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<UpgradeResp> response) {
                        isFromCacheManagerAppUpgrade = response.isCache;
                        if(BaseConstants.API_HANDLE_SUCCESS == response.code) {
                            managerAppUpgradeResp = response.data;
                            if (managerAppUpgradeResp != null) {
                                if (managerAppUpgradeResp.getVersionId() == null || managerAppUpgradeResp.getVersionId().isEmpty()) {
                                    managerAppUpgradeResp = null;
                                }
                            }
                            mldManagerAppUpgradeState.setValue(LoadState.Success);
                            if (!isFromCacheManagerAppUpgrade && response.localTime != null && !response.localTime.isEmpty()) {
                                Date localDateTime = DateTimeUtils.formatStringToDate(response.localTime, null);
                                if (localDateTime != null) {
                                    lastManagerAppUpgradeRespTime = localDateTime.getTime();
                                    mldTimeCheckState.setValue(localDateTime.getTime());
                                }
                            }
                        }else{
                            mldToastMsg.setValue(response.message);
                            mldManagerAppUpgradeState.setValue(LoadState.Failed);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mldManagerAppUpgradeState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void reqHostAppUpgrade(String version) {
        mldHostAppUpgradeState.setValue(LoadState.Loading);
        UpgradeReq upgradeReq = new UpgradeReq(version, BaseConstants.HOST_APP_SOFTWARE_CODE);
        dataRepository.getUpgradeVersionInfo(upgradeReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<UpgradeResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<UpgradeResp> response) {
                        isFromCacheHostAppUpgrade = response.isCache;
                        if(BaseConstants.API_HANDLE_SUCCESS == response.code) {
                            hostAppUpgradeResp = response.data;
                            if (hostAppUpgradeResp != null) {
                                if (hostAppUpgradeResp.getVersionId() == null || hostAppUpgradeResp.getVersionId().isEmpty()) {
                                    hostAppUpgradeResp = null;
                                }
                            }
                            mldHostAppUpgradeState.setValue(LoadState.Success);
                        }else{
                            mldToastMsg.setValue(response.message);
                            mldHostAppUpgradeState.setValue(LoadState.Failed);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mldHostAppUpgradeState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void reqUmsServicePackInfo(){
        mldServicePackInfoLoadState.setValue(LoadState.Loading);
        dataRepository.getUmsServicePackInfo(CommonUtils.getDeviceSn())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<ServicePkgInfoResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<ServicePkgInfoResp> response) {
                        isFromCacheServicePkgInfoResp = response.isCache;
                        if(response.code == BaseConstants.API_HANDLE_SUCCESS){
                            servicePkgInfoResp = response.data;
                            if(servicePkgInfoResp == null || servicePkgInfoResp.packId == 0 || servicePkgInfoResp.themeJson == null) {
                                reqDeviceInfo();
                            } else {
                                mldServicePackInfoLoadState.setValue(LoadState.Success);
                            }
                        }else{
                            mldToastMsg.setValue(response.message);
                            mldServicePackInfoLoadState.setValue(LoadState.Failed);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mldToastMsg.setValue("网络请求失败！");
                        mldServicePackInfoLoadState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void reqDeviceInfo() {
        dataRepository.getDeviceInfo(CommonUtils.getDeviceSn())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<DeviceInfoResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<DeviceInfoResp> resp) {
                        if (resp.code == BaseConstants.API_HANDLE_SUCCESS) {
                            if (resp.data != null) {
                                if (resp.data.activateStatus == 0) {
                                    mldDeviceUnActivated.setValue(true);
                                } else {
                                    mldServicePackInfoLoadState.setValue(LoadState.Failed);
                                    mldToastMsg.setValue("服务包包配置错误！");
                                }
                            } else {
                                mldServicePackInfoLoadState.setValue(LoadState.Failed);
                                mldToastMsg.setValue("设备信息查询失败！");
                            }
                        } else {
                            mldServicePackInfoLoadState.setValue(LoadState.Failed);
                            mldToastMsg.setValue(resp.message);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mldToastMsg.setValue("网络请求失败！");
                        mldServicePackInfoLoadState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void reqThemeInfo(){
        mldThemeInfoLoadState.setValue(LoadState.Loading);
        dataRepository.getThemeInfo(CommonUtils.getDeviceSn())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<ThemeInfoResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<ThemeInfoResp> response) {
                        isFromCacheThemeInfo = response.isCache;
                        if(BaseConstants.API_HANDLE_SUCCESS == response.code){
                            if(response.data != null && response.data.softwareCode != null
                                    && !response.data.softwareCode.isEmpty()) {
                                themeInfoResp = response.data;
                                mldThemeInfoLoadState.setValue(LoadState.Success);
                                if (!isFromCacheThemeInfo) {
                                    mldTimeCheckState.setValue(themeInfoResp.timestamp);
                                }
                            }else{
                                mldToastMsg.setValue("主题包配置错误！");
                                mldThemeInfoLoadState.setValue(LoadState.Failed);
                            }
                        }else{
                            mldToastMsg.setValue(response.message);
                            mldThemeInfoLoadState.setValue(LoadState.Failed);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mldToastMsg.setValue("网络请求失败！");
                        mldThemeInfoLoadState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void reqHostPluginPublishInfo(String softwareCode) {
        mldHostPluginPublishState.setValue(LoadState.Loading);
        dataRepository.getPublishedVersionInfo(new PublishReq(softwareCode))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new DisposableObserver<BaseResp<PublishResp>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<PublishResp> response) {
                        isFromCacheHostPluginPublish = response.isCache;
                        if(BaseConstants.API_HANDLE_SUCCESS == response.code){
                            hostPluginPublishResp = response.data;
                            if(hostPluginPublishResp == null || hostPluginPublishResp.getSoftwareInfo() == null
                                    || hostPluginPublishResp.getSoftwareInfo().getSoftwareCode() == null){
                                hostPluginPublishResp = null;
                                mldToastMsg.setValue("未配置主题应用！");
                                mldHostPluginPublishState.setValue(LoadState.Failed);
                            }else{
                                mldHostPluginPublishState.setValue(LoadState.Success);
                            }
                        }else{
                            mldToastMsg.setValue(response.message);
                            mldHostPluginPublishState.setValue(LoadState.Failed);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        mldToastMsg.setValue("网络请求失败！");
                        mldHostPluginPublishState.setValue(LoadState.Failed);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void reqUploadLog(String message, String packageName) {
        String time = DateTimeUtils.formatDateToString(new Date(), LogFileConfig.LOG_TIME_FORMAT);
        UploadLogReq uploadLogReq = new UploadLogReq(packageName, time, message, packageName);
        uploadLogReq.moduleName = CommonUtils.getCrashModuleInfo();
        dataRepository.uploadLog(uploadLogReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new ResourceObserver<BaseResp<String>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<String> resp) {}

                    @Override
                    public void onError(@NonNull Throwable e) {}

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void uploadLog(String time, String message) {
        UploadLogReq uploadLogReq = new UploadLogReq(message, time);
        uploadLogReq.moduleName = CommonUtils.getCrashModuleInfo();
        dataRepository.uploadLog(uploadLogReq)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this)
                .subscribe(new ResourceObserver<BaseResp<String>>() {
                    @Override
                    public void onNext(@NonNull BaseResp<String> resp) {
                        if (resp.code == 0) {
                            SPUtils.getInstance().put(SharePrefer.upLoadLogFile, "");
                        } else {
                            CareLog.e(TAG, resp.message);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        CareLog.e(TAG, "日志上传错误！");
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    public void checkCrashLog() {
        List<File> fileList = FileUtils.getFiles(LogFileConfig.directoryPath);
        if (fileList != null && fileList.size() > LogFileConfig.maxSize) {
            for (int i = fileList.size() - 1; i >= LogFileConfig.maxSize; i--) {
                String deleteFilePath = fileList.get(i).getAbsolutePath();
                boolean deleteResult = FileUtils.deleteFile(deleteFilePath);
                if (!deleteResult) {
                    CareLog.e(TAG, fileList.get(i).getAbsolutePath() + "删除失败");
                }
            }
        }

        String logFileName = SPUtils.getInstance().getString(SharePrefer.upLoadLogFile);
        if(CommonUtils.isDebugEnable()){
            CareLog.debug = true;
            LogHelper.debug = true;
        }

        if (logFileName == null || logFileName.isEmpty() || logFileName.equals("null")) {
            return;
        }

        String time = DateTimeUtils.formatDateToString(new Date(), LogFileConfig.LOG_TIME_FORMAT);
        String logPath = LogFileConfig.directoryPath + logFileName;
        String logMessage = FileUtils.readFile(logPath);
        if (logMessage != null) {
            uploadLog(time, "STATE=重启后\n" + logMessage);
        }
    }

}
