package com.jesen.cod.jetpackvideo.ui.find;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;

import com.jesen.cod.jetpackvideo.R;
import com.jesen.cod.jetpackvideo.databinding.ActivityTagDetailFeedListBinding;
import com.jesen.cod.jetpackvideo.databinding.LayoutTagDetailFeedListHeaderBinding;
import com.jesen.cod.jetpackvideo.exoplayer.PageListPlayDetector;
import com.jesen.cod.jetpackvideo.exoplayer.PageListPlayManager;
import com.jesen.cod.jetpackvideo.model.Feed;
import com.jesen.cod.jetpackvideo.model.TagList;
import com.jesen.cod.jetpackvideo.ui.home.FeedAdapter;
import com.jesen.cod.libcommon.extention.AbsPagedListAdapter;
import com.jesen.cod.libcommon.utils.Og;
import com.jesen.cod.libcommon.utils.PixUtils;
import com.jesen.cod.libcommon.view.EmptyView;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.RefreshState;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.jetbrains.annotations.NotNull;

/*
 *  指定Tag下的帖子列表
 * */
public class TagDetailFeedListActivity extends AppCompatActivity implements View.OnClickListener, OnRefreshListener, OnLoadMoreListener {

    private static final String TAG = "TagDetailFeedListActivity";
    private static final String KEY_TAG_LIST = "key_tag_list";
    private static final String KEY_FEED_TAG_TYPE = "key_feed_tag_type";
    // 标题栏高度
    private static final int HEADER_TITLE_BAR_HEIGHT = 48;

    private ActivityTagDetailFeedListBinding mBinding;
    private RecyclerView mRecyclerView;
    private EmptyView mEmptyView;
    private SmartRefreshLayout mRefreshLayout;
    private TagList mTagList;
    private PageListPlayDetector mPlayDetector;
    private boolean shouldPause = true;
    private AbsPagedListAdapter mAdapter;
    private int totalScrollY;
    private TagDetailFeedListViewModel mFeedListViewModel;

    public static void startActivity(Context context, TagList tagList) {
        Intent intent = new Intent(context, TagDetailFeedListActivity.class);
        intent.putExtra(KEY_TAG_LIST, tagList);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_tag_detail_feed_list);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_tag_detail_feed_list);

        mRecyclerView = mBinding.refreshViewLayout.recyclerView;
        mEmptyView = mBinding.refreshViewLayout.emptyView;
        mRefreshLayout = mBinding.refreshViewLayout.refreshLayout;

        mBinding.backBtn.setOnClickListener(this);

        mTagList = (TagList) getIntent().getSerializableExtra(KEY_TAG_LIST);
        mBinding.setTagList(mTagList);
        mBinding.setOwner(this);

        mAdapter = (AbsPagedListAdapter) getAdapter();
        mRecyclerView.setAdapter(mAdapter);
        DividerItemDecoration decoration = new DividerItemDecoration(this, LinearLayoutManager.VERTICAL);
        decoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.list_divider));
        mRecyclerView.addItemDecoration(decoration);
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 监听下拉上拉动作，回调中处理
        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setOnLoadMoreListener(this);

        mFeedListViewModel = new ViewModelProvider(this)
                .get(TagDetailFeedListViewModel.class);
        Og.d(TAG + ", onCreate, mTagList.title: " + mTagList.title);
        mFeedListViewModel.setFeedType(mTagList.title);
        mFeedListViewModel.getPagedListLiveData().observe(this, new Observer<PagedList<Feed>>() {
            @Override
            public void onChanged(PagedList<Feed> feeds) {
                submitList(feeds);
            }
        });
        mFeedListViewModel.getBoundaryPageData().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean hasData) {
                finishRefresh(hasData);
            }
        });

        mPlayDetector = new PageListPlayDetector(this, mRecyclerView);

        addHeaderView();
    }

    private void submitList(PagedList<Feed> feeds) {
        if (feeds.size() > 0) {
            mAdapter.submitList(feeds);
        }
        finishRefresh(feeds.size() > 0);
    }

    private void finishRefresh(boolean hasdata) {
        PagedList currentList = mAdapter.getCurrentList();
        hasdata = currentList != null && currentList.size() > 0 || hasdata;
        if (hasdata) {
            mEmptyView.setVisibility(View.GONE);
        } else {
            mEmptyView.setVisibility(View.VISIBLE);
        }
        RefreshState state = mRefreshLayout.getState();
        if (state.isOpening && state.isHeader) {
            mRefreshLayout.finishRefresh();
        } else if (state.isOpening && state.isFooter) {
            mRefreshLayout.finishLoadMore();
        }
    }

    private void addHeaderView() {
        LayoutTagDetailFeedListHeaderBinding headerBinding = LayoutTagDetailFeedListHeaderBinding
                .inflate(LayoutInflater.from(this), mRecyclerView, false);
        headerBinding.setTagList(mTagList);
        headerBinding.setLifecycleOwner(this);
        mAdapter.addHeaderView(headerBinding.getRoot());

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull @NotNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalScrollY += dy;
                if (totalScrollY > PixUtils.dp2px(HEADER_TITLE_BAR_HEIGHT)) {
                    mBinding.tagLogo.setVisibility(View.VISIBLE);
                    mBinding.tagTitle.setVisibility(View.VISIBLE);
                    mBinding.followTagBtn.setVisibility(View.VISIBLE);
                    mBinding.backBtn.setColorFilter(Color.BLACK);
                } else {
                    mBinding.tagLogo.setVisibility(View.GONE);
                    mBinding.tagTitle.setVisibility(View.GONE);
                    mBinding.followTagBtn.setVisibility(View.GONE);
                    mBinding.backBtn.setColorFilter(Color.WHITE);
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        finish();
    }

    /*
     * 复用了首页的Adapter
     * */
    public PagedListAdapter getAdapter() {
        return new FeedAdapter(this, KEY_FEED_TAG_TYPE) {
            @Override
            public void onViewAttachedToWindow2(@NonNull FeedAdapter.ViewHolder holder) {
                if (holder.isVideoItem()) {
                    mPlayDetector.addTarget(holder.getListPlayerView());
                }
            }

            @Override
            public void onViewDetachedFromWindow2(@NonNull ViewHolder holder) {
                mPlayDetector.removeTarget(holder.getListPlayerView());
            }

            @Override
            public void onStartFeedDetailActivity(Feed feed) {
                boolean isVideo = feed.itemType == Feed.TYPE_VIDEO;
                // 为了无缝续播，视频类型跳转页面，不暂停
                shouldPause = !isVideo;
            }

            @Override
            public void onCurrentListChanged(@Nullable PagedList<Feed> previousList, @Nullable PagedList<Feed> currentList) {
                //这个方法是在我们每提交一次 pagelist对象到adapter 就会触发一次
                //每调用一次 adpater.submitlist
                if (previousList != null && currentList != null) {
                    if (!currentList.containsAll(previousList)) {
                        mRecyclerView.scrollToPosition(0);
                    }
                }
            }
        };
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (shouldPause) {
            mPlayDetector.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPlayDetector.onResume();
    }

    @Override
    protected void onDestroy() {
        PageListPlayManager.removePageListPlay(KEY_FEED_TAG_TYPE);
        super.onDestroy();
    }

    @Override
    public void onRefresh(@NonNull @NotNull RefreshLayout refreshLayout) {
        // 触发重新加载
        mFeedListViewModel.getDataSource().invalidate();
    }

    @Override
    public void onLoadMore(@NonNull @NotNull RefreshLayout refreshLayout) {
        PagedList currentList = getAdapter().getCurrentList();
        finishRefresh(currentList != null && currentList.size() > 0);
        // 都委托给了Paging 框架
    }
}