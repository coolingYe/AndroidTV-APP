package com.zwn.lib_download.db;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.zwn.lib_download.model.DownloadInfo;
import com.zwn.lib_download.model.LoadedInfo;

import java.util.ArrayList;
import java.util.List;

public class CareController {
    private final Context context;
    public static volatile CareController instance;

    public static void init(Context context){
        if(instance == null){
            synchronized (CareController.class){
                if(instance == null){
                    instance = new CareController(context);
                }
            }
        }
    }

    private CareController(Context context){
        this.context = context;
    }

    public boolean addDownloadInfo(DownloadInfo downloadInfo) {
        ContentValues contentValues = new ContentValues(12);
        contentValues.put(CareSettings.DownloadInfo.FILE_ID, downloadInfo.fileId);
        contentValues.put(CareSettings.DownloadInfo.FILENAME, downloadInfo.fileName);
        contentValues.put(CareSettings.DownloadInfo.FILE_IMG_URL, downloadInfo.fileImgUrl);
        contentValues.put(CareSettings.DownloadInfo.MAIN_CLASS_PATH, downloadInfo.mainClassPath);
        contentValues.put(CareSettings.DownloadInfo.URL, downloadInfo.url);
        contentValues.put(CareSettings.DownloadInfo.FILE_SIZE, downloadInfo.fileSize);
        contentValues.put(CareSettings.DownloadInfo.LOADED_SIZE, downloadInfo.loadedSize);
        contentValues.put(CareSettings.DownloadInfo.FILE_PATH, downloadInfo.filePath);
        contentValues.put(CareSettings.DownloadInfo.VERSION, downloadInfo.version);
        contentValues.put(CareSettings.DownloadInfo.STATUS, downloadInfo.status);
        contentValues.put(CareSettings.DownloadInfo.TYPE, downloadInfo.type);
        contentValues.put(CareSettings.DownloadInfo.PACKAGE_MD5, downloadInfo.packageMd5);
        contentValues.put(CareSettings.DownloadInfo.RELY_IDS, downloadInfo.relyIds);
        contentValues.put(CareSettings.DownloadInfo.EXTRA_ID, downloadInfo.extraId);
        contentValues.put(CareSettings.DownloadInfo.SAVE_TIME, downloadInfo.saveTime);
        contentValues.put(CareSettings.DownloadInfo.DESC, downloadInfo.describe);

        Uri uri = context.getContentResolver().insert(CareSettings.DownloadInfo.CONTENT_URI, contentValues);
        if (uri != null && uri.getPathSegments() != null && uri.getPathSegments().size() == 2){
            long rowId = ContentUris.parseId(uri);
            if(rowId > 0) return true;
        }
        return false;
    }

    public List<DownloadInfo> getAllDownloadInfo(String selection){
        Cursor cursor = context.getContentResolver().query(CareSettings.DownloadInfo.CONTENT_URI,
                CareSettings.DownloadInfo.DOWNLOAD_INFO_QUERY_COLUMNS, selection, null, CareSettings.DownloadInfo.SAVE_TIME + " DESC");
        List<DownloadInfo> downloadInfoList = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()){
                downloadInfoList.add(new DownloadInfo(cursor));
            }
            cursor.close();
        }

        return downloadInfoList;
    }

    public Cursor getDownloadInfo(String selection){
        return context.getContentResolver().query(CareSettings.DownloadInfo.CONTENT_URI,
                CareSettings.DownloadInfo.DOWNLOAD_INFO_QUERY_COLUMNS, selection, null, CareSettings.DownloadInfo.SAVE_TIME + " DESC");
    }

    public DownloadInfo getDownloadInfoByFileId(String fileId){
        DownloadInfo downloadInfo = null;
        Cursor cursor =  context.getContentResolver().query(CareSettings.DownloadInfo.CONTENT_URI,
                CareSettings.DownloadInfo.DOWNLOAD_INFO_QUERY_COLUMNS, "fileId='" + fileId + "'", null, null);
        if (cursor != null) {
            if(cursor.moveToFirst()) {
                downloadInfo = new DownloadInfo(cursor);
            }
            cursor.close();
        }
        return downloadInfo;
    }

    public DownloadInfo getLatestPendingDownloadInfo(){
        DownloadInfo downloadInfo = null;
        Cursor cursor =  context.getContentResolver().query(CareSettings.DownloadInfo.CONTENT_URI,
                CareSettings.DownloadInfo.DOWNLOAD_INFO_QUERY_COLUMNS, "status=0", null, CareSettings.DownloadInfo.SAVE_TIME + " DESC LIMIT 1");
        if (cursor != null) {
            if(cursor.moveToFirst()) {
                downloadInfo = new DownloadInfo(cursor);
            }
            cursor.close();
        }
        return downloadInfo;
    }

    public List<DownloadInfo> getLatestPendingList(){
        Cursor cursor =  context.getContentResolver().query(CareSettings.DownloadInfo.CONTENT_URI,
                CareSettings.DownloadInfo.DOWNLOAD_INFO_QUERY_COLUMNS, "status=0", null, CareSettings.DownloadInfo.SAVE_TIME + " DESC LIMIT 5");
        List<DownloadInfo> downloadInfoList = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()){
                downloadInfoList.add(new DownloadInfo(cursor));
            }
            cursor.close();
        }
        return downloadInfoList;
    }

    public int updateDownloadInfoNewVersion(String fileId, int status, String filePath, String version, String url, String relyIds){
        ContentValues contentValues = new ContentValues(7);
        contentValues.put(CareSettings.DownloadInfo.STATUS, status);
        contentValues.put(CareSettings.DownloadInfo.FILE_PATH, filePath);
        contentValues.put(CareSettings.DownloadInfo.VERSION, version);
        contentValues.put(CareSettings.DownloadInfo.URL, url);
        contentValues.put(CareSettings.DownloadInfo.RELY_IDS, relyIds);
        contentValues.put(CareSettings.DownloadInfo.LOADED_SIZE, 0);
        contentValues.put(CareSettings.DownloadInfo.FILE_SIZE, 0);
        return context.getContentResolver().update(CareSettings.DownloadInfo.CONTENT_URI, contentValues, "fileId='" + fileId + "'", null);
    }

    public int updateDownloadInfoStatus(String fileId, int status){
        ContentValues contentValues = new ContentValues(1);
        contentValues.put(CareSettings.DownloadInfo.STATUS, status);
        return context.getContentResolver().update(CareSettings.DownloadInfo.CONTENT_URI, contentValues, "fileId='" + fileId + "'", null);
    }

    public int updateDownloadInfoPending(String fileId){
        ContentValues contentValues = new ContentValues(1);
        contentValues.put(CareSettings.DownloadInfo.STATUS, DownloadInfo.STATUS_PENDING);
        return context.getContentResolver().update(CareSettings.DownloadInfo.CONTENT_URI, contentValues, "fileId='" + fileId + "' and status=2", null);
    }

    public int updateDownloadInfoLoading(String fileId, int status, long fileSize){
        ContentValues contentValues = new ContentValues(2);
        contentValues.put(CareSettings.DownloadInfo.STATUS, status);
        contentValues.put(CareSettings.DownloadInfo.FILE_SIZE, fileSize);
        return context.getContentResolver().update(CareSettings.DownloadInfo.CONTENT_URI, contentValues, "fileId='" + fileId + "' and status<>1", null);
    }

    public int updateDownloadInfoStop(){
        ContentValues contentValues = new ContentValues(1);
        contentValues.put(CareSettings.DownloadInfo.STATUS, DownloadInfo.STATUS_STOPPED);
        return context.getContentResolver().update(CareSettings.DownloadInfo.CONTENT_URI, contentValues, "(status=0 or status=1)", null);
    }

    public int updateFailedDownloadStatus(String fileId){
        ContentValues contentValues = new ContentValues(1);
        contentValues.put(CareSettings.DownloadInfo.STATUS, DownloadInfo.STATUS_STOPPED);
        return context.getContentResolver().update(CareSettings.DownloadInfo.CONTENT_URI, contentValues,
                "fileId='" + fileId + "' and (status=0 or status=1 or status=2)" , null);
    }

    public int deleteDownloadInfo(String fileId){
        return context.getContentResolver().delete(CareSettings.DownloadInfo.CONTENT_URI, "fileId='" + fileId + "'", null);
    }

    public int bulkUpdateLoadedSize(List<LoadedInfo> loadedInfoList){
        ContentValues[] arrayContentValues = new ContentValues[loadedInfoList.size()];
        LoadedInfo loadedInfo;
        for (int i = 0; i < loadedInfoList.size(); i++) {
            loadedInfo = loadedInfoList.get(i);
            arrayContentValues[i] = new ContentValues(2);
            arrayContentValues[i].put(CareSettings.DownloadInfo.FILE_ID, loadedInfo.fileId);
            arrayContentValues[i].put(CareSettings.DownloadInfo.LOADED_SIZE, loadedInfo.loadedSize);
        }
        return context.getContentResolver().bulkInsert(CareSettings.DownloadInfo.CONTENT_URI, arrayContentValues);
    }

}
