package com.easyetz.modules.walletmanage.createwallet;

import com.easyetz.bean.WalletBean;
import com.easyetz.blockchain.walletutils.BtcWalletUtil;
import com.easyetz.blockchain.walletutils.WalletUtils;

import java.util.Arrays;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class CreateWalletInteract {

    public Single<WalletBean> create(final String name, final String pwd, String confirmPwd) {
        return Single.fromCallable(() -> {
            WalletBean ethWallet = WalletUtils.generateMnemonic(name, pwd);
            return ethWallet;
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

    }

    public Single<WalletBean> loadWalletByKeystore(final String keystore, final String pwd, final String walletName, final String wallettype) {
        return Single.fromCallable(() -> {
            WalletBean ethWallet = WalletUtils.loadWalletByKeystore(keystore, pwd, walletName, wallettype);
            return ethWallet;
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<WalletBean> loadWalletByPrivateKey(final String privateKey, final String pwd, final String walletName, final String wallettype) {
        return Single.fromCallable(() -> {

                    WalletBean ethWallet = null;
            if (wallettype.equalsIgnoreCase("BTC")) {
                ethWallet= BtcWalletUtil.fromWatchingKeyB58(BtcWalletUtil.getParams(),privateKey,walletName,pwd);
            } else {
                ethWallet  = WalletUtils.loadWalletByPrivateKey(privateKey, pwd, walletName, wallettype);
            }
                    return ethWallet;
                }
        ).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

    }

    public Single<WalletBean> loadWalletByMnemonic(final String bipPath, final List<String> mnemonic, final String pwd, final String walletName, final String wallettype) {
        return Single.fromCallable(() -> {
            WalletBean ethWallet = null;
            if (wallettype.equalsIgnoreCase("BTC")) {
                ethWallet= BtcWalletUtil.getMnemonicCodeInputWall(mnemonic,pwd,walletName);
            } else {
                ethWallet = WalletUtils.importMnemonic(bipPath, mnemonic, pwd, walletName, wallettype);
            }
            return ethWallet;
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());


    }

}
