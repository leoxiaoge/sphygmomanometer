package com.sx.portal.util;

import android.util.SparseArray;

import com.sx.portal.R;

/**
 * author:Javier
 * time:2016/6/11.
 * mail:38244704@qq.com
 * <p/>
 * 根据舒张压和收缩压计算血压范围
 * <p/>
 * <p/>
 * 血压范围对照表
 * <p/>
 * 偏低   0 - 90           0 - 60
 * 理想   90 - 120         60 - 80
 * 正常   120 - 140        80 - 90
 * 轻度   140 - 150        90 - 100
 * 中度   150 - 180        100 - 110
 * 重度   180 - 300        110以上
 */
public class LevelUtil {

    public static SparseArray<Integer> BLOOD_PRESSURE_LABEL = new SparseArray<>();

    static {
        BLOOD_PRESSURE_LABEL.put(0, R.string.level_0);
        BLOOD_PRESSURE_LABEL.put(1, R.string.level_1);
        BLOOD_PRESSURE_LABEL.put(2, R.string.level_2);
        BLOOD_PRESSURE_LABEL.put(3, R.string.level_3);
        BLOOD_PRESSURE_LABEL.put(4, R.string.level_4);
        BLOOD_PRESSURE_LABEL.put(5, R.string.level_5);
        BLOOD_PRESSURE_LABEL.put(6, R.string.level_6);
    }

    public static SparseArray<Integer> BLOOD_PRESSURE_ICON = new SparseArray<>();

    static {
        BLOOD_PRESSURE_ICON.put(0, R.mipmap.level_0);
        BLOOD_PRESSURE_ICON.put(1, R.mipmap.level_1);
        BLOOD_PRESSURE_ICON.put(2, R.mipmap.level_2);
        BLOOD_PRESSURE_ICON.put(3, R.mipmap.level_3);
        BLOOD_PRESSURE_ICON.put(4, R.mipmap.level_4);
        BLOOD_PRESSURE_ICON.put(5, R.mipmap.level_5);
        BLOOD_PRESSURE_ICON.put(6, R.mipmap.level_6);
    }

    public static SparseArray<Integer> LEVEL_LOW_SBP = new SparseArray<>();

    static {
        LEVEL_LOW_SBP.put(0, 0);
        LEVEL_LOW_SBP.put(1, 90);
        LEVEL_LOW_SBP.put(2, 120);
        LEVEL_LOW_SBP.put(3, 140);
        LEVEL_LOW_SBP.put(4, 150);
        LEVEL_LOW_SBP.put(5, 180);
        LEVEL_LOW_SBP.put(6, 300);
    }

    public static SparseArray<Integer> LEVEL_LOW_DBP = new SparseArray<>();

    static {
        LEVEL_LOW_DBP.put(0, 0);
        LEVEL_LOW_DBP.put(1, 60);
        LEVEL_LOW_DBP.put(2, 80);
        LEVEL_LOW_DBP.put(3, 90);
        LEVEL_LOW_DBP.put(4, 100);
        LEVEL_LOW_DBP.put(5, 110);
        LEVEL_LOW_DBP.put(6, 300);
    }

    public static int getLevel(int sbp, int dbp) {
        if (sbp == LEVEL_LOW_SBP.get(0)) {
            return 0;
        } else if (sbp < LEVEL_LOW_SBP.get(1) || dbp < LEVEL_LOW_DBP.get(1)) {
            return 1;
        } else if (sbp < LEVEL_LOW_SBP.get(2) && dbp < LEVEL_LOW_DBP.get(2)) {
            return 2;
        } else if (sbp < LEVEL_LOW_SBP.get(3) && dbp < LEVEL_LOW_DBP.get(3)) {
            return 3;
        } else if (sbp < LEVEL_LOW_SBP.get(4) || dbp < LEVEL_LOW_DBP.get(4)) {
            return 4;
        } else if (sbp < LEVEL_LOW_SBP.get(5) || dbp < LEVEL_LOW_DBP.get(5)) {
            return 5;
        } else if (sbp < LEVEL_LOW_SBP.get(6) || dbp < LEVEL_LOW_DBP.get(6)) {
            return 6;
        } else {
            return -1;
        }
    }

    public static float pressure_chart_mark(int sbp, int dbp) {
        int level = getLevel(sbp, dbp);

        float levelRange = 0f;

        if (level == 0) {
            return 0;
        } else if (level == 1) {
            levelRange = LEVEL_LOW_SBP.get(level) / Constants.CHART_ITEM_RANGE;
        } else {
            levelRange = (LEVEL_LOW_SBP.get(level) - LEVEL_LOW_SBP.get(level - 1)) / Constants.CHART_ITEM_RANGE;
        }

        float y = level * Constants.CHART_ITEM_RANGE;

        float ys = (sbp - LEVEL_LOW_SBP.get(level)) / levelRange;

        return y + ys;
//
//
//

//
//        return y + ys;

//        float averageSBP = (sbp - Constants.LOW_SBP) / 2;
//        float averageDBP = (dbp - Constants.LOW_DBP) / 2;
//
//        float avg = (averageSBP + averageDBP) / 2;
//
//        Logger.getLogger().i(" -- " + avg);
//
//        return avg;
//        return (averageSBP + averageDBP) / 2;

//        return sbp / Constants.CHART_MARK;
    }
}