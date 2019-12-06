package com.javier.simplemvc.data.http;

/**
 * author:Javier
 * time:2016/4/30.
 * mail:38244704@qq.com
 */
public class ResponseEntity {
    // 认证字段
    private String tokenId;
    // 响应内容
    private String content;
    // 响应码
    private int responseCode;
    // 预留字段
    Object tag;

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public Object getTag() {
        return tag;
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }
}
