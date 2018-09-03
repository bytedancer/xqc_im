package com.bonade.xxp.xqc_android_im.util;

import java.util.Calendar;
import java.util.Date;

public class DateUtil {

    public static boolean needDisplayTime(int predateTime, int curdateTime) {
        long timediff = (curdateTime - predateTime);
        return (timediff >= 5 * 60 );
    }

    public static String getTimeDiffDesc(Date date) {

        if (date == null) {
            return null;
        }

        String strDesc = null;
        Calendar curCalendar = Calendar.getInstance();
        Date curDate = new Date();
        curCalendar.setTime(curDate);
        Calendar thenCalendar = Calendar.getInstance();
        thenCalendar.setTime(date);

        String[] weekDays = {
                "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"
        };
        int w = thenCalendar.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0)
            w = 0;
        // SimpleDateFormat format = new
        // SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar current = Calendar.getInstance();
        Calendar today = Calendar.getInstance(); // 今天
        today.set(Calendar.YEAR, current.get(Calendar.YEAR));
        today.set(Calendar.MONTH, current.get(Calendar.MONTH));
        today.set(Calendar.DAY_OF_MONTH, current.get(Calendar.DAY_OF_MONTH));
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        // Date datetoday = today.getTime();
        // System.out.println(format.format(datetoday));

        Calendar yesterday = Calendar.getInstance(); // 昨天
        yesterday.setTime(curDate);
        yesterday.add(Calendar.DATE, -1);
        yesterday.set(Calendar.HOUR_OF_DAY, 0);
        yesterday.set(Calendar.MINUTE, 0);
        yesterday.set(Calendar.SECOND, 0);
        // Date dateyestoday = yesterday.getTime();
        // System.out.println(format.format(dateyestoday));

        Calendar sevendaysago = Calendar.getInstance(); // 7天
        sevendaysago.setTime(curDate);
        sevendaysago.add(Calendar.DATE, -7);
        sevendaysago.set(Calendar.HOUR_OF_DAY, 0);
        sevendaysago.set(Calendar.MINUTE, 0);
        sevendaysago.set(Calendar.SECOND, 0);
        // Date datesevenago = sevendaysago.getTime();
        // System.out.println(format.format(datesevenago));
        /*
         * Date tasktime = yesterday.getTime(); SimpleDateFormat df=new
         * SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
         * System.out.println(df.format(tasktime));
         */

        int thenMonth = thenCalendar.get(Calendar.MONTH);
        int thenDay = thenCalendar.get(Calendar.DAY_OF_MONTH);
        int h = thenCalendar.get(Calendar.HOUR_OF_DAY);
        int m = thenCalendar.get(Calendar.MINUTE);
        String sh = "", sm = "";
        if (h < 10)
            sh = "0";

        if (m < 10)
            sm = "0";
        if (thenCalendar.after(today))// today
        {
            if (h < 6) {
                strDesc = "凌晨 " + sh + h + ":" + sm + m;
            } else if (h < 12) {
                strDesc = "上午 " + sh + h + ":" + sm + m;
            } else if (h < 13) {
                strDesc = "下午 " + h + ":" + sm + m;
            } else if (h < 19) {
                strDesc = "下午 " + (h - 12) + ":" + sm + m;
            } else {
                strDesc = "晚上 " + (h - 12) + ":" + sm + m;
            }
        } else if (thenCalendar.before(today) && thenCalendar.after(yesterday)) {// yestoday
            // System.out.println("yestoday");
            if (h < 6) {
                strDesc = "昨天凌晨 " + sh + h + ":" + sm + m;
            } else if (h < 12) {
                strDesc = "昨天上午 " + sh + h + ":" + sm + m;
            } else if (h < 13) {
                strDesc = "昨天下午 " + h + ":" + sm + m;
            } else if (h < 19) {
                strDesc = "昨天下午 " + (h - 12) + ":" + sm + m;
            } else {
                strDesc = "昨天晚上 " + (h - 12) + ":" + sm + m;
            }
        } else if (thenCalendar.before(yesterday)
                && thenCalendar.after(sevendaysago)) {// 2 ~ 7days ago
            // System.out.println("2~7");
            if (h < 6) {
                strDesc = weekDays[w] + "凌晨 " + sh + h + ":" + sm + m;
            } else if (h < 12) {
                strDesc = weekDays[w] + "上午 " + sh + h + ":" + sm + m;
            } else if (h < 13) {
                strDesc = weekDays[w] + "下午 " + h + ":" + sm + m;
            } else if (h < 19) {
                strDesc = weekDays[w] + "下午 " + (h - 12) + ":" + sm + m;
            } else {
                strDesc = weekDays[w] + "晚上 " + (h - 12) + ":" + sm + m;
            }
        } else {
            // System.out.println("7~");
            if (h < 6) {
                strDesc = (thenMonth + 1) + "月" + thenDay + "日" + "凌晨 " + sh
                        + h + ":" + sm + m;
            } else if (h < 12) {
                strDesc = (thenMonth + 1) + "月" + thenDay + "日" + "上午 " + sh
                        + h + ":" + sm + m;
            } else if (h < 13) {
                strDesc = (thenMonth + 1) + "月" + thenDay + "日" + "下午 " + h
                        + ":" + sm + m;
            } else if (h < 19) {
                strDesc = (thenMonth + 1) + "月" + thenDay + "日" + "下午 "
                        + (h - 12) + ":" + sm + m;
            } else {
                strDesc = (thenMonth + 1) + "月" + thenDay + "日" + "晚上 "
                        + (h - 12) + ":" + sm + m;
            }
        }
        // System.out.println(strDesc);
        return strDesc;
    }
}
