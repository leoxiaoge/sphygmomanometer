package com.sx.portal;

import android.os.Bundle;

import com.javier.simplemvc.patterns.view.SimpleActivity;
import com.javier.simplemvc.patterns.view.SimpleFragment;
import com.sx.portal.entity.MemberEntity;
import com.sx.portal.fragment.GuideScanningFragment;
import com.sx.portal.fragment.GuideSetMemberFragment;

/**
 * author:Javier
 * time:2016/4/11.
 * mail:38244704@qq.com
 */
public class GuideActivity extends SimpleActivity {

    public static final String GUIDE_SET_MEMBER = "guide_set_member";
    public static final String GUIDE_SCAN_DEVICE = "guide_scan_device";

    private MemberEntity mGuideMember;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_guide);
    }

    @Override
    public void onInitView() {

    }

    @Override
    public void onSetEventListener() {

    }

    @Override
    public void onInitComplete() {
        switchFragment(GUIDE_SET_MEMBER, R.id.guide_container);
    }

    @Override
    protected String[] getFragmentTags() {
        return new String[]{GUIDE_SET_MEMBER, GUIDE_SCAN_DEVICE};
    }

    @Override
    protected SimpleFragment getFragment(String tag) {
        SimpleFragment fragment = null;

        switch (tag) {
            case GUIDE_SET_MEMBER:
                fragment = new GuideSetMemberFragment();
                break;
            case GUIDE_SCAN_DEVICE:
                fragment = new GuideScanningFragment();
                break;
        }

        return fragment;
    }

    public void nextFragment() {
        switchFragment(GUIDE_SCAN_DEVICE, R.id.guide_container);
    }

    public void setMember(MemberEntity e) {
        this.mGuideMember = e;
    }

    public MemberEntity getMember() {
        return mGuideMember;
    }
}