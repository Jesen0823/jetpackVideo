package com.jesen.cod.jetpackvideo.ui.detail;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.OverScroller;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.customview.widget.ViewDragHelper;

import com.jesen.cod.jetpackvideo.R;
import com.jesen.cod.jetpackvideo.ui.view.FullScreenPlayerView;
import com.jesen.cod.libcommon.utils.Og;
import com.jesen.cod.libcommon.utils.PixUtils;

import org.jetbrains.annotations.NotNull;

public class ViewZoomBehavior extends CoordinatorLayout.Behavior<FullScreenPlayerView> {

    private static final String TAG = "ViewZoomBehavior";
    private OverScroller mOverScroller;
    private int minHeight;
    private int scrollingId;
    private ViewDragHelper mViewDragHelper;
    private View scrollingView;
    private FullScreenPlayerView refChild;
    // PlayView 的原始高度
    private int childOriginalHeight;
    // 是否可以全屏播放
    private boolean canFullScreen;
    private ViewZoomCallback mViewZoomCallback;
    private FlingRunnable runnable;

    public ViewZoomBehavior() {

    }

    public ViewZoomBehavior(Context context, AttributeSet attributeSet) {
        TypedArray array = context.obtainStyledAttributes(attributeSet, R.styleable.view_zoom_behavior, 0, 0);
        // 获取滑动布局，获取视频布局最小高度
        minHeight = array.getDimensionPixelOffset(R.styleable.view_zoom_behavior_min_height, PixUtils.dp2px(200));
        scrollingId = array.getResourceId(R.styleable.view_zoom_behavior_scrolling_id, 0);
        array.recycle();

        mOverScroller = new OverScroller(context);
    }

    private ViewDragHelper.Callback mCallback = new ViewDragHelper.Callback() {

        /*
         * 告诉ViewDragHelper 什么时候可以拦截手指触摸的View的手势分发
         * */
        @Override
        public boolean tryCaptureView(@NonNull @NotNull View child, int pointerId) {
            if (!canFullScreen) return false;
            if (runnable != null) {
                refChild.removeCallbacks(runnable);
            }

            int refChildBottom = refChild.getBottom();
            if (child == refChild) {
                return refChildBottom >= minHeight && refChildBottom <= childOriginalHeight;
            }

            if (child == scrollingView) {
                boolean isScrolling = refChildBottom != minHeight && refChildBottom != childOriginalHeight;
                Og.d(TAG + ", tryCaptureView, isScrolling: " + isScrolling);
                return isScrolling;
            }
            return false;
        }

        /*
         * 告诉ViewDragHelper 在屏幕上滑动多少距离才算是拖拽
         * */
        @Override
        public int getViewVerticalDragRange(@NonNull View child) {
            return mViewDragHelper.getTouchSlop();
        }

        /*
         * 告诉ViewDragHelper 手指拖动的view 本次滑动最终能移动多少距离
         * dy < 0,从下往上滑动
         * dy > 0,从上往下滑动
         * */
        @Override
        public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
            if (refChild == null || dy == 0) {
                return 0;
            }

            /*
             * 滑上去的情况,refChild的bottom最小滑到minHeight
             * 滑下来的情况：
             *  1. refChild的bottom最大到达全屏,也就是父容器高度
             *  2. scrollView 还未滑出它的顶部，还可以继续往下滑动，让它自行滑动，不做拦截
             *
             *  另，为了适配滑动过程播放布局高度变小的时候保持宽高比，播放器FullScreenPlayerView重载
             *  setLayoutParams()方法，进行等比缩放
             * */
            if ((dy < 0 && refChild.getBottom() <= minHeight)
                    || (dy > 0 && refChild.getBottom() >= childOriginalHeight)
                    || (dy > 0 && (scrollingView != null && scrollingView.canScrollVertically(-1)))) {

                Og.d(TAG + ", clampViewPositionVertical, canScrollVertically :"
                        + scrollingView.canScrollVertically(-1));
                // 这些情况，都不做拦截
                return 0;
            }

            // 可滑动最大距离
            int maxConsumed = 0;
            if (dy > 0) {
                if (refChild.getBottom() + dy > childOriginalHeight) {
                    maxConsumed = childOriginalHeight - refChild.getBottom();
                } else {
                    maxConsumed = dy;
                }
            } else {
                if (refChild.getBottom() + dy < minHeight) {
                    maxConsumed = minHeight - refChild.getBottom();
                } else {
                    maxConsumed = dy;
                }
            }

            ViewGroup.LayoutParams layoutParams = refChild.getLayoutParams();
            layoutParams.height = layoutParams.height + maxConsumed;
            refChild.setLayoutParams(layoutParams);
            Og.d(TAG + ", clampViewPositionVertical, height: " + layoutParams.height);

            if (mViewZoomCallback != null) {
                mViewZoomCallback.onDragZoom(layoutParams.height);
            }

            return maxConsumed;
        }

        /*
         * 手指离开屏幕时被调用
         * */
        @Override
        public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
            super.onViewReleased(releasedChild, xvel, yvel);
            // 惯性滑动效果
            if (refChild.getBottom() > minHeight && refChild.getBottom() < childOriginalHeight
                    && yvel != 0) {
                runnable = new FlingRunnable(refChild);
                refChild.removeCallbacks(runnable);
                runnable.fling((int) xvel, (int) yvel);
                Og.d(TAG + ", onViewReleased.");
            }
        }
    };

    /*
     * 获取 scrollingView,并全局保存下child view.
     * 计算出初始时 child的底部值，也就是它的高度。后续拖拽滑动的时候，它就是最大高度的限制
     * 与此同时 还需要计算出，当前页面是否可以进行视频的全屏展示，即h>w即可。
     * */
    @Override
    public boolean onLayoutChild(@NonNull CoordinatorLayout parent, @NonNull FullScreenPlayerView child,
                                 int layoutDirection) {
        if (mViewDragHelper == null) {
            mViewDragHelper = ViewDragHelper.create(parent, 1.0f, mCallback);
            this.scrollingView = parent.findViewById(scrollingId);
            this.refChild = child;
            this.childOriginalHeight = child.getMeasuredHeight();

            // 如果大于屏幕宽度，则可以全屏播放
            canFullScreen = childOriginalHeight > parent.getMeasuredWidth();
        }

        return super.onLayoutChild(parent, child, layoutDirection);
    }

    @Override
    public boolean onTouchEvent(@NonNull @NotNull CoordinatorLayout parent, @NonNull @NotNull FullScreenPlayerView child, @NonNull @NotNull MotionEvent ev) {
        if (!canFullScreen || mViewDragHelper == null) {
            return super.onTouchEvent(parent, child, ev);
        }

        mViewDragHelper.processTouchEvent(ev);
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull @NotNull CoordinatorLayout parent, @NonNull @NotNull FullScreenPlayerView child, @NonNull @NotNull MotionEvent ev) {
        if (!canFullScreen || mViewDragHelper == null) {
            return super.onInterceptTouchEvent(parent, child, ev);
        }
        return mViewDragHelper.shouldInterceptTouchEvent(ev);
    }

    public void setViewZoomCallback(ViewZoomCallback callback) {
        mViewZoomCallback = callback;
    }

    public interface ViewZoomCallback {
        void onDragZoom(int height);
    }

    private class FlingRunnable implements Runnable {

        private View mFlingView;

        public FlingRunnable(View flingView) {
            mFlingView = flingView;
        }

        /**
         * startX:开始的X值，由于我们不需要再水平方向滑动 所以为0
         * startY:开始滑动时Y的起始值，那就是flingview的bottom值
         * xvel:水平方向上的速度，实际上为0的
         * yvel:垂直方向上的速度。即松手时的速度
         * minX:水平方向上 滚动回弹的越界最小值，给0即可
         * maxX:水平方向上 滚动回弹越界的最大值，实际上给0也是一样的
         * minY：垂直方向上 滚动回弹的越界最小值，给0即可
         * maxY:垂直方向上，滚动回弹越界的最大值，实际上给0 也一样
         */
        public void fling(int xVel, int yVel) {
            mOverScroller.fling(0,
                    mFlingView.getBottom(),
                    xVel, yVel, 0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE);
            run();
        }

        @Override
        public void run() {
            ViewGroup.LayoutParams params = mFlingView.getLayoutParams();
            int height = params.height;

            // 本次滑动是否已结束
            if (mOverScroller.computeScrollOffset() && height >= minHeight && height <= childOriginalHeight) {
                // mOverScroller.getCurrY():当前滚动的Y值
                int newHeight = Math.min(mOverScroller.getCurrY(), childOriginalHeight);
                Og.d(TAG + ", run, newHeight:" + newHeight + ",height: " + height);
                if (newHeight != height) {
                    params.height = newHeight;
                    mFlingView.setLayoutParams(params);

                    if (mViewZoomCallback != null) {
                        mViewZoomCallback.onDragZoom(newHeight);
                    }
                }
                // 驱动惯性滑动
                ViewCompat.postOnAnimation(mFlingView, this);
            } else {
                mFlingView.removeCallbacks(this);
            }
        }
    }
}
