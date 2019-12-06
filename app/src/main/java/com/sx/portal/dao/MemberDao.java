package com.sx.portal.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.javier.simplemvc.patterns.model.SimpleDao;
import com.sx.portal.entity.MemberEntity;

import java.util.ArrayList;

/**
 * author:Javier
 * time:2016/4/11.
 * mail:38244704@qq.com
 * <p/>
 * 操作member表的dao
 */
@SuppressWarnings("unused")
public class MemberDao extends SimpleDao {

    private final String MEMBER = "member";

    public MemberDao(Context context, SQLiteDatabase database) {
        super(context, database);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String member_sql = "CREATE TABLE IF NOT EXISTS "
                + MEMBER
                + "(id INTEGER NOT NULL PRIMARY KEY, name TEXT, age INTEGER, sex INTEGER, icon INTEGER, headIcon TEXT)";
        sqLiteDatabase.execSQL(member_sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int ii) {
        String upgrade_sql = "drop table " + MEMBER;
        sqLiteDatabase.execSQL(upgrade_sql);
        onCreate(sqLiteDatabase);
    }

    public void createMember(MemberEntity member) {
        contentValues.clear();
        contentValues.put("name", member.getName());
        contentValues.put("age", member.getAge());
        contentValues.put("icon", member.getIcon());
        contentValues.put("headIcon", member.getHeadIcon());
        contentValues.put("sex", member.getSex());

        long ret = database.insert(MEMBER, null, contentValues);

        if (ret != -1) {
            logger.d("create member success");
        }
    }

    public void createMembers(ArrayList<MemberEntity> members) {
        for (MemberEntity entity : members) {
            createMember(entity);
        }
    }

    public void updateMember(MemberEntity member) {
        contentValues.clear();
        contentValues.put("name", member.getName());
        contentValues.put("age", member.getAge());
        contentValues.put("icon", member.getIcon());
        contentValues.put("headIcon", member.getHeadIcon());
        contentValues.put("sex", member.getSex());

        database.update(MEMBER, contentValues, "id = ?", new String[]{String.valueOf(member.getId())});
    }

    public ArrayList<MemberEntity> getAllMembers() {
        Cursor cursor = database.query(MEMBER, new String[]{"id", "name",
                "icon", "age", "headIcon", "sex"}, null, null, null, null, null);

        ArrayList<MemberEntity> es = new ArrayList<>();

        while (cursor.moveToNext()) {
            MemberEntity entity = new MemberEntity();
            entity.setId(cursor.getInt(cursor
                    .getColumnIndex("id")));
            entity.setName(cursor.getString(cursor
                    .getColumnIndex("name")));
            entity.setAge(cursor.getInt(cursor.getColumnIndex("age")));
            entity.setSex(cursor.getInt(cursor.getColumnIndex("sex")));
            entity.setHeadIcon(cursor.getString(cursor.getColumnIndex("headIcon")));
            entity.setIcon(cursor.getInt(cursor.getColumnIndex("icon")));

            es.add(entity);
        }

        cursor.close();

        logger.i("read all member size is : " + es.size());

        return es;
    }
}