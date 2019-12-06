package com.sx.portal;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.javier.simplemvc.patterns.notify.NotifyMessage;
import com.javier.simplemvc.patterns.view.SimpleActivity;
import com.sx.portal.entity.MemberEntity;

/**
 * author:Javier
 * time:2016/6/12.
 * mail:38244704@qq.com
 */
public class MemberInfoActivity extends SimpleActivity {

    private MemberEntity memberEntity;

    private TextView title;
    private EditText name, age;
    private RadioGroup sexGroup;
    private RadioButton man, woman;
    private Button save;
    private ImageButton back;
    private int sex;

    private boolean bEdit = false;

    // 0 添加  1 修改
    private int type = 0;

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            bEdit = true;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_info);
    }

    @Override
    public void onInitView() {
        title = (TextView) findViewById(R.id.member_info_title);
        name = (EditText) findViewById(R.id.member_name);
        age = (EditText) findViewById(R.id.member_age);
        sexGroup = (RadioGroup) findViewById(R.id.sex_group);

        man = (RadioButton) findViewById(R.id.select_man);
        woman = (RadioButton) findViewById(R.id.select_woman);

        save = (Button) findViewById(R.id.member_save);
        back = (ImageButton) findViewById(R.id.member_info_back);
    }

    @Override
    public void onSetEventListener() {
        name.addTextChangedListener(textWatcher);
        age.addTextChangedListener(textWatcher);

        sexGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                bEdit = true;

                if (checkedId == R.id.select_man) {
                    sex = 0;
                } else {
                    sex = 1;
                }
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (type == 1 && bEdit) {
                    memberEntity.setName(name.getText().toString());
                    memberEntity.setAge(Integer.parseInt(age.getText().toString()));
                    memberEntity.setSex(sex);

                    notifyManager.sendNotifyMessage(MsgConstants.MSG_UPDATE_MEMBER, memberEntity);

                } else if (type == 0) {
                    MemberEntity entity = new MemberEntity();
                    entity.setName(name.getText().toString());
                    entity.setAge(Integer.parseInt(age.getText().toString()));
                    entity.setSex(sex);
                    entity.setIcon(R.mipmap.ic_def_user);
                    entity.setHeadIcon("null");
                    notifyManager.sendNotifyMessage(MsgConstants.MSG_ADD_MEMBER, entity);
                }
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onInitComplete() {
        type = getIntent().getIntExtra("type", 0);

        if (type == 0) {
            title.setText(R.string.add_memeber);

            sex = 0;
            man.setChecked(true);
        } else {
            title.setText(R.string.member_info);

            memberEntity = getIntent().getParcelableExtra("select_member");

            if (memberEntity != null) {
                name.setText(memberEntity.getName());
                age.setText(String.valueOf(memberEntity.getAge()));

                if (memberEntity.getSex() == 0) {
                    man.setChecked(true);
                } else {
                    woman.setChecked(true);
                }
            }
        }
    }

    @Override
    public String[] listMessage() {
        return new String[]{
                MsgConstants.MSG_UPDATE_MEMBER_COMPLETE,
                MsgConstants.MSG_ADD_MEMBER_COMPLETE
        };
    }

    @Override
    public void handlerMessage(NotifyMessage message) {
        super.handlerMessage(message);

        switch (message.getName()) {
            case MsgConstants.MSG_UPDATE_MEMBER_COMPLETE:
                finish();
                break;
            case MsgConstants.MSG_ADD_MEMBER_COMPLETE:
                finish();
                break;
        }
    }
}
