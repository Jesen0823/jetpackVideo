package com.jesen.cod.jetpackvideo.ui.detail;

import androidx.annotation.NonNull;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;

import com.alibaba.fastjson.TypeReference;
import com.jesen.cod.jetpackvideo.AbsViewModel;
import com.jesen.cod.jetpackvideo.model.Comment;
import com.jesen.cod.jetpackvideo.ui.login.UserManager;
import com.jesen.cod.libcommon.utils.Og;
import com.jesen.cod.libnetwork.ApiResponse;
import com.jesen.cod.libnetwork.ApiService;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FeedDetailViewModel extends AbsViewModel<Comment> {

    private static final String TAG = "FeedDetailViewModel";

    private long mItemId;

    private static final String COMMENT_LIST_URL = "/comment/queryFeedComments";

    @Override
    public DataSource createDataSource() {
        return new DataSource();
    }

    class DataSource extends ItemKeyedDataSource<Integer, Comment> {

        @Override
        public void loadInitial(@NonNull @NotNull LoadInitialParams<Integer> params, @NonNull @NotNull LoadInitialCallback<Comment> callback) {
            loadData(params.requestedInitialKey, params.requestedLoadSize, callback);
        }

        @Override
        public void loadAfter(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Comment> callback) {
            if (params.key > 0) {
                loadData(params.key, params.requestedLoadSize, callback);
            }
        }

        private void loadData(Integer key, int requestedLoadSize, LoadCallback<Comment> callback) {
            ApiResponse<List<Comment>> response = ApiService.get(COMMENT_LIST_URL)
                    .addParams("id", key)
                    .addParams("itemId", mItemId)
                    .addParams("userId", UserManager.get().getUserId())
                    .addParams("pageCount", requestedLoadSize)
                    .responseType(new TypeReference<ArrayList<Comment>>() {
                    }.getType())
                    .execute();

            List<Comment> list = response.body == null ? Collections.emptyList() : response.body;
            callback.onResult(list);
        }

        @Override
        public void loadBefore(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Comment> callback) {
            callback.onResult(Collections.emptyList());
        }

        @NonNull
        @NotNull
        @Override
        public Integer getKey(@NonNull Comment item) {
            return item.id;
        }
    }

    public void setItemId(long itemId) {
        mItemId = itemId;
    }
}
