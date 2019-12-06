package com.javier.simplemvc.core;

import android.content.Context;
import android.os.Bundle;

import com.javier.simplemvc.SimpleContext;
import com.javier.simplemvc.patterns.notify.NotifyMessage;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * author:Javier
 * time:2016/5/29.
 * mail:38244704@qq.com
 */
public class NotifyManager extends SimpleManager {

    private static NotifyManager manager;

    public static NotifyManager getInstance() {
        if (manager == null) {
            manager = new NotifyManager();
        }

        return manager;
    }

    public void sendNotifyMessage(NotifyMessage message) {
        SimpleContext.getSimpleContext().notifyObserver(message);
    }

    public void sendNotifyMessage(String message, Object... param) {
        sendNotifyMessage(new NotifyMessage(message, param));
    }

    public void sendNotifyMessage(String message, Object param) {
        sendNotifyMessage(new NotifyMessage(message, param));
    }

    public void sendNotifyMessage(String message, Bundle b) {
        sendNotifyMessage(new NotifyMessage(message, b));
    }

    public void sendNotifyMessage(String message) {
        sendNotifyMessage(new NotifyMessage(message));
    }

    public void sendNotifyMessage(String message , ArrayList list) {
        sendNotifyMessage(new NotifyMessage(message, list));
    }

    public void sendNotifyMessage(String message, HashMap map) {
        sendNotifyMessage(new NotifyMessage(message, map));
    }

    public void sendNotifyMessage(String message, Context context) {
        sendNotifyMessage(new NotifyMessage(message, context));
    }

    @Override
    public void destroy() {

    }
}
