package com.jesen.cod.jetpackvideo.ui.find;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jesen.cod.jetpackvideo.R;
import com.jesen.cod.jetpackvideo.model.SofaTab;
import com.jesen.cod.jetpackvideo.ui.sofa.SofaFragment;
import com.jesen.cod.jetpackvideo.utils.AppConfig;
import com.jesen.cod.libnavannotation.FragmentDestination;

import org.jetbrains.annotations.NotNull;

/**
 * 由于发现页面跟Sofa页面结构相似，所有继承SofaFragment的实现
 */
@FragmentDestination(pageUrl = "main/tabs/find")
public class FindFragment extends SofaFragment {

    @Override
    public Fragment getTabFragment(int position) {
        FindTagListFragment fragment = FindTagListFragment.newInstance(getTabConfig().tabs.get(position).tag);
        return fragment;
    }

    // 获取顶部标签的tab信息
    @Override
    public SofaTab getTabConfig() {
        return AppConfig.getFindTabConfig();
    }

    /*
     * ListFragment添加到当前Fragment的时候被调用
     * */
    @Override
    public void onAttachFragment(@NonNull @NotNull Fragment childFragment) {
        super.onAttachFragment(childFragment);
        String tagType = childFragment.getArguments().getString(FindTagListFragment.KEY_TAG_TYPE);
        if (TextUtils.equals(tagType, "onlyFollow")) {
            FindTagListViewModel viewModel = new ViewModelProvider(childFragment).get(FindTagListViewModel.class);
            viewModel.getSwitchTabLiveData().observe(this, new Observer() {
                @Override
                public void onChanged(Object o) {
                    if (o != null) {
                        viewPager.setCurrentItem(1);
                    }
                }
            });
        }
    }
}