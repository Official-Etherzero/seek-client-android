package com.easyetz.modules.walletoperation.send;

import com.easyetz.base.BaseView;

public interface SendView extends BaseView{

    void sendGasLimitSuccess(String to,String gas,String data);
    void sendViewError(String str);
    void sendGasPriceSuccess(String price);
}
