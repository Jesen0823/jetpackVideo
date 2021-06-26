package com.jesen.cod.jetpackvideo.ui.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;
import android.widget.Toast;

import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.jesen.cod.jetpackvideo.R;
import com.jesen.cod.jetpackvideo.model.Comment;
import com.jesen.cod.jetpackvideo.model.Feed;
import com.jesen.cod.jetpackvideo.model.User;
import com.jesen.cod.jetpackvideo.ui.ShareDialog;
import com.jesen.cod.jetpackvideo.ui.login.UserManager;
import com.jesen.cod.jetpackvideo.utils.BitmapUtil;
import com.jesen.cod.jetpackvideo.utils.FileUtil;
import com.jesen.cod.jetpackvideo.utils.Og;
import com.jesen.cod.libcommon.JetAppGlobal;
import com.jesen.cod.libnetwork.ApiResponse;
import com.jesen.cod.libnetwork.ApiService;
import com.jesen.cod.libnetwork.JsonCallback;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.util.Date;

public class InteractionPresenter {

    private static final String URL_TOGGLE_FEED_LIKE = "/ugc/toggleFeedLike";
    private static final String URL_TOGGLE_FEED_DIS_LIKE = "/ugc/dissFeed";
    private static final String URL_SHARE = "/ugc/increaseShareCount";
    private static final String URL_TOGGLE_COMMENT_LIKE = "/ugc/toggleCommentLike";

    private static Uri shareImgUri;

    public static void init() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bp = BitmapUtil.getIconBitmap(JetAppGlobal.getApplication(), R.mipmap.ic_launcher);
                InputStream is = BitmapUtil.bitmap2InputStream(bp);
                File file = FileUtil.checkFile("share_icon.png");
                FileUtil.writeStreamToFile(is, file);
                shareImgUri = FileUtil.getFileUri(JetAppGlobal.getApplication(), file);
            }
        }).start();
    }


    //给一个帖子点赞/取消点赞，它和给帖子点踩一踩是互斥的
    public static void toggleFeedLike(LifecycleOwner owner, Feed feed) {
        if (!isLogin(owner, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                toggleFeedLikeInternal(feed);
            }
        })) {
        } else {
            toggleFeedLikeInternal(feed);
        }

    }

    /*
     * 点赞
     * */
    private static void toggleFeedLikeInternal(Feed feed) {
        ApiService.get(URL_TOGGLE_FEED_LIKE)
                .addParams("userId", UserManager.get().getUserId())
                .addParams("itemId", feed.itemId)
                .execute(new JsonCallback<JSONObject>() {
                    @Override
                    public void onSuccess(ApiResponse<JSONObject> response) {
                        boolean hasLiked = false;
                        if (response.body != null) {
                            try {
                                hasLiked = response.body.getBoolean("hasLiked");
                                // 改变数据dataBinding触发UI变化
                                feed.getUgc().setHasLiked(hasLiked);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onError(ApiResponse<JSONObject> response) {
                        super.onError(response);
                    }

                    @Override
                    public void onCacheSuccess(ApiResponse<JSONObject> response) {
                        super.onCacheSuccess(response);
                    }
                });
    }


    // 踩
    public static void toggleFeedDisLike(LifecycleOwner owner, Feed feed) {
        if (!UserManager.get().isLogin()) {
            LiveData<User> loginLiveData = UserManager.get().login(JetAppGlobal.getApplication());
            loginLiveData.observe(owner, new Observer<User>() {
                @Override
                public void onChanged(User user) {
                    if (user != null) {
                        toggleFeedDisLikeInternal(feed);
                    }
                    loginLiveData.removeObserver(this);
                }
            });
            return;
        }
        toggleFeedDisLikeInternal(feed);

    }

    private static void toggleFeedDisLikeInternal(Feed feed) {
        ApiService.get(URL_TOGGLE_FEED_DIS_LIKE)
                .addParams("userId", UserManager.get().getUserId())
                .addParams("itemId", feed.itemId)
                .execute(new JsonCallback<JSONObject>() {
                    @Override
                    public void onSuccess(ApiResponse<JSONObject> response) {
                        boolean hasDiss = false;
                        if (response.body != null) {
                            try {
                                hasDiss = response.body.getBoolean("hasdiss");
                                feed.getUgc().setHasdiss(hasDiss);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onError(ApiResponse<JSONObject> response) {
                        super.onError(response);
                    }
                });

    }

    private static boolean isLogin(LifecycleOwner owner, Observer<User> observer) {
        if (UserManager.get().isLogin()) {
            return true;
        } else {
            LiveData<User> liveData = UserManager.get().login(JetAppGlobal.getApplication());
            if (owner == null) {
                liveData.observeForever(loginObserver(observer, liveData));
            } else {
                liveData.observe(owner, loginObserver(observer, liveData));
            }
            return false;
        }
    }

    @NotNull
    private static Observer<User> loginObserver(Observer<User> observer, LiveData<User> liveData) {
        return new Observer<User>() {
            @Override
            public void onChanged(User user) {
                liveData.removeObserver(this);
                if (user != null && observer != null) {
                    observer.onChanged(user);
                }
            }
        };
    }

    /*
     * 打开分享
     * */
    public static void openShare(Context context, Feed feed) {
        String url = "http://h5.aliyun.ppjoke.com/item/%s?timestamp=%s&user_id=%s";
        String shareUrl = String.format(url, feed.itemId, new Date().getTime(), UserManager.get().getUserId());

        ShareDialog shareDialog = new ShareDialog(context);
        shareDialog.setShareContent(shareUrl);
        Og.d("openShare, shareImgUri: "+shareImgUri);
        shareDialog.setShareImg(shareImgUri);
        shareDialog.setShareItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ApiService.get(URL_SHARE).addParams("itemId", feed.itemId)
                        .execute(new JsonCallback<JSONObject>() {
                            @Override
                            public void onSuccess(ApiResponse<JSONObject> response) {
                                if (response.body != null) {
                                    int count = 0;
                                    try {
                                        count = response.body.getInt("count");
                                        feed.getUgc().setShareCount(count);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            @Override
                            public void onError(ApiResponse<JSONObject> response) {
                                showToast(response.message);
                            }
                        });
            }
        });
        shareDialog.show();
    }

    @SuppressLint("RestrictedApi")
    private static void showToast(String message) {
        ArchTaskExecutor.getMainThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(JetAppGlobal.getApplication(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    //给一个帖子的评论点赞/取消点赞
    public static void toggleCommentLike(LifecycleOwner owner, Comment comment) {
        if (!isLogin(owner, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                toggleCommentLikeInternal(comment);
            }
        })) {
        } else {
            toggleCommentLikeInternal(comment);
        }
    }

    private static void toggleCommentLikeInternal(Comment comment) {

        ApiService.get(URL_TOGGLE_COMMENT_LIKE)
                .addParams("commentId", comment.commentId)
                .addParams("userId", UserManager.get().getUserId())
                .execute(new JsonCallback<JSONObject>() {
                    @Override
                    public void onSuccess(ApiResponse<JSONObject> response) {
                        if (response.body != null) {
                            boolean hasLiked = false;
                            try {
                                hasLiked = response.body.getBoolean("hasLiked");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            comment.getUgc().setHasLiked(hasLiked);
                        }
                    }

                    @Override
                    public void onError(ApiResponse<JSONObject> response) {
                        showToast(response.message);
                    }
                });
    }
}
