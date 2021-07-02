package com.jesen.cod.jetpackvideo.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.fragment.NavHostFragment;


import com.jesen.cod.jetpackvideo.ui.view.WindowInsertsFrameLayout;

import org.jetbrains.annotations.NotNull;

public class WindowInsertNavHostFragment extends NavHostFragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        WindowInsertsFrameLayout layout = new WindowInsertsFrameLayout(inflater.getContext());
        layout.setId(getId());
        return layout;
    }
}
