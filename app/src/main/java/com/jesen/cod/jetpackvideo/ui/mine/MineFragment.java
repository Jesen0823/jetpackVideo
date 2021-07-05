package com.jesen.cod.jetpackvideo.ui.mine;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jesen.cod.jetpackvideo.R;
import com.jesen.cod.jetpackvideo.databinding.FragmentMineBinding;
import com.jesen.cod.jetpackvideo.model.PersonalTabType;
import com.jesen.cod.jetpackvideo.model.User;
import com.jesen.cod.jetpackvideo.ui.login.UserManager;
import com.jesen.cod.libcommon.utils.StatusBarUtil;
import com.jesen.cod.libnavannotation.FragmentDestination;

import org.jetbrains.annotations.NotNull;

/**
 * 我的个人中心页面
 */
@FragmentDestination(pageUrl = "main/tabs/mine", needLogin = true)
public class MineFragment extends Fragment {

    FragmentMineBinding mBinding;
    private AlertDialog logoutDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentMineBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        User user = UserManager.get().getUser();
        mBinding.setUser(user);

        UserManager.get().refresh().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(User user) {
                mBinding.setUser(user);
            }
        });

        mBinding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoutDialog = new AlertDialog.Builder(getContext())
                        .setMessage(getString(R.string.fragment_my_logout))
                        .setPositiveButton(getString(R.string.fragment_my_logout_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                logoutDialog.dismiss();
                                UserManager.get().logout();
                                getActivity().onBackPressed();
                            }
                        })
                        .setNegativeButton(getString(R.string.fragment_my_logout_cancel), null)
                        .create();
                logoutDialog.show();
            }
        });

        mBinding.avatarUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserPersonalActivity.startActivity(getContext(), PersonalTabType.TAB_ALL.getName());
            }
        });

        mBinding.goDetail.setOnClickListener(v -> {
            UserPersonalActivity.startActivity(getContext(), PersonalTabType.TAB_ALL.getName());
        });
        mBinding.userFeed.setOnClickListener(v -> {
            UserPersonalActivity.startActivity(getContext(), PersonalTabType.TAB_ALL.getName());
        });
        mBinding.userComment.setOnClickListener(v -> {
            UserPersonalActivity.startActivity(getContext(), PersonalTabType.TAB_COMMENT.getName());
        });
        mBinding.userFavorite.setOnClickListener(v -> {
            UserFavoriteHistoryActivity.startActivity(getContext(), UserFavoriteHistoryActivity.BEHAVIOR_FAVORITE);
        });
        mBinding.userHistory.setOnClickListener(v -> {
            UserFavoriteHistoryActivity.startActivity(getContext(), UserFavoriteHistoryActivity.BEHAVIOR_HISTORY);
        });

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        StatusBarUtil.lightStatusBar(getActivity(), false);
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onHiddenChanged(boolean hidden) {
        // 改变状态栏颜色,Fragment第一次创建不会走onHiddenChanged，需要在onCreate处理
        super.onHiddenChanged(hidden);
        StatusBarUtil.lightStatusBar(getActivity(), hidden);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (logoutDialog != null && logoutDialog.isShowing()) {
            logoutDialog.dismiss();
            logoutDialog = null;
        }
    }
}