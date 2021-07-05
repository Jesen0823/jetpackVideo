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

        mListAdapter.setFeed(feed);

        mCategory = mActivity.getIntent().getStringExtra(FeedDetailActivity.KEY_CATEGORY);
        mPlayView.bindData(mCategory, mFeed.width, mFeed.height, mFeed.cover, mFeed.url);

        mPlayView.post(new Runnable() {
            @Override
            public void run() {
                boolean fullScreen = mPlayView.getBottom() >= coordinator.getBottom();
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
        mInteractionBinding.setFullScreen(fullScreen);
        mBinding.fullscreenAuthorInfo.getRoot().setVisibility(fullScreen ? View.VISIBLE : View.GONE);
        //底部互动区域的高度
        int interactionHeight = mInteractionBinding.getRoot().getMeasuredHeight();
        //播放控制器的高度
        int controllerHeight = mPlayView.getPlayController().getMeasuredHeight();
        //播放控制器的bottom值
        int controllerBottom = mPlayView.getPlayController().getBottom();

        // 全屏播放时调整进度条控制区高度，防止挡住底部互动栏
        mPlayView.getPlayController().setY(
                fullScreen ? controllerBottom - interactionHeight - controllerHeight
                        : controllerBottom - controllerHeight);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!backPressed) {
            mPlayView.inActive();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        backPressed = false;
        mPlayView.onActive();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        backPressed = true;
        // 恢复播放器位置
        mPlayView.getPlayController().setTranslationY(0);
    }
}
