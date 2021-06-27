package com.jesen.cod.jetpackvideo.ui.sofa;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.jesen.cod.jetpackvideo.R;
import com.jesen.cod.jetpackvideo.databinding.FragmentSofaBinding;
import com.jesen.cod.jetpackvideo.model.SofaTab;
import com.jesen.cod.jetpackvideo.ui.home.HomeFragment;
import com.jesen.cod.jetpackvideo.utils.AppConfig;
import com.jesen.cod.jetpackvideo.utils.Og;
import com.jesen.cod.libnavannotation.FragmentDestination;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@FragmentDestination(pageUrl = "main/tabs/sofa", asStarter = false)
public class SofaFragment extends Fragment {

    private FragmentSofaBinding binding;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private SofaTab tabConfig;
    private List<SofaTab.Tabs> tabs;
    private Map<Integer, Fragment> mFragmentMap = new HashMap<>();
    private TabLayoutMediator mediator;

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
         binding = FragmentSofaBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        viewPager = binding.viewPager;
        tabLayout = binding.tabLayout;

        // 过滤tabs
         tabConfig = getTabConfig();
         tabs = new ArrayList<>();
        for (SofaTab.Tabs tab : tabConfig.tabs) {
            if (tab.enable){
                tabs.add(tab);
            }
        }

        viewPager.setOffscreenPageLimit(ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT);
        viewPager.setAdapter(new FragmentStateAdapter(getChildFragmentManager(), this.getLifecycle()) {
            @NonNull
            @NotNull
            @Override
            public Fragment createFragment(int position) {
                Fragment fragment = mFragmentMap.get(position);
                if (fragment == null){
                    fragment = getTabFragment(position);
                }
                return fragment;
            }

            @Override
            public int getItemCount() {
                return tabs.size();
            }
        });

         mediator = new TabLayoutMediator(tabLayout, viewPager, false, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull @NotNull TabLayout.Tab tab, int position) {
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
                viewPager.setCurrentItem(tabConfig.select);
            }
        });
    }

    ViewPager2.OnPageChangeCallback mPageChangeCallback = new ViewPager2.OnPageChangeCallback() {
        @Override
        public void onPageSelected(int position) {
            int tabCount = tabLayout.getTabCount();
            for(int i =0; i< tabCount;i++){
                TabLayout.Tab tab = tabLayout.getTabAt(i);
                TextView customView = (TextView) tab.getCustomView();
                if (tab.getPosition() == position){
                    customView.setTextSize(tabConfig.activeSize);
                    customView.setTypeface(Typeface.DEFAULT_BOLD);
                }else {
                    customView.setTextSize(tabConfig.normalSize);
                    customView.setTypeface(Typeface.DEFAULT);
                }

            }

        }
    };

    private View makeTabView(int position) {
        TextView tabView = new TextView(getContext());
        int [][] states = new int[2][];
        states[0] = new int[]{android.R.attr.state_selected};
        states[1] = new int[]{};

        int [] colors = new int[]{Color.parseColor(tabConfig.activeColor), Color.parseColor(tabConfig.normalColor)};
        ColorStateList stateList = new ColorStateList(states, colors);
        tabView.setTextColor(stateList);
        tabView.setText(tabs.get(position).title);
        tabView.setTextSize(tabConfig.normalSize);
        return tabView;
    }

    private Fragment getTabFragment(int position) {
        return HomeFragment.newInstance(tabs.get(position).tag);
    }

    private SofaTab getTabConfig() {
        return AppConfig.getSofaTabConfig();
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