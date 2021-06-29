package com.jesen.cod.jetpackvideo.ui.detail;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;

import com.jesen.cod.jetpackvideo.R;
import com.jesen.cod.jetpackvideo.databinding.LayoutDialogCommentBinding;
import com.jesen.cod.jetpackvideo.model.Comment;
import com.jesen.cod.jetpackvideo.ui.login.UserManager;
import com.jesen.cod.jetpackvideo.ui.publish.CaptureActivity;
import com.jesen.cod.libcommon.utils.FileUploadManager;
import com.jesen.cod.libcommon.utils.FileUtil;
import com.jesen.cod.jetpackvideo.utils.ToastUtil;
import com.jesen.cod.libcommon.JetAppGlobal;
import com.jesen.cod.libcommon.dialog.LoadingDialog;
import com.jesen.cod.libnetwork.ApiResponse;
import com.jesen.cod.libnetwork.ApiService;
import com.jesen.cod.libnetwork.JsonCallback;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

public class CommentDialog extends DialogFragment implements View.OnClickListener {

    private static final String URL_COMMENT = "/comment/addComment";
    private static final String KEY_ITEM_ID = "key_item_id";

    private LayoutDialogCommentBinding mBinding;

    private CommentAddResultListener mCommentResultListener;

    private long mItemId;

    // 拍摄所得数据的参数
    private String filePath;
    private int fileWidth;
    private int fileHeight;
    private boolean fileIsVideo;
    private LoadingDialog loadingDialog;

    // 文件上传到服务器的路径
    private String fileUrl;
    private String coverUrl;

    public static CommentDialog getInstance(long itemId) {
        Bundle args = new Bundle();
        args.putLong(KEY_ITEM_ID, itemId);
        CommentDialog dialog = new CommentDialog();
        dialog.setArguments(args);
        return dialog;
    }

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        mBinding = LayoutDialogCommentBinding.inflate(inflater, container, false);
        mBinding.commentVideo.setOnClickListener(this);
        mBinding.commentDelete.setOnClickListener(this);
        mBinding.commentSend.setOnClickListener(this);

        Window window = getDialog().getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

        this.mItemId = getArguments().getLong(KEY_ITEM_ID, 0L);

        return mBinding.getRoot();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.comment_send:
                publishComment();
                break;
            case R.id.comment_video:
                CaptureActivity.startActivityForResult(getActivity());
                break;
            case R.id.comment_delete:
                filePath = null;
                fileIsVideo = false;
                fileWidth = 0;
                fileHeight = 0;
                mBinding.commentCover.setImageUrl(null);
                mBinding.commentExtLayout.setVisibility(View.GONE);
                mBinding.commentVideo.setEnabled(false);
                mBinding.commentVideo.setAlpha(1.0f);

                break;
            default:

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CaptureActivity.REQ_CODE_TO_CAPTURE && resultCode == Activity.RESULT_OK) {
            filePath = data.getStringExtra(CaptureActivity.PREVIEW_RESULT_FILE_PATH);
            fileWidth = data.getIntExtra(CaptureActivity.PREVIEW_RESULT_FILE_WIDTH, 0);
            fileHeight = data.getIntExtra(CaptureActivity.PREVIEW_RESULT_FILE_HEIGHT, 0);
            fileIsVideo = data.getBooleanExtra(CaptureActivity.PREVIEW_RESULT_FILE_TYPE, false);

            if (!TextUtils.isEmpty(filePath)) {
                mBinding.commentExtLayout.setVisibility(View.VISIBLE);
                mBinding.commentCover.setImageUrl(filePath);
                if (fileIsVideo) {
                    mBinding.commentIconVideo.setVisibility(View.VISIBLE);
                }
            }

            mBinding.commentVideo.setEnabled(false);
            mBinding.commentVideo.setAlpha(.5f);
        }
    }

    private void publishComment() {

        if (fileIsVideo && !TextUtils.isEmpty(filePath)) { // 上传视频
            FileUtil.generateVideoCover(filePath).observe(this, new Observer<String>() {
                @Override
                public void onChanged(String coverPath) {
                    uploadFileToServer(coverPath, filePath);
                }
            });

        }else if (!TextUtils.isEmpty(filePath)){ // 上传图片
            uploadFileToServer(null, filePath);
        }else {
            publish();
        }
    }

    /*
     * 上传文件到阿里云服务器
     * com.aliyun.dpa:oss-android-sdk:+
     * */
    @SuppressLint("RestrictedApi")
    private void uploadFileToServer(String coverPath, String videoPath) {
        // 线程同步：AtomicInteger,CountDownLatch,CyclicBarrier
        AtomicInteger count = new AtomicInteger(1);

        showHideLoadingDialog(true);
        if (!TextUtils.isEmpty(coverPath)){
            count.set(2);
            ArchTaskExecutor.getIOThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    int remain = count.decrementAndGet();
                    coverUrl = FileUploadManager.upload(coverPath);
                    if (remain <=0){
                        if (!TextUtils.isEmpty(fileUrl) && !TextUtils.isEmpty(coverUrl)){
                            publish();
                        }else {
                            showHideLoadingDialog(false);
                            ToastUtil.showOnUI(getContext(), getString(R.string.file_upload_failed));
                        }
                    }
                }
            });
        }
        ArchTaskExecutor.getIOThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                int remain = count.decrementAndGet();
                fileUrl = FileUploadManager.upload(filePath);
                if (remain <= 0){
                    if (!TextUtils.isEmpty(fileUrl) || !TextUtils.isEmpty(coverPath) && !TextUtils.isEmpty(coverUrl)){
                        publish();
                    }
                }else {
                    showHideLoadingDialog(false);
                    ToastUtil.showOnUI(getContext(), getString(R.string.file_upload_failed));
                }
            }
        });
    }

    private void publish() {
        String inputText = mBinding.inputEdit.getText().toString();
        /*if (TextUtils.isEmpty(inputText)) {
            return;
        }*/
        ApiService.post(URL_COMMENT)
                .addParams("userId", UserManager.get().getUserId())
                .addParams("itemId", mItemId)
                .addParams("commentText", inputText)
                .addParams("image_url", fileIsVideo?coverUrl:fileUrl)
                .addParams("videoUrl", fileUrl)
                .addParams("width", fileWidth)
                .addParams("height", fileHeight)
                .execute(new JsonCallback<Comment>() {
                    @Override
                    public void onSuccess(ApiResponse<Comment> response) {
                        onCommentSuccess(response.body);
                        showHideLoadingDialog(false);
                    }

                    @SuppressLint("RestrictedApi")
                    @Override
                    public void onError(ApiResponse<Comment> response) {
                        showHideLoadingDialog(false);
                                ToastUtil.showOnUI(JetAppGlobal.getApplication(), "评论失败，" + response.message);
                    }
                });
    }

    private void showHideLoadingDialog(boolean isShow) {
        if (isShow) {
            if (loadingDialog == null) {
                loadingDialog = new LoadingDialog(getContext());
            }
            loadingDialog.setLoadingText(getString(R.string.upload_text));
            loadingDialog.show();
        }else {
            if (loadingDialog != null){
                loadingDialog.dismiss();
            }
        }
    }

    @SuppressLint("RestrictedApi")
    private void onCommentSuccess(Comment body) {
        ArchTaskExecutor.getMainThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                ToastUtil.show(JetAppGlobal.getApplication(), "评论成功");
            }
        });
        if (mCommentResultListener != null) {
            mCommentResultListener.onAddComment(body);
        }
    }

    public interface CommentAddResultListener {
        void onAddComment(Comment comment);
    }

    public void setCommentAddResultListener(CommentAddResultListener listener) {
        mCommentResultListener = listener;
    }

}
