package com.easyetz.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class TransactionRecords implements Parcelable {
    public final String from;
    public final String to;
    public final String value;
    public final String date;
    public final String gasLimt;
    public final String gasPrice;
    public final String blockNumber;
    public final String tid;
    public final String status;
    public boolean isReceived;

    public TransactionRecords(String from, String to, String value,String date,String gasLimt,String gasPrice,String blockNumber,String tid,String status,boolean isReceived) {
        this.from = from;
        this.to = to;
        this.value = value;
        this.date=date;
        this.gasLimt=gasLimt;
        this.gasPrice=gasPrice;
        this.blockNumber=blockNumber;
        this.tid=tid;
        this.status=status;
        this.isReceived=isReceived;
    }

    private TransactionRecords(Parcel in) {
        from = in.readString();
        to = in.readString();
        value = in.readString();
        date=in.readString();
        gasLimt=in.readString();
        gasPrice=in.readString();
        blockNumber=in.readString();
        tid=in.readString();
        status=in.readString();
    }

    public static final Creator<TransactionRecords> CREATOR = new Creator<TransactionRecords>() {
        @Override
        public TransactionRecords createFromParcel(Parcel in) {
            return new TransactionRecords(in);
        }

        @Override
        public TransactionRecords[] newArray(int size) {
            return new TransactionRecords[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(from);
        dest.writeString(to);
        dest.writeString(value);
        dest.writeString(date);
        dest.writeString(gasLimt);
        dest.writeString(gasPrice);
        dest.writeString(blockNumber);
        dest.writeString(tid);
        dest.writeString(status);
    }
}
