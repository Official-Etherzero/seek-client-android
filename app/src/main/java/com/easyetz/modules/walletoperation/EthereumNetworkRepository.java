package com.easyetz.modules.walletoperation;


import com.easyetz.base.BaseUrl;
import com.easyetz.bean.NetworkInfo;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;

import java.math.BigInteger;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;


public class EthereumNetworkRepository {


    private NetworkInfo defaultNetwork= new NetworkInfo(BaseUrl.getEthereumRpcUrl());

    public NetworkInfo getDefaultNetwork() {
        return defaultNetwork;
    }

    public Single<NetworkInfo> find() {
        return Single.just(getDefaultNetwork())
                .observeOn(AndroidSchedulers.mainThread());
    }
    public Single<BigInteger> getLastTransactionNonce(Web3j web3j, String walletAddress)
    {
        return Single.fromCallable(() -> {
            EthGetTransactionCount ethGetTransactionCount = web3j
                    .ethGetTransactionCount(walletAddress, DefaultBlockParameterName.PENDING)   // or DefaultBlockParameterName.LATEST
                    .send();
            return ethGetTransactionCount.getTransactionCount();
        });
    }

}
