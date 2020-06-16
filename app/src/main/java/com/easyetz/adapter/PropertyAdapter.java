package com.easyetz.adapter;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.easyetz.R;
import com.easyetz.adapter.baseAdapter.base.MyBaseViewHolder;
import com.easyetz.app.MyApp;
import com.easyetz.bean.Token;
import com.easyetz.modules.walletmanage.WalletsMaster;
import com.easyetz.utils.CurrencyUtils;
import com.easyetz.utils.ExchangeRateUtil;
import com.easyetz.utils.MyLog;
import com.easyetz.utils.SharedPrefsUitls;
import com.easyetz.utils.Util;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

public class PropertyAdapter extends BaseQuickAdapter<Token, MyBaseViewHolder> {

    public PropertyAdapter(int layoutResId, @Nullable List<Token> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(MyBaseViewHolder helper, Token item) {


        String iso = item.tokenInfo.symbol;
        if (Util.isNullOrEmpty(iso)) return;
        MyLog.i("PropertyAdapter==" + SharedPrefsUitls.getInstance().getPreferredFiatIso());
        MyLog.i("getPreferredFiatIso==" + Locale.getDefault().getLanguage());
        String fiatBalance = CurrencyUtils.getFormattedAmount(mContext, SharedPrefsUitls.getInstance().getPreferredFiatIso(),
                WalletsMaster.getInstance().getWalletByIso(MyApp.getBreadContext(), iso).getFiatBalance(iso, item.balance));
        String cryptoBalance = CurrencyUtils.getFormattedAmount(mContext, iso, new BigDecimal(Util.isNullOrEmpty(item.balance) ? "0" : item.balance));
        helper.setText(R.id.wallet_trade_price, item.tokenInfo.name);
        helper.setText(R.id.wallet_name, iso);
        helper.setText(R.id.wallet_balance_fiat, fiatBalance);
        helper.setText(R.id.wallet_balance_currency, cryptoBalance);
        ImageView im = helper.getView(R.id.wallet_icon);
        im.setBackgroundResource(R.mipmap.seek);
    }

    public void setTokens(List<Token> tokens) {
        setNewData(tokens);
    }
}
