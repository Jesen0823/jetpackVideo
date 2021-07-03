package com.jesen.cod.jetpackvideo.ui.find;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jesen.cod.jetpackvideo.R;
import com.jesen.cod.jetpackvideo.model.SofaTab;
import com.jesen.cod.jetpackvideo.ui.sofa.SofaFragment;
import com.jesen.cod.jetpackvideo.utils.AppConfig;
import com.jesen.cod.libnavannotation.FragmentDestination;

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
}