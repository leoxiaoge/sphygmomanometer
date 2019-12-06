package com.javier.simplemvc.interfaces;

import android.database.sqlite.SQLiteDatabase;

/**
 * author:Javier
 * time:2016/5/28.
 * mail:38244704@qq.com
 */
public interface IDao {
    void onCreate(SQLiteDatabase db);
    void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);
}
