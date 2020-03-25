package com.asiainfo.frame.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Ares
 * @date 2018/6/1 10:44
 */
public class StringUtil
{
    public static boolean isNotEmpty(String s)
    {
        return null != s && !s.isEmpty();
    }

    public static boolean isEmpty(String s)
    {
        return null == s || s.isEmpty();
    }

    /**
     * @author: Ares
     * @description: 下划线转大驼峰式
     * @date: 2019/6/13 17:31
     * @param: [source] 请求参数
     * @return: java.lang.String 响应参数
     */
    public static String underlineToBigCamelCase(String source)
    {
        String[] strs = source.split("_");
        StringBuffer stringBuffer = new StringBuffer();
        for (String s : strs)
        {
            stringBuffer.append(upperFirst(s.toLowerCase()));
        }
        return String.valueOf(stringBuffer);
    }

    /**
     * @author: Ares
     * @description: 中划线转大驼峰式
     * @date: 2020/3/24 13:34
     * @param: [source] 请求参数
     * @return: java.lang.String 响应参数
     */
    public static String strikeToBigCamelCase(String source)
    {
        String[] strs = source.split("-");
        StringBuffer stringBuffer = new StringBuffer();
        for (String s : strs)
        {
            stringBuffer.append(upperFirst(s.toLowerCase()));
        }
        return String.valueOf(stringBuffer);
    }

    /**
     * @author: Ares
     * @description: 中划线转小驼峰式
     * @date: 2019/6/13 17:31
     * @param: [source] 请求参数
     * @return: java.lang.String 响应参数
     */
    public static String strikeToLittleCamelCase(String source)
    {
        return lowerFirst(strikeToBigCamelCase(source));
    }

    /**
     * @author: Ares
     * @description: 下划线转小驼峰式
     * @date: 2019/6/13 17:31
     * @param: [source] 请求参数
     * @return: java.lang.String 响应参数
     */
    public static String underlineToLittleCamelCase(String source)
    {
        return lowerFirst(underlineToBigCamelCase(source));
    }

    /**
     * @author: Ares
     * @description: 首字母大写
     * @date: 2019/6/13 17:31
     * @param: [source] 请求参数
     * @return: java.lang.String 响应参数
     */
    public static String upperFirst(String source)
    {
        char[] chars = source.toCharArray();
        chars[0] = 97 <= chars[0] && chars[0] <= 122 ? (char) (chars[0] - 32) : chars[0];
        return String.valueOf(chars);
    }

    /**
     * @author: Ares
     * @description: 首字母小写
     * @date: 2019/6/13 17:33
     * @param: [source] 请求参数
     * @return: java.lang.String 响应参数
     */
    public static String lowerFirst(String source)
    {
        char[] chars = source.toCharArray();
        chars[0] = 65 <= chars[0] && chars[0] <= 90 ? (char) (chars[0] + 32) : chars[0];
        return String.valueOf(chars);
    }

    /**
     * @author: Ares
     * @description: 校验邮箱格式
     * @date: 2019/11/2 16:43
     * @param: [email] 请求参数
     * @return: boolean 响应参数
     */
    public static boolean validateEmailFromat(String email)
    {
        if (StringUtil.isEmpty(email))
        {
            return false;
        }
        String regex = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}
