package com.jesen.cod.jetpackvideo.ui.publish;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.jesen.cod.jetpackvideo.R;
import com.jesen.cod.jetpackvideo.model.TagList;
import com.jesen.cod.jetpackvideo.ui.login.UserManager;
import com.jesen.cod.jetpackvideo.utils.ToastUtil;
import com.jesen.cod.libcommon.utils.PixUtils;
import com.jesen.cod.libnetwork.ApiResponse;
import com.jesen.cod.libnetwork.ApiService;
import com.jesen.cod.libnetwork.JsonCallback;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class TagBottomSheetDialog extends BottomSheetDialogFragment {


    private static final String URL_TAG_LIST = "/tag/queryTagList";
    private RecyclerView recyclerView;
    private List<TagList> mTagsResult = new ArrayList<>();
    private TagsAdapter mTagAdapter;
    private OnTagItemSelectListener mTagItemSelectListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_tag_bottom_sheet_dialog,
                null, false);
        recyclerView = view.findViewById(R.id.record_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mTagAdapter = new TagsAdapter();
        recyclerView.setAdapter(mTagAdapter);

        dialog.setContentView(view);

        ViewGroup viewGroup = (ViewGroup) view.getParent();
        BottomSheetBehavior<ViewGroup> sheetBehavior = BottomSheetBehavior.from(viewGroup);
        sheetBehavior.setPeekHeight(PixUtils.getScreenHeight() / 3);
        // 滑动到指定高度时隐藏，而不是一直收缩到0
        sheetBehavior.setHideable(false);

        ViewGroup.LayoutParams layoutParams = viewGroup.getLayoutParams();
        // Dialog展开时的最大高度
        layoutParams.height = PixUtils.getScreenHeight() / 3 * 2;
        viewGroup.setLayoutParams(layoutParams);

        requestTagList();
        return dialog;
    }

    private void requestTagList() {
        ApiService.get(URL_TAG_LIST)
                .addParams("userId", UserManager.get().getUserId())
                .addParams("pageCount", 100)
                .addParams("tagId", 0)
                .execute(new JsonCallback<List<TagList>>() {
                    @SuppressLint("RestrictedApi")
                    @Override
                    public void onSuccess(ApiResponse<List<TagList>> response) {
                        if (response.body != null) {
                            List<TagList> tagLists = response.body;
                            mTagsResult.clear();
                            mTagsResult.addAll(tagLists);
                            ArchTaskExecutor.getMainThreadExecutor().execute(new Runnable() {
                                @Override
                                public void run() {
                                    mTagAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }

                    @Override
                    public void onError(ApiResponse<List<TagList>> response) {
                        super.onError(response);
                        ToastUtil.showOnUI(getContext(), response.message);
                    }
                });
    }

    class TagsAdapter extends RecyclerView.Adapter {

        @NonNull
        @NotNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
            TextView textView = new TextView(parent.getContext());
            textView.setTextSize(13);
            textView.setTypeface(Typeface.DEFAULT_BOLD);
            textView.setGravity(Gravity.CENTER_VERTICAL);
            textView.setTextColor(ContextCompat.getColor(parent.getContext(), R.color.color_000));
            textView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    PixUtils.dp2px(45)));
            RecyclerView.ViewHolder viewHolder = new RecyclerView.ViewHolder(textView) {
            };

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {
            TextView textView = (TextView) holder.itemView;
            TagList tagList = mTagsResult.get(position);
            textView.setText(tagList.title);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mTagItemSelectListener != null) {
                        mTagItemSelectListener.onSelectTag(tagList);
                        dismiss();
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mTagsResult.isEmpty() ? 0 : mTagsResult.size();
        }
    }

    public void setTagItemSelectListener(OnTagItemSelectListener listener) {
        mTagItemSelectListener = listener;
    }

    public interface OnTagItemSelectListener {
        void onSelectTag(TagList tagList);
    }
}
