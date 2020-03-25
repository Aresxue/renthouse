package com.asiainfo.frame.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * @author: Ares
 * @date: 2019/2/25 21:31
 * @description: 日期工具类
 * @version: JDK 1.8
 */
public class DateUtil
{
    public static final String DATE_FORMAT_A = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_FORMAT_B = "yyyy-MM-dd";
    public static final String DATE_FORMAT_C = "yyyy/MM/dd HH:mm:ss";
    public static final String DATE_FORMAT_D = "yyyy/MM/dd";
    public static final String DATE_FORMAT_E = "yyyyMMddHHmmss";
    public static final String DATE_FORMAT_F = "yyyyMMdd";
    public static final String DATE_FORMAT_G = "yyyyMM";

    public static final String T_VALUE = "T";
    public static final String TIME_SUFFIX = "+0000";

    /**
     * 存储格式的Map
     */
    private static final ThreadLocal<Map<String, SimpleDateFormat>> DATE_FORMAT_MAP = new ThreadLocal<Map<String, SimpleDateFormat>>()
    {
        @Override
        protected Map<String, SimpleDateFormat> initialValue()
        {
            return new HashMap<String, SimpleDateFormat>();
        }
    };

    private static SimpleDateFormat getFormat(final String pattern)
    {
        Map<String, SimpleDateFormat> dateFormatMap = DATE_FORMAT_MAP.get();
        SimpleDateFormat dateFormat = dateFormatMap.get(pattern);
        if (null == dateFormat)
        {
            if (StringUtil.isEmpty(pattern))
            {
                dateFormat = new SimpleDateFormat("yyyy-MM-dd'" + T_VALUE + "'HH:mm:ss.SSS'" + TIME_SUFFIX + "'");
                dateFormatMap.put(pattern, dateFormat);
            }
            else
            {
                dateFormat = new SimpleDateFormat(pattern);
                dateFormatMap.put(pattern, dateFormat);
            }
        }
        return dateFormat;
    }

    public static String format(Date date, String pattern)
    {
        return getFormat(pattern).format(date);
    }

    public static Date parse(String dateStr, String pattern) throws ParseException
    {
        return getFormat(pattern).parse(dateStr);
    }

    public static Date parse(String dateStr) throws ParseException
    {
        return getFormat("").parse(dateStr);
    }

    public static String getNextDayStart()
    {
        long nowTime = System.currentTimeMillis();
        long nextDayStartTime = nowTime - (nowTime + TimeZone.getDefault().getRawOffset()) % (1000 * 3600 * 24) + 1000 * 3600 * 24;
        String nextDayStart = getFormat(DATE_FORMAT_A).format(new Date(nextDayStartTime));
        return nextDayStart;
    }

    public static String getTodayEnd()
    {
        long nowTime = System.currentTimeMillis();
        long todayEndTime = nowTime - (nowTime + TimeZone.getDefault().getRawOffset()) % (1000 * 3600 * 24) + 1000 * 3600 * 24 - 1;
        String todayEnd = getFormat(DATE_FORMAT_A).format(new Date(todayEndTime));
        return todayEnd;
    }

    public static String getTodayStart()
    {
        long nowTime = System.currentTimeMillis();
        long todayStartTime = nowTime - (nowTime + TimeZone.getDefault().getRawOffset()) % (1000 * 3600 * 24);
        String todayStart = getFormat(DATE_FORMAT_A).format(new Date(todayStartTime));
        return todayStart;
    }
}
