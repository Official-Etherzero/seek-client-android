package com.easyetz.bean;

import com.easyetz.utils.Util;

import java.math.BigDecimal;


public class CryptoRequest {
    public static final String TAG = CryptoRequest.class.getName();
    public String iso = "BTC"; //make it default
    public String address;
    public String scheme;
    public String r;
    public BigDecimal amount;
    public String data;
    public String gasL;
    public String gasP;
    public String label;
    public String message;
    public String req;
    public BigDecimal value; // ETH payment request amounts are called `value`

    public String cn;
    public boolean isAmountRequested;

    public CryptoRequest(String certificationName, boolean isAmountRequested, String message, String address, BigDecimal amount, String data, String gasL, String gasP) {
        this.isAmountRequested = isAmountRequested;
        this.cn = certificationName;
        this.address = address;
        this.amount = amount;
        this.value = amount;
        this.message = message;
        this.gasL = gasL;
        this.gasP = gasP;
        this.data = data;
    }

    public CryptoRequest() {

    }
//
public boolean isPaymentProtocol() {
    return !Util.isNullOrEmpty(r);
}
    public boolean hasAddress() {
        return !Util.isNullOrEmpty(address);
    }

}
