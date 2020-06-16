package com.easyetz.adapter.baseAdapter;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.easyetz.R;
import com.easyetz.adapter.baseAdapter.base.MyBaseViewHolder;
import com.easyetz.bean.WalletBean;
import com.easyetz.modules.walletsetting.WalletSetting;
import com.easyetz.sqlite.BalanceDataSource;
import com.easyetz.utils.CurrencyUtils;
import com.easyetz.utils.SharedPrefsUitls;
import com.easyetz.utils.Util;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class WalletManageAdapter extends BaseQuickAdapter<WalletBean, MyBaseViewHolder> {

    public WalletManageAdapter(int layoutResId, @Nullable List<WalletBean> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(MyBaseViewHolder helper, WalletBean item) {
        Map<String, String> balances = BalanceDataSource.getInstance().getWalletTokensBalance(item.getId());
        helper.setText(R.id.wallet_item_name, item.getName());
        helper.setText(R.id.wallet_item_address, item.getAddress());

        if (balances.containsKey("SEEK")) {
            String cryptoBalance = CurrencyUtils.getFormattedAmount(mContext, "SEEK", new BigDecimal(Util.isNullOrEmpty(balances.get("SEEK")) ? "0" : balances.get("SEEK")));
            helper.setText(R.id.wallet_item_balance, cryptoBalance);
        }
        else {
            helper.setText(R.id.wallet_item_balance, "0 SEEK");
        }
        helper.getView(R.id.wallet_manager_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, WalletSetting.class);
                intent.putExtra("wallet", item);
                mContext.startActivity(intent);
            }
        });


    }

    public void setTokens(List<WalletBean> tokens) {
        setNewData(tokens);
    }
}
