package com.jesen.cod.jetpackvideo.exoplayer;

import android.graphics.Point;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.jesen.cod.jetpackvideo.utils.Og;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PageListPlayDetector {

    private List<IPlayTarget> mTargets = new ArrayList<>();

    private RecyclerView mRecyclerView;
    private IPlayTarget mPlayingTarget;

    public void addTarget(IPlayTarget target) {
        mTargets.add(target);
    }

    public void removeTarget(IPlayTarget target) {
        mTargets.remove(target);
    }

    public PageListPlayDetector(LifecycleOwner owner, RecyclerView recyclerView) {
        mRecyclerView = recyclerView;

        owner.getLifecycle().addObserver(new LifecycleEventObserver() {
            @Override
            public void onStateChanged(@NonNull @NotNull LifecycleOwner source, @NonNull @NotNull Lifecycle.Event event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    mPlayingTarget = null;
                    mTargets.clear();
                    mRecyclerView.removeCallbacks(delayAutoPlay);
                    recyclerView.removeOnScrollListener(scrollListener);
                    owner.getLifecycle().removeObserver(this);
                    recyclerView.getAdapter().unregisterAdapterDataObserver(mDataObserver);
                    owner.getLifecycle().removeObserver(this);
                }
            }
        });

        recyclerView.getAdapter().registerAdapterDataObserver(mDataObserver);
        recyclerView.addOnScrollListener(scrollListener);
    }

    RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(@NonNull @NotNull RecyclerView recyclerView, int newState) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                autoPlay();
            }
        }

        @Override
        public void onScrolled(@NonNull @NotNull RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            Og.d("PageListPlayDetector, recyclerView, onScrolled dx,dy : " + dx + "," + dy);
            if (dx == 0 && dy == 0){
                /*当执行了AdapterDataObserver#onItemRangeInserted  可能还没有被布局到RecyclerView上。
                所以此时 recyclerView.getChildCount()还是等于0的。
                等childView 被布局到RecyclerView上之后，会执行onScrolled（）方法
                并且此时 dx,dy都等于0*/
                postAutoPlay();
            }else {
                // 如果有正在播放，且滑出屏幕，暂停
                if (mPlayingTarget != null && mPlayingTarget.isPlaying() && !isTargetInBounds(mPlayingTarget)) {
                    mPlayingTarget.inActive();
                }
            }
        }
    };

    private void postAutoPlay() {
        mRecyclerView.post(delayAutoPlay);
    }

    Runnable delayAutoPlay = new Runnable(){

        @Override
        public void run() {
            autoPlay();
        }
    };

    private RecyclerView.AdapterDataObserver mDataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            postAutoPlay();
        }
    };

    private void autoPlay() {
        if (mTargets.size() <= 0 || mRecyclerView.getChildCount() <= 0) {
            Og.i("PageListPlayDetector, autoPlay, mTargets.size()=" + mTargets.size());
            Og.i("PageListPlayDetector, autoPlay, mRecyclerView.getChildCount()=" + mRecyclerView.getChildCount());
            return;
        }

        // 上一个target正在播放且还处于屏幕内可见
        if (mPlayingTarget != null && mPlayingTarget.isPlaying() && isTargetInBounds(mPlayingTarget)) {
            return;
        }
        IPlayTarget activeTarget = null;

        for (IPlayTarget target : mTargets) {
            boolean inBounds = isTargetInBounds(target);
            if (inBounds) {
                activeTarget = target;
                break;
            }
        }

        if (activeTarget != null) {
            // 把上一个target关闭，更新target
            if (mPlayingTarget != null) {
                mPlayingTarget.inActive();
            }
            mPlayingTarget = activeTarget;
            activeTarget.onActive();
        }
    }

    /*
    *  检测 IPlayTarget 所在的 viewGroup 是否至少还有一半的大小在屏幕内
    * */
    private boolean isTargetInBounds(IPlayTarget target) {
        ViewGroup owner = target.getOwner();
        ensureRecyclerViewLocation();
        if (!owner.isShown() || !owner.isAttachedToWindow()) {
            return false;
        }
        int[] location = new int[2];
        owner.getLocationOnScreen(location);
        int center = location[1] + owner.getHeight() / 2;

        //承载视频播放画面的ViewGroup它需要至少一半的大小 在RecyclerView上下范围内
        return center >= recyclerLocation.x && center <= recyclerLocation.y;
    }

    private Point recyclerLocation = null;

    private Point ensureRecyclerViewLocation() {
        if (recyclerLocation == null) {
            int[] location = new int[2];
            mRecyclerView.getLocationOnScreen(location);
            int top = location[1];
            int bottom = top + mRecyclerView.getHeight();

            recyclerLocation = new Point(top, bottom);

        }
        return recyclerLocation;
    }

    public void onPause() {
        if (mPlayingTarget != null) {
            mPlayingTarget.inActive();
        }
    }

    public void onResume() {
        if (mPlayingTarget != null) {
            mPlayingTarget.onActive();
        }
    }
}
