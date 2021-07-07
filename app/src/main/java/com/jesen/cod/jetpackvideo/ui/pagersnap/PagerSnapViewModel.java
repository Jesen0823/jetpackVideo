package com.jesen.cod.jetpackvideo.ui.pagersnap;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;

import com.alibaba.fastjson.TypeReference;
import com.jesen.cod.jetpackvideo.AbsViewModel;
import com.jesen.cod.jetpackvideo.model.Feed;
import com.jesen.cod.jetpackvideo.ui.MutablePageKeyedDataSource;
import com.jesen.cod.jetpackvideo.ui.login.UserManager;
import com.jesen.cod.libcommon.utils.Og;
import com.jesen.cod.libnetwork.ApiResponse;
import com.jesen.cod.libnetwork.ApiService;
import com.jesen.cod.libnetwork.JsonCallback;
import com.jesen.cod.libnetwork.Request;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class PagerSnapViewModel extends AbsViewModel<Feed> {

    private static final String TAG = "PagerSnapViewModel";

    private volatile boolean withCache = true;

    private MutableLiveData<PagedList<Feed>> cacheLiveData = new MutableLiveData<>();
    // 同步位标记，防止数据重复
    private AtomicBoolean loadAfter = new AtomicBoolean(false);
    private String mFeedType = "video";

    public PagerSnapViewModel() {

    }

    @Override
    public DataSource createDataSource() {
        return new DataSource();
    }


    public void setFeedType(String feedType){
        mFeedType = feedType;
    }

    public MutableLiveData<PagedList<Feed>> getCacheLiveData(){
        return cacheLiveData;
    }

    @SuppressLint("RestrictedApi")
    public void loadAfter(int id, ItemKeyedDataSource.LoadCallback<Feed> callback){
        if (loadAfter.get()){
            callback.onResult(Collections.emptyList());
            return;
        }
        ArchTaskExecutor.getIOThreadExecutor().execute(() -> loadData(id, config.pageSize,callback));
    }




    private class DataSource extends ItemKeyedDataSource<Integer, Feed> {

        @Override
        public void loadInitial(@NonNull ItemKeyedDataSource.LoadInitialParams<Integer> params, @NonNull ItemKeyedDataSource.LoadInitialCallback<Feed> callback) {
            Og.d(TAG+", loadInitial:" );
            loadData(0, params.requestedLoadSize, callback);
            withCache = false;
        }

        @Override
        public void loadAfter(@NonNull ItemKeyedDataSource.LoadParams<Integer> params, @NonNull ItemKeyedDataSource.LoadCallback<Feed> callback) {
            Og.d(TAG+", loadAfter:" );
            loadData(params.key, params.requestedLoadSize,callback);
        }

        @Override
        public void loadBefore(@NonNull ItemKeyedDataSource.LoadParams<Integer> params, @NonNull ItemKeyedDataSource.LoadCallback<Feed> callback) {
            callback.onResult(Collections.emptyList());
        }

        @NonNull
        @Override
        public Integer getKey(@NonNull Feed item) {
            return item.id;
        }
    }

    private void loadData(int key, int count, ItemKeyedDataSource.LoadCallback<Feed> callback) {
        if (key > 0) { // 不加载分页
            loadAfter.set(true);
        }
        Request request = ApiService.get("/feeds/queryHotFeedsList")
                .addParams("feedType", mFeedType)
                .addParams("userId", UserManager.get().getUserId())
                .addParams("feedId", key)
                .addParams("pageCount", count)
                .addParams("pageCount", 10)
                .responseType(
                        new TypeReference<ArrayList<Feed>>() {
                        }.getType()
                );

        if (withCache) {
            request.cacheStrategy(Request.CACHE_ONLY);
            request.execute(new JsonCallback<List<Feed>>() {
                @Override
                public void onCacheSuccess(ApiResponse<List<Feed>> response) {
                    Og.d("HomeViewModel, loadData, onCacheSuccess.");
                    MutablePageKeyedDataSource dataSource = new MutablePageKeyedDataSource<Feed>();
                    dataSource.data.addAll(response.body);

                    PagedList pagedList = dataSource.buildNewPageList(config);
                    cacheLiveData.postValue(pagedList);
                }
            });
        }
        try {
            Request requestNet = withCache ? request.clone() : request;
            // 第1次加载 NET_CACHE，下拉刷新 NET_ONLY
            requestNet.cacheStrategy(key == 0 ? Request.NET_CACHE : Request.NET_ONLY);
            ApiResponse<List<Feed>> response = requestNet.execute();
            List<Feed> data = response.body == null ? Collections.emptyList() : response.body;
            Og.d("response.body size:" + response.body.size());
            callback.onResult(data);

            if (key > 0) {
                // 通过LiveData发送数据，告诉UI层是否应该主动关闭上拉加载分页的动画
                ((MutableLiveData) getBoundaryPageData()).postValue(data.size() > 0);
                loadAfter.set(false);
            }
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        Og.d("loadData, key = " + key);
    }
}














