package com.easyetz.modules.walletmanage.createwallet;

import com.easyetz.base.BaseView;

public interface CreateWalletView extends BaseView {


    void getWalletSuccess(String address);
    void getWalletFail(String msg);


}
