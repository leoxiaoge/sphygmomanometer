package com.sx.portal.entity;

import android.bluetooth.BluetoothDevice;

/**
 * author:Javier
 * time:2016/4/11.
 * mail:38244704@qq.com
 */
public class DeviceEntity {
    private String name;
    private String address;
    private boolean connectStatus;
    private int battery;
    private BluetoothDevice mBluetoothDevice;

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public boolean isConnectStatus() {
        return connectStatus;
    }

    public void setConnectStatus(boolean connectStatus) {
        this.connectStatus = connectStatus;
    }

    public BluetoothDevice getmBluetoothDevice() {
        return mBluetoothDevice;
    }

    public void setmBluetoothDevice(BluetoothDevice mBluetoothDevice) {
        this.mBluetoothDevice = mBluetoothDevice;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}