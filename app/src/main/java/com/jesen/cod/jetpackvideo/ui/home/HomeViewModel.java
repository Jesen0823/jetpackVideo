package com.jesen.cod.jetpackvideo.ui.home;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;

import com.alibaba.fastjson.TypeReference;
import com.jesen.cod.jetpackvideo.AbsViewModel;
import com.jesen.cod.jetpackvideo.model.Feed;
import com.jesen.cod.jetpackvideo.utils.Og;
import com.jesen.cod.libnetwork.ApiResponse;
import com.jesen.cod.libnetwork.ApiService;
import com.jesen.cod.libnetwork.JsonCallback;
import com.jesen.cod.libnetwork.Request;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    @Override
    public DataSource createDataSource() {
        return mDataSource;
    }

    ItemKeyedDataSource<Integer,Feed> mDataSource = new ItemKeyedDataSource<Integer, Feed>() {
        @Override
        public void loadInitial(@NonNull @NotNull ItemKeyedDataSource.LoadInitialParams<Integer> params, @NonNull @NotNull ItemKeyedDataSource.LoadInitialCallback<Feed> callback) {
            // 加载初始化数据
            loadData(0, callback);
            withCache = false;
        }

        @Override
        public void loadAfter(@NonNull @NotNull ItemKeyedDataSource.LoadParams<Integer> params, @NonNull @NotNull ItemKeyedDataSource.LoadCallback<Feed> callback) {
            // 加载分页数据
            loadData(params.key, callback);
        }

        @Override
        public void loadBefore(@NonNull @NotNull ItemKeyedDataSource.LoadParams<Integer> params, @NonNull @NotNull ItemKeyedDataSource.LoadCallback<Feed> callback) {
            // 可以向前加载
            callback.onResult(Collections.emptyList());
        }

        @NonNull
        @NotNull
        @Override
        public Integer getKey(@NonNull @NotNull Feed item) {
            return item.id;
        }
    };

    private void loadData(int key, ItemKeyedDataSource.LoadCallback<Feed> callback) {
        Request request = ApiService.get("/feeds/queryHotFeedsList")
                .addParams("feedType", null)
                .addParams("userId", 0)
                .addParams("feedId", key)
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
                    super.onCacheSuccess(response);
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
                getBoundaryPageData().postValue(data.size()>0);
            }
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

}