package com.easyetz.modules.walletsetting.exportKeystore;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.baidu.mobstat.IIgnoreAutoTrace;
import com.easyetz.R;
import com.easyetz.base.BaseActivity;
import com.easyetz.modules.normalvp.NormalPresenter;
import com.easyetz.modules.normalvp.NormalView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ExportKeystoreActivity extends BaseActivity<NormalView, NormalPresenter> implements NormalView,IIgnoreAutoTrace {
//public class ExportKeystoreActivity extends BaseActivity<NormalView, NormalPresenter> implements NormalView {
    @BindView(R.id.tv_keystore)
    TextView tvKeystore;
    @BindView(R.id.btn_copy)
    TextView btnCopy;
    @BindView(R.id.tv_title)
    TextView tvTitle;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_export_keystore;
    }

    @Override
    public NormalPresenter initPresenter() {
        return new NormalPresenter();
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        String walletKeystore = getIntent().getStringExtra("walletKeystore");
        tvKeystore.setText(walletKeystore);
        tvTitle.setText(R.string.derive_keystore_title);

    }

    @Override
    protected void initData() {

    }

    @Override
    public void initEvent() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }

    @OnClick({R.id.iv_back, R.id.btn_copy})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                onBackPressed();
                break;
            case R.id.btn_copy:
                ClipboardManager cm = (ClipboardManager)getApplication().getSystemService(Context.CLIPBOARD_SERVICE);
                // 将文本内容放到系统剪贴板里。
                if (cm != null) {
                    // 创建普通字符型ClipData
                    ClipData mClipData = ClipData.newPlainText("Label", tvKeystore.getText().toString());
                    // 将ClipData内容放到系统剪贴板里。
                    cm.setPrimaryClip(mClipData);
                }
                btnCopy.setText(R.string.derive_private_key_already_copy_btn);
                break;
        }
    }
}
