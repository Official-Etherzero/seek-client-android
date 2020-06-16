package com.easyetz.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.easyetz.utils.Util;

public class WalletBean implements Parcelable{

    private String id;
    private String name;
    public String address;
    private String password;
    private String keystorePath;
    private String mnemonic;
    private String mStartColor;
    private String mEndColor;
    private int decimals;

    public WalletBean(String id,  String name,String address, String password,
                      String keystorePath, String mnemonic,String mStartColor,String mEndColor,int decimals) {
        this.id = id;
        this.address = address;
        this.name = name;
        this.password = password;
        this.keystorePath = keystorePath;
        this.mnemonic = mnemonic;
        this.mStartColor = mStartColor;
        this.mEndColor = mEndColor;
        this.decimals = decimals;
    }

    public WalletBean() {
    }


    protected WalletBean(Parcel in) {
        id = in.readString();
        name = in.readString();
        address = in.readString();
        password = in.readString();
        keystorePath = in.readString();
        mnemonic = in.readString();
        mStartColor = in.readString();
        mEndColor = in.readString();
        decimals = in.readInt();
    }

    public static final Creator<WalletBean> CREATOR = new Creator<WalletBean>() {
        @Override
        public WalletBean createFromParcel(Parcel in) {
            return new WalletBean(in);
        }

        @Override
        public WalletBean[] newArray(int size) {
            return new WalletBean[size];
        }
    };

    public String getStartColor() {
        return mStartColor;
    }

    public void setStartColor(String mStartColor) {
        this.mStartColor = mStartColor;
    }

    public String getEndColor() {
        return mEndColor;
    }

    public void setEndColor(String mEndColor) {
        this.mEndColor = mEndColor;
    }

    public int getDecimals() {
        return decimals;
    }

    public void setDecimals(int decimals) {
        this.decimals = decimals;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getKeystorePath() {
        return keystorePath;
    }

    public void setKeystorePath(String keystorePath) {
        this.keystorePath = keystorePath;
    }

    public String getMnemonic() {
        if (Util.isNullOrEmpty(mnemonic))return "";
        return mnemonic;
    }

    public void setMnemonic(String mnemonic) {
        this.mnemonic = mnemonic;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "ETHWallet{" +
                "id=" + id +
                ", address='" + address + '\'' +
                ", name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", keystorePath='" + keystorePath + '\'' +
                ", mnemonic='" + mnemonic + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(address);
        dest.writeString(password);
        dest.writeString(keystorePath);
        dest.writeString(mnemonic);
        dest.writeString(mStartColor);
        dest.writeString(mEndColor);
        dest.writeInt(decimals);
    }
}
