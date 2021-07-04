package com.jesen.cod.jetpackvideo.ui.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jesen.cod.jetpackvideo.AbsViewModel;
import com.jesen.cod.jetpackvideo.R;
import com.jesen.cod.jetpackvideo.databinding.LayoutRefreshViewBinding;
import com.jesen.cod.libcommon.utils.Og;
import com.jesen.cod.libcommon.view.EmptyView;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.constant.RefreshState;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class AbsListFragment<T, M extends AbsViewModel<T>> extends Fragment implements
        OnRefreshListener, OnLoadMoreListener {

    private LayoutRefreshViewBinding binding;
    protected RecyclerView mRecyclerView;
    protected SmartRefreshLayout mRefreshLayout;
    protected EmptyView mEmptyView;
    protected PagedListAdapter<T, RecyclerView.ViewHolder> mAdapter;
    protected M mViewModel;

    @Nullable
    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        binding = LayoutRefreshViewBinding.inflate(inflater, container, false);
        mRecyclerView = binding.recyclerView;
        mRefreshLayout = binding.refreshLayout;
        mEmptyView = binding.emptyView;

        mRefreshLayout.setEnableRefresh(true);
        mRefreshLayout.setEnableLoadMore(true);
        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setOnLoadMoreListener(this);

        mAdapter = getAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),
                LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setItemAnimator(null);
        DividerItemDecoration decoration = new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL);
        decoration.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.list_divider));
        mRecyclerView.addItemDecoration(decoration);

        genericViewModel();

        return binding.getRoot();
    }

    private void genericViewModel() {
        //利用 子类传递的 泛型参数实例化出absViewModel 对象。
        ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
        Og.d("AbsListFragment, onViewCreated");
        Type[] arguments = type.getActualTypeArguments();
        if (arguments.length > 1) {
            Type argument = arguments[1];
            Og.d("argument[1] = " + argument);
            Class subclass = ((Class) argument).asSubclass(AbsViewModel.class);
            Og.d("subclass = " + subclass.getName());

            // mViewModel = (M)ViewModelProviders.of(this).get(subclass);
            mViewModel = (M) new ViewModelProvider(this).get(subclass);
            // 触发页面初始化数据加载的逻辑
            mViewModel.getPagedListLiveData().observe(getViewLifecycleOwner(), new Observer<PagedList<T>>() {
                @Override
                public void onChanged(PagedList<T> pagedList) {
                    //  Set the new list to be displayed.
                    submitList(pagedList);
                }
            });
            // 监听分页时有无更多数据,以决定是否关闭上拉加载的动画
            mViewModel.getBoundaryPageData().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean hasData) {
                    finishRefresh(hasData);
                }
            });
        }
    }

    /**
     * 因而 我们在 onCreateView的时候 创建了 PagedListAdapter
     * 所以，如果arguments 有参数需要传递到Adapter 中，那么需要在getAdapter()方法中取出参数。
     *
     * @return
     */
    public abstract PagedListAdapter getAdapter();

    /*
     *  将下拉刷新的数据传给adapter
     *  只有当新数据集合大于0 的时候，才调用adapter.submitList
        否则可能会出现 页面----有数据----->被清空-----空布局
     * */
    public void submitList(PagedList<T> pagedList) {
        if (pagedList.size() > 0) {
            mAdapter.submitList(pagedList);
        }
        finishRefresh(pagedList.size() > 0);
    }


    public void finishRefresh(boolean hasData) {
        PagedList<T> currentList = mAdapter.getCurrentList();
        hasData = hasData || currentList != null && currentList.size() > 0;
        RefreshState state = mRefreshLayout.getState();
        if (state.isFooter && state.isOpening) {
            mRefreshLayout.finishLoadMore();
        } else if (state.isHeader && state.isOpening) {
            mRefreshLayout.finishRefresh();
        }

        if (hasData) {
            mEmptyView.setVisibility(View.GONE);
        } else {
            mEmptyView.setVisibility(View.VISIBLE);
        }
    }
}
