package com.jesen.cod.jetpackvideo.ui.detail;

import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.CallSuper;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jesen.cod.jetpackvideo.R;
import com.jesen.cod.jetpackvideo.databinding.LayoutFeedDetailBottomInteractionBinding;
import com.jesen.cod.jetpackvideo.model.Comment;
import com.jesen.cod.jetpackvideo.model.Feed;
import com.jesen.cod.libcommon.view.EmptyView;

public abstract class ViewHandler {

    private final FeedDetailViewModel detailViewModel;
    protected FragmentActivity mActivity;
    protected Feed mFeed;

    private EmptyView mEmptyView;
    protected RecyclerView mRecyclerView;
    protected FeedCommentAdapter mListAdapter;
    protected LayoutFeedDetailBottomInteractionBinding mInteractionBinding;

    public ViewHandler(FragmentActivity activity) {

        mActivity = activity;
        detailViewModel = new ViewModelProvider(activity).get(FeedDetailViewModel.class);
    }


    @CallSuper
    public void bindInitData(Feed feed) {
        mInteractionBinding.setLifecycleOwner(mActivity);
        mFeed = feed;
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity,
                LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setItemAnimator(null);
        mListAdapter = new FeedCommentAdapter();
        mRecyclerView.setAdapter(mListAdapter);

        detailViewModel.setItemId(mFeed.itemId);

        detailViewModel.getPagedListLiveData().observe(mActivity, new Observer<PagedList<Comment>>() {
            @Override
            public void onChanged(PagedList<Comment> comments) {
                mListAdapter.submitList(comments);

                handleEmpty(comments.size()>0);
            }
        });

    }

    private  void handleEmpty(boolean hasData){
        if (hasData){
            if (mEmptyView != null){
                mListAdapter.removeHeaderView(mEmptyView);
            }
        }else {
            if (mEmptyView == null){
                mEmptyView = new EmptyView(mActivity);
                mEmptyView.setLayoutParams(new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                mEmptyView.setTitle(mActivity.getString(R.string.feed_comment_empty));
                mListAdapter.addHeaderView(mEmptyView);
            }
        }
    }
}
