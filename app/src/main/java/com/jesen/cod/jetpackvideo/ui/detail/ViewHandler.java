package com.jesen.cod.jetpackvideo.ui.detail;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jesen.cod.jetpackvideo.R;
import com.jesen.cod.jetpackvideo.databinding.LayoutFeedDetailBottomInteractionBinding;
import com.jesen.cod.jetpackvideo.model.Comment;
import com.jesen.cod.jetpackvideo.model.Feed;
import com.jesen.cod.jetpackvideo.model.User;
import com.jesen.cod.jetpackvideo.ui.MutableItemKeyedDataSource;
import com.jesen.cod.jetpackvideo.ui.login.UserManager;
import com.jesen.cod.libcommon.JetAppGlobal;
import com.jesen.cod.libcommon.utils.Og;
import com.jesen.cod.libcommon.view.EmptyView;

import org.jetbrains.annotations.NotNull;

public abstract class ViewHandler {

    private static final String TAG = "ViewHandler";
    private final FeedDetailViewModel detailViewModel;
    protected FragmentActivity mActivity;
    protected Feed mFeed;

    private EmptyView mEmptyView;
    protected RecyclerView mRecyclerView;
    protected FeedCommentAdapter mListAdapter;
    protected LayoutFeedDetailBottomInteractionBinding mInteractionBinding;
    private CommentDialog commentDialog;

    public ViewHandler(FragmentActivity activity) {

        mActivity = activity;
        detailViewModel = new ViewModelProvider(activity).get(FeedDetailViewModel.class);
    }


    @CallSuper
    public void bindInitData(Feed feed) {
        mInteractionBinding.setOwner(mActivity);

        mFeed = feed;
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity,
                LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setItemAnimator(null);
        mListAdapter = new FeedCommentAdapter(mActivity) {
            @Override
            public void onCurrentListChanged(@Nullable PagedList<Comment> previousList, @Nullable @org.jetbrains.annotations.Nullable PagedList<Comment> currentList) {
                boolean empty = currentList.size() <= 0;
                handleEmpty(!empty);
            }
        };
        mRecyclerView.setAdapter(mListAdapter);
        Og.i("ViewHandler, set to detailViewModel, feed:"+mFeed.id+", itemId:"+mFeed.itemId+", feeds_text:"+mFeed.feeds_text);

        detailViewModel.setItemId(mFeed.itemId);

        detailViewModel.getPagedListLiveData().observe(mActivity, new Observer<PagedList<Comment>>() {
            @Override
            public void onChanged(PagedList<Comment> comments) {

                mListAdapter.submitList(comments);

                handleEmpty(comments.size() > 0);
            }
        });

        mInteractionBinding.inputView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!UserManager.get().isLogin()) {
                    LiveData<User> loginLiveData = UserManager.get().login(JetAppGlobal.getApplication());
                    loginLiveData.observe(mActivity, new Observer<User>() {
                        @Override
                        public void onChanged(User user) {
                            if (user != null) {
                                showCommentDialog();
                            }
                            loginLiveData.removeObserver(this);
                        }
                    });
                    return;
                }
                showCommentDialog();
            }
        });

    }

    private void showCommentDialog() {
        if (commentDialog == null) {
            commentDialog = CommentDialog.getInstance(mFeed.itemId);
        }
        commentDialog.setCommentAddResultListener(comment -> {
            handleEmpty(true);
            mListAdapter.addAndRefreshList(comment);
        });
        commentDialog.show(mActivity.getSupportFragmentManager(), "comment_dialog");
    }

    public void handleEmpty(boolean hasData) {
        if (hasData) {
            if (mEmptyView != null) {
                mListAdapter.removeHeaderView(mEmptyView);
            }
        } else {
            if (mEmptyView == null) {
                mEmptyView = new EmptyView(mActivity);
                mEmptyView.setLayoutParams(new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                mEmptyView.setTitle(mActivity.getString(R.string.feed_comment_empty));
            }
            mListAdapter.addHeaderView(mEmptyView);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (commentDialog != null && commentDialog.isAdded()) {
            commentDialog.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void onPause() {

    }

    public void onResume() {

    }

    public void onBackPressed() {

    }
}
