package com.zee.setting.base;

import android.os.Environment;

public class BaseConstants {


    public static final String AUTH_SYSTEM_CODE = "ZWN_AIIP_001";
    public static final String HOST_APP_SOFTWARE_CODE = "ZWN_SW_ANDROID_AIIP_003";
    //  public static final String HOST_APP_SOFTWARE_CODE = "ZWN_SW_ANDROID_NATIVE_001";
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
    public static final String ZEE_UPDATE_ACTIVITY_ACTION = "com.zee.launcher.START_UPGRADE_ACTION";
    public static final String ZEE_PACKAGE_NAME = "com.zee.launcher";
    public static final String ZEE_PRIVACY_AGREEMENT = "AISPACE_APP_LAW_PRIVACY_POLOICY";
    public static final String ZEE_USER_AGREEMENT = "AISPACE_APP_NONAGE_PRIVACY_POLOICY";
    public static final String ZEE_GESTURE_PACKAGE_NAME = "com.zeewain.ai";
    public static final String ZEE_SETTINGS_UPDATE = "com.zee.setting.update";
    public static final String ZEE_SETTINGS_CAMERA_STATE = "com.zee.setting.CAMERA_STATE";

    public static final String EXTRA_CAMERA_EVENT = "CameraEvent";

    public static final String SP_KEY_TIMER_PLAN = "sp_key_timer_plan";
    public static final String SP_KEY_TIMER_PLAN_ENABLE = "sp_key_timer_plan_enable";

    public static final String SP_KEY_CAMERA_UPDATE_STATE = "persist.sys.zee.camera.update.state";
    public static final String SP_KEY_CAMERA_FUNCTION_SWITCH = "persist.sys.zee.camera.function.switch";
    public static final String SP_KEY_CAMERA_QR_URL = "sp_key_camera_qr_url";
    public static final String ZEE_LAUNCHER_PLUGIN_PLAY = "com.zee.launcher.PLUGIN_PLAY";


    /**
     * use for unity Courseware
     */
    public static final String EXTRA_SHOW_ACTION = "ShowAction";

    public static class ShowCode {
        public static int CODE_CAMERA_ERROR = 1;
    }

    public static class DownloadFileType {
        public static final int MODEL_BIN = -2;
        public static final int SHARE_LIB = -1;
        public static final int HOST_APP = 0;
        public static final int PLUGIN_APP = 1;
        public static final int MANAGER_APP = 10;
    }

    public static class ApiPath {
        public static final String SERVICE_PACKAGE_INFO = "/dmsmgr/purchase/device/servicePackInfo";
        public static final String PRODUCT_RECOMMEND_LIST = "/dmsmgr/purchase/device/recommend";
        public static final String PRODUCT_ONLINE_QUERY_LIST = "/product/online/query/list";
        public static final String PRODUCT_DETAIL = "/product/online/detail";
        public static final String SW_VERSION_LATEST = "/software/version/latest-published";
        public static final String SW_VERSION_NEWER = "/software/version/newer-published";

        public static final String USER_FAVORITES_PAGE_LIST = "/ums/favorite/courseware/page";
        public static final String USER_FAVORITES_ITEM_INFO = "/ums/favorite/courseware/info";
        public static final String USER_PLAY_RECORD_LIST = "/ums/playHistory/record";
        public static final String APP_AGREEMENT = "/website/agreement/info";
    }

    public static final int API_HANDLE_SUCCESS = 0;

    //正式环境&测试环境使用
    public static final boolean buildRelease = true;
    public static final String baseUrl = buildRelease ? "https://aiip.zeewain.com" : "https://test.local.zeewain.com";
    public static final String basePath = "/api";

    //开发环境使用
//    public static final String baseUrl = "https://dev.local.zeewain.com";
//    public static final String basePath = "/api";

}
