package com.easyetz.blockchain.walletutils;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.style.TypefaceSpan;

import com.easyetz.base.Constants;
import com.easyetz.bean.WalletBean;
import com.easyetz.blockchain.data.OnWalletLoadedListener;
import com.easyetz.service.BlockchainService;
import com.easyetz.sqlite.WalletDataStore;
import com.easyetz.utils.AppFilePath;
import com.easyetz.utils.ETZKeyStore;
import com.easyetz.utils.Md5Utils;
import com.easyetz.utils.MyLog;
import com.easyetz.utils.SharedPrefsUitls;
import com.easyetz.utils.Util;
import com.google.common.base.Stopwatch;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptException;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.KeyChainGroup;
import org.bitcoinj.wallet.Protos;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.WalletFiles;
import org.bitcoinj.wallet.WalletProtobufSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.utils.Numeric;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


import static com.easyetz.blockchain.walletutils.WalletUtils.getContentValues;

public class BtcWalletUtil {

    private static WalletFiles walletFiles = null;

    public static NetworkParameters getParams() {
//        return MainNetParams.get();
        return TestNet3Params.get();
    }

    public static WalletBean createWallet(NetworkParameters params, DeterministicSeed ds, String wname, String pwd) {
        Wallet btc = Wallet.fromSeed(params, ds, Script.ScriptType.P2PKH);
        MyLog.i("----" + btc.toString());
        String keystorePath = saveWalletFile(btc);//保存钱包文件
        Address address = btc.freshReceiveAddress();
        MyLog.i(address.toString());
        ECKey wkey = null;
        List<ECKey> ecKeys = btc.getIssuedReceiveKeys();
        for (ECKey key : ecKeys) {
            if (address.toString().equalsIgnoreCase(Address.fromKey(params, key, Script.ScriptType.P2PKH).toString())) {
                wkey = key;
            }
            MyLog.i(Address.fromKey(params, key, Script.ScriptType.P2PKH).toString());
            MyLog.i(key.getPrivateKeyAsWiF(params));
        }
        if (ecKeys.size() == 0 || wkey == null) {
            wkey = btc.currentReceiveKey();
        }
        String phrase = "";
        try {
            phrase=  ETZKeyStore.encryptData(WalletUtils.convertMnemonicList(getSeedWordsFromWallet(btc)));
//            MyLog.i("DeterministicSeed====="+phrase);
//            String rrr=ETZKeyStore.decodetData(phrase);
//            MyLog.i("DeterministicSeed====="+rrr);

        } catch (Exception e) {
            e.printStackTrace();
        }
        WalletBean wallet = new WalletBean();
        int wid = SharedPrefsUitls.getInstance().getWalletId("BTC") + 1;//或取当前有几个同类型的钱包
        wallet.setId("BTC-" + wid);
        wallet.setName(wname);
        wallet.setAddress(Address.fromKey(params, wkey, Script.ScriptType.P2PKH).toString());
        wallet.setKeystorePath(keystorePath);
        wallet.setMnemonic(phrase);
        wallet.setStartColor("#f29500");
        wallet.setEndColor("#f29500");
        wallet.setDecimals(8);
        wallet.setPassword(Md5Utils.md5(pwd));
        SharedPrefsUitls.getInstance().putWalletId("BTC", wid);//保存钱包个数
        boolean isetz = WalletDataStore.getInstance().insertWallet(getContentValues(wallet));
        return wallet;
    }

    /**
     * 通过Wallet 获取 助记词
     *
     * @param wallet
     * @return
     */
    public static List<String> getSeedWordsFromWallet(Wallet wallet) {
        DeterministicSeed seed = wallet.getKeyChainSeed();
        return seed.getMnemonicCode();
    }

    /**
     * 通过私钥获取ECKey
     *
     * @param priKey
     * @return
     */
    public static ECKey getECKeyFromPriKey(String priKey) {
        ECKey ecKey = ECKey.fromPrivate(Numeric.toBigInt(priKey));
        return ecKey;
    }

    public static String getPubKeyFrom(ECKey ecKey) {
        NetworkParameters params = getParams();
        return Address.fromKey(params, ecKey, Script.ScriptType.P2PKH).toString();
    }

    /**
     * 助词导入钱包
     *
     * @param mnemonicCode
     * @param password
     * @param wname
     * @return
     */
    public static WalletBean getMnemonicCodeInputWall(List<String> mnemonicCode, String password, String wname) {
        long creationTimeSeconds = System.currentTimeMillis() / 1000;
        DeterministicSeed ds = new DeterministicSeed(mnemonicCode, null, "", creationTimeSeconds);
        return createWallet(getParams(), ds, wname, password);
    }

    /**
     * 私钥导入钱包
     *
     * @param params
     * @param watchKeyB58
     * @return
     */
    public static WalletBean fromWatchingKeyB58(NetworkParameters params, String watchKeyB58, String wname, String pwd) {

        DumpedPrivateKey dpk = DumpedPrivateKey.fromBase58(params, watchKeyB58);
        ECKey ecKey = dpk.getKey();
        long creationTimeSeconds = System.currentTimeMillis() / 1000;
        ecKey.setCreationTimeSeconds(creationTimeSeconds);
        KeyChainGroup group = KeyChainGroup.builder(params).build();
        group.importKeys(ecKey);
//        group.importKeys(ECKey.fromPublicOnly(dpk.getKey().getPubKeyPoint()));
//        DeterministicSeed seed = group.getActiveKeyChain().getSeed();

//        seed.setCreationTimeSeconds(creationTimeSeconds);
        Wallet btc = new Wallet(params, group);
        String keystorePath = saveWalletFile(btc);//保存钱包文件
        int wid = SharedPrefsUitls.getInstance().getWalletId("BTC") + 1;//或取当前有几个同类型的钱包

        MyLog.i("*********" + btc.toString());
        WalletBean walletBean = new WalletBean();
        walletBean.setId("BTC-" + wid);
        walletBean.setName(wname);
        walletBean.setKeystorePath(keystorePath);
//        walletBean.setMnemonic(ecKey.getPrivateKeyAsWiF(params));
        walletBean.setAddress(Address.fromKey(params, ecKey, Script.ScriptType.P2PKH).toString());
        walletBean.setStartColor("#f29500");
        walletBean.setEndColor("#f29500");
        walletBean.setDecimals(8);
        walletBean.setPassword(Md5Utils.md5(pwd));
        MyLog.i(walletBean.toString());
        MyLog.i("*********" + walletBean.toString());
        boolean isetz = WalletDataStore.getInstance().insertWallet(getContentValues(walletBean));
        return walletBean;
    }

    /**
     * 保存钱包文件
     *
     * @param btc
     */
    public static String saveWalletFile(Wallet btc) {
        String wallet_dir = AppFilePath.Wallet_DIR;
        int wid = SharedPrefsUitls.getInstance().getWalletId("BTC") + 1;//或取当前有几个同类型的钱包
        String walletId = "BTC-" + wid;
        String keystorePath = "keystore_" + walletId;
        File btcFile = new File(wallet_dir, keystorePath);
        WalletFiles walletFiles = btc.autosaveToFile(btcFile, 3000,
                TimeUnit.MILLISECONDS, null);
        try {
            walletFiles.saveNow();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return btcFile.getAbsolutePath();
    }

    public String exportPrivateKey(String wid,String pwd){
        WalletBean wb = WalletDataStore.getInstance().queryWallet(wid);
        WalletFiles wf=loadWalletFromFile(wb.getKeystorePath());
        Wallet wallet=wf.getWallet();
        return wallet.currentReceiveKey().getPrivateKeyAsWiF(getParams());
    }


    public static void getWalletAsync(OnWalletLoadedListener listener) {
        String wid = SharedPrefsUitls.getInstance().getCurrentWallet();
        MyLog.i("getWalletAsync=" + wid);
        if (Util.isNullOrEmpty(wid) || !wid.contains("BTC")) return;
        WalletBean wb = WalletDataStore.getInstance().queryWallet(wid);
        org.bitcoinj.core.Context.propagate(new Context(getParams()));
        Wallet wallet = null;
        if (walletFiles == null) {
            wallet=loadWalletFromFile(wb.getKeystorePath()).getWallet();
        } else {
            wallet=walletFiles.getWallet();
        }
        MyLog.i("getWalletAsync=" + wallet.freshReceiveAddress());
        listener.onWalletLoaded(wallet);
//        return loadWalletFromFile(wb.getKeystorePath()).getWallet();
    }

    public static WalletFiles loadWalletFromFile(String path) {

        File walletFile = new File(path);
        Wallet wallet = null;
        if (walletFile.exists()) {
            try (final FileInputStream walletStream = new FileInputStream(walletFile)) {
                final Stopwatch watch = Stopwatch.createStarted();
                wallet = new WalletProtobufSerializer().readWallet(walletStream);
                watch.stop();
            } catch (final IOException | UnreadableWalletException x) {
                MyLog.i(x.toString());
            }
            wallet.cleanup();
            walletFiles = wallet.autosaveToFile(walletFile, 3000,
                    TimeUnit.MILLISECONDS, null);
        }
        return walletFiles;
    }


    private static final Logger log = LoggerFactory.getLogger(WalletUtils.class);

    public static Spanned formatAddress(final Address address, final int groupSize, final int lineSize) {
        return formatHash(address.toString(), groupSize, lineSize);
    }

    public static Spanned formatAddress(final String prefix, final Address address, final int groupSize,
                                        final int lineSize) {
        return formatHash(prefix, address.toString(), groupSize, lineSize, Constants.CHAR_THIN_SPACE);
    }

    public static Spanned formatHash(final String address, final int groupSize, final int lineSize) {
        return formatHash(null, address, groupSize, lineSize, Constants.CHAR_THIN_SPACE);
    }

    public static long longHash(final Sha256Hash hash) {
        final byte[] bytes = hash.getBytes();

        return (bytes[31] & 0xFFl) | ((bytes[30] & 0xFFl) << 8) | ((bytes[29] & 0xFFl) << 16)
                | ((bytes[28] & 0xFFl) << 24) | ((bytes[27] & 0xFFl) << 32) | ((bytes[26] & 0xFFl) << 40)
                | ((bytes[25] & 0xFFl) << 48) | ((bytes[23] & 0xFFl) << 56);
    }

    private static class MonospaceSpan extends TypefaceSpan {
        public MonospaceSpan() {
            super("monospace");
        }

        // TypefaceSpan doesn't implement this, and we need it so that Spanned.equals() works.
        @Override
        public boolean equals(final Object o) {
            if (o == this)
                return true;
            if (o == null || o.getClass() != getClass())
                return false;
            return true;
        }

        @Override
        public int hashCode() {
            return 0;
        }
    }

    public static Spanned formatHash(final String prefix, final String address, final int groupSize,
                                     final int lineSize, final char groupSeparator) {
        final SpannableStringBuilder builder = prefix != null ? new SpannableStringBuilder(prefix)
                : new SpannableStringBuilder();

        final int len = address.length();
        for (int i = 0; i < len; i += groupSize) {
            final int end = i + groupSize;
            final String part = address.substring(i, end < len ? end : len);

            builder.append(part);
            builder.setSpan(new MonospaceSpan(), builder.length() - part.length(), builder.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (end < len) {
                final boolean endOfLine = lineSize > 0 && end % lineSize == 0;
                builder.append(endOfLine ? '\n' : groupSeparator);
            }
        }

        return SpannedString.valueOf(builder);
    }

    public static Address getToAddressOfSent(final Transaction tx, final Wallet wallet) {
        for (final TransactionOutput output : tx.getOutputs()) {
            try {
                if (!output.isMine(wallet)) {
                    final Script script = output.getScriptPubKey();
                    return script.getToAddress(getParams(), true);
                }
            } catch (final ScriptException x) {
                // swallow
            }
        }

        return null;
    }

    public static Address getWalletAddressOfReceived(final Transaction tx, final Wallet wallet) {
        for (final TransactionOutput output : tx.getOutputs()) {
            try {
                if (output.isMine(wallet)) {
                    final Script script = output.getScriptPubKey();
                    return script.getToAddress(getParams(), true);
                }
            } catch (final ScriptException x) {
                // swallow
            }
        }

        return null;
    }

    public static boolean isEntirelySelf(final Transaction tx, final Wallet wallet) {
        for (final TransactionInput input : tx.getInputs()) {
            final TransactionOutput connectedOutput = input.getConnectedOutput();
            if (connectedOutput == null || !connectedOutput.isMine(wallet))
                return false;
        }

        for (final TransactionOutput output : tx.getOutputs()) {
            if (!output.isMine(wallet))
                return false;
        }

        return true;
    }

    public static void autoBackupWallet(final android.content.Context context, final Wallet wallet) {
        final Stopwatch watch = Stopwatch.createStarted();
        final Protos.Wallet.Builder builder = new WalletProtobufSerializer().walletToProto(wallet).toBuilder();

        // strip redundant
        builder.clearTransaction();
        builder.clearLastSeenBlockHash();
        builder.setLastSeenBlockHeight(-1);
        builder.clearLastSeenBlockTimeSecs();
        final Protos.Wallet walletProto = builder.build();

        try (final OutputStream os = context.openFileOutput(Constants.Files.WALLET_KEY_BACKUP_PROTOBUF,
                android.content.Context.MODE_PRIVATE)) {
            walletProto.writeTo(os);
            watch.stop();
            log.info("wallet backed up to: '{}', took {}", Constants.Files.WALLET_KEY_BACKUP_PROTOBUF, watch);
        } catch (final IOException x) {
            log.error("problem writing wallet backup", x);
        }
    }

    public static Wallet restoreWalletFromAutoBackup(final android.content.Context context) {
        try (final InputStream is = context.openFileInput(Constants.Files.WALLET_KEY_BACKUP_PROTOBUF)) {
            final Wallet wallet = new WalletProtobufSerializer().readWallet(is, true, null);
            if (!wallet.isConsistent())
                throw new Error("inconsistent backup");

            BlockchainService.resetBlockchain(context);
            log.info("wallet restored from backup: '" + Constants.Files.WALLET_KEY_BACKUP_PROTOBUF + "'");
            return wallet;
        } catch (final IOException | UnreadableWalletException x) {
            throw new Error("cannot read backup", x);
        }
    }

    public static Wallet restoreWalletFromProtobuf(final InputStream is,
                                                   final NetworkParameters expectedNetworkParameters) throws IOException {
        try {
            final Wallet wallet = new WalletProtobufSerializer().readWallet(is, true, null);

            if (!wallet.getParams().equals(expectedNetworkParameters))
                throw new IOException("bad wallet backup network parameters: " + wallet.getParams().getId());
            if (!wallet.isConsistent())
                throw new IOException("inconsistent wallet backup");

            return wallet;
        } catch (final UnreadableWalletException x) {
            throw new IOException("unreadable wallet", x);
        }
    }

    public static final FileFilter BACKUP_FILE_FILTER = new FileFilter() {
        @Override
        public boolean accept(final File file) {
            try (final InputStream is = new FileInputStream(file)) {
                return WalletProtobufSerializer.isWallet(is);
            } catch (final IOException x) {
                return false;
            }
        }
    };

    public static boolean isPayToManyTransaction(final Transaction transaction) {
        return transaction.getOutputs().size() > 20;
    }

}
