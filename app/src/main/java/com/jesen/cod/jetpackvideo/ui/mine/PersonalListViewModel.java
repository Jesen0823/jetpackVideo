package com.jesen.cod.jetpackvideo.ui.mine;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;

import com.alibaba.fastjson.TypeReference;
import com.jesen.cod.jetpackvideo.AbsViewModel;
import com.jesen.cod.jetpackvideo.model.Feed;
import com.jesen.cod.jetpackvideo.ui.login.UserManager;
import com.jesen.cod.libnetwork.ApiResponse;
import com.jesen.cod.libnetwork.ApiService;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PersonalListViewModel extends AbsViewModel<Feed> {

    private static final String URL_PERSONAL_LIST_FEED = "/feeds/queryProfileFeeds";
    private String profileType;

    public void setProfileType(String tabType) {
        this.profileType = tabType;
    }

    @Override
    public DataSource createDataSource() {
        return new DataSource();
    }

    private class DataSource extends ItemKeyedDataSource<Long, Feed> {

        @Override
        public void loadInitial(@NonNull LoadInitialParams<Long> params, @NonNull LoadInitialCallback<Feed> callback) {
            loadData(params.requestedInitialKey, callback);
        }

        @Override
        public void loadAfter(@NonNull LoadParams<Long> params, @NonNull LoadCallback<Feed> callback) {
            loadData(params.key, callback);
        }

        @Override
        public void loadBefore(@NonNull LoadParams<Long> params, @NonNull LoadCallback<Feed> callback) {
            callback.onResult(Collections.emptyList());
        }

        @NonNull
        @Override
        public Long getKey(@NonNull Feed item) {
            return item.itemId;
        }

        private void loadData(Long key, LoadCallback<Feed> callback) {
            // inId: 上次请求数据的最后一条的id
            ApiResponse<List<Feed>> response = ApiService.get(URL_PERSONAL_LIST_FEED)
                    .addParams("inId", key)
                    .addParams("userId", UserManager.get().getUserId())
                    .addParams("pageCount", 10)
                    .addParams("profileType", profileType)
                    .responseType(new TypeReference<ArrayList<Feed>>() {
                    }.getType())
                    .execute();
            List<Feed> result = response.body == null ? Collections.emptyList() : response.body;
            callback.onResult(result);

            if (key > 0) { // 分页加载，通知关闭加载动画
                ((MutableLiveData) getBoundaryPageData()).postValue(result.size() > 0);
            }
        }
    }
}
