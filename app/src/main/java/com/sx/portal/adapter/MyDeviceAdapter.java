package com.sx.portal.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sx.portal.R;
import com.sx.portal.entity.DeviceEntity;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/1/17.
 * <p/>
 * 显示我的设备的列表项
 */
public class MyDeviceAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<DeviceEntity> myDevices;

    private LayoutInflater mInflator;

    public MyDeviceAdapter(Context context, ArrayList<DeviceEntity> devices) {
        this.mContext = context;
        this.myDevices = devices;

        mInflator = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return myDevices.size();
    }

    @Override
    public Object getItem(int position) {
        return myDevices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;

        if (convertView == null) {
            holder = new ViewHolder();

            convertView = mInflator.inflate(R.layout.adapter_my_device, null);

            holder.deviceName = (TextView) convertView.findViewById(R.id.device_name);
            holder.deviceAddress = (TextView) convertView.findViewById(R.id.device_address);
            holder.connStatus = (TextView) convertView.findViewById(R.id.connect_status);
            holder.battery = (TextView) convertView.findViewById(R.id.device_battery);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        DeviceEntity e = myDevices.get(position);

        if (e.getmBluetoothDevice() != null) {
            holder.deviceName.setText(e.getmBluetoothDevice().getName());
            holder.deviceAddress.setText(e.getmBluetoothDevice().getAddress());
        }

        holder.connStatus.setText(e.isConnectStatus()? mContext.getString(R.string.connected):mContext.getString(R.string.disconnect));
        holder.battery.setText(String.valueOf(e.getBattery()) + "%");

        return convertView;
    }

    class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
        TextView connStatus;
        TextView battery;
    }
}