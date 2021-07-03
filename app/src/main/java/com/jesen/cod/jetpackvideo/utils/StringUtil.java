package com.jesen.cod.jetpackvideo.utils;

public class StringUtil {

    public static String subBeforeLastString(String src, String part) {
        String substring = src.substring(0,src.lastIndexOf(part));
        return substring;
    }

    public static String subBeforeString(String src, String part) {
        if (src.contains(part)){
            String[] s = src.split(part);
            return s[0];
        }
        return "";
    }

}
