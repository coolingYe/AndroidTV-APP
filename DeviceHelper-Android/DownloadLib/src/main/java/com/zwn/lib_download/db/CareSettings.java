package com.zwn.lib_download.db;

import android.net.Uri;

public class CareSettings {

    public static final class DownloadInfo  {
        public static final Uri CONTENT_URI = Uri.parse("content://" +
                CareProvider.AUTHORITY + "/" + CareProvider.TABLE_DOWNLOAD_INFO );
        public static final String _ID = "_id";
        public static final String FILE_ID = "fileId";
        public static final String FILENAME = "fileName";
        public static final String FILE_IMG_URL = "fileImgUrl";
        public static final String MAIN_CLASS_PATH = "mainClassPath";
        public static final String URL = "url";
        public static final String FILE_SIZE = "fileSize";
        public static final String LOADED_SIZE = "loadedSize";
        public static final String FILE_PATH = "filePath";
        public static final String VERSION = "version";
        public static final String STATUS = "status";
        public static final String TYPE = "type";
        public static final String PACKAGE_MD5 = "packageMd5";
        public static final String RELY_IDS = "relyIds";
        public static final String EXTRA_ID = "extraId";
        public static final String EXTRA_ONE = "extraOne";
        public static final String EXTRA_TWO = "extraTwo";
        public static final String SAVE_TIME = "saveTime";
        public static final String DESC = "describe";

        public static final String[] DOWNLOAD_INFO_QUERY_COLUMNS = { _ID, FILE_ID, FILENAME, FILE_IMG_URL, MAIN_CLASS_PATH,
                URL, FILE_SIZE, LOADED_SIZE, FILE_PATH, VERSION, STATUS, TYPE, PACKAGE_MD5, RELY_IDS, EXTRA_ID, EXTRA_ONE, EXTRA_TWO, SAVE_TIME, DESC};
    }

    public static final class AppLibInfo  {
        public static final Uri CONTENT_URI = Uri.parse("content://" +
                CareProvider.AUTHORITY + "/" + CareProvider.TABLE_APP_LIB_INFO );
        public static final String _ID = "_id";
        public static final String FILE_ID = "fileId";
        public static final String PACKAGE_NAME = "packageName";
        public static final String LIB_PATH = "libPath";
        public static final String LIB_MD5 = "libMd5";
        public static final String STATUS = "status";
        public static final String SAVE_TIME = "saveTime";
        public static final String DESC = "describe";

        public static final String[] APP_LIB_INFO_QUERY_COLUMNS = { _ID, FILE_ID, PACKAGE_NAME, LIB_PATH, LIB_MD5,
                STATUS, SAVE_TIME, DESC};
    }

}
