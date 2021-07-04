package com.jesen.cod.jetpackvideo.ui.login;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.jesen.cod.jetpackvideo.MainActivity;
import com.jesen.cod.jetpackvideo.model.User;
import com.jesen.cod.libcommon.JetAppGlobal;
import com.jesen.cod.libcommon.utils.Og;
import com.jesen.cod.libnetwork.ApiResponse;
import com.jesen.cod.libnetwork.ApiService;
import com.jesen.cod.libnetwork.JsonCallback;
import com.jesen.cod.libnetwork.cache.CacheManager;

public class UserManager {

    private static final String KEY_CACHE_USER = "cache_user";
    private static UserManager mUserManager = new UserManager();
    private MutableLiveData<User> userLiveData;
    private User mUser;

    public static UserManager get() {
        return mUserManager;
    }

    private UserManager(){
        User userCache = (User) CacheManager.readCache(KEY_CACHE_USER);
        if (userCache != null){
            mUser = userCache;
        }
    }

    public void save(User user){
        if (TextUtils.isEmpty(user.description)){
            user.description = "用户描述是空白，可能是用户没有设置";
        }
        mUser = user;
        Og.d("UserManager, save, mUser:"+mUser.userId+", name: "+ mUser.name);

        CacheManager.save(KEY_CACHE_USER, user);
        if (getUserLiveData().hasObservers()){
            getUserLiveData().postValue(user);
        }
    }

    public LiveData<User> login(Context context){
        Intent intent = new Intent(context, LoginActivity.class);
        if (!(context instanceof Activity)){
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
        return getUserLiveData();
    }

    public boolean isLogin() { //解决登录卡死的问题，expires_time时间戳有问题，会一直小于 System.currentTimeMillis();
        return mUser != null ;//? false : mUser.expires_time > System.currentTimeMillis();
    }

    public User getUser() {
        if (mUser !=null && TextUtils.isEmpty(mUser.description)){ // 防止UI空白
            mUser.description = "用户描述是空白，可能是用户没有设置";
        }
        return isLogin() ? mUser : null;
    }

    public long getUserId() {
        return isLogin() ? mUser.userId : 0;
    }

    public LiveData<User> refresh(){
        if (!isLogin()){
            return login(JetAppGlobal.getApplication());
        }
        MutableLiveData<User> liveData = new MutableLiveData<>();
        ApiService.get("/user/query")
                .addParams("userId", getUserId())
                .execute(new JsonCallback<User>() {
                    @Override
                    public void onSuccess(ApiResponse<User> response) {
                        // 更新本地缓存
                        save(response.body);
                        liveData.postValue(getUser());
                    }

                    @SuppressLint("RestrictedApi")
                    @Override
                    public void onError(ApiResponse<User> response) {
                        ArchTaskExecutor.getMainThreadExecutor().execute(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(JetAppGlobal.getApplication(), response.message, Toast.LENGTH_SHORT).show();
                            }
                        });
                        liveData.postValue(null);
                    }
                });
        return liveData;
    }

    /**
     * bugfix:  liveData默认情况下是支持黏性事件的，
     * 即之前已经发送了一条消息，当有新的observer注册进来的时候，也会把先前的消息发送给他，
     * <p>
     * 就造成了{@linkplain MainActivity# onNavigationItemSelected(MenuItem) }死循环
     * <p>
     * 那有两种解决方法
     * 1.我们在退出登录的时候，把livedata置为空，或者将其内的数据置为null
     * 2.利用我们改造的stickyLiveData来发送这个登录成功的事件
     * <p>
     * 我们选择第一种,把livedata置为空
     */
    public void logout() {
        CacheManager.delete(KEY_CACHE_USER, mUser);
        mUser = null;
        userLiveData = null;
    }

    private MutableLiveData<User> getUserLiveData() {
        if (userLiveData == null){
            userLiveData = new MutableLiveData<>();
        }
        return userLiveData;
    }
}
