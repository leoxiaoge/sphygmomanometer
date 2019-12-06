package com.sx.portal.fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.javier.simplemvc.patterns.notify.NotifyMessage;
import com.javier.simplemvc.patterns.view.SimpleFragment;
import com.sx.portal.GuideActivity;
import com.sx.portal.MsgConstants;
import com.sx.portal.R;
import com.sx.portal.entity.MemberEntity;

public class GuideSetMemberFragment extends SimpleFragment<GuideActivity> implements View.OnClickListener {

    private Button mNext;

    private EditText mName, mAge;
    private RadioGroup mSexGroup;

    // 0 : 男   1 : 女
    private int sex = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_guide_member, container, false);
    }

    @Override
    public void onInitView() {
        mNext = (Button) findViewById(R.id.guide_set_member_next);

        mName = (EditText) findViewById(R.id.member_name);
        mAge = (EditText) findViewById(R.id.member_age);

        mSexGroup = (RadioGroup) findViewById(R.id.sex_group);
    }

    @Override
    public void onSetEventListener() {
        mNext.setOnClickListener(this);

        mSexGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.select_man) {
                    sex = 0;
                } else {
                    sex = 1;
                }
            }
        });
    }

    @Override
    public void onInitComplete() {

    }

    @Override
    public String[] listMessage() {
        return new String[]{MsgConstants.MSG_ADD_MEMBER_COMPLETE};
    }

    @Override
    public void handlerMessage(NotifyMessage notifyMessage) {
        switch (notifyMessage.getName()) {
            case MsgConstants.MSG_ADD_MEMBER_COMPLETE:
                findActivity().nextFragment();
                break;
        }
    }

    @Override
    public void onClick(View v) {
        if (mName.getText() == null || mAge.getText() == null) {
            Toast.makeText(getActivity(), R.string.guide_member_note, Toast.LENGTH_LONG).show();
            return;
        }

        MemberEntity e = new MemberEntity();
        e.setName(mName.getText().toString());
        e.setAge(Integer.parseInt(mAge.getText().toString()));
        e.setSex(sex);
        e.setIcon(R.mipmap.ic_def_user);
        e.setHeadIcon("null");
        notifyManager.sendNotifyMessage(MsgConstants.MSG_ADD_MEMBER, e);
    }
}