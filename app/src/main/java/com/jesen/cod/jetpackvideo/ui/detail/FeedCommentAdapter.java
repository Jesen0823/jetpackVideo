package com.jesen.cod.jetpackvideo.ui.detail;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.jesen.cod.jetpackvideo.databinding.LayoutFeedCommentListItemBinding;
import com.jesen.cod.jetpackvideo.model.Comment;
import com.jesen.cod.jetpackvideo.model.Feed;
import com.jesen.cod.jetpackvideo.ui.MutableItemKeyedDataSource;
import com.jesen.cod.jetpackvideo.ui.home.InteractionPresenter;
import com.jesen.cod.jetpackvideo.ui.login.UserManager;
import com.jesen.cod.jetpackvideo.ui.publish.PreviewActivity;
import com.jesen.cod.libcommon.extention.AbsPagedListAdapter;
import com.jesen.cod.libcommon.utils.Og;
import com.jesen.cod.libcommon.utils.PixUtils;

import org.jetbrains.annotations.NotNull;

public class FeedCommentAdapter extends AbsPagedListAdapter<Comment, FeedCommentAdapter.ViewHolder> {

    private static final String TAG = "FeedCommentAdapter";
    private Context mContext;
    private LayoutInflater mInflater;
    private Feed mFeed;

    public void setFeed(Feed feed){
        mFeed = feed;
    }

    protected FeedCommentAdapter(Context context) {
        super(new DiffUtil.ItemCallback<Comment>() {
            @Override
            public boolean areItemsTheSame(@NonNull Comment oldItem, @NonNull Comment newItem) {
                Og.d(TAG + ", areItemsTheSame, comment newItem: id= " + newItem.id + ", content= " + newItem.commentCount);
                return oldItem.id == newItem.id;
            }

            @Override
            public boolean areContentsTheSame(@NonNull Comment oldItem, @NonNull Comment newItem) {
                return oldItem.equals(newItem);
            }
        });
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    protected ViewHolder onCreateViewHolder2(ViewGroup parent, int viewType) {
        LayoutFeedCommentListItemBinding binding = LayoutFeedCommentListItemBinding
                .inflate(mInflater, parent, false);
        return new ViewHolder(binding.getRoot(), binding);
    }

    @Override
    protected void onBindViewHolder2(ViewHolder holder, int position) {
        Comment comment = getItem(position);

        Og.d(TAG + String.format(", onBindViewHolder2, position:\n %d, id: %d, content: %s",
                position, comment.id, comment.commentCount));

        holder.bindData(comment);

        holder.mBinding.commentDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InteractionPresenter.deleteFeedComment(mContext, comment.itemId, comment.commentId)
                        .observe((LifecycleOwner) mContext, new Observer<Boolean>() {
                            @Override
                            public void onChanged(Boolean success) {
                                if (success) {
                                    deleteAndRefreshList(comment);
                                }
                            }
                        });
            }
        });
        holder.mBinding.commentCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isVideo = comment.commentType == Comment.COMMENT_TYPE_VIDEO;
                PreviewActivity.startActivityForResult((Activity) mContext,
                        isVideo ? comment.videoUrl : comment.imageUrl,
                        isVideo, null);
            }
        });
    }

    public void deleteAndRefreshList(Comment item) {
        MutableItemKeyedDataSource<Integer, Comment> dataSource = new MutableItemKeyedDataSource<Integer, Comment>((ItemKeyedDataSource) getCurrentList().getDataSource()) {
            @NonNull
            @Override
            public Integer getKey(@NonNull Comment item) {
                return item.id;
            }
        };
        PagedList<Comment> currentList = getCurrentList();
        for (Comment comment : currentList) {
            if (comment != item) {
                dataSource.data.add(comment);
            }
        }
        PagedList<Comment> pagedList = dataSource.buildNewItemList(getCurrentList().getConfig());
        submitList(pagedList);
    }

    public void addAndRefreshList(Comment comment) {
        PagedList<Comment> currentList = getCurrentList();
        MutableItemKeyedDataSource<Integer, Comment> dataSource =
                new MutableItemKeyedDataSource<Integer, Comment>((ItemKeyedDataSource) currentList.getDataSource()) {
                    @NonNull
                    @Override
                    public @NotNull Integer getKey(@NonNull Comment item) {
                        return item.id;
                    }
                };
        dataSource.data.add(comment);
        dataSource.data.addAll(currentList);
        PagedList<Comment> pagedList = dataSource.buildNewItemList(currentList.getConfig());
        submitList(pagedList);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private LayoutFeedCommentListItemBinding mBinding;

        public ViewHolder(@NonNull View itemView, LayoutFeedCommentListItemBinding binding) {
            super(itemView);
            mBinding = binding;
        }

        public void bindData(Comment item) {
            mBinding.setComment(item);
            boolean isAuthor = item.author == null||mFeed==null ? false : mFeed.authorId == item.author.userId;

            boolean self = item.author == null ? false : UserManager.get().getUserId() == item.author.userId;
            mBinding.labelAuthor.setVisibility(isAuthor ? View.VISIBLE : View.GONE);
            if (self){
                mBinding.labelAuthor.setVisibility(View.VISIBLE);
                mBinding.labelAuthor.setText("我");
            }
            mBinding.commentDelete.setVisibility(self ? View.VISIBLE : View.GONE);
            if (!TextUtils.isEmpty(item.imageUrl)) {
                mBinding.commentExt.setVisibility(View.VISIBLE);
                mBinding.commentCover.setVisibility(View.VISIBLE);
                mBinding.commentCover.bindData(item.imageUrl, item.width, item.height, 0,
                        PixUtils.dp2px(200), PixUtils.dp2px(200));
                if (!TextUtils.isEmpty(item.videoUrl)) {
                    mBinding.videoIcon.setVisibility(View.VISIBLE);
                } else {
                    mBinding.videoIcon.setVisibility(View.GONE);
                }
            } else {
                mBinding.commentCover.setVisibility(View.GONE);
                mBinding.videoIcon.setVisibility(View.GONE);
                mBinding.commentExt.setVisibility(View.GONE);
            }
        }
    }
}
