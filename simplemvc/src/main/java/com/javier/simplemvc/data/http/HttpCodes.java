package com.javier.simplemvc.data.http;

import android.util.SparseArray;

/**
 * author:Javier
 * time:2016/5/24.
 * mail:38244704@qq.com
 */
public class HttpCodes {
    public static SparseArray<String> HTTP_CODE = new SparseArray<>();
    static {
        HTTP_CODE.put(400, "错误的请求");
        HTTP_CODE.put(401, "认证失败");
        HTTP_CODE.put(403, "服务器拒绝");
        HTTP_CODE.put(404, "接口未找到");
        HTTP_CODE.put(411, "服务器拒绝，HTTP头中需指定content-length");
        HTTP_CODE.put(413, "请求数据长度超过服务器接受最大限制");
        HTTP_CODE.put(415, "服务器不支持的资源格式");
        HTTP_CODE.put(408, "连接服务器超时");
        HTTP_CODE.put(500, "服务器内部错误");
    }
}