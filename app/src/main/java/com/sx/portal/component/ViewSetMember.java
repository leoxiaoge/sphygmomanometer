package com.sx.portal.component;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sx.portal.R;
import com.sx.portal.entity.MemberEntity;

/**
 * TODO: document your custom view class.
 */
public class ViewSetMember extends LinearLayout {

    private TextView mMemberName, mMemberAge;

    public ViewSetMember(Context context) {
        super(context);
        init(context);
    }

    public ViewSetMember(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ViewSetMember(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater=(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        inflater.inflate(R.layout.view_add_member,this);

        mMemberName = (TextView) findViewById(R.id.member_name);
        mMemberAge = (TextView) findViewById(R.id.member_age);
    }

    public String getMemerName() {
        return mMemberName.getText().toString();
    }

    public String getMemberAge() {
        return mMemberAge.getText().toString();
    }

    public boolean isNull() {
        String name = mMemberName.getText().toString();
        String age = mMemberAge.getText().toString();

        if((name.equals("") || name == null) && (age.equals("") || age == null)) {
            return true;
        }

        return false;
    }

    public boolean checkName() {
        String name = mMemberName.getText().toString();

        if (name.equals("") || name == null) {
            return false;
        }

        return true;
    }

    public MemberEntity getMemberEntity() {
        MemberEntity m = new MemberEntity();
        m.setName(mMemberName.getText().toString());
        m.setAge(Integer.parseInt(mMemberAge.getText().toString()));
        m.setIcon(1);
        m.setHeadIcon("null");

        return m;
    }
}