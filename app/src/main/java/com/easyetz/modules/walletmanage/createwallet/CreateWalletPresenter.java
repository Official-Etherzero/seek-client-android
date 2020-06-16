package com.easyetz.modules.walletmanage.createwallet;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.easyetz.R;
import com.easyetz.app.MyApp;
import com.easyetz.base.BasePresent;
import com.easyetz.blockchain.walletutils.WalletUtils;
import com.easyetz.sqlite.WalletDataStore;
import com.easyetz.utils.MyLog;
import com.easyetz.utils.ToastUtils;
import com.easyetz.view.CustomToast;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.util.List;

public class CreateWalletPresenter extends BasePresent<CreateWalletView> {

    public boolean verifyInfo(Context app, String walletName, String walletPwd, String confirmPwd) {
        if (TextUtils.isEmpty(walletName)) {//钱包名称不能为空
            ToastUtils.showLongToast(app,R.string.create_wallet_name_input_tips);
            return false;
        } else if (WalletDataStore.getInstance().QueryWalletName(walletName)) {//钱包名称是否存在
            ToastUtils.showLongToast(app,R.string.create_wallet_name_repeat_tips);
            // 同时不可重复
            return false;
        } else if (TextUtils.isEmpty(walletPwd)||walletPwd.length()<6) {
            ToastUtils.showLongToast(app,R.string.create_wallet_pwd_input_tips);
            // 同时判断强弱
            return false;
        } else if (TextUtils.isEmpty(confirmPwd) || !TextUtils.equals(confirmPwd, walletPwd)) {
            ToastUtils.showLongToast(app,R.string.create_wallet_pwd_confirm_input_tips);
            return false;
        }
        return true;
    }

    public boolean verifyMnemonic(Context app,List<String> list) {
        if (list.size() != 12) {
            ToastUtils.showLongToast(app,R.string.recovery_wallet_mnemonic);
            return false;
        }
        return true;
    }

    /**
     *
     * @param key
     * @param walletType
     * @return
     */
    public boolean verifyPrivateKeyToAddress(Context app,String key, String walletType) {
        ECKeyPair ecKeyPair = ECKeyPair.create(Numeric.toBigInt(key));
        String address = Keys.toChecksumAddress(Keys.getAddress(ecKeyPair));
        MyLog.i("--------------" + address);
        boolean isExist = WalletDataStore.getInstance().addressQueryWallet(walletType, address);
        if (isExist) {
            ToastUtils.showLongToast(app,R.string.wallet_exist);
        }
        return isExist;
    }

    /**
     * 判断钱包是否存在
     * @param keystore
     * @param pwd
     * @param walletName
     * @param walletType
     * @return
     */
    public boolean verifyKeystoreToAddress(Context app,String keystore, String pwd, String walletName, String walletType) {
        if (TextUtils.isEmpty(walletName)) {//钱包名称不能为空
            ToastUtils.showLongToast(app,R.string.create_wallet_name_input_tips);
            return true;
        } else if (WalletDataStore.getInstance().QueryWalletName(walletName)) {//钱包名称是否存在
            ToastUtils.showLongToast(app,R.string.create_wallet_name_repeat_tips);
            // 同时不可重复
            return true;
        }else if (TextUtils.isEmpty(pwd)) {
            ToastUtils.showLongToast(app,R.string.create_wallet_pwd_input_tips);
            // 同时判断强弱
            return false;
        }
        ECKeyPair ecKeyPair = null;
        try {
            WalletFile walletFile = WalletUtils.objectMapper.readValue(keystore, WalletFile.class);
            ecKeyPair = Wallet.decrypt(pwd, walletFile);
        } catch (IOException e) {
            e.printStackTrace();
            MyLog.e(e.toString());
        } catch (CipherException e) {
            MyLog.i("----------CipherException---" + e.toString());
            ToastUtils.showLongToast(app,R.string.load_wallet_by_official_wallet_keystore_input_tip);
            e.printStackTrace();
        }
        if (ecKeyPair == null) return true;
        String address = Keys.toChecksumAddress(Keys.getAddress(ecKeyPair));
        MyLog.i("--------------" + address);
        boolean isExist = WalletDataStore.getInstance().addressQueryWallet(walletType, address);
        if (isExist) {
            ToastUtils.showLongToast(app,R.string.wallet_exist);
        }
        return isExist;
    }

    public boolean verifyKeyToWallet(Context app,String key){
        boolean isExist= WalletDataStore.getInstance().mnemonicQueryWallet("BTC",key);
        if (isExist){
            ToastUtils.showLongToast(app,R.string.wallet_exist);
        }
        return isExist;
    }

}
