package com.sx.portal.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

/**
 * 作者：Javier
 * 时间：2017/4/18.
 * 描述：权限控制
 */
public class PermissionUtil {
    /**
     * 6.0 系统需要申请定位权限才可以搜索蓝牙设备
     *
     * @return
     */
    public static boolean mayRequestLocation(Activity activity) {
        if (Build.VERSION.SDK_INT >= 23) {
            int checkCallPhonePermission = ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION);
            if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
                return true;
            }
        }

        return false;
    }

    public static boolean mayRequestLocation(Fragment fragment) {
        if (Build.VERSION.SDK_INT >= 23) {
            int checkCallPhonePermission = ContextCompat.checkSelfPermission(fragment.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION);
            if (checkCallPhonePermission != PackageManager.PERMISSION_GRANTED) {
                fragment.requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
                return true;
            }
        }

        return false;
    }


}