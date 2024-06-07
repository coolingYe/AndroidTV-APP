package com.zwn.lib_download.model;

import android.annotation.SuppressLint;
import android.database.Cursor;

import com.zwn.lib_download.db.CareSettings;

public class AppLibInfo {
    public static final int STATUS_DEFAULT = 0; //安装前；
    public static final int STATUS_INSTALLED = 1;//成功安装；

    public static final String LIB_NAME = "libmain.so";

    public long _id;
    public String fileId;
    public String packageName;
    public String libPath;
    public String libMd5;
    public int status;
    public long saveTime;
    public String describe;

    public AppLibInfo(String fileId, String packageName) {
        this.fileId = fileId;
        this.packageName = packageName;
        this.libPath = "";
        this.libMd5 = "";
        this.status = STATUS_DEFAULT;
        this.saveTime = System.currentTimeMillis();
    }

    public AppLibInfo() {}

    @SuppressLint("Range")
    public AppLibInfo(Cursor cursor){
        _id = cursor.getLong(cursor.getColumnIndex(CareSettings.AppLibInfo._ID));
        fileId = cursor.getString(cursor.getColumnIndex(CareSettings.AppLibInfo.FILE_ID));
        packageName = cursor.getString(cursor.getColumnIndex(CareSettings.AppLibInfo.PACKAGE_NAME));
        libPath = cursor.getString(cursor.getColumnIndex(CareSettings.AppLibInfo.LIB_PATH));
        libMd5 = cursor.getString(cursor.getColumnIndex(CareSettings.AppLibInfo.LIB_MD5));
        status = cursor.getInt(cursor.getColumnIndex(CareSettings.AppLibInfo.STATUS));
        saveTime = cursor.getLong(cursor.getColumnIndex(CareSettings.AppLibInfo.SAVE_TIME));
        describe = cursor.getString(cursor.getColumnIndex(CareSettings.AppLibInfo.DESC));
    }
}
