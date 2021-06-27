package com.jesen.cod.jetpackvideo.exoplayer;

import android.view.ViewGroup;

public interface IPlayTarget {
    // 获取容器
    ViewGroup getOwner();

    // 滚进屏幕，满足条件时调用该方法启动播放
    void onActive();

    // 滚出屏幕，可以调用停止播放
    void inActive();

    boolean isPlaying();
}
