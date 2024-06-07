package com.zee.setting.base;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.jakewharton.disklrucache.DiskLruCache;
import com.zee.setting.utils.DiskCacheHelper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class DiskCacheManager {

    @SuppressLint("StaticFieldLeak")
    private static DiskCacheManager instance;
    private DiskLruCache diskLruCache;
    private final Context context;

    private DiskCacheManager(Context context){
        this.context = context;
        if(diskLruCache==null){
            try {
                File directory = DiskCacheHelper.getCacheDirectory(context);
                int appVersion = DiskCacheHelper.getAppVersion(context);
                int maxSize = 50;//单位M
                diskLruCache = DiskLruCache.open(directory, appVersion, 1, maxSize * 1024 * 1024);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static DiskCacheManager init(Context context){
        if(instance==null){
            synchronized (DiskCacheManager.class){
                if(instance==null){
                    instance = new DiskCacheManager(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    public static DiskCacheManager getInstance(){
        return instance;
    }

    /**
     * 保存Object对象，Object要实现Serializable
     * @param key
     * @param value
     */
    public void put(String key, Object value){
        try {
            key = DiskCacheHelper.toMd5Key(key);
            DiskLruCache.Editor editor = diskLruCache.edit(key);
            if (editor != null) {
                OutputStream os = editor.newOutputStream(0);
                if (DiskCacheHelper.writeObject(os, value)) {
                    editor.commit();
                } else {
                    editor.abort();
                }
                diskLruCache.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存Bitmap
     * @param key
     * @param bitmap
     */
    public void putBitmap(String key, Bitmap bitmap) {
        put(key, DiskCacheHelper.bitmap2Bytes(bitmap));
    }

    /**
     * 保存Drawable
     * @param key
     * @param value
     */
    public void putDrawable(String key, Drawable value) {
        putBitmap(key, DiskCacheHelper.drawable2Bitmap(value));
    }

    /**
     * 根据key获取保存对象
     * @param key
     * @param <T>
     * @return
     */
    public <T> T get(String key){
        try {
            key = DiskCacheHelper.toMd5Key(key);
            DiskLruCache.Snapshot snapshot = diskLruCache.get(key);

            if (snapshot != null) {
                InputStream inputStream = snapshot.getInputStream(0);
                return (T)DiskCacheHelper.readObject(inputStream);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Bitmap getBitmap(String key) {
        byte[] bytes = (byte[]) get(key);
        if (bytes == null) return null;
        return DiskCacheHelper.bytes2Bitmap(bytes);
    }

    public Drawable getDrawable(String key) {
        byte[] bytes = (byte[]) get(key);
        if (bytes == null) {
            return null;
        }
        return DiskCacheHelper.bitmap2Drawable(context, DiskCacheHelper.bytes2Bitmap(bytes));
    }

    public long size() {
        return diskLruCache.size();
    }

    public void setMaxSize(int maxSize) {
        diskLruCache.setMaxSize((long) maxSize * 1024 * 1024);
    }

    public File getDirectory() {
        return diskLruCache.getDirectory();
    }

    public long getMaxSize() {
        return diskLruCache.getMaxSize();
    }

    public boolean remove(String key) {
        try {
            key = DiskCacheHelper.toMd5Key(key);
            return diskLruCache.remove(key);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void delete(){
        try {
            diskLruCache.delete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void flush(){
        try {
            diskLruCache.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close(){
        try {
            diskLruCache.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isClosed() {
        return diskLruCache.isClosed();
    }
}