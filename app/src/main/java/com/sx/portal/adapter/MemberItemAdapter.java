package com.sx.portal.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sx.portal.R;
import com.sx.portal.entity.MemberEntity;

import java.io.File;
import java.util.ArrayList;

import com.sx.portal.plugin.circleimageview.CircleImageView;

/**
 * Created by Administrator on 2016/1/15 0015.
 */
public class MemberItemAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<MemberEntity> mMemberEntities;

    private LayoutInflater mInflator;

    public MemberItemAdapter(Context context, ArrayList<MemberEntity> entities) {
        this.mContext = context;
        this.mMemberEntities = entities;

        mInflator = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mMemberEntities.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        MemberAdapterHolder holder = null;

        if (view == null) {
            holder = new MemberAdapterHolder();
            view = mInflator.inflate(R.layout.adapter_family_member_item, null);

            holder.iv = (CircleImageView) view.findViewById(R.id.fm_member_icon);
            holder.mname = (TextView) view.findViewById(R.id.fm_member_name);
            holder.mage = (TextView) view.findViewById(R.id.fm_member_age);

            view.setTag(holder);
        } else {
            holder = (MemberAdapterHolder) view.getTag();
        }

        MemberEntity memberEntity = mMemberEntities.get(i);

        if (memberEntity.getHeadIcon() == null || memberEntity.getHeadIcon().equals("null")) {
            holder.iv.setImageResource(memberEntity.getIcon());
        } else {
            try {
                File file = new File(memberEntity.getHeadIcon());

                if(file.exists())
                {
                    Bitmap bitmap = BitmapFactory.decodeFile(memberEntity.getHeadIcon());
                    holder.iv.setImageBitmap(bitmap);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        holder.mname.setText(memberEntity.getName());
        holder.mage.setText(String.format(mContext.getString(R.string.age_s), memberEntity.getAge()));

        return view;
    }

    class MemberAdapterHolder {
        CircleImageView iv;
        TextView mname, mage;
    }
}