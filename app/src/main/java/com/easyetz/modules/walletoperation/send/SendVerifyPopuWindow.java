package com.easyetz.modules.walletoperation.send;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.easyetz.R;
import com.easyetz.utils.ToastUtils;
import com.easyetz.utils.Util;
import com.easyetz.view.MText;


public class SendVerifyPopuWindow extends PopupWindow implements View.OnClickListener {
    private MText ruAddress;
    private MText fuAddress;
    private MText sendKGF;
    private MText sendMoney;
    private EditText pwd;
    private LinearLayout signal_layout;
    private LinearLayout input_pwd_layout;
    private View mPopView;
    private OnSendPwdClickListener mListener;
    String to, from, kgf, money;
    private Activity app;


    public SendVerifyPopuWindow(Activity context, String to, String from, String kgf, String money) {
        super(context);
        this.app=context;
        this.to = to;
        this.from = from;
        this.kgf = kgf;
        this.money = money;
        init(context);
        setPopupWindow();

    }

    /**
     * 初始化
     *
     * @param context
     */
    private void init(Context context) {
        // TODO Auto-generated method stub
        LayoutInflater inflater = LayoutInflater.from(context);
        //绑定布局
        mPopView = inflater.inflate(R.layout.pop_send_verify_layout, null);
        ruAddress = mPopView.findViewById(R.id.ru_address);
        fuAddress = mPopView.findViewById(R.id.fu_address);
        sendKGF = mPopView.findViewById(R.id.send_kgf);
        sendMoney = mPopView.findViewById(R.id.send_money);
        pwd = mPopView.findViewById(R.id.pwd_edit);
        signal_layout = mPopView.findViewById(R.id.signal_layout);
        input_pwd_layout = mPopView.findViewById(R.id.input_pwd_layout);
        mPopView.findViewById(R.id.send_btn_confirm).setOnClickListener(this);
        mPopView.findViewById(R.id.send_btn_pwd).setOnClickListener(this);
        mPopView.findViewById(R.id.close_button).setOnClickListener(this);
        mPopView.findViewById(R.id.back_button).setOnClickListener(this);
        ruAddress.setText(to);
        fuAddress.setText(from);
        sendKGF.setText(kgf);
        sendMoney.setText(money);
    }

    /**
     * 设置窗口的相关属性
     */
    @SuppressLint("InlinedApi")
    private void setPopupWindow() {
        this.setContentView(mPopView);// 设置View
        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);// 设置弹出窗口的宽
        this.setHeight(ViewGroup.LayoutParams.MATCH_PARENT);// 设置弹出窗口的高
        this.setFocusable(true);// 设置弹出窗口可
        this.setBackgroundDrawable(new ColorDrawable(0x30000000));// 设置背景透明
        this.setBackgroundDrawable(new BitmapDrawable());//注意这里如果不设置，下面的setOutsideTouchable(true);允许点击外部消失会失效
        this.setOutsideTouchable(true);   //设置外部点击关闭ppw窗口
        this.setFocusable(true);
    }


    /**
     * 定义一个接口，公布出去 在Activity中操作按钮的单击事件
     */
    public interface OnSendPwdClickListener {
        void setOnSendPwd(String pwd);
    }

    public void setOnSendPwdClickListener(OnSendPwdClickListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.close_button:
                dismiss();
                break;
            case R.id.back_button:
                signal_layout.setVisibility(View.VISIBLE);
                input_pwd_layout.setVisibility(View.GONE);
                break;
            case R.id.send_btn_pwd:
                String possword=pwd.getText().toString().trim();
                if (Util.isNullOrEmpty(possword)){
                    ToastUtils.showLongToast(app,R.string.send_input_zhifu_pwd);
                }else {
                    mListener.setOnSendPwd(possword);
                }

                break;
            case R.id.send_btn_confirm:
                signal_layout.setVisibility(View.GONE);
                input_pwd_layout.setVisibility(View.VISIBLE);
                break;
        }
    }


}
