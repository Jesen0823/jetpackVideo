package com.jesen.cod.jetpackvideo.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jesen.cod.jetpackvideo.R;
import com.jesen.cod.jetpackvideo.ui.view.ViImageView;
import com.jesen.cod.jetpackvideo.utils.Og;
import com.jesen.cod.libcommon.utils.PixUtils;
import com.jesen.cod.libcommon.view.CornerFrameLayout;
import com.jesen.cod.libcommon.view.ViewHelper;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ShareDialog extends AlertDialog {

    private List<ResolveInfo> mShareItems = new ArrayList<>();
    private ShareAdapter mShareAdapter;
    private String mShareContent;
    private View.OnClickListener mClickListener;
    private RecyclerView mGridView;
    private CornerFrameLayout mLayout;
    private Uri mShareImgUri;

    public ShareDialog(@NonNull @NotNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        mLayout = new CornerFrameLayout(getContext());
        mLayout.setBackgroundColor(Color.WHITE);
        mLayout.setViewOutline(PixUtils.dp2px(20), ViewHelper.RADIUS_TOP);

        mGridView = new RecyclerView(getContext());
        mGridView.setLayoutManager(new GridLayoutManager(getContext(), 4));
        mShareAdapter = new ShareAdapter();
        mGridView.setAdapter(mShareAdapter);

        FrameLayout.LayoutParams params = new FrameLayout
                .LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = params.topMargin = params.rightMargin = params.bottomMargin
                = PixUtils.dp2px(20);
        params.gravity = Gravity.CENTER;
        mLayout.addView(mGridView, params);

        setContentView(mLayout);
        getWindow().setGravity(Gravity.BOTTOM);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        queryShareItems();
    }

    private void queryShareItems() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("text/plain");

        // 查询文本形式分享的所有入口
        List<ResolveInfo> resolveInfos = getContext().getPackageManager().queryIntentActivities(intent, 0);
        Og.d("ShareDialog, resolveInfos size:" + resolveInfos.size());
        for (ResolveInfo resolveInfo : resolveInfos) {
            String packageName = resolveInfo.activityInfo.packageName;

            // 过滤所有的分享类型，只保留腾讯系列
            if (TextUtils.equals(packageName, "com.tencent.mm")
                    || TextUtils.equals(packageName, "com.tencent.mobileqq")) {
                mShareItems.add(resolveInfo);
            }
        }
        mShareAdapter.notifyDataSetChanged();
    }

    public void setShareContent(String content) {
        mShareContent = content;
    }

    public void setShareItemClickListener(View.OnClickListener listener) {

        mClickListener = listener;
    }

    public void setShareImg(Uri shareImgUri) {
        mShareImgUri = shareImgUri;
    }

    private class ShareAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final PackageManager packageManager;

        public ShareAdapter() {
            packageManager = getContext().getPackageManager();
        }

        @NonNull
        @NotNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext())
                    .inflate(R.layout.layout_share_item, parent, false);
            return new RecyclerView.ViewHolder(view) {
            };

        }

        @Override
        public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {
            ResolveInfo resolveInfo = mShareItems.get(position);
            ViImageView icon = holder.itemView.findViewById(R.id.share_icon);
            Drawable drawable = resolveInfo.loadIcon(packageManager);
            icon.setImageDrawable(drawable);

            TextView text = holder.itemView.findViewById(R.id.name);
            text.setText(resolveInfo.loadLabel(packageManager));

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String pkg = resolveInfo.activityInfo.packageName;
                    String cls = resolveInfo.activityInfo.name;

                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    if (mShareImgUri != null){
                        intent.setType("image/text/plain");
                        intent.putExtra(Intent.EXTRA_STREAM, mShareImgUri);
                    }
                    intent.setComponent(new ComponentName(pkg, cls));
                    intent.putExtra(Intent.EXTRA_TEXT, mShareContent);

                    getContext().startActivity(intent);

                    if (mClickListener != null) {
                        mClickListener.onClick(v);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mShareItems == null ? 0 : mShareItems.size();
        }
    }
}
