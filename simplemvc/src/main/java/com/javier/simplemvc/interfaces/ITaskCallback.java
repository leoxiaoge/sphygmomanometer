package com.javier.simplemvc.interfaces;


import com.javier.simplemvc.data.http.ErrorEntity;
import com.javier.simplemvc.patterns.model.SimpleTask;

/**
 * author:Javier
 * time:2016/4/30.
 * mail:38244704@qq.com
 */
@SuppressWarnings("unused")
public interface ITaskCallback<T> {
    /**
     * 正确的时候调用
     *
     * @param code   标示
     * @param result 返回内容
     * @param target 调用者
     */
    void onResult(int code, T result, SimpleTask target);

    /**
     * 正确的时候调用
     *
     * @param code   标示
     * @param error  描述错误的实体对象
     * @param target 调用者
     */
    void onFailed(int code, ErrorEntity error, SimpleTask target);
}
