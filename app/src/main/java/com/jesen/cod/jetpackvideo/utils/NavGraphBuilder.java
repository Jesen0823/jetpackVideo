package com.jesen.cod.jetpackvideo.utils;

import android.content.ComponentName;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.ActivityNavigator;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.NavGraphNavigator;
import androidx.navigation.NavigatorProvider;
import androidx.navigation.fragment.FragmentNavigator;

import com.jesen.cod.jetpackvideo.FixFragmentNavigator;
import com.jesen.cod.jetpackvideo.model.Destination;
import com.jesen.cod.libcommon.JetAppGlobal;
import com.jesen.cod.libnavannotation.FragmentDestination;

import java.util.HashMap;
import java.util.Iterator;

public class NavGraphBuilder {

    public static void build(NavController controller, FragmentManager childFragmentManager, FragmentActivity activity, int containerId) {
        NavigatorProvider provider = controller.getNavigatorProvider();

        //NavGraphNavigator也是页面路由导航器的一种，只不过他比较特殊。
        //它只为默认的展示页提供导航服务,但真正的跳转还是交给对应的navigator来完成的
        NavGraph navGraph = new NavGraph(new NavGraphNavigator(provider));

        // 改用自定义的导航器
        //FragmentNavigator fragmentNavigator = provider.getNavigator(FragmentNavigator.class);
        // convert:
        FixFragmentNavigator fragmentNavigator =
                new FixFragmentNavigator(activity, childFragmentManager, containerId);
        provider.addNavigator(fragmentNavigator);

        ActivityNavigator activityNavigator = provider.getNavigator(ActivityNavigator.class);

        HashMap<String, Destination> destConfig = AppConfig.getDestConfig();
        Iterator<Destination> iterator = destConfig.values().iterator();
        while (iterator.hasNext()){
            Destination node = iterator.next();
            if (node.isFragment) {
                FragmentNavigator.Destination destination = fragmentNavigator.createDestination();
                destination.setClassName(node.className);
                destination.setId(node.id);
                destination.addDeepLink(node.pageUrl);

                navGraph.addDestination(destination);
            } else {
                ActivityNavigator.Destination destination = activityNavigator.createDestination();
                destination.setId(node.id);
                destination.addDeepLink(node.pageUrl);
                destination.setComponentName(new ComponentName(
                        JetAppGlobal.getApplication().getPackageName(), node.className));

                navGraph.addDestination(destination);
            }

            //给APP页面导航结果图 设置一个默认的展示页的id
            if (node.asStarter) {
                navGraph.setStartDestination(node.id);
            }
        }

        controller.setGraph(navGraph);
    }
}
