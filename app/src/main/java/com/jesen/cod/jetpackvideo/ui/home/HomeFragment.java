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
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.jesen.cod.jetpackvideo.R;
import com.jesen.cod.jetpackvideo.model.Feed;
import com.jesen.cod.jetpackvideo.ui.view.AbsListFragment;
import com.jesen.cod.jetpackvideo.utils.Og;
import com.jesen.cod.libnavannotation.FragmentDestination;
import com.scwang.smartrefresh.layout.api.RefreshLayout;

import org.jetbrains.annotations.NotNull;

@FragmentDestination(pageUrl = "main/tabs/home", asStarter = true)
public class HomeFragment extends AbsListFragment<Feed, HomeViewModel> {

    private HomeViewModel homeViewModel;

    public static HomeFragment newInstance(String feedType) {
        Bundle args = new Bundle();
        args.putString("feedType", feedType);
        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void afterCreateView() {
        Og.d("HomeFragment, afterCreateView");


    }

    @Override
    public PagedListAdapter getAdapter() {
        String feedType = getArguments() == null? "all":getArguments().getString("feedType");
        return new FeedAdapter(getContext(), feedType);
    }

    @Override
    public void onLoadMore( @NotNull RefreshLayout refreshLayout) {

    }

    @Override
    public void onRefresh(@NotNull RefreshLayout refreshLayout) {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}