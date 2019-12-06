package com.javier.simplemvc.core;

import com.javier.simplemvc.SimpleContext;
import com.javier.simplemvc.interfaces.IObserverFunction;
import com.javier.simplemvc.interfaces.IView;
import com.javier.simplemvc.patterns.notify.NotifyMessage;
import com.javier.simplemvc.patterns.observer.Observer;
import com.javier.simplemvc.util.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * author:Javier
 * time:2016/5/28.
 * mail:38244704@qq.com
 */
@SuppressWarnings("unused")
public final class ViewManager extends SimpleManager {

    private static ViewManager manager;

    private HashMap<IView, String[]> widgetHashMap;

    public static ViewManager getInstance() {
        if (manager == null) {
            manager = new ViewManager();
        }

        return manager;
    }

    public ViewManager() {
        widgetHashMap = new HashMap<>();
    }

    public void registerView(IView view) {
        String[] messages = view.listMessage();

        if (messages == null || messages.length == 0) {
            return;
        }

        widgetHashMap.put(view, messages);

        for (String message : messages) {
            SimpleContext.getSimpleContext().registerObserver(message, observer);
        }

        view.onRegister();
    }

    public void removeView(IView view) {
        if (widgetHashMap.get(view) == null) {
            Logger.getLogger().w("View " + view + " wat not register.");
            return;
        }

        widgetHashMap.remove(view);

        String[] messages = view.listMessage();

        for (String message : messages) {
            SimpleContext.getSimpleContext().removeObserver(message);
        }

        view.onRemove();

        if (Logger.DEBUG) {
//            checkingReg();
        }
    }

    private Observer observer = new Observer(new IObserverFunction() {
        @Override
        public void ObserverFunction(NotifyMessage message) {

            for (Object o : widgetHashMap.entrySet()) {
                HashMap.Entry entry = (HashMap.Entry) o;

                IView view = (IView) entry.getKey();
                String[] followMessages = (String[]) entry.getValue();

                for (String msg : followMessages) {
                    if (msg.equalsIgnoreCase(message.getName())) {
                        view.handlerMessage(message);
                    }
                }
            }
        }
    });

    private void checkingReg() {
        testCheckingObserver();
        testCheckingView();
    }

    private void testCheckingObserver() {
        ObserverManager.getInstance().testCheckingObserver();
    }

    private void testCheckingView() {
        Logger.getLogger().d("################## CHECKING VIEW START ###################");
        Logger.getLogger().d("register view size : " + widgetHashMap.size());

        for (Object o : widgetHashMap.entrySet()) {
            Map.Entry entry = (Map.Entry) o;

            IView view = (IView) entry.getKey();
            String[] msg = (String[]) entry.getValue();

            Logger.getLogger().d("-- REGISTER VIEW : " + view.getClass().getName());

            for (String bMsg : msg) {
                Logger.getLogger().d("------ FOLLOW MESSAGE : " + bMsg);
            }
        }
        Logger.getLogger().d("################## CHECKING VIEW END ###################");
    }

    @Override
    public void destroy() {
        widgetHashMap.clear();
        widgetHashMap = null;

        manager = null;
    }
}