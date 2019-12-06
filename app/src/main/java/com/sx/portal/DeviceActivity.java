package com.sx.portal;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;

import com.javier.simplemvc.patterns.notify.NotifyMessage;
import com.javier.simplemvc.patterns.view.SimpleActivity;
import com.sx.portal.adapter.MyDeviceAdapter;
import com.sx.portal.entity.DeviceEntity;

import java.util.ArrayList;

/**
 * 显示设备列表
 */
public class DeviceActivity extends SimpleActivity implements View.OnClickListener {

    private ImageButton mDeviceBack;
    private ListView mDeviceList;
    private MyDeviceAdapter myDeviceAdapter;

    private BluetoothDevice mDevice;
    private DeviceEntity entity;
    private ArrayList<DeviceEntity> entities = new ArrayList<DeviceEntity>();

    private boolean read = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
    }


    @Override
    public void onInitView() {
        mDeviceBack = (ImageButton) findViewById(R.id.device_back);
        mDeviceList = (ListView) findViewById(R.id.my_device_list);

        myDeviceAdapter = new MyDeviceAdapter(this, entities);
        mDeviceList.setAdapter(myDeviceAdapter);
    }

    @Override
    public void onSetEventListener() {
        mDeviceBack.setOnClickListener(this);
    }

    @Override
    public void onInitComplete() {
        mDevice = getIntent().getParcelableExtra("bluetooth_device");
    }

    @Override
    public void onRegister() {
        super.onRegister();

        notifyManager.sendNotifyMessage(MsgConstants.MSG_BIND_BLUETOOTH_SERVICE, this);
    }

    @Override
    public void onRemove() {
        super.onRemove();

        notifyManager.sendNotifyMessage(MsgConstants.MSG_UNBIND_BLUETOOTH_SERVICE, this);
    }

    @Override
    public String[] listMessage() {
        return new String[]{
                MsgConstants.MSG_BIND_BLUETOOTH_SERVICE_COMPLETE,
                MsgConstants.MSG_BATTERY,
                MsgConstants.MSG_DISCONNECT,
                MsgConstants.MSG_CONNECTED

        };
    }

    @Override
    public void handlerMessage(NotifyMessage message) {
        super.handlerMessage(message);

        switch (message.getName()) {
            case MsgConstants.MSG_BIND_BLUETOOTH_SERVICE_COMPLETE:
                logger.i("bind bluetooth service complete");
                break;
            case MsgConstants.MSG_BATTERY:
                if (read) {
                    read = false;

                    entity = new DeviceEntity();
                    entity.setBattery(Integer.parseInt(message.getParam().toString()));
                    entity.setConnectStatus(true);
                    entity.setmBluetoothDevice(mDevice);
                    entities.add(entity);

                    myDeviceAdapter.notifyDataSetChanged();
                }
                break;
            case MsgConstants.MSG_DISCONNECT:
                if (entity != null) {
                    entity.setConnectStatus(false);

                    myDeviceAdapter.notifyDataSetChanged();
                }
                break;
        }
    }

    @Override
    public void onClick(View view) {
        finish();
    }
}