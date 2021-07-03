package com.jesen.cod.jetpackvideo.ui.find;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;

import com.jesen.cod.jetpackvideo.R;
import com.jesen.cod.jetpackvideo.model.TagList;
import com.jesen.cod.jetpackvideo.ui.MutableItemKeyedDataSource;
import com.jesen.cod.jetpackvideo.ui.view.AbsListFragment;
import com.jesen.cod.libcommon.view.EmptyView;
import com.scwang.smartrefresh.layout.api.RefreshLayout;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FindTagListFragment extends AbsListFragment<TagList, FindTagListViewModel> {

    public static final String KEY_TAG_TYPE = "key_tag_type";
    private String mTagType;

    public static FindTagListFragment newInstance(String tagType) {
        Bundle args = new Bundle();
        args.putString(KEY_TAG_TYPE, tagType);
        FindTagListFragment fragment = new FindTagListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (TextUtils.equals(mTagType, "onlyFollow")){
            mEmptyView.setTitle(getString(R.string.tag_list_no_follow));
            mEmptyView.setActionBtn(getString(R.string.tag_list_no_follow_button), new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // 需要和FindFragment页面通信,ViewModel中定义一个发送事件的方法
                    mViewModel.getSwitchTabLiveData().setValue(new Object());
                }
            });
        }
        mViewModel.setTagType(mTagType);
    }

    @Override
    public PagedListAdapter getAdapter() {
        mTagType = getArguments().getString(KEY_TAG_TYPE);
        FindTagListAdapter tagListAdapter = new FindTagListAdapter(getContext());
        return tagListAdapter;
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        PagedList<TagList> currentList = getAdapter().getCurrentList();
        long currentTagId = currentList == null ? 0 : currentList.get(currentList.size() - 1).tagId;
        mViewModel.loadData(currentTagId, new ItemKeyedDataSource.LoadCallback() {

            @Override
            public void onResult(@NonNull List data) {
                MutableItemKeyedDataSource<Long, TagList> mutableItemKeyedDataSource 
                        = new MutableItemKeyedDataSource<Long, TagList>((ItemKeyedDataSource) mViewModel.getDataSource()) {

                    @Override
                    public @NotNull Long getKey(@NonNull TagList item) {
                        return item.tagId;
                    }
                };
                
                // 传入当前列表和本次请求得到的列表,进行差分异
                mutableItemKeyedDataSource.data.addAll(currentList);
                mutableItemKeyedDataSource.data.addAll(data);
                PagedList<TagList> newTagLists = mutableItemKeyedDataSource.buildNewItemList(currentList.getConfig());
                if (data.size()>0){
                    submitList(newTagLists);
                }
            }
        });
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        mViewModel.getDataSource().invalidate();
    }
}
