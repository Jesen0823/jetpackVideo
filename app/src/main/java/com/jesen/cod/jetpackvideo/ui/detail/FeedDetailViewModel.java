package com.jesen.cod.jetpackvideo.ui.detail;

import androidx.annotation.NonNull;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;

import com.alibaba.fastjson.TypeReference;
import com.jesen.cod.jetpackvideo.AbsViewModel;
import com.jesen.cod.jetpackvideo.model.Comment;
import com.jesen.cod.jetpackvideo.ui.login.UserManager;
import com.jesen.cod.libnetwork.ApiResponse;
import com.jesen.cod.libnetwork.ApiService;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FeedDetailViewModel extends AbsViewModel<Comment> {

    private long mItemId;

    private static final String COMMENT_LIST_URL = "/comment/queryFeedComments";

    @Override
    public DataSource createDataSource() {
        return null;
    }

    class DataSource extends ItemKeyedDataSource<Integer, Comment>{

        @Override
        public void loadInitial(@NonNull @NotNull ItemKeyedDataSource.LoadInitialParams<Integer> params, @NonNull @NotNull ItemKeyedDataSource.LoadInitialCallback<Comment> callback) {
            loadData(params.requestedInitialKey, params.requestedLoadSize,callback);
        }

        private void loadData(Integer requestedInitialKey, int requestedLoadSize, LoadCallback<Comment> callback) {
           ApiResponse<List<Comment>> response = ApiService.get(COMMENT_LIST_URL)
            .addParams("id",requestedInitialKey)
                    .addParams("itemId",mItemId)
                    .addParams("userId", UserManager.get().getUserId())
                    .addParams("pageCount",requestedLoadSize)
                    .responseType(new TypeReference<ArrayList<Comment>>(){}.getType())
                    .execute();

           List<Comment> list = response.body==null?Collections.emptyList():response.body;
           callback.onResult(list);
        }

        @Override
        public void loadAfter(@NonNull @NotNull ItemKeyedDataSource.LoadParams<Integer> params, @NonNull @NotNull ItemKeyedDataSource.LoadCallback<Comment> callback) {
            loadData(params.key, params.requestedLoadSize,callback);
        }

        @Override
        public void loadBefore(@NonNull @NotNull ItemKeyedDataSource.LoadParams<Integer> params, @NonNull @NotNull ItemKeyedDataSource.LoadCallback<Comment> callback) {
            callback.onResult(Collections.emptyList());
        }

        @NonNull
        @NotNull
        @Override
        public Integer getKey(@NonNull @NotNull Comment item) {
            return item.id;
        }
    }

    public void setItemId(long itemId) {
        mItemId = itemId;
    }
}
