package com.zeewain.base.config;

import android.os.Environment;

public class BaseConstants {

    public static final String PACKAGE_INSTALLED_ACTION = "plugin.install.SESSION_API_PACKAGE_INSTALLED";
    public static final String MANAGER_INSTALLED_ACTION = "manager.launcher.SESSION_API_PACKAGE_INSTALLED";
    public static final String START_CACHE_MANAGER_ACTION = "START_CACHE_MANAGER_ACTION";
    public static final String EXTRA_HOME_PAGE_CACHE_INFO = "HomePageCacheInfo";
    public static final String EXTRA_REGISTER = "Register";
    public static final String EXTRA_UPGRADE_INFO = "UpgradeInfo";
    public static final String EXTRA_PLUGIN_NAME = "PluginName";
    public static final String EXTRA_PLUGIN_FILE_PATH = "PluginFilePath";
    public static final String EXTRA_AUTH_AK_CODE = "AuthAkCode";
    public static final String EXTRA_AUTH_SK_CODE = "AuthSkCode";
    public static final String EXTRA_SKU_NAME = "SkuName";
    public static final String EXTRA_SKU_URL = "SkuUrl";
    public static final String EXTRA_HOST_PKG = "HostPkg";
    public static final String EXTRA_AUTH_URI = "AuthUri";
    public static final String EXTRA_AUTH_TOKEN = "AuthToken";
    public static final String EXTRA_MODELS_DIR_PATH = "ModelsDirPath";
    public static final String EXTRA_LICENSE_PATH = "LicensePath";
    public static final String EXTRA_LICENSE_CONTENT = "LicenseContent";
    public static final String EXTRA_DONE_TO_PACKAGE_NAME = "DoneToPackageName";
    public static final String EXTRA_DONE_TO_CLASS_NAME = "DoneToClassName";
    public static final String DONE_TO_CLASS_NAME = "com.zwn.launcher.MainActivity";

    public static final String EXTRA_APK_PATH = "ApkPath";
    public static final String EXTRA_APK_INSTALL_RESULT = "ApkInstallResult";

    public static final String AUTH_SYSTEM_CODE = "ZWN_AIIP_003";
    public static final String HOST_APP_SOFTWARE_CODE = "ZWN_SW_ANDROID_AIIP_005";
    public static final String MANAGER_APP_SOFTWARE_CODE = "ZWN_SW_ANDROID_AIIP_090";

    //used for third party app default enable all Permission
    public static final String PERSIST_SYS_PERMISSION_PKG = "persist.sys.zeewain.pkgs";

    public static final String PLUGIN_MODEL_PATH = Environment.getExternalStorageDirectory() + "/.system/models";
    public static final String PRIVATE_DATA_PATH = Environment.getExternalStorageDirectory() + "/.system";
    public static final String LICENSE_FILE_PATH = PRIVATE_DATA_PATH + "/zeewain";
    public static final String LICENSE_V2_FILE_PATH = PRIVATE_DATA_PATH + "/zeewainV2";

    public static final String MANAGER_PACKAGE_NAME = "com.zee.manager";
    public static final String MANAGER_INSTALL_ACTIVITY = "com.zee.manager.InstallActivity";
    public static final String MANAGER_SERVICE_ACTION = "com.zee.manager.service";
    public static final String ZEE_SETTINGS_ACTIVITY_ACTION = "com.zee.setting.START_SETTINGS_ACTION";

    public static final String PRIVATE_DATA_DEBUG_CODE = "c74882ec2b890da7daf800837ace6756";
    public static final String PERSIST_DATA_DEBUG_STATUS = "persist.sys.zee.debug.status";

    /**
     * use for unity Courseware
     */
    public static final String EXTRA_SHOW_ACTION = "ShowAction";
    public static class ShowCode{
        public static int CODE_CAMERA_ERROR = 1;
    }

    public static class DownloadFileType{
        public static final int MODEL_BIN = -2;
        public static final int SHARE_LIB = -1;
        public static final int HOST_APP = 0;
        public static final int PLUGIN_APP = 1;
        public static final int MANAGER_APP = 10;
    }

    public static class ApiPath{
        public static final String SERVICE_PACKAGE_INFO = "/dmsmgr/purchase/device/servicePackage";
        public static final String PRODUCT_RECOMMEND_LIST = "/ums/client/product/cms/recommend";
        public static final String PRODUCT_ONLINE_QUERY_LIST = "/ums/client/product/cms/query/list";
        public static final String PRODUCT_DETAIL = "/ums/client/product/cms/detail";
        public static final String SW_VERSION_LATEST = "/software/version/latest-published";
        public static final String SW_VERSION_NEWER = "/software/version/newer-published";

        public static final String USER_FAVORITES_PAGE_LIST = "/ums/favorite/courseware/page";
        public static final String USER_FAVORITES_ITEM_INFO = "/ums/favorite/courseware/info";
        public static final String USER_PLAY_RECORD_LIST = "/ums/playHistory/v2/record";
    }


    public static class SpecialRespCode {
        public static final int FINISH_PAGE = 4000;
    }

    public static final int API_HANDLE_SUCCESS = 0;
    public static final String basePathHolder = "/basePathHolder";
    public static final String basePath = basePathHolder;
}
