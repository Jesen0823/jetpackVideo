package com.jesen.cod.libcommon.utils;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class StatusBarUtil {

    public static void fitSystemBar(Activity activity){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            return;
        }
        Window window = activity.getWindow();
        View decorView  = window.getDecorView();
        /*
        SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN,能够使得页面布局延伸到状态栏之下，但不会隐藏状态栏
        即状态栏遮盖在布局之上；
        SYSTEM_UI_FLAG_FULLSCREEN，能够使得页面布局延伸到状态栏之下，但状态栏隐藏,等价：
        WindowManager.LayoutParams.FLAG_FULLSCREEN
         */
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        // 允许window 对状态栏背景开启绘制
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);
    }

    public static void lightStatusBar(Activity activity, boolean light){
        if (Build.VERSION.SDK_INT<Build.VERSION_CODES.M){
            return;
        }
        Window window = activity.getWindow();
        View decorView = window.getDecorView();
        int visibility = decorView.getWindowSystemUiVisibility();
        if (light){ // 白底黑字
            visibility |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }else {
            visibility = visibility & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;

        }
        decorView.setSystemUiVisibility(visibility);
    }
}
