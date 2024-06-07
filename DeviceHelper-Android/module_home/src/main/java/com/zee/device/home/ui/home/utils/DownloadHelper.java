package com.zee.device.home.ui.home.utils;

import android.content.Context;


import com.zee.device.base.config.BaseConstants;
import com.zee.device.base.utils.CommonUtils;
import com.zee.device.home.data.protocol.response.UpgradeResp;
import com.zwn.lib_download.model.DownloadInfo;


public class DownloadHelper {

    public static DownloadInfo buildHostUpgradeDownloadInfo(Context context, UpgradeResp upgradeResp){
        DownloadInfo downloadInfo = new DownloadInfo();
        downloadInfo.fileId = BaseConstants.ZEE_HELP_APP_SOFTWARE_CODE;
        downloadInfo.fileName = "DeviceHelp";
        downloadInfo.fileImgUrl = "";
        downloadInfo.mainClassPath = BaseConstants.MAIN_PKG_CLASS_NAME;
        downloadInfo.url = upgradeResp.getPackageUrl();
        downloadInfo.version = upgradeResp.getSoftwareVersion();
        downloadInfo.type = BaseConstants.DownloadFileType.HOST_APP;
        downloadInfo.filePath = CommonUtils.getFileUsePath(downloadInfo.fileId, downloadInfo.version, downloadInfo.type, context);;
        downloadInfo.packageMd5 = upgradeResp.getPackageMd5();
        downloadInfo.extraId = "";
        downloadInfo.describe = "";
        return downloadInfo;
    }







}
