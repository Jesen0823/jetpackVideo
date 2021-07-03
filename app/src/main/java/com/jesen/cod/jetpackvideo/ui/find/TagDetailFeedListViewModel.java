package com.jesen.cod.jetpackvideo.ui.find;

import androidx.annotation.NonNull;
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

public class TagDetailFeedListViewModel extends AbsViewModel<Feed> {

    // 帖子列表接口，跟首页一样，只是feedType传递的是tagType,用来请求同一tag下的帖子
    private static final String URL_HOT_LIST_BY_TAG = "/feeds/queryHotFeedsList";
    private String mFeedType;

    public void setFeedType(String feedType) {
        mFeedType = feedType;
    }

    @Override
    public DataSource createDataSource() {
        return new DataSource();
    }


    private class DataSource extends ItemKeyedDataSource<Integer, Feed> {

        @Override
        public void loadInitial(@NonNull LoadInitialParams<Integer> params, @NonNull LoadInitialCallback<Feed> callback) {
            loadData(params.requestedInitialKey, callback);
        }

        @Override
        public void loadAfter(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Feed> callback) {
            loadData(params.key, callback);
        }

        @Override
        public void loadBefore(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Feed> callback) {
            callback.onResult(Collections.emptyList());
        }

        @NonNull
        @Override
        public Integer getKey(@NonNull Feed item) {
            return item.id;
        }

        private void loadData(Integer feedId, LoadCallback<Feed> callback) {
            ApiResponse<List<Feed>> response = ApiService.get(URL_HOT_LIST_BY_TAG)
                    .addParams("userId", UserManager.get().getUserId())
                    .addParams("pageCount", 10)
                    .addParams("feedType", mFeedType)
                    .addParams("feedId", feedId)
                    .responseType(new TypeReference<ArrayList<Feed>>() {
                    }.getType())
                    .execute();
            List<Feed> result = response.body == null ? Collections.emptyList() : response.body;
            callback.onResult(result);
        }
    }
}
