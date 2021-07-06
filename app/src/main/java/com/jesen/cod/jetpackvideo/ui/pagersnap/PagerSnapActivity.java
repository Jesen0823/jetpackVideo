package com.jesen.cod.jetpackvideo.ui.dashboard;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import com.jesen.cod.jetpackvideo.R;
import com.jesen.cod.jetpackvideo.utils.ToastUtil;

import java.util.List;

public class PagerSnapActivity extends AppCompatActivity {

    private PagerSnapViewModel pagerSnapViewModel;
    private RecyclerView mRecyclerView;
    private PagerSnapHelperAdapter mAdapter;


    public static void startActivity(Context context){
        Intent intent = new Intent(context, PagerSnapActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pager_snap);

        pagerSnapViewModel =
                new ViewModelProvider(this).get(PagerSnapViewModel.class);

        mRecyclerView = findViewById(R.id.ps_recycler_view);
        mRecyclerView.setNestedScrollingEnabled(false);

        initUI();
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

                ToastUtil.show(PagerSnapActivity.this,"滑到到 " + targetPos + "位置");

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
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
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

                // 设置Adapter
                mRecyclerView.setAdapter(mAdapter);
                pagerSnapViewModel.getData().observe(PagerSnapActivity.this, new Observer<List<String>>() {
                    @Override
                    public void onChanged(List<String> strings) {
                        mAdapter.setDataList(strings);
                    }
                });
            }
        });
    }
}