package com.jesen.cod.jetpackvideo.ui.detail;

import android.widget.LinearLayout;

import androidx.annotation.CallSuper;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jesen.cod.jetpackvideo.databinding.LayoutFeedDetailBottomInteractionBinding;
import com.jesen.cod.jetpackvideo.model.Feed;

public abstract class ViewHandler {

    protected FragmentActivity mActivity;
    protected Feed mFeed;

    protected RecyclerView mRecyclerView;
    protected FeedCommentAdapter mListAdapter;
    protected LayoutFeedDetailBottomInteractionBinding mInteractionBinding;

    public ViewHandler(FragmentActivity activity){

        mActivity = activity;
    }


    @CallSuper
    public void bindInitData(Feed feed){

        mFeed = feed;
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity,
                LinearLayoutManager.VERTICAL,false));
        mRecyclerView.setItemAnimator(null);
         mListAdapter = new FeedCommentAdapter();
        mRecyclerView.setAdapter(mListAdapter);
    }
}
