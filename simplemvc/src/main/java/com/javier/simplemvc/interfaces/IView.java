package com.javier.simplemvc.interfaces;

/**
 * author:Javier
 * time:2016/5/28.
 * mail:38244704@qq.com
 */
public interface IView extends IModule {
    void onInitView();
    void onSetEventListener();
    void onInitComplete();
}