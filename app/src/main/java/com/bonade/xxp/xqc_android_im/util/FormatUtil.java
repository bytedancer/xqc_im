package com.bonade.xxp.xqc_android_im.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FormatUtil {

    private static final SimpleDateFormat sFormat = new SimpleDateFormat();
    public static final String DATE_MD = "MM月dd日";
    public static final String DATE_YMD = "yyyy年MM月dd日";
    public static final String FORMAT_ALL_DATE = "yyyy-MM-dd HH:mm:ss";
    public static final String FORMAT_DATE_WITHOUT_SS = "yyyy-MM-dd HH:mm";
    public static final String FORMAT_DATE_MM = "MM-dd HH:mm";

    public static final String dateFormatAll(long date) {
        sFormat.applyPattern(FORMAT_ALL_DATE);
        return sFormat.format(new Date(date));
    }

    public static final String dateFormatYMD(long date) {
        sFormat.applyPattern(DATE_YMD);
        return sFormat.format(new Date(date));
    }
    public static final String dateFormatMD(long date) {
        sFormat.applyPattern(DATE_MD);
        return sFormat.format(new Date(date));
    }
    public static final String dateFormatMM(Date date) {
        sFormat.applyPattern(FORMAT_DATE_MM);
        return sFormat.format(date);
    }

    public static final String dateFormatMM(long date) {
        sFormat.applyPattern(FORMAT_DATE_MM);
        return sFormat.format(date);
    }

    public static final String dateFormatWithoutSS(Date date) {
        sFormat.applyPattern(FORMAT_DATE_WITHOUT_SS);
        return sFormat.format(date);
    }

    public static final String dateFormatWithoutSS(long date) {
        sFormat.applyPattern(FORMAT_DATE_WITHOUT_SS);
        return sFormat.format(date);
    }

    public static Date getTheDate(String time, String formate) throws ParseException {
        sFormat.applyPattern(formate);
        return sFormat.parse(time);
    }
}
