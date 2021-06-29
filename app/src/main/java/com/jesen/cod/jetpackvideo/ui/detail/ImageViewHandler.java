package com.jesen.cod.jetpackvideo.ui.detail;

import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.jesen.cod.jetpackvideo.R;
import com.jesen.cod.jetpackvideo.databinding.ActivityFeedDetailTypeImageBinding;
import com.jesen.cod.jetpackvideo.databinding.LayoutFeedDetailTypeImageHeaderBinding;
import com.jesen.cod.jetpackvideo.model.Feed;
import com.jesen.cod.jetpackvideo.ui.view.ViImageView;

import org.jetbrains.annotations.NotNull;

public class ImageViewHandler extends ViewHandler {

    protected ActivityFeedDetailTypeImageBinding mImageBinding;
    private LayoutFeedDetailTypeImageHeaderBinding mHeaderBinding;

    public ImageViewHandler(FragmentActivity activity) {
        super(activity);

        mImageBinding = DataBindingUtil.setContentView(activity, R.layout.activity_feed_detail_type_image);
        mRecyclerView = mImageBinding.recyclerView;
        mInteractionBinding = mImageBinding.bottomInteractionLayout;
        mImageBinding.backClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActivity.finish();
            }
        });
    }

    @Override
    public void bindInitData(Feed feed) {
        super.bindInitData(feed);
        mImageBinding.setFeed(mFeed);

        mHeaderBinding = LayoutFeedDetailTypeImageHeaderBinding
                .inflate(LayoutInflater.from(mActivity), mRecyclerView,false);
        mHeaderBinding.setFeed(mFeed);

        ViImageView headerImage = mHeaderBinding.headerImage;
        headerImage.bindData(mFeed.cover,mFeed.width, mFeed.height,mFeed.width>mFeed.height?0:16);

        mListAdapter.addHeaderView(mHeaderBinding.getRoot());

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull @NotNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // 滑动距离是否超过标题栏高度
                boolean visible = mHeaderBinding.getRoot().getTop()
                        <= -mImageBinding.pageTitleLayout.getMeasuredHeight();
                mImageBinding.detailAuthorInfoLayout.getRoot().setVisibility(visible? View.VISIBLE: View.GONE);
                mImageBinding.pageTitleTv.setVisibility(visible?View.GONE:View.VISIBLE);
            }
        });
        handleEmpty(false);
    }

}
