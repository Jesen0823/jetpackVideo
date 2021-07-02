package com.jesen.cod.jetpackvideo.ui;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.paging.PageKeyedDataSource;
import androidx.paging.PagedList;

import com.jesen.cod.jetpackvideo.model.Feed;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MutablePageKeyedDataSource<Value> extends PageKeyedDataSource<Integer, Value> {

    public List<Value> data = new ArrayList<>();

    public PagedList<Value> buildNewPageList(PagedList.Config config){
        @SuppressLint("RestrictedApi")
        PagedList<Value> pagedList = new PagedList.Builder<Integer, Value>(this, config)
                .setFetchExecutor(ArchTaskExecutor.getIOThreadExecutor())
                .setNotifyExecutor(ArchTaskExecutor.getMainThreadExecutor())
                .build();
        return pagedList;
    }

    @Override
    public void loadInitial(@NonNull @NotNull PageKeyedDataSource.LoadInitialParams<Integer> params, @NonNull @NotNull PageKeyedDataSource.LoadInitialCallback<Integer, Value> callback) {
        callback.onResult(data, null, null);
    }

    @Override
    public void loadBefore(@NonNull @NotNull PageKeyedDataSource.LoadParams<Integer> params, @NonNull @NotNull PageKeyedDataSource.LoadCallback<Integer, Value> callback) {
        callback.onResult(Collections.emptyList(), null);
    }

    @Override
    public void loadAfter(@NonNull @NotNull PageKeyedDataSource.LoadParams<Integer> params, @NonNull @NotNull PageKeyedDataSource.LoadCallback<Integer, Value> callback) {
        callback.onResult(Collections.emptyList(), null);
    }
}
