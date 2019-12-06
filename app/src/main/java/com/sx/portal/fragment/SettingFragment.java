package com.sx.portal.fragment;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.javier.simplemvc.patterns.view.SimpleFragment;
import com.sx.portal.AboutActivity;
import com.sx.portal.DeviceActivity;
import com.sx.portal.FamilyMemberActivity;
import com.sx.portal.MainActivity;
import com.sx.portal.R;

/**
 * Created by Administrator on 2016/1/10.
 * <p>
 * 设置页面
 */
public class SettingFragment extends SimpleFragment<MainActivity> implements View.OnClickListener {

    private TextView mMember, mDevice, mAbout;

    private BluetoothDevice bluetoothDevice;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }

    @Override
    public void onInitView() {
        mMember = (TextView) findViewById(R.id.item_family_member);
        mDevice = (TextView) findViewById(R.id.item_my_device);
        mAbout = (TextView) findViewById(R.id.item_about);
    }

    @Override
    public void onSetEventListener() {
        mMember.setOnClickListener(this);
        mDevice.setOnClickListener(this);
        mAbout.setOnClickListener(this);
    }

    @Override
    public void onInitComplete() {
        if (getArguments() != null) {
            bluetoothDevice = getArguments().getParcelable("connect_bluetooth_device");
        }
    }

    @Override
    public void onClick(View view) {
        Intent intent = null;

        if (view == mDevice) {
            intent = new Intent(getActivity(), DeviceActivity.class);
            intent.putExtra("bluetooth_device", bluetoothDevice);
        } else if (view == mMember) {
            intent = new Intent(getActivity(), FamilyMemberActivity.class);
        } else if (view == mAbout) {
            intent = new Intent(getActivity(), AboutActivity.class);
        }

        startActivity(intent);
    }
}