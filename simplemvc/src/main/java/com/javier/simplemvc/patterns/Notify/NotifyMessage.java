package com.javier.simplemvc.patterns.notify;

import android.content.Context;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * author:Javier
 * time:2016/5/1.
 * mail:38244704@qq.com
 */
public class NotifyMessage {
    private String name;
    private Object[] params;
    private Object param;
    private Bundle bundle;
    private Context context;
    private ArrayList list;
    private HashMap map;

    public NotifyMessage() {

    }

    public NotifyMessage(String name) {
        this.name = name;
    }

    public NotifyMessage(String name, Object... param) {
        this(name);
        this.params = param;
    }

    public NotifyMessage(String name, Object object) {
        this(name);
        this.param = object;
    }

    public NotifyMessage(String name, Bundle bundle) {
        this(name);
        this.bundle = bundle;
    }

    public NotifyMessage(String name, Context context) {
        this(name);
        this.context = context;
    }

    public NotifyMessage(String name, ArrayList list) {
        this(name);
        this.list = list;
    }

    public NotifyMessage(String name, HashMap map) {
        this(name);
        this.map = map;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getParam() {
        return param;
    }

    public void setParam(Object param) {
        this.param = param;
    }

    public Object[] getParams() {
        return params;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

    public Bundle getBundle() {
        return bundle;
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public ArrayList getList() {
        return list;
    }

    public void setList(ArrayList list) {
        this.list = list;
    }

    public HashMap getMap() {
        return map;
    }

    public void setMap(HashMap map) {
        this.map = map;
    }
}