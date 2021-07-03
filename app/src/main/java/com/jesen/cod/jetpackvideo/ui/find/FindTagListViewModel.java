package com.jesen.cod.jetpackvideo.ui.find;

import android.annotation.SuppressLint;
import android.os.Build;
import android.text.Html;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;

import com.alibaba.fastjson.TypeReference;
import com.jesen.cod.jetpackvideo.AbsViewModel;
import com.jesen.cod.jetpackvideo.R;
import com.jesen.cod.jetpackvideo.model.TagList;
import com.jesen.cod.jetpackvideo.ui.login.UserManager;
import com.jesen.cod.jetpackvideo.utils.StringUtil;
import com.jesen.cod.libcommon.JetAppGlobal;
import com.jesen.cod.libnetwork.ApiResponse;
import com.jesen.cod.libnetwork.ApiService;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/*
 * 选择ItemKey类型的DataSource,是因为分页的时候需要依赖上次列表的tagId信息
 * */

public class FindTagListViewModel extends AbsViewModel<TagList> {

    private static final String URL_QUERY_TAG_LIST = "/tag/queryTagList";
    // tab类型，“关注” or "推荐"
    private String mTagType;
    private int offSet;
    private AtomicBoolean loadAfter = new AtomicBoolean();
    private MutableLiveData switchTabLiveData = new MutableLiveData();

    @Override
    public DataSource createDataSource() {
        return new DataSource();
    }

    public void setTagType(String tagType) {
        mTagType = tagType;
    }

    public MutableLiveData getSwitchTabLiveData() {
        return switchTabLiveData;
    }

    @SuppressLint("RestrictedApi")
    public void loadData(long tagId, ItemKeyedDataSource.LoadCallback callback) {
        if (tagId <= 0 || loadAfter.get()) { // 正在分页当中
            callback.onResult(Collections.emptyList());
            return;
        }
        ArchTaskExecutor.getIOThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                ((FindTagListViewModel.DataSource) getDataSource()).loadData(tagId, callback);
            }
        });

    }

    // Key设为long,即item列表上最后一条Item的id信息,Value是数据载体
    private class DataSource extends ItemKeyedDataSource<Long, TagList> {

        /*  初始加载数据 */
        @Override
        public void loadInitial(@NonNull LoadInitialParams<Long> params, @NonNull LoadInitialCallback<TagList> callback) {
            loadData(0L, callback);
        }

        /* 加载分页数据*/
        @Override
        public void loadAfter(@NonNull LoadParams<Long> params, @NonNull LoadCallback<TagList> callback) {
            loadData(params.key, callback);
        }

        /* 向前加载数据*/
        @Override
        public void loadBefore(@NonNull LoadParams<Long> params, @NonNull LoadCallback<TagList> callback) {
            callback.onResult(Collections.emptyList());
        }

        /*获取分页符入参*/
        @NonNull
        @Override
        public Long getKey(@NonNull TagList item) {
            return item.tagId;
        }

        private void loadData(Long requestKey, LoadCallback<TagList> callback) {
            if (requestKey > 0) { // 分页
                loadAfter.set(true);
            }
            ApiResponse<List<TagList>> response = ApiService.get(URL_QUERY_TAG_LIST)
                    .addParams("userId", UserManager.get().getUserId())
                    .addParams("tagId", requestKey)
                    .addParams("tagType", mTagType)
                    .addParams("pageCount", 10)
                    .addParams("offset", offSet)
                    .responseType(new TypeReference<ArrayList<TagList>>() {
                    }.getType())
                    .execute();
            List<TagList> result = response.body == null ? Collections.emptyList() : response.body;
            // 将请求结果回调出去
            callback.onResult(result);

            if (requestKey > 0) { // 分页请求
                loadAfter.set(false); // 分页结束，标记位恢复
                // 累计标记位
                offSet += result.size();
                // 分页结果传递出去以便加载动画结束
                ((MutableLiveData) getBoundaryPageData()).postValue(result.size() > 0);
            } else { // 初始请求或下拉刷新
                offSet = result.size();
            }
        }
    }
}
