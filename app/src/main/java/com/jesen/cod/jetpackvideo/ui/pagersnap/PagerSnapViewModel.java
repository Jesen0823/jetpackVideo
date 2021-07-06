package com.jesen.cod.jetpackvideo.ui.dashboard;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;

import com.jesen.cod.jetpackvideo.AbsViewModel;
import com.jesen.cod.jetpackvideo.model.Feed;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PagerSnapViewModel extends AbsViewModel<Feed> {

    private MutableLiveData<String> mText;

    private MutableLiveData<List<String>> mDataList;

    public PagerSnapViewModel() {
        mDataList = new MutableLiveData<>();
        List<String> data = new ArrayList<>();
        for (int i = 1; i < 100; i++) {
            data.add("item" + i);
        }
        mDataList.setValue(data);
    }

    @Override
    public DataSource createDataSource() {
        return null;
    }

    public LiveData<List<String>> getData() {
        return mDataList;
    }


    private class DataSource extends ItemKeyedDataSource<Integer,Feed>{

        @Override
        public void loadInitial(@NonNull @NotNull ItemKeyedDataSource.LoadInitialParams<Integer> params, @NonNull @NotNull ItemKeyedDataSource.LoadInitialCallback<Feed> callback) {

        }

        @Override
        public void loadAfter(@NonNull @NotNull ItemKeyedDataSource.LoadParams<Integer> params, @NonNull @NotNull ItemKeyedDataSource.LoadCallback<Feed> callback) {

        }

        @Override
        public void loadBefore(@NonNull @NotNull ItemKeyedDataSource.LoadParams<Integer> params, @NonNull @NotNull ItemKeyedDataSource.LoadCallback<Feed> callback) {

        }

        @NonNull
        @NotNull
        @Override
        public Integer getKey(@NonNull @NotNull Feed item) {
            return null;
        }
    }
}














