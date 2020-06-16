package com.easyetz.bean;

public class NetworkInfo {
    public final String rpcServerUrl;
    public final int chainId;

    public NetworkInfo( String rpcServerUrl) {
        this.rpcServerUrl = rpcServerUrl;
        this.chainId = 1;
    }
}
