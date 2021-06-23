package com.jesen.cod.libcommon.utils;

import android.util.DisplayMetrics;

import com.jesen.cod.libcommon.JetAppGlobal;

public class PixUtils {

    public static int dp2px(int dpValue) {
        DisplayMetrics metrics = JetAppGlobal.getApplication().getResources().getDisplayMetrics();
        return (int) (metrics.density * dpValue + 0.5f);
    }

    public static int getScreenWidth() {
        DisplayMetrics metrics = JetAppGlobal.getApplication().getResources().getDisplayMetrics();
        return metrics.widthPixels;
    }

    public static int getScreenHeight() {
        DisplayMetrics metrics = JetAppGlobal.getApplication().getResources().getDisplayMetrics();
        return metrics.heightPixels;
    }
}
