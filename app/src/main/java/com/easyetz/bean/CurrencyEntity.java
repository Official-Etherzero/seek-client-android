package com.easyetz.bean;

import java.io.Serializable;


/**
 * 钱包价格实例
 */

public class CurrencyEntity implements Serializable {

    //Change this after modifying the class
    private static final long serialVersionUID = 7526472295622776147L;

    public static final String TAG = CurrencyEntity.class.getName();
    public String code;//this currency code (USD, RUB)
    public String name;//this currency name (Dollar)
    public float rate;
    public String iso="BTC";//this wallet's iso (BTC, BCH)

    public CurrencyEntity(String code, String name, float rate, String iso) {
        this.code = code;
        this.name = name;
        this.rate = rate;
        this.iso = iso;
    }

    public CurrencyEntity() {
    }
}
