package com.jesen.cod.jetpackvideo.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.jesen.cod.jetpackvideo.R;
import com.jesen.cod.jetpackvideo.exoplayer.PageListPlayDetector;
import com.jesen.cod.jetpackvideo.model.Feed;
import com.jesen.cod.jetpackvideo.ui.MutablePageKeyedDataSource;
import com.jesen.cod.jetpackvideo.ui.view.AbsListFragment;
import com.jesen.cod.jetpackvideo.utils.Og;
import com.jesen.cod.libnavannotation.FragmentDestination;
import com.scwang.smartrefresh.layout.api.RefreshLayout;

import org.jetbrains.annotations.NotNull;

import java.util.List;

@FragmentDestination(pageUrl = "main/tabs/home", asStarter = true)
public class HomeFragment extends AbsListFragment<Feed, HomeViewModel> {

    private HomeViewModel homeViewModel;
    private PageListPlayDetector playDetector;
    private static String mFeedType;

    public static HomeFragment newInstance(String feedType) {
        mFeedType = feedType;
        Bundle args = new Bundle();
        args.putString("feedType", feedType);
        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void afterCreateView() {


    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        Og.d("HomeFragment, onViewCreated");
        mViewModel.getCacheLiveData().observe(getViewLifecycleOwner(), new Observer<PagedList<Feed>>() {
            @Override
            public void onChanged(PagedList<Feed> feeds) {
                mAdapter.submitList(feeds);
            }
        });
         playDetector = new PageListPlayDetector(this, mRecyclerView);

    }

    @Override
    public PagedListAdapter getAdapter() {
        String feedType = getArguments() == null? "all":getArguments().getString("feedType");
        return new FeedAdapter(getContext(), feedType){
            @Override
            public void onViewAttachedToWindow(@NonNull @NotNull FeedAdapter.ViewHolder holder) {
                super.onViewAttachedToWindow(holder);
                if (holder.isVideoItem()){
                    playDetector.addTarget(holder.getListPlayerView());
                }
            }

            @Override
            public void onViewDetachedFromWindow(@NonNull @NotNull FeedAdapter.ViewHolder holder) {
                super.onViewDetachedFromWindow(holder);
                playDetector.removeTarget(holder.getListPlayerView());
            }
        };
    }

    @Override
    public void onLoadMore( @NotNull RefreshLayout refreshLayout) {
        Feed feed = mAdapter.getCurrentList().get(mAdapter.getItemCount() - 1);
        mViewModel.loadAfter(feed.id, new ItemKeyedDataSource.LoadCallback<Feed>() {
            @Override
            public void onResult(@NonNull @NotNull List<Feed> data) {
                PagedList.Config config = mAdapter.getCurrentList().getConfig();
                if (data !=null && data.size() > 0){
                    MutablePageKeyedDataSource dataSource = new MutablePageKeyedDataSource();
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
        playDetector.onPause();
        super.onPause();
    }

    @Override
    public void onResume() {
        Og.d("HomeFragment, onResume, feedType:"+ mFeedType);
        super.onResume();
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
    public void onDestroyView() {
        Og.d("HomeFragment, onDestroyView, feedType:"+ mFeedType);
        super.onDestroyView();
    }
}