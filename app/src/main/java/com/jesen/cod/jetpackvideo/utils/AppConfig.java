package com.jesen.cod.jetpackvideo.utils;

import android.content.res.AssetManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.jesen.cod.jetpackvideo.model.BottomBar;
import com.jesen.cod.jetpackvideo.model.Destination;
import com.jesen.cod.jetpackvideo.model.SofaTab;
import com.jesen.cod.libcommon.JetAppGlobal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class AppConfig {

    private static HashMap<String, Destination> sDestConfig;

    private static BottomBar mBottomBar;

    private static SofaTab sSofaTab, mFindTabConfig;


    public static BottomBar getBottomBar() {
        if (mBottomBar == null) {
            String content = parseFile("main_tabs_config.json");
            mBottomBar = JSON.parseObject(content, BottomBar.class);
        }
        return mBottomBar;
    }

    public static HashMap<String, Destination> getDestConfig() {
        if (sDestConfig == null) {
            String fileContent = parseFile("destination.json");
            sDestConfig = JSON.parseObject(fileContent,
                    new TypeReference<HashMap<String, Destination>>() {
                    }.getType());
        }
        return sDestConfig;
    }

    private static String parseFile(String fileName) {
        AssetManager assetManager = JetAppGlobal.getApplication().getResources().getAssets();
        InputStream stream = null;
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();
        try {
            stream = assetManager.open(fileName);
            reader = new BufferedReader(new InputStreamReader(stream));
            String line = null;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return builder.toString();
    }

    public static SofaTab getSofaTabConfig() {
        if (sSofaTab == null) {
            String content = parseFile("sofa_tabs_config.json");
            sSofaTab = JSON.parseObject(content, SofaTab.class);
            Collections.sort(sSofaTab.tabs, new Comparator<SofaTab.Tabs>() {
                @Override
                public int compare(SofaTab.Tabs tabs, SofaTab.Tabs tabs2) {
                    return tabs.index < tabs2.index ? -1 : 1;
                }
            });
        }
        return sSofaTab;
    }

    public static SofaTab getFindTabConfig() {
        if (mFindTabConfig == null) {
            String content = parseFile("find_tabs_config.json");
            mFindTabConfig = JSON.parseObject(content, SofaTab.class);
            Collections.sort(mFindTabConfig.tabs, new Comparator<SofaTab.Tabs>() {
                @Override
                public int compare(SofaTab.Tabs tab1, SofaTab.Tabs tab2) {
                    return tab1.index < tab2.index ? -1 : 1;
                }
            });
        }
        return mFindTabConfig;
    }
}
