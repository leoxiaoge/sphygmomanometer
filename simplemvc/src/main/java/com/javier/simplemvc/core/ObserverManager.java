package com.javier.simplemvc.core;

import com.javier.simplemvc.patterns.notify.NotifyMessage;
import com.javier.simplemvc.patterns.observer.Observer;
import com.javier.simplemvc.util.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * author:Javier
 * time:2016/5/28.
 * mail:38244704@qq.com
 */
public final class ObserverManager extends SimpleManager {
    private static ObserverManager manager;

    private HashMap<String, ArrayList<Observer>> observerMap;

    public static ObserverManager getInstance() {
        if (manager == null) {
            manager = new ObserverManager();
        }

        return manager;
    }

    public ObserverManager() {
        observerMap = new HashMap<>();
    }

    /**
     * 一个消息可以有多个观察者，因此消息与观察者为一对多的关系
     * @param message       消息
     * @param observer      观察者
     */
    public void registerObserver(String message, Observer observer) {
        if (observerMap.get(message) == null) {
            observerMap.put(message, new ArrayList<Observer>());
        }

        ArrayList<Observer> observers = observerMap.get(message);
        observers.add(observer);
    }

    public synchronized void notifyObserver(NotifyMessage message) {
        ArrayList<Observer> observers = observerMap.get(message.getName());

        if (observers == null) {
            Logger.getLogger().e("message for " + message.getName() + " may not registered!");
            return;
        }

        for (int i = 0; i < observers.size(); i++) {
            Observer observer = observers.get(i);
            observer.notifyObserver(message);
        }
    }

    public void removeObserver(String message) {
        observerMap.remove(message);
    }

    public void testCheckingObserver() {
        Logger.getLogger().d("*****************  CHECKING OBSERVER START *****************");
        Logger.getLogger().d("observerMap size : " + observerMap.size());

        for (Object o : observerMap.entrySet()) {
            Map.Entry entry = (Map.Entry) o;

            Logger.getLogger().d("-- MESSAGE : " + entry.getKey().toString());

            ArrayList<Observer> observers = (ArrayList<Observer>) entry.getValue();

            for (Observer observer : observers) {
                Logger.getLogger().d("---- FOLLOW OBSERVER : " + observer.getClass().getName());
            }
        }
        Logger.getLogger().d("*****************  CHECKING OBSERVER END *****************");
    }

    @Override
    public void destroy() {
        observerMap.clear();
        observerMap = null;

        manager = null;
    }
}