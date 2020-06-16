package com.easyetz.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.easyetz.R;

public class CustomToast extends Dialog implements Runnable {
    private Context mContext;
    private Dialog mDialog;
    private TextView mTipsText;
    private int mShowTime;

    public CustomToast(Context context) {
        super(context, R.style.ToastDialog);
        this.mContext = context;
        mDialog = new Dialog(mContext, R.style.ToastDialog);//自定义Dialog的样式
        initView();
        initData();
    }

    private void initView() {
        mDialog.setContentView(R.layout.custom_toast);
        mTipsText = (TextView) mDialog.findViewById(R.id.toastText);
    }

    private void initData() {
//        Window dialogWindow = mDialog.getWindow();//得到要显示的窗口
//        //得到窗口的属性
//        WindowManager.LayoutParams winLayoutParams = dialogWindow.getAttributes();
//        dialogWindow.setGravity(Gravity.CENTER);
//        //重定义窗口的大小
//        winLayoutParams.width = 500;
//        winLayoutParams.height = 300;
//        dialogWindow.setAttributes(winLayoutParams);

        mDialog.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_BACK:
                            dismiss();
                            break;
                    }
                }
                return false;
            }
        });
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(mShowTime);//睡眠时间
                sendMessage(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendMessage(int msg) {
        Message message = new Message();
        message.what = msg;
        mHandler.sendMessage(message);
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (mDialog.isShowing())
                        dismiss();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public void setMessage(int msg) {
        mTipsText.setText(msg);
    }

    public void setMessage(CharSequence msg) {
        mTipsText.setText(msg);
    }

    public void showTime(int time) {
        mShowTime = time;
    }

    public void show() {
        mDialog.show();
        new Thread(this).start();
    }

    public void dismiss() {
        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }
}

//        CustomToast myToast = new CustomToast(this);
//        myToast.setMessage("Hello World !");
//        myToast.showTime(2000);
//        myToast.show();
