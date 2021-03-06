package com.jesen.cod.jetpackvideo.ui.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;
import com.jesen.cod.jetpackvideo.R;
import com.jesen.cod.jetpackvideo.model.BottomBar;
import com.jesen.cod.jetpackvideo.model.Destination;
import com.jesen.cod.jetpackvideo.utils.AppConfig;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AppBottomBar extends BottomNavigationView {

    private static final int[] icons = {R.drawable.ic_home_24, R.drawable.ic_sofa_toll_24,
            R.drawable.post_add, R.drawable.ic_find_24, R.drawable.ic_mine_24};

    private BottomBar config;

    public AppBottomBar(@NonNull @NotNull Context context) {
        this(context, null);
    }

    public AppBottomBar(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @SuppressLint("RestrictedApi")
    public AppBottomBar(@NonNull @NotNull Context context, @Nullable @org.jetbrains.annotations.Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        config = AppConfig.getBottomBar();

        BottomBar bottomBar = AppConfig.getBottomBar();
        List<BottomBar.Tabs> tabs = bottomBar.tabs;

        int[][] states = new int[2][];
        states[0] = new int[]{android.R.attr.state_selected};
        states[1] = new int[]{};
        int[] colors = new int[]{Color.parseColor(bottomBar.activeColor), Color.parseColor(bottomBar.inActiveColor)};
        ColorStateList colorStateList = new ColorStateList(states, colors);
        setItemIconTintList(colorStateList);
        setItemTextColor(colorStateList);
        //LABEL_VISIBILITY_LABELED:??????????????????????????????????????????
        //LABEL_VISIBILITY_AUTO:????????????????????????????????????????????????????????????????????????3????????????5???????????????????????????????????????????????????
        //LABEL_VISIBILITY_SELECTED??????????????????????????????????????????????????????
        //LABEL_VISIBILITY_UNLABELED:?????????????????????????????????
        setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
        setSelectedItemId(bottomBar.selectTab);
        for (int i = 0; i < tabs.size(); i++) {
            BottomBar.Tabs tab = tabs.get(i);
            if (!tab.enable) {
                continue; // ??????????????????????????????
            }
            int itemId = getItemId(tab.pageUrl);
            if (itemId < 0) {
                continue;
            }

            MenuItem menuItem = getMenu().add(0, itemId, tab.index, tab.title);
            menuItem.setIcon(icons[tab.index]);
        }
        // ?????????????????????????????????icon?????????
        for (int i = 0; i < tabs.size(); i++) {
            BottomBar.Tabs tab = tabs.get(i);
            if (!tab.enable){
                continue;
            }
            int itemId = getItemId(tab.pageUrl);
            if (itemId < 0){
                continue;
            }
            int iconSize = dp2px(tab.size);
            BottomNavigationMenuView menuView = (BottomNavigationMenuView) getChildAt(0);
            BottomNavigationItemView itemView = (BottomNavigationItemView) menuView.getChildAt(tab.index);
            itemView.setIconSize(iconSize);

            if (TextUtils.isEmpty(tab.title)){
                itemView.setIconTintList(ColorStateList.valueOf(Color.parseColor(tab.tintColor)));
                itemView.setShifting(false);
            }
        }
    }

    private int dp2px(int dpValue) {
        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        return (int) (metrics.density * dpValue + 0.5f);
    }

    private int getItemId(String pageUrl) {
        Destination destination = AppConfig.getDestConfig().get(pageUrl);
        if (destination == null) {
            return -1;
        }
        return destination.id;
    }
}
