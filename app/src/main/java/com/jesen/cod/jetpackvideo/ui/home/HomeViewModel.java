package com.jesen.cod.jetpackvideo.ui.home;

import android.annotation.SuppressLint;
import android.util.Log;

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
import com.jesen.cod.jetpackvideo.utils.Og;
import com.jesen.cod.libnetwork.ApiResponse;
import com.jesen.cod.libnetwork.ApiService;
import com.jesen.cod.libnetwork.JsonCallback;
import com.jesen.cod.libnetwork.Request;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class HomeViewModel extends AbsViewModel<Feed> {

    /*
    * DataSource<Key, Value>数据源：
    * key对应加载数据的条件信息，value对应数据实体
    *
    * PageKeyedDataSource<Key,Value> 适用于目标数据根据，页信息请求数据的场景，根据页码，下拉第2,3...页
    * ItemKeyedDataSource<Key,Value> 适用于目标数据加载依赖特定item信息的场景
    * PositionalDataSource<T> 使用于目标数据总数固定，通过特定位置加载数据
    *
    * */

    private volatile boolean withCache = true;

    private MutableLiveData<PagedList<Feed>>  cacheLiveData = new MutableLiveData<>();
    // 同步位标记，防止数据重复
    private AtomicBoolean loadAfter = new AtomicBoolean(false);
    private String mFeedType;

    @Override
    public DataSource createDataSource() {
        return new FeedDataSource();
    }

    public void setFeedType(String feedType){
        mFeedType = feedType;
    }

    private void loadData(int key,int count, ItemKeyedDataSource.LoadCallback<Feed> callback) {
        if (key > 0){ // 不加载分页
            loadAfter.set(true);
        }
        Request request = ApiService.get("/feeds/queryHotFeedsList")
                .addParams("feedType", mFeedType)
                .addParams("userId", UserManager.get().getUserId())
                .addParams("feedId", key)
                .addParams("pageCount",count)
                .addParams("pageCount", 10)
                .responseType(
                        new TypeReference<ArrayList<Feed>>() {
                        }.getType()
                );

        if (withCache){
            request.cacheStrategy(Request.CACHE_ONLY);
            request.execute(new JsonCallback<List<Feed>>() {
                @Override
                public void onCacheSuccess(ApiResponse<List<Feed>> response) {
                    Og.d("HomeViewModel, loadData, onCacheSuccess.");
                    MutablePageKeyedDataSource dataSource = new MutablePageKeyedDataSource<Feed>();
                    if(response.body != null) {
                        dataSource.data.addAll(response.body);
                    }

                    PagedList pagedList = dataSource.buildNewPageList(config);
                    cacheLiveData.postValue(pagedList);
                }
            });
        }
        try {
            Request requestNet = withCache? request.clone():request;
            // 第1次加载 NET_CACHE，下拉刷新 NET_ONLY
            requestNet.cacheStrategy(key == 0? Request.NET_CACHE:Request.NET_ONLY);
            ApiResponse<List<Feed>> response = requestNet.execute();
            List<Feed> data = response.body == null? Collections.emptyList():response.body;
            Og.d("response.body size:"+response.body.size());
            callback.onResult(data);

            if (key > 0){
                // 通过LiveData发送数据，告诉UI层是否应该主动关闭上拉加载分页的动画
                ((MutableLiveData)getBoundaryPageData()).postValue(data.size()>0);
                loadAfter.set(false);
            }
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        Og.d("loadData, key = "+key);
    }

    public MutableLiveData<PagedList<Feed>> getCacheLiveData() {
        return cacheLiveData;
    }

    @SuppressLint("RestrictedApi")
    public void loadAfter(int id, ItemKeyedDataSource.LoadCallback<Feed> feedLoadCallback) {
        if (loadAfter.get()){
            feedLoadCallback.onResult(Collections.emptyList());
            return;
        }
        ArchTaskExecutor.getIOThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                loadData(id,config.pageSize ,feedLoadCallback);
            }
        });
    }

    class FeedDataSource extends ItemKeyedDataSource<Integer, Feed>{

        @Override
        public void loadInitial(@NonNull LoadInitialParams<Integer> params, @NonNull LoadInitialCallback<Feed> callback) {
            //加载初始化数据的
            Log.e("homeviewmodel", "loadInitial: ");
            loadData(0, params.requestedLoadSize, callback);
            withCache = false;
        }

        @Override
        public void loadAfter(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Feed> callback) {
            //向后加载分页数据的
            Log.e("homeviewmodel", "loadAfter: ");
            loadData(params.key, params.requestedLoadSize, callback);
        }

        @Override
        public void loadBefore(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Feed> callback) {
            callback.onResult(Collections.emptyList());
            //能够向前加载数据的
        }

        @NonNull
        @Override
        public Integer getKey(@NonNull Feed item) {
            return item.id;
        }
    }
}