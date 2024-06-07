package com.zee.setting.utils;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;
import android.service.dreams.DreamService;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class DreamUtils {
    final static String TAG = "DreamUtils";

    @SuppressLint("WrongConstant")
    public static ComponentName getActiveDream() {
        Log.d(TAG, "getActiveDream");
        try{
            @SuppressLint("PrivateApi")
            Class <?> ServiceManager = Class.forName("android.os.ServiceManager");
            Method getService = ServiceManager.getMethod("getService", java.lang.String.class);
            Object objectRemoteService = getService.invoke(null, "dreams");
            if (objectRemoteService == null){
                Log.d(TAG, "getActiveDream() objectRemoteService is null");
                return null;
            }

            @SuppressLint("PrivateApi")
            Class <?> cStub = Class.forName("android.service.dreams.IDreamManager$Stub");
            Method asInterface = cStub.getMethod("asInterface", android.os.IBinder.class);
            Object dreamManagerService = asInterface.invoke(null, objectRemoteService);

            if (dreamManagerService == null){
                Log.d(TAG, "getActiveDream() dreamManagerService is null");
                return null;
            } else {
                Log.d(TAG, "getActiveDream() dreamManagerService get success!");
                Method getDreamComponentsMethod = dreamManagerService.getClass().getMethod("getDreamComponents");
                Object objMethod = getDreamComponentsMethod.invoke(dreamManagerService);
                if (objMethod instanceof ComponentName[]) {
                    Log.d(TAG, "getActiveDream() dreamManagerService getDreamComponents ok!");
                    ComponentName[] dreams = (ComponentName[])objMethod;
                    for (ComponentName componentName: dreams) {
                        Log.d(TAG, "getActiveDream() componentName=" + componentName.getPackageName());
                    }
                    return dreams.length > 0 ? dreams[0] : null;
                }
            }

        } catch (ClassNotFoundException e) {
            Log.e(TAG, "getActiveDream - ClassNotFoundException!");
        } catch (InvocationTargetException e) {
            Log.e(TAG, "getActiveDream - InvocationTargetException!");
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "getActiveDream - NoSuchMethodException!");
        } catch (IllegalAccessException e) {
            Log.e(TAG, "getActiveDream - IllegalAccessException!");
        }
        return null;
    }

    @SuppressLint("WrongConstant")
    public static void setActiveDream(ComponentName dream) {
        Log.d(TAG, "setActiveDream");
        try{
            @SuppressLint("PrivateApi")
            Class <?> ServiceManager = Class.forName("android.os.ServiceManager");
            Method getService = ServiceManager.getMethod("getService", java.lang.String.class);
            Object objectRemoteService = getService.invoke(null, "dreams");
            if (objectRemoteService == null){
                Log.d(TAG, "setActiveDream() objectRemoteService is null");
                return;
            }

            @SuppressLint("PrivateApi")
            Class <?> cStub = Class.forName("android.service.dreams.IDreamManager$Stub");
            Method asInterface = cStub.getMethod("asInterface", android.os.IBinder.class);
            Object dreamManagerService = asInterface.invoke(null, objectRemoteService);

            if (dreamManagerService == null){
                Log.d(TAG, "setActiveDream() dreamManagerService is null");
                return;
            } else {
                Log.d(TAG, "setActiveDream() dreamManagerService get success!");
                Method getDreamComponentsMethod = dreamManagerService.getClass().getMethod("setDreamComponents", ComponentName[].class);
                ComponentName[] dreams = {dream};
                if (dream == null) {
                    getDreamComponentsMethod.invoke(dreamManagerService, (Object)null);
                } else {
                    getDreamComponentsMethod.invoke(dreamManagerService, new Object[]{dreams});
                }

            }

        } catch (ClassNotFoundException e) {
            Log.e(TAG, "setActiveDream - ClassNotFoundException!");
        } catch (InvocationTargetException e) {
            Log.e(TAG, "setActiveDream - InvocationTargetException!");
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "setActiveDream - NoSuchMethodException!");
        } catch (IllegalAccessException e) {
            Log.e(TAG, "setActiveDream - IllegalAccessException!");
        }
    }

    public static List<DreamInfo> getDreamInfoList(Context context) {
        Log.e(TAG, "getDreamInfoList()");
        ComponentName activeDream = getActiveDream();
        PackageManager pm = context.getPackageManager();
        Intent dreamIntent = new Intent(DreamService.SERVICE_INTERFACE);
        List<ResolveInfo> resolveInfos = pm.queryIntentServices(dreamIntent,
                PackageManager.GET_META_DATA);
        List<DreamInfo> dreamInfos = new ArrayList<>(resolveInfos.size());
        for (ResolveInfo resolveInfo : resolveInfos) {
            if (resolveInfo.serviceInfo == null)
                continue;
            DreamInfo dreamInfo = new DreamInfo();
            dreamInfo.caption = resolveInfo.loadLabel(pm);
            dreamInfo.icon = resolveInfo.loadIcon(pm);
            dreamInfo.componentName = getDreamComponentName(resolveInfo);
            dreamInfo.isActive = dreamInfo.componentName.equals(activeDream);
            dreamInfo.settingsComponentName = getSettingsComponentName(pm, resolveInfo);
            dreamInfos.add(dreamInfo);
        }
        return dreamInfos;
    }

    private static ComponentName getSettingsComponentName(PackageManager pm, ResolveInfo resolveInfo) {
        if (resolveInfo == null
                || resolveInfo.serviceInfo == null
                || resolveInfo.serviceInfo.metaData == null)
            return null;
        String cn = null;
        XmlResourceParser parser = null;
        Exception caughtException = null;
        try {
            parser = resolveInfo.serviceInfo.loadXmlMetaData(pm, DreamService.DREAM_META_DATA);
            if (parser == null) {
                Log.w(TAG, "No " + DreamService.DREAM_META_DATA + " meta-data");
                return null;
            }
            Resources res = pm.getResourcesForApplication(resolveInfo.serviceInfo.applicationInfo);
            AttributeSet attrs = Xml.asAttributeSet(parser);
            int type;
            while ((type=parser.next()) != XmlPullParser.END_DOCUMENT
                    && type != XmlPullParser.START_TAG) {
            }
            String nodeName = parser.getName();
            if (!"dream".equals(nodeName)) {
                Log.w(TAG, "Meta-data does not start with dream tag");
                return null;
            }

            if (parser.getAttributeCount() > 0 && "settingsActivity".equals(parser.getAttributeName(0))) {
                cn = parser.getAttributeValue(0);
            }
            Log.w(TAG, "getDreamComponentName() cn=" + cn);

            /*TypedArray sa = res.obtainAttributes(attrs, com.android.internal.R.styleable.Dream);
            cn = sa.getString(com.android.internal.R.styleable.Dream_settingsActivity);
            sa.recycle();*/
        } catch (PackageManager.NameNotFoundException | IOException | XmlPullParserException e) {
            caughtException = e;
        } finally {
            if (parser != null) parser.close();
        }
        if (caughtException != null) {
            Log.w(TAG, "Error parsing : " + resolveInfo.serviceInfo.packageName, caughtException);
            return null;
        }
        if (cn != null && cn.indexOf('/') < 0) {
            cn = resolveInfo.serviceInfo.packageName + "/" + cn;
        }
        return cn == null ? null : ComponentName.unflattenFromString(cn);
    }

    private static ComponentName getDreamComponentName(ResolveInfo resolveInfo) {
        if (resolveInfo == null || resolveInfo.serviceInfo == null)
            return null;
        return new ComponentName(resolveInfo.serviceInfo.packageName, resolveInfo.serviceInfo.name);
    }

    public static void launchSettings(Context uiContext, DreamUtils.DreamInfo dreamInfo) {
        if (dreamInfo == null || dreamInfo.settingsComponentName == null) {
            return;
        }
        uiContext.startActivity(new Intent().setComponent(dreamInfo.settingsComponentName));
    }

    public static class DreamInfo {
        public CharSequence caption;
        public Drawable icon;
        public boolean isActive;
        public ComponentName componentName;
        public ComponentName settingsComponentName;

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(DreamInfo.class.getSimpleName());
            sb.append('[').append(caption);
            if (isActive)
                sb.append(",active");
            sb.append(',').append(componentName);
            if (settingsComponentName != null)
                sb.append("settings=").append(settingsComponentName);
            return sb.append(']').toString();
        }
    }
}
