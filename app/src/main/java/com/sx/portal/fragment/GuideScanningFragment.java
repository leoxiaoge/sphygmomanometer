package com.sx.portal.fragment;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.javier.simplemvc.patterns.notify.NotifyMessage;
import com.javier.simplemvc.patterns.view.SimpleFragment;
import com.sx.portal.GuideActivity;
import com.sx.portal.MainActivity;
import com.sx.portal.MsgConstants;
import com.sx.portal.R;
import com.sx.portal.adapter.LeDeviceListAdapter;
import com.sx.portal.entity.DeviceEntity;

import java.util.ArrayList;

/**
 * author:Javier
 * time:2016/4/11.
 * mail:38244704@qq.com
 */
public class GuideScanningFragment extends SimpleFragment<GuideActivity> implements AdapterView.OnItemClickListener, View.OnClickListener {

    private LinearLayout mScanningLayout;
    private LinearLayout mScanningAgainLayout;
    private ImageView mScanWaiting;
    private ListView mScanDeviceList;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private Button mTryAgain, mNextTime;
    private TextView mDeviceConnectStatus;

    private AnimationDrawable mAnimationDrawable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_guide_scan, container, false);
    }

    @Override
    public void onInitView() {
        mScanWaiting = (ImageView) findViewById(R.id.guide_scan_waiting);
        mAnimationDrawable = (AnimationDrawable) mScanWaiting.getBackground();
        mScanningLayout = (LinearLayout) findViewById(R.id.guide_scaning_layout);
        mScanningAgainLayout = (LinearLayout) findViewById(R.id.try_again_layout);
        mTryAgain = (Button) findViewById(R.id.guide_scan_again);
        mNextTime = (Button) findViewById(R.id.guide_next_time);
        mDeviceConnectStatus = (TextView) findViewById(R.id.device_status_tip);

        if (mAnimationDrawable != null) {
            mAnimationDrawable.start();
        }

        mScanDeviceList = (ListView) findViewById(R.id.guide_scan_device_list);
    }

    @Override
    public void onSetEventListener() {
        mScanDeviceList.setOnItemClickListener(this);
        mTryAgain.setOnClickListener(this);
        mNextTime.setOnClickListener(this);
    }

    @Override
    public void onInitComplete() {
        // 写入非第一次登陆标志位
//        SharedPreferences pref = getActivity().getSharedPreferences("portal_setting", Context.MODE_PRIVATE);
//        SharedPreferences.Editor editor = pref.edit();
//        editor.putBoolean("first_running", false);
//        editor.commit();
        // 绑定蓝牙服务
        notifyManager.sendNotifyMessage(MsgConstants.MSG_BIND_BLUETOOTH_SERVICE, getContext());
    }

    @Override
    public void onRemove() {
        super.onRemove();

        notifyManager.sendNotifyMessage(MsgConstants.MSG_UNBIND_BLUETOOTH_SERVICE, getContext());
    }

    @Override
    public String[] listMessage() {
        return new String[]{
                MsgConstants.MSG_BIND_BLUETOOTH_SERVICE_COMPLETE,
                MsgConstants.MSG_SCANNING_DEVICE_MORE,
                MsgConstants.MSG_BLUETOOTH_NOT_FOUND,
                MsgConstants.MSG_CONNECTED,
                MsgConstants.MSG_DISCONNECT
        };
    }

    @Override
    public void handlerMessage(NotifyMessage message) {
        switch (message.getName()) {
            case MsgConstants.MSG_BIND_BLUETOOTH_SERVICE_COMPLETE:
                // 绑定成功，开始搜索设备
                notifyManager.sendNotifyMessage(MsgConstants.MSG_SCANNING_DEVICE);
                break;
            case MsgConstants.MSG_BLUETOOTH_NOT_FOUND:
                if (mAnimationDrawable != null) {
                    mAnimationDrawable.stop();
                }

                // 未搜索到设备
                mScanningAgainLayout.setVisibility(View.VISIBLE);
                mScanningLayout.setVisibility(View.GONE);
                break;
            case MsgConstants.MSG_SCANNING_DEVICE_MORE:
                if (mAnimationDrawable != null) {
                    mAnimationDrawable.stop();
                }

                // 搜索到多个设备
                onScanMore((ArrayList<DeviceEntity>) message.getList());
                break;
            case MsgConstants.MSG_CONNECTED:
                if (mAnimationDrawable != null) {
                    mAnimationDrawable.stop();
                }

                onGotoMain((BluetoothDevice) message.getParam());
                break;
            case MsgConstants.MSG_DISCONNECT:
                if (mAnimationDrawable != null) {
                    mAnimationDrawable.stop();
                }

                onGotoMain(null);
                break;
        }
    }

    /**
     * 当搜索到多个设备的时候调用
     * 当搜索到多个设备以后，需要弹出一个选择框
     *
     * @param deviceEntities 搜索到的设备列表
     */
    private void onScanMore(ArrayList<DeviceEntity> deviceEntities) {
        mLeDeviceListAdapter = new LeDeviceListAdapter(getActivity(), deviceEntities);
        mScanDeviceList.setAdapter(mLeDeviceListAdapter);
        mScanDeviceList.setVisibility(View.VISIBLE);
        mScanningLayout.setVisibility(View.GONE);
    }

    private void onGotoMain(BluetoothDevice device) {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(MainActivity.EXTRAS_DEVICE_NAME, device);
        intent.putExtra(MainActivity.EXTRAS_DEVICE_BUNDLE, bundle);
        intent.putExtra("connected", device == null);
        startActivity(intent);
        getActivity().finish();
    }

    @Override
    public void onClick(View v) {
        if (v == mTryAgain) {
            // 重新搜索
            mScanningAgainLayout.setVisibility(View.GONE);
            if (mAnimationDrawable != null) {
                mAnimationDrawable.start();
            }
            mScanningLayout.setVisibility(View.VISIBLE);
            logger.i("start scan device again!");
            notifyManager.sendNotifyMessage(MsgConstants.MSG_SCANNING_DEVICE);

        } else if (v == mNextTime) {
            onGotoMain(null);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mDeviceConnectStatus.setText(R.string.scan_tip3);
        DeviceEntity deviceEntity = mLeDeviceListAdapter.getDevice(position);
        notifyManager.sendNotifyMessage(MsgConstants.MSG_CONNECT_DEVICE, deviceEntity.getmBluetoothDevice());
    }
}