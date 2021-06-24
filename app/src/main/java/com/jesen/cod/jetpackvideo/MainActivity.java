package com.jesen.cod.jetpackvideo;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.jesen.cod.jetpackvideo.model.Destination;
import com.jesen.cod.jetpackvideo.model.User;
import com.jesen.cod.jetpackvideo.ui.login.UserManager;
import com.jesen.cod.jetpackvideo.ui.view.AppBottomBar;
import com.jesen.cod.jetpackvideo.utils.AppConfig;
import com.jesen.cod.jetpackvideo.utils.NavGraphBuilder;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * App 主页 入口
 * <p>
 * 1.底部导航栏 使用AppBottomBar 承载
 * 2.内容区域 使用WindowInsetsNavHostFragment 承载
 * <p>
 * 3.底部导航栏 和 内容区域的 切换联动 使用NavController驱动
 * 4.底部导航栏 按钮个数和 内容区域destination个数。由注解处理器NavProcessor来收集,生成assetsdestination.json。
 */
public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private NavController navController;
    private AppBottomBar navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main);
        navController = NavHostFragment.findNavController(fragment);

        //NavGraphBuilder.build(navController);
        NavGraphBuilder.build(navController, this, fragment.getId());
        navView.setOnNavigationItemSelectedListener(this);
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
        HashMap<String, Destination> destConfig = AppConfig.getDestConfig();
        Iterator<Map.Entry<String, Destination>> iterator = destConfig.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Destination> entry = iterator.next();
            Destination value = entry.getValue();
            if (value != null && !UserManager.get().isLogin() && value.needLogin && value.id
                    == item.getItemId()) {
                UserManager.get().login(this).observe(this, new Observer<User>() {
                    @Override
                    public void onChanged(User user) {
                        // 登录完成后跳回
                        navView.setSelectedItemId(item.getItemId());
                    }
                });
                return false;
            }
        }

        navController.navigate(item.getItemId());
        return !TextUtils.isEmpty(item.getTitle());
    }
}