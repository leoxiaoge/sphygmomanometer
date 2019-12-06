// IBluetoothDeviceService.aidl
package com.sx.portal;

// Declare any non-default types here with import statements

import com.sx.portal.IBluetoothDeviceServiceCallback;

import android.bluetooth.BluetoothDevice;

interface IBluetoothDeviceService {
    void registerCallback(IBluetoothDeviceServiceCallback callback);
    void unRegisterCallback(IBluetoothDeviceServiceCallback callback);
    void checkBluetoothStatus();
    void scanDevice(long timeout, int scanCount);
    void connectDevice(in BluetoothDevice device);
    void startMeasure();
    void stopMeasure();
    void obtainMeasureResult();
}