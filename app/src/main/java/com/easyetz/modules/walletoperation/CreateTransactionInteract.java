package com.easyetz.modules.walletoperation;

import com.easyetz.bean.WalletBean;
import com.easyetz.utils.MyLog;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.Sign;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.rlp.RlpEncoder;
import org.web3j.rlp.RlpList;
import org.web3j.rlp.RlpString;
import org.web3j.rlp.RlpType;
import org.web3j.utils.Bytes;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class CreateTransactionInteract {
    private final EthereumNetworkRepository networkRepository;


    public CreateTransactionInteract(
            EthereumNetworkRepository networkRepository) {
        this.networkRepository = networkRepository;

    }

    public Single<byte[]> sign(WalletBean wallet, byte[] message, String pwd) {
        return getSignature(wallet, message, pwd);
    }

    /**
     * 创建ETH交易
     * @param from
     * @param to
     * @param amount
     * @param gasPrice
     * @param gasLimit
     * @param password
     * @return
     */
    public Single<String> createEthTransaction(WalletBean from, String to, BigInteger amount, BigInteger gasPrice, BigInteger gasLimit, String password) {
        final Web3j web3j = Web3j.build(new HttpService(networkRepository.getDefaultNetwork().rpcServerUrl));

        return networkRepository.getLastTransactionNonce(web3j, from.address)
                .flatMap(nonce -> Single.fromCallable(() -> {

                    Credentials credentials = WalletUtils.loadCredentials(password, from.getKeystorePath());
                    RawTransaction rawTransaction = RawTransaction.createEtherTransaction(nonce, gasPrice, gasLimit, to, amount);
                    byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);

                    String hexValue = Numeric.toHexString(signedMessage);
                    EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).send();

                    return ethSendTransaction.getTransactionHash();

                }).subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread()));
    }

    /**
     * 创建代币交易
     * @param from
     * @param to
     * @param contractAddress
     * @param amount
     * @param gasPrice
     * @param gasLimit
     * @param password
     * @return
     */
    public Single<String> createERC20Transfer(WalletBean from, String to, String contractAddress, BigInteger amount, BigInteger gasPrice, BigInteger gasLimit, String password,String data) {
        final Web3j web3j = Web3j.build(new HttpService(networkRepository.getDefaultNetwork().rpcServerUrl));

//        String callFuncData = createTokenTransferData(to, amount);

        return networkRepository.getLastTransactionNonce(web3j, from.address)
                .flatMap(nonce -> getRawTransaction(nonce, gasPrice, gasLimit,data,contractAddress))
                .flatMap(rawTx -> signEncodeRawTransaction(rawTx, password, from, networkRepository.getDefaultNetwork().chainId))
                .flatMap(signedMessage -> Single.fromCallable(() -> {
                    MyLog.i("getRawTransaction--signedMessage="+Numeric.toHexString(signedMessage));
                    EthSendTransaction raw = web3j
                            .ethSendRawTransaction(Numeric.toHexString(signedMessage))
                            .send();
                    if (raw.hasError()) {
                        throw new Exception(raw.getError().getMessage());
                    }
                    MyLog.i("getRawTransaction--signedMessage="+raw.getTransactionHash());
                    return raw.getTransactionHash();
                })).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());

//        return networkRepository.getLastTransactionNonce(web3j, from.address)
//                .flatMap(nonce -> Single.fromCallable(() -> {
//
//                    Credentials credentials = WalletUtils.loadCredentials(password, from.getKeystorePath());
//                    RawTransaction rawTransaction = RawTransaction.createTransaction(
//                            nonce, gasPrice, gasLimit, contractAddress, callFuncData);
//
//                    MyLog.d("rawTransaction:" + rawTransaction);
//
//                    byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
//
//                    String hexValue = Numeric.toHexString(signedMessage);
//                    EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).send();
//
//                    return ethSendTransaction.getTransactionHash();
//
//                }).subscribeOn(Schedulers.computation())
//                        .observeOn(AndroidSchedulers.mainThread()));
    }

    public Single<String> create(WalletBean from, String to, BigInteger subunitAmount, BigInteger gasPrice, BigInteger gasLimit, String data, String pwd) {
        return createTransaction(from, to, subunitAmount, gasPrice, gasLimit, data, pwd)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread());
    }


    public Single<String> createContract(WalletBean from, BigInteger gasPrice, BigInteger gasLimit, String data, String pwd) {
        return createTransaction(from, gasPrice, gasLimit, data, pwd)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread());
    }


    // https://github.com/web3j/web3j/issues/208
    // https://ethereum.stackexchange.com/questions/17708/solidity-ecrecover-and-web3j-sign-signmessage-are-not-compatible-is-it

    // message : TransactionEncoder.encode(rtx)   // may wrong

    public Single<byte[]> getSignature(WalletBean wallet, byte[] message, String password) {
        return Single.fromCallable(() -> {
            Credentials credentials = WalletUtils.loadCredentials(password, wallet.getKeystorePath());
            Sign.SignatureData signatureData = Sign.signMessage(
                    message, credentials.getEcKeyPair());

            List<RlpType> result = new ArrayList<>();
            result.add(RlpString.create(message));

            if (signatureData != null) {
                result.add(RlpString.create(signatureData.getV()));
                result.add(RlpString.create(Bytes.trimLeadingZeroes(signatureData.getR())));
                result.add(RlpString.create(Bytes.trimLeadingZeroes(signatureData.getS())));
            }

            RlpList rlpList = new RlpList(result);
            return RlpEncoder.encode(rlpList);
        });
    }

    /***
     * 创建ETZ交易
     * @param from
     * @param toAddress
     * @param subunitAmount
     * @param gasPrice
     * @param gasLimit
     * @param data
     * @param password
     * @return
     */
    public Single<String> createTransaction(WalletBean from, String toAddress, BigInteger subunitAmount, BigInteger gasPrice, BigInteger gasLimit, String data, String password) {
        final Web3j web3j = Web3j.build(new HttpService(networkRepository.getDefaultNetwork().rpcServerUrl));
        return networkRepository.getLastTransactionNonce(web3j, from.address)
                .flatMap(nonce -> getRawTransaction(nonce, gasPrice, gasLimit, toAddress, subunitAmount, data))
                .flatMap(rawTx -> signEncodeRawTransaction(rawTx, password, from, networkRepository.getDefaultNetwork().chainId))
                .flatMap(signedMessage -> Single.fromCallable(() -> {
                    MyLog.i("getRawTransaction--signedMessage="+Numeric.toHexString(signedMessage));
                    EthSendTransaction raw = web3j
                            .ethSendRawTransaction(Numeric.toHexString(signedMessage))
                            .send();
                    if (raw.hasError()) {
                        throw new Exception(raw.getError().getMessage());
                    }
                    return raw.getTransactionHash();
                })).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }


    /**
     * DAPP交易
     * @param from
     * @param gasPrice
     * @param gasLimit
     * @param data
     * @param password
     * @return
     */
    public Single<String> createTransaction(WalletBean from, BigInteger gasPrice, BigInteger gasLimit, String data, String password) {

        final Web3j web3j = Web3j.build(new HttpService(networkRepository.getDefaultNetwork().rpcServerUrl));

        return networkRepository.getLastTransactionNonce(web3j, from.address)
                .flatMap(nonce -> getRawTransaction(nonce, gasPrice, gasLimit, BigInteger.ZERO, data))
                .flatMap(rawTx -> signEncodeRawTransaction(rawTx, password, from, networkRepository.getDefaultNetwork().chainId))
                .flatMap(signedMessage -> Single.fromCallable(() -> {
                    MyLog.i("getRawTransaction--signedMessage="+signedMessage);
                    EthSendTransaction raw = web3j
                            .ethSendRawTransaction(Numeric.toHexString(signedMessage))
                            .send();
                    if (raw.hasError()) {
                        throw new Exception(raw.getError().getMessage());
                    }
                    return raw.getTransactionHash();
                })).subscribeOn(Schedulers.io());

    }



    // for DApp  create contract  transaction
    private Single<RawTransaction> getRawTransaction(BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit, BigInteger value, String data) {
        return Single.fromCallable(() ->
                RawTransaction.createContractTransaction(
                        nonce,
                        gasPrice,
                        gasLimit,
                        value,
                        data));
    }

    private Single<RawTransaction> getRawTransaction(BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit, String to, BigInteger value, String data) {
        return Single.fromCallable(() ->
                RawTransaction.createTransaction(
                        nonce,
                        gasPrice,
                        gasLimit,
                        to,
                        value,
                        data));
    }
    private Single<RawTransaction> getRawTransaction(BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit, String data,String contractAddress) {
        return Single.fromCallable(() ->
                RawTransaction.createTransaction(
                        nonce,
                        gasPrice,
                        gasLimit,
                        contractAddress,
                        data));
    }

    private Single<byte[]> signEncodeRawTransaction(RawTransaction rtx, String password, WalletBean wallet, int chainId) {

        return Single.fromCallable(() -> {
            Credentials credentials = WalletUtils.loadCredentials(password, wallet.getKeystorePath());
            byte[] signedMessage = TransactionEncoder.signMessage(rtx,(byte) chainId, credentials);
//            byte[] signedMessage = TransactionEncoder.signMessage(rtx, credentials);
            return signedMessage;
        });
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
