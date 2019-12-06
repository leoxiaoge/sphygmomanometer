package com.sx.portal.fragment;

import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.javier.simplemvc.patterns.notify.NotifyMessage;
import com.javier.simplemvc.patterns.view.SimpleFragment;
import com.sx.portal.MainActivity;
import com.sx.portal.MsgConstants;
import com.sx.portal.R;
import com.sx.portal.adapter.MemberPagerAdapter;
import com.sx.portal.entity.DeviceEntity;
import com.sx.portal.entity.MemberEntity;
import com.sx.portal.plugin.clipViewPager.ClipViewPager;
import com.sx.portal.plugin.clipViewPager.ScalePageTransformer;
import com.sx.portal.util.DialogBuild;
import com.sx.portal.util.PermissionUtil;

import java.util.ArrayList;
import java.util.HashMap;

public class MeasureFragment extends SimpleFragment<MainActivity> implements View.OnClickListener {

    private RelativeLayout mPagerContainer;
    private LinearLayout mMeasuringPressure;
    private ClipViewPager mViewPager;
    private MemberPagerAdapter mPagerAdapter;
    private TextView mMeasuring, mSbp, mdbp, mHeartRate, mConnStatus, mBattery;
    private Button mStartMeasure;

    // 默认需要测量的用户
    private MemberEntity mDefaultMemberEntity;

    // 用户列表
    private ArrayList<MemberEntity> mMemberEntitys;

    // 需要连接的蓝牙设备对象
    private BluetoothDevice mDevice;

    // 是否已连接设备
    private boolean bConnect = false;

    // 是否已经开始测量
    private boolean bStartMeasure = false;

    // 显示用户的viewpager应该显示第几页
    private int currentPage = 0;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mCacheView == null) {
            return inflater.inflate(R.layout.fragment_measure, container, false);
        } else {
            return mCacheView;
        }
    }

    @Override
    protected void onFragmentStateRestored(Bundle savedInstanceState) {
        super.onFragmentStateRestored(savedInstanceState);

        if (savedInstanceState != null) {
            bConnect = savedInstanceState.getBoolean("bConnect");
            bStartMeasure = savedInstanceState.getBoolean("bStartMeasure");
            mDefaultMemberEntity = savedInstanceState.getParcelable("mDefaultMemberEntity");
            currentPage = savedInstanceState.getInt("currentPage");
        }
    }


    @Override
    public void onInitView() {
        mConnStatus = (TextView) findViewById(R.id.device_conn_status);
        mBattery = (TextView) findViewById(R.id.device_battery);

        mPagerContainer = (RelativeLayout) findViewById(R.id.page_container);
        mMeasuringPressure = (LinearLayout) findViewById(R.id.measuring_pressure);

        mViewPager = (ClipViewPager) findViewById(R.id.member_pager);
        mViewPager.setPageTransformer(true, new ScalePageTransformer());

        mStartMeasure = (Button) findViewById(R.id.start_measure);
        mMeasuring = (TextView) findViewById(R.id.measure_by);

        mSbp = (TextView) findViewById(R.id.sbp);
        mdbp = (TextView) findViewById(R.id.dbp);
        mHeartRate = (TextView) findViewById(R.id.heartRate);

        mPagerAdapter = new MemberPagerAdapter(getActivity());
        mViewPager.setAdapter(mPagerAdapter);
    }

    @Override
    public void onSetEventListener() {
        mPagerContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mViewPager.dispatchTouchEvent(event);
            }
        });

        mViewPager.setOnPageChangeListener(pageChangeListener);

        mStartMeasure.setOnClickListener(this);
    }

    @Override
    public void onInitComplete() {
        if (getArguments() != null) {
            mDefaultMemberEntity = getArguments().getParcelable(
                    "default_measure_member");
            mDevice = getArguments().getParcelable("connect_bluetooth_device");

            boolean isConnect = getArguments().getBoolean("connected");

            connectChange(isConnect);
        }
    }

    @Override
    public void onRegister() {
        super.onRegister();

        // 读取所有家庭成员列表
        notifyManager.sendNotifyMessage(MsgConstants.MSG_READ_ALL_MEMBERS);
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
                MsgConstants.MSG_READ_ALL_MEMBERS_COMPLETE,
                MsgConstants.MSG_BIND_BLUETOOTH_SERVICE_COMPLETE,
                MsgConstants.MSG_MEASURE_STARTED,
                MsgConstants.MSG_SCANNING_DEVICE_MORE,
                MsgConstants.MSG_BLUETOOTH_NOT_FOUND,
                MsgConstants.MSG_DISCONNECT,
                MsgConstants.MSG_CONNECTED,
                MsgConstants.MSG_SHOULD_PRESSURE,
                MsgConstants.MSG_MEASURE_END,
                MsgConstants.MSG_MEASURE_INTERRUPTED,
                MsgConstants.MSG_ON_ERROR,
                MsgConstants.MSG_BATTERY
        };
    }

    @Override
    public void handlerMessage(NotifyMessage message) {
        super.handlerMessage(message);

        switch (message.getName()) {
            case MsgConstants.MSG_READ_ALL_MEMBERS_COMPLETE:
                // 读取所有用户返回
                readAllMemberComplete(message.getList());
                break;
            case MsgConstants.MSG_BIND_BLUETOOTH_SERVICE_COMPLETE:
                // 绑定蓝牙服务成功
                logger.i("bind bluetooth service complete.");
                break;
            case MsgConstants.MSG_SCANNING_DEVICE_MORE:
                // 搜索到多个设备的时候调用(搜索到1个设备的时候，自动连接)
                onScanMore(message.getList());
                break;
            case MsgConstants.MSG_BLUETOOTH_NOT_FOUND:
                // 未搜索到设备
                Toast.makeText(getActivity(), R.string.scan_err, Toast.LENGTH_SHORT).show();
                break;
            case MsgConstants.MSG_CONNECTED:
                // 连接成功
                onConnected();
                break;
            case MsgConstants.MSG_DISCONNECT:
                // 连接失败
                onDisconnection();
                break;
            case MsgConstants.MSG_MEASURE_STARTED:
                if (mMeasuringPressure.getVisibility() != View.VISIBLE) {
                    mMeasuringPressure.setVisibility(View.VISIBLE);
                    mPagerContainer.setVisibility(View.GONE);
                }

                bStartMeasure = true;

                mStartMeasure.setText(R.string.stop_measure);
                break;
            case MsgConstants.MSG_SHOULD_PRESSURE:
                mMeasuring.setText(message.getParam().toString());
                break;

            case MsgConstants.MSG_MEASURE_END:
                if (mMeasuringPressure.getVisibility() != View.GONE) {
                    mMeasuringPressure.setVisibility(View.GONE);
                    mPagerContainer.setVisibility(View.VISIBLE);
                }

                bStartMeasure = false;

                mdbp.setText(message.getBundle().getString("diastolic"));
                mSbp.setText(message.getBundle().getString("systolic"));
                mHeartRate.setText(message.getBundle().getString("heart_rate"));

                mStartMeasure.setText(R.string.start_measure);
                break;

            case MsgConstants.MSG_MEASURE_INTERRUPTED:
                if (mMeasuringPressure.getVisibility() != View.GONE) {
                    mMeasuringPressure.setVisibility(View.GONE);
                    mPagerContainer.setVisibility(View.VISIBLE);
                }

                bStartMeasure = false;

                mStartMeasure.setText(R.string.start_measure);
                break;
            case MsgConstants.MSG_ON_ERROR:
                if (mMeasuringPressure.getVisibility() != View.GONE) {
                    mMeasuringPressure.setVisibility(View.GONE);
                    mPagerContainer.setVisibility(View.VISIBLE);
                }

                bStartMeasure = false;

                Toast.makeText(getActivity(), message.getParam().toString(), Toast.LENGTH_LONG).show();
                break;
            case MsgConstants.MSG_BATTERY:
                connectChange(true);
                mBattery.setText(getString(R.string.device_battery) + String.valueOf(message.getParam()) + "%");
                break;
        }
    }

    /**
     * 读取所有用户信息返回
     */
    private void readAllMemberComplete(ArrayList<MemberEntity> memberEntities) {
        this.mMemberEntitys = memberEntities;

        if (mDefaultMemberEntity == null) {
            logger.i("default measure member is null!");

            mDefaultMemberEntity = mMemberEntitys.get(0);
        } else {
            logger.i("default measure member id is : " + mDefaultMemberEntity.getId());

            if (currentPage == 0) {
                for (int i = 0; i < mMemberEntitys.size(); i++) {
                    if (mMemberEntitys.get(i).getId() == mDefaultMemberEntity.getId()) {
                        currentPage = i;
                    }
                }
            } else {
                mDefaultMemberEntity = mMemberEntitys.get(currentPage);
            }
        }

        mViewPager.setCurrentItem(currentPage);
        mViewPager.setOffscreenPageLimit(mMemberEntitys.size());

        mPagerAdapter.addAll(mMemberEntitys);
    }

    // 当选择某一个用户后执行
    private ViewPager.OnPageChangeListener pageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            if (mMemberEntitys != null && mMemberEntitys.size() > 0) {
                mDefaultMemberEntity = mMemberEntitys.get(position);

                findActivity().setCurrentSelectedMember(mDefaultMemberEntity);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    /**
     * 当点击开始测量按钮，如果设备未连接，弹出提示，提示用户重新搜索并且选择设备连接，连接成功后开始测量
     *
     * @param view 点击的按钮
     */
    @Override
    public void onClick(View view) {
        if (!bConnect) {
            // 弹出提示，提示用户重新搜索并连接设备
            alertWhenDeviceDisconnect();
            return;
        }

        // 如果还没有开始测量，则发送测量指令
        if (!bStartMeasure) {
            savedMeasureMember(mDefaultMemberEntity.getId());
            notifyManager.sendNotifyMessage(MsgConstants.MSG_START_MEASURE, mDefaultMemberEntity.getId());
            return;
        }

        // 如果已经开始测试，并且按钮显示内容为"停止测量"，则发送停止测量指令
        if (bStartMeasure && mStartMeasure.getText().toString().equalsIgnoreCase(getString(R.string.stop_measure))) {
            notifyManager.sendNotifyMessage(MsgConstants.MSG_INTERRUPTED_MEASURE);
        }
    }

    /**
     * 设备连接改变
     *
     * @param connect 连接状态
     */
    private void connectChange(boolean connect) {
        bConnect = connect;

        if (!bConnect) {
            mConnStatus.setText(R.string.disconnect);
            mBattery.setText("");
        } else {
            mConnStatus.setText(R.string.connected);
        }
    }

    /**
     * 保存当前测量的用户
     */
    private void savedMeasureMember(int mid) {
        SharedPreferences preferences = getContext().getSharedPreferences("sx_setting", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("last_member_id", mid);
        editor.commit();
    }

    /**
     * 当设备未连接的时候，弹出提示
     */
    private void alertWhenDeviceDisconnect() {
        DialogBuild dialogBuild = DialogBuild.getBuild();
        dialogBuild.createConfirmDialog(getActivity(), getString(R.string.dev_disconnect), new DialogBuild.OnConfirmListener() {
            @Override
            public void onConfirm(Dialog dialog, boolean isConfirm) {
                if (isConfirm) {
                    mConnStatus.setText(R.string.scanning_again);
                    boolean mayRequest = PermissionUtil.mayRequestLocation(MeasureFragment.this);

                    if (!mayRequest) {
                        // 开始搜索设备
                        notifyManager.sendNotifyMessage(MsgConstants.MSG_SCANNING_DEVICE);
                    }
                }

                dialog.dismiss();
            }
        }).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case 0:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // The requested permission is granted.
                    notifyManager.sendNotifyMessage(MsgConstants.MSG_SCANNING_DEVICE);
                } else {
                    // The user disallowed the requested permission.
                    logger.w("user disallowed the requested permission");
                    Toast.makeText(getContext(), "定位服务已被禁用，无法搜索设备", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private void onConnected() {
        connectChange(true);

        if (bStartMeasure) {
            notifyManager.sendNotifyMessage(MsgConstants.MSG_START_MEASURE);
        }
    }

    /**
     * 设备连接已断开
     */
    private void onDisconnection() {
        connectChange(false);

        if (bStartMeasure) {
            if (mMeasuringPressure.getVisibility() != View.GONE) {
                mMeasuringPressure.setVisibility(View.GONE);
                mPagerContainer.setVisibility(View.VISIBLE);
            }

            bStartMeasure = false;
        }
    }

    /**
     * 当搜索到多个设备的时候调用
     * 当搜索到多个设备以后，需要弹出一个选择框
     *
     * @param deviceEntities 搜索到的设备列表
     */
    private void onScanMore(final ArrayList<DeviceEntity> deviceEntities) {
        ArrayList<HashMap<String, String>> selValue = new ArrayList<>();

        for (int i = 0; i < deviceEntities.size(); i++) {
            DeviceEntity d = deviceEntities.get(i);

            HashMap<String, String> map = new HashMap<>();
            if (d.getName() == null || d.getName().equals("")) {
                map.put("name", getString(R.string.unknown_device));
            } else {
                map.put("name", d.getName());
            }
            map.put("address", d.getAddress());
            selValue.add(map);
        }

        Dialog dialog = DialogBuild.getBuild().createListDialog(getActivity(), getString(R.string.pls_select_device), selValue, false, new DialogBuild.OnListMenuSelect() {
            @Override
            public void onMenuSelect(AdapterView<?> arg0, View arg1, int arg2, long arg3, Dialog dialog) {
                dialog.dismiss();

                notifyManager.sendNotifyMessage(MsgConstants.MSG_CONNECT_DEVICE, deviceEntities.get(arg2).getmBluetoothDevice());
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onExit() {

            }

            @Override
            public void onGoto() {

            }
        });
        dialog.show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("bConnect", bConnect);
        outState.putBoolean("bStartMeasure", bStartMeasure);
        outState.putParcelable("mDefaultMemberEntity", mDefaultMemberEntity);
        outState.putInt("currentPage", currentPage);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        Bundle outState = getArguments();
        if (outState != null) {
            outState.putBoolean("bConnect", bConnect);
            outState.putBoolean("bStartMeasure", bStartMeasure);
            outState.putParcelable("mDefaultMemberEntity", mDefaultMemberEntity);
            outState.putInt("currentPage", currentPage);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}