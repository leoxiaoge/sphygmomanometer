package com.javier.simplemvc.patterns.observer;

import com.javier.simplemvc.interfaces.IObserverFunction;
import com.javier.simplemvc.patterns.notify.NotifyMessage;

/**
 * author:Javier
 * time:2016/4/30.
 * mail:38244704@qq.com
 *
 * 一个消息会被多个观察者所关注
 */
public final class Observer {

    private IObserverFunction messageFunction;

    public Observer(IObserverFunction messageFunction) {
        setMessageFunction(messageFunction);
    }

    public void notifyObserver(NotifyMessage message) {
        getMessageFunction().ObserverFunction(message);
    }

    public IObserverFunction getMessageFunction() {
        return messageFunction;
    }

    public void setMessageFunction(IObserverFunction messageFunction) {
        this.messageFunction = messageFunction;
    }
}