package com.jesen.cod.jetpackvideo.ui.detail;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

import com.jesen.cod.jetpackvideo.R;
import com.jesen.cod.jetpackvideo.model.Feed;

public class FeedDetailActivity extends AppCompatActivity {

    private static final String KEY_FEED = "key_feed";
    private static final String KEY_CATEGORY = "key_category";
    private ViewHandler viewHandler = null;


    public static void startFeedDetailActivity(Context context, Feed item, String category) {
        Intent intent = new Intent(context, FeedDetailActivity.class);
        intent.putExtra(KEY_FEED,item);
        intent.putExtra(KEY_CATEGORY,category);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Feed feed = (Feed)getIntent().getSerializableExtra(KEY_FEED);
        if (feed == null){
            finish();
            return;
        }

        if (feed.itemType ==Feed.TYPE_IMAGE_TEXT){
            viewHandler = new ImageViewHandler(this);
        }else {
            viewHandler = new VideoHandler(this);
        }

        viewHandler.bindInitData(feed);
    }
}