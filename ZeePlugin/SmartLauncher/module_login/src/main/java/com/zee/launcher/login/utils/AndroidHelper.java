package com.zee.launcher.login.utils;

import android.annotation.SuppressLint;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AndroidHelper {
    // 手机号段资料：https://www.qqzeng.com/article/phone.html
    public static boolean isPhone(String phone) {
        String regex = "^((13[0-9])|(15([0-3]|[5-9]))|(16[6])|(17([2-3]|[5-9]))|(18[0-9])|(19([1-4]|[5-9])))\\d{8}$";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(phone).matches();
    }

    public static boolean isNumeric(String str){
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        return isNum.matches();
    }

    public static Date str2Date(String dateStr) {
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date(0);
        try {
            date = simpleDateFormat.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    // ^(?![A-Z]+$)(?![a-z]+$)(?!\d+$)(?![\W_]+$)\S{8,16}$
    // "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])[\\da-zA-Z]{8,16}$"
    public static boolean isCompliantPassword(String password) {
        String regex = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])[\\da-zA-Z]{8,16}$";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(password).matches();
    }
}
