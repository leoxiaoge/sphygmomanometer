package com.javier.simplemvc;

import android.app.Application;
import android.util.SparseArray;

import com.javier.simplemvc.patterns.entity.CommandEntity;
import com.javier.simplemvc.patterns.entity.DaoEntity;

import java.util.ArrayList;

/**
 * author:Javier
 * time:2016/5/28.
 * mail:38244704@qq.com
 */
public abstract class SimpleApplication extends Application {
    protected SimpleContext simpleContext;

    /**
     * 启动SimpleMVC框架
     */
    protected void startUpSimpleMVC() {
        simpleContext = SimpleContext.getSimpleContext();
        simpleContext.init(getApplicationContext());

        initLogger();

        ArrayList<CommandEntity> commandEntities = listCommand();
        if (commandEntities != null && commandEntities.size() > 0) {
            simpleContext.registerCommand(listCommand());
        }
    }

    /**
     * 初始化数据库
     * @param dbName    数据库名称
     * @param version   数据库版本
     */
    protected void initDatabase(String dbName, int version) {
        ArrayList<DaoEntity> daoEntities = listDao();

        if (daoEntities != null && daoEntities.size() > 0) {
            simpleContext.initDatabase(dbName, version, daoEntities);
        }
    }

    /**
     * 初始化日志
     */
    protected abstract void initLogger();

    /**
     * 需要注册的DAO CLASS对象集合
     * @return
     */
    protected abstract ArrayList<DaoEntity> listDao();

    /**
     * 需要注册的command的class数组
     * @return
     */
    protected abstract ArrayList<CommandEntity> listCommand();

    @Override
    public void onTerminate() {
        super.onTerminate();

        simpleContext.destroy();
    }
}