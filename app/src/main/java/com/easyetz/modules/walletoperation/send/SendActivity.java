package com.easyetz.modules.walletoperation.send;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;

import com.easyetz.R;
import com.easyetz.base.BaseActivity;
import com.easyetz.base.Constants;
import com.easyetz.bean.BalanceEntity;
import com.easyetz.bean.TokenInfo;
import com.easyetz.bean.WalletBean;
import com.easyetz.blockchain.EtzWalletManager;
import com.easyetz.modules.walletoperation.CreateTransactionInteract;
import com.easyetz.modules.walletoperation.EthereumNetworkRepository;
import com.easyetz.sqlite.BalanceDataSource;
import com.easyetz.utils.ETZAnimator;
import com.easyetz.utils.KeyBoardUtil;
import com.easyetz.utils.MyLog;
import com.easyetz.utils.SharedPrefsUitls;
import com.easyetz.utils.ToastUtils;
import com.easyetz.utils.Util;
import com.easyetz.view.BRDialogView;
import com.easyetz.view.ETZDialog;
import com.easyetz.view.LoadingDialog;

import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.schedulers.Schedulers;

public class SendActivity extends BaseActivity<SendView, SendPresenter> implements SendView {
//public class SendActivity extends BaseActivity<SendView, SendPresenter> implements SendView {

    @BindView(R.id.address_edit)
    EditText addressEdit;
    @BindView(R.id.amount_edit)
    EditText amountEdit;
    @BindView(R.id.gas_price)
    EditText gasPrice;
    @BindView(R.id.gas_limit)
    EditText gasLimit;
    @BindView(R.id.comment_data)
    EditText commentData;
    @BindView(R.id.comment_edit)
    EditText commentEdit;

    private TokenInfo token;
    private WalletBean currentWallet;
    private boolean isSendingTokens;
    private CreateTransactionInteract createTransaction;
    SendVerifyPopuWindow svpw;

    private String toAddress;
    private String iso;
    //    private String limit;
//    private String price;
//    private String data;
//    private String money;
    private String contract = "";
    private LoadingDialog loadingDialog;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_transfer_accounts;
    }

    @Override
    public SendPresenter initPresenter() {
        return new SendPresenter();
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        setCenterTitle(getString(R.string.Send_title));
        Intent ent = getIntent();
        iso = ent.getStringExtra("iso");
        toAddress = ent.getStringExtra("toAddress");
//        money=ent.getStringExtra("money");
//        data=ent.getStringExtra("data");
//        limit=ent.getStringExtra("limit");
//        price=ent.getStringExtra("price");
//        isSendingTokens= !iso.equals("SEEK");
        if (isSendingTokens) {
            //去获取合约地址
        }
        presenter.getGasPrice(activity);
        currentWallet = presenter.getCurrentWallet();
        createTransaction = new CreateTransactionInteract(new EthereumNetworkRepository());
        addressEdit.setText(Util.isNullOrEmpty(toAddress) ? "" : toAddress);
//        amountEdit.setText(Util.isNullOrEmpty(money)?"":money);
//        gasLimit.setText(Constants.DEFAULT_GAS_LIMIT_FOR_ETH);
//        gasPrice.setText(Constants.DEFAULT_GAS_PRICE_GWEI);
        if (!Util.isNullOrEmpty(toAddress) && Util.isAddressValid(toAddress)) {
            if (isSendingTokens) {
//                        String data = createTransaction.createTokenTransferData(s.toString(), Convert.toWei("1", Convert.Unit.ETHER).toBigInteger());
//                        presenter.getGasEstimate(activity, contract, currentWallet.address,  data);
            } else {
                presenter.getGasEstimate(activity, toAddress, currentWallet.address, "0x");
            }
        }

    }

    @Override
    protected void initData() {

    }

    @Override
    public void initEvent() {
        addressEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {


                if (s != null && Util.isAddressValid(s.toString())) {

                    if (isSendingTokens) {
//                        String data = createTransaction.createTokenTransferData(s.toString(), Convert.toWei("1", Convert.Unit.ETHER).toBigInteger());
//                        presenter.getGasEstimate(activity, contract, currentWallet.address,  data);
                    } else {
                        presenter.getGasEstimate(activity, s.toString(), currentWallet.address, "0x");
                    }
                }

            }

            @Override
            public void afterTextChanged(Editable s) {
                MyLog.i("addTextChangedListener---" + s.toString());

            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }


    //只在etz显示data输入框
    private void visibleAdvView() {

    }

    @OnClick({R.id.scan, R.id.btn_transfer})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.scan:
                ETZAnimator.openScanner(this, Constants.SCANNER_REQUEST);
                break;
            case R.id.btn_transfer:
                String rawAddress = addressEdit.getText().toString().trim();
                String amountStr = amountEdit.getText().toString().trim();
                String gasL = gasLimit.getText().toString().trim();
                String gasP = gasPrice.getText().toString().trim();
                if (Util.isNullOrEmpty(rawAddress)) {
                    ETZDialog.showCustomDialog(this, getString(R.string.Alert_error), getString(R.string.Send_noAddress),
                            getString(R.string.AccessibilityLabels_close), null, new BRDialogView.BROnClickListener() {
                                @Override
                                public void onClick(BRDialogView brDialogView) {
                                    brDialogView.dismissWithAnimation();
                                }
                            }, null, null, 0);

                    return;
                }
                if (!Util.isAddressValid(rawAddress)) {
                    sayInvalidClipboardData();
                    return;
                }
                if (Util.isNullOrEmpty(amountStr)) {
                    ToastUtils.showLongToast(activity, R.string.send_input_transfer_sum);
                } else if (Util.isNullOrEmpty(gasP)) {
                    presenter.getGasPrice(activity);
                } else if (Util.isNullOrEmpty(gasL)) {
                    if (isSendingTokens) {
                        String data = createTransaction.createTokenTransferData(rawAddress, Convert.toWei(amountStr, Convert.Unit.ETHER).toBigInteger());
                        presenter.getGasEstimate(activity, contract, currentWallet.address, data);
                    } else {
                        presenter.getGasEstimate(activity, rawAddress, currentWallet.address, "0x");
                    }
                } else {
                    BigInteger price = new BigInteger(gasP).multiply(new BigInteger("1000000000"));
                    BigInteger limit = new BigInteger(gasL);
                    BigInteger free = limit.multiply(price);
                    BigInteger amount = Convert.toWei(amountStr, Convert.Unit.ETHER).toBigInteger();
                    BalanceEntity be = BalanceDataSource.getInstance().getTokenBalance(iso, SharedPrefsUitls.getInstance().getCurrentWallet());
                    String balance = be == null ? "0" : be.money;
                    BigInteger wbalance = new BigInteger(balance);

                    String kgf = new BigDecimal(free).divide(new BigDecimal(EtzWalletManager.ETHER_WEI)).toPlainString();
//                    MyLog.i("balance========" + balance);
                    if (wbalance.compareTo(amount.add(free)) == -1) {

                        String am = new BigDecimal(wbalance.subtract(free)).divide(new BigDecimal(EtzWalletManager.ETHER_WEI)).toPlainString();
                        String message = String.format(getString(R.string.Import_confirm), am, kgf);
                        ETZDialog.showCustomDialog(activity, getString(R.string.Send_insufficientFunds), message,
                                getString(R.string.input_pwd_dialog_confirm), getString(R.string.Button_cancel), new BRDialogView.BROnClickListener() {
                                    @Override
                                    public void onClick(BRDialogView brDialogView) {

//                                        if (isSendingTokens) {
//                                            String data = createTransaction.createTokenTransferData(rawAddress, Convert.toWei(amountStr, Convert.Unit.ETHER).toBigInteger());
//                                            show0nfirmation(rawAddress, amount, gasL, gasP, data, kgf);
//                                        } else {
                                        show0nfirmation(rawAddress, am, limit, price, "0x", kgf);
                                        ETZDialog.hideDialog();
//                                        }
                                    }
                                }, new BRDialogView.BROnClickListener() {
                                    @Override
                                    public void onClick(BRDialogView brDialogView) {
                                        ETZDialog.hideDialog();
                                    }
                                }, null, 0);

                    } else {

                        if (isSendingTokens) {
                            String data = createTransaction.createTokenTransferData(rawAddress, Convert.toWei(amountStr, Convert.Unit.ETHER).toBigInteger());
                            show0nfirmation(rawAddress, amountStr, limit, price, data, kgf);
                        } else {
                            show0nfirmation(rawAddress, amountStr, limit, price, "0x", kgf);
                        }
                    }

                }


                break;
        }
    }


    private void show0nfirmation(String to, String amount, BigInteger gasL, BigInteger gasP, String data, String kgf) {
        svpw = new SendVerifyPopuWindow(this, to, currentWallet.address, kgf, amount);
        svpw.showAtLocation(findViewById(R.id.activity_transfer), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        svpw.setOnSendPwdClickListener(new SendVerifyPopuWindow.OnSendPwdClickListener() {
            @Override
            public void setOnSendPwd(String pwd) {
                if (isSendingTokens) {
                    createTransaction.createERC20Transfer(currentWallet,
                            to,
                            token.address,
                            Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger(),
                            gasP,
                            gasL,
                            pwd,
                            data).subscribeOn(Schedulers.io())
                            .subscribe(SendActivity.this::onSuccessTransaction, SendActivity.this::onErrorTransaction);

                } else {
                    loadingDialog = new LoadingDialog(activity);
                    loadingDialog.show();
                    loadingDialog.setLoadingContent(getString(R.string.is_submitting));
                    createTransaction.createTransaction(currentWallet,
                            to,
                            Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger(),
                            gasP,
                            gasL,
                            "",
                            pwd).subscribeOn(Schedulers.io())
                            .subscribe(SendActivity.this::onSuccessTransaction, SendActivity.this::onErrorTransaction);
                }
            }
        });


    }

    private void sayInvalidClipboardData() {
        ETZDialog.showCustomDialog(this, "", getResources().getString(R.string.Send_invalidAddressTitle),
                getString(R.string.AccessibilityLabels_close), null, new BRDialogView.BROnClickListener() {
                    @Override
                    public void onClick(BRDialogView brDialogView) {
                        brDialogView.dismiss();
                    }
                }, null, null, 0);
    }


    private void onSuccessTransaction(String transaction) {
        loadingDialog.dismiss();
        ToastUtils.showLongToast(activity,R.string.transfer_ok );
        MyLog.i("onSuccessTransaction=="+transaction);
        svpw.dismiss();
        KeyBoardUtil.getInstance(activity).hide();
        finish();

    }

    private void onErrorTransaction(Throwable throwable) {
        loadingDialog.dismiss();
        ToastUtils.showLongToast(activity, "交易失败：" + throwable.getMessage());
    }

    @Override
    public void sendGasLimitSuccess(String to, String gas, String data) {
        gasLimit.setText(gas);
    }

    @Override
    public void sendViewError(String str) {
        ETZDialog.showSimpleDialog(this, getString(R.string.WipeWallet_failedTitle), str);
    }

    @Override
    public void sendGasPriceSuccess(String price) {
        String gp="";
        if (Float.valueOf(price)<=0){
            gp="1";
        }else if(Float.valueOf(price)>=100){
            gp="100";
        }else {
            gp=price;
        }
        gasPrice.setText(gp);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        MyLog.i("**************" + requestCode);
        if (data == null) return;
        switch (requestCode) {
            case Constants.SCANNER_REQUEST:
                String result = data.getStringExtra("result");
                MyLog.i("**************" + result);
                if (!Util.isNullOrEmpty(result) && Util.isAddressValid(result)) {
                    addressEdit.setText(result);
                    if (isSendingTokens) {
//                        String data = createTransaction.createTokenTransferData(s.toString(), Convert.toWei("1", Convert.Unit.ETHER).toBigInteger());
//                        presenter.getGasEstimate(activity, contract, currentWallet.address,  data);
                    } else {
                        presenter.getGasEstimate(activity, result, currentWallet.address, "0x");
                    }
                } else {
                    ToastUtils.showLongToast(activity, R.string.Send_invalidAddressTitle);
                }

//                if (CryptoUriParser.isCryptoUrl(activity, result)) {
//                    CryptoRequest cryptoRequest = CryptoUriParser.parseRequest(activity, result);
////                    intent.putExtra("iso",cryptoRequest.label);
//                    addressEdit.setText(cryptoRequest.address==null?"":cryptoRequest.address);
//                    amountEdit.setText(cryptoRequest.value==null?"":cryptoRequest.value.toPlainString());
//                }
                break;
        }
    }
}

