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
                new DefaultRenderersFactory(application),
                new DefaultTrackSelector(),
                new DefaultLoadControl());
        mPlayerView = (PlayerView) LayoutInflater.from(application).inflate(
                R.layout.layout_exo_player_view, null, false);

        mControllerView = (PlayerControlView) LayoutInflater.from(application)
                .inflate(R.layout.layout_exo_player_controller_view, null, false);
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
