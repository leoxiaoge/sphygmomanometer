package com.javier.simplemvc.patterns.entity;

/**
 * author:Javier
 * time:2016/5/29.
 * mail:38244704@qq.com
 */
public class DaoEntity {
    private int id;
    private Class daoClass;

    public DaoEntity() {

    }

    public DaoEntity(int id, Class daoClass) {
        this.id = id;
        this.daoClass = daoClass;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Class getDaoClass() {
        return daoClass;
    }

    public void setDaoClass(Class daoClass) {
        this.daoClass = daoClass;
    }
}