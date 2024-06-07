package com.zee.device.base.db;

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

import com.zee.device.base.BuildConfig;

public class DatabaseProvider extends ContentProvider {
    private static final String TAG = "DatabaseProvider";
    private static final boolean DEBUG = false;
    private static final String DATABASE_NAME = "database.db";
    private static final int DATABASE_VERSION = 1;

    static final String AUTHORITY = "com.zee.device.base";
    static final String TABLE_DEVICE_INFO = "device_info";

    private static DatabaseHelper sOpenHelper;

    private static final String SQL_TABLE_DEVICE_INFO = "CREATE TABLE " + TABLE_DEVICE_INFO + " (" +
            "_id INTEGER PRIMARY KEY," +
            "sn VARCHAR(64) UNIQUE," +
            "mac VARCHAR(17)," +
            "name VARCHAR(128)," +
            "ip VARCHAR(15)," +
            "port INTEGER," +
            "extraOne INTEGER," +
            "extraTwo TEXT," +
            "saveTime LONG," +
            "describe TEXT" +
            ");";

    @Override
    public boolean onCreate() {
        if (DEBUG) Log.d(TAG, "creating new DatabaseProvider");
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
            if (DEBUG) Log.d(TAG, "creating new database");
            db.execSQL(SQL_TABLE_DEVICE_INFO);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (DEBUG) Log.d(TAG, "onUpgrade database, oldVersion="
                    + oldVersion + ", newVersion=" + newVersion);
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
