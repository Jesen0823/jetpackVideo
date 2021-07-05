package com.jesen.cod.jetpackvideo.ui.mine;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;

import com.jesen.cod.jetpackvideo.R;
import com.jesen.cod.jetpackvideo.model.Feed;
import com.jesen.cod.jetpackvideo.model.PersonalTabType;
import com.jesen.cod.jetpackvideo.ui.MutableItemKeyedDataSource;
import com.jesen.cod.jetpackvideo.ui.home.FeedAdapter;
import com.jesen.cod.jetpackvideo.ui.home.InteractionPresenter;
import com.jesen.cod.jetpackvideo.ui.login.UserManager;
import com.jesen.cod.jetpackvideo.utils.TimeUtils;
import com.jesen.cod.libcommon.utils.Og;

import org.jetbrains.annotations.NotNull;

/*
 * 复用首页的feedAdapter
 * */
public class PersonalListAdapter extends FeedAdapter {

    private static final String TAG = "PersonalListAdapter";
    private boolean isCommentTab = false;

    protected PersonalListAdapter(Context context, String category) {
        super(context, category);
    }

    @Override
    public int getItemViewType2(int position) {
        Og.d(TAG + ",getItemViewType2, PersonalTabType.TAB_COMMENT: " + PersonalTabType.TAB_COMMENT.toString());
        Og.d(TAG + ",getItemViewType2, mCategory: " + mCategory);
        if (TextUtils.equals(mCategory, PersonalTabType.TAB_COMMENT.toString())) {
            isCommentTab = true;
            return R.layout.layout_feed_type_comment;

        } else if (TextUtils.equals(mCategory, PersonalTabType.TAB_ALL.toString())) {
            isCommentTab = false;
            Feed feed = getItem(position);
            if (feed.topComment != null && feed.topComment.userId == UserManager.get().getUserId()) {
                return R.layout.layout_feed_type_comment;
            }
            return super.getItemViewType2(position);
        } else {
            isCommentTab = false;
            return super.getItemViewType2(position);
        }
    }

    @Override
    protected void onBindViewHolder2(ViewHolder holder, int position) {
        super.onBindViewHolder2(holder, position);
        View delFeedV = holder.itemView.findViewById(R.id.feed_delete);
        TextView createTime = holder.itemView.findViewById(R.id.create_time);

        Feed item = getItem(position);
        createTime.setVisibility(View.VISIBLE);
        createTime.setText(TimeUtils.calculate(item.createTime));

        delFeedV.setVisibility(View.VISIBLE);
        delFeedV.setOnClickListener(v -> {
            if (isCommentTab) {
                // 删除评论
                InteractionPresenter.deleteFeedComment(mContext, item.itemId, item.topComment.commentId).observe((LifecycleOwner) mContext, new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean success) {
                        refreshList(item);
                    }
                });
            }
            {
                // 删除帖子
                InteractionPresenter.deleteFeed(mContext, item.itemId).observe((LifecycleOwner) mContext, new Observer<Boolean>() {
                    @Override
                    public void onChanged(Boolean success) {
                        refreshList(item);
                    }
                });
            }
        });
    }

    /*
     * 一般方案是重新请求数据刷新页面
     * 2. 使用 MutableItemKeyedDataSource
     * */
    private void refreshList(Feed delete) {
        PagedList<Feed> currentList = getCurrentList();
        MutableItemKeyedDataSource<Integer, Feed> dataSource
                = new MutableItemKeyedDataSource<Integer, Feed>((ItemKeyedDataSource) currentList.getDataSource()) {
            @Override
            public @NotNull Integer getKey(@NonNull Feed item) {
                return item.id;
            }
        };

        // 过滤掉刚才删除的帖子
        for (Feed feed : currentList) {
            if (feed != delete) {
                dataSource.data.add(feed);
            }
        }

        PagedList<Feed> newPageList = dataSource.buildNewItemList(currentList.getConfig());
        submitList(newPageList);
    }
}
