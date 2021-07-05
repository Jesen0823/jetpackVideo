package com.jesen.cod.jetpackvideo.ui.mine;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;

import com.jesen.cod.jetpackvideo.exoplayer.PageListPlayDetector;
import com.jesen.cod.jetpackvideo.exoplayer.PageListPlayManager;
import com.jesen.cod.jetpackvideo.model.Feed;
import com.jesen.cod.jetpackvideo.model.PersonalTabType;
import com.jesen.cod.jetpackvideo.ui.view.AbsListFragment;
import com.jesen.cod.libcommon.utils.Og;
import com.scwang.smartrefresh.layout.api.RefreshLayout;

import org.jetbrains.annotations.NotNull;

public class PersonalListFragment extends AbsListFragment<Feed, PersonalListViewModel> {
    private static final String TAG = "PersonalListFragment";

    private int mTabType;
    private PageListPlayDetector mPlayDetector;
    private boolean shouldPause = true;
    private String tabTypeValue;

    public static PersonalListFragment newInstance(int tabType) {

        Bundle args = new Bundle();
        args.putInt(UserPersonalActivity.KEY_TYPE_PERSONAL_TAB, tabType);
        PersonalListFragment fragment = new PersonalListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPlayDetector = new PageListPlayDetector(this, mRecyclerView);
        mViewModel.setProfileType(tabTypeValue);
        // 不需要下拉刷新
        mRefreshLayout.setEnableRefresh(false);
    }

    @Override
    public PagedListAdapter getAdapter() {
        mTabType = getArguments().getInt(UserPersonalActivity.KEY_TYPE_PERSONAL_TAB);
        tabTypeValue = PersonalTabType.values()[mTabType].toString();
        Og.d(TAG + ", get from intent, mTabType :" + tabTypeValue);
        return new PersonalListAdapter(getContext(), tabTypeValue) {
            @Override
            public void onViewDetachedFromWindow2(ViewHolder holder) {
                if (holder.isVideoItem()) {
                    mPlayDetector.removeTarget(holder.listPlayerView);
                }
            }

            @Override
            public void onViewAttachedToWindow2(ViewHolder holder) {
                if (holder.isVideoItem()) {
                    mPlayDetector.addTarget(holder.listPlayerView);
                }
            }

            @Override
            public void onStartFeedDetailActivity(Feed feed) {
                shouldPause = false;
            }
        };
    }


    @Override
    public void onLoadMore(@NonNull @NotNull RefreshLayout refreshLayout) {
        PagedList<Feed> currentList = mAdapter.getCurrentList();
        finishRefresh(currentList != null && currentList.size() > 0);
    }

    @Override
    public void onRefresh(@NonNull @NotNull RefreshLayout refreshLayout) {

    }

    @Override
    public void onDestroyView() {
        PageListPlayManager.removePageListPlay(tabTypeValue);
        super.onDestroyView();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (shouldPause) {
            mPlayDetector.onPause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        shouldPause = true;
        //从评论tab页跳转到 详情页之后再返回回来，咱们需要暂停视频播放。因为评论和tab页是没有视频的
        if (TextUtils.equals(tabTypeValue, PersonalTabType.TAB_COMMENT.toString())) {
            mPlayDetector.onPause();
        } else {
            mPlayDetector.onResume();
        }
    }
}
