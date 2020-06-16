package com.easyetz.modules.createrecovery;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.easyetz.R;
import com.easyetz.base.BaseActivity;
import com.easyetz.base.Constants;
import com.easyetz.modules.main.MainActivity;
import com.easyetz.modules.normalvp.NormalPresenter;
import com.easyetz.modules.normalvp.NormalView;
import com.easyetz.modules.walletmanage.createwallet.CreateWallet;
import com.easyetz.modules.walletmanage.importwallet.ImportWallet;
import com.easyetz.utils.SharedPrefsUitls;
import com.easyetz.utils.Util;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class CreateRecoveryActivity extends BaseActivity<NormalView, NormalPresenter> implements NormalView {

    @Override
    protected int getLayoutId() {
        return R.layout.activity_create_recovery;
    }

    @Override
    public NormalPresenter initPresenter() {
        return new NormalPresenter();
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        String wid=SharedPrefsUitls.getInstance().getCurrentWallet();
        if (!Util.isNullOrEmpty(wid)){
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected void initData() {

    }

    @Override
    public void initEvent() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }


    @OnClick({R.id.button_new_wallet, R.id.button_recover_wallet})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.button_new_wallet:
                startActivity(new Intent(this, CreateWallet.class).putExtra("from",1));
                break;
            case R.id.button_recover_wallet:
                Intent intent=new Intent(this, ImportWallet.class);
                intent.putExtra(Constants.WALLET_TYPE,"SEEK");
                intent.putExtra("from",1);
                startActivity(intent);
                break;
        }
    }
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
