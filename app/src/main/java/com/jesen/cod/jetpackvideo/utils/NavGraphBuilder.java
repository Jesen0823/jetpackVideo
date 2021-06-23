package com.jesen.cod.jetpackvideo.utils;

import android.content.ComponentName;

import androidx.fragment.app.FragmentActivity;
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

public class NavGraphBuilder {

    public static void build(NavController controller, FragmentActivity activity, int containerId) {
        NavigatorProvider provider = controller.getNavigatorProvider();

        // 改用自定义的导航器
        //FragmentNavigator fragmentNavigator = provider.getNavigator(FragmentNavigator.class);
        // convert:
        FixFragmentNavigator fragmentNavigator =
                new FixFragmentNavigator(activity, activity.getSupportFragmentManager(), containerId);
        provider.addNavigator(fragmentNavigator);

        ActivityNavigator activityNavigator = provider.getNavigator(ActivityNavigator.class);

        NavGraph navGraph = new NavGraph(new NavGraphNavigator(provider));

        HashMap<String, Destination> destConfig = AppConfig.getDestConfig();
        for (Destination value : destConfig.values()) {
            if (value.isFragment) {
                FragmentNavigator.Destination destination = fragmentNavigator.createDestination();
                destination.setClassName(value.className);
                destination.setId(value.id);
                destination.addDeepLink(value.pageUrl);

                navGraph.addDestination(destination);
            } else {
                ActivityNavigator.Destination destination = activityNavigator.createDestination();
                destination.setId(value.id);
                destination.addDeepLink(value.pageUrl);
                destination.setComponentName(new ComponentName(
                        JetAppGlobal.getApplication().getPackageName(), value.className));

                navGraph.addDestination(destination);
            }

            if (value.asStarter) {
                navGraph.setStartDestination(value.id);
            }
        }

        controller.setGraph(navGraph);
    }
}
