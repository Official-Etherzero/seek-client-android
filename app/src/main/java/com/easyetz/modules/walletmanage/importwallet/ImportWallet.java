package com.easyetz.modules.walletmanage.importwallet;

import android.os.Bundle;
import android.support.v4.view.ViewPager;

import com.easyetz.R;
import com.easyetz.adapter.LoadWalletPageFragmentAdapter;
import com.easyetz.base.BaseActivity;
import com.easyetz.base.BaseFragment;
import com.easyetz.base.Constants;
import com.easyetz.modules.walletmanage.createwallet.CreateWalletPresenter;
import com.easyetz.modules.walletmanage.createwallet.CreateWalletView;
import com.easyetz.modules.walletmanage.fragment.ImportKeystoreFragment;
import com.easyetz.modules.walletmanage.fragment.ImportMnemonicFragment;
import com.easyetz.modules.walletmanage.fragment.ImportPrivateKeyFragment;
import com.easyetz.utils.Util;
import com.shizhefei.view.indicator.IndicatorViewPager;
import com.shizhefei.view.indicator.ScrollIndicatorView;
import com.shizhefei.view.indicator.slidebar.TextWidthColorBar;
import com.shizhefei.view.indicator.transition.OnTransitionTextListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class ImportWallet extends BaseActivity<CreateWalletView, CreateWalletPresenter> implements CreateWalletView {
//public class ImportWallet extends BaseActivity<CreateWalletView, CreateWalletPresenter> implements CreateWalletView  {

    @BindView(R.id.indicator_view)
    ScrollIndicatorView indicatorView;
    @BindView(R.id.vp_load_wallet)
    ViewPager vpLoadWallet;

    String type;

    private List<BaseFragment> fragmentList = new ArrayList<>();
    private LoadWalletPageFragmentAdapter loadWalletPageFragmentAdapter;
    private IndicatorViewPager indicatorViewPager;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_import_wallet;
    }

    @Override
    public CreateWalletPresenter initPresenter() {
        return new CreateWalletPresenter();
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        type = getIntent().getStringExtra(Constants.WALLET_TYPE);
        fragmentList.add(new ImportMnemonicFragment());
        fragmentList.add(new ImportPrivateKeyFragment());
        if (!type.equalsIgnoreCase("BTC")){
            fragmentList.add(new ImportKeystoreFragment());
        }

        setCenterTitle(getResources().getString(R.string.load_wallet_btn));

        indicatorView.setSplitAuto(true);
        indicatorView.setOnTransitionListener(new OnTransitionTextListener()
                .setColor(getResources().getColor(R.color.zt_lu), getResources().getColor(R.color.zt_hui))
                .setSize(14, 14));
        indicatorView.setScrollBar(new TextWidthColorBar(this, indicatorView, getResources().getColor(R.color.zt_lu), Util.dip2px(2)));
        indicatorView.setScrollBarSize(50);
        indicatorViewPager = new IndicatorViewPager(indicatorView, vpLoadWallet);
        loadWalletPageFragmentAdapter = new LoadWalletPageFragmentAdapter(this, getSupportFragmentManager(), fragmentList);
        indicatorViewPager.setAdapter(loadWalletPageFragmentAdapter);
        indicatorViewPager.setCurrentItem(0, false);
        vpLoadWallet.setOffscreenPageLimit(4);

    }

    @Override
    protected void initData() {

    }

    @Override
    public void initEvent() {

    }

    @Override
    public void getWalletSuccess(String address) {

    }

    @Override
    public void getWalletFail(String msg) {

    }

}
