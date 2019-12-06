package com.sx.portal.util;

import android.os.ParcelUuid;

import java.util.UUID;

/**
 * This class includes a subset of standard GATT attributes for demonstration purposes.
 * <p>@author and </p>
 * <p>@version 1 </p>
 * <p>data: 2015-8-27 </p>
 */
public class BLEGattAttributes {

	public static final UUID BLOODPRESSURE_SERY_UUID = UUID.fromString("00001810-0000-1000-8000-00805f9b34fb"); //服务的UUID
	
	public static final UUID BLOODPRESSURE_WRITE_UUID = UUID.fromString("00002a50-0000-1000-8000-00805f9b34fb"); //接受手机命令请求的ID
	public static final UUID BLOODPRESSURE_READ_UUID = UUID.fromString("00002a51-0000-1000-8000-00805f9b34fb"); //发送手机命令应答的ID
	public static final UUID BLOODPRESSURE_NOTIFY_UUID = UUID.fromString("00002a52-0000-1000-8000-00805f9b34fb"); //通知发送测量结果数据
	
	public static final byte[] device_battery = new byte[]{(byte) 0xFA, 0x02, (byte) -0xFC, (byte) 0xFA}; // 电池
	public static final byte[] device_message = new byte[]{(byte) 0xFA, 0x01, 0x05, (byte) 0xFA}; //设备
	public static final byte[] blood_pressure_start = new byte[]{(byte) 0xFA, 0x20, 0x00, (byte) 0xE6, (byte) 0xFA}; // 测量血压
	public static final byte[] blood_pressure_interrupt = new byte[]{(byte) 0xFA, 0x20, 0x01, (byte) 0xE5, (byte) 0xFA}; // 打断正在测量血压
	public static final byte[] blood_pressure_read_result = new byte[]{(byte) 0xFA, 0x21, (byte) 0xE5, (byte) 0xFA}; // 读取测量血压的结果
	
	public static ParcelUuid getSeryUuid() {
		return new ParcelUuid(BLOODPRESSURE_SERY_UUID);
	}
}