package com.easyetz.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.easyetz.R;
import com.easyetz.adapter.baseAdapter.base.MyBaseViewHolder;
import com.easyetz.bean.TokenInfo;
import com.easyetz.utils.SharedPrefsUitls;
import com.easyetz.utils.Util;

import java.util.List;
import java.util.Set;

public class TokenManageAdapter extends BaseQuickAdapter<TokenInfo, MyBaseViewHolder> {

    private List<String> tokenList = null;
    Context context;
    RequestOptions options;
    String wid;

    public TokenManageAdapter(Context context, int layoutResId, @Nullable List<TokenInfo> data, List<String> tokenList) {
        super(layoutResId, data);
        this.tokenList = tokenList;
        this.context = context;

        options = new RequestOptions()
                .placeholder(R.mipmap.etz_logo_white)    //加载成功之前占位图
                .error(R.mipmap.error_img);
    }

    @Override
    protected void convert(MyBaseViewHolder helper, TokenInfo item) {
        helper.setText(R.id.token_symbol, item.symbol);
        helper.setText(R.id.token_name, item.name);
        Switch swithc = helper.getView(R.id.token_manager_switch);
        if (tokenList.contains(item.symbol)) {
            swithc.setChecked(true);
        }else {
            swithc.setChecked(false);
        }
        swithc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (!tokenList.contains(item.symbol))
                        tokenList.add(item.symbol);
                } else {
                    if (tokenList.contains(item.symbol)) tokenList.remove(item.symbol);
                }
            }
        });
        if (Util.isNullOrEmpty(item.image)){
            int iconResourceId = mContext.getResources().getIdentifier(item.symbol.toLowerCase(), "mipmap", mContext.getPackageName());
            helper.setImageResource(R.id.token_icon,iconResourceId);
        }else {
            Glide.with(context)
                    .load(item.image)
                    .apply(options)
                    .into((ImageView) helper.getView(R.id.token_icon));
        }

    }

    public void setTokens(List<TokenInfo> tokens) {
        wid=SharedPrefsUitls.getInstance().getCurrentWallet();
        setNewData(tokens);
    }

    public void saveTokenChange(String wid) {
        SharedPrefsUitls.getInstance().putWalletTokenList(wid, tokenList);
    }
}
