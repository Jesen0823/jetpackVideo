package com.jesen.cod.jetpackvideo.ui.publish;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.Observer;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;

import com.alibaba.fastjson.JSONObject;
import com.jesen.cod.jetpackvideo.R;
import com.jesen.cod.jetpackvideo.album.SystemAlbumHelper;
import com.jesen.cod.jetpackvideo.databinding.ActivityPublishBinding;
import com.jesen.cod.jetpackvideo.model.Feed;
import com.jesen.cod.jetpackvideo.model.MediaData;
import com.jesen.cod.jetpackvideo.model.TagList;
import com.jesen.cod.jetpackvideo.ui.login.UserManager;
import com.jesen.cod.jetpackvideo.utils.TimeUtils;
import com.jesen.cod.jetpackvideo.utils.ToastUtil;
import com.jesen.cod.libcommon.utils.DialogUtil;
import com.jesen.cod.libcommon.utils.FileUtil;
import com.jesen.cod.libcommon.utils.Og;
import com.jesen.cod.libnavannotation.ActivityDestination;
import com.jesen.cod.libnetwork.ApiResponse;
import com.jesen.cod.libnetwork.ApiService;
import com.jesen.cod.libnetwork.JsonCallback;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@ActivityDestination(pageUrl = "main/tabs/publish", needLogin = true)
public class PublishActivity extends AppCompatActivity implements View.OnClickListener {

    private ActivityPublishBinding mBinding;

    private int fileWidth, fileHeight;
    private String filePath = "";
    private String coverFilePath;
    private boolean fileIsVideo;
    private UUID coverUuid;
    private UUID fileUuid;
    private TagList mTagList = null;

    private static final String TAG = "PublishActivity";
    private static final String URL_PUBLISH_TOPIC = "/feeds/publish";
    protected static final String PRE_UPLOAD_FILE_PATH = "pre_upload_file_path";
    protected static final String AFT_UPLOAD_DONE_URL = "aft_upload_done_url";

    // 上传完成得到的视频封面和文件地址
    private String upDoneCoverUrl = "";
    private String upDoneFileUrl = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_publish);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_publish);

        mBinding.closeBtn.setOnClickListener(this);
        mBinding.publishBtn.setOnClickListener(this::onClick);
        mBinding.deleteFileBtn.setOnClickListener(this::onClick);
        mBinding.addTagBtn.setOnClickListener(this::onClick);
        mBinding.addFileBtn.setOnClickListener(this::onClick);
        mBinding.addAlbumVideoBtn.setOnClickListener(this::onClick);
        mBinding.addAlbumPhotoBtn.setOnClickListener(this::onClick);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.action_close:
                showExitDialog();
                break;
            case R.id.delete_file_btn:
                clearAllFile();
                break;
            case R.id.publish_btn:
                publishTopic();
                break;
            case R.id.add_tag_btn:
                showTagBottomDialog();
                break;
            case R.id.add_file_btn:
                CaptureActivity.startActivityForResult(this);
                break;
            case R.id.add_album_video_btn:
                SystemAlbumHelper.openSystemLocalVideo(this);
                break;
            case R.id.add_album_photo_btn:
                SystemAlbumHelper.openSystemLocalPhoto(this);
                break;
            default:
        }
    }

    private void publishTopic() {
        Og.d(TAG + ", publishTopic, filePath: " + filePath + ", isVideo: " + fileIsVideo);
        DialogUtil.showHideLoading(PublishActivity.this, true, getString(R.string.feed_publish_ing));
        List<OneTimeWorkRequest> workRequests = new ArrayList<>();
        if (!TextUtils.isEmpty(filePath)) {
            // 如果是视频，先上传封面
            if (fileIsVideo) {
                FileUtil.generateVideoCover(filePath).observe(this, new Observer<String>() {
                    @SuppressLint("RestrictedApi")
                    @Override
                    public void onChanged(String coverPath) {
                        coverFilePath = coverPath;
                        Og.d(TAG + ", publishTopic, onChanged, coverPath:" + coverPath);
                        OneTimeWorkRequest coverRequest = getOneTimeWorkRequest(coverPath);
                        coverUuid = coverRequest.getId();
                        workRequests.add(coverRequest);

                        enqueueWork(workRequests);
                    }
                });
            }
            // 再上传文件，视频或照片
            OneTimeWorkRequest fileRequest = getOneTimeWorkRequest(filePath);
            fileUuid = fileRequest.getId();
            workRequests.add(fileRequest);

            if (!fileIsVideo) {
                enqueueWork(workRequests);
            }

        } else {
            // 只有文字内容
            publishToServer();
        }
    }

    private void enqueueWork(List<OneTimeWorkRequest> workRequests) {
        WorkContinuation workContinuation = WorkManager.getInstance(
                PublishActivity.this).beginWith(workRequests);
        // 加入队列
        workContinuation.enqueue();
        workContinuation.getWorkInfosLiveData().observe(PublishActivity.this, new Observer<List<WorkInfo>>() {
            @Override
            public void onChanged(List<WorkInfo> workInfos) {
                // block, running, enquaqed, failed, success, finish
                int completedCount = 0;
                int failedCount = 0;
                for (WorkInfo workInfo : workInfos) {
                    WorkInfo.State state = workInfo.getState();
                    Data outputData = workInfo.getOutputData();
                    UUID uuid = workInfo.getId();

                    if (state == WorkInfo.State.FAILED) {
                        Og.d(TAG + ", enqueueWork, failed uuid:" + uuid);
                        if (uuid.equals(coverUuid)) {
                            ToastUtil.showOnUI(PublishActivity.this, getString(R.string.file_upload_cover_message));
                        } else if (uuid.equals(fileUuid)) {
                            ToastUtil.showOnUI(PublishActivity.this, getString(R.string.file_upload_original_message));
                        }
                        failedCount++;
                    } else if (state == WorkInfo.State.SUCCEEDED) {
                        String getUrl = outputData.getString(AFT_UPLOAD_DONE_URL);
                        Og.d(TAG + ", enqueueWork, success uuid:" + uuid);
                        if (uuid.equals(coverUuid)) {
                            upDoneCoverUrl = getUrl;
                        } else if (uuid.equals(fileUuid)) {
                            upDoneFileUrl = getUrl;
                        }
                        completedCount++;
                    }
                }
                Og.d(TAG + ", enqueueWork, completedCount: " + completedCount);
                Og.d(TAG + ", enqueueWork, taskSize: " + workInfos.size());

                if (completedCount >= workInfos.size()) {
                    publishToServer();
                } else if (failedCount > 0) {
                    DialogUtil.showHideLoading(PublishActivity.this, false, null);
                }
            }
        });
    }

    private void publishToServer() {

        Og.d(TAG + ", publishToServer, upDoneCoverUrl: " + upDoneCoverUrl);
        Og.d(TAG + ", publishToServer, isVideo: " + fileIsVideo);
        Og.d(TAG + ", publishToServer, upDoneFileUrl: " + upDoneFileUrl);
        Og.d(TAG + ", publishToServer, fileWidth: " + fileWidth + ", fileHeight: " + fileHeight);

        if (!fileIsVideo && !TextUtils.isEmpty(upDoneCoverUrl)) {
            upDoneFileUrl = upDoneCoverUrl;
            upDoneCoverUrl = "";
        }

        ApiService.post(URL_PUBLISH_TOPIC)
                .addParams("coverUrl", upDoneCoverUrl)
                .addParams("fileUrl", upDoneFileUrl)
                .addParams("fileWidth", fileWidth)
                .addParams("fileHeight", fileHeight)
                .addParams("userId", UserManager.get().getUserId())
                .addParams("tagId", mTagList == null ? 0 : mTagList.tagId)
                .addParams("tagTitle", mTagList == null ? "" : mTagList.title)
                .addParams("feedText", mBinding.inputEditView.getText().toString())
                .addParams("feedType", fileIsVideo ? Feed.TYPE_VIDEO : Feed.TYPE_IMAGE_TEXT)
                .execute(new JsonCallback<JSONObject>() {
                    @Override
                    public void onSuccess(ApiResponse<JSONObject> response) {
                        ToastUtil.showOnUI(PublishActivity.this, getString(R.string.feed_publisj_success));
                        PublishActivity.this.finish();
                        DialogUtil.showHideLoading(PublishActivity.this, false, null);
                    }

                    @Override
                    public void onError(ApiResponse<JSONObject> response) {
                        ToastUtil.showOnUI(PublishActivity.this, response.message);
                        DialogUtil.showHideLoading(PublishActivity.this, false, null);
                    }
                });
    }

    @NotNull
    @SuppressLint("RestrictedApi")
    private OneTimeWorkRequest getOneTimeWorkRequest(String coverPath) {
        Data inputData = new Data.Builder()
                .putString(PRE_UPLOAD_FILE_PATH, coverPath)
                .build();

       /* // 任务执行的条件约束
        Constraints constraints = new Constraints();
        // 设备存储空间充足时执行，剩余空间> 15%
        constraints.setRequiresStorageNotLow(true);
        // 在指定的网络条件下执行
        constraints.setRequiredNetworkType(NetworkType.UNMETERED); // 不计流量
        // 电量充足 > 15%
        constraints.setRequiresBatteryNotLow(true);
        // 只有在设备充电的情况下才执行
        constraints.setRequiresCharging(true);
        // 只有在设备空闲的情况下才执行
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            constraints.setRequiresDeviceIdle(true);
        }
        // workManager利用contentObserver监控传递进来的uri对应的内容是否是发送变化
        // 当且仅当它发生变化时任务才执行
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            constraints.setContentUriTriggers(null);
        }
        // 从content变化到被执行的延迟时间，期间content发生变化则延迟时间重新计算
        constraints.setTriggerContentUpdateDelay(0L);
        // 从content变化到被执行，这中间最大的延迟时间
        constraints.setTriggerMaxContentDelay(0L);
*/
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(UploadFileWorker.class)
                .setInputData(inputData)
                /*.setConstraints(constraints)
                // 设置拦截器，任务执行之前做拦截，修改入参数据返回新数据交给work
                .setInputMerger(null)
                // 当一个任务调度失败后的重试策略，可通过BackOffPolicy执行具体的策略
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
                // 任务被调度执行的延迟时间
                .setInitialDelay(10, TimeUnit.SECONDS)
                // 任务失败后最大可重试次数
                .setInitialRunAttemptCount(3)
                // 设置任务开始执行的时间
                .setPeriodStartTime(0, TimeUnit.SECONDS)
                // 指定该任务被调度的时间
                .setScheduleRequestedAt(0, TimeUnit.SECONDS)
                // 当一个任务执行状态编程finish时，又没有后续的观察者来消费这个结果，难么workamnager会在
                // 内存中保留一段时间的该任务的结果。超过这个时间，这个结果就会被存储到数据库中
                // 下次想要查询该任务的结果时，会触发workManager的数据库查询操作，可以通过uuid来查询任务的状态
                .keepResultsForAtLeast(10, TimeUnit.SECONDS)*/
                .build();
        coverUuid = request.getId();
        return request;
    }

    private void clearAllFile() {
        mBinding.addFileBtn.setVisibility(View.VISIBLE);
        mBinding.addAlbumVideoBtn.setVisibility(View.VISIBLE);
        mBinding.addAlbumPhotoBtn.setVisibility(View.VISIBLE);
        mBinding.fileContainerLayout.setVisibility(View.GONE);
        mBinding.coverImage.setImageUrl(null);
        filePath = null;
        fileHeight = fileWidth = 0;
        fileIsVideo = false;
    }

    private void showTagBottomDialog() {
        TagBottomSheetDialog sheetDialog = new TagBottomSheetDialog();
        sheetDialog.setTagItemSelectListener(new TagBottomSheetDialog.OnTagItemSelectListener() {
            @Override
            public void onSelectTag(TagList tagList) {
                mTagList = tagList;
                mBinding.addTagBtn.setText(tagList.title);
            }
        });

        sheetDialog.show(getSupportFragmentManager(), "tag_dialog");
    }

    private void showExitDialog() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.publish_exit_message)
                .setNegativeButton(R.string.publish_exit_action_cancel, null)
                .setPositiveButton(R.string.publish_exit_action_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        PublishActivity.this.finish();
                    }
                }).create().show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CaptureActivity.REQ_CODE_TO_CAPTURE && resultCode == RESULT_OK && data != null) {
            fileWidth = data.getIntExtra(CaptureActivity.PREVIEW_RESULT_FILE_WIDTH, 0);
            fileHeight = data.getIntExtra(CaptureActivity.PREVIEW_RESULT_FILE_HEIGHT, 0);
            filePath = data.getStringExtra(CaptureActivity.PREVIEW_RESULT_FILE_PATH);
            fileIsVideo = data.getBooleanExtra(CaptureActivity.PREVIEW_RESULT_FILE_TYPE, false);

            showFileThumbnail();
        } else if ((requestCode == SystemAlbumHelper.REQ_OPEN_ALBUM_PHOTO_CODE
                || requestCode == SystemAlbumHelper.REQ_OPEN_ALBUM_VIDEO_CODE)
                && resultCode == RESULT_OK && data != null) {

            MediaData mediaData = SystemAlbumHelper.handleSystemAlbumResult(this, data, requestCode);
            if (!TextUtils.isEmpty(mediaData.getMimeType())) {
                filePath = mediaData.getFilePath();
                if (TextUtils.equals(mediaData.getMimeType(), "video")) {
                    fileIsVideo = true;
                    if (!TextUtils.isEmpty(mediaData.getResolution())) {
                        String[] xes = mediaData.getResolution().split("x");
                        fileWidth = Integer.parseInt(xes[0]);
                        fileHeight = Integer.parseInt(xes[1]);
                    }
                } else {
                    fileIsVideo = false;
                    fileWidth = mediaData.getWidth();
                    fileHeight = mediaData.getHeight();
                }
                showFileThumbnail();
            }
        }
    }

    private void showFileThumbnail() {
        Og.d(TAG + String.format("showFileThumbnail, \n fileIsVideo: %s, path is: %s, width: %d,"
                + " height : %d", fileIsVideo, filePath, fileWidth, fileHeight));

        if (TextUtils.isEmpty(filePath)) {
            return;
        }
        mBinding.addFileBtn.setVisibility(View.GONE);
        mBinding.addAlbumVideoBtn.setVisibility(View.GONE);
        mBinding.addAlbumPhotoBtn.setVisibility(View.GONE);
        mBinding.fileContainerLayout.setVisibility(View.VISIBLE);
        mBinding.coverImage.setImageUrl(filePath);
        mBinding.videoIcon.setVisibility(fileIsVideo ? View.VISIBLE : View.GONE);
        mBinding.coverImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PreviewActivity.startActivityForResult(PublishActivity.this, filePath,
                        fileIsVideo, null);
            }
        });
    }
}