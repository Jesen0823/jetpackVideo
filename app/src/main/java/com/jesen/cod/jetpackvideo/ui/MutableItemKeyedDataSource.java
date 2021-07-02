package com.jesen.cod.jetpackvideo.ui;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class MutableItemKeyedDataSource<Key, Value> extends ItemKeyedDataSource<Key, Value> {

    private ItemKeyedDataSource mDataSource;
    public List<Value> data = new ArrayList<>();


    public MutableItemKeyedDataSource(ItemKeyedDataSource dataSource) {

        mDataSource = dataSource;
    }

    public PagedList<Value> buildNewItemList(PagedList.Config config) {
        @SuppressLint("RestrictedApi")
        PagedList<Value> pagedList = new PagedList.Builder<Key, Value>(this, config)
                .setFetchExecutor(ArchTaskExecutor.getIOThreadExecutor())
                .setNotifyExecutor(ArchTaskExecutor.getMainThreadExecutor())
                .build();
        return pagedList;
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<Key> params, @NonNull LoadInitialCallback<Value> callback) {
        callback.onResult(data);
    }

    @Override
    public void loadAfter(@NonNull LoadParams<Key> params, @NonNull LoadCallback<Value> callback) {
        if (mDataSource != null) {
            //一旦 和当前DataSource关联的PagedList被提交到PagedListAdapter。那么ViewModel中创建的DataSource 就不会再被调用了
            //我们需要在分页的时候 代理一下 原来的DataSource，迫使其继续工作
            mDataSource.loadAfter(params, callback);
        }
    }

    @Override
    public void loadBefore(@NonNull LoadParams<Key> params, @NonNull LoadCallback<Value> callback) {
        callback.onResult(Collections.emptyList());
    }

    @NotNull
    @Override
    public abstract Key getKey(@NonNull @NotNull Value item);
}
