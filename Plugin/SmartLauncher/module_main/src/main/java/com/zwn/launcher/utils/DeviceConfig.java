package com.zwn.launcher.utils;

import android.annotation.SuppressLint;

import java.lang.reflect.Method;

public class DeviceConfig {
	public static boolean getBoolean(String namespace, String name, boolean def) {
		boolean ret = def;
		try {
			@SuppressLint("PrivateApi") Class<?> clazz = Class.forName("android.provider.DeviceConfig");
			Method mthd = clazz.getMethod("getBoolean", String.class, String.class, boolean.class);
			mthd.setAccessible(true);
			Object obj = mthd.invoke(clazz, namespace, name, def);
			if (obj instanceof Boolean) {
				ret = (Boolean) obj;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}
	

	public static boolean setProperty(String namespace, String name, String value, boolean makeDefault) {
		try {
			@SuppressLint("PrivateApi") Class<?> clazz = Class.forName("android.provider.DeviceConfig");
			Method mthd = clazz.getMethod("setProperty", String.class, String.class, String.class, boolean.class);
			mthd.setAccessible(true);
			Object obj = mthd.invoke(clazz, namespace, name, value, makeDefault);
			if (obj instanceof Boolean) {
				return (boolean) obj;
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}