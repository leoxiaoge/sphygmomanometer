package com.javier.simplemvc.patterns.model;

import com.javier.simplemvc.data.database.SimpleDatabase;
import com.javier.simplemvc.data.http.ErrorEntity;
import com.javier.simplemvc.interfaces.IDao;
import com.javier.simplemvc.interfaces.ITaskCallback;

/**
 * author:Javier
 * time:2016/4/30.
 * mail:38244704@qq.com
 */
@SuppressWarnings("unused")
public abstract class SimpleDatabaseTask<T> extends SimpleTask {

    protected SimpleDatabaseTask(ITaskCallback callback) {
        super(callback);
    }

    public IDao getDao(int daoId) {
        return simpleContext.getDao(daoId);
    }

    @Override
    protected T doInBackground(Object[] params) {

        int opt = Integer.parseInt(params[0].toString());
        Object[] tmpParams = (Object[]) params[1];

        T t = null;

        switch (opt) {
            case SimpleDatabase.INSERT:
                logger.i("execute insert.");
                t = insert(tmpParams);
                break;
            case SimpleDatabase.UPDATE:
                logger.i("execute update.");
                t = update(tmpParams);
                break;
            case SimpleDatabase.DELETE:
                logger.i("execute delete.");
                t = delete(tmpParams);
                break;
            case SimpleDatabase.SELECT:
                logger.i("execute select.");
                t = query(tmpParams);
                break;
        }

        return t;
    }

    protected T insert(Object[] params) {
        return null;
    }

    protected T update(Object[] params) {
        return null;
    }

    protected T delete(Object[] params) {
        return null;
    }

    protected T query(Object[] params) {
        return null;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);

        try {
            onExecuteFinish((T) o);
        } catch (Exception e) {
            logger.e(e.getMessage());

            if (null != callback) {
                onFailed(5000, "应用程序内部错误");
            }
        }
    }

    protected abstract void onExecuteFinish(T t);

    protected void onResult(int code, Object param) {
        if (null != callback) {
            callback.onResult(code, param, this);
        }
    }

    protected void onFailed(int code, String errorMessage) {
        if (null != callback) {
            ErrorEntity errorEntity = new ErrorEntity();
            errorEntity.setCode(code);
            errorEntity.setMessage(errorMessage);

            callback.onFailed(code, errorEntity, this);
        }
    }
}