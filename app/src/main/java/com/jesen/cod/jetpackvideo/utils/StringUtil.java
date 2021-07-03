package com.jesen.cod.jetpackvideo.utils;

import androidx.annotation.StringRes;

import com.jesen.cod.libcommon.JetAppGlobal;

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

    public static String getStringRes(@StringRes int resId, int num){
        return String.format(JetAppGlobal.getApplication().getResources().getString(resId),num);
    }

    public static String getString(String resStr, int num){
        return String.format(resStr,num);
    }
}
