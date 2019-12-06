package com.sx.portal.util;

import android.util.SparseArray;

/**
 * author:Javier
 * time:2016/4/11.
 * mail:38244704@qq.com
 *
 * 常量
 */
public class Constants {
    /**
     * 搜索蓝牙设备超时时间，单位为ms
     */
    public static final int SCAN_PERIOD = 2000;

    /**
     * 搜索蓝牙设备的次数，当搜索蓝牙设备失败以后，连续在执行几次搜索
     */
    public static final int SCAN_COUNT = 3;

    /**
     * 如果是第一次登陆，则延迟两秒进入向导
     */
    public static final int INTO_GUIDE_DELAY = 2000;

    /**
     * 血压仪测量范围
     */
    public static int LOW_RANGE = 0;

    public static int MAX_RANGE = 300;

    /**
     * 图表刻度单位
     * 1个刻度相当于3个mmHg单位
     */
    public static int CHART_MARK = MAX_RANGE / 100;

    public static float CHART_ITEM_RANGE = 16.67f;
}