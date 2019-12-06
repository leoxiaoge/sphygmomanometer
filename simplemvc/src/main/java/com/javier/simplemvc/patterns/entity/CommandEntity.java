package com.javier.simplemvc.patterns.entity;

/**
 * author:Javier
 * time:2016/5/29.
 * mail:38244704@qq.com
 */
public class CommandEntity {
    private Class commandClass;
    private boolean isHolder;

    public CommandEntity() {

    }

    public CommandEntity(Class commandClass, boolean isHolder) {
        this.commandClass = commandClass;
        this.isHolder = isHolder;
    }

    public Class getCommandClass() {
        return commandClass;
    }

    public void setCommandClass(Class commandClass) {
        this.commandClass = commandClass;
    }

    public boolean isHolder() {
        return isHolder;
    }

    public void setHolder(boolean holder) {
        isHolder = holder;
    }
}
