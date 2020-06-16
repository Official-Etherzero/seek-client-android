package com.easyetz.adapter;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.easyetz.R;
import com.easyetz.adapter.baseAdapter.base.MyBaseViewHolder;
import com.easyetz.bean.Token;
import com.easyetz.bean.WalletBean;
import com.easyetz.modules.walletsetting.WalletSetting;
import com.easyetz.utils.CurrencyUtils;
import com.easyetz.utils.ExchangeRateUtil;
import com.easyetz.utils.MyLog;
import com.easyetz.utils.SharedPrefsUitls;
import com.easyetz.utils.Util;

import java.math.BigDecimal;
import java.util.List;

public class WalletsAdapter extends BaseQuickAdapter<WalletBean, MyBaseViewHolder> {

    public WalletsAdapter(int layoutResId, @Nullable List<WalletBean> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(MyBaseViewHolder helper, WalletBean item) {

        helper.setText(R.id.wallets_name, item.getName());
        String currentWalletID = SharedPrefsUitls.getInstance().getCurrentWallet();
        if (item.getId().equals(currentWalletID)) {
            helper.setImageResource(R.id.wallwts_icon, R.mipmap.current_wallet_icon);
            helper.setTextColor(R.id.wallets_name,mContext.getResources().getColor(R.color.zt_black));
        } else {
            helper.setImageResource(R.id.wallwts_icon, R.mipmap.head_wallet_icon);
            helper.setTextColor(R.id.wallets_name,mContext.getResources().getColor(R.color.zt_hui50));
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
