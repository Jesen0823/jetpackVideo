package com.jesen.cod.jetpackvideo.ui.detail;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
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
import com.jesen.cod.jetpackvideo.ui.MutableItemKeyedDataSource;
import com.jesen.cod.libcommon.view.EmptyView;

import org.jetbrains.annotations.NotNull;

public abstract class ViewHandler {

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
        mListAdapter = new FeedCommentAdapter(mActivity){
            @Override
            public void onCurrentListChanged(@Nullable  PagedList<Comment> previousList, @Nullable @org.jetbrains.annotations.Nullable PagedList<Comment> currentList) {
                boolean empty = currentList.size() <= 0;
                handleEmpty(!empty);
            }
        };
        mRecyclerView.setAdapter(mListAdapter);

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
                if (commentDialog == null) {
                    commentDialog = CommentDialog.getInstance(mFeed.itemId);
                }
                commentDialog.setCommentAddResultListener(new CommentDialog.CommentAddResultListener() {
                    @Override
                    public void onAddComment(Comment comment) {
                        MutableItemKeyedDataSource<Integer, Comment> dataSource
                                = new MutableItemKeyedDataSource<Integer, Comment>((ItemKeyedDataSource) detailViewModel.getDataSource()) {

                            @NonNull
                            @Override
                            public @NotNull Integer getKey(@NonNull @NotNull Comment item) {
                                return item.id;
                            }
                        };

                        dataSource.data.add(comment);
                        dataSource.data.addAll(mListAdapter.getCurrentList());
                        // 使得新添加/发布的评论处于列表的第一项
                        PagedList<Comment> pagedList = dataSource.buildNewItemList(mListAdapter.getCurrentList().getConfig());
                        mListAdapter.submitList(pagedList);
                    }
                });
                commentDialog.show(mActivity.getSupportFragmentManager(), "cmt_dialog");
            }
        });

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
}
