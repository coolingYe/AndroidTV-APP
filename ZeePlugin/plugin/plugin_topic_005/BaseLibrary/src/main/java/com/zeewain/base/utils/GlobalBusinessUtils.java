package com.zeewain.base.utils;

import java.util.TimeZone;

/**
 * 如果有全球业务的话
 */
public class GlobalBusinessUtils {

    /**
     * 获取当前时区
     * @return 时区
     */
    public static String getCurrentTimeZone() {
        return TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT);
    }
}
