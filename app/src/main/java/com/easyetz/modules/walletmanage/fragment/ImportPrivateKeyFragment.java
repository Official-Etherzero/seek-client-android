package com.easyetz.modules.walletmanage.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.easyetz.R;
import com.easyetz.base.BaseFragment;
import com.easyetz.base.Constants;
import com.easyetz.bean.WalletBean;
import com.easyetz.modules.main.MainActivity;
import com.easyetz.modules.walletmanage.WalletsMaster;
import com.easyetz.modules.walletmanage.createwallet.CreateWalletInteract;
import com.easyetz.modules.walletmanage.createwallet.CreateWalletPresenter;
import com.easyetz.modules.walletmanage.createwallet.CreateWalletView;
import com.easyetz.utils.ToastUtils;
import com.easyetz.utils.Util;
import com.easyetz.view.LoadingDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class ImportPrivateKeyFragment extends BaseFragment<CreateWalletView, CreateWalletPresenter> implements CreateWalletView {

    @BindView(R.id.et_private_key)
    EditText etPrivateKey;
    @BindView(R.id.key_wallet_pwd)
    EditText etWalletPwd;
    @BindView(R.id.key_wallet_pwd_again)
    EditText etWalletPwdAgain;
    Unbinder unbinder;
    @BindView(R.id.keybtn_load_wallet)
    TextView btnLoadWallet;
    @BindView(R.id.key_wallet_name)
    EditText rwWalletName;
    private String walletType;
    private CreateWalletInteract createWalletInteract;
    private LoadingDialog loadingDialog;

    @Override
    public CreateWalletPresenter initPresenter() {
        return new CreateWalletPresenter();
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        walletType = getActivity().getIntent().getStringExtra(Constants.WALLET_TYPE);
        createWalletInteract = new CreateWalletInteract();
    }

    @Override
    protected void initData() {

    }

    @Override
    public void initEvent() {
        etPrivateKey.addTextChangedListener(textWatcher);
        etWalletPwd.addTextChangedListener(textWatcher);
        etWalletPwdAgain.addTextChangedListener(textWatcher);
        rwWalletName.addTextChangedListener(textWatcher);
    }

    @Override
    protected int getContentViewLayoutID() {
        return R.layout.fragment_load_wallet_by_private_key;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: inflate a fragment view
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @SuppressLint("CheckResult")
    @OnClick({R.id.keybtn_load_wallet})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.keybtn_load_wallet:
                String key = etPrivateKey.getText().toString().trim();
                String walletName = rwWalletName.getText().toString().trim();
                String walletPwd = etWalletPwd.getText().toString().trim();
                String confirmPwd = etWalletPwdAgain.getText().toString().trim();
                boolean verifyWalletInfo = presenter.verifyInfo(getActivity(),walletName, walletPwd, confirmPwd);
                boolean verifyAddress;
//                if (walletType.equalsIgnoreCase("BTC")) {
//                    verifyAddress = presenter.verifyKeyToWallet(key);
//                } else {
                verifyAddress = presenter.verifyPrivateKeyToAddress(getActivity(),key, walletType);
//                }

                if (verifyWalletInfo && !verifyAddress) {
                    loadingDialog = new LoadingDialog(getActivity());
                    loadingDialog.show();
                    createWalletInteract.loadWalletByPrivateKey(key, walletPwd, walletName, walletType).subscribe(this::jumpToWalletBackUp, this::showError);
                }
                break;
        }
    }

    public void showError(Throwable errorInfo) {
        loadingDialog.dismiss();
        ToastUtils.showLongToast(getActivity(),errorInfo.toString());
    }

    public void jumpToWalletBackUp(WalletBean wallet) {
        loadingDialog.dismiss();
        ToastUtils.showLongToast(getActivity(),getResources().getString(R.string.load_wallet_ok));
        WalletsMaster.getInstance().getWalletByIso(getActivity(), wallet.getId().substring(0, 4)).setmAddress(wallet.getAddress());
        if (getActivity().getIntent().getIntExtra("from", 0) != 0) {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
        }
        getActivity().finish();
    }

    @Override
    public void getWalletSuccess(String address) {

    }

    @Override
    public void getWalletFail(String msg) {

    }
      TextWatcher textWatcher =new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!Util.isNullOrEmpty(etPrivateKey.getText().toString())
                    &&!Util.isNullOrEmpty(etWalletPwd.getText().toString())
                    &&!Util.isNullOrEmpty(etWalletPwdAgain.getText().toString())
                    &&!Util.isNullOrEmpty(rwWalletName.getText().toString())){
                btnLoadWallet.setEnabled(true);
            }else {
                btnLoadWallet.setEnabled(false);
            }

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };
}
