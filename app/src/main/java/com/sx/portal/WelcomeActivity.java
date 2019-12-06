package com.sx.portal;

import android.Manifest;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Toast;

import com.javier.simplemvc.patterns.notify.NotifyMessage;
import com.javier.simplemvc.patterns.view.SimpleActivity;
import com.javier.simplemvc.util.Logger;
import com.sx.portal.entity.DeviceEntity;
import com.sx.portal.entity.MemberEntity;
import com.sx.portal.util.Constants;
import com.sx.portal.util.DialogBuild;
import com.sx.portal.util.PermissionUtil;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * author:Javier
 * time:2016/4/11.
 * mail:38244704@qq.com
 */
@SuppressWarnings("unchecked")
public class WelcomeActivity extends SimpleActivity {

    /**
     * 执行等待的动画
     */
    private AnimationDrawable animationDrawable;

    private MemberEntity mDefaultMeasureMember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
    }

    @Override
    public void onInitView() {
        ImageView mLoading = (ImageView) findViewById(R.id.waiting);

        if (mLoading != null) {
            animationDrawable = (AnimationDrawable) mLoading.getBackground();
            animationDrawable.start();
        }
    }

    @Override
    public void onSetEventListener() {

    }

    @Override
    public void onInitComplete() {
    }

    @Override
    public void onRegister() {
        super.onRegister();
        notifyManager.sendNotifyMessage(MsgConstants.MSG_BIND_BLUETOOTH_SERVICE, this);
    }

    @Override
    public void onRemove() {
        super.onRemove();
        // 解除绑定
        notifyManager.sendNotifyMessage(MsgConstants.MSG_UNBIND_BLUETOOTH_SERVICE, this);
    }

    @Override
    public String[] listMessage() {
        return new String[]{
                MsgConstants.MSG_READ_ALL_MEMBERS_COMPLETE,
                MsgConstants.MSG_READ_ALL_MEMBERS_FAILED,
                MsgConstants.MSG_BIND_BLUETOOTH_SERVICE_COMPLETE,
                MsgConstants.MSG_BLUETOOTH_DISABLE,
                MsgConstants.MSG_BLUETOOTH_ENABLE,
                MsgConstants.MSG_BLUETOOTH_NOT_FOUND,
                MsgConstants.MSG_SCANNING_DEVICE_MORE,
                MsgConstants.MSG_DISCONNECT,
                MsgConstants.MSG_CONNECTED
        };
    }

    @Override
    public void handlerMessage(NotifyMessage notifyMessage) {
        switch (notifyMessage.getName()) {
            case MsgConstants.MSG_BIND_BLUETOOTH_SERVICE_COMPLETE:
                logger.i("on bind bluetooth server complete.");
                // 绑定蓝牙服务成功，检测蓝牙状态。 检测蓝牙状态完成后，自动启动设备搜索
                notifyManager.sendNotifyMessage(MsgConstants.MSG_CHECK_BLUETOOTH_STATUS);
                break;

            case MsgConstants.MSG_BLUETOOTH_DISABLE:
                logger.i("bluetooth is disable , open it!");
                // 如果蓝牙设备未打开，跳转到打开蓝牙界面，打开蓝牙通过startActivityResult的方式，onActivityResult方法在activity中实现
                Intent mIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(mIntent, 1);
                break;

            case MsgConstants.MSG_BLUETOOTH_ENABLE:
                // 蓝牙设备可用
                notifyManager.sendNotifyMessage(MsgConstants.MSG_READ_ALL_MEMBERS);
                break;

            case MsgConstants.MSG_READ_ALL_MEMBERS_COMPLETE:
                logger.i("read all member complete");
                int lastMid = readLastMeasureMember();

                if (lastMid == -1) {
                    mDefaultMeasureMember = (MemberEntity) notifyMessage.getList().get(0);
                } else {
                    for (int i = 0; i < notifyMessage.getList().size(); i++) {
                        MemberEntity memberEntity = (MemberEntity) notifyMessage.getList().get(i);

                        if (lastMid == memberEntity.getId()) {
                            mDefaultMeasureMember = memberEntity;
                            break;
                        }
                    }
                }
                // 读取用户成功，开始搜索设备
                boolean mayRequest = PermissionUtil.mayRequestLocation(this);
                if (!mayRequest) {
                    notifyManager.sendNotifyMessage(MsgConstants.MSG_SCANNING_DEVICE);
                }
                break;
            case MsgConstants.MSG_READ_ALL_MEMBERS_FAILED:
                logger.i("read all member failed. go in to guide");
                // 如果第一次登录，检测蓝牙设备成功后，延迟2S进入设置向导
                boolean b = new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent guideIntent = new Intent(WelcomeActivity.this, GuideActivity.class);
                        startActivity(guideIntent);
                        finish();
                    }
                }, Constants.INTO_GUIDE_DELAY);

                if (b) {
                    logger.i("start GuideActivity delay execute success");
                }
                break;
            case MsgConstants.MSG_BLUETOOTH_NOT_FOUND:
                // 未搜索到蓝牙设备则提示
//                Toast.makeText(this, R.string.scan_err, Toast.LENGTH_SHORT).show();
                onGotoMain(null);
                break;
            case MsgConstants.MSG_SCANNING_DEVICE_MORE:
                onScanMore(notifyMessage.getList());
                break;
            case MsgConstants.MSG_CONNECTED:
                onGotoMain((BluetoothDevice) notifyMessage.getParam());
                break;
            case MsgConstants.MSG_DISCONNECT:
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

        Dialog dialog = DialogBuild.getBuild().createListDialog(this, getString(R.string.pls_select_device), selValue, false, new DialogBuild.OnListMenuSelect() {
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
                finish();
                System.exit(0);
            }

            @Override
            public void onGoto() {
                onGotoMain(null);
            }
        });
        dialog.show();
    }

    /**
     * 从sharedpreference中获取上一次测量的用户的ID
     */
    private int readLastMeasureMember() {
        SharedPreferences preferences = getSharedPreferences("sx_setting", Context.MODE_PRIVATE);
        int mid = preferences.getInt("last_member_id", -1);
        return mid;
    }

    private void onGotoMain(BluetoothDevice device) {
        Intent intent = new Intent(this, MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(MainActivity.EXTRAS_DEVICE_NAME, device);
        intent.putExtra(MainActivity.EXTRAS_DEVICE_BUNDLE, bundle);
        intent.putExtra("connected", device == null ? false : true);
        intent.putExtra("default_measure_member", mDefaultMeasureMember);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, R.string.bluetooth_open, Toast.LENGTH_LONG).show();

                // 读取用户列表
                notifyManager.sendNotifyMessage(MsgConstants.MSG_READ_ALL_MEMBERS);
            } else if (resultCode == RESULT_CANCELED) {
                finish();
                System.exit(0);
            }
        }
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
                }
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (animationDrawable != null) {
            animationDrawable.stop();
        }
    }
}