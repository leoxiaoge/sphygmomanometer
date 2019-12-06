package com.sx.portal.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.javier.simplemvc.util.Logger;
import com.sx.portal.R;
import com.sx.portal.entity.MemberEntity;
import com.sx.portal.plugin.clipViewPager.adapter.RecyclingPagerAdapter;

import java.io.File;
import java.util.ArrayList;

import com.sx.portal.plugin.circleimageview.CircleImageView;

public class MemberPagerAdapter extends RecyclingPagerAdapter {
    private ArrayList<MemberEntity> mList;
    private LayoutInflater mInflator;

    public MemberPagerAdapter(Context context, ArrayList<MemberEntity> entitys) {
        mList = entitys;

        mInflator = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public MemberPagerAdapter(Context context) {
        mList = new ArrayList<MemberEntity>();

        mInflator = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void addAll(ArrayList<MemberEntity> entitys) {

        if (mList.size() == 0) {
            mList.addAll(entitys);
        }

        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup container) {
//        ImageView imageView = null;
//        if (convertView == null) {
//            imageView = new ImageView(mContext);
//        } else {
//            imageView = (ImageView) convertView;
//        }
//        imageView.setTag(position);
//        imageView.setImageResource(mList.get(position).getIcon());
//        return imageView;

        ViewHolder holder = null;

        if (convertView == null) {
            convertView = mInflator.inflate(R.layout.adapter_member_item, null);

            holder = new ViewHolder();
            holder.memberName = (TextView) convertView.findViewById(R.id.member_item_name);
            holder.memberIcon = (CircleImageView) convertView.findViewById(R.id.member_item_icon);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        MemberEntity e = mList.get(position);

        holder.memberName.setText(e.getName());

        Logger.getLogger().i("head icon : " + e.getHeadIcon());

        if (e.getHeadIcon() == null || e.getHeadIcon().equals("null")) {
            holder.memberIcon.setImageResource(e.getIcon());
        } else {
            try {
                File file = new File(e.getHeadIcon());

                if (file.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(e.getHeadIcon());
                    holder.memberIcon.setImageBitmap(bitmap);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return convertView;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    class ViewHolder {
        TextView memberName;
        CircleImageView memberIcon;
    }
}