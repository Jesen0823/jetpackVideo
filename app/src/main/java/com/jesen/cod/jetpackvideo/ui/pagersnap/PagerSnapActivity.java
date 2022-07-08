package com.jesen.cod.jetpackvideo.ui.pagersnap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.jesen.cod.jetpackvideo.R;
import com.jesen.cod.jetpackvideo.databinding.ActivityPagerSnapBinding;
import com.jesen.cod.jetpackvideo.exoplayer.PageListPlayManager;
import com.jesen.cod.jetpackvideo.utils.ToastUtil;
import com.jesen.cod.libcommon.utils.Og;
import com.jesen.cod.libcommon.utils.StatusBarUtil;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PagerSnapActivity extends AppCompatActivity {

    private static final String TAG = "PagerSnapActivity";

    private ActivityPagerSnapBinding mBinding;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, PagerSnapActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StatusBarUtil.fitSystemBar(this);

        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_pager_snap);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_pager_snap);

        String[] tabs = getResources().getStringArray(R.array.snap_tabs);
        ViewPager2 viewPager2 = mBinding.viewPager;
        TabLayout tabLayout = mBinding.tabLayout;

        viewPager2.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                Og.d(TAG + ",setAdapter createFragment");
                return PagerSnapFragment.newInstance(getTabTypeByPosition(position));
            }

            @Override
            public int getItemCount() {
                return tabs.length;
            }
        });


        // ViewPager2和TabLayout的关联
        // autoRefresh: 当调用ViewPager的adapter#notifyChanged()时要不要主动把tabLayout选项卡移除掉重新复制
        new TabLayoutMediator(tabLayout, viewPager2, false, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull @NotNull TabLayout.Tab tab, int position) {
                tab.setText(tabs[position]);
            }
        }).attach();

        // 默认选中第一个tab
        viewPager2.post(() -> viewPager2.setCurrentItem(0, false));

    }

    private String getTabTypeByPosition(int position) {
        String feedType = "";
        if (position == 0) {
            feedType = "video";
        } else if (position == 1) {
            feedType = "text";
        }
        return feedType;
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PageListPlayManager.removePageListPlay("video");
    }
}