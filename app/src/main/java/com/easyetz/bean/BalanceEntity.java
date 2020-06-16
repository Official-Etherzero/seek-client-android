package com.easyetz.bean;

import java.io.Serializable;


/**
 * 钱包价格实例
 */

public class BalanceEntity implements Serializable {

    private static final long serialVersionUID = 7526472295622776147L;

    public static final String TAG = BalanceEntity.class.getName();
    public String bid;//
    public String wid;//
    public String iso;//
    public String money;


    public BalanceEntity(String bid, String wid, String iso, String money) {
        this.bid = bid;
        this.wid = wid;
        this.iso = iso;
        this.money = money;
    }

    public BalanceEntity() {
    }
}
