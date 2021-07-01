package com.jesen.cod.jetpackvideo.ui.detail;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;

import com.jesen.cod.jetpackvideo.R;
import com.jesen.cod.jetpackvideo.databinding.ActivityFeedDetailTypeVideoBinding;
import com.jesen.cod.jetpackvideo.databinding.LayoutFeedDetailTypeImageHeaderBinding;
import com.jesen.cod.jetpackvideo.databinding.LayoutFeedDetailTypeVideoHeaderBinding;
import com.jesen.cod.jetpackvideo.databinding.LayoutFeedDetailTypeVideoHeaderBindingImpl;
import com.jesen.cod.jetpackvideo.model.Feed;
import com.jesen.cod.jetpackvideo.ui.view.FullScreenPlayerView;

public class VideoViewHandler extends ViewHandler {

    private ActivityFeedDetailTypeVideoBinding mBinding;
    private String mCategory;
    private FullScreenPlayerView mPlayView;
    private final CoordinatorLayout coordinator;


    // 是否点击了返回键
    private boolean backPressed;

    public VideoViewHandler(FragmentActivity activity) {
        super(activity);

        mBinding = DataBindingUtil.setContentView(activity, R.layout.activity_feed_detail_type_video);
        mInteractionBinding = mBinding.bottomInteraction;
        mRecyclerView = mBinding.recyclerView;
        mPlayView = mBinding.playerViewFull;
        coordinator = mBinding.coordinator;

        View authorInfoRoot = mBinding.authorInfo.getRoot();
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) authorInfoRoot.getLayoutParams();
        params.setBehavior(new ViewAnchorBehavior(R.id.player_view_full));

        // 设置滑动效果
        CoordinatorLayout.LayoutParams playViewParams = (CoordinatorLayout.LayoutParams) mPlayView.getLayoutParams();
        ViewZoomBehavior behavior = (ViewZoomBehavior) playViewParams.getBehavior();
        behavior.setViewZoomCallback(new ViewZoomBehavior.ViewZoomCallback() {
            @Override
            public void onDragZoom(int height) {
                int bottom = mPlayView.getBottom();
                boolean moveUp = height < bottom;
                boolean fullScreen = moveUp ? height >= coordinator.getBottom()
                        - mInteractionBinding.getRoot().getHeight() : height >= coordinator.getBottom();
                setViewAppearance(fullScreen);
            }
        });
    }

    @Override
    public void bindInitData(Feed feed) {
        super.bindInitData(feed);
        mBinding.setFeed(feed);

        mCategory = mActivity.getIntent().getStringExtra(FeedDetailActivity.KEY_CATEGORY);
        mBinding.playerViewFull.bindData(mCategory, mFeed.width, mFeed.height, mFeed.cover, mFeed.url);

        mBinding.playerViewFull.post(new Runnable() {
            @Override
            public void run() {
                boolean fullScreen = mBinding.playerViewFull.getBottom() >= mBinding.coordinator.getBottom();
                setViewAppearance(fullScreen);
            }
        });

        // 添加头部布局
        LayoutFeedDetailTypeVideoHeaderBinding headerBinding
                = LayoutFeedDetailTypeVideoHeaderBinding.inflate(LayoutInflater.from(mActivity), mRecyclerView, false);
        headerBinding.setFeed(feed);
        mListAdapter.addHeaderView(headerBinding.getRoot());

    }

    private void setViewAppearance(boolean fullScreen) {
        mBinding.setFullScreen(fullScreen);
        mBinding.fullscreenAuthorInfo.getRoot().setVisibility(fullScreen ? View.VISIBLE : View.GONE);
        int interactionHeight = mInteractionBinding.getRoot().getMeasuredHeight();
        int controllerHeight = mBinding.playerViewFull.getPlayController().getMeasuredHeight();
        int controllerBottom = mBinding.playerViewFull.getPlayController().getBottom();

        // 全屏播放时调整进度条控制区高度，防止挡住底部互动栏
        mBinding.playerViewFull.getPlayController().setY(
                fullScreen ? controllerBottom - interactionHeight - controllerHeight
                        : controllerBottom - controllerHeight);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!backPressed) {
            mBinding.playerViewFull.inActive();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        backPressed = false;
        mBinding.playerViewFull.onActive();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        backPressed = true;
        // 恢复播放器位置
        mBinding.playerViewFull.getPlayController().setTranslationY(0);
    }
}
