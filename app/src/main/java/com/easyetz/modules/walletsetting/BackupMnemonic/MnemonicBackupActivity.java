package com.easyetz.modules.walletsetting.BackupMnemonic;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.mobstat.IIgnoreAutoTrace;
import com.easyetz.R;
import com.easyetz.base.BaseActivity;
import com.easyetz.modules.main.MainActivity;
import com.easyetz.modules.normalvp.NormalPresenter;
import com.easyetz.modules.normalvp.NormalView;
import com.easyetz.view.MnemonicBackupAlertDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MnemonicBackupActivity extends BaseActivity<NormalView, NormalPresenter>implements NormalView,IIgnoreAutoTrace {
//public class MnemonicBackupActivity extends BaseActivity<NormalView, NormalPresenter>implements NormalView {
    private static final int VERIFY_MNEMONIC_BACKUP_REQUEST = 1101;
    @BindView(R.id.tv_mnemonic)
    TextView tvMnemonic;
    @BindView(R.id.iv_back)
    ImageView ivBack;
    private String walletMnemonic;
    private String walletId;
    private int from;//第一次创建为1，2...为2，从钱包设置为0


    @Override
    protected int getLayoutId() {
        return R.layout.activity_mnemonic_backup;
    }

    @Override
    public NormalPresenter initPresenter() {
        return new NormalPresenter();
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        setCenterTitle(getString(R.string.mnemonic_backup_title));
        MnemonicBackupAlertDialog mnemonicBackupAlertDialog = new MnemonicBackupAlertDialog(this, R.style.MyDialog);
        mnemonicBackupAlertDialog.show();
        Intent intent = getIntent();
        from = intent.getIntExtra("from", 0);
        walletId = intent.getStringExtra("walletId");
        walletMnemonic = intent.getStringExtra("walletMnemonic");

        if (from != 0) {
            ivBack.setVisibility(View.INVISIBLE);
        }
        tvMnemonic.setText(walletMnemonic);
    }

    @Override
    protected void initData() {

    }

    @Override
    public void initEvent() {

    }

    @OnClick(R.id.btn_next)
    public void onClick(View view) {
        Intent intent = new Intent(this, VerifyMnemonicBackupActivity.class);
        intent.putExtra("walletId", walletId);
        intent.putExtra("walletMnemonic", walletMnemonic);
        startActivityForResult(intent, VERIFY_MNEMONIC_BACKUP_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VERIFY_MNEMONIC_BACKUP_REQUEST) {
            if (data != null) {
                if (from ==1)
                    startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(from!=0&&event.getKeyCode() == KeyEvent.KEYCODE_BACK ) {
            return true;
        }else {
            return super.dispatchKeyEvent(event);
        }
    }
}
