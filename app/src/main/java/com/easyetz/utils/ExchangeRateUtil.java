package com.easyetz.utils;

import android.content.Context;

import com.easyetz.bean.CurrencyEntity;
import com.easyetz.sqlite.RatesDataSource;

import java.math.BigDecimal;

public class ExchangeRateUtil {

    public static BigDecimal getFiatExchangeRate(Context app,String iso) {
        BigDecimal fiatData = getFiatForEth(app, new BigDecimal(1),SharedPrefsUitls.getInstance().getPreferredFiatIso(),iso);
        if (fiatData == null) return new BigDecimal("0");
        return fiatData; //dollars
    }

    private static BigDecimal getFiatForEth(Context app, BigDecimal ethAmount, String code,String iso) {
        //fiat rate for btc
        CurrencyEntity btcRate = RatesDataSource.getInstance(app).getCurrencyByCode(app, "BTC", code);

        //Btc rate for ether
        CurrencyEntity ethBtcRate = RatesDataSource.getInstance(app).getCurrencyByCode(app, iso, "BTC");
        if (btcRate == null) {
            MyLog.e("getUsdFromBtc: No USD rates for BTC");
            return BigDecimal.ZERO;
        }
        if (ethBtcRate == null) {
            MyLog.e("getUsdFromBtc: No BTC rates for ETH");
            return BigDecimal.ZERO;
        }

        MyLog.i("****************"+ethBtcRate.rate);
        MyLog.i("****************"+btcRate.rate);
        return ethAmount.multiply(new BigDecimal(ethBtcRate.rate)).multiply(new BigDecimal(btcRate.rate));
    }

}
