package com.easyetz.utils;

import android.content.Context;

import com.easyetz.base.Constants;
import com.easyetz.blockchain.BaseWalletManager;
import com.easyetz.modules.walletmanage.WalletsMaster;


import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Currency;
import java.util.Locale;


public class CurrencyUtils {
    public static final String TAG = CurrencyUtils.class.getName();

    public static String getFormattedAmount(Context app, String iso, BigDecimal amount) {
        //Use default (wallet's maxDecimal places)
        MyLog.i("getFormattedAmount---"+amount.toString());
        return getFormattedAmount(app, iso, amount, -1);
    }

    /**
     * @param app                       - the Context
     * @param iso                       - the iso for the currency we want to format the amount for
     * @param amount                    - the smallest denomination currency (e.g. dollars or satoshis)
     * @param maxDecimalPlacesForCrypto - max decimal places to use or -1 for wallet's default
     * @return - the formatted amount e.g. $535.50 or b5000
     */
    public static String getFormattedAmount(Context app, String iso, BigDecimal amount, int maxDecimalPlacesForCrypto) {
//        if (amount == null) return "---"; //to be able to detect in a bug
        if (amount == null) amount = BigDecimal.valueOf(0.00); //to be able to detect in a bug
        if (Util.isNullOrEmpty(iso)) throw new RuntimeException("need iso for formatting!");
        DecimalFormat currencyFormat;
        // This formats currency values as the user expects to read them (default locale).
        currencyFormat = (DecimalFormat) DecimalFormat.getCurrencyInstance(Locale.getDefault());
        // This specifies the actual currency that the value is in, and provide
        // s the currency symbol.
        DecimalFormatSymbols decimalFormatSymbols = currencyFormat.getDecimalFormatSymbols();
        BaseWalletManager wallet = WalletsMaster.getInstance().getWalletByIso(app, iso);
        currencyFormat.setGroupingUsed(true);
        currencyFormat.setRoundingMode(Constants.ROUNDING_MODE);

        if (wallet != null) {
            amount = wallet.getCryptoForSmallestCrypto(app, amount);
            MyLog.i("amount ="+amount );
            decimalFormatSymbols.setCurrencySymbol("");
            currencyFormat.setDecimalFormatSymbols(decimalFormatSymbols);
            currencyFormat.setMaximumFractionDigits(maxDecimalPlacesForCrypto == -1 ? wallet.getMaxDecimalPlaces(app, iso) : maxDecimalPlacesForCrypto);
            currencyFormat.setMinimumFractionDigits(0);
            return String.format("%s %s", currencyFormat.format(amount), iso.toUpperCase());
        } else {
            try {
                Currency currency = Currency.getInstance(iso);
                String symbol = currency.getSymbol();
                decimalFormatSymbols.setCurrencySymbol(symbol);
                currencyFormat.setDecimalFormatSymbols(decimalFormatSymbols);
                currencyFormat.setNegativePrefix("-" + symbol);
                currencyFormat.setMaximumFractionDigits(currency.getDefaultFractionDigits());
                currencyFormat.setMinimumFractionDigits(currency.getDefaultFractionDigits());
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            return currencyFormat.format(amount);
        }
    }
    /**
     * @param app                       - the Context
     * @param iso                       - the iso for the currency we want to format the amount for
     * @param amount                    - the smallest denomination currency (e.g. dollars or satoshis)
     * @param maxDecimalPlacesForCrypto - max decimal places to use or -1 for wallet's default
     * @return - the formatted amount e.g. 535.50 or 5000
     */
    public static String getFormattedAmountnotlabe(Context app, String iso, BigDecimal amount, int maxDecimalPlacesForCrypto) {
//        if (amount == null) return "---"; //to be able to detect in a bug
        if (amount == null) amount = BigDecimal.valueOf(0.00); //to be able to detect in a bug
        if (Util.isNullOrEmpty(iso)) throw new RuntimeException("need iso for formatting!");
        DecimalFormat currencyFormat;
        // This formats currency values as the user expects to read them (default locale).
        currencyFormat = (DecimalFormat) DecimalFormat.getCurrencyInstance(Locale.getDefault());
        // This specifies the actual currency that the value is in, and provide
        // s the currency symbol.
        DecimalFormatSymbols decimalFormatSymbols = currencyFormat.getDecimalFormatSymbols();
        BaseWalletManager wallet = WalletsMaster.getInstance().getWalletByIso(app, iso);
        currencyFormat.setGroupingUsed(true);
        currencyFormat.setRoundingMode(Constants.ROUNDING_MODE);

        if (wallet != null) {
            amount = wallet.getCryptoForSmallestCrypto(app, amount);
            decimalFormatSymbols.setCurrencySymbol("");
            currencyFormat.setDecimalFormatSymbols(decimalFormatSymbols);
            currencyFormat.setMaximumFractionDigits(maxDecimalPlacesForCrypto == -1 ? wallet.getMaxDecimalPlaces(app, iso) : maxDecimalPlacesForCrypto);
            currencyFormat.setMinimumFractionDigits(0);
            return currencyFormat.format(amount);
        } else {
            try {
                Currency currency = Currency.getInstance(iso);
                String symbol = currency.getSymbol();
                decimalFormatSymbols.setCurrencySymbol("");
                currencyFormat.setDecimalFormatSymbols(decimalFormatSymbols);
                currencyFormat.setNegativePrefix("-" + "");
                currencyFormat.setMaximumFractionDigits(currency.getDefaultFractionDigits());
                currencyFormat.setMinimumFractionDigits(currency.getDefaultFractionDigits());
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            return currencyFormat.format(amount);
        }
    }

//    public static String getSymbolByIso(Context app, String iso) {
//        String symbol;
//        BaseWalletManager wallet = WalletsMaster.getInstance(app).getWalletByIso(app, iso);
//        if (wallet != null) {
//            symbol = wallet.getSymbol(app);
//        } else {
//            Currency currency;
//            try {
//                currency = Currency.getInstance(iso);
//            } catch (IllegalArgumentException e) {
//                currency = Currency.getInstance(Locale.getDefault());
//            }
//            symbol = currency.getSymbol();
//        }
//        return Util.isNullOrEmpty(symbol) ? iso : symbol;
//    }
//
//    public static int getMaxDecimalPlaces(Context app, String iso) {
//        BaseWalletManager wallet = WalletsMaster.getInstance(app).getWalletByIso(app, iso);
//        if (wallet == null) {
//            Currency currency = Currency.getInstance(iso);
//            return currency.getDefaultFractionDigits();
//        } else {
//            return wallet.getMaxDecimalPlaces(app);
//        }
//
//    }

}
