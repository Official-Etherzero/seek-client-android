package com.easyetz.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class TokenInfo implements Parcelable {
    public final String address;
    public final String name;
    public final String symbol;
    public final String image; // may need to change to int if logos become resources
    public final String mStartColor;
    public final String mEndColor;
    public final int decimals;

    public TokenInfo(String address, String name, String symbol,String image,String mStartColor,String mEndColor,int decimals) {
        this.address = address;
        this.name = name;
        this.symbol = symbol;
        this.image=image;
        this.mStartColor=mStartColor;
        this.mEndColor=mEndColor;
        this.decimals = decimals;
    }

    private TokenInfo(Parcel in) {
        address = in.readString();
        name = in.readString();
        symbol = in.readString();
        image=in.readString();
        mStartColor=in.readString();
        mEndColor=in.readString();
        decimals = in.readInt();
    }

    public static final Creator<TokenInfo> CREATOR = new Creator<TokenInfo>() {
        @Override
        public TokenInfo createFromParcel(Parcel in) {
            return new TokenInfo(in);
        }

        @Override
        public TokenInfo[] newArray(int size) {
            return new TokenInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(address);
        dest.writeString(name);
        dest.writeString(symbol);
        dest.writeString(image);
        dest.writeString(mStartColor);
        dest.writeString(mEndColor);
        dest.writeInt(decimals);
    }
}
