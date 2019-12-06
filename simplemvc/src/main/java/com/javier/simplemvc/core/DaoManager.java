package com.javier.simplemvc.core;

import android.content.Context;

import com.javier.simplemvc.data.database.SimpleDatabase;
import com.javier.simplemvc.interfaces.IDao;
import com.javier.simplemvc.patterns.entity.DaoEntity;
import com.javier.simplemvc.util.Logger;

import java.lang.reflect.Constructor;
import java.util.ArrayList;

/**
 * author:Javier
 * time:2016/5/28.
 * mail:38244704@qq.com
 */
@SuppressWarnings("unused")
public final class DaoManager extends SimpleManager {
    private static DaoManager manager;

    private ArrayList<DaoEntity> registerDaoEntities ;

    public static DaoManager getInstance() {
        if (manager == null) {
            manager = new DaoManager();
        }

        return manager;
    }

    public DaoManager() {
        registerDaoEntities = new ArrayList<>();
    }

    public void registerDao(ArrayList<DaoEntity> daoEntities) {
        for (int i = 0; i < daoEntities.size(); i++) {
            DaoEntity daoEntity = daoEntities.get(i);

            Class clazz = daoEntity.getDaoClass();

            if (IDao.class.isAssignableFrom(clazz)) {
                registerDaoEntities.add(daoEntity);
            } else {
                Logger.getLogger().w("class " + clazz.getName() + " was not implements IDao interface.");
            }
        }
    }

    public IDao getDao(Context context, int daoId) {
        if (registerDaoEntities.size() == 0) {
            return null;
        }

        for (DaoEntity daoEntity : registerDaoEntities) {
            if (daoEntity.getId() == daoId) {
                Class clazz = daoEntity.getDaoClass();

                try {
                    Constructor<IDao>[] cons = clazz.getConstructors();
                    return cons[0].newInstance(context, SimpleDatabase.getSimpleDatabase().open());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    @Override
    public void destroy() {
        registerDaoEntities.clear();
        registerDaoEntities = null;

        manager = null;
    }
}