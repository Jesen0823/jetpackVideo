package com.jesen.cod.jetpackvideo.ui.publish;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.util.Util;
import com.jesen.cod.jetpackvideo.R;
import com.jesen.cod.jetpackvideo.databinding.ActivityPreviewBinding;
import com.jesen.cod.libcommon.utils.Og;

import java.io.File;

public class PreviewActivity extends AppCompatActivity implements View.OnClickListener{

    private ActivityPreviewBinding mBinding;
    private static final String KEY_PREVIEW_URL = "key_preview_url";
    private static final String KEY_PREVIEW_IS_VIDEO = "key_preview_isVideo";
    private static final String KEY_PREVIEW_BTNTEXT = "key_preview_btnText";

    public static final int REQ_PREVIEW_CODE = 1002;
    private String mMediaUrl;
    private boolean mIsVideo;
    private SimpleExoPlayer mExoPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_preview);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_preview);

        mMediaUrl = getIntent().getStringExtra(KEY_PREVIEW_URL);
        mIsVideo = getIntent().getBooleanExtra(KEY_PREVIEW_IS_VIDEO,false);
        String btnText = getIntent().getStringExtra(KEY_PREVIEW_BTNTEXT);

        if (TextUtils.isEmpty(btnText)){
            mBinding.btnEnsure.setVisibility(View.GONE);
        }
        mBinding.btnEnsure.setOnClickListener(this);
        mBinding.btnClose.setOnClickListener(this);

        if (mIsVideo){
            previewVideo(mMediaUrl);
        }else {
            previewImage(mMediaUrl);
        }
    }

    public static void startActivityForResult(Activity activity, String previewUrl, boolean isVideo, String btnText){
        Intent intent = new Intent(activity, PreviewActivity.class);
        intent.putExtra(KEY_PREVIEW_URL, previewUrl);
        intent.putExtra(KEY_PREVIEW_IS_VIDEO, isVideo);
        intent.putExtra(KEY_PREVIEW_BTNTEXT, btnText);

        activity.startActivityForResult(intent, REQ_PREVIEW_CODE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_ensure:
                setResult(RESULT_OK, new Intent());
                finish();
                break;
            case R.id.back_close:
                finish();
                break;
            default:
                Og.d("PreviewActivity, default.");
        }
    }

    private void previewImage(String url) {
        mBinding.photoView.setVisibility(View.VISIBLE);
        Glide.with(this).load(url).into(mBinding.photoView);
    }

    private void previewVideo(String url) {
        mBinding.photoView.setVisibility(View.VISIBLE);
         mExoPlayer = ExoPlayerFactory.newSimpleInstance(this, new DefaultRenderersFactory(this),
                new DefaultTrackSelector(), new DefaultLoadControl());

         Uri uri = null;
        File file = new File(url);
        if (file.exists()){
            DataSpec dataSpec = new DataSpec(Uri.fromFile(file));
            FileDataSource fileDataSource = new FileDataSource();
            try {
                fileDataSource.open(dataSpec);
                uri = fileDataSource.getUri();
            } catch (FileDataSource.FileDataSourceException e) {
                e.printStackTrace();
            }
        }else {
            Uri.parse(url);
        }

        ProgressiveMediaSource.Factory factory = new ProgressiveMediaSource.Factory(new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, getPackageName())));
        ProgressiveMediaSource mediaSource = factory.createMediaSource(uri);
        mExoPlayer.prepare(mediaSource);
        mExoPlayer.setPlayWhenReady(true);
        mBinding.playerView.setPlayer(mExoPlayer);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mExoPlayer != null){
            mExoPlayer.setPlayWhenReady(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mExoPlayer != null){
            mExoPlayer.setPlayWhenReady(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mExoPlayer !=null){
            mExoPlayer.setPlayWhenReady(false);
            mExoPlayer.stop(true);
            mExoPlayer.release();
            mExoPlayer = null;
        }
    }
}