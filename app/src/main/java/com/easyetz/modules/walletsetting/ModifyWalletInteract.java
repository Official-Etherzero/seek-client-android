package com.easyetz.modules.walletsetting;


import com.easyetz.blockchain.walletutils.WalletUtils;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ModifyWalletInteract {

    public ModifyWalletInteract() {
    }

    /**
     * 修改钱包密码
     * @param walletId
     * @param oldPassword
     * @param newPassword
     * @return
     */
    public Single<Boolean> modifyWalletPwd(final String walletId, final String oldPassword, final String newPassword) {
        return Single.fromCallable(() -> {
            return WalletUtils.modifyPassword(walletId, oldPassword, newPassword);
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

    }

    /***
     * 导出密钥
     * @param walletId
     * @param password
     * @return
     */
    public Single<String>  deriveWalletPrivateKey(final String walletId, final String password) {

        return Single.fromCallable(() -> {
            return WalletUtils.derivePrivateKey(walletId, password);
        } ).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 导出Keystore
     * @param walletId
     * @param password
     * @return
     */
    public  Single<String>  deriveWalletKeystore(final String walletId, final String password) {

        return Single.fromCallable(() -> {
            return WalletUtils.deriveKeystore(walletId, password);
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        // mView.showDeriveKeystore(keystore);

    }


}