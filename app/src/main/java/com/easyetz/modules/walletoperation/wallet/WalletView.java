package com.easyetz.modules.walletoperation.wallet;

import com.easyetz.base.BaseView;
import com.easyetz.bean.TransactionRecords;

import java.util.List;

public interface WalletView extends BaseView {

    void walletTransactions(List<TransactionRecords> list);
    void showError(String err);

}
