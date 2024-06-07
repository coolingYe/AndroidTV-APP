package com.zee.device.base.db;

import android.net.Uri;

public class DatabaseSettings {

    public static final class DeviceInfo  {
        public static final Uri CONTENT_URI = Uri.parse("content://" +
                DatabaseProvider.AUTHORITY + "/" + DatabaseProvider.TABLE_DEVICE_INFO );
        public static final String _ID = "_id";
        public static final String SN = "sn";
        public static final String MAC = "mac";
        public static final String NAME = "name";
        public static final String IP = "ip";
        public static final String PORT = "port";
        public static final String EXTRA_ONE = "extraOne";
        public static final String EXTRA_TWO = "extraTwo";
        public static final String SAVE_TIME = "saveTime";
        public static final String DESC = "describe";

        public static final String[] DEVICE_INFO_QUERY_COLUMNS = { _ID, SN, MAC, NAME, IP, PORT, EXTRA_ONE, EXTRA_TWO, SAVE_TIME, DESC};
    }

}
