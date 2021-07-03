package com.jesen.cod.jetpackvideo.album;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.jesen.cod.jetpackvideo.model.MediaData;
import com.jesen.cod.jetpackvideo.utils.StringUtil;
import com.jesen.cod.libcommon.utils.Og;

public class SystemAlbumHelper {

    private static final String TAG = "SystemAlbumHelper";
    public static final int REQ_OPEN_ALBUM_PHOTO_CODE = 1004;
    public static final int REQ_OPEN_ALBUM_VIDEO_CODE = 1005;


    public static void openSystemLocalPhoto(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        activity.startActivityForResult(intent, REQ_OPEN_ALBUM_PHOTO_CODE);
    }

    public static void openSystemLocalVideo(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        intent.setType("video/*");
        activity.startActivityForResult(intent, REQ_OPEN_ALBUM_VIDEO_CODE);
    }

    public static MediaData handleSystemAlbumResult(Context context, Intent data, int codeType) {
        Uri selectUri = data.getData();
        MediaData mediaData = new MediaData();
        Cursor cursor = null;
        String[] filePathColumn = null;

        if (codeType == REQ_OPEN_ALBUM_PHOTO_CODE) {
            filePathColumn = new String[]{
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.MIME_TYPE,
                    MediaStore.Images.Media.WIDTH,
                    MediaStore.Images.Media.HEIGHT,
            };
        } else if (codeType == REQ_OPEN_ALBUM_VIDEO_CODE) {
            filePathColumn = new String[]{
                    MediaStore.Video.Media.DATA,
                    MediaStore.Video.Media.MIME_TYPE, // image/jpeg
                    MediaStore.Video.Media.RESOLUTION
            };
        }

        cursor = context.getContentResolver().query(selectUri, filePathColumn, null, null, null);
        if (cursor != null) {
            //cursor.moveToFirst();
            while (cursor.moveToNext()) {
                int pathIndex = cursor.getColumnIndex(filePathColumn[0]);
                int mimeIndex = cursor.getColumnIndex(filePathColumn[1]);
                String mimeType = cursor.getString(mimeIndex);
                Og.d(TAG + ", handleSystemAlbumResult, mimeType: " + mimeType);

                mediaData.setFilePath(cursor.getString(pathIndex));
                if (!TextUtils.isEmpty(mimeType)) {
                    mediaData.setMimeType(StringUtil.subBeforeString(mimeType, "/"));
                }
                if (codeType == REQ_OPEN_ALBUM_PHOTO_CODE) {
                    int widthIndex = cursor.getColumnIndex(filePathColumn[2]);
                    int heightIndex = cursor.getColumnIndex(filePathColumn[3]);
                    mediaData.setWidth(cursor.getInt(widthIndex));
                    mediaData.setHeight(cursor.getInt(heightIndex));
                } else if (codeType == REQ_OPEN_ALBUM_VIDEO_CODE) {
                    int resIndex = cursor.getColumnIndex(filePathColumn[2]);
                    String resolution = cursor.getString(resIndex);
                    Og.d(TAG + ", handleSystemAlbumResult, resolution: " + resolution);
                    mediaData.setResolution(resolution);
                }
            }
        }
        return mediaData;
    }

}
