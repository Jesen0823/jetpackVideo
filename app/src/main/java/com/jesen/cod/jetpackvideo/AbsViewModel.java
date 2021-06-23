package com.jesen.cod.jetpackvideo;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.DataSource;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import org.jetbrains.annotations.NotNull;

public abstract class AbsViewModel<T> extends ViewModel {

    private DataSource dataSource;

    private LiveData<PagedList<T>> pagedListLiveData;

    private MutableLiveData<Boolean> boundaryPageData = new MutableLiveData<>();

    public AbsViewModel() {
        PagedList.Config config = new PagedList.Config.Builder()
                // 分页每页加载数量
                .setPageSize(10)
                // 分页第一次加载数量
                .setInitialLoadSizeHint(12)
                // 最大数据量，一般不用
                //.setMaxSize(100)
                // 占位相关
                //.setEnablePlaceholders(false)
                // 屏幕可见位置item剩余几条时，开始加载下一页，默认pageSize大小
                //.setPrefetchDistance()
                .build();

        pagedListLiveData = new LivePagedListBuilder(factory, config)
                .setInitialLoadKey(0)
                //.setFetchExecutor()
                // 监听PageList数据加载的状况
                .setBoundaryCallback(callback)
                .build();
    }

    public LiveData<PagedList<T>> getPagedListLiveData() {
        return pagedListLiveData;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public MutableLiveData<Boolean> getBoundaryPageData() {
        return boundaryPageData;
    }

    PagedList.BoundaryCallback<T> callback = new PagedList.BoundaryCallback<T>() {
        // 返回0条数据
        @Override
        public void onZeroItemsLoaded() {
            boundaryPageData.postValue(false); //没有数据
        }

        // 第一条数据被加载
        @Override
        public void onItemAtFrontLoaded(@NonNull @NotNull T itemAtFront) {
            boundaryPageData.postValue(true);
        }

        // pageList的最后一条数据被加载
        @Override
        public void onItemAtEndLoaded(@NonNull @NotNull T itemAtEnd) {
            super.onItemAtEndLoaded(itemAtEnd);
        }
    };

    DataSource.Factory factory = new DataSource.Factory() {
        @NonNull
        @NotNull
        @Override
        public DataSource create() {
            dataSource = createDataSource();
            return dataSource;
        }
    };

    public abstract DataSource createDataSource();
}

