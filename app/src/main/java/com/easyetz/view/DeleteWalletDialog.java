package com.easyetz.view;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.easyetz.R;
import com.easyetz.modules.walletoperation.send.SendPopupWindow;


public class DeleteWalletDialog extends Dialog implements View.OnClickListener {

    private TextView btn_del;
    protected OnDeleteClickListener mListener;
    public DeleteWalletDialog(@NonNull Context context) {
        super(context);
    }

    public DeleteWalletDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_delete_wallet);
        setCanceledOnTouchOutside(false);
        btn_del =  findViewById(R.id.btn_del);
        //初始化界面控件的事件
        initEvent();
    }

    private void initEvent() {
        btn_del.setOnClickListener(this);
        findViewById(R.id.lly_close).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_del:// 确定
                if (mListener!=null){
                    mListener.setOnDeleteClick();
                }
                break;
            case R.id.lly_close:
                dismiss();
                break;
        }
    }

    /**
     * 定义一个接口，公布出去 在Activity中操作按钮的单击事件
     */
    public interface OnDeleteClickListener {
        void setOnDeleteClick();
    }

    public void setOnDeleteClickListener(DeleteWalletDialog.OnDeleteClickListener listener) {
        this.mListener = listener;
    }


}
