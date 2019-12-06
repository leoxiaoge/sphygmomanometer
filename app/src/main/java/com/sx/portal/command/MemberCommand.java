package com.sx.portal.command;

import com.javier.simplemvc.patterns.command.SimpleCommand;
import com.javier.simplemvc.patterns.notify.NotifyMessage;
import com.sx.portal.MsgConstants;
import com.sx.portal.R;
import com.sx.portal.dao.MeasureDao;
import com.sx.portal.dao.MemberDao;
import com.sx.portal.entity.MeasureEntity;
import com.sx.portal.entity.MemberEntity;

import java.lang.reflect.Member;
import java.util.ArrayList;

/**
 * author:Javier
 * time:2016/4/11.
 * mail:38244704@qq.com
 * <p/>
 * 操作member相关业务逻辑的action
 */
public class MemberCommand extends SimpleCommand {

    @Override
    public String[] listMessage() {
        return new String[]{
                MsgConstants.MSG_READ_ALL_MEMBERS,
                MsgConstants.MSG_ADD_MEMBER,
                MsgConstants.MSG_READ_MEASURE_RECODE,
                MsgConstants.MSG_UPDATE_MEMBER
        };
    }

    @Override
    public void handlerMessage(NotifyMessage notifyMessage) {
        switch (notifyMessage.getName()) {
            case MsgConstants.MSG_READ_ALL_MEMBERS:
                readAllMembers();
                break;
            case MsgConstants.MSG_ADD_MEMBER:
                addMember((MemberEntity) notifyMessage.getParam());
                break;
            case MsgConstants.MSG_READ_MEASURE_RECODE:
                readMeasureRecordDesc(notifyMessage.getBundle().getLong("startTime"),
                        notifyMessage.getBundle().getLong("endTime"),
                        notifyMessage.getBundle().getInt("memberId"),
                        notifyMessage.getBundle().getString("order"));
                break;
            case MsgConstants.MSG_UPDATE_MEMBER:
                MemberEntity entity = (MemberEntity) notifyMessage.getParam();
                updateMember(entity);
                break;
        }
    }

    /**
     * 读取所有的家庭成员
     */
    private void readAllMembers() {
        MemberDao memberDao = (MemberDao) getDao(R.id.id_member_dao);
        ArrayList<MemberEntity> memberEntities = memberDao.getAllMembers();

        if (memberEntities == null || memberEntities.size() == 0) {
            notifyManager.sendNotifyMessage(MsgConstants.MSG_READ_ALL_MEMBERS_FAILED);
        } else {
            notifyManager.sendNotifyMessage(MsgConstants.MSG_READ_ALL_MEMBERS_COMPLETE, memberEntities);
        }
    }

    /**
     * 添加家庭成员
     */
    private void addMember(MemberEntity member) {
        MemberDao dao = (MemberDao) getDao(R.id.id_member_dao);
        dao.createMember(member);

        notifyManager.sendNotifyMessage(MsgConstants.MSG_ADD_MEMBER_COMPLETE);
    }

    /**
     * 修改家庭成员信息
     * @param memberEntity  需要修改家庭成员信息
     */
    private void updateMember(MemberEntity memberEntity) {
        MemberDao dao = (MemberDao) getDao(R.id.id_member_dao);
        dao.updateMember(memberEntity);

        notifyManager.sendNotifyMessage(MsgConstants.MSG_UPDATE_MEMBER_COMPLETE);
    }

    /**
     * 读取测量记录
     */
    private void readMeasureRecordDesc(long start, long end, int mid, String order) {
        MeasureDao dao = (MeasureDao) getDao(R.id.id_measure_dao);

        ArrayList<MeasureEntity> measureEntities = dao.readMeasureByTime(start, end, mid, order);

        notifyManager.sendNotifyMessage(MsgConstants.MSG_READ_MEASURE_RECORD_RETURN, measureEntities);
    }


}