package com.jesen.cod.jetpackvideo.ui.detail;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.jesen.cod.jetpackvideo.R;
import com.jesen.cod.libcommon.utils.PixUtils;

import org.jetbrains.annotations.NotNull;

/*
 *  管理各组件之间的相对位置
 * */
public class ViewAnchorBehavior extends CoordinatorLayout.Behavior<View> {

    private int extraUsed;
    private int anchorId;

    public ViewAnchorBehavior() {

    }

    public ViewAnchorBehavior(Context context, AttributeSet attributeSet) {
        TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.view_anchor_behavior, 0, 0);
        anchorId = typedArray.getResourceId(R.styleable.view_anchor_behavior_anchorId, 0);
        typedArray.recycle();

        extraUsed = PixUtils.dp2px(48); // 底部互动区高度
    }

    public ViewAnchorBehavior(int anchorId) {
        this.anchorId = anchorId;
        extraUsed = PixUtils.dp2px(48);
    }

    @Override
    public boolean layoutDependsOn(@NotNull CoordinatorLayout parent, @NotNull View child, @NotNull View dependency) {
        return anchorId == dependency.getId();
    }

    /*
     * CoordinatorLayout 测量每一个子View时会调用该方法，
     * @return 如果返回 true，则不会再次测量childView,使用我们传入的测量值放置view的位置
     *
     * */
    @Override
    public boolean onMeasureChild(@NotNull CoordinatorLayout parent,
                                  @NotNull View child, int parentWidthMeasureSpec,
                                  int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
        View anchorView = parent.findViewById(anchorId);
        if (anchorView == null) {
            return false;
        }
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
        int topMargin = layoutParams.topMargin;
        int bottom = anchorView.getBottom();
        // 已经使用了的高度
        heightUsed = bottom + topMargin + extraUsed;
        // 水平方向的不关心
        parent.onMeasureChild(child, parentWidthMeasureSpec, 0, parentHeightMeasureSpec, heightUsed);
        return true;
    }


    /*
     * CoordinatorLayout 在layout每一个子view的时候回调该方法
     * 如果return true, 不会再次layout该child view
     *
     * */
    @Override
    public boolean onLayoutChild(@NotNull CoordinatorLayout parent, @NotNull View child, int layoutDirection) {
        View anchorView = parent.findViewById(anchorId);
        if (anchorView == null) {
            return false;
        }
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
        int topMargin = params.topMargin;
        int bottom = anchorView.getBottom();
        parent.onLayoutChild(child, layoutDirection);
        // 设置垂直方向偏移量
        child.offsetTopAndBottom(bottom + topMargin);

        return true;
    }
}
