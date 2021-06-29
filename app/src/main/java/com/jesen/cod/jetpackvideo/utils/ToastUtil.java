package com.jesen.cod.jetpackvideo.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.Toast;

import androidx.arch.core.executor.ArchTaskExecutor;

/**
 * Created by wondertek on 2019/11/18
 * e-mail : xie_stacol@163.com
 * desc   :
 * version: 1.0
 */
public class ToastUtil {

    private static Toast mToast;

    public static void show(Context context, String msg) {
        if (mToast == null) {
            mToast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        } else {
            mToast.setText(msg);
        }
        mToast.show();
    }

    @SuppressLint("RestrictedApi")
    public static void showOnUI(Context context, String msg){
        ArchTaskExecutor.getMainThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                show(context,msg);
            }
        });
    }
}
