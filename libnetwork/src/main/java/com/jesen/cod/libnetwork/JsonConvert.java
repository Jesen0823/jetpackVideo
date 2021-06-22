package com.jesen.cod.libnetwork;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.lang.reflect.Type;

public class JsonConvert implements Convert {
    @Override
    public Object convert(String response, Type type) {
        JSONObject jsonObject = JSON.parseObject(response);
        JSONObject data = jsonObject.getJSONObject("data");// 返回体最外层"data"
        if (data != null){
            Object object = data.get("data");
            return JSON.parseObject(object.toString(), type);
        }
        return null;
    }

    @Override
    public Object convert(String response, Class clazz) {
        JSONObject jsonObject = JSON.parseObject(response);
        JSONObject data = jsonObject.getJSONObject("data");// 返回体最外层"data"
        if (data != null){
            Object object = data.get("data");
            return JSON.parseObject(object.toString(), clazz);
        }
        return null;
    }
}
