package com.sx.portal.util;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.sx.portal.R;

import java.util.ArrayList;
import java.util.HashMap;

public class DialogBuild {
    private AnimationDrawable mAnimationDrawable;

    private static DialogBuild build;

    private Dialog waittingDialog;

    private OnWaittingDialogTimeout waittingDialogTimeoutListener;
    private Context mContext;

    public interface OnConfirmListener {
        void onConfirm(Dialog dialog, boolean isConfirm);
    }

    public interface OnListMenuSelect {
        void onMenuSelect(AdapterView<?> arg0, View arg1, int arg2, long arg3,
                          Dialog dialog);

        void onExit();

        void onGoto();

        void onCancel();
    }

    public interface OnSetMemeberListener {
        void onSetMember(Dialog dialog, String name, int age);
    }

    public interface OnWaittingDialogTimeout {
        boolean onTimeout();

        void onDialogShow(Dialog dialog);
    }

    private DialogBuild() {

    }

    public static DialogBuild getBuild() {
        if (build == null) {
            build = new DialogBuild();
        }

        return build;
    }

    public Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            String message = msg.obj.toString();

            boolean isShow = true;

            if (waittingDialogTimeoutListener != null) {
                isShow = waittingDialogTimeoutListener.onTimeout();
            }

            if (isShow) {
                Dialog dialog = createWaittingDialog(mContext, message);

                if (waittingDialogTimeoutListener != null) {
                    waittingDialogTimeoutListener.onDialogShow(dialog);
                }
            }
        }
    };

    // 延迟弹出等待对话框
    public void createWaittingDialogDelay(Context context, String message,
                                          int delayMillis, OnWaittingDialogTimeout timeoutListener) {
        this.waittingDialogTimeoutListener = timeoutListener;
        this.mContext = context;

        Message msg = new Message();
        msg.what = 0;
        msg.obj = message;

        handler.sendMessageDelayed(msg, delayMillis);
    }

    // 等待对话框
    public Dialog createWaittingDialog(Context context, String msg) {
        if (waittingDialog != null && waittingDialog.isShowing()) {
            waittingDialog.dismiss();
            waittingDialog = null;
        }

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        waittingDialog = new Dialog(context, R.style.Theme_XDialog);
        View layout = inflater.inflate(R.layout.view_waiting_dialog, null);

        TextView message = (TextView) layout.findViewById(R.id.wait_msg);
        Drawable[] drawables = message.getCompoundDrawables();
        mAnimationDrawable = (AnimationDrawable) drawables[1];
        mAnimationDrawable.start();

        if (!TextUtils.isEmpty(msg)) {
            message.setText(msg);
        }

        waittingDialog.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (mAnimationDrawable != null)
                    mAnimationDrawable.stop();
            }
        });

        waittingDialog.setContentView(layout);

        Display d = waittingDialog.getWindow().getWindowManager()
                .getDefaultDisplay();
        Window dialogWindow = waittingDialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.CENTER); // 此处可以设置dialog显示的位置
        // dialogWindow.setWindowAnimations(R.style.dialog_anim); // 添加动画
        lp.width = 380; // 宽度
        lp.height = 380; // 高度
        lp.alpha = 0.9f;
        dialogWindow.setAttributes(lp);

        return waittingDialog;
    }

    // 提示对话框
    public Dialog createAlarmDialog(Context context,
                                    String confirmContent, final OnConfirmListener listener) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final Dialog dialog = new Dialog(context, R.style.Theme_XDialog);
        View layout = inflater.inflate(R.layout.view_alarm_dialog, null);

        // TextView alarm_title = (TextView)
        // layout.findViewById(R.id.alarm_title);
        TextView alarm_content = (TextView) layout
                .findViewById(R.id.alarm_content);

        // alarm_title.setText(alarmTitle);
        alarm_content.setText(confirmContent);

        Button confirm = (Button) layout.findViewById(R.id.alarm_ok);

        confirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

                if (listener != null) {
                    listener.onConfirm(dialog, true);
                }
            }
        });

        dialog.setContentView(layout);
        toastDialogStyle(dialog);

        return dialog;
    }

    // 确认对话框
    public Dialog createConfirmDialog(Context context, String confirmContent,
                                      final OnConfirmListener listener) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final Dialog dialog = new Dialog(context, R.style.Theme_XDialog);
        View layout = inflater.inflate(R.layout.view_confirm_dialog, null);

        // TextView confirm_title = (TextView) layout
        // .findViewById(R.id.confirm_title);
        TextView confirm_content = (TextView) layout
                .findViewById(R.id.confirm_content);
        Button cancel = (Button) layout.findViewById(R.id.confirm_cancel);
        Button confirm = (Button) layout.findViewById(R.id.confirm_ok);

        // confirm_title.setText(confirmTitle);
        confirm_content.setText(confirmContent);

        cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                if (listener != null) {
                    listener.onConfirm(dialog, false);
                }
            }
        });

        confirm.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

                if (listener != null) {
                    listener.onConfirm(dialog, true);
                }
            }
        });

        dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int keyCode,
                                 KeyEvent keyEvent) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    return true;
                }
                return false;
            }
        });

        dialog.setContentView(layout);

        toastDialogStyle(dialog);

        return dialog;
    }

    // 菜单对话框
    public Dialog createListDialog(Context context, String title,
                                   ArrayList<HashMap<String, String>> data, boolean showConfirm,
                                   final OnListMenuSelect listener) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final Dialog dialog = new Dialog(context, R.style.Theme_XDialog);
        View layout = inflater.inflate(R.layout.view_select_menu_dialog, null);

        dialog.addContentView(layout, new LayoutParams(
                LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));

        TextView titleView = (TextView)
                layout.findViewById(R.id.sel_menu_title);
        View divider = (View) layout.findViewById(R.id.sel_menu_divider);

        if (TextUtils.isEmpty(title)) {
            titleView.setVisibility(View.GONE);
            divider.setVisibility(View.GONE);
        } else {
            titleView.setText(title);
        }

        LinearLayout dialog_select_menu_button = (LinearLayout) layout.findViewById(R.id.dialog_select_menu_button);
        Button cancel = (Button) layout.findViewById(R.id.dialog_cancel);

        Button let_goto = (Button) layout.findViewById(R.id.dialog_goto);
        Button let_exit = (Button) layout.findViewById(R.id.dialog_exit);

        if (showConfirm) {
            cancel.setVisibility(View.VISIBLE);
            dialog_select_menu_button.setVisibility(View.GONE);

            cancel.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        dialog.dismiss();
                        listener.onCancel();
                    }
                }
            });
        } else {
            let_goto.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        dialog.dismiss();
                        listener.onGoto();
                    }
                }
            });

            let_exit.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        dialog.dismiss();
                        listener.onExit();
                    }
                }
            });
        }

        ListView modeList = (ListView) layout.findViewById(R.id.sel_menu_list);
        SimpleAdapter as = new SimpleAdapter(context, data,
                R.layout.view_adapter_dialog_select_menu,
                new String[]{"name", "address"}, new int[]{R.id.sel_menu_itemname, R.id.sel_menu_address});
        modeList.setAdapter(as);

        modeList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                if (listener != null) {
                    dialog.dismiss();
                    listener.onMenuSelect(arg0, arg1, arg2, arg3, dialog);
                }
            }
        });

        dialog.setContentView(layout);

        Display d = dialog.getWindow().getWindowManager().getDefaultDisplay();
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.CENTER); // 此处可以设置dialog显示的位置
        dialogWindow.setWindowAnimations(R.style.dialog_anim); // 添加动画
        lp.width = (int) (d.getWidth() * 0.9); // 宽度
        lp.alpha = 1.0f;
        dialogWindow.setAttributes(lp);

        return dialog;
    }

    private void toastDialogStyle(Dialog dialog) {
        Display d = dialog.getWindow().getWindowManager().getDefaultDisplay();
        Window dialogWindow = dialog.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.CENTER); // 此处可以设置dialog显示的位置
        dialogWindow.setWindowAnimations(R.style.dialog_anim); // 添加动画
        lp.width = (int) (d.getWidth() * 0.9); // 宽度
        lp.height = (int) (d.getHeight() * 0.25); // 高度
        lp.alpha = 1.0f;
        dialogWindow.setAttributes(lp);
    }
}