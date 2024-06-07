package com.zeewain.base.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateTimeUtils {

    public static String formatDateToString(Date date) {
        return formatDateToString(date, null);
    }

    public static String formatDateToString(Date date, String format) {
        SimpleDateFormat formatter;
        if(format == null)
            formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        else
            formatter = new SimpleDateFormat(format, Locale.CHINA);
        String dateString = formatter.format(date);
        return dateString;
    }

    public static Date formatStringToDate(String dateTimeString, String format) {
        SimpleDateFormat formatter;
        if(format == null)
            formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        else
            formatter = new SimpleDateFormat(format, Locale.CHINA);

        Date date = null;
        try {
            date = formatter.parse(dateTimeString);
        } catch (ParseException ignored) {}
        return date;
    }


    public static String formatToTimeString(long millionSeconds) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millionSeconds);
        return simpleDateFormat.format(c.getTime());
    }
}
