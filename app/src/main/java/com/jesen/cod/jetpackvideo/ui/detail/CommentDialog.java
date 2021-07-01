package com.jesen.cod.jetpackvideo.ui.detail;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
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
import com.jesen.cod.libcommon.utils.PixUtils;
import com.jesen.cod.libcommon.view.ViewHelper;
import com.jesen.cod.libnetwork.ApiResponse;
import com.jesen.cod.libnetwork.ApiService;
import com.jesen.cod.libnetwork.JsonCallback;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;

public class CommentDialog extends AppCompatDialogFragment implements View.OnClickListener {

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
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Window window = getDialog().getWindow();
        window.setWindowAnimations(0);

        mBinding = LayoutDialogCommentBinding.inflate(inflater, window.findViewById(android.R.id.content), false);
        mBinding.commentVideo.setOnClickListener(this);
        mBinding.commentDelete.setOnClickListener(this);
        mBinding.commentSend.setOnClickListener(this);

        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

        this.mItemId = getArguments().getLong(KEY_ITEM_ID, 0L);

        ViewHelper.setViewOutline(mBinding.getRoot(), PixUtils.dp2px(10), ViewHelper.RADIUS_TOP);

        mBinding.getRoot().post(new Runnable() {
            @Override
            public void run() {
                showSoftInputMethod();
            }
        });
        dismissWhenPressBack();

        return mBinding.getRoot();
    }

    private void showSoftInputMethod() {
        mBinding.inputEdit.setFocusable(true);
        mBinding.inputEdit.setFocusableInTouchMode(true);
        mBinding.inputEdit.requestFocus();

        InputMethodManager manager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.showSoftInput(mBinding.inputEdit, 0);
    }

    private void dismissWhenPressBack() {
        mBinding.inputEdit.setOnBackKeyEventListener(() -> {
            mBinding.inputEdit.postDelayed(new Runnable() {
                @Override
                public void run() {
                    dismiss();
                }
            }, 200);
            return true;
        });
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
                mBinding.commentCover.setImageDrawable(null);
                mBinding.commentExtLayout.setVisibility(View.GONE);
                mBinding.commentVideo.setEnabled(true);
                mBinding.commentVideo.setAlpha(1.0f);

                break;
            default:

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
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
            mBinding.commentVideo.setAlpha(.8f);
        }
    }

    private void publishComment() {
        if (TextUtils.isEmpty(mBinding.inputEdit.getText())) {
            ToastUtil.show(getContext(), "文字不可为空");
            return;
        }

        if (fileIsVideo && !TextUtils.isEmpty(filePath)) { // 上传视频
            FileUtil.generateVideoCover(filePath).observe(this, new Observer<String>() {
                @Override
                public void onChanged(String coverPath) {
                    uploadFileToServer(coverPath, filePath);
                }
            });

        } else if (!TextUtils.isEmpty(filePath)) { // 上传图片
            uploadFileToServer(null, filePath);
        } else {
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
        showHideLoadingDialog(true);

        AtomicInteger count = new AtomicInteger(1);
        if (!TextUtils.isEmpty(coverPath)) {
            count.set(2);
            ArchTaskExecutor.getIOThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    int remain = count.decrementAndGet();
                    coverUrl = FileUploadManager.upload(coverPath);
                    if (remain <= 0) {
                        if (!TextUtils.isEmpty(fileUrl) && !TextUtils.isEmpty(coverUrl)) {
                            publish();
                        } else {
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
                if (remain <= 0) {
                    if (!TextUtils.isEmpty(fileUrl) || !TextUtils.isEmpty(coverPath) && !TextUtils.isEmpty(coverUrl)) {
                        publish();
                    }
                } else {
                    showHideLoadingDialog(false);
                    ToastUtil.showOnUI(getContext(), getString(R.string.file_upload_failed));
                }
            }
        });
    }

    private void publish() {
        String inputText = mBinding.inputEdit.getText().toString();
        ApiService.post(URL_COMMENT)
                .addParams("userId", UserManager.get().getUserId())
                .addParams("itemId", mItemId)
                .addParams("commentText", inputText)
                .addParams("image_url", fileIsVideo ? coverUrl : fileUrl)
                .addParams("videoUrl", fileIsVideo ? fileUrl : null)
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

    @SuppressLint("RestrictedApi")
    private void showHideLoadingDialog(boolean isShow) {
        if (isShow) {
            if (loadingDialog == null) {
                loadingDialog = new LoadingDialog(getContext());
                loadingDialog.setLoadingText(getString(R.string.upload_text));
                loadingDialog.setCanceledOnTouchOutside(false);
                loadingDialog.setCancelable(false);
            }
            if (!loadingDialog.isShowing()) {
                loadingDialog.show();
            }
        } else {
            if (loadingDialog != null) {
                if (Looper.myLooper() == Looper.getMainLooper()) {
                    ArchTaskExecutor.getMainThreadExecutor().execute(() -> {
                        if (loadingDialog != null && loadingDialog.isShowing()) {
                            loadingDialog.dismiss();
                        }
                    });
                } else if (loadingDialog.isShowing()) {
                    loadingDialog.dismiss();
                }
            }
        }
    }

    @SuppressLint("RestrictedApi")
    private void onCommentSuccess(Comment body) {
        ToastUtil.showOnUI(JetAppGlobal.getApplication(), "评论成功");
        ArchTaskExecutor.getMainThreadExecutor().execute(() -> {
            if (mCommentResultListener != null) {
                mCommentResultListener.onAddComment(body);
            }
            dismiss();
        });
    }

    @Override
    public void dismiss() {
        super.dismiss();
        showHideLoadingDialog(false);
        filePath = null;
        fileUrl = null;
        coverUrl = null;
        fileIsVideo = false;
        fileWidth = 0;
        fileHeight = 0;
    }

    public interface CommentAddResultListener {
        void onAddComment(Comment comment);
    }

    public void setCommentAddResultListener(CommentAddResultListener listener) {
        mCommentResultListener = listener;
    }

}
