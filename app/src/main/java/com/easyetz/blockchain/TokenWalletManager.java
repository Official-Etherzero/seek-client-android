package com.easyetz.blockchain;

import android.content.Context;

import com.easyetz.app.MyApp;
import com.easyetz.base.Constants;
import com.easyetz.bean.CurrencyEntity;
import com.easyetz.bean.Token;
import com.easyetz.sqlite.RatesDataSource;
import com.easyetz.utils.BalanceUtils;
import com.easyetz.utils.MyLog;
import com.easyetz.utils.SharedPrefsUitls;
import com.easyetz.utils.TokenUtil;
import com.easyetz.utils.Util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

public class TokenWalletManager implements BaseWalletManager{

    private Token tokenWallet;

    private static TokenWalletManager instance;
    public synchronized static TokenWalletManager getInstance(Context app) {
        if (instance == null) {
            instance = new TokenWalletManager();
        }
        return instance;
    }

    @Override
    public void updateBalance(String wid) {

    }

    @Override
    public void setmAddress(String address) {

    }

    @Override
    public BigDecimal getCryptoForSmallestCrypto(Context app, BigDecimal amount) {
        if (amount == null) return BigDecimal.ZERO;
        return BalanceUtils.weiToEth(amount.toBigInteger()); //only using Tokens
    }

    @Override
    public int getMaxDecimalPlaces(Context app,String iso) {
        int tokenDecimals = Objects.requireNonNull(TokenUtil.getTokenItem(iso)).decimals;
        boolean isMaxDecimalLargerThanTokenDecimals = Constants.MAX_DECIMAL_PLACES > tokenDecimals;
        return isMaxDecimalLargerThanTokenDecimals ? tokenDecimals : Constants.MAX_DECIMAL_PLACES;
    }
    @Override
    public BigDecimal getFiatBalance(String iso,String balance) {
        return getFiatForSmallestCrypto(iso, BalanceUtils.weiToEth(new BigInteger(Util.isNullOrEmpty(balance)?"0":balance)), null);
    }

    @Override
    public BigDecimal getFiatForSmallestCrypto(String iso, BigDecimal amount, CurrencyEntity ent) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        String fiso = SharedPrefsUitls.getInstance().getPreferredFiatIso();
        if (ent != null) {
            //passed in a custom CurrencyEntity
            //get crypto amount
            //multiply by fiat rate
            return amount.multiply(new BigDecimal(ent.rate));
        }

        BigDecimal fiatData = getFiatForToken(iso,amount, fiso);
        if (fiatData == null) return BigDecimal.ZERO;
        return fiatData;
    }

    @Override
    public BigDecimal getFiatExchangeRate(Context app,String iso) {
        BigDecimal fiatData = getFiatForToken(iso, new BigDecimal(1), SharedPrefsUitls.getInstance().getPreferredFiatIso());
        if (fiatData == null) return BigDecimal.ZERO;
        return fiatData; //dollars
    }

    private BigDecimal getFiatForToken(String iso, BigDecimal tokenAmount, String code) {
        //fiat rate for btc
        CurrencyEntity btcRate = RatesDataSource.getInstance(MyApp.getmInstance()).getCurrencyByCode(MyApp.getmInstance(), "BTC", code);

        //Btc rate for the token
        CurrencyEntity tokenBtcRate = RatesDataSource.getInstance(MyApp.getmInstance()).getCurrencyByCode(MyApp.getmInstance(), iso, "BTC");
        if (btcRate == null) {
            MyLog.e( "getUsdFromBtc: No USD rates for BTC");
            return BigDecimal.ZERO;
        }
        if (tokenBtcRate == null) {
            MyLog.e( "getUsdFromBtc: No BTC rates for ETH");
            return BigDecimal.ZERO;
        }
        if (tokenBtcRate.rate == 0 || btcRate.rate == 0) return BigDecimal.ZERO;

        return tokenAmount.multiply(new BigDecimal(tokenBtcRate.rate)).multiply(new BigDecimal(btcRate.rate));
    }
}
