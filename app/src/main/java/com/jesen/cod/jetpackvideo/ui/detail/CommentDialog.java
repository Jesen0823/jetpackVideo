package com.jesen.cod.jetpackvideo.ui.detail;

import android.annotation.SuppressLint;
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

import com.jesen.cod.jetpackvideo.R;
import com.jesen.cod.jetpackvideo.databinding.LayoutDialogCommentBinding;
import com.jesen.cod.jetpackvideo.model.Comment;
import com.jesen.cod.jetpackvideo.ui.login.UserManager;
import com.jesen.cod.jetpackvideo.utils.ToastUtil;
import com.jesen.cod.libcommon.JetAppGlobal;
import com.jesen.cod.libnetwork.ApiResponse;
import com.jesen.cod.libnetwork.ApiService;
import com.jesen.cod.libnetwork.JsonCallback;

import org.jetbrains.annotations.NotNull;

public class CommentDialog extends DialogFragment implements View.OnClickListener{

    private static final String URL_COMMENT = "/comment/addComment";
    private static final String KEY_ITEM_ID = "key_item_id";

    private LayoutDialogCommentBinding mBinding;

    private CommentAddResultListener mCommentResultListener;

    private long mItemId;

    public static CommentDialog getInstance(long itemId){
        Bundle args = new Bundle();
        args.putLong(KEY_ITEM_ID,itemId);
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
        switch (view.getId()){
            case R.id.comment_send:
                publishComment();
                break;
            case R.id.comment_video:
                break;
            case R.id.comment_delete:
                break;
            default:

        }
    }

    private void publishComment() {
        String inputText = mBinding.inputEdit.getText().toString();
        if (TextUtils.isEmpty(inputText)){
            return;
        }

        ApiService.post(URL_COMMENT)
                .addParams("userId", UserManager.get().getUserId())
                .addParams("itemId",mItemId)
                .addParams("commentText",inputText)
                .addParams("image_url",null)
                .addParams("videoUrl",null)
                .addParams("width",0)
                .addParams("height",0)
                .execute(new JsonCallback<Comment>() {
                    @Override
                    public void onSuccess(ApiResponse<Comment> response) {
                        onCommentSuccess(response.body);
                    }

                    @SuppressLint("RestrictedApi")
                    @Override
                    public void onError(ApiResponse<Comment> response) {
                        ArchTaskExecutor.getMainThreadExecutor().execute(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtil.show(JetAppGlobal.getApplication(), "评论失败，"+ response.message);
                            }
                        });
                    }
                });
    }

    @SuppressLint("RestrictedApi")
    private void onCommentSuccess(Comment body) {
        ArchTaskExecutor.getMainThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                ToastUtil.show(JetAppGlobal.getApplication(),"评论成功");
            }
        });
        if (mCommentResultListener != null){
            mCommentResultListener.onAddComment(body);
        }
    }

    public interface CommentAddResultListener{
        void onAddComment(Comment comment);
    }

    public void setCommentAddResultListener(CommentAddResultListener listener){
        mCommentResultListener = listener;
    }

}
