package com.jesen.cod.jetpackvideo.ui.dashboard;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.jesen.cod.jetpackvideo.R;
import com.jesen.cod.jetpackvideo.model.Feed;
import com.jesen.cod.libcommon.extention.AbsPagedListAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PagerSnapHelperAdapter extends AbsPagedListAdapter<Feed,PagerSnapHelperAdapter.ViewHolder> {

    // 数据集
    private List<String> mDataList;

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
    }

    public void setDataList(List<String> dataList) {
        this.mDataList = dataList;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Log.e("xiaxl: ", "---onCreateViewHolder---");
        // 创建一个View，简单起见直接使用系统提供的布局，就是一个TextView
        View view = View.inflate(viewGroup.getContext(), R.layout.ps_recycle_pager_item, null);


        // 创建一个ViewHolder
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    protected ViewHolder onCreateViewHolder2(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        Log.e("xiaxl: ", "---onBindViewHolder---");

        // 绑定数据到ViewHolder上
        viewHolder.itemView.setTag(position);
        //
        viewHolder.mTextView.setText(position + " item" + "| " + mDataList.get(position));
    }

    @Override
    protected void onBindViewHolder2(ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    /**
     *
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView mTextView;

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
