package com.sx.portal.entity;

/**
 * author:Javier
 * time:2016/4/11.
 * mail:38244704@qq.com
 */
public class MeasureEntity {
    private int id;
    // 收缩压
    private int sbp;
    // 舒张压
    private int dbp;
    // 心率
    private int heart_beat;
    // 用户ID
    private int uid;
    // 测量时间
    private long time;
    // 测量时间, 格式yyyy-MM-dd
    private String time_date;
    // 测量日期，格式yyyy-MM-dd HH:mm:ss
    private String dateTime;
    // header id
    private long headerId;
    // 血压级别
    private int level;

    public int getDbp() {
        return dbp;
    }

    public void setDbp(int dbp) {
        this.dbp = dbp;
    }

    public int getHeart_beat() {
        return heart_beat;
    }

    public void setHeart_beat(int heart_beat) {
        this.heart_beat = heart_beat;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSbp() {
        return sbp;
    }

    public void setSbp(int sbp) {
        this.sbp = sbp;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getTime_date() {
        return time_date;
    }

    public void setTime_date(String time_date) {
        this.time_date = time_date;
    }

    public long getHeaderId() {
        return headerId;
    }

    public void setHeaderId(long headerId) {
        this.headerId = headerId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}