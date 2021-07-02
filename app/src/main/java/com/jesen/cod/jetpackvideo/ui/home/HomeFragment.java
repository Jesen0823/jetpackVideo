package com.jesen.cod.jetpackvideo.ui.home;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;

import com.jesen.cod.jetpackvideo.exoplayer.PageListPlayDetector;
import com.jesen.cod.jetpackvideo.exoplayer.PageListPlayManager;
import com.jesen.cod.jetpackvideo.model.Feed;
import com.jesen.cod.jetpackvideo.ui.MutablePageKeyedDataSource;
import com.jesen.cod.jetpackvideo.ui.view.AbsListFragment;
import com.jesen.cod.libcommon.utils.Og;
import com.jesen.cod.libnavannotation.FragmentDestination;
import com.scwang.smartrefresh.layout.api.RefreshLayout;

import org.jetbrains.annotations.NotNull;

import java.util.List;

@FragmentDestination(pageUrl = "main/tabs/home", asStarter = true)
public class HomeFragment extends AbsListFragment<Feed, HomeViewModel> {

    private PageListPlayDetector playDetector;
    private String mFeedType;
    private boolean shouldPause = true;


    public static HomeFragment newInstance(String feedType) {
       Og.d("HomeFragment, feedType: "+feedType);
        Bundle args = new Bundle();
        args.putString("feedType", feedType);
        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        Og.d("HomeFragment, onViewCreated");
        mViewModel.getCacheLiveData().observe(getViewLifecycleOwner(), new Observer<PagedList<Feed>>() {
            @Override
            public void onChanged(PagedList<Feed> feeds) {
                submitList(feeds);
            }
        });
         playDetector = new PageListPlayDetector(this, mRecyclerView);
         mViewModel.setFeedType(mFeedType);
    }

    @Override
    public PagedListAdapter getAdapter() {
        mFeedType = getArguments() == null ? "all" : getArguments().getString("feedType");
        return new FeedAdapter(getContext(), mFeedType) {
            @Override
            public void onViewAttachedToWindow2(@NonNull ViewHolder holder) {
                if (holder.isVideoItem()) {
                    playDetector.addTarget(holder.getListPlayerView());
                }
            }

            @Override
            public void onViewDetachedFromWindow2(@NonNull ViewHolder holder) {
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
    public void onLoadMore( @NotNull RefreshLayout refreshLayout) {
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
                if (data !=null && data.size() > 0){
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
    public void onRefresh(@NotNull RefreshLayout refreshLayout) {
        //invalidate 之后Paging会重新创建一个DataSource 重新调用它的loadInitial方法加载初始化数据
        //详情见：LivePagedListBuilder#compute方法
        mViewModel.getDataSource().invalidate();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden){
            playDetector.onPause();
        }else {
            playDetector.onResume();
        }
    }

    @Override
    public void onPause() {
        Og.d("HomeFragment, onPause, feedType:"+ mFeedType);
        //如果是跳转到详情页,不暂停视频播放
        //如果是前后台切换 或者去别的页面了 都是需要暂停视频播放的
        if (shouldPause) {
            playDetector.onPause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        Og.d("HomeFragment, onResume, feedType:"+ mFeedType);
        super.onResume();
        shouldPause = true;
        if (getParentFragment() != null){
            if (getParentFragment().isVisible() && isVisible()){
                playDetector.onResume();
            }
        }else {
            if (isVisible()){
                playDetector.onResume();
            }
        }

    }

    @Override
    public void onDestroy() {
        Og.d("HomeFragment, onDestroy, feedType:"+ mFeedType);
        PageListPlayManager.removePageListPlay(mFeedType);
        super.onDestroy();
    }
}