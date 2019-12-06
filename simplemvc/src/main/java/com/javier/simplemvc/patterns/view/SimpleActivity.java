package com.javier.simplemvc.patterns.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.javier.simplemvc.SimpleContext;
import com.javier.simplemvc.core.NotifyManager;
import com.javier.simplemvc.interfaces.IView;
import com.javier.simplemvc.patterns.notify.NotifyMessage;
import com.javier.simplemvc.util.Logger;

/**
 * author:Javier
 * time:2016/5/28.
 * mail:38244704@qq.com
 */
@SuppressWarnings("unused")
public abstract class SimpleActivity extends FragmentActivity implements IView {
    protected Logger logger = Logger.getLogger();

    protected FragmentManager mFragmentManager;
    private FragmentTransaction mTransaction;
    private String[] mFragmentTags = null;
    protected NotifyManager notifyManager = NotifyManager.getInstance();

    protected SimpleContext simpleContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        simpleContext = SimpleContext.getSimpleContext();

        mFragmentManager = getSupportFragmentManager();
        mFragmentTags = getFragmentTags();
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        onInitView();
        onSetEventListener();
        onInitComplete();
    }

    @Override
    protected void onResume() {
        super.onResume();

        simpleContext.registerView(this);
    }

    @Override
    protected void onPause() {
        super.onPause();

        simpleContext.removeView(this);
    }

    protected void switchFragment(String tag, int layoutId) {
        switchFragment(tag, layoutId, null);
    }

    /**
     * 切换fragment
     *
     * @param tag      需要切换到的fragment的tag
     * @param layoutId 显示fragment的布局id
     * @param args     传递给fragment的参数
     */
    protected void switchFragment(String tag, int layoutId, Bundle args) {
        if (mFragmentTags == null) {
            logger.w("fragment tags is null");
            return;
        }

        beginTransaction();

        for (String fTag : mFragmentTags) {
            if (tag.equalsIgnoreCase(fTag)) {
                Fragment attachFragment = loadFragment(tag, args);

                if (attachFragment == null) {
                    logger.e("can not find fragment by tag : " + tag);
                    return;
                }

                attachFragment(layoutId, attachFragment, tag);
            } else {
                Fragment detachFragment = loadFragment(fTag, args);

                if (detachFragment == null) {
                    logger.e("can not find fragment by tag : " + fTag);
                    return;
                }

                detachFragment(detachFragment);
            }
        }

        commitTransaction();
    }

    protected String[] getFragmentTags() {
        return null;
    }

    protected SimpleFragment getFragment(String tag) {
        return null;
    }

    /**
     * 开始事物
     *
     * @return FragmentTransaction
     */
    private FragmentTransaction beginTransaction() {
        if (mTransaction == null) {
            mTransaction = mFragmentManager.beginTransaction();
        }

        return mTransaction;
    }


    /**
     * 提交事务
     */
    private void commitTransaction() {
        if (mTransaction != null && !mTransaction.isEmpty()) {
            mTransaction.commit();
            mTransaction = null;
        }
    }

    /**
     * attach fragment
     *
     * @param layoutId 显示fragment的布局ID
     * @param f        需要attach的fragment
     * @param tag      fragment的tag
     */
    private void attachFragment(int layoutId, Fragment f, String tag) {
        if (f != null) {
            if (f.isDetached()) {
                mTransaction.attach(f);
            } else if (!f.isAdded()) {
                mTransaction.add(layoutId, f, tag);
            }
        }
    }

    /**
     * detach fragment
     *
     * @param f 需要detach的fragment
     */
    private void detachFragment(Fragment f) {
        if (f != null && !f.isDetached()) {
            mTransaction.detach(f);
        }
    }

    private Fragment loadFragment(String tag, Bundle args) {
        Fragment f = mFragmentManager.findFragmentByTag(tag);

        if (f == null) {
            f = getFragment(tag);
            if (args != null) {
                f.setArguments(args);
            }
        }

        return f;
    }

    @Override
    public void onRemove() {

    }

    @Override
    public void onRegister() {

    }

    @Override
    public String[] listMessage() {
        return new String[0];
    }

    @Override
    public void handlerMessage(NotifyMessage message) {

    }
}