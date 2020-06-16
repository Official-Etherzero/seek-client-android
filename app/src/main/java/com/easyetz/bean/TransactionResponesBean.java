package com.easyetz.bean;

import com.easyetz.utils.Util;

import java.io.Serializable;

public class TransactionResponesBean implements Serializable {
    private String blockHash;
    private String blockNumber;
    private String from;
    private String gasUsed;
    private String gasPrice;
    private String to;
    private String value;
    private String timestamp;
    private String timeStamp;
    private String status;
    private String hash;

    public String getBlockHash() {
        return blockHash;
    }

    public String getFrom() {
        return from;
    }

    public String getBlockNumber() {
        return blockNumber;
    }

    public String getGasPrice() {
        return gasPrice;
    }

    public String getGasUsed() {
        return gasUsed;
    }

    public String getTimestamp() {
        if (Util.isNullOrEmpty(timestamp))return timeStamp;
        return timestamp;
    }

    public String getTo() {
        return to;
    }

    public String getValue() {
        return value;
    }

    public String getStatus() {
        return status;
    }

    public String getHash() {
        return hash;
    }
}
