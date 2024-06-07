package com.zee.setting.utils;

import android.annotation.SuppressLint;

import java.lang.reflect.Method;

public class SystemProperties {

    public static String get(String key){
        String ret = null;
        try{
            @SuppressLint("PrivateApi") Class<?> clazz = Class.forName("android.os.SystemProperties");
            Method mthd = clazz.getMethod("get", String.class);
            mthd.setAccessible(true);
            Object obj = mthd.invoke(clazz, key);
            if (obj instanceof String){
                ret = (String) obj;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return ret;
    }

    public static String get(String key, String def){
        String ret = def;
        try{
            @SuppressLint("PrivateApi") Class<?> clazz = Class.forName("android.os.SystemProperties");
            Method mthd = clazz.getMethod("get", String.class, String.class);
            mthd.setAccessible(true);
            Object obj = mthd.invoke(clazz, key, def);
            if (obj instanceof String){
                ret = (String) obj;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return ret;
    }

    public static boolean getBoolean(String key, boolean def){
        boolean ret = def;
        try{
            @SuppressLint("PrivateApi") Class<?> clazz = Class.forName("android.os.SystemProperties");
            Method mthd = clazz.getMethod("getBoolean", String.class, boolean.class);
            mthd.setAccessible(true);
            Object obj = mthd.invoke(clazz, key, def);
            if (obj instanceof Boolean){
                ret = (Boolean) obj;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static int getInt(String key, int def){
        int ret = def;
        try{
            @SuppressLint("PrivateApi") Class<?> clazz = Class.forName("android.os.SystemProperties");
            Method mthd = clazz.getMethod("getInt", String.class, int.class);
            mthd.setAccessible(true);
            Object obj = mthd.invoke(clazz, key, def);
            if (obj instanceof Integer){
                ret = (Integer) obj;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static long getLong(String key, long def){
        long ret = def;
        try{
            @SuppressLint("PrivateApi") Class<?> clazz = Class.forName("android.os.SystemProperties");
            Method mthd = clazz.getMethod("getLong", String.class, long.class);
            mthd.setAccessible(true);
            Object obj = mthd.invoke(clazz, key, def);
            if (obj instanceof Long){
                ret = (Long) obj;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static void set(String key, String value){
        try{
            @SuppressLint("PrivateApi") Class<?> clazz = Class.forName("android.os.SystemProperties");
            Method mthd = clazz.getMethod("set", String.class, String.class);
            mthd.setAccessible(true);
            mthd.invoke(clazz, key, value);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void setBoolean(String key, boolean value) {
        try {
            Class<?> clazz = Class.forName("android.os.SystemProperties");
            Method setMethod = clazz.getMethod("set", String.class, String.class);
            setMethod.invoke(null, key, Boolean.toString(value));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}