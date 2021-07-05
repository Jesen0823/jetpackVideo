package com.jesen.cod.jetpackvideo.ui.mine;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TableLayout;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.jesen.cod.jetpackvideo.R;
import com.jesen.cod.jetpackvideo.databinding.ActivityUserPersonalBinding;
import com.jesen.cod.jetpackvideo.model.PersonalTabType;
import com.jesen.cod.jetpackvideo.model.User;
import com.jesen.cod.jetpackvideo.ui.detail.FeedDetailActivity;
import com.jesen.cod.jetpackvideo.ui.login.UserManager;
import com.jesen.cod.libcommon.utils.Og;

import org.jetbrains.annotations.NotNull;

import static com.jesen.cod.jetpackvideo.model.PersonalTabType.*;

/* 个人主页 */
public class UserPersonalActivity extends AppCompatActivity {

    private static final String TAG = "UserPersonalActivity";
    public static final String KEY_TYPE_PERSONAL_TAB = "key_type_personal_user_tab";

    private ActivityUserPersonalBinding mBinding;
    private int mTabType;


    public static void startActivity(Context context, String dstTabType) {
        Intent intent = new Intent(context, UserPersonalActivity.class);
        intent.putExtra(KEY_TYPE_PERSONAL_TAB, dstTabType);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_user_personal);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_user_personal);

        User user = UserManager.get().getUser();
        mBinding.setUser(user);
        mBinding.backBtn.setOnClickListener(v -> {
            finish();
        });

        String[] tabs = getResources().getStringArray(R.array.personal_tabs);
        ViewPager2 viewPager = mBinding.viewPager;
        TabLayout tabLayout = mBinding.tabLayout;

        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                Og.d(TAG + ",setAdapter createFragment");
                return PersonalListFragment.newInstance(getTabTypeByPosition(position));
            }

            @Override
            public int getItemCount() {
                return tabs.length;
            }
        });

        // ViewPager2和TabLayout的关联
        // autoRefresh: 当调用ViewPager的adapter#notifyChanged()时要不要主动把tabLayout选项卡移除掉重新复制
        new TabLayoutMediator(tabLayout, viewPager, false, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setText(tabs[position]);
            }
        }).attach();

        mTabType = getInitTabPosition();
        if (mTabType != TAB_ALL.ordinal()) {
            viewPager.post(() -> viewPager.setCurrentItem(mTabType, false));
        }

        mBinding.appbar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                // 是否折叠
                Og.d(TAG + ", appbar, onOffsetChanged(), verticalOffset:" + verticalOffset);
                Og.d(TAG + ", appbar, onOffsetChanged(), getTotalScrollRange:" + appBarLayout.getTotalScrollRange());

                boolean expand = Math.abs(verticalOffset) < appBarLayout.getTotalScrollRange();
                mBinding.setExpand(expand);
            }
        });
    }

    private int getTabTypeByPosition(int position) {
        Og.d(TAG + ", getTabTypeByPosition, position: " + position);
        return PersonalTabType.values()[position].ordinal();
    }

    private int getInitTabPosition() {
        return getIntent().getIntExtra(KEY_TYPE_PERSONAL_TAB, -1);
    }
}