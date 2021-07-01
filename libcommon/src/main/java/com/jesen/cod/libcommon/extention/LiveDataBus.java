package com.jesen.cod.libcommon.extention;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;

public class LiveDataBus {


    private static class Lazy {
        static LiveDataBus sLiveDataBus = new LiveDataBus();
    }

    public static LiveDataBus getInstance() {
        return Lazy.sLiveDataBus;
    }

    private ConcurrentHashMap<String, StickyLiveData> mHashMap = new ConcurrentHashMap<>();

    public StickyLiveData with(String eventName){
        StickyLiveData liveData = mHashMap.get(eventName);
        if (liveData == null){
            liveData = new StickyLiveData(eventName);
            mHashMap.put(eventName, liveData);
        }
        return liveData;
    }

    public class StickyLiveData<T> extends LiveData<T> {
        private String mEventName;

        private T mStickyData;

        // 记录发送事件的次数
        private int mVersion = 0;

        public StickyLiveData(String eventName) {
            mEventName = eventName;
        }

        @Override
        protected void setValue(T value) {
            mVersion++;
            super.setValue(value);
        }

        @Override
        public void postValue(T value) {
            mVersion++;
            super.postValue(value);
        }

        // 发送同步事件
        public void setStickyData(T stickyData) {
            this.mStickyData = stickyData;
            setValue(stickyData);
        }

        // 发送异步事件
        public void postStickyData(T stickyData) {
            this.mStickyData = stickyData;
            postValue(stickyData);
        }

        @Override
        public void observe(@NonNull @NotNull LifecycleOwner owner, @NonNull @NotNull Observer<? super T> observer) {
            observerSticky(owner, observer, false);
        }

        public void observerSticky(LifecycleOwner owner, Observer<? super T> observer, boolean isSticky) {
            super.observe(owner, new WrapperObserver(this, observer, isSticky));
            owner.getLifecycle().addObserver(new LifecycleEventObserver() {
                @Override
                public void onStateChanged(@NonNull @NotNull LifecycleOwner source, @NonNull @NotNull Lifecycle.Event event) {
                    if (event == Lifecycle.Event.ON_DESTROY){
                        mHashMap.remove(mEventName);
                    }
                }
            });
        }

        private class WrapperObserver<T> implements Observer<T> {

            private StickyLiveData<T> mLiveData;
            private Observer<T> mObserver;
            private boolean mIsSticky;

            private int mLastVersion = 0;

            public WrapperObserver(StickyLiveData liveData, Observer<T> observer, boolean isSticky) {

                mLiveData = liveData;
                mObserver = observer;
                mIsSticky = isSticky;
                mLastVersion = mLiveData.mVersion;
            }

            @Override
            public void onChanged(T t) {
                // 接受次数小于发送次数
                if (mLastVersion >= mLiveData.mVersion) {

                    if (mIsSticky && mLiveData.mStickyData != null) {
                        mObserver.onChanged(mLiveData.mStickyData);
                    }
                    return;
                }

                mLastVersion = mLiveData.mVersion;
                mObserver.onChanged(t);
            }
        }
    }
}
