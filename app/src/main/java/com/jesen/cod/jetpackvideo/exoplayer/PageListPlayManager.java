package com.jesen.cod.jetpackvideo.exoplayer;

import android.app.Application;
import android.net.Uri;

import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheDataSinkFactory;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;
import com.jesen.cod.libcommon.JetAppGlobal;

import java.util.HashMap;

public class PageListPlayManager {
    private static HashMap<String, PageListPlay> sPageListPlayHashMap = new HashMap<>();

    private static final ProgressiveMediaSource.Factory mediaSourceFactory;

    static {
        Application application = JetAppGlobal.getApplication();
        // 一个可以下载视频的工厂类
        DefaultHttpDataSourceFactory dataSourceFactory = new DefaultHttpDataSourceFactory(
                Util.getUserAgent(application,application.getPackageName()));
        Cache cache = new SimpleCache(application.getCacheDir(),
                new LeastRecentlyUsedCacheEvictor(1024*1024*200));

        // 缓存的写入
        CacheDataSinkFactory cacheDataSinkFactory = new CacheDataSinkFactory(cache, Long.MAX_VALUE);
        // 读取缓存文件,如果没有缓存文件根据url区下载文件
        CacheDataSourceFactory cacheDataSourceFactory
                = new CacheDataSourceFactory(cache, dataSourceFactory, new FileDataSourceFactory(),
                cacheDataSinkFactory, CacheDataSource.FLAG_BLOCK_ON_CACHE, null);
         mediaSourceFactory = new ProgressiveMediaSource.Factory(cacheDataSourceFactory);
    }

    /*
    *  创建MediaSource数据源
    * */
    public static MediaSource createMediaSource(String url){
        return mediaSourceFactory.createMediaSource(Uri.parse(url));
    }

    public static PageListPlay get(String pageName){
        PageListPlay pageListPlay = sPageListPlayHashMap.get(pageName);
        if (pageListPlay == null){
            pageListPlay = new PageListPlay();
            sPageListPlayHashMap.put(pageName, pageListPlay);
        }
        return pageListPlay;
    }

    public static void removePageListPlay(String pageName){
        PageListPlay pageListPlay = sPageListPlayHashMap.get(pageName);
        if (pageListPlay != null){
            pageListPlay.release();
        }
    }
}
