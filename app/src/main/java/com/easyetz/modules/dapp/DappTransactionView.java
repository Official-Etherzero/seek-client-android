package com.easyetz.modules.dapp;

import com.easyetz.base.BaseView;

public interface DappTransactionView extends BaseView{

    void onGasEstimate(String gas);
    void onGasPrice(String price);
    void onError(String err);
}
