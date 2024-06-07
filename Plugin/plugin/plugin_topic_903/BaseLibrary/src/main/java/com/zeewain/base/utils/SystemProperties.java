package com.zeewain.base.utils;

import java.lang.reflect.Method;
 
public class SystemProperties
{
 
	public static String get(String key)
	{
		String ret = null;
		try
		{
			Class<?> clazz = Class.forName("android.os.SystemProperties");
			Method mthd = clazz.getMethod("get", new Class[] { String.class });
			mthd.setAccessible(true);
			Object obj = mthd.invoke(clazz, new Object[] { key });
			if (obj != null && obj instanceof String)
			{
				ret = (String) obj;
 
			}
 
		}
		catch (Exception e)
		{
			e.printStackTrace();
 
		}
		return ret;
 
	}
 
	public static String get(String key, String def)
	{
		String ret = def;
		try
		{
			Class<?> clazz = Class.forName("android.os.SystemProperties");
			Method mthd = clazz.getMethod("get", new Class[] { String.class, String.class });
			mthd.setAccessible(true);
			Object obj = mthd.invoke(clazz, new Object[] { key, def });
			if (obj != null && obj instanceof String)
			{
				ret = (String) obj;
 
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
 
		}
 
		return ret;
	}
	
	public static boolean getBoolean(String key, boolean def)
	{
		boolean ret = def;
		try
		{
			Class<?> clazz = Class.forName("android.os.SystemProperties");
			Method mthd = clazz.getMethod("getBoolean", new Class[] { String.class, boolean.class });
			mthd.setAccessible(true);
			Object obj = mthd.invoke(clazz, new Object[] { key, def });
			if (obj != null && obj instanceof Boolean)
			{
				ret = (Boolean) obj;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return ret;
	}
	
	public static int getInt(String key, int def)
	{
		int ret = def;
		try
		{
			Class<?> clazz = Class.forName("android.os.SystemProperties");
			Method mthd = clazz.getMethod("getInt", new Class[] { String.class, int.class });
			mthd.setAccessible(true);
			Object obj = mthd.invoke(clazz, new Object[] { key, def });
			if (obj != null && obj instanceof Integer)
			{
				ret = (Integer) obj;
 
			}
 
		}
		catch (Exception e)
		{
			e.printStackTrace();
 
		}
		return ret;
 
	}
	
	public static long getLong(String key, long def)
	{
		long ret = def;
		try
		{
			Class<?> clazz = Class.forName("android.os.SystemProperties");
			Method mthd = clazz.getMethod("getLong", new Class[] { String.class, long.class });
			mthd.setAccessible(true);
			Object obj = mthd.invoke(clazz, new Object[] { key, def });
			if (obj != null && obj instanceof Long)
			{
				ret = (Long) obj;
 
			}
 
		}
		catch (Exception e)
		{
			e.printStackTrace();
 
		}
		return ret;
	}
	
	public static void set(String key, String value)
	{
		try
		{
			Class<?> clazz = Class.forName("android.os.SystemProperties");
			Method mthd = clazz.getMethod("set", new Class[] { String.class, String.class });
			mthd.setAccessible(true);
			mthd.invoke(clazz, new Object[] { key, value });
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}