package com.easyetz.modules.dapp;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import com.easyetz.R;
import com.easyetz.base.BaseActivity;
import com.easyetz.bean.BalanceEntity;
import com.easyetz.bean.Token;
import com.easyetz.bean.TokenInfo;
import com.easyetz.bean.WalletBean;
import com.easyetz.blockchain.EtzWalletManager;
import com.easyetz.modules.WebViewActivity;
import com.easyetz.modules.walletoperation.CreateTransactionInteract;
import com.easyetz.modules.walletoperation.EthereumNetworkRepository;
import com.easyetz.modules.walletoperation.send.InputPwdView;
import com.easyetz.modules.walletoperation.wallet.WalletActivity;
import com.easyetz.sqlite.BalanceDataSource;
import com.easyetz.utils.MyLog;
import com.easyetz.utils.SharedPrefsUitls;
import com.easyetz.utils.ToastUtils;
import com.easyetz.utils.Util;
import com.easyetz.view.BRDialogView;
import com.easyetz.view.ETZDialog;
import com.easyetz.view.LoadingDialog;
import com.easyetz.view.MEdit;
import com.easyetz.view.MText;

import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.schedulers.Schedulers;

public class DappTransaction extends BaseActivity<DappTransactionView, DappTransactionPresenter> implements DappTransactionView {

    @BindView(R.id.dapp_to)
    MText d_to;
    @BindView(R.id.dapp_value)
    MText d_value;
    @BindView(R.id.dapp_gasl)
    MEdit d_gasl;
    @BindView(R.id.dapp_gasp)
    MEdit d_gasp;
    @BindView(R.id.dapp_data)
    MText d_data;
    @BindView(R.id.dapp_send_button)
    TextView dappSendButton;
    private String to, vaule, data, gasL, gasP;
    private boolean isSend = false;
    private Dialog dialog;
    private CreateTransactionInteract interact;
    private LoadingDialog loadingDialog;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_dapp_transaction;
    }

    @Override
    public DappTransactionPresenter initPresenter() {
        return new DappTransactionPresenter();
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        to = getIntent().getStringExtra("to");
        vaule = getIntent().getStringExtra("value");
        data = getIntent().getStringExtra("data");
        gasL = getIntent().getStringExtra("gasL");
        gasP = getIntent().getStringExtra("gasP");
        interact = new CreateTransactionInteract(new EthereumNetworkRepository());
        d_to.setText(to);
        BigDecimal bv = new BigDecimal(vaule);
        String strv = bv.divide(new BigDecimal(EtzWalletManager.ETHER_WEI)).toPlainString();
        d_value.setText(strv);
        d_data.setText(data);
        //评估GasL
        presenter.getGasPrice(activity);
        presenter.getGasEstimate(activity, to, vaule, data, dappSendButton);
        d_gasl.addTextChangedListener(addET);
        d_gasp.addTextChangedListener(addET);

    }

    @Override
    protected void initData() {


    }

    @Override
    public void initEvent() {

    }

    /**
     * 输入首字不能为0和.
     */
    private TextWatcher addET = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {

        }

        @Override
        public void afterTextChanged(Editable edt) {
            String temp = edt.toString();
            int posDot = temp.indexOf(".");
            int frist = temp.indexOf("0");
            if (frist == 0 || posDot == 0) {
                edt.delete(0, 1);
            }
        }
    };


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }

    @OnClick({R.id.iv_back, R.id.dapp_send_button})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.dapp_send_button:
                //not allowed now
                if (!isSend) {
                    //评估GasL
                    presenter.getGasEstimate(activity, to, vaule, data, dappSendButton);
                    return;
                }
                dappSendButton.setEnabled(false);
                String rawAddress = d_to.getText().toString().trim();
                String amount = d_value.getText().toString().trim();
                String gasL = d_gasl.getText().toString().trim();
                String gasP = d_gasp.getText().toString().trim();
                String data = d_data.getText().toString().trim();

                BigInteger price = new BigInteger(gasP).multiply(new BigInteger("1000000000"));
                BigInteger limit = new BigInteger(gasL);
                BigInteger amount_eth = Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger();
                BigInteger free = limit.multiply(price);

                BalanceEntity be = BalanceDataSource.getInstance().getTokenBalance("SEEK", SharedPrefsUitls.getInstance().getCurrentWallet());
                BigInteger wbalance = new BigInteger(be == null ? "0" : be.money);

                String kgf = new BigDecimal(free).divide(new BigDecimal(EtzWalletManager.ETHER_WEI)).toPlainString();
                String content = rawAddress + "\n\n" + getString(R.string.Confirmation_amountLabel) + amount + "(SEEK)\n" + getString(R.string.Confirmation_feeLabel) + kgf + "(SEEK)\n";
                if (wbalance.compareTo(amount_eth.add(free)) == -1) {
                    String sum_amount = new BigDecimal(amount_eth.add(free)).divide(new BigDecimal(EtzWalletManager.ETHER_WEI)).toPlainString();
                    String message = String.format(getString(R.string.Import_confirm_dapp), amount, kgf, sum_amount);
                    ETZDialog.showCustomDialog(activity, getString(R.string.Send_insufficientFunds), message,
                            getString(R.string.AccessibilityLabels_close), "", new BRDialogView.BROnClickListener() {
                                @Override
                                public void onClick(BRDialogView brDialogView) {
                                    ETZDialog.hideDialog();
                                    dappSendButton.setEnabled(true);
                                }
                            }, null, null, 0);

                    return;

                }
                InputPwdView pwdView = new InputPwdView(this, content, pwd -> {
                    loadingDialog = new LoadingDialog(activity);
                    loadingDialog.show();
                    loadingDialog.setLoadingContent(getString(R.string.is_submitting));
                    interact.createTransaction(presenter.getCurrentWallet(),
                            rawAddress,
                            amount_eth,
                            price,
                            limit,
                            data,
                            pwd).subscribeOn(Schedulers.io())
                            .subscribe(this::onSuccessTransaction, this::onErrorTransaction);
                });

                dialog = new Dialog(DappTransaction.this);
                dialog.setTitle(R.string.VerifyPin_touchIdMessage);
                dialog.setContentView(pwdView);
                dialog.setCancelable(true);
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();

                dappSendButton.setEnabled(true);
                break;
        }
    }

    @Override
    public void onGasEstimate(String gas) {
        if (Util.isNullOrEmpty(gas)) return;
        String g = new BigInteger(gas.substring(2, gas.length()), 16).toString(10);
        isSend = true;
        d_gasl.setText(g);
        MyLog.i("etzTransaction----address=" + g);
    }

    @Override
    public void onGasPrice(String price) {
        BigInteger g = new BigInteger(price.substring(2, price.length()), 16);
        String p = g.divide(new BigInteger("1000000000")).toString();
        String gp;
        if (Float.valueOf(p)<=0){
            gp="1";
        }else if(Float.valueOf(p)>=100){
            gp="100";
        }else {
            gp=p;
        }
        d_gasp.setText(gp);

    }

    @Override
    public void onError(String err) {
        MyLog.i("onError-------" + err);
//        ETZDialog.showSimpleDialog(DappTransaction.this, getString(R.string.WipeWallet_failedTitle), err);
    }

    private void onSuccessTransaction(String transaction) {
        loadingDialog.dismiss();
        ToastUtils.showLongToast(activity, R.string.transfer_ok);
        MyLog.i("onSuccessTransaction==" + transaction);
        SharedPrefsUitls.getInstance().putlastDappHash(transaction);
        dialog.dismiss();
        if (!WebViewActivity.getInstance().isFinishing()) {
            WebViewActivity.getInstance().finish();
        }
        Intent intent = new Intent(DappTransaction.this, WalletActivity.class);
        WalletBean wallet = presenter.getCurrentWallet();
        Token token = new Token(new TokenInfo(wallet.getAddress(), "SeekChain", "SEEK", "",
                wallet.getStartColor(), wallet.getEndColor(), wallet.getDecimals()), "");
        intent.putExtra("item", token);
        startActivity(intent);
        finish();

    }

    private void onErrorTransaction(Throwable throwable) {
        loadingDialog.dismiss();
        ToastUtils.showLongToast(activity, "交易失败：" + throwable.getMessage());
        MyLog.i("onError-------" + throwable.getMessage());
    }
}
