package com.jesen.cod.jetpackvideo.ui.detail;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.jesen.cod.jetpackvideo.databinding.LayoutFeedCommentListItemBinding;
import com.jesen.cod.jetpackvideo.model.Comment;
import com.jesen.cod.jetpackvideo.ui.login.UserManager;
import com.jesen.cod.libcommon.extention.AbsPagedListAdapter;
import com.jesen.cod.libcommon.utils.PixUtils;

import org.jetbrains.annotations.NotNull;

public class FeedCommentAdapter extends AbsPagedListAdapter<Comment, FeedCommentAdapter.ViewHolder> {

    protected FeedCommentAdapter() {
        super(new DiffUtil.ItemCallback<Comment>() {
            @Override
            public boolean areItemsTheSame(@NonNull @NotNull Comment oldItem, @NonNull @NotNull Comment newItem) {
                return oldItem.id == newItem.id;
            }

            @Override
            public boolean areContentsTheSame(@NonNull @NotNull Comment oldItem, @NonNull @NotNull Comment newItem) {
                return oldItem.equals(newItem);
            }
        });
    }

    @Override
    protected ViewHolder onCreateViewHolder2(ViewGroup parent, int viewType) {
        LayoutFeedCommentListItemBinding binding = LayoutFeedCommentListItemBinding
                .inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding.getRoot(), binding);
    }

    @Override
    protected void onBindViewHolder2(ViewHolder holder, int position) {
        Comment comment = getItem(position);
        holder.bindData(comment);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private LayoutFeedCommentListItemBinding mBinding;

        public ViewHolder(@NonNull @NotNull View itemView, LayoutFeedCommentListItemBinding binding) {
            super(itemView);
            mBinding = binding;
        }

        public void bindData(Comment item) {
            mBinding.setComment(item);
            mBinding.labelAuthor.setVisibility(UserManager.get().getUserId()
                    == item.author.userId ? View.VISIBLE : View.GONE);
            mBinding.commentDelete.setVisibility(UserManager.get().getUserId()
                    == item.author.userId ? View.VISIBLE : View.GONE);
            if (!TextUtils.isEmpty(item.imageUrl)) {
                mBinding.commentCover.setVisibility(View.VISIBLE);
                mBinding.commentCover.bindData(item.imageUrl, item.width, item.height, 0,
                        PixUtils.dp2px(200), PixUtils.dp2px(200));

                mBinding.videoIcon.setVisibility(TextUtils.isEmpty(item.videoUrl) ? View.GONE : View.VISIBLE);
            } else {
                mBinding.commentCover.setVisibility(View.GONE);
                mBinding.videoIcon.setVisibility(View.GONE);
            }
        }
    }
}
