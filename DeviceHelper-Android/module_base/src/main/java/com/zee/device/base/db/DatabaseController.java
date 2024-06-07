package com.zee.device.base.db;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;


import com.zee.device.base.model.DeviceInfo;

import java.util.ArrayList;
import java.util.List;

public class DatabaseController {
    private final Context context;
    public static volatile DatabaseController instance;

    public static void init(Context context){
        if(instance == null){
            synchronized (DatabaseController.class){
                if(instance == null){
                    instance = new DatabaseController(context);
                }
            }
        }
    }

    private DatabaseController(Context context){
        this.context = context;
    }

    public boolean addDeviceInfo(DeviceInfo deviceInfo) {
        ContentValues contentValues = new ContentValues(7);
        contentValues.put(DatabaseSettings.DeviceInfo.SN, deviceInfo.sn);
        contentValues.put(DatabaseSettings.DeviceInfo.MAC, deviceInfo.mac);
        contentValues.put(DatabaseSettings.DeviceInfo.NAME, deviceInfo.name);
        contentValues.put(DatabaseSettings.DeviceInfo.IP, deviceInfo.ip);
        contentValues.put(DatabaseSettings.DeviceInfo.PORT, deviceInfo.port);
        contentValues.put(DatabaseSettings.DeviceInfo.SAVE_TIME, deviceInfo.saveTime);
        contentValues.put(DatabaseSettings.DeviceInfo.DESC, deviceInfo.describe);

        Uri uri = context.getContentResolver().insert(DatabaseSettings.DeviceInfo.CONTENT_URI, contentValues);
        if (uri.getPathSegments().size() == 2){
            long rowId = ContentUris.parseId(uri);
            if(rowId > 0) return true;
        }
        return false;
    }

    public List<DeviceInfo> getAllDeviceInfo(String selection){
        Cursor cursor = context.getContentResolver().query(DatabaseSettings.DeviceInfo.CONTENT_URI,
                DatabaseSettings.DeviceInfo.DEVICE_INFO_QUERY_COLUMNS, selection, null, DatabaseSettings.DeviceInfo.SAVE_TIME + " DESC");
        List<DeviceInfo> dataList = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()){
                dataList.add(new DeviceInfo(cursor));
            }
            cursor.close();
        }

        return dataList;
    }

    public Cursor getDeviceInfo(String selection){
        return context.getContentResolver().query(DatabaseSettings.DeviceInfo.CONTENT_URI,
                DatabaseSettings.DeviceInfo.DEVICE_INFO_QUERY_COLUMNS, selection, null, DatabaseSettings.DeviceInfo.SAVE_TIME + " DESC");
    }

    public DeviceInfo getDeviceInfoBySN(String sn){
        DeviceInfo deviceInfo = null;
        Cursor cursor =  context.getContentResolver().query(DatabaseSettings.DeviceInfo.CONTENT_URI,
                DatabaseSettings.DeviceInfo.DEVICE_INFO_QUERY_COLUMNS, "sn='" + sn + "'", null, null);
        if (cursor != null) {
            if(cursor.moveToFirst()) {
                deviceInfo = new DeviceInfo(cursor);
            }
            cursor.close();
        }
        return deviceInfo;
    }

    public int updateDeviceInfo(DeviceInfo deviceInfo){
        ContentValues contentValues = new ContentValues(5);
        contentValues.put(DatabaseSettings.DeviceInfo.MAC, deviceInfo.mac);
        contentValues.put(DatabaseSettings.DeviceInfo.NAME, deviceInfo.name);
        contentValues.put(DatabaseSettings.DeviceInfo.IP, deviceInfo.ip);
        contentValues.put(DatabaseSettings.DeviceInfo.PORT, deviceInfo.port);
        contentValues.put(DatabaseSettings.DeviceInfo.SAVE_TIME, deviceInfo.saveTime);

        return context.getContentResolver().update(DatabaseSettings.DeviceInfo.CONTENT_URI, contentValues, "sn='" + deviceInfo.sn + "'", null);
    }

    public int deleteDeviceInfo(String sn){
        return context.getContentResolver().delete(DatabaseSettings.DeviceInfo.CONTENT_URI, "sn='" + sn + "'", null);
    }


}
