package com.easyetz.modules.walletoperation;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.easyetz.base.BaseActivity;
import com.easyetz.base.BasePresent;
import com.easyetz.base.BaseView;
import com.easyetz.base.BaseViewModel;
import com.easyetz.bean.ConfirmationType;
import com.easyetz.bean.GasSettings;
import com.easyetz.bean.NetworkInfo;
import com.easyetz.bean.WalletBean;
import com.easyetz.utils.ToastUtils;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import io.reactivex.schedulers.Schedulers;

public class ConfirmationViewModel extends BaseViewModel {
    private final MutableLiveData<String> newTransaction = new MutableLiveData<>();
    private final MutableLiveData<WalletBean> defaultWallet = new MutableLiveData<>();
    private final MutableLiveData<GasSettings> gasSettings = new MutableLiveData<>();

    private GasSettings gasSettingsOverride = null;   // use setting

    private final MutableLiveData<NetworkInfo> defaultNetwork = new MutableLiveData<>();

    private final CreateTransactionInteract createTransactionInteract;


    public ConfirmationViewModel(CreateTransactionInteract createTransactionInteract) {
        this.createTransactionInteract = createTransactionInteract;
    }

    private void onCreateTransaction(String transaction) {
        progress.postValue(false);
        newTransaction.postValue(transaction);
    }

    public void createTransaction(WalletBean walletBean,String password, String to, BigInteger amount, BigInteger gasPrice, BigInteger gasLimit) {
        progress.postValue(true);

        createTransactionInteract.createTransaction(walletBean, to, amount, gasPrice, gasLimit,"", password)
                .subscribeOn(Schedulers.io())
                .subscribe(this::onCreateTransaction, this::onError);

    }

    public void createTokenTransfer(WalletBean walletBean,String password, String to, String contractAddress,
                                    BigInteger amount, BigInteger gasPrice, BigInteger gasLimit,String data) {
        progress.postValue(true);
        createTransactionInteract.createERC20Transfer(walletBean, to, contractAddress, amount, gasPrice, gasLimit, password,data)
                .subscribeOn(Schedulers.io())
                .subscribe(this::onCreateTransaction, this::onError);
    }





    private void onGasPrice(BigInteger currentGasPrice)
    {
        if (this.gasSettings.getValue() != null //protect against race condition
                && this.gasSettingsOverride == null //only update if user hasn't overriden
                )
        {
            GasSettings updateSettings = new GasSettings(currentGasPrice, gasSettings.getValue().gasLimit);
            this.gasSettings.postValue(updateSettings);
        }
    }

    /**
     * 生成代币交易DATA数据
     * @param to
     * @param tokenAmount
     * @return
     */
    public  String createTokenTransferData(String to, BigInteger tokenAmount) {
        List<Type> params = Arrays.<Type>asList(new Address(to), new Uint256(tokenAmount));

        List<TypeReference<?>> returnTypes = Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {
        });

        Function function = new Function("transfer", params, returnTypes);
        return FunctionEncoder.encode(function);
//        return Numeric.hexStringToByteArray(Numeric.cleanHexPrefix(encodedFunction));
    }

}
