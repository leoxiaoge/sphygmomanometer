package com.sx.portal.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * author:Javier
 * time:2016/4/11.
 * mail:38244704@qq.com
 */
public class MemberEntity implements Parcelable {
    private int id;
    private String name;
    // 默认头像
    private int icon;
    // 年龄
    private int age;
    // 性别
    private int sex;
    // 自定义头像
    private String headIcon;

    public MemberEntity(int id, String name, int icon, int age, String headIcon, int sex) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.age = age;
        this.headIcon = headIcon;
        this.sex = sex;
    }

    public MemberEntity() {

    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHeadIcon() {
        return headIcon;
    }

    public void setHeadIcon(String headIcon) {
        this.headIcon = headIcon;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeInt(icon);
        dest.writeInt(age);
        dest.writeString(headIcon);
        dest.writeInt(sex);
    }

    public static final Creator<MemberEntity> CREATOR = new Creator<MemberEntity>() {
        @Override
        public MemberEntity[] newArray(int size) {
            return new MemberEntity[size];
        }

        @Override
        public MemberEntity createFromParcel(Parcel in) {
            return new MemberEntity(in);
        }
    };

    public MemberEntity(Parcel in) {
        id = in.readInt();
        name = in.readString();
        icon = in.readInt();
        age = in.readInt();
        headIcon = in.readString();
        sex = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }
}