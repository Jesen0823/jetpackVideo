package com.jesen.cod.jetpackvideo.ui.sofa;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.jesen.cod.jetpackvideo.databinding.FragmentSofaBinding;
import com.jesen.cod.jetpackvideo.model.SofaTab;
import com.jesen.cod.jetpackvideo.ui.home.HomeFragment;
import com.jesen.cod.jetpackvideo.utils.AppConfig;
import com.jesen.cod.libcommon.utils.Og;
import com.jesen.cod.libnavannotation.FragmentDestination;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


@FragmentDestination(pageUrl = "main/tabs/sofa", asStarter = false)
public class SofaFragment extends Fragment {

    private FragmentSofaBinding binding;
    public ViewPager2 viewPager;
    private TabLayout tabLayout;
    private SofaTab tabConfig;
    private List<SofaTab.Tabs> tabs;
    //private Map<Integer, Fragment> mFragmentMap = new HashMap<>();
    private TabLayoutMediator mediator;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable  ViewGroup container, @Nullable  Bundle savedInstanceState) {
        binding = FragmentSofaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable  Bundle savedInstanceState) {
        viewPager = binding.viewPager;
        tabLayout = binding.tabLayout;

        // 过滤tabs
        tabConfig = getTabConfig();
        tabs = new ArrayList<>();
        for (SofaTab.Tabs tab : tabConfig.tabs) {
            if (tab.enable) {
                tabs.add(tab);
            }
        }

        viewPager.setOffscreenPageLimit(ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT);

        //viewPager2默认只有一种类型的Adapter。FragmentStateAdapter
        //并且在页面切换的时候 不会调用子Fragment的setUserVisibleHint ，取而代之的是onPause(),onResume()、
        viewPager.setAdapter(new FragmentStateAdapter(getChildFragmentManager(), this.getLifecycle()) {
            @NonNull
            @NotNull
            @Override
            public Fragment createFragment(int position) {
                /*Fragment fragment = mFragmentMap.get(position);
                if (fragment == null) {
                    fragment = getTabFragment(position);
                    mFragmentMap.put(position, fragment);
                }
                return fragment;*/

                //这里不需要自己保管了,FragmentStateAdapter内部自己会管理已实例化的fragment对象
                return getTabFragment(position);
            }

            @Override
            public int getItemCount() {
                return tabs.size();
            }
        });

        tabLayout.setTabGravity(tabConfig.tabGravity);

        //autoRefresh:如果viewPager2 中child的数量发生了变化，即调用了adapter#notifyItemChanged()前后getItemCount不同。
        //是否需要重新刷野tabLayout的tab标签视情况而定,sofaFragment的tab数量一旦固定了是不会变的，传true/false 都可以
        mediator = new TabLayoutMediator(tabLayout, viewPager, true, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setCustomView(makeTabView(position));
            }
        });
        // tabLayout 和 viewPage联动
        mediator.attach();

        // viewPage页面选中回调
        viewPager.registerOnPageChangeCallback(mPageChangeCallback);
        viewPager.post(new Runnable() {
            @Override
            public void run() {
                viewPager.setCurrentItem(tabConfig.select, false);
            }
        });
    }

    ViewPager2.OnPageChangeCallback mPageChangeCallback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageSelected(int position) {
            int tabCount = tabLayout.getTabCount();
            for (int i = 0; i < tabCount; i++) {
                TabLayout.Tab tab = tabLayout.getTabAt(i);
                TextView customView = (TextView) tab.getCustomView();
                if (tab.getPosition() == position) {
                    customView.setTextSize(tabConfig.activeSize);
                    customView.setTypeface(Typeface.DEFAULT_BOLD);
                } else {
                    customView.setTextSize(tabConfig.normalSize);
                    customView.setTypeface(Typeface.DEFAULT);
                }
            }
        }
    };

    private View makeTabView(int position) {
        TextView tabView = new TextView(getContext());
        int[][] states = new int[2][];
        states[0] = new int[]{android.R.attr.state_selected};
        states[1] = new int[]{};

        int[] colors = new int[]{Color.parseColor(tabConfig.activeColor), Color.parseColor(tabConfig.normalColor)};
        ColorStateList stateList = new ColorStateList(states, colors);
        tabView.setTextColor(stateList);
        tabView.setText(tabs.get(position).title);
        tabView.setTextSize(tabConfig.normalSize);
        return tabView;
    }

    public Fragment getTabFragment(int position) {
        return HomeFragment.newInstance(tabs.get(position).tag);
    }

    public SofaTab getTabConfig() {
        return AppConfig.getSofaTabConfig();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        List<Fragment> fragments = getChildFragmentManager().getFragments();
        for (Fragment fragment : fragments) {
            if (fragment.isAdded() && fragment.isVisible()) {
                fragment.onHiddenChanged(hidden);
                break;
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Og.d("SofaFragment, onPause.");
    }

    @Override
    public void onResume() {
        super.onResume();
        Og.d("SofaFragment, onResume.");
    }

    @Override
    public void onDestroy() {
        mediator.detach();
        viewPager.unregisterOnPageChangeCallback(mPageChangeCallback);
        super.onDestroy();
    }
}