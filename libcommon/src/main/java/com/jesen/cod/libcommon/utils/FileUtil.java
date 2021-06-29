package com.jesen.cod.libcommon.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.core.content.FileProvider;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jesen.cod.libcommon.JetAppGlobal;

import java.io.ByteArrayOutputStream;
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

    public static File checkFile(String fileName) {
        File f = new File(ROOT_PATH);
        if (!f.exists()) {
            f.mkdir();
        }
        String apkPath = ROOT_PATH + fileName;
        Log.i("xxxxx", apkPath);
        File file = new File(apkPath);
        //创建文件
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

    public static Uri getFileUri(Context context, File file) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);

        } else {
            return Uri.fromFile(file);
        }
    }

    @SuppressLint("RestrictedApi")
    public static LiveData<String> generateVideoCover(String videoPath) {
        MutableLiveData<String> liveData = new MutableLiveData<>();
        ArchTaskExecutor.getIOThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(videoPath);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    Bitmap frame = retriever.getFrameAtIndex(-1);
                    if (frame != null) {
                        byte[] bytes = compressBitmap(frame, 200);
                        File file = new File(Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_DCIM), System.currentTimeMillis() + ".jpeg");
                        try {
                            file.createNewFile();
                            FileOutputStream fos = new FileOutputStream(file);
                            fos.write(bytes);
                            fos.flush();
                            fos.close();
                            fos = null;
                            liveData.postValue(file.getAbsolutePath());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        liveData.postValue(null);
                    }
                }
            }
        });
        return liveData;
    }

    private static byte[] compressBitmap(Bitmap bitmap, int limitSize) {
        if (bitmap != null && limitSize > 0) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int options = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
            while (baos.toByteArray().length > limitSize * 1024) {
                baos.reset();
                options -= 5;
                bitmap.compress(Bitmap.CompressFormat.JPEG, options, baos);
            }
            byte[] bytes = baos.toByteArray();
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                baos = null;
            }
            return bytes;
        }
        return new byte[0];
    }

}
