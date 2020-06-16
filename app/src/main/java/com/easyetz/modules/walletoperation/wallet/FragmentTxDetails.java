package com.easyetz.modules.walletoperation.wallet;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.easyetz.R;
import com.easyetz.base.Constants;
import com.easyetz.bean.CurrencyEntity;
import com.easyetz.bean.TransactionRecords;
import com.easyetz.blockchain.BaseWalletManager;
import com.easyetz.blockchain.EtzWalletManager;
import com.easyetz.modules.walletmanage.WalletsMaster;
import com.easyetz.utils.ClipboardManager;
import com.easyetz.utils.CurrencyUtils;
import com.easyetz.utils.DateUtil;
import com.easyetz.utils.Util;
import com.easyetz.view.MText;

import java.math.BigDecimal;

/**
 * Created by byfieldj on 2/26/18.
 * <p>
 * Reusable dialog fragment that display details about a particular transaction
 */

public class FragmentTxDetails extends DialogFragment {

    private static final String TAG = FragmentTxDetails.class.getSimpleName();

    private TransactionRecords mTransaction;

    private MText mTxAction;
    private MText mTxAmount;
    private MText mTxStatus;
    private MText mTxDate;
    private MText mToFrom;
    private MText mToFromAddress;

    private MText mGasPrice;
    private MText mGasLimit;
    private MText fee_primary;


    private MText mConfirmedInBlock;
    private MText mTransactionId;


    private ImageButton mCloseButton;
    private String iso;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, 0);

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.transaction_details, container, false);

        mTxAction = rootView.findViewById(R.id.tx_action);
        mTxAmount = rootView.findViewById(R.id.tx_amount);

        mTxStatus = rootView.findViewById(R.id.tx_status);
        mTxDate = rootView.findViewById(R.id.tx_date);
        mToFrom = rootView.findViewById(R.id.tx_to_from);
        mToFromAddress = rootView.findViewById(R.id.tx_to_from_address);
        mConfirmedInBlock = rootView.findViewById(R.id.confirmed_in_block_number);
        mTransactionId = rootView.findViewById(R.id.transaction_id);
        mCloseButton = rootView.findViewById(R.id.close_button);
        mGasPrice = rootView.findViewById(R.id.gas_price);
        mGasLimit = rootView.findViewById(R.id.gas_limit);
        fee_primary = rootView.findViewById(R.id.fee_primary);

        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        updateUi();
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    public void setTransaction(TransactionRecords item, String iso) {

        this.mTransaction = item;
        this.iso = iso;
    }

    private void updateUi() {
        Activity app = getActivity();

        BaseWalletManager walletManager = WalletsMaster.getInstance().getWalletByIso(getActivity(),iso);

        // Set mTransction fields
        if (mTransaction != null) {
            String price=new BigDecimal(mTransaction.gasPrice).divide(new BigDecimal("1000000000")).toPlainString();
            mGasPrice.setText(String.format("%s %s", price, "gwei"));
            mGasLimit.setText(new BigDecimal(mTransaction.gasLimt).toPlainString());
            String mfee=new BigDecimal(mTransaction.gasPrice).multiply(new BigDecimal(mTransaction.gasLimt)).divide(new BigDecimal(EtzWalletManager.ETHER_WEI)).toPlainString();
            fee_primary.setText(mfee);
        }
//        mTxStatus.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        BigDecimal cryptoAmount = new BigDecimal(mTransaction.value).setScale(walletManager.getMaxDecimalPlaces(app, iso), Constants.ROUNDING_MODE);


        mTxAction.setText(!mTransaction.isReceived ? getString(R.string.TransactionDetails_titleSent) : getString(R.string.TransactionDetails_titleReceived));
        mToFrom.setText(!mTransaction.isReceived ? getString(R.string.Confirmation_to) + " " : getString(R.string.TransactionDetails_addressViaHeader) + " ");

        mToFromAddress.setText(!mTransaction.isReceived ? mTransaction.to : mTransaction.from); //showing only the destination address

        mToFromAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the default color based on theme
                final int color = mToFromAddress.getCurrentTextColor();

                mToFromAddress.setTextColor(getResources().getColor(R.color.light_gray));
                String address = mToFromAddress.getText().toString();
                ClipboardManager.putClipboard(getActivity(), address);
                Toast.makeText(getActivity(), getString(R.string.Receive_copied), Toast.LENGTH_LONG).show();

                mToFromAddress.setTextColor(color);


            }
        });

        //this is always crypto amount
        mTxAmount.setText(CurrencyUtils.getFormattedAmount(app, iso, mTransaction.isReceived ? cryptoAmount : cryptoAmount.negate()));

        if (mTransaction.isReceived) {
            mTxAmount.setTextColor(getResources().getColor(R.color.transaction_amount_received_color));
        }

        // timestamp is 0 if it's not confirmed in a block yet so make it now
        mTxDate.setText(DateUtil.getFullDate(Long.valueOf(Util.isNullOrEmpty(mTransaction.date) ? "0" : mTransaction.date) == 0 ? System.currentTimeMillis() : (Long.valueOf(mTransaction.date) * DateUtils.SECOND_IN_MILLIS)));

        // Set the transaction id
        mTransactionId.setText(mTransaction.tid);

        // Allow the transaction id to be copy-able
        mTransactionId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Get the default color based on theme
                final int color = mTransactionId.getCurrentTextColor();

                mTransactionId.setTextColor(getResources().getColor(R.color.light_gray));
                String id = mTransaction.tid;
                ClipboardManager.putClipboard(getActivity(), id);
                Toast.makeText(getActivity(), getString(R.string.Receive_copied), Toast.LENGTH_LONG).show();

                mTransactionId.setTextColor(color);

            }
        });

        // Set the transaction block number
        mConfirmedInBlock.setText(Util.isNullOrEmpty(mTransaction.blockNumber)?"":mTransaction.blockNumber);
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
    }


}

