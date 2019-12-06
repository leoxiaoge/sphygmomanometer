package com.sx.portal.fragment;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.javier.simplemvc.patterns.notify.NotifyMessage;
import com.javier.simplemvc.patterns.view.SimpleFragment;
import com.sx.portal.MainActivity;
import com.sx.portal.MeasureChartActivity;
import com.sx.portal.MsgConstants;
import com.sx.portal.R;
import com.sx.portal.adapter.MeasureListAdapter;
import com.sx.portal.adapter.MemberItemAdapter;
import com.sx.portal.entity.MemberEntity;
import com.sx.portal.plugin.circleimageview.CircleImageView;
import com.sx.portal.plugin.stickylistheaders.ExpandableStickyListHeadersListView;
import com.sx.portal.util.DateTimeUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.WeakHashMap;

public class MeasureRecordFragment extends SimpleFragment<MainActivity> implements RadioGroup.OnCheckedChangeListener {

    private RadioGroup mButtonBar;

    // 用户列表
    private ArrayList<MemberEntity> mMemberEntitys;

    // 当前用户
    private MemberEntity mCurrentMember;

    // 显示当前用户的组件
    private CircleImageView mMemberIcon;
    private TextView mMemberName, mMemberAge;
    private LinearLayout mMemberInfoContainer;
    private ImageView mMemberListIcon;

    private ImageButton mChart;

    private long startTime;
    private long endTime;

    // 测量记录列表
    private ExpandableStickyListHeadersListView mMeasureList;
    private MeasureListAdapter mAdapater;

    WeakHashMap<View, Integer> mOriginalViewHeightPool = new WeakHashMap<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_measure_record, container, false);
    }

    @Override
    public void onInitView() {
        mMemberInfoContainer = (LinearLayout) findViewById(R.id.fm_member_container);
        mMemberIcon = (CircleImageView) findViewById(R.id.fm_member_icon);
        mMemberName = (TextView) findViewById(R.id.fm_member_name);
        mMemberAge = (TextView) findViewById(R.id.fm_member_age);
        mMemberListIcon = (ImageView) findViewById(R.id.member_list_icon);

        mChart = (ImageButton) findViewById(R.id.measure_chart);

        mButtonBar = (RadioGroup) findViewById(R.id.buttonbar_group);
        mMeasureList = (ExpandableStickyListHeadersListView) findViewById(R.id.measure_record_list);
        mMeasureList.setAnimExecutor(new AnimationExecutor());
    }

    @Override
    public void onSetEventListener() {
        mButtonBar.setOnCheckedChangeListener(this);

        mMemberInfoContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMemberListIcon.setImageResource(R.mipmap.ic_list_close);
                displayPopWindow();
            }
        });

        mChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chartIntent = new Intent(getActivity(), MeasureChartActivity.class);
                chartIntent.putExtra("member", mCurrentMember);
                startActivity(chartIntent);
            }
        });
    }

    @Override
    public void onInitComplete() {
        startTime = DateTimeUtil.getTimesmorning().getTime();
        endTime = DateTimeUtil.getTimesnight().getTime();

        if (getArguments() != null) {
            mCurrentMember = getArguments().getParcelable("default_measure_member");
        }

        setMemberInfo();
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
                MsgConstants.MSG_READ_MEASURE_RECORD_RETURN
        };
    }

    @Override
    public void handlerMessage(NotifyMessage message) {
        super.handlerMessage(message);

        switch (message.getName()) {
            case MsgConstants.MSG_READ_ALL_MEMBERS_COMPLETE:
                mMemberEntitys = message.getList();

                if (mCurrentMember == null) {
                    mCurrentMember = mMemberEntitys.get(0);
                    setMemberInfo();
                }

                readMeasureRecord();
                break;
            case MsgConstants.MSG_READ_MEASURE_RECORD_RETURN:
                if (mAdapater == null) {
                    mAdapater = new MeasureListAdapter(getActivity(), message.getList());
                    mMeasureList.setAdapter(mAdapater);
                } else {
                    mAdapater.refersh(message.getList());
                }
                break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.day:
                startTime = DateTimeUtil.getTimesmorning().getTime();
                endTime = DateTimeUtil.getTimesnight().getTime();
                break;
            case R.id.week:
                startTime = DateTimeUtil.getTimesWeekmorning().getTime();
                endTime = DateTimeUtil.getTimesWeeknight().getTime();
                break;
            case R.id.month:
                startTime = DateTimeUtil.getTimesMonthmorning().getTime();
                endTime = DateTimeUtil.getTimesMonthnight().getTime();
                break;
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date sd = new Date(startTime);
        Date ed = new Date(endTime);

        logger.i("start time : " + format.format(sd));
        logger.i("end time : " + format.format(ed));
    }

    @Override
    public void onStop() {
        super.onStop();

        mAdapater = null;
    }

    private void setMemberInfo() {
        if (mCurrentMember != null) {
            mMemberIcon.setImageResource(mCurrentMember.getIcon());
            mMemberName.setText(mCurrentMember.getName());
            mMemberAge.setText(mCurrentMember.getAge() + "");
        }
    }

    private void readMeasureRecord() {
        Bundle bundle = new Bundle();
        bundle.putLong("startTime", startTime);
        bundle.putLong("endTime", endTime);
        bundle.putInt("memberId", mCurrentMember.getId());
        bundle.putString("order", "desc");
        notifyManager.sendNotifyMessage(MsgConstants.MSG_READ_MEASURE_RECODE, bundle);
    }

    //animation executor
    class AnimationExecutor implements ExpandableStickyListHeadersListView.IAnimationExecutor {
        @Override
        public void executeAnim(final View target, final int animType) {
            if (ExpandableStickyListHeadersListView.ANIMATION_EXPAND == animType && target.getVisibility() == View.VISIBLE) {
                return;
            }
            if (ExpandableStickyListHeadersListView.ANIMATION_COLLAPSE == animType && target.getVisibility() != View.VISIBLE) {
                return;
            }
            if (mOriginalViewHeightPool.get(target) == null) {
                mOriginalViewHeightPool.put(target, target.getHeight());
            }
            final int viewHeight = mOriginalViewHeightPool.get(target);
            float animStartY = animType == ExpandableStickyListHeadersListView.ANIMATION_EXPAND ? 0f : viewHeight;
            float animEndY = animType == ExpandableStickyListHeadersListView.ANIMATION_EXPAND ? viewHeight : 0f;
            final ViewGroup.LayoutParams lp = target.getLayoutParams();
            ValueAnimator animator = ValueAnimator.ofFloat(animStartY, animEndY);
            animator.setDuration(200);
            target.setVisibility(View.VISIBLE);
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    if (animType == ExpandableStickyListHeadersListView.ANIMATION_EXPAND) {
                        target.setVisibility(View.VISIBLE);
                    } else {
                        target.setVisibility(View.GONE);
                    }
                    target.getLayoutParams().height = viewHeight;
                }

                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    lp.height = ((Float) valueAnimator.getAnimatedValue()).intValue();
                    target.setLayoutParams(lp);
                    target.requestLayout();
                }
            });
            animator.start();
        }
    }

    // 弹出选择家庭成员pop window
    public void displayPopWindow() {
        View contentView = LayoutInflater.from(getActivity()).inflate(
                R.layout.popwin_member_selector, null);

        final PopupWindow popupWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);

        final ListView memberList = (ListView) contentView.findViewById(R.id.pop_member_list);

        memberList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                logger.i("on select member");
                popupWindow.dismiss();

                mCurrentMember = mMemberEntitys.get(position);

                setMemberInfo();
                readMeasureRecord();
            }
        });

        MemberItemAdapter adapter = new MemberItemAdapter(getActivity(), mMemberEntitys);
        memberList.setAdapter(adapter);
        popupWindow.setOutsideTouchable(true);

        popupWindow.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == event.ACTION_UP) {
                    logger.i("y == " + event.getY());
                    logger.i("h == " + memberList.getHeight());

                    if (event.getY() > memberList.getHeight()) {
                        popupWindow.dismiss();
                        return true;
                    }
                }

                return false;
            }
        });

        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                mMemberListIcon.setImageResource(R.mipmap.ic_list_open);
            }
        });

        popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.popwin_bg));
        popupWindow.showAsDropDown(mMemberInfoContainer, 0, 2);
    }
}