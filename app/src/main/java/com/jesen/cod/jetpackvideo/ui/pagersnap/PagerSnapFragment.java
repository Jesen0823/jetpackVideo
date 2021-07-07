package com.jesen.cod.jetpackvideo.ui.pagersnap;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.jesen.cod.jetpackvideo.R;
import com.jesen.cod.jetpackvideo.exoplayer.PageListPlayDetector;
import com.jesen.cod.jetpackvideo.exoplayer.PageListPlayManager;
import com.jesen.cod.jetpackvideo.model.Feed;
import com.jesen.cod.jetpackvideo.ui.MutablePageKeyedDataSource;
import com.jesen.cod.jetpackvideo.ui.home.FeedAdapter;
import com.jesen.cod.jetpackvideo.ui.pagersnap.PagerSnapHelperAdapter;
import com.jesen.cod.jetpackvideo.ui.pagersnap.PagerSnapViewModel;
import com.jesen.cod.jetpackvideo.ui.view.AbsListFragment;
import com.jesen.cod.libcommon.utils.Og;
import com.scwang.smartrefresh.layout.api.RefreshLayout;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PagerSnapFragment extends AbsListFragment<Feed, PagerSnapViewModel> {

    private static final String TAG = "PagerSnapFragment";
    public static final String KEY_FEED_TYPE_SNAP = "snap_feedType";
    private PageListPlayDetector playDetector;
    private String mFeedType;
    private boolean shouldPause = true;
    // 是否点击了返回键
    private boolean backPressed;

    public static PagerSnapFragment newInstance(String feedType) {
        Og.d(TAG + ",newInstance feedType:" + feedType);
        Bundle args = new Bundle();
        args.putString(KEY_FEED_TYPE_SNAP, feedType);
        PagerSnapFragment fragment = new PagerSnapFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void extendSettingView() {
        mRecyclerView.setNestedScrollingEnabled(false);

        mFeedType = getArguments() == null ? "video" : getArguments().getString(KEY_FEED_TYPE_SNAP);

        PagerSnapHelper snapHelper = new PagerSnapHelper() {
            // 在 Adapter的 onBindViewHolder 之后执行
            @Override
            public int findTargetSnapPosition(RecyclerView.LayoutManager layoutManager, int velocityX, int velocityY) {
                // 找到对应的Index
                int targetPos = super.findTargetSnapPosition(layoutManager, velocityX, velocityY);
                Og.d(TAG + ", onCreateView extendSettingView:, targetPos " + targetPos);
                Toast.makeText(getContext(), "滑到到 " + targetPos + "位置", Toast.LENGTH_SHORT).show();
                return targetPos;
            }

            // 在 Adapter的 onBindViewHolder 之后执行
            @Nullable
            @Override
            public View findSnapView(RecyclerView.LayoutManager layoutManager) {
                // 找到对应的View
                View view = super.findSnapView(layoutManager);
                Og.d(TAG + ", extendSettingView findSnapView, tag: " + view.getTag());
                return view;
            }
        };
        snapHelper.attachToRecyclerView(mRecyclerView);

        // ---布局管理器---
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        // 默认是Vertical (HORIZONTAL则为横向列表)
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mRecyclerView.setLayoutManager(linearLayoutManager);

        // 这么写是为了获取RecycleView的宽高
        mRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    mRecyclerView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    mRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }

                /**
                 *  这么写是为了获取RecycleView的宽高
                 */
                // mRecyclerView.setAdapter(mAdapter);
            }
        });
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable Bundle savedInstanceState) {
        mViewModel.getCacheLiveData().observe(getViewLifecycleOwner(), new Observer<PagedList<Feed>>() {
            @Override
            public void onChanged(PagedList<Feed> feeds) {
                submitList(feeds);
            }
        });
        playDetector = new PageListPlayDetector(this, mRecyclerView);
        mViewModel.setFeedType(mFeedType);
        // 禁止下拉刷新
        mRefreshLayout.setEnableRefresh(false);
    }

    @Override
    public PagedListAdapter getAdapter() {
        return new PagerSnapHelperAdapter(getContext(), mFeedType) {
            @Override
            public void onViewAttachedToWindow2(ViewHolder holder) {
                if (holder.isVideoItem()) {
                    playDetector.addTarget(holder.getListPlayerView());
                }
            }

            @Override
            public void onViewDetachedFromWindow2(@NonNull PagerSnapHelperAdapter.ViewHolder holder) {
                playDetector.removeTarget(holder.getListPlayerView());
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
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onLoadMore(@NonNull @NotNull RefreshLayout refreshLayout) {
        final PagedList<Feed> currentList = mAdapter.getCurrentList();
        if (currentList == null || currentList.size() <= 0) {
            finishRefresh(false);
            return;
        }

        Feed feed = currentList.get(mAdapter.getItemCount() - 1);
        mViewModel.loadAfter(feed.id, new ItemKeyedDataSource.LoadCallback<Feed>() {
            @Override
            public void onResult(@NonNull @NotNull List<Feed> data) {
                PagedList.Config config = currentList.getConfig();
                if (data != null && data.size() > 0) {
                    //这里 咱们手动接管 分页数据加载的时候 使用MutableItemKeyedDataSource也是可以的。
                    //由于当且仅当 paging不再帮我们分页的时候，我们才会接管。所以 就不需要ViewModel中创建的DataSource继续工作了，所以使用
                    //MutablePageKeyedDataSource也是可以的
                    MutablePageKeyedDataSource dataSource = new MutablePageKeyedDataSource();
                    //这里要把列表上已经显示的先添加到dataSource.data中
                    //而后把本次分页回来的数据再添加到dataSource.data中
                    dataSource.data.addAll(currentList);
                    dataSource.data.addAll(data);
                    PagedList pagedList = dataSource.buildNewPageList(config);
                    submitList(pagedList);
                }
            }
        });
    }

    @Override
    public void onRefresh(@NonNull @NotNull RefreshLayout refreshLayout) {
        //invalidate 之后Paging会重新创建一个DataSource 重新调用它的loadInitial方法加载初始化数据
        //详情见：LivePagedListBuilder#compute方法
        mViewModel.getDataSource().invalidate();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        Og.d(TAG + ", onHiddenChanged, hidden: " + hidden);
        super.onHiddenChanged(hidden);
        if (hidden) {
            playDetector.onPause();
        } else {
            playDetector.onResume();
        }
    }

    @Override
    public void onPause() {
        Og.d(  TAG+", onPause, feedType:" + mFeedType);
        playDetector.onPause();
        super.onPause();
    }

    @Override
    public void onResume() {
        Og.d(  TAG+", onResume, feedType:" + mFeedType);
        super.onResume();
        playDetector.onResume();
    }

    @Override
    public void onDestroy() {
        Og.d(  TAG+", onDestroy, feedType:" + mFeedType);
        PageListPlayManager.removePageListPlay(mFeedType);
        super.onDestroy();
    }
}