package com.zwn.user.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.text.SpannableString;
import android.text.style.ImageSpan;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.zwn.user.R;
import com.zwn.user.widget.CenterAlignImageSpan;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.SimpleFormatter;
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

    public static SpannableString getUserCenterEditTips(Activity activity) {
        int menuSize = activity.getResources().getDimensionPixelSize(R.dimen.src_dp_28);
        Drawable menuIcon = ContextCompat.getDrawable(activity, R.mipmap.icon_menu);
        SpannableString spannableString = new SpannableString("按[icon]键编辑");
        assert menuIcon != null;
        menuIcon.setBounds(0, 0, menuSize, menuSize);
        CenterAlignImageSpan imgSpan = new CenterAlignImageSpan(menuIcon, ImageSpan.ALIGN_BASELINE);
        spannableString.setSpan(imgSpan, 1, 7, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

    public static int fitScreen(int height, Activity activity) {
        int screenHeight = activity.getResources()
                .getDisplayMetrics()
                .heightPixels;
        return (int) ((screenHeight / 1080.f) * height);
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
