package com.sx.portal.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.sx.portal.R;
import com.sx.portal.entity.MeasureEntity;
import com.sx.portal.plugin.stickylistheaders.StickyListHeadersAdapter;
import com.sx.portal.util.LevelUtil;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/1/10.
 */
public class MeasureListAdapter extends BaseAdapter implements
        StickyListHeadersAdapter, SectionIndexer {

    private Context mContext;
    private ArrayList<MeasureEntity> mMeasureEntities;
    private LayoutInflater mInflater;

    public MeasureListAdapter(Context context, ArrayList<MeasureEntity> entities) {
        this.mContext = context;
        this.mMeasureEntities = entities;
        mInflater = LayoutInflater.from(context);
    }

    public void refersh(ArrayList<MeasureEntity> entities) {
        this.mMeasureEntities.clear();

        this.mMeasureEntities.addAll(entities);
        notifyDataSetChanged();
    }

    @Override
    public long getHeaderId(int position) {
        return mMeasureEntities.get(position).getHeaderId();
    }

    @Override
    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder = null;

        if (convertView == null) {
            holder = new HeaderViewHolder();
            convertView = mInflater.inflate(R.layout.adapter_measure_header, parent, false);

            holder.text = (TextView) convertView.findViewById(R.id.measure_header);

            convertView.setTag(holder);
        } else {
            holder = (HeaderViewHolder) convertView.getTag();
        }

        String timeDate = mMeasureEntities.get(position).getTime_date();
        holder.text.setText(timeDate);

        return convertView;
    }

    @Override
    public int getCount() {
        return mMeasureEntities.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.adapter_measure, parent, false);

            holder.mResultIcon = (ImageView) convertView.findViewById(R.id.measure_result_icon);
            holder.mResultText = (TextView) convertView.findViewById(R.id.measure_result_txt);
            holder.mSbpDbp = (TextView) convertView.findViewById(R.id.measure_item_sbp_dbp);
            holder.mDateTime = (TextView) convertView.findViewById(R.id.measure_datetime);
            holder.mHeatbeat = (TextView) convertView.findViewById(R.id.measure_heatbeat);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        MeasureEntity entity = mMeasureEntities.get(position);

        int level = entity.getLevel();

        if (level != -1) {
            holder.mResultIcon.setImageResource(LevelUtil.BLOOD_PRESSURE_ICON.get(level));
            holder.mResultText.setText(LevelUtil.BLOOD_PRESSURE_LABEL.get(level));
        }

        String sbp_dbp = String.format(mContext.getString(R.string.sbp_dbp), entity.getSbp(), entity.getDbp());
        holder.mSbpDbp.setText(sbp_dbp);
        holder.mDateTime.setText(entity.getDateTime());

        holder.mHeatbeat.setText(String.valueOf(entity.getHeart_beat()));

        return convertView;
    }

    @Override
    public Object[] getSections() {
        return new Object[0];
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        return 0;
    }

    @Override
    public int getSectionForPosition(int position) {
        return 0;
    }

    class HeaderViewHolder {
        TextView text;
    }

    class ViewHolder {
        ImageView mResultIcon;
        TextView mResultText;
        TextView mSbpDbp;
        TextView mDateTime;
        TextView mHeatbeat;
    }
}