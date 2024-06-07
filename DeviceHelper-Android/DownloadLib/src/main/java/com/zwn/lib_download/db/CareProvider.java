package com.zwn.lib_download.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class CareProvider extends ContentProvider {
    private static final String TAG = "CareProvider";
    private static final boolean DEBUG = false;
    private static final String DATABASE_NAME = "care.db";
    private static final int DATABASE_VERSION = 2;

    static final String AUTHORITY = "com.zee.device.helper.settings";
    static final String TABLE_DOWNLOAD_INFO = "download_info";
    static final String TABLE_APP_LIB_INFO = "app_lib_info";

    private static DatabaseHelper sOpenHelper;

    private static final String SQL_TABLE_DOWNLOAD_INFO = "CREATE TABLE " + TABLE_DOWNLOAD_INFO + " (" +
            "_id INTEGER PRIMARY KEY," +
            "fileId VARCHAR(32) UNIQUE," +
            "fileName VARCHAR(64)," +
            "fileImgUrl TEXT," +
            "mainClassPath VARCHAR(200)," +
            "url TEXT," +
            "loadedSize LONG," +
            "fileSize LONG," +
            "filePath TEXT," +
            "version VARCHAR(20)," +
            "status INTEGER," +
            "type INTEGER," +
            "packageMd5 VARCHAR(32)," +
            "relyIds TEXT," +
            "extraId VARCHAR(32)," +
            "extraOne INTEGER," +
            "extraTwo TEXT," +
            "saveTime LONG," +
            "describe TEXT" +
            ");";

    private static final String SQL_TABLE_APP_LIB_INFO = "CREATE TABLE " + TABLE_APP_LIB_INFO + " (" +
            "_id INTEGER PRIMARY KEY," +
            "fileId VARCHAR(32) UNIQUE," +
            "packageName VARCHAR(128)," +
            "libPath TEXT," +
            "libMd5 VARCHAR(32)," +
            "status INTEGER," +
            "saveTime LONG," +
            "describe TEXT" +
            ");";

    @Override
    public boolean onCreate() {
        if (DEBUG) Log.d(TAG, "creating new HealthProvider");
        sOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SqlArguments args = new SqlArguments(uri, selection, selectionArgs);
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(args.table);

        SQLiteDatabase db = sOpenHelper.getWritableDatabase();
        Cursor result = qb.query(db, projection, args.where, args.args, null, null, sortOrder);
        result.setNotificationUri(getContext().getContentResolver(), uri);

        return result;
    }

    @Override
    public String getType(Uri uri) {
        SqlArguments args = new SqlArguments(uri, null, null);
        if (TextUtils.isEmpty(args.where)) {
            return "vnd.android.cursor.dir/" + args.table;
        } else {
            return "vnd.android.cursor.item/" + args.table;
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SqlArguments args = new SqlArguments(uri);

        SQLiteDatabase db = sOpenHelper.getWritableDatabase();
        final long rowId = db.insert(args.table, null, values);

        uri = ContentUris.withAppendedId(uri, rowId);
        getContext().getContentResolver().notifyChange(uri, null);
        return uri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        if (CareSettings.DownloadInfo.CONTENT_URI.equals(uri)) {
            SQLiteDatabase db = sOpenHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                for (ContentValues value : values) {
                    long loadedSize = value.getAsLong(CareSettings.DownloadInfo.LOADED_SIZE);
                    String fileId = value.getAsString(CareSettings.DownloadInfo.FILE_ID);
                    String sqlUpdate = "update download_info set loadedSize=" + loadedSize + " where fileId='" + fileId + "'";
                    db.execSQL(sqlUpdate);
                }
                db.setTransactionSuccessful();
                getContext().getContentResolver().notifyChange(uri, null);
                return values.length;
            } finally {
                db.endTransaction();
            }
        }
        return 0;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SqlArguments args = new SqlArguments(uri, selection, selectionArgs);

        SQLiteDatabase db = sOpenHelper.getWritableDatabase();
        int count = db.delete(args.table, args.where, args.args);
        if (DEBUG) Log.d(TAG, "*** notifyChange() ==== " + " url " + uri);
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SqlArguments args = new SqlArguments(uri, selection, selectionArgs);

        SQLiteDatabase db = sOpenHelper.getWritableDatabase();
        int count = db.update(args.table, values, args.where, args.args);
        if (DEBUG) Log.d(TAG, "*** notifyChange() ==== " + " url " + uri);
        if(count > 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    public static DatabaseHelper getOpenHelper() {
        return sOpenHelper;
    }

    public static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            if (DEBUG) Log.d(TAG, "creating new zwn care database");
            db.execSQL(SQL_TABLE_DOWNLOAD_INFO);
            db.execSQL(SQL_TABLE_APP_LIB_INFO);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (DEBUG) Log.d(TAG, "onUpgrade zwn care database, oldVersion="
                    + oldVersion + ", newVersion=" + newVersion);
            if(oldVersion == 1){
                db.execSQL(SQL_TABLE_APP_LIB_INFO);
            }
        }
    }


    static class SqlArguments {
        public final String table;
        public final String where;
        public final String[] args;

        SqlArguments(Uri url, String where, String[] args) {
            if (url.getPathSegments().size() == 1) {
                this.table = url.getPathSegments().get(0);
                this.where = where;
                this.args = args;
            } else if (url.getPathSegments().size() != 2) {
                throw new IllegalArgumentException("Invalid URI: " + url);
            } /*else if (!TextUtils.isEmpty(where)) {
                throw new UnsupportedOperationException("WHERE clause not supported: " + url);
            }*/ else {
                this.table = url.getPathSegments().get(0);
                this.where = "_id=" + ContentUris.parseId(url);
                this.args = null;
            }
        }

        SqlArguments(Uri url) {
            if (url.getPathSegments().size() == 1) {
                table = url.getPathSegments().get(0);
                where = null;
                args = null;
            } else {
                throw new IllegalArgumentException("Invalid URI: " + url);
            }
        }
    }

}
