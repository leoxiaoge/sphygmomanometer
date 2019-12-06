// IBluetoothDeviceServiceCallback.aidl
package com.sx.portal;

// Declare any non-default types here with import statements

import android.bluetooth.BluetoothDevice;

interface IBluetoothDeviceServiceCallback {
        /**
        * 设备不支持蓝牙
        */
        void onNotSupperBluetooth();
        /**
        * 蓝牙设备未打开
        */
        void onBluetoothDisable();
        /**
        * 蓝牙设备检测成功
        */
        void onBluetoothCheckComplete();
        /**
        *  搜索蓝牙设备返回
        */
        void onScanning(in BluetoothDevice device);
        /**
        * 停止搜索蓝牙
        */
        void onStopScan();
        /**
         * 设备连接状态改变
         *
         * @param bConnected 是否连接
         */
        void onConnectChanged(boolean bConnected);

        /**
         * 读取电池电量
         *
         * @param battery 电池电量
         */
        void onReadBattery(String battery);

        /**
         * 设备断开连接
         */
        void onDisconnect();

        /**
         * 连接成功
        */
        void onConnect();

        /**
         * 开始测量血压
         */
        void onPressureMeasureStart();

        /**
         * 测量血压结束
         *
         * @param diastolicPressure 舒张压
         * @param systolicPressure  收缩压
         * @param heartRate         心率
        */
        void onPressureMeasureEnd(int diastolicPressure, int systolicPressure, int heartRate);

        /**
         * 测量血压的臂压
         *
         * @param shoulderPressure 臂压
         */
        void onShouledPressure(int shoulderPressure);

        /**
         * 测量出现错误
         *
         * @param msg 错误提示信息
         */
        void onError(String msg);

        /**
         * 打断测量血压
         */
        void onMeasureInterrupted();

        /**
         * 设备未连接
         */
         void onStartButDeviceDisconnect();
}
