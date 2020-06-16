package com.easyetz.modules.walletoperation.send;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.easyetz.BuildConfig;
import com.easyetz.R;
import com.easyetz.base.BaseUrl;
import com.easyetz.base.Constants;
import com.easyetz.bean.BalanceEntity;
import com.easyetz.bean.CryptoRequest;
import com.easyetz.bean.ResponseGasBean;
import com.easyetz.bean.Token;
import com.easyetz.bean.TokenInfo;
import com.easyetz.bean.WalletBean;
import com.easyetz.blockchain.EtzWalletManager;
import com.easyetz.http.HttpRequets;
import com.easyetz.http.callback.JsonCallback;
import com.easyetz.modules.main.my.concacts.ContactsActivity;
import com.easyetz.modules.tools.threads.ETZExecutor;
import com.easyetz.modules.walletmanage.WalletsMaster;
import com.easyetz.modules.walletoperation.ConfirmationViewModel;
import com.easyetz.modules.walletoperation.CreateTransactionInteract;
import com.easyetz.modules.walletoperation.EthereumNetworkRepository;
import com.easyetz.modules.walletoperation.wallet.WalletActivity;
import com.easyetz.sqlite.BalanceDataSource;
import com.easyetz.utils.BalanceUtils;
import com.easyetz.utils.ClipboardManager;
import com.easyetz.utils.CurrencyUtils;
import com.easyetz.utils.ETZAnimator;
import com.easyetz.utils.MyLog;
import com.easyetz.utils.SharedPrefsUitls;
import com.easyetz.utils.ToastUtils;
import com.easyetz.utils.Util;
import com.easyetz.view.BRDialogView;
import com.easyetz.view.ETZDialog;
import com.easyetz.view.MEdit;
import com.easyetz.view.MText;
import com.lzy.okgo.model.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;

import io.reactivex.schedulers.Schedulers;

import static com.easyetz.utils.CryptoUriParser.parseRequest;


public class SendPopupWindow extends PopupWindow implements View.OnClickListener {
    private MEdit addressEdit;
    private MText isoText;
    private MEdit amountEdit;
    private MText balanceText;
    private MText isoButton;
    private MEdit commentEdit;
    private MEdit gasLimit;
    private MEdit gasPrice;
    private MEdit commentData;
    private LinearLayout commentDataView;
    private LinearLayout advEditText;
    private RelativeLayout advBtnView;
    private View mPopView;
    private OnItemClickListener mListener;
    private boolean isadv = false;
    private Activity ctx;
    private CreateTransactionInteract createTransaction;
    //    private ConfirmationViewModel viewModel;
    private WalletBean walletBean;
    private TokenInfo tokenInfo;
    private boolean isSendingTokens;
    private Dialog dialog;
    private String balance;

    public SendPopupWindow(Activity context, WalletBean walletBean, TokenInfo tokenInfo) {
        super(context);
        // TODO Auto-generated constructor stub
        ctx = context;
        this.walletBean = walletBean;
        this.tokenInfo = tokenInfo;
        createTransaction = new CreateTransactionInteract(new EthereumNetworkRepository());
//        viewModel = new ConfirmationViewModel(createTransaction);
        isSendingTokens = !tokenInfo.symbol.equals("SEEK");
        init(context);
        setPopupWindow();

    }

    /**
     * 初始化
     *
     * @param context
     */
    private void init(Context context) {
        // TODO Auto-generated method stub
        LayoutInflater inflater = LayoutInflater.from(context);
        //绑定布局
        mPopView = inflater.inflate(R.layout.activity_send, null);
        addressEdit = mPopView.findViewById(R.id.address_edit);
        isoText = mPopView.findViewById(R.id.iso_text);
        amountEdit = mPopView.findViewById(R.id.amount_edit);
        balanceText = mPopView.findViewById(R.id.balance_text);
        isoButton = mPopView.findViewById(R.id.iso_button);
        commentEdit = mPopView.findViewById(R.id.comment_edit);
        gasLimit = mPopView.findViewById(R.id.gas_limit);
        gasPrice = mPopView.findViewById(R.id.gas_price);
        commentData = mPopView.findViewById(R.id.comment_data);
        commentDataView = mPopView.findViewById(R.id.comment_data_view);
        advEditText = mPopView.findViewById(R.id.adv_edit_text);
        advBtnView = mPopView.findViewById(R.id.adv_btn_view);
        BalanceEntity be = BalanceDataSource.getInstance().getTokenBalance(tokenInfo.symbol, SharedPrefsUitls.getInstance().getCurrentWallet());
        balance = be == null ? "0" : (new BigDecimal(be.money).divide(new BigDecimal(EtzWalletManager.ETHER_WEI)).toPlainString());
        String balanceString = String.format(ctx.getString(R.string.Send_balance), balance + " " + tokenInfo.symbol);
        balanceText.setText(balanceString);
        mPopView.findViewById(R.id.close_button).setOnClickListener(this);
        mPopView.findViewById(R.id.paste_button).setOnClickListener(this);
        mPopView.findViewById(R.id.send_contacts).setOnClickListener(this);
        mPopView.findViewById(R.id.scan).setOnClickListener(this);
        mPopView.findViewById(R.id.advanced_btn).setOnClickListener(this);
        mPopView.findViewById(R.id.send_button).setOnClickListener(this);
        amountEdit.addTextChangedListener(addET);

    }

    /**
     * 设置窗口的相关属性
     */
    @SuppressLint("InlinedApi")
    private void setPopupWindow() {
        this.setContentView(mPopView);// 设置View
        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);// 设置弹出窗口的宽
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);// 设置弹出窗口的高
        this.setFocusable(true);// 设置弹出窗口可
//        this.setAnimationStyle(R.style.mypopwindow_anim_style);// 设置动画
        this.setBackgroundDrawable(new ColorDrawable(0x30000000));// 设置背景透明
        this.setBackgroundDrawable(new BitmapDrawable());//注意这里如果不设置，下面的setOutsideTouchable(true);允许点击外部消失会失效
        this.setOutsideTouchable(true);   //设置外部点击关闭ppw窗口
        this.setFocusable(true);
    }


    /**
     * 定义一个接口，公布出去 在Activity中操作按钮的单击事件
     */
    public interface OnItemClickListener {
        void setOnItemClick(View v);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.close_button:
                dismiss();
                break;
            case R.id.paste_button:
                String theUrl = ClipboardManager.getClipboard(ctx);
                if (Util.isNullOrEmpty(theUrl)) {
                    sayClipboardEmpty();
                    return;
                }

                final CryptoRequest obj = parseRequest(ctx, theUrl);

                if (obj == null || Util.isNullOrEmpty(obj.address)) {
                    sayInvalidClipboardData();
                    return;
                }
                if (!Util.isNullOrEmpty(obj.address)) {
                    final Activity app = (Activity) ctx;
                    if (app == null) {
                        MyLog.e("paste onClick: app is null");
                        return;
                    }
                    ETZExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
                        @Override
                        public void run() {
                            if (walletBean.getAddress().equals(obj.address)) {
                                app.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ETZDialog.showCustomDialog(ctx, "", ctx.getResources().getString(R.string.Send_containsAddress),
                                                ctx.getResources().getString(R.string.AccessibilityLabels_close), null, new BRDialogView.BROnClickListener() {
                                                    @Override
                                                    public void onClick(BRDialogView brDialogView) {
                                                        brDialogView.dismiss();
                                                    }
                                                }, null, null, 0);
                                        ClipboardManager.putClipboard(ctx, "");
                                    }
                                });

                            } else if (!Util.isAddressValid(obj.address)) {
                                app.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        sayInvalidClipboardData();
                                    }
                                });
                            } else {
                                app.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        addressEdit.setText(obj.address);

                                    }
                                });
                            }
                        }
                    });

                } else {
                    sayInvalidClipboardData();
                }
                break;
            case R.id.send_contacts:
                Intent intent = new Intent(ctx, ContactsActivity.class);
                intent.putExtra("from", 1);
                ctx.startActivityForResult(intent,Constants.CONTACTS_REQUEST);
                break;
            case R.id.scan:
                MyLog.i("scan-------------");
                ETZAnimator.openScanner((Activity) ctx, Constants.SCANNER_REQUEST);
                break;
            case R.id.advanced_btn:
                if (!isadv) {
                    advEditText.setVisibility(View.VISIBLE);
                    isadv = true;
                } else {
                    advEditText.setVisibility(View.GONE);
                    isadv = false;
                }
                break;
            case R.id.send_button:

                String rawAddress = addressEdit.getText().toString().trim();
                String amountStr = amountEdit.getText().toString().trim();
                if (Util.isNullOrEmpty(rawAddress)) {
                    ETZDialog.showCustomDialog(ctx, ctx.getString(R.string.Alert_error), ctx.getString(R.string.Send_noAddress),
                            ctx.getString(R.string.AccessibilityLabels_close), null, new BRDialogView.BROnClickListener() {
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

                if (isSendingTokens) {
                    String data = createTransaction.createTokenTransferData(rawAddress, Convert.toWei(amountStr, Convert.Unit.ETHER).toBigInteger());
                    getGasEstimate(tokenInfo.address, amountStr, data);
                } else {
                    getGasEstimate(rawAddress, amountStr, "0x");
                }
                break;
        }
    }

    public void isShowAdv(boolean show) {
        if (show) {
            advEditText.setVisibility(View.VISIBLE);
            isadv = true;
        } else {
            advEditText.setVisibility(View.GONE);
            isadv = false;
        }
    }

    private void sayClipboardEmpty() {
        ETZDialog.showCustomDialog(ctx, "", ctx.getResources().getString(R.string.Send_emptyPasteboard),
                ctx.getString(R.string.AccessibilityLabels_close), null, new BRDialogView.BROnClickListener() {
                    @Override
                    public void onClick(BRDialogView brDialogView) {
                        brDialogView.dismiss();
                    }
                }, null, null, 0);
    }

    private void sayInvalidClipboardData() {
        ETZDialog.showCustomDialog(ctx, "", ctx.getResources().getString(R.string.Send_invalidAddressTitle),
                ctx.getString(R.string.AccessibilityLabels_close), null, new BRDialogView.BROnClickListener() {
                    @Override
                    public void onClick(BRDialogView brDialogView) {
                        brDialogView.dismiss();
                    }
                }, null, null, 0);
    }

    public void scanResultSetText(String address) {
        addressEdit.setText(address);
        MyLog.i("*************request.amount=" + address);
//        amountEdit.setText(request.amount.toString());
    }

    //只在etz显示data输入框
    public void visibleAdvView(String iso) {
        isoButton.setText(iso);
        isoText.setText(iso + ":");
//        String formattedBalance = CurrencyUtils.getFormattedAmount(ctx, iso,
//                isIsoCrypto ? wm.getSmallestCryptoForCrypto(app, isoBalance) : isoBalance);
//
//        balanceString = String.format(getString(R.string.Send_balance), formattedBalance);
        if (iso.equalsIgnoreCase("SEEK")) {
            commentDataView.setVisibility(View.VISIBLE);
        } else {
            commentDataView.setVisibility(View.GONE);
        }

        if (iso.equalsIgnoreCase("BTC")) {
            advBtnView.setVisibility(View.GONE);
        } else {
            advBtnView.setVisibility(View.VISIBLE);
        }
    }

    private void getGasEstimate(final String to, final String amount, final String data) {
        ETZExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                final String ethUrl = BaseUrl.getEthereumRpcUrl();
                final JSONObject payload = new JSONObject();
                final JSONArray params = new JSONArray();
                MyLog.i("gas-----------" + amount);
                BigDecimal bv = new BigDecimal(Util.isNullOrEmpty(amount) ? "0" : amount);
                String value = bv.multiply(new BigDecimal(EtzWalletManager.ETHER_WEI)).setScale(0).toString();
                MyLog.i("gas-----------" + value);
                try {
                    JSONObject json = new JSONObject();
                    json.put("from", walletBean.getAddress());
                    json.put("to", to);
                    if (Util.isNullOrEmpty(value) || new BigDecimal(value) == BigDecimal.ZERO) {
                        json.put("value", "");
                    } else {
//                        json.put("value", "0x" + new BigInteger("0", 10).toString(16));
                        json.put("value", "0x0" );
                    }
                    json.put("data", data);
                    params.put(json);
                    payload.put(Constants.JSONRPC, "2.0");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    payload.put(Constants.METHOD, Constants.ETH_ESTIMATE_GAS);
                    payload.put(Constants.PARAMS, params);
                    payload.put(Constants.ID, "0");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                MyLog.i("gasLimit=== " + payload.toString());
                HttpRequets.postRequest(ethUrl, getClass(), payload.toString(), new JsonCallback<ResponseGasBean>() {
                    @Override
                    public void onSuccess(Response<ResponseGasBean> response) {

                        if (response.body().result != null) {
                            String gas = response.body().result;
                            MyLog.i("getGasEstimate: gasLimit==" + gas);
                            String gasl = new BigInteger(gas.substring(2, gas.length()), 16).toString(10);
                            String content = to + "\n\n" + ctx.getString(R.string.Confirmation_amountLabel) + amount + "(" + tokenInfo.symbol + ")\n";
                            InputPwdView pwdView = new InputPwdView(ctx, content, pwd -> {
                                if (isSendingTokens) {
                                    createTransaction.createERC20Transfer(walletBean,
                                            to,
                                            tokenInfo.address,
                                            Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger(),
                                            new BigInteger(Constants.DEFAULT_GAS_PRICE),
                                            new BigInteger(gasl),
                                            pwd,
                                            data).subscribeOn(Schedulers.io())
                                            .subscribe(SendPopupWindow.this::onSuccessTransaction, SendPopupWindow.this::onErrorTransaction);

                                } else {
                                    createTransaction.createTransaction(walletBean,
                                            to,
                                            Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger(),
                                            new BigInteger(Constants.DEFAULT_GAS_PRICE),
                                            new BigInteger(gasl),
                                            "",
                                            pwd).subscribeOn(Schedulers.io())
                                            .subscribe(SendPopupWindow.this::onSuccessTransaction, SendPopupWindow.this::onErrorTransaction);
                                }
                            });

                            dialog = new Dialog(ctx);
                            dialog.setTitle(R.string.VerifyPin_touchIdMessage);
                            dialog.setContentView(pwdView);
                            dialog.setCancelable(true);
                            dialog.setCanceledOnTouchOutside(true);
                            dialog.show();


                        } else if (response.body().error != null) {
                            ETZDialog.showSimpleDialog(ctx, ctx.getString(R.string.WipeWallet_failedTitle), response.body().error.message);
                        }

                    }

                    @Override
                    public void onError(Response<ResponseGasBean> response) {

                        if (response.body().error != null) {
                            ETZDialog.showSimpleDialog(ctx, ctx.getString(R.string.WipeWallet_failedTitle), response.body().error.message);
                        } else {
                            ETZDialog.showSimpleDialog(ctx, ctx.getString(R.string.WipeWallet_failedTitle), ctx.getString(R.string.socket_exception));
                        }

                    }
                });


            }
        });
    }


    private void onSuccessTransaction(String transaction) {


        ToastUtils.showLongToast(ctx,R.string.transfer_ok + transaction);
        dialog.dismiss();
        dismiss();

    }

    private void onErrorTransaction(Throwable throwable) {
        ToastUtils.showLongToast(ctx,"交易失败：" + throwable.getMessage());
    }

    /**
     * 输入首字不能为0和.
     */
    private TextWatcher addET = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            if (Double.parseDouble(Util.isNullOrEmpty(s.toString())?"0":s.toString()) >= Double.parseDouble(balance)) {
                ToastUtils.showLongToast(ctx,R.string.Receive_not_sufficient_funds);
                amountEdit.setTextColor(ctx.getResources().getColor(R.color.dialog_know_btn_bg_color));
                balanceText.setTextColor(ctx.getResources().getColor(R.color.dialog_know_btn_bg_color));
            } else {
                amountEdit.setTextColor(ctx.getResources().getColor(R.color.black_trans));
                balanceText.setTextColor(ctx.getResources().getColor(R.color.black_trans));
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {



        }

        @Override
        public void afterTextChanged(Editable edt) {
        }
    };

}
