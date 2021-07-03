package com.jesen.cod.jetpackvideo.ui.find;

import androidx.annotation.NonNull;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;

import com.jesen.cod.jetpackvideo.AbsViewModel;
import com.jesen.cod.jetpackvideo.model.TagList;

import org.jetbrains.annotations.NotNull;

/*
* 选择ItemKey类型的DataSource
* */

public class FindTagListViewModel extends AbsViewModel<TagList> {

    private static final String URL_HOT_FEED_LIST = "/feeds/queryHotFeedsList";

    @Override
    public DataSource createDataSource() {
        return null;
    }

    // Key设为long,即item列表上最后一条Item的id信息,Value是数据载体
    private class DataSource extends ItemKeyedDataSource<Long, TagList>{

        @Override
        public void loadInitial(@NonNull @NotNull ItemKeyedDataSource.LoadInitialParams<Long> params, @NonNull @NotNull ItemKeyedDataSource.LoadInitialCallback<TagList> callback) {

        }

        @Override
        public void loadAfter(@NonNull @NotNull ItemKeyedDataSource.LoadParams<Long> params, @NonNull @NotNull ItemKeyedDataSource.LoadCallback<TagList> callback) {

        }

        @Override
        public void loadBefore(@NonNull @NotNull ItemKeyedDataSource.LoadParams<Long> params, @NonNull @NotNull ItemKeyedDataSource.LoadCallback<TagList> callback) {

        }

        @NonNull
        @NotNull
        @Override
        public Long getKey(@NonNull @NotNull TagList item) {
            return null;
        }
    }
}
