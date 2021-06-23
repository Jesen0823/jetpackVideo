package com.jesen.cod.jetpackvideo.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.AsyncDifferConfig;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.jesen.cod.jetpackvideo.databinding.LayoutFeedTypeImageBinding;
import com.jesen.cod.jetpackvideo.databinding.LayoutFeedTypeVideoBinding;
import com.jesen.cod.jetpackvideo.model.Feed;

import org.jetbrains.annotations.NotNull;

/*
* Paging框架的PageList有两个能力：
* 1.计算数据的差分，PagedStorageDiffHelper比较新数据和旧数据，比较完后去刷新有差异的数据Item或删除
* DiffUtil使用的差分异算法是Myers算法
* 2. 监听PageList数据的变更，AsyncPageListDiffer会回调insert,remove,changed,通过UpdateCallback中转
*
* */
public class FeedAdapter extends PagedListAdapter<Feed, FeedAdapter.ViewHolder> {

    private  String mCategory;
    private LayoutInflater mLayoutInflater;
    private Context mContext;

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

    protected FeedAdapter(@NonNull @NotNull AsyncDifferConfig<Feed> config) {
        super(config);
    }

    @Override
    public int getItemViewType(int position) {
        Feed item = getItem(position);
        return item.itemType;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        ViewDataBinding binding = null;
        if (viewType == Feed.TYPE_IMAGE_TEXT){
             binding = LayoutFeedTypeImageBinding.inflate(mLayoutInflater);
        }else {
             binding = LayoutFeedTypeVideoBinding.inflate(mLayoutInflater);
        }
        return new ViewHolder(binding.getRoot(), binding);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        holder.bindData(getItem(position));
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ViewDataBinding mBinding;

        public ViewHolder(@NonNull @NotNull View itemView, ViewDataBinding binding) {
            super(itemView);
            mBinding = binding;
        }

        public void bindData(Feed item) {
            if (mBinding instanceof LayoutFeedTypeImageBinding){
                LayoutFeedTypeImageBinding imageBinding = (LayoutFeedTypeImageBinding) mBinding;
                imageBinding.setFeed(item);
                imageBinding.feedImage.bindData(item.cover,item.width, item.height,16);
            }else if (mBinding instanceof LayoutFeedTypeVideoBinding){
                LayoutFeedTypeVideoBinding videoBinding = (LayoutFeedTypeVideoBinding) mBinding;
                videoBinding.setFeed(item);
                videoBinding.listPlayerView.bindData(mCategory, item.width,item.height,item.cover,item.url);
            }
        }
    }
}
