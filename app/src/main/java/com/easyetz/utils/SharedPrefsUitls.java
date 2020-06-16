package com.easyetz.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import com.easyetz.app.MyApp;
import com.easyetz.base.Constants;
import com.easyetz.bean.NodeEntity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Currency;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class SharedPrefsUitls {

    private static final String PREFS_NAME = "MyPrefsFile";
    public static final String FIRST_WALLET_TAG = "CurrentWallet";
    public static final String CURRENT_WALLET_ADDRESS = "mAddress";
    private static volatile SharedPrefsUitls instance;
    private final SharedPreferences mSharedPreferences;

    public SharedPrefsUitls(Context context) {
        mSharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static SharedPrefsUitls getInstance() {
        if (instance == null) {
            synchronized (SharedPrefsUitls.class) {
                if (instance == null) {
                    instance = new SharedPrefsUitls(MyApp.getmInstance());
                }
            }
        }
        return instance;
    }

    public void putWalletId(String iso, int id) {
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putInt(iso, id);
        edit.apply();
    }

    public int getWalletId(String iso) {
        return mSharedPreferences.getInt(iso, 0);
    }

    public void putCurrentWalletAddress(String address) {
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putString(CURRENT_WALLET_ADDRESS, address);
        edit.apply();
    }

    public String getCurrentWalletAddress() {
        return mSharedPreferences.getString(CURRENT_WALLET_ADDRESS, "");
    }

    public void putCurrentWallet(String wid) {
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putString(FIRST_WALLET_TAG, wid);
        edit.apply();
    }

    public String getCurrentWallet() {
        return mSharedPreferences.getString(FIRST_WALLET_TAG, "");
    }

    public void putWalletTokenList(String wid, List<String> set) {
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        Gson gson = new Gson();
        String jsonSting = gson.toJson(set);
        edit.putString(wid, jsonSting);
        edit.apply();
    }

    public List<String> getWalletTokenList(String wid) {

        Gson gson = new Gson();
        String nList = mSharedPreferences.getString(wid, "");
        List<String> list = gson.fromJson(nList, new TypeToken<List<String>>() {
        }.getType());
        return list;
    }

    public String getPreferredFiatIso() {
        String defIso;
//        try {
//            defIso = Currency.getInstance(Locale.getDefault()).getCurrencyCode();
//        } catch (IllegalArgumentException e) {
//            e.printStackTrace();
        String lang = Locale.getDefault().getLanguage();
        if (lang.equalsIgnoreCase("ko")) {
            defIso = "KRW";
        } else if (lang.equalsIgnoreCase("zh")) {
            defIso = "CNY";
        } else
            defIso = "USD";
//        }
        MyLog.i("getPreferredFiatIso==" + defIso);
        return mSharedPreferences.getString("currentCurrency", defIso);
    }

    public void putPreferredFiatIso(Context context, String iso) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("currentCurrency", iso.equalsIgnoreCase(Locale.getDefault().getISO3Language()) ? null : iso);
        editor.apply();

    }

    public String getCurrentETZNode() {
        return mSharedPreferences.getString("etzNode", "");
    }

    public void putCurrentETZNode(String node) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("etzNode", node);
        editor.apply();
    }

    public List<NodeEntity> getETZNodeList() {
        Gson gson = new Gson();
        String nList = mSharedPreferences.getString("NodeList", "");
        List<NodeEntity> list = gson.fromJson(nList, new TypeToken<List<NodeEntity>>() {
        }.getType());
        return list;

    }

    public void putETZNodeList(List<NodeEntity> nList) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        Gson gson = new Gson();
        String jsonSting = gson.toJson(nList);
        editor.putString("NodeList", jsonSting);
        editor.apply();
    }

    //if the user prefers all in crypto units, not fiat currencies
    public boolean isCryptoPreferred() {
        return mSharedPreferences.getBoolean("priceInCrypto", true);
    }

    //if the user prefers all in crypto units, not fiat currencies
    public void setIsCryptoPreferred(boolean b) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean("priceInCrypto", b);
        editor.apply();
    }

    public String getTokenLogBlock(String cAddress) {
        return mSharedPreferences.getString(cAddress, "");

    }

    public void putTokenLogBlock(String cAddress, String block) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(cAddress, block);
        editor.apply();
    }

//    public List<NodeEntity> getNodeList() {
//        Gson gson = new Gson();
//        String nList = mSharedPreferences.getString("NodeList", "");
//
//        List<NodeEntity> list = gson.fromJson(nList, new TypeToken<List<NodeEntity>>() {
//        }.getType());
//        return list;
//
//    }
//
//    public void putNodeList(List<NodeEntity> nList) {
//        SharedPreferences.Editor editor = mSharedPreferences.edit();
//        Gson gson = new Gson();
//        String jsonSting = gson.toJson(nList);
//        editor.putString("NodeList", jsonSting);
//        editor.apply();
//    }

    public String getCurrentNode() {
        return mSharedPreferences.getString("etzNode", "");
    }

    public void putCurrentNode(String node) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("etzNode", node);
        editor.apply();
    }

    // BTC, mBTC, Bits
    //ignore iso, using same denomination for both for now
    public int getCryptoDenomination(String iso) {
        return mSharedPreferences.getInt("currencyUnit", Constants.CURRENT_UNIT_BITCOINS);

    }

    // BTC, mBTC, Bits
    //ignore iso, using same denomination for both for now
    public void putCryptoDenomination(String iso, int unit) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt("currencyUnit", unit);
        editor.apply();
    }

    public String getlastDappHash() {
        return mSharedPreferences.getString("dappHash", "");
    }

    public void putlastDappHash(String hash) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString("dappHash", hash);
        editor.apply();
    }

    public void storeEncryptedData(byte[] data, String name) {
        String base64 = Base64.encodeToString(data, Base64.DEFAULT);
        MyLog.i("base64=" + base64);
        SharedPreferences.Editor edit = mSharedPreferences.edit();
        edit.putString(name, base64);
        edit.apply();

    }

//    public static void destroyEncryptedData(Context ctx, String name) {
//        SharedPreferences pref = ctx.getSharedPreferences(KEY_STORE_PREFS_NAME, Context.MODE_PRIVATE);
//        SharedPreferences.Editor edit = pref.edit();
//        edit.remove(name);
//        edit.apply();
//
//    }

    public byte[] retrieveEncryptedData(String name) {
        String base64 = mSharedPreferences.getString(name, null);
        if (base64 == null) return null;
        return Base64.decode(base64, Base64.DEFAULT);
    }

    public void deleteItem(String name) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.remove(name);
        editor.apply();
    }

    public String getMinerInfo() {
        return mSharedPreferences.getString("MinerInfo", "");
    }

    public void setMinerInfo(String miner) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        MyLog.i("getMinerInfo===" + miner);
        editor.putString("MinerInfo", miner);
        editor.apply();
    }
}
