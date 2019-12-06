package com.sx.portal;

import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.javier.simplemvc.patterns.view.SimpleFragment;
import com.sx.portal.entity.MeasureEntity;
import com.sx.portal.entity.MemberEntity;
import com.sx.portal.fragment.MeasureFragment;
import com.sx.portal.fragment.MeasureRecordFragment;
import com.sx.portal.fragment.SettingFragment;
import com.sx.portal.service.BluetoothDeviceService;
import com.sx.portal.util.DialogBuild;

import java.lang.reflect.Member;

public class MainActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener {

    public static final String MODE_MEASURE = "mode_measure";
    public static final String MODE_RECORD = "mode_record";
    public static final String MODE_SETTING = "mode_setting";

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_BUNDLE = "DEVICE_BUNDLE";

    private RadioGroup mTabGroup;

    private BluetoothDevice device;

    private MemberEntity mCurrentMember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onInitView() {
        mTabGroup = (RadioGroup) findViewById(R.id.tab_group);
        RadioButton mTabMeasure = (RadioButton) findViewById(R.id.tab_measure);

        mTabMeasure.setChecked(true);
    }

    @Override
    public void onSetEventListener() {
        mTabGroup.setOnCheckedChangeListener(this);
    }

    @Override
    public void onInitComplete() {
        Bundle bundle = getIntent().getBundleExtra(EXTRAS_DEVICE_BUNDLE);

        if (bundle != null) {
            device = bundle.getParcelable(EXTRAS_DEVICE_NAME);
        }

        mCurrentMember = getIntent().getParcelableExtra("default_measure_member");
        boolean bConnect = getIntent().getBooleanExtra("connected", false);

        if (device != null || mCurrentMember != null) {
            Bundle args = new Bundle();
            if (device != null) {
                args.putParcelable("connect_bluetooth_device", device);
            }

            if (mCurrentMember != null) {
                args.putParcelable("default_measure_member", mCurrentMember);
            }

            args.putBoolean("connected", bConnect);

            switchFragment(MODE_MEASURE, R.id.fragment_container, args);
        } else {
            switchFragment(MODE_MEASURE, R.id.fragment_container);
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.tab_measure:
                switchFragment(MODE_MEASURE, R.id.fragment_container);
                break;
            case R.id.tab_record:
                Bundle b = new Bundle();
                b.putParcelable("default_measure_member", mCurrentMember);
                switchFragment(MODE_RECORD, R.id.fragment_container, b);
                break;
            case R.id.tab_setting:
                Bundle bundle = new Bundle();
                bundle.putParcelable("connect_bluetooth_device", device);

                switchFragment(MODE_SETTING, R.id.fragment_container, bundle);
                break;
        }
    }

    @Override
    protected String[] getFragmentTags() {
        return new String[]{MODE_MEASURE, MODE_RECORD, MODE_SETTING};
    }

    @Override
    protected SimpleFragment getFragment(String tag) {
        SimpleFragment fragment = null;

        switch (tag) {
            case MODE_MEASURE:
                fragment = new MeasureFragment();
                break;
            case MODE_RECORD:
                fragment = new MeasureRecordFragment();
                break;
            case MODE_SETTING:
                fragment = new SettingFragment();
                break;
        }

        return fragment;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Dialog dialog = DialogBuild.getBuild().createConfirmDialog(this,
                    getString(R.string.exit_confirm), new DialogBuild.OnConfirmListener() {
                        @Override
                        public void onConfirm(Dialog dialog, boolean isConfirm) {
                            dialog.dismiss();

                            if (isConfirm) {
                                // 停止蓝牙服务
                                Intent intent = new Intent(MainActivity.this, BluetoothDeviceService.class);
                                stopService(intent);

                                finish();
                                getApplication().onTerminate();
                                System.exit(0);
                            }
                        }
                    });
            dialog.show();
        }
        return super.onKeyDown(keyCode, event);
    }

    public void setCurrentSelectedMember(MemberEntity entity) {
        this.mCurrentMember = entity;
    }
}