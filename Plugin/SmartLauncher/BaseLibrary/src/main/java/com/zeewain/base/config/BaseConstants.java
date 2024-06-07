package com.zeewain.base.config;

import android.os.Environment;

public class BaseConstants {

    public static final String MAIN_PKG_CLASS_NAME = "com.zwn.launcher.MainActivity";
    public static final String LOGIN_PKG_CLASS_NAME = "com.zee.launcher.login.ui.LoginActivity";
    public static final String UPGRADE_PKG_CLASS_NAME = "com.zwn.launcher.ui.upgrade.UpgradeTipDialogActivity";
    public static final String GUIDE_PKG_CLASS_NAME = "com.zee.guide.ui.GuideActivity";

    public static final String PACKAGE_INSTALLED_ACTION = "plugin.install.SESSION_API_PACKAGE_INSTALLED";
    public static final String MANAGER_INSTALLED_ACTION = "manager.launcher.SESSION_API_PACKAGE_INSTALLED";
    public static final String SETTINGS_APP_RECEIVER_ACTION = "com.zee.setting.START_RECEIVER_ACTION";
    public static final String GESTURE_AI_SERVICE_CHECK_ACTION = "GESTURE_AI_SERVICE_CHECK";
    public static final String EXTRA_START_HOST_PLUGIN_COUNT = "StartHostPluginCount";
    public static final String EXTRA_INSTALLED_PACKAGE_NAME = "InstalledPackageName";
    public static final String EXTRA_INSTALLED_APP_TYPE = "InstalledAppType";
    public static final String EXTRA_REFRESH_DATA = "RefreshData";
    public static final String EXTRA_REGISTER = "Register";
    public static final String EXTRA_CLEAR_ALL_CACHE = "ClearAllCache";
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
    public static final String EXTRA_MANAGER_LOADING_TIP = "LoadingTip";
    public static final String EXTRA_MANAGER_ACTION = "ManagerAction";
    public static final String EXTRA_MANAGER_ACTION_PWD = "ManagerActionPwd";
    public static final String EXTRA_MANAGER_RUN_COMMAND = "RunCommand";
    public static final String EXTRA_MANAGER_RUN_COMMAND_RESULT = "RunCommandResult";
    public static final String DONE_TO_CLASS_NAME = MAIN_PKG_CLASS_NAME;

    public static final String EXTRA_APK_PATH = "ApkPath";
    public static final String EXTRA_APK_INSTALL_RESULT = "ApkInstallResult";

    public static final String AUTH_SYSTEM_CODE = "ZWN_AIIP_003";
    public static final String HOST_APP_SOFTWARE_CODE = "ZWN_SW_ANDROID_AIIP_005";
    public static final String MANAGER_APP_SOFTWARE_CODE = "ZWN_SW_ANDROID_AIIP_090";
    public static final String SETTINGS_APP_SOFTWARE_CODE = "ZWN_SW_ANDROID_AIIP_091";
    public static final String SETTINGS_APP_PACKAGE_NAME = "com.zee.setting";
    public static final String ZEE_GESTURE_AI_APP_SOFTWARE_CODE = "ZWN_SW_ANDROID_AIIP_092";
    public static final String ZEE_GESTURE_AI_APP_PACKAGE_NAME = "com.zeewain.ai";

    //used for third party app default enable all Permission
    public static final String PERSIST_SYS_PERMISSION_PKG = "persist.sys.zeewain.pkgs";

    public static final String PLUGIN_MODEL_PATH = Environment.getExternalStorageDirectory() + "/.system/models";
    public static final String PRIVATE_DATA_PATH = Environment.getExternalStorageDirectory() + "/.system";
    public static final String LICENSE_FILE_PATH = PRIVATE_DATA_PATH + "/zeewain";
    public static final String LICENSE_V2_FILE_PATH = PRIVATE_DATA_PATH + "/zeewainV2";

    public static final String MANAGER_PACKAGE_NAME = "com.zee.manager";
    public static final String MANAGER_MANAGER_ACTIVITY = "com.zee.manager.ManagerActivity";
    public static final String MANAGER_INSTALL_ACTIVITY = "com.zee.manager.InstallActivity";
    public static final String MANAGER_SERVICE_ACTION = "com.zee.manager.service";

    public static final String ZEE_SETTINGS_ACTIVITY_ACTION = "com.zee.setting.START_SETTINGS_ACTION";
    public static final String ZEE_SETTINGS_AGREEMENT_ACTIVITY_ACTION = "com.zee.setting.SHOW_AGREEMENT_ACTION";
    public static final String EXTRA_ZEE_SETTINGS_AGREEMENT_CODE = "agreementCode";

    public static final String PRIVATE_DATA_CARE_CODE = "970719cb674e0f9af2450bae18c19cb9";
    public static final String PRIVATE_DATA_DEBUG_CODE = "c74882ec2b890da7daf800837ace6756";
    public static final String PERSIST_DATA_DEBUG_STATUS = "persist.sys.zee.debug.status";
    public static final String PERSIST_DATA_STATUS_BAR = "persist.sys.zee.statusbar.pull";
    public static final String PERSIST_DATA_MENU_SETTINGS = "persist.sys.zee.menu.settings.pkg";
    public static final String PERSIST_DATA_DEVICE_SERIAL_NO = "persist.sys.zee.device.serialno";

    public static class AgreementCode{
        public static final String CODE_PRIVACY_AGREEMENT = "AISPACE_APP_LAW_PRIVACY_POLOICY";
        public static final String CODE_USER_AGREEMENT = "AISPACE_APP_NONAGE_PRIVACY_POLOICY";
    }

    /**
     * use for unity Courseware
     */
    public static String ACTION_CRASH_MSG = "com.zee.unity.CRASH_MSG_ACTION";
    public static final String EXTRA_CRASH_MSG = "CrashMsg";
    public static final String EXTRA_CRASH_PKG = "CrashPkg";

    public static final String EXTRA_SHOW_ACTION = "ShowAction";
    public static final String EXTRA_USE_REMOTE_CAMERA = "UseRemoteCamera";
    public static class ShowCode{
        public static int CODE_CAMERA_ERROR = 1;
        public static int CODE_CAMERA_INVALID = 2;
    }

    public static class DownloadFileType{
        public static final int MODEL_BIN = -2;
        public static final int SHARE_LIB = -1;
        public static final int HOST_APP = 0;
        public static final int PLUGIN_APP = 1;
        public static final int HOST_PLUGIN_PKG = 2;
        public static final int MANAGER_APP = 10;
        public static final int SETTINGS_APP = 11;
        public static final int ZEE_GESTURE_AI_APP = 12;
    }

    public static class DeviceStatus{
        public static final int ACTIVATED = 1;
        public static final int UNACTIVATED = 0;
    }

    public static class UserEventCode{
        public static final String PRESS_HOME_KEY_ON_UNITY = "PressHomeKeyOnUnity";
        public static final String MEM_EXCEEDS_LIMIT = "MemExceedsLimit";
    }

    public static class ApiPath{
        public static final String THEME_PACKAGE_INFO = "/dmsmgr/purchase/device/themePackage";     //cached
        public static final String UMS_SERVICE_PACKAGE_INFO = "/ums/device/servicePackage/info";    //cached
        public static final String SERVICE_PACKAGE_INFO = "/dmsmgr/purchase/device/servicePackage";
        public static final String SERVICE_PACKAGE_INFO_2 = "/dmsmgr/purchase/device/servicePackInfo"; //topic 101,900, 901, 002 use;
        public static final String DEVICE_HOLD_CHECK = "/ums/device/checkDevice";
        public static final String PURCHASE_DEVICE_INFO = "/dmsmgr/purchase/device/info";
        public static final String DEVICE_HEALTH = "/dmsmgr/purchase/device/health";    //t
        public static final String DEVICE_OFFLINE = "/dmsmgr/purchase/device/offline";  //t

        public static final String SW_VERSION_LATEST = "/software/version/latest-published";    //cached
        public static final String SW_VERSION_NEWER = "/software/version/newer-published";  //cached
        public static final String LOG_UPLOAD = "/logcollection/log/upload";

        public static final String APP_INSTALL_RECORD = "/ums/device/app/download/record";
        public static final String ADD_USER_EVENT_RECORD = "/app/user/event/record/add";
        public static final String DEVICE_STATUS_REPORT = "/ums/device/status/report";

        public static final String DEVICE_LOGIN_BINDING = "/ums/device/loginBinding";
        public static final String DEVICE_REGISTER_BINDING = "/ums/device/registerBinding";
        public static final String DEVICE_ACTIVATE = "/ums/device/activate";
        public static final String SSO_DO_LOGIN = "/operation/sso/doLogin";
        public static final String CAPTCHA_IMAGE = "/captcha/captcha/image";
        public static final String CAPTCHA_SMS_CODE = "/captcha/captcha/sms";
        public static final String CLIENT_GET_AK_SK = "/auth/client/get-ak-sk"; //t
        public static final String CLIENT_GET_USER_INFO = "/ums/user/info"; //t
    }

    public static final int API_HANDLE_SUCCESS = 0;

    public static final String baseUrl = EnConfig.BASE_URL;
    public static final String basePath = EnConfig.BASE_PATH;

}
