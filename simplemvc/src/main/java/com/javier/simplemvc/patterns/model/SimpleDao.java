package com.javier.simplemvc.patterns.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.javier.simplemvc.interfaces.IDao;
import com.javier.simplemvc.util.Logger;

/**
 * Created by javier on 2016/4/5.
 * <p/>
 * 抽象类，为所有 DAO 的父类，实现了 IDao 接口，定义了 Dao 的构造方法
 */
@SuppressWarnings("unused")
public abstract class SimpleDao implements IDao {
    protected Context context;
    protected SQLiteDatabase database;
    protected ContentValues contentValues;
    protected Logger logger = Logger.getLogger();

    protected SimpleDao(Context context, SQLiteDatabase database) {
        this.context = context;
        this.database = database;

        contentValues = new ContentValues();
    }
}