package com.jesen.cod.jetpackvideo.ui.pagersnap;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.jesen.cod.jetpackvideo.BR;
import com.jesen.cod.jetpackvideo.R;
import com.jesen.cod.jetpackvideo.databinding.LayoutFeedTypeImageBinding;
import com.jesen.cod.jetpackvideo.databinding.LayoutFeedTypeVideoBinding;
import com.jesen.cod.jetpackvideo.databinding.LayoutPsRecycleImageItemBinding;
import com.jesen.cod.jetpackvideo.databinding.LayoutPsRecycleVideoItemBinding;
import com.jesen.cod.jetpackvideo.model.Feed;
import com.jesen.cod.jetpackvideo.ui.detail.FeedDetailActivity;
import com.jesen.cod.jetpackvideo.ui.home.InteractionPresenter;
import com.jesen.cod.jetpackvideo.ui.view.FullScreenPlayerView;
import com.jesen.cod.jetpackvideo.ui.view.ListPlayerView;
import com.jesen.cod.libcommon.extention.AbsPagedListAdapter;
import com.jesen.cod.libcommon.extention.LiveDataBus;
import com.jesen.cod.libcommon.utils.Og;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PagerSnapHelperAdapter extends AbsPagedListAdapter<Feed,PagerSnapHelperAdapter.ViewHolder> {

    private static final String TAG = "PagerSnapHelperAdapter";
    protected String mFeedType;
    private final LayoutInflater mLayoutInflater;
    protected Context mContext;
    private SFeedObserver  sFeedObserver;

    //
    public PagerSnapHelperAdapter(Context context, String category) {

        super(new DiffUtil.ItemCallback<Feed>() {

            // 比较两个Item是否相同
            @Override
            public boolean areItemsTheSame(@NonNull @NotNull Feed oldItem, @NonNull @NotNull Feed newItem) {
                return oldItem.id == newItem.id;
            }

            // 比较两个Item的内容是否相同
            @Override
            public boolean areContentsTheSame(@NonNull @NotNull Feed oldItem, @NonNull @NotNull Feed newItem) {
                return oldItem.equals(newItem);
            }
        });
        mContext = context;
        mFeedType = category;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    protected int getItemViewType2(int position) {
        Feed feed = getItem(position);
        Og.d(TAG+", getItemViewType2, itemType:"+feed.itemType);
        if (feed.itemType == Feed.TYPE_IMAGE_TEXT) {
            return R.layout.layout_ps_recycle_image_item;
        } else if (feed.itemType == Feed.TYPE_VIDEO) {
            return R.layout.layout_ps_recycle_video_item;
        }
        return 0;
    }


    @Override
    protected ViewHolder onCreateViewHolder2(ViewGroup parent, int viewType) {
        Og.d(TAG+", onCreateViewHolder2, viewType:"+viewType);
        ViewDataBinding binding = DataBindingUtil.inflate(mLayoutInflater,viewType,parent,false);
        return new ViewHolder(binding.getRoot(),binding);
    }

    @Override
    protected void onBindViewHolder2(ViewHolder holder, int position) {
        // 绑定数据到ViewHolder上
        holder.itemView.setTag(position);
        final Feed feed = getItem(position);
        holder.bindData(feed);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Og.i("item click, go detail page, feed:"+feed.id+", itemId:"+feed.itemId+", feedContent:"+feed.feeds_text);
                FeedDetailActivity.startFeedDetailActivity(mContext, feed,mFeedType);
                onStartFeedDetailActivity(feed);
                if (sFeedObserver == null){
                    sFeedObserver = new SFeedObserver();
                    LiveDataBus.getInstance().with(InteractionPresenter.EVENT_DATA_FROM_INTERACTION)
                            .observe((LifecycleOwner) mContext, sFeedObserver);
                }
                sFeedObserver.setFeed(feed);
            }
        });
    }

    /**
     *
     */
    public class ViewHolder extends RecyclerView.ViewHolder {

        public ViewDataBinding mBinding;
        public FullScreenPlayerView listPlayerView;
        public ImageView feedImage;

        public ViewHolder(@NonNull View itemView, ViewDataBinding binding) {
            super(itemView);
            mBinding = binding;
        }

        public void bindData(Feed item) {
            //这里之所以手动绑定数据的原因是 图片 和视频区域都是需要计算的
            //而dataBinding的执行默认是延迟一帧的。
            //当列表上下滑动的时候 ，会明显的看到宽高尺寸不对称的问题

            mBinding.setVariable(BR.feed, item);
            mBinding.setVariable(BR.lifeCycleOwner, mContext);
            if (mBinding instanceof LayoutPsRecycleVideoItemBinding) {
                LayoutPsRecycleVideoItemBinding videoBinding = (LayoutPsRecycleVideoItemBinding) mBinding;
                videoBinding.playerViewFull.bindData(mFeedType, item.width, item.height, item.cover, item.url);
                listPlayerView = videoBinding.playerViewFull;

            } else if (mBinding instanceof LayoutPsRecycleImageItemBinding) {
                LayoutPsRecycleImageItemBinding imageBinding = (LayoutPsRecycleImageItemBinding) mBinding;
                feedImage = imageBinding.feedImage;
                imageBinding.feedImage.bindData(item.cover,item.width, item.height, 16);
            }
        }

        public boolean isVideoItem() {
            return mBinding instanceof LayoutPsRecycleVideoItemBinding;
        }

        public ListPlayerView getListPlayerView() {
            return listPlayerView;
        }
    }


    private class SFeedObserver implements Observer<Feed>{
        private Feed feed;

        @Override
        public void onChanged(Feed newFeed) {
            if (feed.id != newFeed.id) return;;
            feed.author = newFeed.author;
            feed.ugc = newFeed.ugc;
            feed.notifyChange();
        }

        public void setFeed(Feed feed){
            this.feed = feed;
        }
    }

    public void onStartFeedDetailActivity(Feed feed){

    }
}
