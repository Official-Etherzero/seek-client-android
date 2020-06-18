package com.easyetz.modules.walletsetting.BackupMnemonic;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.easyetz.R;
import com.easyetz.adapter.VerifyBackupMnemonicWordsAdapter;
import com.easyetz.adapter.VerifyBackupSelectedMnemonicWordsAdapter;
import com.easyetz.base.BaseActivity;
import com.easyetz.modules.normalvp.NormalPresenter;
import com.easyetz.modules.normalvp.NormalView;
import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class VerifyMnemonicBackupActivity extends BaseActivity<NormalView, NormalPresenter>implements NormalView {
//public class VerifyMnemonicBackupActivity extends BaseActivity<NormalView, NormalPresenter>implements NormalView{
    private static final int VERIFY_SUCCESS_RESULT = 2202;
    @BindView(R.id.rv_selected)
    RecyclerView rvSelected;
    @BindView(R.id.rv_mnemonic)
    RecyclerView rvMnemonic;
    @BindView(R.id.rv_errer)
    TextView rvErrer;
    private String walletMnemonic;

    private List<String> mnemonicWords;

    private List<String> selectedMnemonicWords;

    private VerifyBackupMnemonicWordsAdapter verifyBackupMenmonicWordsAdapter;
    private VerifyBackupSelectedMnemonicWordsAdapter verifyBackupSelectedMnemonicWordsAdapter;
    private long walletId;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_verify_mnemonic_backup;
    }

    @Override
    public NormalPresenter initPresenter() {
        return new NormalPresenter();
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        setCenterTitle(getString(R.string.verify_backup_title));
        walletId = getIntent().getLongExtra("walletId", -1);
        walletMnemonic = getIntent().getStringExtra("walletMnemonic");
        String[] words = walletMnemonic.split("\\s+");
        mnemonicWords = new ArrayList<>();
        for (int i = 0; i < words.length; i++) {
            mnemonicWords.add(words[i]);
        }
        // 乱序
        Collections.shuffle(mnemonicWords);

        // 未选中单词
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(this);
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        layoutManager.setAlignItems(AlignItems.STRETCH);
        rvMnemonic.setLayoutManager(layoutManager);
        verifyBackupMenmonicWordsAdapter = new VerifyBackupMnemonicWordsAdapter(R.layout.list_item_mnemoic, mnemonicWords);
        rvMnemonic.setAdapter(verifyBackupMenmonicWordsAdapter);


        // 已选中单词
        FlexboxLayoutManager layoutManager2 = new FlexboxLayoutManager(this);
        layoutManager2.setFlexWrap(FlexWrap.WRAP);
        layoutManager2.setAlignItems(AlignItems.STRETCH);
        rvSelected.setLayoutManager(layoutManager2);
        selectedMnemonicWords = new ArrayList<>();
        verifyBackupSelectedMnemonicWordsAdapter = new VerifyBackupSelectedMnemonicWordsAdapter(R.layout.list_item_mnemoic_selected, selectedMnemonicWords);
        rvSelected.setAdapter(verifyBackupSelectedMnemonicWordsAdapter);
    }

    @Override
    protected void initData() {

    }

    @Override
    public void initEvent() {
        verifyBackupMenmonicWordsAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                String mnemonicWord = verifyBackupMenmonicWordsAdapter.getData().get(position);
                    verifyBackupSelectedMnemonicWordsAdapter.addData(mnemonicWord);
                    verifyBackupMenmonicWordsAdapter.remove(position);
            }
        });
        verifyBackupSelectedMnemonicWordsAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                verifyBackupMenmonicWordsAdapter.addData(verifyBackupSelectedMnemonicWordsAdapter.getData().get(position));
                verifyBackupSelectedMnemonicWordsAdapter.remove(position);
                if (verifyBackupSelectedMnemonicWordsAdapter.getData().size()<12){
                    rvErrer.setVisibility(View.GONE);
                }
            }

        });
    }

    @OnClick(R.id.btn_confirm)
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_confirm:
//                LogUtils.d("VerifyMnemonicBackUp", "Click!!");
                List<String> data = verifyBackupSelectedMnemonicWordsAdapter.getData();
                int size = data.size();
                if (size == 12) {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int i = 0; i < size; i++) {
                        stringBuilder.append(data.get(i));
                        if (i != size - 1) {
                            stringBuilder.append(" ");
                        }
                    }
                    String verifyMnemonic = stringBuilder.toString();
                    String trim = verifyMnemonic.trim();
                    if (TextUtils.equals(trim, walletMnemonic)) {
                        // TODO 修改该钱包备份标识
                        setResult(VERIFY_SUCCESS_RESULT, new Intent());
                        finish();
                    } else {
//                        ToastUtils.showLongToast(R.string.verify_backup_failed);
                        rvErrer.setVisibility(View.VISIBLE);
                    }
                }
                break;
        }
    }
}
