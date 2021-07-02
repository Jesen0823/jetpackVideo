package com.jesen.cod.jetpackvideo.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
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
import com.jesen.cod.jetpackvideo.model.Feed;
import com.jesen.cod.jetpackvideo.ui.detail.FeedDetailActivity;
import com.jesen.cod.jetpackvideo.ui.view.ListPlayerView;
import com.jesen.cod.libcommon.utils.Og;
import com.jesen.cod.libcommon.extention.AbsPagedListAdapter;
import com.jesen.cod.libcommon.extention.LiveDataBus;

import org.jetbrains.annotations.NotNull;

/*
* Paging框架的PageList有两个能力：
* 1.计算数据的差分，PagedStorageDiffHelper比较新数据和旧数据，比较完后去刷新有差异的数据Item或删除
* DiffUtil使用的差分异算法是Myers算法
* 2. 监听PageList数据的变更，AsyncPageListDiffer会回调insert,remove,changed,通过UpdateCallback中转
*
* */
public class FeedAdapter extends AbsPagedListAdapter<Feed, FeedAdapter.ViewHolder> {

    protected  String mCategory;
    private final LayoutInflater mLayoutInflater;
    protected Context mContext;
    private FeedObserver mFeedObserver;

    // DiffCallback 用来处理差分
    protected FeedAdapter(Context context, String category) {
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
        mCategory = category;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getItemViewType2(int position) {
        Feed feed = getItem(position);
        if (feed.itemType == Feed.TYPE_IMAGE_TEXT) {
            return R.layout.layout_feed_type_image;
        } else if (feed.itemType == Feed.TYPE_VIDEO) {
            return R.layout.layout_feed_type_video;
        }
        return 0;
    }

    @NonNull
   /* @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        ViewDataBinding binding = null;
        if (viewType == Feed.TYPE_IMAGE_TEXT){
             binding = LayoutFeedTypeImageBinding.inflate(mLayoutInflater);

        }else {
             binding = LayoutFeedTypeVideoBinding.inflate(mLayoutInflater);
        }
        return new ViewHolder(binding.getRoot(), binding);
    }*/

    @Override
    protected ViewHolder onCreateViewHolder2(ViewGroup parent, int viewType) {
        ViewDataBinding binding = DataBindingUtil.inflate(mLayoutInflater, viewType, parent, false);
        return new ViewHolder(binding.getRoot(), binding);
    }

    /*@Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        holder.bindData(getItem(position));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Og.i("item click, go detail page.");
                FeedDetailActivity.startFeedDetailActivity(mContext, getItem(position),mCategory);
            }
        });
    }*/

    @Override
    protected void onBindViewHolder2(ViewHolder holder, int position) {
        final Feed feed = getItem(position);
        holder.bindData(feed);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Og.i("item click, go detail page, feed:"+feed.id+",mCategory:"+mCategory+",mContext:"+mContext.toString());
                FeedDetailActivity.startFeedDetailActivity(mContext, feed,mCategory);
                onStartFeedDetailActivity(feed);
                if (mFeedObserver == null){
                    mFeedObserver = new FeedObserver();
                    LiveDataBus.getInstance().with(InteractionPresenter.EVENT_DATA_FROM_INTERACTION)
                            .observe((LifecycleOwner) mContext, mFeedObserver);
                }
                mFeedObserver.setFeed(feed);
            }
        });
    }

    private class FeedObserver implements Observer<Feed>{

        private Feed mFeed;

        @Override
        public void onChanged(Feed newFeed) {
            if(mFeed.id != newFeed.id){
                return;
            }
            mFeed.author = newFeed.author;
            mFeed.ugc = newFeed.ugc;
            mFeed.notifyChange();
        }

        public void setFeed(Feed feed) {

            mFeed = feed;
        }
    }

    public void onStartFeedDetailActivity(Feed feed) {

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ViewDataBinding mBinding;
        public ListPlayerView listPlayerView;
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
            if (mBinding instanceof LayoutFeedTypeImageBinding) {
                LayoutFeedTypeImageBinding imageBinding = (LayoutFeedTypeImageBinding) mBinding;
                feedImage = imageBinding.feedImage;
                imageBinding.feedImage.bindData(item.cover,item.width, item.height, 16);
                //imageBinding.setFeed(item);
                //imageBinding.interactionBinding.setLifeCycleOwner((LifecycleOwner) mContext);
            } else if (mBinding instanceof LayoutFeedTypeVideoBinding) {
                LayoutFeedTypeVideoBinding videoBinding = (LayoutFeedTypeVideoBinding) mBinding;
                videoBinding.listPlayerView.bindData(mCategory, item.width, item.height, item.cover, item.url);
                listPlayerView = videoBinding.listPlayerView;
                //videoBinding.setFeed(item);
                //videoBinding.interactionBinding.setLifeCycleOwner((LifecycleOwner) mContext);
            }
        }

        public boolean isVideoItem() {
            return mBinding instanceof LayoutFeedTypeVideoBinding;
        }

        public ListPlayerView getListPlayerView() {
            return listPlayerView;
        }
    }

}
