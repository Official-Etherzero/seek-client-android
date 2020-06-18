package com.easyetz.modules.walletsetting.updatePassword;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.easyetz.R;
import com.easyetz.base.BaseActivity;
import com.easyetz.bean.WalletBean;
import com.easyetz.modules.walletsetting.ModifyWalletInteract;
import com.easyetz.utils.Md5Utils;
import com.easyetz.utils.ToastUtils;
import com.easyetz.utils.Util;
import com.easyetz.view.MEdit;
import com.easyetz.view.MText;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

public class UpdatePasswordActivity extends BaseActivity<UpdatePwdView, UpdatePwdPresenter> implements UpdatePwdView {


    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.pwd_wallet_icon)
    CircleImageView pwdWalletIcon;
    @BindView(R.id.pwd_wallet_name)
    MText pwdWalletName;
    @BindView(R.id.pwd_wallet_address)
    MText pwdWalletAddress;
    @BindView(R.id.pwd_wallet_rl)
    RelativeLayout pwdWalletRl;
    @BindView(R.id.current_pwd)
    MEdit currentPwd;
    @BindView(R.id.new_pwd)
    MEdit newPwd;
    @BindView(R.id.affirm_pwd)
    MEdit affirmPwd;
    @BindView(R.id.updata_pwd_btn)
    TextView updata_pwd_btn;

    WalletBean walletBean;
    ModifyWalletInteract modifyWalletInteract;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_update_password;
    }

    @Override
    public UpdatePwdPresenter initPresenter() {
        return new UpdatePwdPresenter();
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {

        walletBean = getIntent().getParcelableExtra("wallet");
        modifyWalletInteract = new ModifyWalletInteract();
        Drawable drawable = getResources().getDrawable(R.drawable.crypto_card_shape, null).mutate();
        //create gradient with 2 colors if exist
        ((GradientDrawable) drawable).setColors(new int[]{Color.parseColor(walletBean.getStartColor()), Color.parseColor(walletBean.getEndColor() == null ? walletBean.getStartColor() : walletBean.getEndColor())});
        ((GradientDrawable) drawable).setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
        pwdWalletRl.setBackground(drawable);
        tvTitle.setText(R.string.Wallet_set_pwd);
        int iconResourceId = getResources().getIdentifier(walletBean.getId().substring(0, 3).toLowerCase(), "mipmap", getPackageName());
        pwdWalletIcon.setImageResource(iconResourceId);
        pwdWalletName.setText(walletBean.getName());
        pwdWalletAddress.setText(walletBean.getAddress());

    }

    @Override
    protected void initData() {

    }

    @Override
    public void initEvent() {
        currentPwd.addTextChangedListener(watcher);
        newPwd.addTextChangedListener(watcher);
        affirmPwd.addTextChangedListener(watcher);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }

    @OnClick({R.id.iv_back, R.id.updata_pwd_btn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                onBackPressed();
                break;
            case R.id.updata_pwd_btn:
                String oldPwd = currentPwd.getText().toString().trim();
                String pwd = newPwd.getText().toString().trim();
                String pwdAgain = affirmPwd.getText().toString().trim();
                if (presenter.verifyPassword(activity, oldPwd, pwd, pwdAgain, walletBean.getPassword())) {
                    modifyWalletInteract.modifyWalletPwd(walletBean.getId(), oldPwd, pwd).subscribe(this::modifyPwdSuccess);
                }
                break;
        }
    }

    TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            String oldPwd = currentPwd.getText().toString().trim();
            String pwd = newPwd.getText().toString().trim();
            String pwdAgain = affirmPwd.getText().toString().trim();
            if (Util.isNullOrEmpty(oldPwd) || Util.isNullOrEmpty(pwd) || Util.isNullOrEmpty(pwdAgain)) {
                updata_pwd_btn.setEnabled(false);
            } else {
                updata_pwd_btn.setEnabled(true);
            }
        }
    };

    public void modifyPwdSuccess(boolean b) {
        if (b) {
            ToastUtils.showLongToast(activity, R.string.modify_password_success);
            finish();
        } else
            ToastUtils.showLongToast(activity, R.string.modify_password_fail);

    }
}
