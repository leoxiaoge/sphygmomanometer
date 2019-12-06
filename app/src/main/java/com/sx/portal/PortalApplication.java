package com.sx.portal;

import android.content.Intent;

import com.javier.simplemvc.SimpleApplication;
import com.javier.simplemvc.patterns.entity.CommandEntity;
import com.javier.simplemvc.patterns.entity.DaoEntity;
import com.javier.simplemvc.util.Logger;
import com.sx.portal.command.BluetoothCommand;
import com.sx.portal.command.MemberCommand;
import com.sx.portal.dao.MeasureDao;
import com.sx.portal.dao.MemberDao;
import com.sx.portal.service.BluetoothDeviceService;

import java.util.ArrayList;

/**
 * author:Javier
 * time:2016/4/11.
 * mail:38244704@qq.com
 */
public class PortalApplication extends SimpleApplication {

    @Override
    public void onCreate() {
        super.onCreate();

        startUpSimpleMVC();
        initDatabase("sxportal.db", 1);

        startUpBluetoothService();
    }

    @Override
    protected void initLogger() {
        Logger.initLogger("sxportal", "sx_portal", "sxportal.log", true, false);
    }

    @Override
    protected ArrayList<DaoEntity> listDao() {
        ArrayList<DaoEntity> daoEntities = new ArrayList<>();
        daoEntities.add(new DaoEntity(R.id.id_member_dao, MemberDao.class));
        daoEntities.add(new DaoEntity(R.id.id_measure_dao, MeasureDao.class));
        return daoEntities;
    }

    @Override
    protected ArrayList<CommandEntity> listCommand() {
        ArrayList<CommandEntity> commandEntities = new ArrayList<>();
        commandEntities.add(new CommandEntity(MemberCommand.class, false));
        commandEntities.add(new CommandEntity(BluetoothCommand.class, true));
        return commandEntities;
    }

    private void startUpBluetoothService() {
        startService(new Intent(this, BluetoothDeviceService.class));
    }
}