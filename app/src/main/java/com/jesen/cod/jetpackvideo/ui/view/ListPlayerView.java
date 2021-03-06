package com.jesen.cod.jetpackvideo.ui.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.adapters.ViewBindingAdapter;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.jesen.cod.jetpackvideo.R;
import com.jesen.cod.jetpackvideo.exoplayer.IPlayTarget;
import com.jesen.cod.jetpackvideo.exoplayer.PageListPlay;
import com.jesen.cod.jetpackvideo.exoplayer.PageListPlayManager;
import com.jesen.cod.libcommon.utils.Og;
import com.jesen.cod.libcommon.utils.PixUtils;

import org.jetbrains.annotations.NotNull;

public class ListPlayerView extends FrameLayout implements IPlayTarget, PlayerControlView.VisibilityListener, Player.EventListener {

    private static final String TAG = "ListPlayerView";
    public View bufferView;
    public ViImageView cover, blur;
    private final ImageView playBtn;
    protected String mCategory;
    protected String mVideoUrl;
    protected boolean isPlaying;
    protected int mWidthPx;
    protected int mHeightPx;

    public ListPlayerView(@NonNull Context context) {
        this(context, null);
    }

    public ListPlayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ListPlayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ListPlayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        LayoutInflater.from(context).inflate(R.layout.layout_player_view, this, true);

        //??????????????????view
        bufferView = findViewById(R.id.buffer_view);
        // ?????????
        cover = findViewById(R.id.cover);
        // ?????????????????????
        blur = findViewById(R.id.blur_background);
        playBtn = findViewById(R.id.play_btn);

        playBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Og.d(TAG + ", clicked playBtn, isPlaying:" + isPlaying());
                if (isPlaying()) {
                    inActive();
                } else {
                    onActive();
                }
            }
        });
        this.setTransitionName("listPlayerView");
    }

    public void bindData(String category, int widthPx, int heightPx, String coverUrl, String videoUrl) {
        mCategory = category;
        mVideoUrl = videoUrl;
        mWidthPx = widthPx;
        mHeightPx = heightPx;
        cover.setImageUrl(cover, coverUrl, false);

        if (widthPx < heightPx) {
            blur.setBlurImageUrl(blur, coverUrl, 10);
            blur.setVisibility(VISIBLE);
        } else {
            blur.setVisibility(INVISIBLE);
        }
        setSize(widthPx, heightPx);
    }

    protected void setSize(int widthPx, int heightPx) {
        int maxWidth = PixUtils.getScreenWidth();
        int layoutWidth = maxWidth;
        int layoutHeight = 0;

        int coverWidth;
        int coverHeight;
        if (widthPx > heightPx) {
            coverWidth = maxWidth;
            layoutHeight = coverHeight = (int) (heightPx / (widthPx * 1.f / maxWidth));
        } else {
            layoutHeight = coverHeight = maxWidth;
            coverWidth = (int) (widthPx / (heightPx * 1f / maxWidth));
        }

        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.width = layoutWidth;
        layoutParams.height = layoutHeight;
        setLayoutParams(layoutParams);

        ViewGroup.LayoutParams blurParams = blur.getLayoutParams();
        blurParams.width = layoutWidth;
        blurParams.height = layoutHeight;
        blur.setLayoutParams(blurParams);

        FrameLayout.LayoutParams coverParams = (LayoutParams) cover.getLayoutParams();
        coverParams.width = coverWidth;
        coverParams.height = coverHeight;
        coverParams.gravity = Gravity.CENTER;
        cover.setLayoutParams(coverParams);

        FrameLayout.LayoutParams playBtnParams = (LayoutParams) playBtn.getLayoutParams();
        playBtnParams.gravity = Gravity.CENTER;
        playBtn.setLayoutParams(playBtnParams);
    }


    @Override
    public void onVisibilityChange(int visibility) {
        Og.d(TAG + ", onVisibilityChange, visibility:" + visibility);
        playBtn.setVisibility(visibility);
        playBtn.setImageResource(isPlaying() ? R.drawable.icon_video_pause : R.drawable.icon_video_play);
    }

    @Override
    public ViewGroup getOwner() {
        return this;
    }

    @Override
    public void onActive() {
        Og.d(TAG + ", onActive");
        //????????????,???????????????

        //?????????View???????????????mCategory(??????????????????tab_all,??????tab???tab_video,?????????????????????tag_feed) ?????????
        //????????????????????????Exoplayer????????????ExoplayerView??????View,???????????????PageListPlay
        PageListPlay pageListPlay = PageListPlayManager.get(mCategory);
        PlayerView playerView = pageListPlay.mPlayerView;
        PlayerControlView controlView = pageListPlay.mControllerView;
        SimpleExoPlayer exoPlayer = pageListPlay.exoPlayer;
        if (playerView == null) {
            Og.d(TAG+", onActive , playerView is null, return");
            return;
        }

        // ?????????????????? switchPlayerView???????????????Exoplayer????????????????????????View ExoplayerView?????????
        // ??????????????????????????????Item???????????????????????????????????????????????????????????????????????????Exoplayer???
        // ??????????????????????????????????????????View ExoplayerView?????????????????????????????????????????????
        //?????? ????????????????????????????????????????????????????????????ExoplayerView?????????
        pageListPlay.switchPlayerView(playerView, true);
        //pageListPlay.switchPlayerView(playerView);
        ViewParent parent = playerView.getParent();
        if (parent != this) {

            //????????????????????????View?????????ItemView????????????
            if (parent != null) {
                ((ViewGroup) parent).removeView(playerView);
                //????????????????????????????????????????????????
                ((ListPlayerView) parent).inActive();
            }

            ViewGroup.LayoutParams coverParams = cover.getLayoutParams();
            // index = 1??????????????????????????????????????????
            this.addView(playerView, 1, coverParams);
        }

        ViewParent ctrlParent = controlView.getParent();
        if (ctrlParent != this) {
            //?????????????????? ?????????ItemView????????????
            if (ctrlParent != null) {
                ((ViewGroup) ctrlParent).removeView(controlView);
            }
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.BOTTOM;
            this.addView(controlView, params);
        }

        //??????????????????????????????,???????????????????????????mediaSource???
        //?????????onPlayerStateChanged ??????????????????onPlayerStateChanged()
        if (TextUtils.equals(pageListPlay.playUrl, mVideoUrl)) {
            Og.d("ListPlayerView, is same video url.");
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
        // ??????????????????????????????
        exoPlayer.setPlayWhenReady(true);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Og.d(TAG + ", onAttachedToWindow.");
        isPlaying = false;
        bufferView.setVisibility(GONE);
        cover.setVisibility(VISIBLE);
        playBtn.setVisibility(VISIBLE);
        playBtn.setImageResource(R.drawable.icon_video_play);
    }

    @Override
    public void inActive() {
        Og.d(TAG + ", inActive");

        //??????????????????????????????????????? ?????????????????? ????????????
        PageListPlay pageListPlay = PageListPlayManager.get(mCategory);
        if (pageListPlay.exoPlayer == null || pageListPlay.mControllerView == null) {
            Og.d(TAG+", inActive return");
            return;
        }
        // ????????????
        pageListPlay.exoPlayer.setPlayWhenReady(false);
        pageListPlay.mControllerView.setVisibilityListener(null);
        pageListPlay.exoPlayer.removeListener(this);
        cover.setVisibility(VISIBLE);
        Og.d(TAG + ", inActive, playBtn set Visible");
        playBtn.setVisibility(VISIBLE);
        playBtn.setImageResource(R.drawable.icon_video_play);
    }

    @Override
    public boolean isPlaying() {
        Og.d("ListPlayView, isPlaying: " + isPlaying);
        return isPlaying;
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        //???????????????????????????
        PageListPlay pageListPlay = PageListPlayManager.get(mCategory);
        SimpleExoPlayer exoPlayer = pageListPlay.exoPlayer;
        if (playbackState == Player.STATE_READY && exoPlayer.getBufferedPosition() != 0 && playWhenReady) {
            cover.setVisibility(GONE);
            bufferView.setVisibility(GONE);
        } else if (playbackState == Player.STATE_BUFFERING) {
            bufferView.setVisibility(VISIBLE);
        }
        isPlaying = playbackState == Player.STATE_READY && exoPlayer.getBufferedPosition() != 0 && playWhenReady;
        Og.d(TAG + ", onPlayerStateChanged, playbackState is :" + playbackState + ", isPlaying:" + isPlaying);
        playBtn.setImageResource(isPlaying ? R.drawable.icon_video_pause : R.drawable.icon_video_play);
    }

    public View getPlayController() {
        PageListPlay listPlay = PageListPlayManager.get(mCategory);
        return listPlay.mControllerView;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //?????????????????? ?????????????????????????????????????????????
        PageListPlay pageListPlay = PageListPlayManager.get(mCategory);
        if (pageListPlay.mControllerView != null) {
            pageListPlay.mControllerView.show();
        }
        return true;
    }

}
