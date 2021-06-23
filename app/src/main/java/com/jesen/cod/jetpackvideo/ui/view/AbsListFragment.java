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
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jesen.cod.jetpackvideo.AbsViewModel;
import com.jesen.cod.jetpackvideo.R;
import com.jesen.cod.jetpackvideo.databinding.LayoutRefreshViewBinding;
import com.jesen.cod.jetpackvideo.ui.home.HomeViewModel;
import com.jesen.cod.jetpackvideo.utils.Og;
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
    private RecyclerView mRecyclerView;
    private SmartRefreshLayout mRefreshLayout;
    private EmptyView mEmptyView;
    private PagedListAdapter<T, RecyclerView.ViewHolder> mAdapter;
    private M mViewModel;

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
                LinearLayoutManager.VERTICAL,false));
        mRecyclerView.setItemAnimator(null);
        DividerItemDecoration decoration = new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL);
        decoration.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.list_divider));
        mRecyclerView.addItemDecoration(decoration);

        afterCreateView();

        return binding.getRoot();
    }

    protected abstract void afterCreateView();

    public abstract PagedListAdapter<T, RecyclerView.ViewHolder> getAdapter();

    /*
    *  将下拉刷新的数据传给adapter
    * */
    public void submitList(PagedList<T> pagedList){
        if (pagedList.size() > 0){
            mAdapter.submitList(pagedList);
        }
        finishRefresh(pagedList.size() >0);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
        Type[] arguments = type.getActualTypeArguments();
        if (arguments.length > 1){
            Type argument =  arguments[1];
            Og.d("argument[1] = "+ argument);
            Class subclass = ((Class) argument).asSubclass(AbsViewModel.class);
            Og.d("subclass = "+ subclass.getName());

            //mViewModel = (M)ViewModelProviders.of(this).get(subclass);
            mViewModel = (M) new ViewModelProvider(this).get(subclass);
            mViewModel.getPagedListLiveData().observe(getViewLifecycleOwner(), new Observer<PagedList<T>>() {
                @Override
                public void onChanged(PagedList<T> pagedList) {
                    //  Set the new list to be displayed.
                    mAdapter.submitList(pagedList);
                }
            });
            mViewModel.getBoundaryPageData().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean hasData) {
                    finishRefresh(hasData);
                }
            });
        }
    }

    public void finishRefresh(boolean hasData){
        PagedList<T> currentList = mAdapter.getCurrentList();
        hasData = hasData ||currentList != null && currentList.size() > 0;
        RefreshState state = mRefreshLayout.getState();
        if (state.isFooter && state.isOpening){
            mRefreshLayout.finishRefresh();
        }else if (state.isHeader && state.isOpening){
            mRefreshLayout.finishRefresh();
        }

        if (hasData){
            mEmptyView.setVisibility(View.GONE);
        }else {
            mEmptyView.setVisibility(View.VISIBLE);
        }
    }
}
