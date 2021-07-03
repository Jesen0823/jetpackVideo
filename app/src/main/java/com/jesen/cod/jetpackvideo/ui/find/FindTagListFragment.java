package com.jesen.cod.jetpackvideo.ui.find;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;

import com.jesen.cod.jetpackvideo.model.TagList;
import com.jesen.cod.jetpackvideo.ui.view.AbsListFragment;
import com.scwang.smartrefresh.layout.api.RefreshLayout;

import org.jetbrains.annotations.NotNull;

public class FindTagListFragment extends AbsListFragment<TagList,FindTagListViewModel> {

    public static final String KEY_TAG_TYPE = "key_tag_type";

    public static FindTagListFragment newInstance(String tagType) {
        Bundle args = new Bundle();
        args.putString(KEY_TAG_TYPE, tagType);
        FindTagListFragment fragment = new FindTagListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public PagedListAdapter getAdapter() {
        return null;
    }

    @Override
    public void onLoadMore(@NonNull @NotNull RefreshLayout refreshLayout) {

    }

    @Override
    public void onRefresh(@NonNull @NotNull RefreshLayout refreshLayout) {

    }
}
