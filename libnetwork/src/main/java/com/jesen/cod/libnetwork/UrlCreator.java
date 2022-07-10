package com.jesen.cod.libnetwork;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

public class UrlCreator {

    public static String createUrlFromParams(String url, Map<String, Object> params) {
        StringBuilder builder = new StringBuilder();
        builder.append(url);
        if (url.indexOf("?") > 0 || url.indexOf("&") > 0) {
            builder.append("&");
        } else {
            builder.append("?");
        }
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            try {
                String value = URLEncoder.encode(String.valueOf(entry.getValue()), "UTF-8");
                builder.append(entry.getKey()).append("=").append(value).append("&");

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        // 删除最后一个&
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }
}
