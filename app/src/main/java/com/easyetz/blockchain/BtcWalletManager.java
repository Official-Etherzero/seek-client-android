package com.easyetz.blockchain;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LifecycleService;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;

import com.easyetz.app.MyApp;
import com.easyetz.base.Constants;
import com.easyetz.bean.CurrencyEntity;
import com.easyetz.bean.WalletBean;
import com.easyetz.blockchain.data.WalletBalanceLiveData;
import com.easyetz.blockchain.walletutils.BtcWalletUtil;
import com.easyetz.sqlite.RatesDataSource;
import com.easyetz.sqlite.WalletDataStore;
import com.easyetz.utils.MyLog;
import com.easyetz.utils.SharedPrefsUitls;
import com.easyetz.utils.Util;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.utils.Threading;
import org.bitcoinj.wallet.Protos;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletChangeEventListener;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;
import org.bitcoinj.wallet.listeners.WalletCoinsSentEventListener;
import org.bitcoinj.wallet.listeners.WalletReorganizeEventListener;

import java.math.BigDecimal;
import java.util.List;

public class BtcWalletManager implements BaseWalletManager  {
    private WalletBean btcWallet;
    public static final int ONE_BITCOIN_IN_SATOSHIS = 100000000; // 1 Bitcoin in satoshis, 100 millions
    private static final long MAXIMUM_AMOUNT = 21000000; // Maximum number of coins available
    private static final int SYNC_MAX_RETRY = 3;
    private static Wallet wallet;
    public static WalletBalanceLiveData  wbld;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private static BtcWalletManager instance;
    public synchronized static BtcWalletManager getInstance() {
        if (instance == null) {
            instance = new BtcWalletManager();
            String wid = SharedPrefsUitls.getInstance().getCurrentWallet();
            WalletBean wb = WalletDataStore.getInstance().queryWallet(wid);
//            wallet = BtcWalletUtil.getWalletAsync();
            wbld=new WalletBalanceLiveData(MyApp.getmInstance());
        }
        return instance;
    }

    @Override
    public void updateBalance(String wid) {

        MyLog.i("coin1-------------" + wbld.getValue());
//        Coin coin1 = wallet.getTotalReceived();
//        MyLog.i("coin1-------------" + coin1.getValue());
//        Coin coin2 = wallet.getBalance(Wallet.BalanceType.AVAILABLE);
//        Coin coin3 = wallet.getBalance(Wallet.BalanceType.ESTIMATED);
//        MyLog.i("coin3-------------" + coin3.getValue());

    }

    @Override
    public void setmAddress(String address) {

    }
    protected static void load() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                org.bitcoinj.core.Context.propagate(new org.bitcoinj.core.Context(BtcWalletUtil.getParams()));
                Coin coin=wallet.getBalance();
                MyLog.i("coin-------------" + coin.getValue());
            }
        });
    }
    @Override
    public BigDecimal getCryptoForSmallestCrypto(Context app, BigDecimal amount) {
        if (amount.doubleValue() == 0) return amount;
        BigDecimal result = BigDecimal.ZERO;
//        int unit = BRSharedPrefs.getCryptoDenomination(app, getIso());
//        switch (unit) {
//            case Constants.CURRENT_UNIT_BITS:
//                result = amount.divide(new BigDecimal("100"), 2, ROUNDING_MODE);
//                break;
//            case Constants.CURRENT_UNIT_MBITS:
//                result = amount.divide(new BigDecimal("100000"), 5, ROUNDING_MODE);
//                break;
//            case Constants.CURRENT_UNIT_BITCOINS:
//                result = amount.divide(new BigDecimal("100000000"), 8, ROUNDING_MODE);
//                break;
//        }
        return result;
    }

    @Override
    public int getMaxDecimalPlaces(Context app, String iso) {
        return 0;
    }

    @Override
    public BigDecimal getFiatBalance(String iso, String balance) {
        BigDecimal ba = getFiatForSmallestCrypto(iso, new BigDecimal(Util.isNullOrEmpty(balance) ? "0" : balance), null);
        if (ba == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(ba.doubleValue());
    }

    @Override
    public BigDecimal getFiatForSmallestCrypto(String iso, BigDecimal amount, CurrencyEntity ent) {
        if (amount == null) return BigDecimal.ZERO;
        if (amount.doubleValue() == 0) {
            return amount;
        }
        String fiatIso = SharedPrefsUitls.getInstance().getPreferredFiatIso();
        if (ent == null) {
            ent = RatesDataSource.getInstance(MyApp.getmInstance()).getCurrencyByCode(MyApp.getBreadContext(), "BTC", fiatIso);
        }
        if (ent == null) {
            return BigDecimal.ZERO;
        }
        double rate = ent.rate;
        //get crypto amount
        BigDecimal cryptoAmount = amount.divide(new BigDecimal(ONE_BITCOIN_IN_SATOSHIS), getMaxDecimalPlaces(MyApp.getmInstance(), "BTC"), Constants.ROUNDING_MODE);
        return cryptoAmount.multiply(new BigDecimal(rate));
    }

    @Override
    public BigDecimal getFiatExchangeRate(Context app,String iso) {
        CurrencyEntity ent = RatesDataSource.getInstance(app).getCurrencyByCode(app, "BTC", SharedPrefsUitls.getInstance().getPreferredFiatIso());
        if (ent == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(ent.rate); //dollars
    }


}
