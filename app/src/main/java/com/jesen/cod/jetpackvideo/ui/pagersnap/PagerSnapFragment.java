package com.jesen.cod.jetpackvideo.ui.dashboard;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.jesen.cod.jetpackvideo.R;
import com.jesen.cod.jetpackvideo.model.Feed;
import com.jesen.cod.jetpackvideo.ui.view.AbsListFragment;
import com.scwang.smartrefresh.layout.api.RefreshLayout;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PagerSnapFragment extends AbsListFragment<Feed,PagerSnapViewModel> {

    private PagerSnapViewModel pagerSnapViewModel;
    private RecyclerView mRecyclerView;
    private PagerSnapHelperAdapter mAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        pagerSnapViewModel =
                new ViewModelProvider(this).get(PagerSnapViewModel.class);

        View root = inflater.inflate(R.layout.fragment_dashboard, container,false);
        //mRecyclerView = root.findViewById(R.id.ps_recycler_view);
        mRecyclerView.setNestedScrollingEnabled(false);

        initUI();

        pagerSnapViewModel.getData().observe(getViewLifecycleOwner(), new Observer<List<String>>() {
            @Override
            public void onChanged(List<String> strings) {
                mAdapter.setDataList(strings);
            }
        });
        return root;
    }

    @Override
    public PagedListAdapter getAdapter() {
        return null;
    }


    public void initUI() {
        // PagerSnapHelper
        PagerSnapHelper snapHelper = new PagerSnapHelper() {
            // 在 Adapter的 onBindViewHolder 之后执行
            @Override
            public int findTargetSnapPosition(RecyclerView.LayoutManager layoutManager, int velocityX, int velocityY) {
                // TODO 找到对应的Index
                Log.e("xiaxl: ", "---findTargetSnapPosition---");
                int targetPos = super.findTargetSnapPosition(layoutManager, velocityX, velocityY);
                Log.e("xiaxl: ", "targetPos: " + targetPos);

                Toast.makeText(getContext(), "滑到到 " + targetPos + "位置", Toast.LENGTH_SHORT).show();

                return targetPos;
            }

            // 在 Adapter的 onBindViewHolder 之后执行
            @Nullable
            @Override
            public View findSnapView(RecyclerView.LayoutManager layoutManager) {
                // TODO 找到对应的View
                Log.e("xiaxl: ", "---findSnapView---");
                View view = super.findSnapView(layoutManager);
                Log.e("xiaxl: ", "tag: " + view.getTag());

                return view;
            }
        };
        snapHelper.attachToRecyclerView(mRecyclerView);

        // ---布局管理器---
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        // 默认是Vertical (HORIZONTAL则为横向列表)
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mRecyclerView.setLayoutManager(linearLayoutManager);

        // 这么写是为了获取RecycleView的宽高
        mRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    mRecyclerView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    mRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }

                /**
                 *  这么写是为了获取RecycleView的宽高
                 */

                // 创建Adapter，并指定数据集
                // 设置Adapter
                mRecyclerView.setAdapter(mAdapter);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onLoadMore(@NonNull @NotNull RefreshLayout refreshLayout) {

    }

    @Override
    public void onRefresh(@NonNull @NotNull RefreshLayout refreshLayout) {

    }
}