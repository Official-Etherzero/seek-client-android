package com.easyetz.modules.main.property;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

//import com.baidu.mobstat.StatService;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.easyetz.R;
import com.easyetz.adapter.PropertyAdapter;
import com.easyetz.adapter.WalletsAdapter;
import com.easyetz.base.BaseFragment;
import com.easyetz.base.Constants;
import com.easyetz.bean.CryptoRequest;
import com.easyetz.bean.Token;
import com.easyetz.bean.WalletBean;
import com.easyetz.blockchain.BtcWalletManager;
import com.easyetz.modules.WebViewActivity;
import com.easyetz.modules.main.MainActivity;
import com.easyetz.modules.main.my.wallets.WalletsManageActivity;
import com.easyetz.modules.tokenmanage.TokenManageActivity;
import com.easyetz.modules.tools.threads.ETZExecutor;
import com.easyetz.modules.walletmanage.WalletsMaster;
import com.easyetz.modules.walletmanage.createwallet.CreateWallet;
import com.easyetz.modules.walletmanage.importwallet.ImportWallet;
import com.easyetz.modules.walletoperation.receive.ReceiveQrCodeActivity;
import com.easyetz.modules.walletoperation.send.SendActivity;
import com.easyetz.modules.walletoperation.wallet.WalletActivity;
import com.easyetz.modules.walletsetting.WalletSetting;
import com.easyetz.utils.ClipboardManager;
import com.easyetz.utils.CryptoUriParser;
import com.easyetz.utils.CurrencyUtils;
import com.easyetz.utils.ETZAnimator;
import com.easyetz.utils.MyLog;
import com.easyetz.utils.SharedPrefsUitls;
import com.easyetz.utils.Util;
import com.easyetz.view.MText;
import com.gyf.barlibrary.ImmersionBar;

import org.bitcoinj.core.Coin;

import java.util.ArrayList;
import java.util.Currency;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class PropertyFragment extends BaseFragment<PropertyView, PropertyPresenter> implements PropertyView, View.OnClickListener {
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    Toolbar mToolbar;
    @BindView(R.id.lv_wallet)
    RecyclerView lvWallet;
    @BindView(R.id.drawer)
    DrawerLayout drawer;
    Unbinder unbinder;
    @BindView(R.id.tv_total_value)
    TextView tvTotalValue;
    @BindView(R.id.property_wallet_address)
    TextView tvWalletAddress;
    @BindView(R.id.current_walllet_name)
    TextView currentWalletName;
    @BindView(R.id.home_total_labe)
    TextView totalLabe;
    @BindView(R.id.vip_rank)
    MText vip_rank;
    @BindView(R.id.rel_vip)
    RelativeLayout rel_vip;
    @BindView(R.id.lly_add_token)
    ImageView lly_add_token;
    @BindView(R.id.rl_banner)
    RelativeLayout rl_banner;

    private LinearLayoutManager linearLayoutManager;
    private LinearLayoutManager linearLayoutwallet;
    private PropertyAdapter recyclerAdapter;
    private WalletsAdapter walletsAdapter;
    private WalletBean wallet;
    private Timer timer;

    private TimerTask timerTask;
    private Handler handler;

    @Override
    public PropertyPresenter initPresenter() {
        return new PropertyPresenter();
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        linearLayoutwallet = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        //设置布局管理器
        recyclerView.setLayoutManager(linearLayoutManager);
        lvWallet.setLayoutManager(linearLayoutwallet);
        //        //设置适配器
        recyclerAdapter = new PropertyAdapter(R.layout.wallet_list_item, new ArrayList<>());  //
        walletsAdapter = new WalletsAdapter(R.layout.manage_wallets_list_item, new ArrayList<>());
        recyclerView.setAdapter(recyclerAdapter);
        lvWallet.setAdapter(walletsAdapter);
        handler = new Handler();
        BtcWalletManager.getInstance().wbld.observe(this, new Observer<Coin>() {
            @Override
            public void onChanged(@Nullable Coin coin) {
                MyLog.i("coin1-------------" + coin.getValue());
            }
        });
        recyclerAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {

                Token token = (Token) adapter.getItem(position);
                Intent intent = new Intent(getActivity(), WalletActivity.class);
                intent.putExtra("item", token);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void initData() {

        if (getActivity() == null) return;
        getActivity().runOnUiThread(new Runnable() {
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                wallet = presenter.getCurrentWallet();
                currentWalletName.setText(wallet.getName());
                if (wallet.getId().startsWith("BTC")) lly_add_token.setVisibility(View.INVISIBLE);
                else lly_add_token.setVisibility(View.VISIBLE);
                recyclerAdapter.setTokens(presenter.getTokens(wallet));
                walletsAdapter.setTokens(presenter.getWalletList());
                Currency currency = Currency.getInstance(SharedPrefsUitls.getInstance().getPreferredFiatIso());
                String symbol = currency.getSymbol();
                totalLabe.setText(getResources().getString(R.string.Home_Total_assets) + "(" + symbol + ")");
                String assetValue = CurrencyUtils.getFormattedAmountnotlabe(getActivity(),
                        SharedPrefsUitls.getInstance().getPreferredFiatIso(),
                        presenter.totalAssets(recyclerAdapter.getData()), -1);
//                tvWalletName.setText(wallet.getName());
                tvTotalValue.setText(assetValue);
                tvWalletAddress.setText(wallet.getAddress());
            }
        });

    }


    @Override
    public void initEvent() {
        walletsAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                WalletBean bean = walletsAdapter.getItem(position);
                SharedPrefsUitls.getInstance().putCurrentWallet(bean.getId());
                SharedPrefsUitls.getInstance().putCurrentWalletAddress(bean.getAddress());
                WalletsMaster.getInstance().getWalletByIso(getActivity(), bean.getId().substring(0, 4)).setmAddress(bean.getAddress());
                initData();
                presenter.getMinerMore(wallet.getAddress());
                openOrCloseDrawerLayout();
            }
        });
    }

    @Override
    protected int getContentViewLayoutID() {
        return R.layout.fragment_property01;
    }

    /**
     * 当前包列及资产
     *
     * @param list
     */
    @Override
    public void getCurrentAssets(String list) {

    }

    @Override
    public void setMinerMore(String vip) {
        MyLog.i("setMinerMore=="+vip);
        if (Util.isNullOrEmpty(vip)) {
            rel_vip.setVisibility(View.INVISIBLE);
        } else {
            rel_vip.setVisibility(View.VISIBLE);
            vip_rank.setText("LV." + vip);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: inflate a fragment view
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    // 打开关闭DrawerLayout
    private void openOrCloseDrawerLayout() {
        boolean drawerOpen = drawer.isDrawerOpen(Gravity.END);
        if (drawerOpen) {
            drawer.closeDrawer(Gravity.END);
        } else {
            drawer.openDrawer(Gravity.END);
        }
    }

    //    @OnClick({R.id.lly_menu, R.id.impor_wallet})
//    public void onClick(View view) {
//        Intent intent = null;
////        ETHWallet wallet = null;
//        switch (view.getId()) {
//
//            case R.id.civ_wallet_logo:// 跳转钱包详情
//                intent = new Intent(getActivity(), WalletSetting.class);
//                intent.putExtra("wallet", wallet);
//                startActivity(intent);
//                break;
//
//
//        }
//
//    }
    @OnClick({R.id.property_wallet_address, R.id.btn_scan, R.id.btn_send, R.id.btn_receive, R.id.btn_miner,
            R.id.lly_add_token, R.id.lly_menu, R.id.impor_wallet, R.id.create_wallet, R.id.close_banner, R.id.rl_banner})
    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.property_wallet_address:
                ClipboardManager.putClipboard(MainActivity.getApp(), wallet.address);
                break;
            case R.id.btn_scan:
                ETZAnimator.openScanner(getActivity(), Constants.SCANNER_REQUEST);
                break;
            case R.id.btn_send:
                intent = new Intent(getActivity(), SendActivity.class);
                intent.putExtra("iso", Objects.requireNonNull(recyclerAdapter.getItem(0)).tokenInfo.symbol);
                startActivity(intent);
                break;
            case R.id.btn_receive:
                intent = new Intent(getActivity(), ReceiveQrCodeActivity.class);
                intent.putExtra("tokenInfo", Objects.requireNonNull(recyclerAdapter.getItem(0)).tokenInfo);
                startActivity(intent);
                break;
            case R.id.btn_miner:
                presenter.getMinerMore(wallet.getAddress());
                intent = new Intent(getActivity(), WebViewActivity.class);
                startActivity(intent);
                break;
            case R.id.lly_add_token:
                intent = new Intent(getActivity(), TokenManageActivity.class);
                intent.putExtra("wid", wallet.getId());
                startActivity(intent);
                break;
            case R.id.lly_menu:
                openOrCloseDrawerLayout();
                break;
            case R.id.impor_wallet:// 导入钱包
//                intent = new Intent(getActivity(), SelectWalletType.class);
//                startActivity(intent);
                intent = new Intent(getActivity(), ImportWallet.class);
                intent.putExtra(Constants.WALLET_TYPE, "SEEK");
                startActivity(intent);
                break;
            case R.id.create_wallet:// 创建钱包
                intent = new Intent(getActivity(), CreateWallet.class);
                intent.putExtra("from", 2);
                startActivity(intent);
                break;
            case R.id.close_banner:// 关闭广告
                presenter.getMinerMore(wallet.getAddress());
                rl_banner.setVisibility(View.GONE);
                break;
            case R.id.rl_banner://
                intent = new Intent(getActivity(), WebViewActivity.class);
                startActivity(intent);
                break;

        }
    }

    @Override
    public void onDestroyView() {
        MyLog.i("init1---------------onDestroyView");
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onStop() {
        MyLog.i("init1---------------onStop");
        super.onStop();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onPause() {
        MyLog.i("init1---------------onPause");
        super.onPause();
        stopTimerTask();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ImmersionBar.with(this).destroy();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {

        if (isVisibleToUser) {
            MyLog.i("init1---------------hidden");
            startTimer(getActivity());
            if (wallet != null)
                presenter.getMinerMore(wallet.getAddress());
        } else {
            if (drawer != null) {
                boolean drawerOpen = drawer.isDrawerOpen(Gravity.END);
                if (drawerOpen) {
                    drawer.closeDrawer(Gravity.END);
                }
            }
            MyLog.i("init1---------------");
            stopTimerTask();
//        super.setUserVisibleHint(isVisibleToUser);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        startTimer(getActivity());
        presenter.getMinerMore(wallet.getAddress());
    }


    private void initializeTimerTask(final Context context) {
        timerTask = new TimerTask() {
            public void run() {
                //use a handler to run a toast that shows the current timestamp
                handler.post(new Runnable() {
                    public void run() {
                        ETZExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
                            @Override
                            public void run() {
                                initData();
                            }
                        });
                    }
                });
            }
        };
    }

    public void startTimer(Context context) {
        //set a new Timer
        MyLog.e("init1---------------startTimer: started...");
        if (timer != null) return;
        timer = new Timer();
        MyLog.e("init1---------------startTimer: started...null");
        //initialize the TimerTask's job
        initializeTimerTask(context);

        timer.schedule(timerTask, 1000, 5000);
    }

    public void stopTimerTask() {
        //stop the timer, if it's not already null
        MyLog.e("init1---------------stopTimerTask: started...");
        if (timer != null) {
            MyLog.e("init1---------------stopTimerTask: started...null");
            timer.cancel();
            timer = null;
        }
    }


}
