package com.zwn.lib_download.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class FileHelper {
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

}
