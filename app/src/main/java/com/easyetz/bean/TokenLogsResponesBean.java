package com.easyetz.bean;

import java.io.Serializable;

public class TokenLogsResponesBean implements Serializable {
    private String txHash;
    private String blockNumber;
    private String timestamp;
    private String gasUsed;
    private String gasPrice;
    private String data;
    private String status;
    private String[] topics;

    public String getTimestamp() {
        return timestamp;
    }

    public String getGasUsed() {
        return gasUsed;
    }

    public String getGasPrice() {
        return gasPrice;
    }

    public String getBlockNumber() {
        return blockNumber;
    }

    public String getData() {
        return data;
    }

    public String getTxHash() {
        return txHash;
    }

    public String[] getTopics() {
        return topics;
    }

    public String getStatus() {
        return status;
    }

    public String getFrom(){
        return convertToAddress(topics[1]);
    }
    public String getTo(){
        return convertToAddress(topics[2]);
    }
    private String convertToAddress(String str){
        String address="";
        address=str.substring(str.length()-40,str.length());
        return "0x"+address;
    }
}
