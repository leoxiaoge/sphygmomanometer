package com.sx.portal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.javier.simplemvc.patterns.notify.NotifyMessage;
import com.javier.simplemvc.patterns.view.SimpleActivity;
import com.sx.portal.adapter.MemberItemAdapter;
import com.sx.portal.entity.MemberEntity;

import java.util.ArrayList;

/**
 * 家庭成员列表
 */
public class FamilyMemberActivity extends SimpleActivity implements View.OnClickListener {

    private ImageButton mBack;
    private Button mEdit;
    private ListView mMemberList;

    private TextView mNewMember;

    private MemberItemAdapter mAdapter;

    ArrayList<MemberEntity> mMembers = new ArrayList<MemberEntity>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_family_member);
    }

    @Override
    public void onInitView() {
        mBack = (ImageButton) findViewById(R.id.member_back);
        mEdit = (Button) findViewById(R.id.member_edit);
        mMemberList = (ListView) findViewById(R.id.family_member_list);
        mNewMember = (TextView) findViewById(R.id.add_new_member);

        mAdapter = new MemberItemAdapter(this, mMembers);
        mMemberList.setAdapter(mAdapter);
    }

    @Override
    public void onSetEventListener() {
        mBack.setOnClickListener(this);
        mEdit.setOnClickListener(this);
        mNewMember.setOnClickListener(this);

        mMemberList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MemberEntity entity = mMembers.get(position);

                Intent i = new Intent(FamilyMemberActivity.this, MemberInfoActivity.class);
                i.putExtra("select_member", entity);
                i.putExtra("type", 1);
                startActivity(i);
            }
        });
    }

    @Override
    public void onInitComplete() {
    }

    @Override
    public void onRegister() {
        super.onRegister();

        notifyManager.sendNotifyMessage(MsgConstants.MSG_READ_ALL_MEMBERS);
    }

    @Override
    public void onRemove() {
        super.onRemove();
    }

    @Override
    public String[] listMessage() {
        return new String[]{
                MsgConstants.MSG_READ_ALL_MEMBERS_COMPLETE,
                MsgConstants.MSG_ADD_MEMBER_COMPLETE
        };
    }

    @Override
    public void handlerMessage(NotifyMessage message) {
        super.handlerMessage(message);

        switch (message.getName()) {
            case MsgConstants.MSG_READ_ALL_MEMBERS_COMPLETE:
                mMembers.clear();
                mMembers.addAll(message.getList());
                mAdapter.notifyDataSetChanged();
                break;
            case MsgConstants.MSG_ADD_MEMBER_COMPLETE:
                notifyManager.sendNotifyMessage(MsgConstants.MSG_READ_ALL_MEMBERS);
                break;
        }
    }

    @Override
    public void onClick(View view) {
        if (view == mBack) {
            finish();
        } else if (view == mNewMember) {
            Intent i = new Intent(FamilyMemberActivity.this, MemberInfoActivity.class);
            i.putExtra("type", 0);
            startActivity(i);
        }
    }
}