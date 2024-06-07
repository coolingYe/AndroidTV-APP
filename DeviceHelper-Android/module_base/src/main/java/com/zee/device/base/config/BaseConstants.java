package com.zee.device.base.config;


public class BaseConstants {

    public static final String QRCODE_CONTENT_PREFIX = "ZeeDev";
    public static final int WEBRTC_SIGNAL_SERVER_PORT = 36901;

    public static final String EXTRA_DEVICE_INFO = "DeviceInfo";

    public static final String EXTRA_UPGRADE_INFO = "UpgradeInfo";

    public static final String ACTION_EXIT_WEBRTC_PREVIEW = "ExitWebrtcPreviewAction";

    public static final String MAIN_PKG_CLASS_NAME = "com.zee.device.helper.ui.main.MainActivity";
    public static final String CALL_PKG_CLASS_NAME = "com.zee.device.helper.ui.call.CallShowActivity";

    public static final String ZEE_HELP_APP_SOFTWARE_CODE = "ZWN_SW_ANDROID_AIIP_020";

    public static final int VIDEO_RESOLUTION_WIDTH_1080 = 1080;
    public static final int VIDEO_RESOLUTION_HEIGHT_1920 = 1920;

    public static final int VIDEO_RESOLUTION_WIDTH_720 = 720;
    public static final int VIDEO_RESOLUTION_HEIGHT_1280 = 1280;

    public static final int VIDEO_RESOLUTION_WIDTH_360 = 360;
    public static final int VIDEO_RESOLUTION_HEIGHT_600 = 600;

    public static final int VIDEO_FPS_DEFAULT = 30;

    public static final int CAMERA_WIDTH = 420;
    public static final int CAMERA_HEIGHT = 600;

    public static class DownloadFileType{
        public static final int HOST_APP = 0;
    }

    public static class ApiPath{
        public static final String SW_VERSION_NEWER = "/software/version/newer-published";  //cached
        public static final String USER_ACTION_RECORD = "/app/mobile/link/record/add";
    }

    public static final int API_HANDLE_SUCCESS = 0;

    public static final String baseUrl = EnConfig.BASE_URL;
    public static final String basePath = EnConfig.BASE_PATH;

}
