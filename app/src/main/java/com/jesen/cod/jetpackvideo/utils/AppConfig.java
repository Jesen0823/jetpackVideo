package com.jesen.cod.jetpackvideo.utils;

import android.content.res.AssetManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.jesen.cod.jetpackvideo.model.BottomBar;
import com.jesen.cod.jetpackvideo.model.Destination;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public class AppConfig {

    private static HashMap<String, Destination> sDestConfig;

    private static BottomBar mBottomBar;


    public static BottomBar getBottomBar(){
        if (mBottomBar == null){
            String content = parseFile("main_tabs_config.json");
            mBottomBar = JSON.parseObject(content, BottomBar.class);
        }
        return  mBottomBar;
    }

    public static HashMap<String, Destination> getDestConfig(){
        if (sDestConfig == null){
            String fileContent = parseFile("destination.json");
            sDestConfig = JSON.parseObject(fileContent,
                    new TypeReference<HashMap<String, Destination>>(){}.getType());
        }
        return  sDestConfig;
    }

    private static String parseFile(String fileName){
        AssetManager assetManager = JetAppGlobal.getApplication().getResources().getAssets();
        InputStream stream = null;
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();
        try {
             stream = assetManager.open(fileName);
             reader = new BufferedReader(new InputStreamReader(stream));
             String line = null;
             while ((line = reader.readLine())!= null){
                 builder.append(line);
             }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if (stream != null) {
                    stream.close();
                }
                if (reader != null) {
                    reader.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
        return builder.toString();
    }
}
