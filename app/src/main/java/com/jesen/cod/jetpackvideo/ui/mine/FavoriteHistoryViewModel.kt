package com.jesen.cod.jetpackvideo.ui.mine

import androidx.lifecycle.MutableLiveData
import androidx.paging.ItemKeyedDataSource
import com.alibaba.fastjson.TypeReference
import com.jesen.cod.jetpackvideo.AbsViewModel
import com.jesen.cod.jetpackvideo.model.Feed
import com.jesen.cod.jetpackvideo.ui.login.UserManager
import com.jesen.cod.libnetwork.ApiService
import java.util.*

private const val URL_FAVORITE_HISTORY = "/feeds/queryUserBehaviorList"

class FavoriteHistoryViewModel : AbsViewModel<Feed>() {

    private var mBehavior: Int? = null

    fun setBehavior(behavior: Int) {
        mBehavior = behavior
    }

    override fun createDataSource(): DataSource {
        return DataSource()
    }

    inner class DataSource : ItemKeyedDataSource<Int, Feed>() {
        override fun getKey(item: Feed): Int {
            return item.id
        }

        override fun loadInitial(
            params: LoadInitialParams<Int>,
            callback: LoadInitialCallback<Feed>
        ) {
            params.requestedInitialKey?.let { loadData(it, callback) }
        }

        override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Feed>) {
            loadData(params.key, callback)
        }

        override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Feed>) {
            callback.onResult(Collections.emptyList())
        }

        private fun loadData(feedId: Int, callback: ItemKeyedDataSource.LoadCallback<Feed>) {
            val response = ApiService.get<List<Feed>>(URL_FAVORITE_HISTORY)
                .addParams("behavior", mBehavior)
                .addParams("feedId", feedId)
                .addParams("pageCount", 10)
                .addParams("userId", UserManager.get().userId)
                .responseType(object : TypeReference<ArrayList<Feed?>?>() {}.type)
                .execute()
            val result: List<Feed> =
                if (response.body == null) emptyList() else response.body as List<Feed>
            callback.onResult(result)

            if (feedId > 0) {
                (boundaryPageData as MutableLiveData).postValue(result?.size > 0)
            }
        }
    }
}