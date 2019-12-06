package com.sx.portal.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.javier.simplemvc.patterns.model.SimpleDao;
import com.sx.portal.entity.MeasureEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * author:Javier
 * time:2016/4/11.
 * mail:38244704@qq.com
 *
 * 操作测量记录数据库表的dao
 */
@SuppressWarnings("unused")
public class MeasureDao extends SimpleDao {

    private final String MEASURING_RECORD = "measuring_record";

    public MeasureDao(Context context, SQLiteDatabase database) {
        super(context, database);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String measure_sql = "CREATE TABLE IF NOT EXISTS "
                + MEASURING_RECORD
                + "(id INTEGER NOT NULL PRIMARY KEY, time INTEGER, sbp INTEGER, dbp INTEGER, heartrate INTEGER, mid INTEGER, level INTEGER, FOREIGN KEY(mid) REFERENCES member(id))";
        sqLiteDatabase.execSQL(measure_sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        String upgrade_sql = "drop table " + MEASURING_RECORD;
        sqLiteDatabase.execSQL(upgrade_sql);
        onCreate(sqLiteDatabase);
    }

    /**
     * 添加测量记录
     *
     * @param entity 测量记录对象
     */
    public void addMeasure(MeasureEntity entity) {
        if (entity.getSbp() == 0 || entity.getDbp() == 0 || entity.getHeart_beat() == 0) {
            logger.i("测量数据无效");
            return;
        }

        contentValues.clear();
        contentValues.put("time", entity.getTime());
        contentValues.put("sbp", entity.getSbp());
        contentValues.put("dbp", entity.getDbp());
        contentValues.put("heartrate", entity.getHeart_beat());
        contentValues.put("mid", entity.getUid());
        contentValues.put("level", entity.getLevel());

        long ret = database.insert(MEASURING_RECORD, null, contentValues);

        if (ret != -1) {
            logger.d("add measure success");
        }
    }

    /**
     * 根据时间段查询某一个用户的测量记录
     *
     * @param start 开始时间
     * @param end   结束时间
     * @param mid   用户ID
     * @return 测量记录列表
     */
    public ArrayList<MeasureEntity> readMeasureByTime(long start, long end, int mid, String order) {
        if (order == null || order.equalsIgnoreCase("")) {
            order = "asc";
        }
        String sql = "select * from " + MEASURING_RECORD + " where mid = " + mid + " and time between " + start + " and " + end + " order by time " + order;

        Cursor cursor = database.rawQuery(sql, null);
        ArrayList<MeasureEntity> es = new ArrayList<>();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        while (cursor.moveToNext()) {
            MeasureEntity entity = new MeasureEntity();
            entity.setId(cursor.getInt(cursor
                    .getColumnIndex("id")));
            entity.setSbp(cursor.getInt(cursor.getColumnIndex("sbp")));
            entity.setDbp(cursor.getInt(cursor.getColumnIndex("dbp")));
            entity.setHeart_beat(cursor.getInt(cursor.getColumnIndex("heartrate")));
            entity.setUid(cursor.getInt(cursor.getColumnIndex("mid")));
            entity.setLevel(cursor.getInt(cursor.getColumnIndex("level")));

            long time = cursor.getLong(cursor.getColumnIndex("time"));
            Date date = new Date(time);
            String dateTime = dateTimeFormat.format(date);
            String date_txt = dateFormat.format(date);

            try {
                Date d = dateTimeFormat.parse(date_txt + " " + "00:00:00");
                entity.setHeaderId(d.getTime());
            } catch (Exception e) {
                e.printStackTrace();
            }
            entity.setTime(time);
            entity.setDateTime(dateTime);
            entity.setTime_date(date_txt);

            es.add(entity);
        }

        cursor.close();

        return es;
    }
}
