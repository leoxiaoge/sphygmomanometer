package com.sx.portal.service;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

import com.javier.simplemvc.util.Logger;
import com.sx.portal.IBluetoothDeviceService;
import com.sx.portal.IBluetoothDeviceServiceCallback;
import com.sx.portal.R;
import com.sx.portal.util.BLEGattAttributes;
import com.sx.portal.util.Constants;
import com.sx.portal.util.DialogBuild;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

@TargetApi(21)
public class BluetoothDeviceService extends Service {

    private static final Logger logger = Logger.getLogger();

    private static int conn_response = 3; //重连3次，在连接有反应的时候重置为3，每一次写指令，--

    /**
     * 设备不支持蓝牙
     */
    public static final int MSG_BLUETOOTH_NOT_SUPPORT = 0;
    /**
     * 蓝牙设备未打开
     */
    public static final int MSG_BLUETOOTH_DISABLE = 1;
    /**
     * 蓝牙设备检测成功
     */
    public static final int MSG_BLUETOOTH_CHECK_COMPLETE = 2;
    /**
     * 正在搜索设备
     */
    public static final int MSG_SCANNING = 100;
    /**
     * 停止搜索
     */
    public static final int MSG_STOP_SCAN = 101;
    /**
     * 蓝牙连接状态改变
     */
    public static final int MSG_BLUETOOTH_CONNECT_CHANGED = 102;
    /**
     * 蓝牙连接成功
     */
    public static final int MSG_BLUETOOTH_CONNECT_SUCCESS = 103;
    /**
     * 获取电池电量
     */
    public static final int MSG_BLUETOOTH_DEVICE_BATTERY = 104;
    /**
     * 正在测量
     */
    public static final int MSG_BLOOD_PRESSURE_START = 105;
    /**
     * 停止测量
     */
    public static final int MSG_BLOOD_MEASURE_INTERRUPTED = 106;
    /**
     * 测量错误
     */
    public static final int MSG_BLOOD_MEASURE_ERROR = 107;
    /**
     * 停止测量
     */
    public static final int MSG_BLOOD_PRESSURE_MEASURE_END = 108;
    /**
     * 臂压，测量结果无效的时候显示
     */
    public static final int MSG_BLOOD_SHOULDER_PRESSURE = 109;
    /**
     * 设备断开连接
     */
    public static final int MSG_BLUETOOTH_DEVICE_DISCONNECT = 110;
    /**
     * 设备未连接，需要重新搜索
     */
    public static final int MSG_DEVICE_NONE_CONNECTED = 111;

    private RemoteCallbackList<IBluetoothDeviceServiceCallback> mCallbacks = new RemoteCallbackList<IBluetoothDeviceServiceCallback>();

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattCharacteristic writeGattCharacteristic, readGattCharacteristic;

    // level 21
    private BluetoothLeScanner mScanner;

    // 是否连接
    private boolean bConnected = false;
    //电池电量
    private byte battery_electry = 0;
    // 读取电池电量
    private Timer timerBattery;
    //正在测量血压
    private boolean bBloodMeasuring = false;
    private Timer timerBloodResult;

    private int shoulderPressure = 0;//臂压
    private int systolicPressure = 0;//收缩压
    private int diastolicPressure = 0;//舒张压
    private int heartRate = 0;//心率
    private String msg_error;

    private BluetoothHandler mHandler;

    // 第一次没搜索到蓝牙设备后，重新搜索的次数
    private int scanTryCount = 0;

    // 搜索超时时间
    private long scanTimeout = Constants.SCAN_PERIOD;

    /**
     * 保存搜索到的蓝牙设备。用于判断是否有搜索到蓝牙设备
     */
    private List<SoftReference<BluetoothDevice>> scanningDevices = new ArrayList<>();

    /**
     * 处理蓝牙回调结果，并且通过AIDL回调客户端
     */
    class BluetoothHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            int count = mCallbacks.beginBroadcast();

            try {
                for (int i = 0; i < count; i++) {
                    IBluetoothDeviceServiceCallback callback = mCallbacks.getBroadcastItem(i);

                    switch (msg.what) {
                        // 设备不支持蓝牙
                        case MSG_BLUETOOTH_NOT_SUPPORT:
                            logger.i("the device un support bluetooth");
                            callback.onNotSupperBluetooth();
                            break;
                        // 蓝牙未打开
                        case MSG_BLUETOOTH_DISABLE:
                            logger.i("bluetooth disable");
                            callback.onBluetoothDisable();
                            break;
                        case MSG_BLUETOOTH_CHECK_COMPLETE:
                            logger.i("check bluetooth device success");
                            callback.onBluetoothCheckComplete();
                            break;
                        // 正在搜索设备，搜索到一个设备回调一次
                        case MSG_SCANNING:
                            logger.i("scanning once");
                            BluetoothDevice d = (BluetoothDevice) msg.obj;

                            // 将搜索到的蓝牙设备以软引用的形式保存在list中
                            SoftReference<BluetoothDevice> softReference = new SoftReference<BluetoothDevice>(d);
                            scanningDevices.add(softReference);

                            callback.onScanning(d);
                            break;
                        // 停止搜索
                        case MSG_STOP_SCAN:
                            logger.i("stop scanning");

                            if (Build.VERSION.SDK_INT >= 21) {
                                mScanner.stopScan(newScanCallBack());
                            } else {
                                if (mBluetoothAdapter != null) {
                                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                                }
                            }

                            if (scanningDevices.size() == 0 && scanTryCount > 0) {
                                logger.i("scanTryCount is " + scanTryCount + ", Try again!");
                                doScanning();
                            } else {
                                callback.onStopScan();
                            }
                            break;
                        // 连接设备成功
                        case MSG_BLUETOOTH_CONNECT_SUCCESS:
                            logger.i("connect device success");
                            startGetDeviceBattery();
                            callback.onConnect();
                            break;
                        // 设备断开连接
                        case MSG_BLUETOOTH_DEVICE_DISCONNECT:
                            callback.onDisconnect();
                            break;
                        // 连接状态改变
                        case MSG_BLUETOOTH_CONNECT_CHANGED:
                            callback.onConnectChanged(bConnected);
                            break;
                        // 开始测量
                        case MSG_BLOOD_PRESSURE_START:
                            logger.i("start pressure measure");
                            bBloodMeasuring = true;
                            timerBloodResult = new Timer();
                            timerBloodResult.schedule(new ReadBloodResultTask(), 0, 250);

                            callback.onPressureMeasureStart();
                            break;
                        // 停止测量
                        case MSG_BLOOD_PRESSURE_MEASURE_END:
                            logger.i("pressure measure result:" + diastolicPressure + " - " + systolicPressure + " - " + heartRate);
                            stopReadBloodResult();

                            callback.onPressureMeasureEnd(diastolicPressure, systolicPressure, heartRate);
                            break;
                        // 中断测量
                        case MSG_BLOOD_MEASURE_INTERRUPTED:
                            logger.i("measure interrupted");
                            stopReadBloodResult();

                            callback.onMeasureInterrupted();
                            break;
                        // 测量错误
                        case MSG_BLOOD_MEASURE_ERROR:
                            logger.i("measure error!");
                            stopReadBloodResult();

                            callback.onError(msg_error);
                            break;
                        // 臂压
                        case MSG_BLOOD_SHOULDER_PRESSURE:
//                            logger.i("shoulder pressure ：" + shoulderPressure);
                            callback.onShouledPressure(shoulderPressure);
                            break;
                        // 获取设备电池电量
                        case MSG_BLUETOOTH_DEVICE_BATTERY:
//                            logger.i(String.format("battery：%s", battery_electry));
                            callback.onReadBattery(String.format("%s", battery_electry));
                            break;
                        // 设备未连接，点击开始测量的时候调用
                        case MSG_DEVICE_NONE_CONNECTED:
                            logger.i("start measure but device not connected,did you want reconnect again?");
                            callback.onStartButDeviceDisconnect();
                            break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mCallbacks.finishBroadcast();
            }
        }
    }

    private IBluetoothDeviceService.Stub mBinder = new IBluetoothDeviceService.Stub() {
        @Override
        public void registerCallback(IBluetoothDeviceServiceCallback callback) throws RemoteException {
            if (mCallbacks == null) {
                logger.i("mCallbacks is null!");
            }
            mCallbacks.register(callback);
        }

        @Override
        public void unRegisterCallback(IBluetoothDeviceServiceCallback callback) throws RemoteException {
            mCallbacks.unregister(callback);
        }

        @Override
        public void checkBluetoothStatus() {
            logger.i("start bluetooth checking ...");
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            if (mBluetoothAdapter == null) {
                Dialog dialog = DialogBuild.getBuild().createAlarmDialog(BluetoothDeviceService.this, getString(R.string.none_bluetooth), new DialogBuild.OnConfirmListener() {
                    @Override
                    public void onConfirm(Dialog dialog, boolean isConfirm) {
                        dialog.dismiss();
                        mHandler.sendEmptyMessage(MSG_BLUETOOTH_NOT_SUPPORT);
                    }
                });
                dialog.show();
                return;
            }

            if (!mBluetoothAdapter.isEnabled()) {
                mHandler.sendEmptyMessage(MSG_BLUETOOTH_DISABLE);
                return;
            }

            mHandler.sendEmptyMessage(MSG_BLUETOOTH_CHECK_COMPLETE);
        }

        @Override
        public void scanDevice(long timeout, int scanCount) throws RemoteException {
            logger.i("stat scanning......");

            scanTimeout = timeout;
            scanTryCount = scanCount;

            doScanning();
        }

        @Override
        public void connectDevice(BluetoothDevice device) throws RemoteException {
            //得到选择的设备，进行连接
            mBluetoothGatt = device.connectGatt(BluetoothDeviceService.this, false, mGattCallback);

            mBluetoothGatt.connect();
            mBluetoothGatt.discoverServices();
        }

        @Override
        public void startMeasure() throws RemoteException {
            startBloodMeasure();
        }

        @Override
        public void stopMeasure() throws RemoteException {
            startBloodMeasure();
        }

        @Override
        public void obtainMeasureResult() throws RemoteException {

        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mHandler = new BluetoothHandler();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
        }
    }

    /**
     * 执行搜索，当搜索次数为0的时候，则停止搜索
     */
    private void doScanning() {
        if (scanTryCount > 0) {
            scanTryCount--;
        } else {
            return;
        }

        if (mBluetoothAdapter != null) {
            if (Build.VERSION.SDK_INT >= 21) {
                mScanner = mBluetoothAdapter.getBluetoothLeScanner();
                ArrayList<ScanFilter> filters = new ArrayList<>();
                filters.add(new ScanFilter.Builder().setServiceUuid(BLEGattAttributes.getSeryUuid()).build());
                mScanner.startScan(filters, new ScanSettings.Builder().build(), newScanCallBack());
            } else {
                mBluetoothAdapter.startLeScan(new UUID[]{BLEGattAttributes.BLOODPRESSURE_SERY_UUID}, mLeScanCallback);
            }
        }

        mHandler.sendEmptyMessageDelayed(MSG_STOP_SCAN, scanTimeout);
        logger.i("start scanning end");
    }

    /**
     * 搜索蓝牙设备回调
     */
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            Message msg = new Message();
            msg.what = MSG_SCANNING;
            msg.obj = device;

            mHandler.sendMessage(msg);
        }
    };

    @TargetApi(21)
    private ScanCallback newScanCallBack() {
        ScanCallback mScanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                logger.i("onScanResult");

                Message msg = new Message();
                msg.what = MSG_SCANNING;
                msg.obj = result.getDevice();

                mHandler.sendMessage(msg);
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
                logger.i("onBatchScanResults");
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
            }
        };

        return mScanCallback;
    }

    /**
     * 开始测量 / 中断测量
     * 调用见handler
     */
    public void startBloodMeasure() {
        logger.i("startBloodMeasure : " + bBloodMeasuring);
        if (bBloodMeasuring) {
            interruptBloodMeasure();
        } else {
            bloodPressureStart();
        }
    }

    /**
     * 开始测量血压
     */
    private void bloodPressureStart() {
        if (writeGattCharacteristic != null) {
            writeGattCharacteristic.setValue(BLEGattAttributes.blood_pressure_start); // 测量
            mBluetoothGatt.writeCharacteristic(writeGattCharacteristic);
        } else if (!bConnected) {
            mHandler.sendEmptyMessage(MSG_DEVICE_NONE_CONNECTED);
        }
    }

    /**
     * 开始获取电池电量
     */
    private void startGetDeviceBattery() {
        // 连接成功后开始读取电池电量，每隔2s读取一次
        if (writeGattCharacteristic != null) {
            logger.i("start read battery every 2s");
            timerBattery = new Timer();
            timerBattery.schedule(new BatteryTimerTask(), 0, 2000); //每隔2s写一次获取电池的命令
        } else {
            logger.w("writeGattCharacteristic is null");
        }
    }

    /**
     * 测量血压结束
     */
    private void stopReadBloodResult() {
        bBloodMeasuring = false;
        logger.i("stop measure and stop read blood result");
        if (timerBloodResult != null) {
            timerBloodResult.cancel();
            timerBloodResult = null;
        }
    }

    /**
     * 中断测量血压，该方法由客户端主动发起，在测量的过程中中断测量
     */
    private void interruptBloodMeasure() {
        logger.i("interrupt blood measure");
        if (timerBloodResult != null) {
            timerBloodResult.cancel();
            timerBloodResult = null;
        }
        writeGattCharacteristic.setValue(BLEGattAttributes.blood_pressure_interrupt); // 停止
        mBluetoothGatt.writeCharacteristic(writeGattCharacteristic);
    }

    /**
     * 将一个byte每一位输出一个string
     *
     * @param bit bit对象
     * @return 字符串
     */
    private String byteBitToString(byte bit) {
        byte[] array = new byte[8];
        StringBuffer buf = new StringBuffer();
        for (int i = 7; i >= 0; i--) {
            array[i] = (byte) (bit & 1);
            bit = (byte) (bit >> 1);
        }
        for (int i = 0; i < 8; i++) {
            buf.append(array[i]);
        }
        return buf.toString();
    }

    /**
     * 获取byte 的某一位
     *
     * @param paramByte byte对象
     * @param paramInt  位置
     * @return true:1   false:0
     */
    private boolean getByteBit(byte paramByte, int paramInt) {
        boolean[] arrayOfBoolean = new boolean[8];
        int i = 0;
        while (i < 8) {
            int j;
            if ((paramByte & 0x1) != 1) j = 0;
            else j = 1;
//            arrayOfBoolean[i] = j == 0 ? false : true;
            arrayOfBoolean[i] = j != 0;
            paramByte >>= 1;
//	    	Log.i(TAG, "byte bit:"+ j);
            i += 1;
        }
        return arrayOfBoolean[paramInt];
    }

    /**
     * 读取测量血压结果信息
     */
    class ReadBloodResultTask extends TimerTask {
        @Override
        public void run() {
//            logger.i("read measure pressure result");
            writeGattCharacteristic.setValue(BLEGattAttributes.blood_pressure_read_result); // 获取血压测量结果
            mBluetoothGatt.writeCharacteristic(writeGattCharacteristic);
        }
    }

    /**
     * 获取电池信息
     */
    class BatteryTimerTask extends TimerTask {
        @Override
        public void run() {
//            logger.i("conn_response:" + conn_response);
            if (conn_response < 1) {
                logger.i("disconnect");
                bConnected = false;
                mHandler.sendEmptyMessage(MSG_BLUETOOTH_DEVICE_DISCONNECT); //断开了连接
                this.cancel();
            }
            if (!bBloodMeasuring) { //正在测量的话，就不写读取电池指令
                conn_response--; //每一次检测 -1
                writeGattCharacteristic.setValue(BLEGattAttributes.device_battery); // 电池
                mBluetoothGatt.writeCharacteristic(writeGattCharacteristic);
            }
        }
    }

    /**
     * 蓝牙操作的回调
     */
    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            logger.i(descriptor.toString());
        }

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                logger.i("connected");
                logger.i("onConnectionStateChange --> find device service =" + mBluetoothGatt.discoverServices());
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                logger.i("disconnected");
                bConnected = false; //断开连接
                mHandler.sendEmptyMessage(MSG_BLUETOOTH_CONNECT_CHANGED);
            }
            //正在连接   正在断开连接
        }

        // 找到服务的回调方法
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                final List<BluetoothGattService> services = mBluetoothGatt.getServices();

                for (BluetoothGattService service : services) {
                    logger.i("service uuid : " + service.getUuid());
                    /*
                    logger.i("local uuid : " + BLEGattAttributes.BLOODPRESSURE_SERY_UUID.toString());
                    logger.i("local temp uuid : " + BLEGattAttributes.BLOOPRESSURE_SERY_TEMP_UUID.toString());
                    */
                    if (service.getUuid().equals(BLEGattAttributes.BLOODPRESSURE_SERY_UUID)) { //得到了可以发送指令的服务UUID
                        bConnected = true; //连接成功

                        readGattCharacteristic = service.getCharacteristic(BLEGattAttributes.BLOODPRESSURE_READ_UUID);
                        logger.i("readGattCharacteristic：" + readGattCharacteristic.getUuid().toString());

                        writeGattCharacteristic = service.getCharacteristic(BLEGattAttributes.BLOODPRESSURE_WRITE_UUID);
                        logger.i("writeGattCharacteristic：" + writeGattCharacteristic.getUuid().toString());

                        BluetoothGattCharacteristic notifyGattCharacteristic = service.getCharacteristic(BLEGattAttributes.BLOODPRESSURE_NOTIFY_UUID);
                        logger.i("BluetoothGattCharacteristic：" + notifyGattCharacteristic.getUuid().toString());

                        mHandler.sendEmptyMessage(MSG_BLUETOOTH_CONNECT_SUCCESS);
                    }
                }

                if (bConnected) {
                    logger.i("conn_response task start");
                }
            }
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            logger.i("onReliableWriteCompleted -->" + status);
        }

        // 读取Characteristic 信息的 回调方法
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            byte[] data = characteristic.getValue();
//            logger.i("data length:" + data.length);

//            for (int i = 0; i < data.length; i++) {
//                logger.i("读取的数据是:" + i + "=" + data[i] + "binnary:" + byteBitToString(data[i]));
//            }

            if (status == BluetoothGatt.GATT_SUCCESS) {
                conn_response = 3; //重置3次
                if (characteristic.getUuid().equals(BLEGattAttributes.BLOODPRESSURE_READ_UUID)) {
//                    logger.i("read:" + "READ_UUID");
                    if (data.length > 0 && data[0] == -6) {
                        bluetoothDataRead(data);
                    }
                }
            }
        }

        // 写入数据后  回调
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
//            logger.i("onCharacteristicWrite()");
            mBluetoothGatt.readCharacteristic(readGattCharacteristic);
        }
    };

    /**
     * 处理蓝牙返回来的数据
     */
    private void bluetoothDataRead(byte[] data) {
        switch (data[1]) {
            case 1: //设备信息
                break;
            case 2: //电池
                battery_electry = data[2];
                mHandler.sendEmptyMessage(MSG_BLUETOOTH_DEVICE_BATTERY);
                break;

            case 32: //发指令开始测量，停止测量
                if (data[2] == 0) {
                    //测量
                    if (getByteBit(data[3], 7)) {
                        //正在测量
                        mHandler.sendEmptyMessage(MSG_BLOOD_PRESSURE_START);
                    } else {
                        //没有测量 结束
                        logger.i("none measure，finish");
                    }
                } else {
                    //停止测量
                    mHandler.sendEmptyMessage(MSG_BLOOD_MEASURE_INTERRUPTED);
                }
                break;

            case 33: //读取到的测量血压结果
                int error = data[2] & 0x3F;
                if (error != 0) {
                    switch (error) {
                        case 3:
                        case 4:
                        case 5:
                        case 16:
                        case 17:
                        case 18:
                        default:
                            msg_error = "测量过程未知错误";
                            break;
                        case 2:
                            msg_error = "血压模块自检错误，可能是传感器或A/D错误";
                            break;
                        case 6:
                            msg_error = "袖带松或者未连接袖带";
                            break;
                        case 7:
                            msg_error = "漏气（阀门等处）";
                            break;
                        case 8:
                            msg_error = "气压错误（可能是阀门没有正常打开）";
                            break;
                        case 9:
                            msg_error = "弱信号（可能是袖带太松等, 可能是血压模块自检错误，也可能是传感器错误）";
                            break;
                        case 10:
                            msg_error = "超范围 （测量对象超过设备测量范围）";
                            break;
                        case 11:
                            msg_error = "过分运动（有信号干扰等）";
                            break;
                        case 12:
                            msg_error = "过压";
                            break;
                        case 13:
                            msg_error = "信号饱和";
                            break;
                        case 14:
                            msg_error = "漏气";
                            break;
                        case 15:
                            msg_error = "系统错误";
                            break;
                        case 19:
                            msg_error = "测量超时";
                    }

                    mHandler.sendEmptyMessage(MSG_BLOOD_MEASURE_ERROR);
                    break; //跳出
                }

                if (getByteBit(data[2], 7)) {
                    //正在测量
//                    logger.i("正在测量");
                } else {
                    //测量结束
                    if (bBloodMeasuring) {//正在测量 - 测量结束了
                        logger.i("measure end");
                        systolicPressure = ((0xFF & data[5]) << 8) + (0xFF & data[6]); //收缩压
                        diastolicPressure = ((0xFF & data[7]) << 8) + (0xFF & data[8]); //舒张压压
                        heartRate = ((0xFF & data[9]) << 8) + (0xFF & data[10]); //心率

                        mHandler.sendEmptyMessage(MSG_BLOOD_PRESSURE_MEASURE_END);
                    }
                }
                if (getByteBit(data[2], 6)) {
                    //测量结果有效
                    logger.i("measure result is true,  last read measure result again");
                    try {
                        timerBloodResult.cancel();
                        timerBloodResult.schedule(new ReadBloodResultTask(), 0); //最后一次读取测量结果
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    //测量结果无效 - 显示臂带的压力
                    shoulderPressure = ((0xFF & data[3]) << 8) + (0xFF & data[4]);
                    mHandler.sendEmptyMessage(MSG_BLOOD_SHOULDER_PRESSURE);
                }
                break;

            default:
                break;
        }
    }
}