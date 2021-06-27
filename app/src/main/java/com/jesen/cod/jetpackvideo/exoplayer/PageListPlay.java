package com.jesen.cod.jetpackvideo.exoplayer;

import android.app.Application;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.Cache;
import com.google.android.exoplayer2.upstream.cache.CacheDataSinkFactory;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;
import com.jesen.cod.jetpackvideo.R;
import com.jesen.cod.libcommon.JetAppGlobal;

public class PageListPlay {

    public SimpleExoPlayer exoPlayer;
    public PlayerView mPlayerView;
    public PlayerControlView mControllerView;
    public String playUrl;

    public PageListPlay() {
        Application application = JetAppGlobal.getApplication();
        exoPlayer = ExoPlayerFactory.newSimpleInstance(application,
                //视频每一帧的画面如何渲染,实现默认的实现类
                new DefaultRenderersFactory(application),
                //视频的音视频轨道如何加载,使用默认的轨道选择器
                new DefaultTrackSelector(),
                //视频缓存控制逻辑,使用默认的即可
                new DefaultLoadControl());
        mPlayerView = (PlayerView) LayoutInflater.from(application).inflate(
                R.layout.layout_exo_player_view, null, false);

        mControllerView = (PlayerControlView) LayoutInflater.from(application)
                .inflate(R.layout.layout_exo_player_controller_view, null, false);

        //把播放器实例 和 playerView，controlView相关联
        //如此视频画面才能正常显示,播放进度条才能自动更新
        mPlayerView.setPlayer(exoPlayer);
        mControllerView.setPlayer(exoPlayer);
    }

    public void release() {
        if (exoPlayer != null){
            exoPlayer.setPlayWhenReady(false);
            exoPlayer.stop(true);
            exoPlayer.release();
            exoPlayer = null;
        }
        if (mPlayerView != null){
            mPlayerView.setPlayer(null);
            mPlayerView = null;
        }
        if (mControllerView != null){
            mControllerView.setPlayer(null);
            mControllerView.setVisibilityListener(null);
            mControllerView = null;
        }
    }

    /**
     * 切换与播放器exoplayer 绑定的exoplayerView。用于页面切换视频无缝续播的场景
     *
     * @param newPlayerView
     * @param attach
     */
    public void switchPlayerView(PlayerView newPlayerView, boolean attach) {
        mPlayerView.setPlayer(attach ? null : exoPlayer);
        newPlayerView.setPlayer(attach ? exoPlayer : null);
    }
}
