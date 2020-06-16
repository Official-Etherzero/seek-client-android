package com.easyetz.base;

import com.easyetz.bean.WalletBean;
import com.easyetz.sqlite.WalletDataStore;
import com.easyetz.utils.SharedPrefsUitls;

public abstract class BasePresent<T>{
    public T view;

    public void attach(T view){
        this.view = view;
    }

    public void detach(){
        this.view = null;
    }

    public WalletBean getCurrentWallet() {
        String wid = SharedPrefsUitls.getInstance().getCurrentWallet();
        return WalletDataStore.getInstance().queryWallet(wid);
    }
}