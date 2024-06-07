package com.zee.device.base.utils;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * 项目名:    AppUpdate
 * 包名       com.azhon.appupdate.utils
 * 文件名:    ApkUtil
 * 创建时间:  2018/1/28 on 18:16
 * 描述:     TODO apk 工具类
 *
 * @author 阿钟
 */


public final class ApkUtil {
    /**
     * 安装一个apk
     *
     * @param context     上下文
     * @param authorities Android N 授权
     * @param apk         安装包文件
     */
    public static void installApk(Context context, String authorities, File apk) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setAction(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(context, authorities, apk);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(apk);
        }
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    public static boolean isAppInstalled(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException ignored) { }
        return false;
    }

    public static boolean installApkSession(Context context, String apkFilePath, IntentSender statusReceiver) {
        PackageInstaller.Session session = null;
        try {
            File file = new File(apkFilePath);
            if(!file.exists()) return false;
            PackageInstaller packageInstaller = context.getPackageManager().getPackageInstaller();
            PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(
                    PackageInstaller.SessionParams.MODE_FULL_INSTALL);
            int sessionId = packageInstaller.createSession(params);
            session = packageInstaller.openSession(sessionId);

            addApkToInstallSession(apkFilePath, session);

            // Commit the session (this will start the installation workflow).
            session.commit(statusReceiver);
            return true;
        } catch (IOException e) {
            throw new RuntimeException("Couldn't install package", e);
        } catch (RuntimeException e) {
            if (session != null) {
                session.abandon();
            }
            return false;
        }
    }

    private static void addApkToInstallSession(String assetName, PackageInstaller.Session session)
            throws IOException {
        // It's recommended to pass the file size to openWrite(). Otherwise installation may fail
        // if the disk is almost full.
        try (OutputStream packageInSession = session.openWrite("package", 0, -1);
             InputStream is = new FileInputStream(assetName)) {
            byte[] buffer = new byte[16384];
            int n;
            while ((n = is.read(buffer)) >= 0) {
                packageInSession.write(buffer, 0, n);
            }
        }
    }

    /**
     * 获取当前app的升级版本号
     *
     * @param context 上下文
     */
    public static int getVersionCode(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
    }

    /**
     * 获取当前程序版本名
     *
     * @param context
     * @return
     */
    public static String getAppVersionName(Context context) {
        PackageManager manager = context.getPackageManager();
        String versionName = "";
        try {
            versionName = manager.getPackageInfo(context.getPackageName(), PackageManager.GET_CONFIGURATIONS).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return versionName;
    }

    public static String getAppVersionName(Context context, String packageName) {
        PackageManager manager = context.getPackageManager();
        String versionName = "";
        try {
            versionName = manager.getPackageInfo(packageName, PackageManager.GET_CONFIGURATIONS).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    public static int getAppVersionCode(Context context, String packageName) {
        PackageManager manager = context.getPackageManager();
        try {
            return manager.getPackageInfo(packageName, PackageManager.GET_CONFIGURATIONS).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 删除旧版本apk
     *
     * @param context    上下文
     * @param oldApkPath 旧版本保存的文件路径
     * @return 是否删除成功
     */
    public static boolean deleteOldApk(Context context, String oldApkPath) {
        int curVersionCode = getVersionCode(context);
        //文件存在
        try {
            File apk = new File(oldApkPath);
            if (apk.exists()) {
                int oldVersionCode = getVersionCodeByPath(context, oldApkPath);
                if (curVersionCode > oldVersionCode) {
                    return apk.delete();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 对一个apk文件获取相应的信息
     *
     * @param context 上下文
     * @param path    apk路径
     */
    public static int getVersionCodeByPath(Context context, String path) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo = packageManager.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);
        return packageInfo.versionCode;
    }

    /**
     * 获取当前app的版本号
     *
     * @param context 上下文
     */
    public static String getVersionName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return "1.0.0";
        }
    }

    /**
     * 获取app名字
     *
     * @param context 上下文
     */
    public static String getAppName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
            return (String) packageManager.getApplicationLabel(applicationInfo);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * [获取应用程序版本名称信息]
     * @param context
     * @return 当前应用的版本名称
     */
    public static String getAppPackageName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            return packageInfo.packageName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isIntentExisting(Context context, Intent intent) {
        final PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return resolveInfo.size() > 0;
    }

    /**
     * 获取图标 bitmap
     * @param context
     */
    public static Bitmap getBitmap(Context context) {
        PackageManager packageManager = null;
        ApplicationInfo applicationInfo = null;
        try {
            packageManager = context.getApplicationContext()
                    .getPackageManager();
            applicationInfo = packageManager.getApplicationInfo(
                    context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            applicationInfo = null;
        }
        Drawable d = packageManager.getApplicationIcon(applicationInfo); //xxx根据自己的情况获取drawable
        BitmapDrawable bd = (BitmapDrawable) d;
        Bitmap bm = bd.getBitmap();
        return bm;
    }

    public static String getPackageName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            String packageName = context.getPackageName();
            return packageName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
