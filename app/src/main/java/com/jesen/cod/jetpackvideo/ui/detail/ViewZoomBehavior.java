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
import com.jesen.cod.libcommon.utils.PixUtils;

import org.jetbrains.annotations.NotNull;

public class ViewZoomBehavior extends CoordinatorLayout.Behavior<FullScreenPlayerView> {

    private  OverScroller mOverScroller;
    private int minHeight;
    private int scrollingId;
    private ViewDragHelper mDragHelper;
    private View scrollingView;
    private View refChild;
    // PlayView 的原始高度
    private int childOriginalHeight;
    // 是否可以全屏播放
    private boolean canFullScreen;
    private ViewZoomCallback mViewZoomCallback;

    public ViewZoomBehavior() {

    }

    public ViewZoomBehavior(Context context, AttributeSet attributeSet) {
        TypedArray array = context.obtainStyledAttributes(attributeSet, R.styleable.view_zoom_behavior, 0, 0);
        // 获取滑动布局，获取视频布局最小高度
        minHeight = array.getResourceId(R.styleable.view_zoom_behavior_min_height, PixUtils.dp2px(200));
        scrollingId = array.getInt(R.styleable.view_zoom_behavior_scrolling_id, 0);
        array.recycle();

        mOverScroller = new OverScroller(context);
    }

    private ViewDragHelper.Callback mCallback = new ViewDragHelper.Callback() {

        /*
         * 告诉ViewDragHelper 什么时候可以拦截手指触摸的View的手势分发
         * */
        @Override
        public boolean tryCaptureView(@NonNull @NotNull View child, int pointerId) {
            if (canFullScreen && refChild.getBottom() >= minHeight) {
                return true;
            }
            return false;
        }

        /*
         * 告诉ViewDragHelper 在屏幕上滑动多少距离才算是拖拽
         * */
        @Override
        public int getViewVerticalDragRange(@NonNull @NotNull View child) {
            return mDragHelper.getTouchSlop();
        }

        /*
         * 告诉ViewDragHelper 手指拖动的view 本次滑动最终能移动多少距离
         * dy < 0,从下往上滑动
         * dy > 0,从上往下滑动
         * */
        @Override
        public int clampViewPositionVertical(@NonNull @NotNull View child, int top, int dy) {
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
            if ((dy < 0 && refChild.getBottom() < minHeight)
                    || (dy > 0 && refChild.getBottom() > childOriginalHeight)
                    || (dy > 0 && (scrollingView != null && scrollingView.canScrollVertically(-1)))) {
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

            if (mViewZoomCallback != null){
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
             && yvel != 0){
                FlingRunnable runnable = new FlingRunnable(refChild);
                runnable.fling((int)xvel, (int)yvel);
            }
        }
    };


    @Override
    public boolean onLayoutChild(@NonNull CoordinatorLayout parent, @NonNull FullScreenPlayerView child,
                                 int layoutDirection) {
        if (mDragHelper == null) {
            mDragHelper = ViewDragHelper.create(parent, 1.0f, mCallback);
        }
        this.scrollingView = parent.findViewById(scrollingId);
        this.refChild = child;
        this.childOriginalHeight = child.getMeasuredHeight();

        // 如果大于屏幕宽度，则可以全屏播放
        canFullScreen = childOriginalHeight > parent.getMeasuredWidth();

        return super.onLayoutChild(parent, child, layoutDirection);
    }

    @Override
    public boolean onTouchEvent(@NonNull @NotNull CoordinatorLayout parent, @NonNull @NotNull FullScreenPlayerView child, @NonNull @NotNull MotionEvent ev) {
        if (!canFullScreen || mDragHelper ==null){
            return super.onTouchEvent(parent, child, ev);
        }

        mDragHelper.processTouchEvent(ev);
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull @NotNull CoordinatorLayout parent, @NonNull @NotNull FullScreenPlayerView child, @NonNull @NotNull MotionEvent ev) {
        if (!canFullScreen || mDragHelper ==null){
            return super.onInterceptTouchEvent(parent, child, ev);
        }
        return mDragHelper.shouldInterceptTouchEvent(ev);
    }

    public void setViewZoomCallback(ViewZoomCallback callback){
        mViewZoomCallback = callback;
    }

    public interface ViewZoomCallback{
        void onDragZoom(int height);
    }

    private class FlingRunnable implements Runnable{

        private View mFlingView;

        public FlingRunnable(View flingView){
            mFlingView = flingView;
        }

        /*
        * 水平滑动速度，垂直滑动速度
        * */
        public void fling(int xVel, int yVel){
            mOverScroller.fling(0,
                    mFlingView.getBottom(),
                    xVel, yVel,0,Integer.MAX_VALUE,0,Integer.MAX_VALUE);
        }
        @Override
        public void run() {
            ViewGroup.LayoutParams params = mFlingView.getLayoutParams();
            int height = params.height;
            int width = params.width;
            // 本次滑动是否已结束
            if (mOverScroller.computeScrollOffset() && height>= minHeight && height<=childOriginalHeight){
                // mOverScroller.getCurrY():当前滚动的Y值
                int newHeight = Math.min(mOverScroller.getCurrY(), childOriginalHeight);
                if (newHeight != height){
                    params.height = newHeight;
                    mFlingView.setLayoutParams(params);

                    if (mViewZoomCallback != null){
                        mViewZoomCallback.onDragZoom(newHeight);
                    }
                }
                // 驱动惯性滑动
                ViewCompat.postOnAnimation(mFlingView, this);
            }else {
                mFlingView.removeCallbacks(this);
            }
        }
    }
}
