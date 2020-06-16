package com.easyetz.modules.main.my.wallets;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.easyetz.R;
import com.easyetz.adapter.WalletsAdapter;
import com.easyetz.adapter.baseAdapter.WalletManageAdapter;
import com.easyetz.base.BaseActivity;
import com.easyetz.base.Constants;
import com.easyetz.bean.WalletBean;
import com.easyetz.modules.normalvp.NormalPresenter;
import com.easyetz.modules.normalvp.NormalView;
import com.easyetz.modules.walletmanage.createwallet.CreateWallet;
import com.easyetz.modules.walletmanage.importwallet.ImportWallet;
import com.easyetz.modules.walletsetting.WalletSetting;
import com.easyetz.sqlite.WalletDataStore;
import com.easyetz.view.MText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WalletsManageActivity extends BaseActivity<NormalView, NormalPresenter> implements NormalView {

    @BindView(R.id.wm_lv_wallet)
    RecyclerView wmLvWallet;
    @BindView(R.id.wm_ctrate)
    MText wmCtrate;
    @BindView(R.id.wm_import)
    MText wmImport;
    private WalletManageAdapter walletsAdapter;
    private LinearLayoutManager linearLayoutwallet;
    @Override
    protected int getLayoutId() {
        return R.layout.activity_wallets_manage;
    }

    @Override
    public NormalPresenter initPresenter() {
        return new NormalPresenter();
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        setCenterTitle("钱包管理");
        linearLayoutwallet = new LinearLayoutManager(WalletsManageActivity.this, LinearLayoutManager.VERTICAL, false);
        wmLvWallet.setLayoutManager(linearLayoutwallet);
        walletsAdapter = new WalletManageAdapter(R.layout.my_wallets_list_item,new ArrayList<>() );
        wmLvWallet.setAdapter(walletsAdapter);
    }

    @Override
    protected void initData() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        walletsAdapter.setTokens(getWalletList());
    }

    @Override
    public void initEvent() {
        walletsAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                Intent intent =new Intent(WalletsManageActivity.this,WalletSetting.class);
                intent.putExtra("wallet", (WalletBean) adapter.getItem(position));
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }

    @OnClick({R.id.wm_ctrate, R.id.wm_import})
    public void onViewClicked(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.wm_ctrate:
//                StatService.onEvent(this, "wm_ctrate", "wm_ctrate");
                intent=new Intent(WalletsManageActivity.this, CreateWallet.class);
                intent.putExtra("from",2);
                startActivity(intent);
                break;
            case R.id.wm_import:
                Map<String, String> attributes = new HashMap<String, String>();
                attributes.put("import_wallet", "aaaaaaaaaaaaaaaaa");
//                StatService.setAttributes(wmImport, attributes);
                intent=new Intent(WalletsManageActivity.this, ImportWallet.class);
                intent.putExtra(Constants.WALLET_TYPE,"SEEK");
                startActivity(intent);
                break;
        }
    }
    public List<WalletBean> getWalletList() {
        return WalletDataStore.getInstance().queryAllWallets();
    }
}
