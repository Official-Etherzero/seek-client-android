package com.easyetz.adapter;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.Nullable;
import android.text.format.DateUtils;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.easyetz.R;
import com.easyetz.adapter.baseAdapter.base.MyBaseViewHolder;
import com.easyetz.app.MyApp;
import com.easyetz.base.Constants;
import com.easyetz.bean.Token;
import com.easyetz.bean.TokenInfo;
import com.easyetz.bean.TransactionRecords;
import com.easyetz.blockchain.BaseWalletManager;
import com.easyetz.modules.walletmanage.WalletsMaster;
import com.easyetz.utils.CurrencyUtils;
import com.easyetz.utils.DateUtil;
import com.easyetz.utils.ExchangeRateUtil;
import com.easyetz.utils.SharedPrefsUitls;
import com.easyetz.utils.Util;

import java.math.BigDecimal;
import java.util.List;

public class TransactionsAdapter extends BaseQuickAdapter<TransactionRecords, MyBaseViewHolder> {

    private TokenInfo tokenInfo;

    public TransactionsAdapter(int layoutResId, @Nullable List<TransactionRecords> data, TokenInfo tokenInfo) {
        super(layoutResId, data);
        this.tokenInfo = tokenInfo;
    }

    @Override
    protected void convert(MyBaseViewHolder helper, TransactionRecords item) {
        String commentString = "";
        boolean received = item.isReceived;
        if (Util.isNullOrEmpty(item.blockNumber)) {
            helper.setBackgroundRes(R.id.tx_status_icon, R.mipmap.asset_log_verify);
            helper.setText(R.id.transaction_status, mContext.getString(R.string.TransactionDetails_confirming));
        } else if (item.status.equals("success")) {

            if (received) {
                helper.setBackgroundRes(R.id.tx_status_icon, R.mipmap.asset_log_zhuanru);
            } else {
                helper.setBackgroundRes(R.id.tx_status_icon, R.mipmap.asset_log_zhuanchu);
            }
            helper.setText(R.id.transaction_status, mContext.getString(R.string.transfer_ok));

        } else {
            helper.setBackgroundRes(R.id.tx_status_icon, R.mipmap.asset_log_wrong);
            helper.setText(R.id.transaction_status, mContext.getString(R.string.transfer_fail));
        }

        BigDecimal cryptoAmount = new BigDecimal(item.value).abs();
        String formattedAmount = CurrencyUtils.getFormattedAmount(mContext, tokenInfo.symbol, cryptoAmount, Constants.MAX_DECIMAL_PLACES_FOR_UI);
        helper.setText(R.id.tx_amount, formattedAmount);
        helper.setText(R.id.tx_description, !commentString.isEmpty() ? commentString : (!received ? item.to : item.from));
        //if it's 0 we use the current time.
        long timeStamp = Long.valueOf(item.date) == 0 ? System.currentTimeMillis() : Long.valueOf(item.date) * DateUtils.SECOND_IN_MILLIS;

        String shortDate = DateUtil.getShortDate(timeStamp);

        helper.setText(R.id.tx_date, shortDate);


    }

    public void setTokens(List<TransactionRecords> tokens) {
        setNewData(tokens);
    }
}
