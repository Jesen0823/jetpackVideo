package com.jesen.cod.jetpackvideo.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.jesen.cod.libcommon.JetAppGlobal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtil {

    private static String ROOT_PATH;

    static {
        ROOT_PATH = JetAppGlobal.getApplication().getFilesDir().getAbsolutePath() + File.separator;
    }

    public static File checkFile(String fileName){
        File f = new File(ROOT_PATH);
        if (!f.exists()) {
            f.mkdir();
        }
        String apkPath = ROOT_PATH + fileName;
        Log.i("xxxxx", apkPath);
        File file = new File(apkPath);
        //创建apk文件
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    public static void writeStreamToFile(InputStream stream, File file) {
        try {
            OutputStream output = null;
            try {
                output = new FileOutputStream(file);
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            }
            try {
                try {
                    final byte[] buffer = new byte[1024];
                    int read;

                    while ((read = stream.read(buffer)) != -1)
                        output.write(buffer, 0, read);

                    output.flush();
                } finally {
                    output.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Uri getFileUri(Context context, File file){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);

        } else {
            return Uri.fromFile(file);
        }
    }

}
