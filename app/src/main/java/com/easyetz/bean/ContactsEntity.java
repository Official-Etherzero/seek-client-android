package com.easyetz.bean;


import com.easyetz.utils.Util;

public class ContactsEntity {
    private String cname;
    private String walletAddress;
    private String cphone;
    private String remarks;

    public String getCname() {
        if (!Util.isNullOrEmpty(cname)) {
            return cname;
        } else {
            return "";
        }

    }

    public String getCphone() {
        if (!Util.isNullOrEmpty(cphone)) {
            return cphone;
        } else {
            return "";
        }

    }

    public String getRemarks() {
        if (!Util.isNullOrEmpty(remarks)) {
            return remarks;
        } else {
            return "";
        }

    }

    public String getWalletAddress() {
        if (!Util.isNullOrEmpty(walletAddress)) {
            return walletAddress;
        } else {
            return "";
        }

    }

    public ContactsEntity(String cname, String walletAddress, String cphone, String remarks) {
        this.cname = cname;
        this.walletAddress = walletAddress;
        this.cphone = cphone;
        this.remarks = remarks;
    }
}
