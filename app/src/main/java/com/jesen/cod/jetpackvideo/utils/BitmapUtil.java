package com.jesen.cod.jetpackvideo.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class BitmapUtil {
    private static final String TAG = "BitmapUtil";
    private static final int BYTE_SIZE = 1024;


    /**
     * 获取网络图片
     *
     * @param url 网络图片地址
     * @return bitmap
     */
    public static Bitmap loadUrlToBitmap(String url, int width, int height) {
        Bitmap bitmap = null;
        try {
            URL imageUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = getBitmapFromStream(width, height, is);
        } catch (IOException e) {
            Log.e(TAG, "Exception ", e);
        }
        return bitmap;
    }

    /**
     * 从资源文件中获取Bitmap
     *
     * @param reqWidth  要求的宽度
     * @param reqHeight 要求的高度
     * @param resId     资源文件Id
     * @return 图片BitMap
     **/
    public static Bitmap getBitmapFromResource(Context context, int reqWidth, int reqHeight, int resId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), resId, options);
        options.inJustDecodeBounds = false;
        options.inSampleSize = calculateInSampleSize(reqWidth, reqHeight, options);
        return BitmapFactory.decodeResource(context.getResources(), resId, options);
    }

    public static Bitmap getBitmapFromResource(Context context, int resId) {
        return BitmapFactory.decodeResource(context.getResources(), resId);
    }

    /**
     * 从inputStream中获取Bitmap
     *
     * @param reqWidth    要求的宽度
     * @param reqHeight   要求的高度
     * @param inputStream 流
     * @return 图片BitMap
     **/
    public static Bitmap getBitmapFromStream(int reqWidth, int reqHeight, InputStream inputStream) throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        byte[] bytes = readStream(inputStream);
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        options.inJustDecodeBounds = false;
        options.inSampleSize = calculateInSampleSize(reqWidth, reqHeight, options);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
    }

    public static int calculateInSampleSize(int reqWidth, int reqHeight, BitmapFactory.Options options) {
        int inSampleSize = 1;
        if (options.outWidth > reqWidth || options.outHeight > reqHeight) {
            int widthRatio = Math.round((float) options.outWidth / (float) reqWidth);
            int heightRatio = Math.round((float) options.outHeight / (float) reqHeight);
            inSampleSize = Math.min(widthRatio, heightRatio);
        }
        return inSampleSize;
    }


    /**
     * 重新设置Logo bitMap的大小
     *
     * @param bitmap    需要重设的bitMap
     * @param newWidth  bitMap新的宽度
     * @param newHeight bitMap新的高度
     * @return 新的bitMap
     */
    public static Bitmap imageScale(Bitmap bitmap, int newWidth, int newHeight) {
        Bitmap newBitmap = null;
        if (bitmap == null) {
            return null;
        }
        int oldWidth = bitmap.getWidth();
        int oldHeight = bitmap.getHeight();
        float scaleForWidth = ((float) newWidth) / oldWidth;
        float scaleForHeight = ((float) newHeight) / oldHeight;
        Matrix mMatrix = new Matrix();
        mMatrix.postScale(scaleForWidth, scaleForHeight);
        newBitmap = Bitmap.createBitmap(bitmap, 0, 0, oldWidth, oldHeight, mMatrix, true);
        bitmap.recycle();
        return newBitmap;
    }

    /**
     * 从inputStream中获取字节流 数组大小
     *
     * @param inStream 输入流
     **/
    private static byte[] readStream(InputStream inStream) throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[BYTE_SIZE];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        outStream.close();
        inStream.close();
        return outStream.toByteArray();
    }

    /**
     * 清空bitMap
     *
     * @param bitmap bitmap
     */
    public static void recycleBitMap(Bitmap bitmap) {
        if (null != bitmap && !bitmap.isRecycled()) {
            Log.i(TAG, "bitMap recycle " + bitmap);
            bitmap = null;
        }
    }

    public static byte[] Bitmap2Bytes(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }

    public static Bitmap byteArray2Bitmap(byte[] b) {
        if (b != null) {
            return BitmapFactory.decodeByteArray(b, 0, b.length);
        }
        return null;
    }

    public static InputStream byteArray2InputStream(byte[] bytes) {
        return new ByteArrayInputStream(bytes);
    }

    // bitmap -> byte[] -> InputStream
    public static InputStream bitmap2InputStream(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        InputStream is = new ByteArrayInputStream(baos.toByteArray());
        return is;
    }

    /**
     * inputstream 转 bitmap
     */
    public static Bitmap inputStream2Bitmap(InputStream is) {
        Bitmap bitmap = BitmapFactory.decodeStream(is);
        return bitmap;
    }

    /**
     * Drawable 转 bitmap
     */
    public static Bitmap drawable2Bitmap(Drawable img) {
        BitmapDrawable bd = (BitmapDrawable) img;
        Bitmap bitmap = bd.getBitmap();
        return bitmap;
    }

    /**
     * bitmap 转 Drawable
     */
    public static Drawable bitmap2Drawable(Context context, Bitmap bitmap) {
        BitmapDrawable bd = new BitmapDrawable(context.getResources(), bitmap);
        Drawable img = bd;
        return img;
    }
}
