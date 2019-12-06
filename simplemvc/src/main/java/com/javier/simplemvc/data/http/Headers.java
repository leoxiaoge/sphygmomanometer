package com.javier.simplemvc.data.http;

import java.util.HashMap;

/**
 * author:Javier
 * time:2016/4/28.
 * mail:38244704@qq.com
 * <p>
 * 自定义的Http头
 */
public class Headers {
    private boolean isUserCache;
    private String contentType;
    private HashMap<String, String> customHeader;

    public boolean isUserCache() {
        return isUserCache;
    }

    public void setUserCache(boolean userCache) {
        isUserCache = userCache;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public HashMap<String, String> getCustomHeader() {
        return customHeader;
    }

    public void setCustomHeader(String... customHeaders) {
        int size = customHeaders.length;

        if (size == 0) {
            return;
        }

        String key = "";
        String value = "";
        customHeader = new HashMap<>();

        for (int i = 0; i < size; i++) {
            if (i % 2 == 0) {
                value = customHeaders[i];
                customHeader.put(key, value);
            } else {
                key = customHeaders[i];
            }
        }
    }
}