package com.jesen.cod.jetpackvideo.ui.detail;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Parcelable;

import com.jesen.cod.jetpackvideo.R;
import com.jesen.cod.jetpackvideo.model.Feed;

public class FeedDetailActivity extends AppCompatActivity {

    private static final String KEY_FEED = "key_feed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Feed feed = getIntent().getParcelableExtra(KEY_FEED);
        if (feed == null){
            finish();
            return;
        }

        ViewHandler viewHandler = null;

        if (feed.itemType ==Feed.TYPE_IMAGE_TEXT){
            viewHandler = new ImageViewHandler(this);
        }else {
            viewHandler = new VideoHandler(this);
        }

        viewHandler.bindInitData(feed);
    }
}