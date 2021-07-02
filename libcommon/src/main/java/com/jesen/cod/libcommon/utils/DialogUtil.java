package com.jesen.cod.libcommon.utils;

import android.app.Activity;
import android.os.Looper;

import com.jesen.cod.libcommon.R;
import com.jesen.cod.libcommon.dialog.LoadingDialog;

public class DialogUtil {

    private static LoadingDialog mLoadingDialog = null;

    public static void showHideLoading(Activity activity, boolean show, String text) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            if (show) {
                showLoading(activity, text);
            } else {
                dismissLoading();
            }
        } else {
            activity.runOnUiThread(() -> {
                if (show) {
                    showLoading(activity, text);
                } else {
                    dismissLoading();
                }
            });
        }
    }

    private static void showLoading(Activity activity, String text) {
        if (mLoadingDialog == null) {
            mLoadingDialog = new LoadingDialog(activity);
            mLoadingDialog.setLoadingText(text);
        }
        mLoadingDialog.show();
    }

    private static void dismissLoading() {
        if (mLoadingDialog != null) {
            mLoadingDialog.dismiss();
        }
    }
}
