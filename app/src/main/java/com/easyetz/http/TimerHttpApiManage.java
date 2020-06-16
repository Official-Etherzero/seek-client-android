package com.easyetz.http;

import android.content.Context;
import android.os.Handler;
import android.os.NetworkOnMainThreadException;
import android.support.annotation.WorkerThread;

import com.easyetz.app.MyApp;
import com.easyetz.base.BaseUrl;
import com.easyetz.bean.CurrencyEntity;
import com.easyetz.bean.ETHRatesBean;
import com.easyetz.bean.SeekRateBean;
import com.easyetz.bean.ResponseBean;
import com.easyetz.blockchain.BtcWalletManager;
import com.easyetz.blockchain.EthWalletManager;
import com.easyetz.blockchain.EtzWalletManager;
import com.easyetz.http.callback.JsonCallback;
import com.easyetz.modules.tools.threads.ETZExecutor;
import com.easyetz.sqlite.RatesDataSource;
import com.easyetz.utils.ActivityUtils;
import com.easyetz.utils.MyLog;
import com.easyetz.utils.SharedPrefsUitls;
import com.easyetz.utils.Util;
import com.lzy.okgo.model.Response;


import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;


public class TimerHttpApiManage {

    private static TimerHttpApiManage instance;
    private Timer timer;

    private TimerTask timerTask;

    private Handler handler;

    private static String seekusdt = "0";

    private TimerHttpApiManage() {
        handler = new Handler();
    }

    public static TimerHttpApiManage getInstance() {

        if (instance == null) {
            instance = new TimerHttpApiManage();
        }
        return instance;
    }

    /**
     * 法币相对于Btc的汇率
     *
     * @param context
     */
    @WorkerThread
    private void updateRates(Context context) {
        if (ActivityUtils.isMainThread()) {
            throw new NetworkOnMainThreadException();
        }
        HttpRequets.getRequets(BaseUrl.HTTP_UPDATE_RATES, getClass(), new HashMap<String, String>(), new JsonCallback<ResponseBean<List<CurrencyEntity>>>() {

            @Override
            public void onSuccess(com.lzy.okgo.model.Response<ResponseBean<List<CurrencyEntity>>> response) {
                if (response.body() == null) return;
                List<CurrencyEntity> list = response.body().data;
                Set<CurrencyEntity> set = new LinkedHashSet<>();
                for (CurrencyEntity ce : list) {
//                    if (ce.code.equalsIgnoreCase("USD")) {
//                        String code = "BTC";
//                        String name = "SeekChain";
//                        String rate = (1/Float.valueOf(ce.rate))* Float.valueOf(seekusdt) + "";
//                        MyLog.i("updateErc20Rates==" + rate);
//                        MyLog.i("updateErc20Rates==" + seekusdt);
//                        String iso = "SEEK";
//                        CurrencyEntity seek = new CurrencyEntity(code, name, Float.valueOf(rate), iso);
//                        set.add(seek);
//                    }
                    set.add(ce);
                }
                if (set.size() > 0)
                    RatesDataSource.getInstance(context).putCurrencies(context, set);
            }
        });


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
                                updateData(context);
                            }
                        });
                    }
                });
            }
        };
    }

    @WorkerThread
    private void updateData(final Context context) {

        if (MyApp.isAppInBackground(context)) {
            MyLog.e("doInBackground: Stopping timer, no activity on.");
            stopTimerTask();
            return;
        }
        ETZExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                updateETZRates(context);
                updateErc20Rates(context);
                updateCurrentBalances();
            }
        });

        ETZExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                //get each wallet's rates
                updateRates(context);

            }
        });

    }

    /**
     * ETZ相对于Btc汇率
     *
     * @param context
     */
    @WorkerThread
    private synchronized void updateETZRates(Context context) {

        HttpRequets.getRequets(BaseUrl.HTTP_SEEK_RATE, getClass(), new HashMap<String, String>(), new JsonCallback<SeekRateBean>() {
            @Override
            public void onSuccess(Response<SeekRateBean> response) {
                MyLog.i(response.toString());
                if (response.body() != null) {
                    seekusdt = response.body().price;
                }

            }

            @Override
            public void onError(Response<SeekRateBean> response) {
            }
        });
    }

    /***
     * 代币包括EASH相对于Btc汇率
     * @param context
     */
    @WorkerThread
    private synchronized void updateErc20Rates(Context context) {

        if (Util.isNullOrEmpty(seekusdt)) return;
        HttpRequets.getRequets(BaseUrl.HTTP_ETH_RATES, getClass(), new HashMap<String, String>(), new JsonCallback<List<ETHRatesBean>>() {

            @Override
            public void onSuccess(Response<List<ETHRatesBean>> response) {
                List<ETHRatesBean> list = response.body();
                Set<CurrencyEntity> tmp = new LinkedHashSet<>();
                for (ETHRatesBean eb : list) {
                    if (eb.symbol.equalsIgnoreCase("USDT")) {
                        String code = "BTC";
                        String name = "SeekChain";
                        String rate = Float.valueOf(eb.price_btc) * Float.valueOf(seekusdt) + "";
//                        MyLog.i("updateErc20Rates==" + eb.price_btc);
//                        MyLog.i("updateErc20Rates==" + seekusdt);
                        String iso = "SEEK";
                        CurrencyEntity seek = new CurrencyEntity(code, name, Float.valueOf(rate), iso);
                        tmp.add(seek);
                    }
                }
                RatesDataSource.getInstance(context).putCurrencies(context, tmp);
            }
        });

    }

    @WorkerThread
    private synchronized void updateCurrentBalances() {
        //获取当前钱包
        String wid = SharedPrefsUitls.getInstance().getCurrentWallet();
        if (wid.startsWith("SEEK")) {
            EtzWalletManager.getInstance().setTokenList(SharedPrefsUitls.getInstance().getWalletTokenList(wid));
            EtzWalletManager.getInstance().updateBalance(wid);
        } else if (wid.startsWith("BTC")) {
            BtcWalletManager.getInstance().updateBalance(wid);
        } else if (wid.startsWith("ETH")) {
            EthWalletManager.getInstance().setTokenList(SharedPrefsUitls.getInstance().getWalletTokenList(wid));
            EthWalletManager.getInstance().updateBalance(wid);
        }

    }

    public void startTimer(Context context) {
        //set a new Timer
        if (timer != null) return;
        timer = new Timer();
        MyLog.e("startTimer: started...");
        //initialize the TimerTask's job
        initializeTimerTask(context);

        timer.schedule(timerTask, 1000, 15000);
    }

    public void stopTimerTask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }


}
