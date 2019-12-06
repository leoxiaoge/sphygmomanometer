package com.javier.simplemvc.core;

import android.os.AsyncTask;
import android.util.SparseArray;

import com.javier.simplemvc.data.database.SimpleDatabase;
import com.javier.simplemvc.data.http.RequestEntity;
import com.javier.simplemvc.interfaces.IEncrypt;
import com.javier.simplemvc.interfaces.ITaskCallback;
import com.javier.simplemvc.patterns.model.SimpleDatabaseTask;
import com.javier.simplemvc.patterns.model.SimpleTask;
import com.javier.simplemvc.util.Logger;

import java.lang.reflect.Constructor;

/**
 * author:Javier
 * time:2016/5/28.
 * mail:38244704@qq.com
 */
@SuppressWarnings("unused")
public final class TaskManager extends SimpleManager {

    private static TaskManager taskManager;

    private SparseArray<TaskMap> taskSparseArray;

    private SparseArray<SimpleTask> executeTasks;

    class TaskMap {
        Class taskClass;
        ITaskCallback callback;
        IEncrypt encrypt;
    }

    public synchronized static TaskManager getInstance() {
        if (taskManager == null)
            taskManager = new TaskManager();

        return taskManager;
    }

    public TaskManager() {
        taskSparseArray = new SparseArray<>();
        executeTasks = new SparseArray<>();
    }

    /**
     * 注册task
     *
     * @param taskId    task id
     * @param taskClass task class 对象
     */
    public void registerTask(int taskId, Class<?> taskClass, ITaskCallback callback) {
        TaskMap taskMap = new TaskMap();
        taskMap.taskClass = taskClass;
        taskMap.callback = callback;

        taskSparseArray.put(taskId, taskMap);
    }

    public void registerTask(int taskId, Class<?> taskClass, ITaskCallback callback, IEncrypt encrypt) {
        TaskMap taskMap = new TaskMap();
        taskMap.taskClass = taskClass;
        taskMap.callback = callback;
        taskMap.encrypt = encrypt;

        taskSparseArray.put(taskId, taskMap);
    }

    /**
     * 检索task
     *
     * @param taskId task id
     * @return task
     */
    public SimpleTask retrieveTask(int taskId) {
        TaskMap taskMap = taskSparseArray.get(taskId);

        if (taskMap.taskClass == null) {
            Logger.getLogger().e("can not found class by id " + taskId);
            return null;
        }

        try {
            Constructor[] cons = taskMap.taskClass.getConstructors();

            if (taskMap.encrypt == null) {
                return (SimpleTask) cons[0].newInstance(taskMap.callback);
            }

            return (SimpleTask) cons[1].newInstance(taskMap.callback, taskMap.encrypt);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 根据task id来移除任务
     *
     * @param taskId 任务ID
     */
    public void removeTask(int taskId) {
        SimpleTask task = executeTasks.get(taskId);

        if (task != null && !task.isCancelled()) {
            task.cancel(true);
        }

        executeTasks.delete(taskId);
    }

    /**
     * 移除所有任务
     */
    public void removeAllTask() {
        for (int i = 0; i < executeTasks.size(); i++) {
            int key = executeTasks.keyAt(i);

            SimpleTask task = executeTasks.get(key);
            task.cancel(true);
        }

        executeTasks.clear();
    }

    public void insert(int taskId, Object... param) {
        execDatabase(taskId, SimpleDatabase.INSERT, param);
    }

    public void update(int taskId, Object... param) {
        execDatabase(taskId, SimpleDatabase.UPDATE, param);
    }

    public void delete(int taskId, Object... param) {
        execDatabase(taskId, SimpleDatabase.DELETE, param);
    }

    public void query(int taskId, Object... param) {
        execDatabase(taskId, SimpleDatabase.SELECT, param);
    }

    public void post(int taskId, String url, String param) {
        execHttp(taskId, url, "POST", param, false, "");
    }

    public void postWithToken(int taskId, String url, String param, String tokenId) {
        execHttp(taskId, url, "POST", param, false, tokenId);
    }

    public void postEncrypt(int taskId, String url, String param) {
        execHttp(taskId, url, "POST", param, true, "");
    }

    public void postEncryptToken(int taskId, String url, String param, String tokenId) {
        execHttp(taskId, url, "POST", param, true, tokenId);
    }

    public void get(int taskId, String url, String param) {
        execHttp(taskId, url, "POST", param, false, "");
    }

    public void getWithToken(int taskId, String url, String param, String tokenId) {
        execHttp(taskId, url, "POST", param, false, tokenId);
    }

    public void getEncrypt(int taskId, String url, String param) {
        execHttp(taskId, url, "POST", param, true, "");
    }

    public void getEncryptToken(int taskId, String url, String param, String tokenId) {
        execHttp(taskId, url, "POST", param, true, tokenId);
    }

    private void execHttp(int taskId, String url, String method, String param, boolean encrypt, String tokenId) {
        String protocol = "http";

        if (url.startsWith("https")) {
            protocol = "https";
        }

        SimpleTask task = retrieveTask(taskId);

        executeHttpTask(task, url, method, protocol, encrypt, param, tokenId);
    }

    /**
     * 执行task
     *
     * @param task     需要执行的任务
     * @param url      请求的URL
     * @param method   请求方式
     * @param protocol 请求协议
     * @param encrypt  是否加密
     * @param param    请求参数
     * @param tokenId  token id
     */
    private void executeHttpTask(SimpleTask task, String url, String method, String protocol,
                                 boolean encrypt, String param, String tokenId) {
        int encoder_int;

        if (encrypt) {
            encoder_int = 1;
        } else {
            encoder_int = 0;
        }

        RequestEntity requestEntity = new RequestEntity();
        requestEntity.setRequestUrl(url);
        requestEntity.setContent(param);
        requestEntity.setMethod(method);
        requestEntity.setProtocol(protocol);
        requestEntity.setEncrypt(encoder_int);
        requestEntity.setTokenId(tokenId);

        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, requestEntity);

        executeTasks.put(task.getTaskId(), task);
    }

    public void execDatabase(int taskId, int opt, Object... param) {
        SimpleTask task = retrieveTask(taskId);

        if (task instanceof SimpleDatabaseTask) {
            task.execute(opt, param);
        }
    }

    @Override
    public void destroy() {
        removeAllTask();
        taskSparseArray.clear();

        taskSparseArray = null;
        executeTasks = null;

        taskManager = null;
    }
}
