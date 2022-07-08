package com.jesen.cod.jetpackvideo.ui.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.jesen.cod.jetpackvideo.R;
import com.jesen.cod.jetpackvideo.exoplayer.PageListPlay;
import com.jesen.cod.jetpackvideo.exoplayer.PageListPlayManager;
import com.jesen.cod.libcommon.utils.Og;
import com.jesen.cod.libcommon.utils.PixUtils;

import org.jetbrains.annotations.NotNull;

public class SnapScreenPlayerView extends ListPlayerView {

    private static final String TAG = "SnapScreenPlayerView";

    private final PlayerView mExoPlayerView;

    public SnapScreenPlayerView(@NonNull @NotNull Context context) {
        this(context, null);
    }

    public SnapScreenPlayerView(@NonNull @NotNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SnapScreenPlayerView(@NonNull @NotNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SnapScreenPlayerView(@NonNull @NotNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        mExoPlayerView = (PlayerView) LayoutInflater.from(context).inflate(R.layout.layout_exo_player_view, null, false);

    }

    @Override
    protected void setSize(int widthPx, int heightPx) {
        if (widthPx >= heightPx) {
            super.setSize(widthPx, heightPx);
            return;
        }

        int maxWidth = PixUtils.getScreenWidth();
        int maxHeight = PixUtils.getScreenHeight();
        ViewGroup.LayoutParams params = getLayoutParams();
        params.width = maxWidth;
        params.height = maxHeight;

        setLayoutParams(params);

        FrameLayout.LayoutParams coverLayoutParams = (LayoutParams) cover.getLayoutParams();
        // 等比缩放,使封面图可以全屏展示
        coverLayoutParams.width = (int) (widthPx / (heightPx * 1.0f / maxHeight));
        coverLayoutParams.height = maxHeight;
        coverLayoutParams.gravity = Gravity.CENTER;
        cover.setLayoutParams(coverLayoutParams);
    }

    @Override
    public void onActive() {
        Og.d(TAG+", onActive , mCategory:"+mCategory);

        PageListPlay pageListPlay = PageListPlayManager.get(mCategory);
        PlayerView playerView = mExoPlayerView; //pageListPlay.mPlayerView;
        PlayerControlView controlView = pageListPlay.mControllerView;
        SimpleExoPlayer exoPlayer = pageListPlay.exoPlayer;
        if (playerView == null) {
            return;
        }
        if (playerView == null){
            Og.d(TAG+", onActive , playerView is null");

        }
        //主动关联播放器与exoplayerview
        // pageListPlay.switchPlayerView(playerView);
        pageListPlay.switchPlayerView(playerView,true);
        ViewParent parent = playerView.getParent();
        if (parent != this) {

            if (parent != null) {
                ((ViewGroup) parent).removeView(playerView);
            }

            ViewGroup.LayoutParams coverParams = cover.getLayoutParams();
            this.addView(playerView, 1, coverParams);
        }

        ViewParent ctrlParent = controlView.getParent();
        if (ctrlParent != this) {
            if (ctrlParent != null) {
                ((ViewGroup) ctrlParent).removeView(controlView);
            }
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.BOTTOM;
            this.addView(controlView, params);
        }

        //如果是同一个视频资源,则不需要从重新创建mediaSource。
        //但需要onPlayerStateChanged 否则不会触发onPlayerStateChanged()
        Og.d("SnapScreenPlayerView, pageListPlay.playUrl: "+ pageListPlay.playUrl);
        Og.d("SnapScreenPlayerView, mVideoUrl: "+ mVideoUrl);

        if (TextUtils.equals(pageListPlay.playUrl, mVideoUrl)) {
            onPlayerStateChanged(true, Player.STATE_READY);
        } else {
            MediaSource mediaSource = PageListPlayManager.createMediaSource(mVideoUrl);
            exoPlayer.prepare(mediaSource);
            exoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);
            pageListPlay.playUrl = mVideoUrl;
        }
        controlView.show();
        controlView.setVisibilityListener(this);
        exoPlayer.addListener(this);
        exoPlayer.setPlayWhenReady(true);
    }


    @Override
    public void inActive() {
        super.inActive();
        PageListPlay pageListPlay = PageListPlayManager.get(mCategory);
        //主动切断exoplayer与视频播放器的联系
        pageListPlay.switchPlayerView(mExoPlayerView, false);
    }

    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        if (mHeightPx > mWidthPx) {
            int layoutWidth = params.width;
            int layoutHeight = params.height;
            ViewGroup.LayoutParams coverLayoutParams = cover.getLayoutParams();
            // 播放器布局保持等比缩放，以适配滑动过程中视图高度缩小时，仍保持宽高比
            coverLayoutParams.width = (int) (mWidthPx / (mHeightPx * 1.0f / layoutHeight));
            coverLayoutParams.height = layoutHeight;
            cover.setLayoutParams(coverLayoutParams);

            if (mExoPlayerView != null) {
                ViewGroup.LayoutParams playViewParams = mExoPlayerView.getLayoutParams();
                if (playViewParams != null && playViewParams.width>0 && playViewParams.height>0) {
                    float scaleX = coverLayoutParams.width * 1.0f / playViewParams.width;
                    float scaleY = coverLayoutParams.height * 1.0f / playViewParams.height;

                    mExoPlayerView.setScaleX(scaleX);
                    mExoPlayerView.setScaleY(scaleY);
                }
            }
        }
        super.setLayoutParams(params);
    }

}
