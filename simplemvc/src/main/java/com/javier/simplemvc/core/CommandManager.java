package com.javier.simplemvc.core;

import com.javier.simplemvc.SimpleContext;
import com.javier.simplemvc.interfaces.ICommand;
import com.javier.simplemvc.interfaces.IObserverFunction;
import com.javier.simplemvc.patterns.entity.CommandEntity;
import com.javier.simplemvc.patterns.notify.NotifyMessage;
import com.javier.simplemvc.patterns.observer.Observer;
import com.javier.simplemvc.util.Logger;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * author:Javier
 * time:2016/5/28.
 * mail:38244704@qq.com
 */
@SuppressWarnings("unused, unchecked")
public final class CommandManager extends SimpleManager {
    private static CommandManager manager;

    // 注册的command
    private HashMap<CommandEntity, String[]> commandHashMap;

    // 已创建的command
    private HashMap<String, ICommand> commandHolder;

    public static CommandManager getInstance() {
        if (manager == null) {
            manager = new CommandManager();
        }

        return manager;
    }

    public CommandManager() {
        commandHashMap = new HashMap<>();
        commandHolder = new HashMap<>();
    }

    public void registerCommand(ArrayList<CommandEntity> commands) {
        for (CommandEntity commandEntity : commands) {
            Class clazz = commandEntity.getCommandClass();

            boolean flag = ICommand.class.isAssignableFrom(clazz);

            if (!flag) {
                Logger.getLogger().w("class " + clazz.getName() + " was not implements ICommand interface");
                break;
            }

            try {
                ICommand iCommand = (ICommand) clazz.newInstance();
                String[] followMessage = iCommand.listMessage();

                commandHashMap.put(commandEntity, followMessage);

                for (String aFollowMessage : followMessage) {
                    // 注册观察者
                    SimpleContext.getSimpleContext().registerObserver(aFollowMessage, observer);
                }

                iCommand.onRegister();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void removeCommand(Class commandClass) {
        if (commandHashMap.size() == 0) {
            Logger.getLogger().w("command " + commandClass.getName() + " was not register.");
            return;
        }

        String[] s = commandHashMap.get(commandClass);

        Class removedClass = null;

        if (s != null) {
            removedClass = commandClass;
            commandHashMap.remove(commandClass);
        } else {
            for (Object o : commandHashMap.entrySet()) {
                HashMap.Entry entry = (HashMap.Entry) o;

                CommandEntity commandEntity = (CommandEntity) entry.getKey();
                Class clazz = commandEntity.getCommandClass();

                if (clazz.getName().equals(commandClass.getName())) {
                    removedClass = clazz;
                    commandHashMap.remove(commandEntity);
                    break;
                }
            }
        }

        if (removedClass == null) {
            return;
        }

        try {
            ICommand command = (ICommand) removedClass.newInstance();
            command.onRemove();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Observer observer = new Observer(new IObserverFunction() {
        @Override
        public void ObserverFunction(NotifyMessage message) {
            for (Object o : commandHashMap.entrySet()) {
                HashMap.Entry entry = (HashMap.Entry) o;

                CommandEntity commandEntity = (CommandEntity) entry.getKey();

                Class clazz = commandEntity.getCommandClass();
                String[] followMessages = (String[]) entry.getValue();

                for (String msg : followMessages) {
                    if (msg.equalsIgnoreCase(message.getName())) {

                        if (commandEntity.isHolder()) {
                            if (commandHolder.size() > 0 && commandHolder.get(clazz.getName()) != null) {
                                ICommand holderCommand = commandHolder.get(clazz.getName());
                                holderCommand.handlerMessage(message);
                                break;
                            }
                        }

                        try {
                            ICommand command = (ICommand) clazz.newInstance();
                            command.handlerMessage(message);

                            if (commandEntity.isHolder()) {
                                commandHolder.put(clazz.getName(), command);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    });

    @Override
    public void destroy() {
        commandHashMap.clear();
        commandHolder.clear();

        commandHashMap = null;
        commandHolder = null;

        manager = null;
    }
}