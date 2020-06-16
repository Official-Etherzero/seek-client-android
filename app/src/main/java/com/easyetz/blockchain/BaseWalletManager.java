package com.easyetz.blockchain;

import android.content.Context;

import com.easyetz.bean.CurrencyEntity;

import java.math.BigDecimal;

public interface BaseWalletManager {

//    String getAddress();
//    boolean isAddressValid(String address);
//    //get the currency symbol e.g. Bitcoin - ₿, Ether - Ξ
//    String getSymbol(Context app);
//
//    //get the currency denomination e.g. Bitcoin - BTC, Ether - ETH
//    String getIso();
//
//    //get the currency scheme (bitcoin or bitcoincash)
//    String getScheme();
//    String getStartColor();
//    String getEndColor();
//    String getIcon();
//    String getDecimals();


//
//    //get the currency name e.g. Bitcoin
//    String getWalletName();
//
//    //get the currency denomination e.g. BCH, mBCH, Bits
//    String getDenominator();
//
////    String getTokenContractAddress(String tokenIso);
//    String getWalletId();
//
//    String getWalletKeyStore();
//
//    String getWalletPrivateKey();
//
//    byte[] signAndPublishTransaction();
    void updateBalance(String wid);
    void setmAddress(String address);
    /**
     * @param amount - the smallest denomination amount in crypto (e.g. satoshis)
     * @return - the crypto value of the amount in the current favorite denomination (e.g. BTC, mBTC, Bits..)
     */
    BigDecimal getCryptoForSmallestCrypto(Context app, BigDecimal amount);

    //get the number of decimal places to use for this currency
    int getMaxDecimalPlaces(Context app,String iso);

    /**
     * @return - the total balance amount in the user's favorite fiat currency (e.g. dollars)
     */
    BigDecimal getFiatBalance(String iso,String balance);

    /**
     * @param amount - the smallest denomination amount in current wallet's crypto (e.g. Satoshis)
     * @param ent    - provide a currency entity if needed
     * @return - the fiat value of the amount in crypto (e.g. dollars)
     * or null if there is no fiat exchange data from the API yet
     */
    BigDecimal getFiatForSmallestCrypto(String iso, BigDecimal amount, CurrencyEntity ent);

    /**
     * @return - the wallet's currency exchange rate in the user's favorite fiat currency (e.g. dollars)
     */
    BigDecimal getFiatExchangeRate(Context app,String iso);

}
