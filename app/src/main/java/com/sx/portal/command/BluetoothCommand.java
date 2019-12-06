package com.sx.portal.command;

import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;

import com.javier.simplemvc.patterns.command.SimpleCommand;
import com.javier.simplemvc.patterns.notify.NotifyMessage;
import com.sx.portal.IBluetoothDeviceService;
import com.sx.portal.IBluetoothDeviceServiceCallback;
import com.sx.portal.MsgConstants;
import com.sx.portal.R;
import com.sx.portal.dao.MeasureDao;
import com.sx.portal.entity.DeviceEntity;
import com.sx.portal.entity.MeasureEntity;
import com.sx.portal.service.BluetoothDeviceService;
import com.sx.portal.util.Constants;
import com.sx.portal.util.LevelUtil;

import java.util.ArrayList;
import java.util.Date;

/**
 * author:Javier
 * time:2016/4/12.
 * mail:38244704@qq.com
 * <p/>
 * 操作蓝牙设备的action
 */
public class BluetoothCommand extends SimpleCommand {

    /**
     * 调用蓝牙服务的接口
     */
    private IBluetoothDeviceService mDeviceService;

    /**
     * 搜索到的设备列表
     */
    private ArrayList<DeviceEntity> mScanDevices = new ArrayList<>();

    /**
     * 当前连接的蓝牙设备
     */
    private BluetoothDevice mCurrentConnectDevice;

    /**
     * 当前测量的用户ID
     */
    private int currentMeasureMemberId;

    /**
     * 是否已终止测量, 在发送终止测量指令好，若300ms之内没有停止，则在发送一次；当测量开始时，设置为false，终止成功以后设置为true
     */
    private boolean bInterrupted = false;

    /**
     * 发送终止命令的次数
     */
    private int iterruptedCount = 1;

    @Override
    public String[] listMessage() {
        return new String[]{
                MsgConstants.MSG_BIND_BLUETOOTH_SERVICE,
                MsgConstants.MSG_CHECK_BLUETOOTH_STATUS,
                MsgConstants.MSG_SCANNING_DEVICE,
                MsgConstants.MSG_CONNECT_DEVICE,
                MsgConstants.MSG_UNBIND_BLUETOOTH_SERVICE,
                MsgConstants.MSG_START_MEASURE,
                MsgConstants.MSG_INTERRUPTED_MEASURE
        };
    }

    @Override
    public void handlerMessage(NotifyMessage notifyMessage) {
        try {
            switch (notifyMessage.getName()) {
                case MsgConstants.MSG_BIND_BLUETOOTH_SERVICE:
                    startBindBluetoothService(notifyMessage.getContext());
                    break;
                case MsgConstants.MSG_CHECK_BLUETOOTH_STATUS:
                    checkBluetoothStatus();
                    break;
                case MsgConstants.MSG_SCANNING_DEVICE:
                    startScanningDevice();
                    break;
                case MsgConstants.MSG_CONNECT_DEVICE:
                    startConnectDevice((BluetoothDevice) notifyMessage.getParam());
                    break;
                case MsgConstants.MSG_UNBIND_BLUETOOTH_SERVICE:
                    unBindBluetoothService(notifyMessage.getContext());
                    break;
                case MsgConstants.MSG_START_MEASURE:
                    currentMeasureMemberId = Integer.parseInt(notifyMessage.getParam().toString());
                    // 开始测量
                    startMeasure();
                    break;
                case MsgConstants.MSG_INTERRUPTED_MEASURE:
                    // 终止测量
                    interruptedMeasure();
                    break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 开始绑定蓝牙服务
     */
    private void startBindBluetoothService(Context context) {
        Intent intent = new Intent(context, BluetoothDeviceService.class);
        context.bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

    /**
     * 解绑
     */
    private void unBindBluetoothService(Context context) {
        logger.i("unBindBluetoothService");
        if (mDeviceService != null) {
            try {
                mDeviceService.unRegisterCallback(iBluetoothDeviceServiceCallback);
                context.unbindService(conn);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 检测蓝牙状态
     */
    private void checkBluetoothStatus() {
        try {
            if (mDeviceService != null) {
                mDeviceService.checkBluetoothStatus();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 开始搜索设备
     */
    private void startScanningDevice() {
        try {
            if (mDeviceService != null) {
                mDeviceService.scanDevice(Constants.SCAN_PERIOD, Constants.SCAN_COUNT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 开始搜索设备
     */
    private void startConnectDevice(BluetoothDevice device) {
        this.mCurrentConnectDevice = device;

        try {
            if (mDeviceService != null) {
                mDeviceService.connectDevice(device);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 开始测量
     */
    private void startMeasure() {
        try {
            if (mDeviceService != null) {
                mDeviceService.startMeasure();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 终止测量
     */
    private void interruptedMeasure() {
        try {
            if (mDeviceService != null) {
                mDeviceService.stopMeasure();
            }

            if (iterruptedCount > 0) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        iterruptedCount--;

                        if (!bInterrupted) {
                            interruptedMeasure();
                        } else {
                            iterruptedCount = 1;
                            bInterrupted = true;
                        }
                    }
                }, 300);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加测量结果到数据库
     */
    private void savedToDatabase(int diastolicPressure, int systolicPressure, int heartRate) {
        MeasureEntity entity = new MeasureEntity();
        entity.setUid(currentMeasureMemberId);
        entity.setTime(new Date().getTime());
        entity.setDbp(diastolicPressure);
        entity.setSbp(systolicPressure);
        entity.setHeart_beat(heartRate);
        entity.setLevel(LevelUtil.getLevel(systolicPressure, diastolicPressure));

        MeasureDao measureDao = (MeasureDao) getDao(R.id.id_measure_dao);
        measureDao.addMeasure(entity);
    }

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mDeviceService = IBluetoothDeviceService.Stub.asInterface(service);

            try {
                if (mDeviceService != null) {
                    mDeviceService.registerCallback(iBluetoothDeviceServiceCallback);

                    notifyManager.sendNotifyMessage(MsgConstants.MSG_BIND_BLUETOOTH_SERVICE_COMPLETE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    protected IBluetoothDeviceServiceCallback.Stub iBluetoothDeviceServiceCallback;

    {
        iBluetoothDeviceServiceCallback = new IBluetoothDeviceServiceCallback.Stub() {
            @Override
            public void onBluetoothCheckComplete() throws RemoteException {
                notifyManager.sendNotifyMessage(MsgConstants.MSG_BLUETOOTH_ENABLE);
            }

            @Override
            public void onNotSupperBluetooth() throws RemoteException {

            }

            @Override
            public void onBluetoothDisable() throws RemoteException {
                notifyManager.sendNotifyMessage(MsgConstants.MSG_BLUETOOTH_DISABLE);
            }

            @Override
            public void onScanning(BluetoothDevice device) throws RemoteException {

                DeviceEntity entity = new DeviceEntity();
                entity.setName(device.getName());
                entity.setAddress(device.getAddress());
                entity.setmBluetoothDevice(device);

                mScanDevices.add(entity);
            }

            @Override
            public void onStopScan() throws RemoteException {
                // 判断搜索到的设备
                logger.i("scanning device :" + mScanDevices.size());

                if (mScanDevices.size() == 0) {
                    logger.i("onStopScan, Scanning device is null.");
                    notifyManager.sendNotifyMessage(MsgConstants.MSG_BLUETOOTH_NOT_FOUND);
                    return;
                }

                removeDuplicate(mScanDevices);

                logger.i("removeDuplicate scan device :" + mScanDevices.size());

                if (mScanDevices.size() == 1) {
                    logger.i("scanning only one device, start connect");
                    // 当搜索到一个设备的时候，自动连接
                    startConnectDevice(mScanDevices.get(0).getmBluetoothDevice());
                } else if (mScanDevices.size() > 1) {
                    logger.i("scanning more device, pop select dialog.");
                    notifyManager.sendNotifyMessage(MsgConstants.MSG_SCANNING_DEVICE_MORE, mScanDevices);
                }
            }

            @Override
            public void onConnectChanged(boolean bConnected) throws RemoteException {
//            logger.i("onConnectChanged : " + bConnected);

                try {
                    if (!bConnected) {
                        onDisconnect();
                    } else {
                        onConnect();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onReadBattery(String battery) throws RemoteException {
                notifyManager.sendNotifyMessage(MsgConstants.MSG_BATTERY, battery);
            }

            @Override
            public void onDisconnect() throws RemoteException {
                notifyManager.sendNotifyMessage(MsgConstants.MSG_DISCONNECT);
            }

            @Override
            public void onConnect() throws RemoteException {
                notifyManager.sendNotifyMessage(MsgConstants.MSG_CONNECTED, mCurrentConnectDevice);
            }

            @Override
            public void onPressureMeasureStart() throws RemoteException {
                bInterrupted = false;

                notifyManager.sendNotifyMessage(MsgConstants.MSG_MEASURE_STARTED);
            }

            @Override
            public void onPressureMeasureEnd(int diastolicPressure, int systolicPressure, int heartRate) throws RemoteException {
                Bundle bundle = new Bundle();

                if (diastolicPressure < 100) {
                    bundle.putString("diastolic", "0" + diastolicPressure);
                } else {
                    bundle.putString("diastolic", "" + diastolicPressure);
                }

                if (systolicPressure < 100) {
                    bundle.putString("systolic", "0" + systolicPressure);
                } else {
                    bundle.putString("systolic", systolicPressure + "");
                }

                if (heartRate < 100) {
                    bundle.putString("heart_rate", "0" + heartRate);
                } else {
                    bundle.putString("heart_rate", "" + heartRate);
                }

                savedToDatabase(diastolicPressure, systolicPressure, heartRate);

                notifyManager.sendNotifyMessage(MsgConstants.MSG_MEASURE_END, bundle);
            }

            @Override
            public void onShouledPressure(int shoulderPressure) throws RemoteException {
                // 获取测量过程中的数据显示
                notifyManager.sendNotifyMessage(MsgConstants.MSG_SHOULD_PRESSURE, String.valueOf(shoulderPressure));
            }

            @Override
            public void onError(String msg) throws RemoteException {
                logger.w("onError " + msg);
                notifyManager.sendNotifyMessage(MsgConstants.MSG_ON_ERROR, msg);
            }

            @Override
            public void onMeasureInterrupted() throws RemoteException {
                notifyManager.sendNotifyMessage(MsgConstants.MSG_MEASURE_INTERRUPTED);
            }

            @Override
            public void onStartButDeviceDisconnect() throws RemoteException {
            }
        };
    }

    /**
     * 删除搜索到的设备列表中重复的项
     */
    private void removeDuplicate(ArrayList<DeviceEntity> src) {
        try {
            for (int i = 0; i < src.size(); i++) {
                DeviceEntity d_i = src.get(i);

                String i_address = d_i.getAddress();
                String i_name = d_i.getName();
                for (int j = src.size() - 1; j > i; j--) {
                    DeviceEntity d_j = src.get(j);

                    String j_address = d_j.getAddress();
                    String j_name = d_j.getName();

                    if (i_address.equals(j_address) && i_name.equals(j_name)) {
                        src.remove(j);
                    }
                }
            }
        } catch (Exception e) {
            logger.e(e);
        }
    }
}