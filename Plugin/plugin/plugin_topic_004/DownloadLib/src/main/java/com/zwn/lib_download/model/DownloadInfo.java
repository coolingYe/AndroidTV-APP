package com.zwn.lib_download.model;

import android.annotation.SuppressLint;
import android.database.Cursor;

import com.zwn.lib_download.db.CareSettings;

import java.util.HashMap;
import java.util.Map;

public class DownloadInfo {
    public static final int STATUS_PENDING = 0;//等待下载；
    public static final int STATUS_LOADING = 1;//下载中
    public static final int STATUS_STOPPED = 2;//下载停止
    public static final int STATUS_SUCCESS = 3;//下载成功

    public long _id;
    public String fileId;
    public String fileName;
    public String fileImgUrl;
    public String mainClassPath;
    public String url;
    public long fileSize;
    public long loadedSize;
    public String filePath;
    public String version;
    public int status;
    public int type;
    public String packageMd5;
    public String relyIds;
    public String extraId;
    public int extraOne; //reserve
    public String extraTwo; //reserve
    public long saveTime;
    public String describe;

    public DownloadInfo() {
    }

    @SuppressLint("Range")
    public DownloadInfo(Cursor cursor){
        _id = cursor.getLong(cursor.getColumnIndex(CareSettings.DownloadInfo._ID));
        fileId = cursor.getString(cursor.getColumnIndex(CareSettings.DownloadInfo.FILE_ID));
        fileName = cursor.getString(cursor.getColumnIndex(CareSettings.DownloadInfo.FILENAME));
        fileImgUrl = cursor.getString(cursor.getColumnIndex(CareSettings.DownloadInfo.FILE_IMG_URL));
        mainClassPath = cursor.getString(cursor.getColumnIndex(CareSettings.DownloadInfo.MAIN_CLASS_PATH));
        url = cursor.getString(cursor.getColumnIndex(CareSettings.DownloadInfo.URL));
        fileSize = cursor.getLong(cursor.getColumnIndex(CareSettings.DownloadInfo.FILE_SIZE));
        loadedSize = cursor.getLong(cursor.getColumnIndex(CareSettings.DownloadInfo.LOADED_SIZE));
        filePath = cursor.getString(cursor.getColumnIndex(CareSettings.DownloadInfo.FILE_PATH));
        version = cursor.getString(cursor.getColumnIndex(CareSettings.DownloadInfo.VERSION));
        status = cursor.getInt(cursor.getColumnIndex(CareSettings.DownloadInfo.STATUS));
        type = cursor.getInt(cursor.getColumnIndex(CareSettings.DownloadInfo.TYPE));
        packageMd5 = cursor.getString(cursor.getColumnIndex(CareSettings.DownloadInfo.PACKAGE_MD5));
        relyIds = cursor.getString(cursor.getColumnIndex(CareSettings.DownloadInfo.RELY_IDS));
        extraId = cursor.getString(cursor.getColumnIndex(CareSettings.DownloadInfo.EXTRA_ID));
        extraOne = cursor.getInt(cursor.getColumnIndex(CareSettings.DownloadInfo.EXTRA_ONE));
        extraTwo = cursor.getString(cursor.getColumnIndex(CareSettings.DownloadInfo.EXTRA_TWO));
        saveTime = cursor.getLong(cursor.getColumnIndex(CareSettings.DownloadInfo.SAVE_TIME));
        describe = cursor.getString(cursor.getColumnIndex(CareSettings.DownloadInfo.DESC));
    }

    public Map<String, Object> toMap(){
        Map<String, Object> map = new HashMap<>();
        map.put("fileId", fileId);
        map.put("fileName", fileName);
        map.put("fileImgUrl", fileImgUrl);
        map.put("mainClassPath", mainClassPath);
        map.put("filePath", filePath);
        map.put("url", url);
        map.put("status", status);
        map.put("type", type);
        map.put("relyIds", relyIds);
        map.put("version", version);
        map.put("loadedSize", loadedSize);
        map.put("fileSize", fileSize);
        return map;
    }

    @Override
    public String toString() {
        return "DownloadInfo{" +
                "_id=" + _id +
                ", fileId='" + fileId + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileImgUrl='" + fileImgUrl + '\'' +
                ", mainClassPath='" + mainClassPath + '\'' +
                ", url='" + url + '\'' +
                ", fileSize=" + fileSize +
                ", loadedSize=" + loadedSize +
                ", filePath='" + filePath + '\'' +
                ", version='" + version + '\'' +
                ", status=" + status +
                ", type=" + type +
                ", packageMd5='" + packageMd5 + '\'' +
                ", relyIds='" + relyIds + '\'' +
                ", extraId='" + extraId + '\'' +
                ", extraOne=" + extraOne +
                ", extraTwo='" + extraTwo + '\'' +
                ", saveTime=" + saveTime +
                ", describe='" + describe + '\'' +
                '}';
    }
}
