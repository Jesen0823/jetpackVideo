package com.jesen.cod.jetpackvideo.ui.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.jesen.cod.jetpackvideo.R;
import com.jesen.cod.jetpackvideo.model.Comment;
import com.jesen.cod.jetpackvideo.model.Feed;
import com.jesen.cod.jetpackvideo.model.TagList;
import com.jesen.cod.jetpackvideo.model.User;
import com.jesen.cod.jetpackvideo.ui.ShareDialog;
import com.jesen.cod.jetpackvideo.ui.login.UserManager;
import com.jesen.cod.libcommon.utils.BitmapUtil;
import com.jesen.cod.libcommon.utils.FileUtil;
import com.jesen.cod.libcommon.utils.Og;
import com.jesen.cod.libcommon.JetAppGlobal;
import com.jesen.cod.libcommon.extention.LiveDataBus;
import com.jesen.cod.libnetwork.ApiResponse;
import com.jesen.cod.libnetwork.ApiService;
import com.jesen.cod.libnetwork.JsonCallback;

import org.jetbrains.annotations.NotNull;

import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.io.InputStream;

public class InteractionPresenter {

    public static final String EVENT_DATA_FROM_INTERACTION = "data_from_interaction";

    private static final String URL_TOGGLE_FEED_LIKE = "/ugc/toggleFeedLike";
    private static final String URL_TOGGLE_FEED_DIS_LIKE = "/ugc/dissFeed";
    private static final String URL_SHARE = "/ugc/increaseShareCount";
    private static final String URL_TOGGLE_COMMENT_LIKE = "/ugc/toggleCommentLike";
    private static final String URL_FEED_FAVORITE = "/ugc/toggleFavorite";
    private static final String URL_TAG_LIKE = "/tag/toggleTagFollow";

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
                        if (response.body != null) {
                            boolean hasLiked = response.body.getBooleanValue("hasLiked");
                            // 改变数据dataBinding触发UI变化
                            feed.getUgc().setHasLiked(hasLiked);
                            LiveDataBus.getInstance().with(EVENT_DATA_FROM_INTERACTION)
                                    .postValue(feed);
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
                        if (response.body != null) {
                            boolean hasDiss = hasDiss = response.body.getBooleanValue("hasdiss");
                            feed.getUgc().setHasdiss(hasDiss);
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
        //String shareUrl = String.format(url, feed.itemId, new Date().getTime(), UserManager.get().getUserId());
        String shareContent = feed.feeds_text;
        if (!TextUtils.isEmpty(feed.url)) {
            shareContent = feed.url;
        } else if (!TextUtils.isEmpty(feed.cover)) {
            shareContent = feed.cover;
        }

        ShareDialog shareDialog = new ShareDialog(context);
        shareDialog.setShareContent(shareContent);
        Og.d("openShare, shareImgUri: " + shareImgUri);
        shareDialog.setShareImg(shareImgUri);
        shareDialog.setShareItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ApiService.get(URL_SHARE).addParams("itemId", feed.itemId)
                        .execute(new JsonCallback<JSONObject>() {
                            @Override
                            public void onSuccess(ApiResponse<JSONObject> response) {
                                if (response.body != null) {
                                    int count = response.body.getIntValue("count");
                                    feed.getUgc().setShareCount(count);
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
                            boolean hasLiked = response.body.getBooleanValue("hasLiked");
                            comment.getUgc().setHasLiked(hasLiked);
                        }
                    }

                    @Override
                    public void onError(ApiResponse<JSONObject> response) {
                        showToast(response.message);
                    }
                });
    }

    //收藏/取消收藏一个帖子
    public static void toggleFeedFavorite(LifecycleOwner owner, Feed feed) {
        if (!isLogin(owner, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                toggleFeedFavorite(feed);
            }
        })) {
        } else {
            toggleFeedFavorite(feed);
        }
    }


    public static void toggleFeedFavorite(Feed feed) {
        ApiService.get(URL_FEED_FAVORITE)
                .addParams("itemId", feed.itemId)
                .addParams("userId", UserManager.get().getUserId())
                .execute(new JsonCallback<JSONObject>() {
                    @Override
                    public void onSuccess(ApiResponse<JSONObject> response) {
                        if (response.body != null) {
                            boolean hasFavorite = response.body.getBooleanValue("hasFavorite");
                            feed.getUgc().setHasFavorite(hasFavorite);
                            LiveDataBus.getInstance().with(EVENT_DATA_FROM_INTERACTION)
                                    .postValue(feed);
                        }
                    }

                    @Override
                    public void onError(ApiResponse<JSONObject> response) {
                        showToast(response.message);
                    }
                });
    }


    //关注/取消关注一个用户
    public static void toggleFollowUser(LifecycleOwner owner, Feed feed) {
        if (!isLogin(owner, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                toggleFollowUser(feed);
            }
        })) {
        } else {
            toggleFollowUser(feed);
        }
    }

    private static void toggleFollowUser(Feed feed) {
        ApiService.get("/ugc/toggleUserFollow")
                .addParams("followUserId", UserManager.get().getUserId())
                .addParams("userId", feed.author.userId)
                .execute(new JsonCallback<JSONObject>() {
                    @Override
                    public void onSuccess(ApiResponse<JSONObject> response) {
                        if (response.body != null) {
                            boolean hasFollow = response.body.getBooleanValue("hasLiked");
                            feed.getAuthor().setHasFollow(hasFollow);

                            // 利用事件总线将数据变更发送出去，在FeedAdapter观察事件
                            LiveDataBus.getInstance().with(EVENT_DATA_FROM_INTERACTION)
                                    .postValue(feed);
                        }
                    }

                    @Override
                    public void onError(ApiResponse<JSONObject> response) {
                        showToast(response.message);
                    }
                });
    }

    public static LiveData<Boolean> deleteFeed(Context context, long itemId) {
        MutableLiveData<Boolean> liveData = new MutableLiveData<>();
        new AlertDialog.Builder(context)
                .setNegativeButton("删除", (dialog, which) -> {
                    dialog.dismiss();
                    deleteFeedInternal(liveData, itemId);
                }).setPositiveButton("取消", (dialog, which) -> dialog.dismiss()).setMessage("确定要删除这条评论吗？").create().show();
        return liveData;
    }

    private static void deleteFeedInternal(MutableLiveData<Boolean> liveData, long itemId) {
        ApiService.get("/feeds/deleteFeed")
                .addParams("itemId", itemId)
                .execute(new JsonCallback<com.alibaba.fastjson.JSONObject>() {
                    @Override
                    public void onSuccess(ApiResponse<com.alibaba.fastjson.JSONObject> response) {
                        if (response.body != null) {
                            boolean success = response.body.getBoolean("result");
                            liveData.postValue(success);
                            showToast("删除成功");
                        }
                    }

                    @Override
                    public void onError(ApiResponse<com.alibaba.fastjson.JSONObject> response) {
                        showToast(response.message);
                    }
                });
    }

    //删除某个帖子的一个评论
    public static LiveData<Boolean> deleteFeedComment(Context context, long itemId, long commentId) {
        MutableLiveData<Boolean> liveData = new MutableLiveData<>();
        new AlertDialog.Builder(context)
                .setNegativeButton("删除", (dialog, which) -> {
                    dialog.dismiss();
                    deleteFeedCommentInternal(liveData, itemId, commentId);
                }).setPositiveButton("取消", (dialog, which) -> dialog.dismiss()).setMessage("确定要删除这条评论吗？").create().show();
        return liveData;
    }

    private static void deleteFeedCommentInternal(LiveData liveData, long itemId, long commentId) {
        ApiService.get("/comment/deleteComment")
                .addParams("userId", UserManager.get().getUserId())
                .addParams("commentId", commentId)
                .addParams("itemId", itemId)
                .execute(new JsonCallback<JSONObject>() {
                    @Override
                    public void onSuccess(ApiResponse<JSONObject> response) {
                        if (response.body != null) {
                            boolean result = response.body.getBooleanValue("result");
                            ((MutableLiveData) liveData).postValue(result);
                            showToast("评论删除成功");
                        }
                    }

                    @Override
                    public void onError(ApiResponse<JSONObject> response) {
                        showToast(response.message);
                    }
                });
    }

    public static void toggleTagLike(LifecycleOwner owner, TagList tagList){
        if (!isLogin(owner, new Observer<User>() {
            @Override
            public void onChanged(User user) {
                toggleTagLikeInternal(tagList);
            }
        }));
        else {
            toggleTagLikeInternal(tagList);
        }
    }

    private static void toggleTagLikeInternal(TagList tagList) {
        ApiService.get(URL_TAG_LIKE)
                .addParams("tagId", tagList.tagId)
                .addParams("userId", UserManager.get().getUserId())
                .execute(new JsonCallback<JSONObject>() {
                    @Override
                    public void onSuccess(ApiResponse<JSONObject> response) {
                        if (response.body != null) {
                            Boolean follow = response.body.getBoolean("hasFollow");
                            tagList.setHasFollow(follow);
                        }
                    }

                    @Override
                    public void onError(ApiResponse<JSONObject> response) {
                        showToast(response.message);
                    }
                });
    }
}
