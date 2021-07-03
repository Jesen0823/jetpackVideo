package com.jesen.cod.jetpackvideo.ui.find;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.jesen.cod.jetpackvideo.R;
import com.jesen.cod.jetpackvideo.databinding.LayoutFindTagListItemBinding;
import com.jesen.cod.jetpackvideo.model.TagList;
import com.jesen.cod.jetpackvideo.ui.home.InteractionPresenter;
import com.jesen.cod.jetpackvideo.utils.StringUtil;
import com.jesen.cod.libcommon.extention.AbsPagedListAdapter;

import org.jetbrains.annotations.NotNull;

public class FindTagListAdapter extends AbsPagedListAdapter<TagList, FindTagListAdapter.ViewHolder> {

    private static final String TAG = "FindTagListAdapter";
    private final Context mContext;
    private final LayoutInflater mLayoutInflater;


    protected FindTagListAdapter(Context context) {

        // 差分异规则
        super(new DiffUtil.ItemCallback<TagList>() {
            @Override
            public boolean areItemsTheSame(@NonNull TagList oldItem, @NonNull TagList newItem) {
                return oldItem.tagId == newItem.tagId;
            }

            @Override
            public boolean areContentsTheSame(@NonNull TagList oldItem, @NonNull TagList newItem) {
                return oldItem.equals(newItem);
            }
        });
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    protected ViewHolder onCreateViewHolder2(ViewGroup parent, int viewType) {
        LayoutFindTagListItemBinding itemBinding = LayoutFindTagListItemBinding.inflate(mLayoutInflater, parent, false);
        return new ViewHolder(itemBinding.getRoot(),itemBinding);
    }

    @Override
    protected void onBindViewHolder2(ViewHolder holder, int position) {
        holder.bindData(getItem(position));
        holder.mItemBinding.followBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InteractionPresenter.toggleTagLike((LifecycleOwner) mContext,getItem(position));
            }
        });
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private final LayoutFindTagListItemBinding mItemBinding;

        public ViewHolder(@NonNull View itemView, LayoutFindTagListItemBinding binding) {
            super(itemView);
            mItemBinding = binding;

        }

        public void bindData(TagList item){
            mItemBinding.setTagList(item);
        }
    }
}
