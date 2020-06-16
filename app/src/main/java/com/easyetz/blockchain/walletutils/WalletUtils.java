package com.easyetz.blockchain.walletutils;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.security.keystore.UserNotAuthenticatedException;
import android.support.annotation.Nullable;

import com.easyetz.R;
import com.easyetz.app.MyApp;
import com.easyetz.bean.WalletBean;
import com.easyetz.sqlite.BRSQLiteHelper;
import com.easyetz.sqlite.WalletDataStore;
import com.easyetz.utils.AppFilePath;
import com.easyetz.utils.ETZKeyStore;
import com.easyetz.utils.Md5Utils;
import com.easyetz.utils.MyLog;
import com.easyetz.utils.SecureRandomUtils;
import com.easyetz.utils.SharedPrefsUitls;
import com.easyetz.utils.ToastUtils;
import com.easyetz.utils.Util;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.ChildNumber;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.HDKeyDerivation;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.crypto.MnemonicException;
import org.bitcoinj.wallet.DeterministicSeed;
import org.web3j.crypto.Bip39Wallet;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.MnemonicUtils;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.utils.Numeric;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static com.google.common.base.Preconditions.checkArgument;

public class WalletUtils {

    public static ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();

    /**
     * 随机
     */
    private static final SecureRandom secureRandom = SecureRandomUtils.secureRandom();
    private Credentials credentials;

    /**
     * 通用的以太坊基于bip44协议的助记词路径 （imtoken jaxx Metamask myetherwallet）
     */
    public static String ETH_JAXX_TYPE = "m/44'/60'/0'/0/0";
    public static String ETH_LEDGER_TYPE = "m/44'/60'/0'/0";
    public static String ETH_CUSTOM_TYPE = "m/44'/60'/1'/0/0";


    /**
     * 创建助记词，并通过助记词创建钱包
     *
     * @param walletName
     * @param pwd
     * @return
     */
    public static WalletBean generateMnemonic(String walletName, String pwd) {
        String[] pathArray = ETH_JAXX_TYPE.split("/");
        String passphrase = "";
        long creationTimeSeconds = System.currentTimeMillis() / 1000;
        AssetManager assetManager = MyApp.getmInstance().getResources().getAssets();
        List<String> mnemonicCode = null;
        String fileName;
        String languageCode = Locale.getDefault().getLanguage();
        Context ctx = MyApp.getmInstance();
        MyLog.i("glanguageCode===" + ctx.getResources().getConfiguration().locale.getCountry());
        if (languageCode == null) languageCode = "en";
        if (ctx.getResources().getConfiguration().locale.getCountry().equals("TW")) {
            fileName = getWordFileName("zhTW");
        } else {
            fileName = getWordFileName(languageCode);
        }
        try {
            InputStream wordStream = assetManager.open(fileName);
            MnemonicCode mc = new MnemonicCode(wordStream, null);
            mnemonicCode = mc.toMnemonic(getEntropy(secureRandom, 128));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MnemonicException.MnemonicLengthException e) {
            e.printStackTrace();
        }
        DeterministicSeed ds = new DeterministicSeed(mnemonicCode, null, passphrase, creationTimeSeconds);
        MyLog.i("DeterministicSeed=====" + new String(ds.getSeedBytes()));

//创建BTC钱包
//        NetworkParameters parameters = BtcWalletUtil.getParams();
//        WalletBean btc = BtcWalletUtil.createWallet(parameters, ds, walletName,pwd);
//        MyLog.i(btc.toString());

        WalletBean wallet = generateWalletByMnemonic(walletName, ds, pathArray, pwd, "");
        MyLog.i(wallet.toString());
        return wallet;
    }

    private static byte[] getEntropy(SecureRandom random, int bits) {
        checkArgument(bits <= 512, "requested entropy size too large");

        byte[] seed = new byte[bits / 8];
        random.nextBytes(seed);
        return seed;
    }

    /**
     * 通过导入助记词，导入钱包
     *
     * @param path 路径
     * @param list 助记词
     * @param pwd  密码
     * @return
     */
    public static WalletBean importMnemonic(String path, List<String> list, String pwd, String walletName, String wallettype) {
//        if (!path.startsWith("m") && !path.startsWith("M")) {
//            //参数非法
//            return null;
//        }
        String[] pathArray = ETH_JAXX_TYPE.split("/");
        if (pathArray.length <= 1) {
            //内容不对
            return null;
        }

        String passphrase = "";
        long creationTimeSeconds = System.currentTimeMillis() / 1000;
        DeterministicSeed ds = new DeterministicSeed(list, null, passphrase, creationTimeSeconds);

        //首次导入钱包，导入BTC钱包
//        if (Util.isNullOrEmpty(wallettype))
//            BtcWalletUtil.createWallet(BtcWalletUtil.getParams(), ds, walletName,pwd);
        return generateWalletByMnemonic(walletName, ds, pathArray, pwd, wallettype);
    }


    /**
     * @param walletName 钱包名称
     * @param ds         助记词加密种子
     * @param pathArray  助记词标准
     * @param pwd        密码
     * @return
     */
    @Nullable
    public static WalletBean generateWalletByMnemonic(String walletName, DeterministicSeed ds,
                                                      String[] pathArray, String pwd, String walletType) {
        //种子
        byte[] seedBytes = ds.getSeedBytes();
        System.out.println();
        //助记词
        List<String> mnemonic = ds.getMnemonicCode();
        MyLog.i("mnemonic: " + mnemonic);
        if (seedBytes == null)
            return null;
        DeterministicKey dkKey = HDKeyDerivation.createMasterPrivateKey(seedBytes);

        for (int i = 1; i < pathArray.length; i++) {
            ChildNumber childNumber;
            if (pathArray[i].endsWith("'")) {
                int number = Integer.parseInt(pathArray[i].substring(0,
                        pathArray[i].length() - 1));
                childNumber = new ChildNumber(number, true);
            } else {
                int number = Integer.parseInt(pathArray[i]);
                childNumber = new ChildNumber(number, false);
            }
            dkKey = HDKeyDerivation.deriveChildKey(dkKey, childNumber);
        }

        ECKeyPair keyPair = ECKeyPair.create(dkKey.getPrivKeyBytes());
        WalletBean wallet = null;
        String phrase = "";
        try {
            phrase = ETZKeyStore.encryptData(convertMnemonicList(mnemonic));
            MyLog.i("DeterministicSeed=====" + phrase);

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (Util.isNullOrEmpty(walletType)) {
            //创建保存ETH钱包
//            WalletBean ethWallet = generateEthWallet(walletName, pwd, keyPair);
//            if (ethWallet != null) {
//                ethWallet.setMnemonic(phrase);
//                List<String> list = new ArrayList<>();
//                list.add("ETH");
//                SharedPrefsUitls.getInstance().putWalletTokenList(ethWallet.getId(), list);
//            }
//            boolean iseth = WalletDataStore.getInstance().insertWallet(getContentValues(ethWallet));
            wallet = generateEtzWallet(walletName, pwd, keyPair);
            if (wallet != null) {
                wallet.setMnemonic(phrase);
                List<String> list = new ArrayList<>();
                list.add("SEEK");
//                list.add("EASH");
                SharedPrefsUitls.getInstance().putWalletTokenList(wallet.getId(), list);
                SharedPrefsUitls.getInstance().putCurrentWallet(wallet.getId());
                SharedPrefsUitls.getInstance().putCurrentWalletAddress(wallet.getAddress());

            }

            boolean isetz = WalletDataStore.getInstance().insertWallet(getContentValues(wallet));

        } else {
            wallet = generateWallet(walletName, pwd, keyPair, walletType);
            if (wallet != null) {
                wallet.setMnemonic(phrase);
                List<String> list = new ArrayList<>();
                if (walletType.equalsIgnoreCase("SEEK")) {
                    list.add("SEEK");
//                    list.add("EASH");
                } else {
                    list.add("ETH");
                }
                SharedPrefsUitls.getInstance().putWalletTokenList(wallet.getId(), list);
                SharedPrefsUitls.getInstance().putCurrentWallet(wallet.getId());
                SharedPrefsUitls.getInstance().putCurrentWalletAddress(wallet.getAddress());
            }
            boolean isetz = WalletDataStore.getInstance().insertWallet(getContentValues(wallet));
        }

        return wallet;
    }

    public static String convertMnemonicList(List<String> mnemonics) {
        StringBuilder sb = new StringBuilder();
        int size = mnemonics.size();

        for (int i = 0; i < size; i++) {
            sb.append(mnemonics.get(i));
            if (i != size - 1) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    /**
     * ETH钱包
     *
     * @param walletName
     * @param pwd
     * @param ecKeyPair
     * @return
     */
    private static WalletBean generateEthWallet(String walletName, String pwd, ECKeyPair ecKeyPair) {
        return generateWallet(walletName, pwd, ecKeyPair, "ETH");
    }

    /**
     * ETZ钱包
     *
     * @param walletName
     * @param pwd
     * @param ecKeyPair
     * @return
     */
    private static WalletBean generateEtzWallet(String walletName, String pwd, ECKeyPair ecKeyPair) {
        return generateWallet(walletName, pwd, ecKeyPair, "SEEK");
    }

    /**
     * 创建钱包实体保存文件
     *
     * @param walletName
     * @param pwd
     * @param ecKeyPair
     * @param walletType
     * @return
     */
    @Nullable
    private static WalletBean generateWallet(String walletName, String pwd, ECKeyPair ecKeyPair, String walletType) {
        WalletFile keyStoreFile;
        try {
            keyStoreFile = Wallet.create(pwd, ecKeyPair, 1024, 1); // WalletUtils. .generateNewWalletFile();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        BigInteger publicKey = ecKeyPair.getPublicKey();
        String s = publicKey.toString();
        MyLog.i("publicKey = " + s);

        String wallet_dir = AppFilePath.Wallet_DIR;
        MyLog.i("wallet_dir = " + wallet_dir);
        int wid = SharedPrefsUitls.getInstance().getWalletId(walletType) + 1;//或取当前有几个同类型的钱包

        String walletId = walletType + "-" + wid;

        String keystorePath = "keystore_" + walletId + ".json";
        File destination = new File(wallet_dir, keystorePath);

        //目录不存在则创建目录，创建不了则报错
        if (!createParentDir(destination)) {
            return null;
        }
        try {
            objectMapper.writeValue(destination, keyStoreFile);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        WalletBean wallet = new WalletBean();
        MyLog.i("---------------wid=" + walletId);
        wallet.setId(walletId);
        wallet.setName(walletName);
        wallet.setAddress(Keys.toChecksumAddress(keyStoreFile.getAddress()));
        wallet.setKeystorePath(destination.getAbsolutePath());
        wallet.setPassword(Md5Utils.md5(pwd));
        if (walletType.equalsIgnoreCase("SEEK")) {
            wallet.setStartColor("#00BDFF");
            wallet.setEndColor("#5E7AA3");
        } else {
            wallet.setStartColor("#5e6fa5");
            wallet.setEndColor("#5e6fa5");
        }
        wallet.setDecimals(18);
        SharedPrefsUitls.getInstance().putWalletId(walletType, wid);//保存钱包个数
        return wallet;
    }

    /**
     * 通过keystore.json文件导入钱包
     *
     * @param keystore
     * @param pwd
     * @param walletName
     * @param wallettype
     * @return
     */
    public static WalletBean loadWalletByKeystore(String keystore, String pwd, String walletName, String wallettype) {
        ECKeyPair ecKeyPair = null;
        try {
            WalletFile walletFile = null;
            walletFile = objectMapper.readValue(keystore, WalletFile.class);
            ecKeyPair = Wallet.decrypt(pwd, walletFile);
        } catch (IOException e) {
            e.printStackTrace();
            MyLog.e(e.toString());
        } catch (CipherException e) {
            MyLog.e(e.toString());
            ToastUtils.showLongToast(R.string.load_wallet_by_official_wallet_keystore_input_tip);
            e.printStackTrace();
        }
        if (ecKeyPair != null) {
            WalletBean walletBean = generateWallet(walletName, pwd, ecKeyPair, wallettype);
            if (walletBean != null) {
                List<String> list = new ArrayList<>();
                if (wallettype.equalsIgnoreCase("SEEK")) {
                    list.add("SEEK");
//                    list.add("EASH");
                } else {
                    list.add("ETH");
                }
                SharedPrefsUitls.getInstance().putWalletTokenList(walletBean.getId(), list);
                SharedPrefsUitls.getInstance().putCurrentWallet(walletBean.getId());
                SharedPrefsUitls.getInstance().putCurrentWalletAddress(walletBean.getAddress());
                boolean isetz = WalletDataStore.getInstance().insertWallet(getContentValues(walletBean));
            }
            return walletBean;
        }
        return null;
    }

    /**
     * 通过明文私钥导入钱包
     *
     * @param privateKey
     * @param pwd
     * @param walletName
     * @param wallettype
     * @return
     */
    public static WalletBean loadWalletByPrivateKey(String privateKey, String pwd, String walletName, String wallettype) {
        Credentials credentials = null;
        ECKeyPair ecKeyPair = ECKeyPair.create(Numeric.toBigInt(privateKey));
        WalletBean walletBean = generateWallet(walletName, pwd, ecKeyPair, wallettype);
        if (walletBean != null) {
            List<String> list = new ArrayList<>();
            if (wallettype.equalsIgnoreCase("SEEK")) {
                list.add("SEEK");
            } else {
                list.add("ETH");
            }
            SharedPrefsUitls.getInstance().putWalletTokenList(walletBean.getId(), list);
            SharedPrefsUitls.getInstance().putCurrentWallet(walletBean.getId());
            SharedPrefsUitls.getInstance().putCurrentWalletAddress(walletBean.getAddress());
            boolean isetz = WalletDataStore.getInstance().insertWallet(getContentValues(walletBean));
        }
        return walletBean;
    }


    private static boolean createParentDir(File file) {
        //判断目标文件所在的目录是否存在
        if (!file.getParentFile().exists()) {
            //如果目标文件所在的目录不存在，则创建父目录
            System.out.println("目标文件所在目录不存在，准备创建");
            if (!file.getParentFile().mkdirs()) {
                System.out.println("创建目标文件所在目录失败！");
                return false;
            }
        }
        return true;
    }


    public static boolean modifyWalletNick(String walletId, String nick) {
        WalletBean ethWallet = WalletDataStore.getInstance().queryWallet(walletId);
        ethWallet.setName(nick);
        return WalletDataStore.getInstance().updataWallet(getContentValues(ethWallet), walletId);
    }

    /**
     * 修改钱包密码
     *
     * @param walletId
     * @param oldPassword
     * @param newPassword
     * @return
     */
    public static boolean modifyPassword(String walletId, String oldPassword, String newPassword) {
        WalletBean ethWallet = WalletDataStore.getInstance().queryWallet(walletId);
        Credentials credentials = null;
        ECKeyPair keypair = null;
        try {
            credentials = org.web3j.crypto.WalletUtils.loadCredentials(oldPassword, ethWallet.getKeystorePath());
            keypair = credentials.getEcKeyPair();
            File destinationDirectory = new File(AppFilePath.Wallet_DIR, "keystore_" + walletId + ".json");
            WalletFile walletFile = Wallet.createLight(newPassword, keypair);
            objectMapper.writeValue(destinationDirectory, walletFile);

            ethWallet.setPassword(Md5Utils.md5(newPassword));
            return WalletDataStore.getInstance().updataWallet(getContentValues(ethWallet), walletId);
        } catch (CipherException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 导出明文私钥
     *
     * @param walletId 钱包Id
     * @param pwd      钱包密码
     * @return
     */
    public static String derivePrivateKey(String walletId, String pwd) {
        WalletBean wallet = WalletDataStore.getInstance().queryWallet(walletId);
        Credentials credentials;
        ECKeyPair keypair;
        String privateKey = null;
        try {
            credentials = org.web3j.crypto.WalletUtils.loadCredentials(pwd, wallet.getKeystorePath());
            keypair = credentials.getEcKeyPair();
            privateKey = Numeric.toHexStringNoPrefixZeroPadded(keypair.getPrivateKey(), Keys.PRIVATE_KEY_LENGTH_IN_HEX);
        } catch (CipherException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return privateKey;
    }

    /**
     * 导出keystore文件
     *
     * @param walletId
     * @param pwd
     * @return
     */
    public static String deriveKeystore(String walletId, String pwd) {
        WalletBean wallet = WalletDataStore.getInstance().queryWallet(walletId);
        String keystore = null;
        WalletFile walletFile;
        try {
            walletFile = objectMapper.readValue(new File(wallet.getKeystorePath()), WalletFile.class);
            keystore = objectMapper.writeValueAsString(walletFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return keystore;
    }

    /**
     * 删除钱包
     *
     * @param walletId
     * @return
     */
    public static boolean deleteWallet(String walletId) {
        WalletBean ethWallet = WalletDataStore.getInstance().queryWallet(walletId);
        String wid = SharedPrefsUitls.getInstance().getCurrentWallet();
        if (deleteFile(ethWallet.getKeystorePath())) {
            WalletDataStore.getInstance().deleteWallet(walletId);
            SharedPrefsUitls.getInstance().deleteItem(walletId);
            //如果是删除钱包是当前钱包，
            if (walletId.equalsIgnoreCase(wid)) {
                SharedPrefsUitls.getInstance().deleteItem(SharedPrefsUitls.CURRENT_WALLET_ADDRESS);
                SharedPrefsUitls.getInstance().deleteItem(SharedPrefsUitls.FIRST_WALLET_TAG);
                List<WalletBean> list = WalletDataStore.getInstance().queryAllWallets();
                if (list != null && list.size() > 0) {
                    SharedPrefsUitls.getInstance().putCurrentWallet(list.get(0).getId());
                    SharedPrefsUitls.getInstance().putCurrentWalletAddress(list.get(0).getAddress());
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * 删除单个文件
     *
     * @param fileName 要删除的文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    public static boolean deleteFile(String fileName) {
        File file = new File(fileName);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                MyLog.i("删除单个文件" + fileName + "成功！");
                return true;
            } else {
                MyLog.i("删除单个文件" + fileName + "失败！");
                return false;
            }
        } else {
            MyLog.i("删除单个文件失败：" + fileName + "不存在！");
            return false;
        }
    }

    /**
     * ContentValues
     *
     * @param walletBean
     * @return
     */
    public static ContentValues getContentValues(WalletBean walletBean) {
        ContentValues eth = new ContentValues();
        eth.put(BRSQLiteHelper.WALLET_ID, walletBean.getId());
        eth.put(BRSQLiteHelper.WALLET_NAME, walletBean.getName());
        eth.put(BRSQLiteHelper.WALLET_ADDRESS, walletBean.getAddress());
        eth.put(BRSQLiteHelper.WALLET_PASSWORD, walletBean.getPassword());
        eth.put(BRSQLiteHelper.WALLET_KEYSTOREPATH, walletBean.getKeystorePath());
        eth.put(BRSQLiteHelper.WALLET_MNEMONIC, walletBean.getMnemonic());
        eth.put(BRSQLiteHelper.WALLET_STARTCOLOR, walletBean.getStartColor());
        eth.put(BRSQLiteHelper.WALLET_ENDCOLOR, walletBean.getEndColor());
        eth.put(BRSQLiteHelper.WALLET_DECIMALS, walletBean.getDecimals());
        return eth;

    }

    /**
     * 查询所有钱包
     */
    public static List<WalletBean> loadAll() {
        return WalletDataStore.getInstance().queryAllWallets();
    }

    /**
     * 获取当前钱包
     *
     * @return 钱包对象
     */
    public static WalletBean getCurrent() {

        WalletBean ethWallets = WalletDataStore.getInstance().queryWallet(SharedPrefsUitls.getInstance().getCurrentWallet());
        return ethWallets;
    }


    public static String getWordFileName(String languageCode) {
//        String[] LANGS = {"en", "es", "fr", "ja", "zh", "zhTW"};
        String[] LANGS = {"en", "zh"};
        String lc = "words/en-BIP39Words.txt";
        for (String s : LANGS)
            if (s.equalsIgnoreCase(languageCode)) {
                lc = "words/" + languageCode + "-BIP39Words.txt";
            }

        return lc;
    }

}
