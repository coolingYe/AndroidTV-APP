package com.zeewain.base.utils;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileUtils {

    private static final String TAG = "FileUtils";

    public static String FormatFileSize(long fileS){
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        String wrongSize="0B";
        if(fileS==0){
            return wrongSize;
        }
        if (fileS < 1024){
            fileSizeString = df.format((double) fileS) + "B";
        }
        else if (fileS < 1048576){
            fileSizeString = df.format((double) fileS / 1024) + "K";
        }
        else if (fileS < 1073741824){
            fileSizeString = df.format((double) fileS / 1048576) + "M";
        }
        else{
            fileSizeString = df.format((double) fileS / 1073741824) + "G";
        }
        return fileSizeString;
    }


    //换算不加单位
    public static String FormatFileSizeNoUnit(long fileS){
        DecimalFormat df = new DecimalFormat("#");
        String fileSizeString = "";
        String wrongSize="0B";
        if(fileS==0){
            return wrongSize;
        }
        if (fileS < 1024){
            fileSizeString = df.format((double) fileS) ;
        }
        else if (fileS < 1048576){
            fileSizeString = df.format((double) fileS / 1024) ;
        }
        else if (fileS < 1073741824){
            fileSizeString = df.format((double) fileS / 1048576) ;
        }
        else{
            fileSizeString = df.format((double) fileS / 1073741824);
        }
        return fileSizeString;
    }

    /**
     * 文件MD5文件校验
     *
     * @param file
     * @return
     */
    public static String file2MD5(File file) {

        try {
            byte[] hash;
            byte[] buffer = new byte[8192];
            MessageDigest md = MessageDigest.getInstance("MD5");
            FileInputStream fis = new FileInputStream(file);
            int len;
            while ((len = fis.read(buffer)) != -1) {
                md.update(buffer, 0, len);
            }
            hash = md.digest();

            //对生成的16字节数组进行补零操作
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                if ((b & 0xFF) < 0x10) {
                    hex.append("0");
                }
                hex.append(Integer.toHexString(b & 0xFF));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("NoSuchAlgorithmException", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UnsupportedEncodingException", e);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";

    }

    public static boolean isFileExist(String filePath){
        File file = new File(filePath);
        return file.exists();
    }

    public static boolean isNullContentFile(File file, int checkTotalByte) {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
            byte[] bytes = new byte[1024];
            int count;
            int readTotalByte = 0;
            while((count=inputStream.read(bytes))!=-1){
                readTotalByte = readTotalByte + count;
                for(int i=0; i<count; i++){
                    if(bytes[i] != 0x00){
                        return false;
                    }
                }

                if(readTotalByte >= checkTotalByte){
                    return true;
                }
            }

        } catch (Exception ignored) {} finally {
            if(inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return true;
    }

    /**
     * 删除单个文件
     * @param   filePath    被删除文件的文件名
     * @return 文件删除成功返回true，否则返回false
     */
    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            return file.delete();
        } else {
            Log.e(TAG, "不存在");
        }
        return false;
    }

    /**
     * 删除文件夹以及目录下的文件
     * @param   filePath 被删除目录的文件路径
     * @return  目录删除成功返回true，否则返回false
     */
    public static boolean deleteDirectory(String filePath) {
        boolean flag = false;
        //如果filePath不以文件分隔符结尾，自动添加文件分隔符
        if (!filePath.endsWith(File.separator)) {
            filePath = filePath + File.separator;
        }
        File dirFile = new File(filePath);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        flag = true;
        File[] files = dirFile.listFiles();
        //遍历删除文件夹下的所有文件(包括子目录)
        for (File file : files) {
            if (file.isFile()) {
                //删除子文件
                flag = deleteFile(file.getAbsolutePath());
                if (!flag) break;
            } else {
                //删除子目录
                flag = deleteDirectory(file.getAbsolutePath());
                if (!flag) break;
            }
        }
        if (!flag) return false;
        //删除当前空目录
        return dirFile.delete();
    }

    /**
     *  根据路径删除指定的目录或文件，无论存在与否
     *@param filePath  要删除的目录或文件
     *@return 删除成功返回 true，否则返回 false。
     */
    public static boolean deleteFolder(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return false;
        } else {
            if (file.isFile()) {
                // 为文件时调用删除文件方法
                return deleteFile(filePath);
            } else {
                // 为目录时调用删除目录方法
                return deleteDirectory(filePath);
            }
        }
    }

    /**
     * 读取文件
     * @param filePath 文件路径
     * @return 文件内容
     */
    public static String readFile(String filePath) {
        File file = new File(filePath);
        int length = (int) file.length();
        byte[] byteBuffer = new byte[length];
        String result = null;
        try {
            FileInputStream stream = new FileInputStream(file);
            stream.read(byteBuffer);
            stream.close();
            result = new String(byteBuffer, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 获取目录下所有文件名称
     * @param path 路径
     * @return 文件名称列表
     */
    public static List<File> getFiles(String path) {
        File root = new File(path);
        if (!root.exists() || !root.isDirectory()) {
            return null;
        }
        File[] files = root.listFiles();
        if (files == null) {
            Log.e(TAG, "path directory is empty");
            return null;
        }
        List<File> fileList = new ArrayList<>();
        Collections.addAll(fileList, files);
        Collections.sort(fileList, new FileCompareUtils());
        return fileList;
    }

    public static boolean copyFilesFromAssetsTo(Context context, String[] fileNames, String dirPath) {
        try {
            for (String model : fileNames) {
                copyAssetFileToFiles(context, model, dirPath);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static void copyAssetFileToFiles(Context context, String filename, String dirPath) throws IOException {
        File of = new File(dirPath + filename);
        if(!of.exists()){
            InputStream is = context.getAssets().open(filename);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);

            of.createNewFile();
            FileOutputStream os = new FileOutputStream(of);
            os.write(buffer);
            os.close();
            is.close();
        }
    }

    public static boolean copyFilesTo(List<String> srcFiles, String desDir) {
        try {
            for (String file : srcFiles) {
                copyFileToDir(file, desDir);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static void copyFileToDir(String filePath, String desDir) throws IOException {
        String fileName = filePath;
        if(filePath.contains("/")){
            fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
        }
        if(!desDir.endsWith("/")){
            desDir = desDir + "/";
        }
        File of = new File(desDir + fileName);
        if(!of.exists()){
            InputStream is = new FileInputStream(filePath);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);

            of.createNewFile();
            Runtime.getRuntime().exec("chmod 666 " + of);
            FileOutputStream os = new FileOutputStream(of);
            os.write(buffer);
            os.close();
            is.close();
        }
    }
}
