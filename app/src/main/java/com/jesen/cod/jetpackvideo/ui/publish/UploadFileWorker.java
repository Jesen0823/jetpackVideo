package com.jesen.cod.jetpackvideo.ui.publish;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.jesen.cod.libcommon.utils.FileUploadManager;

public class UploadFileWorker extends Worker {

    public UploadFileWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Data inputData = getInputData();
        String uploadPath = inputData.getString(PublishActivity.PRE_UPLOAD_FILE_PATH);
        String uploadUrl = FileUploadManager.upload(uploadPath);
        if (TextUtils.isEmpty(uploadUrl)) {
            return Result.failure();
        } else {
            Data outPutData = new Data.Builder()
                    .putString(PublishActivity.AFT_UPLOAD_DONE_URL, uploadUrl)
                    .build();
            return Result.success(outPutData);
        }
    }
}
