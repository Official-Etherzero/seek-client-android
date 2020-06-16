package com.easyetz.adapter;

import android.support.annotation.Nullable;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.easyetz.R;

import java.util.Collections;
import java.util.List;


public class VerifyBackupMnemonicWordsAdapter extends BaseQuickAdapter<String, BaseViewHolder> {

    public VerifyBackupMnemonicWordsAdapter(int layoutResId, @Nullable List<String> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, String str) {

            helper.setBackgroundRes(R.id.lly_tag, R.drawable.bg_lu_huang);
            helper.setTextColor(R.id.tv_mnemonic_word, mContext.getResources().getColor(R.color.zt_lu));
            helper.setText(R.id.tv_mnemonic_word, str);
    }

}
