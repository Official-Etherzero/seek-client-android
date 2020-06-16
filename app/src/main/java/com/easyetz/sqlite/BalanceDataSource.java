package com.easyetz.sqlite;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.easyetz.app.MyApp;
import com.easyetz.base.Constants;
import com.easyetz.bean.BalanceEntity;
import com.easyetz.bean.CurrencyEntity;
import com.easyetz.utils.MyLog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 余额列表
 */
public class BalanceDataSource implements BRDataSourceInterface {
    private static final String TAG = BalanceDataSource.class.getName();

    // Database fields
    private SQLiteDatabase database;
    private final BRSQLiteHelper dbHelper;
    private final String[] allColumns = {
            BRSQLiteHelper.BALANCE_ID,
            BRSQLiteHelper.BALANCE_WALLET_ID,
            BRSQLiteHelper.BALANCE_TOKEN_NAME,
            BRSQLiteHelper.BALANCE_MONEY,
    };

    private static BalanceDataSource instance;

    public static BalanceDataSource getInstance() {
        if (instance == null) {
            instance = new BalanceDataSource(MyApp.getmInstance());
        }
        return instance;
    }

    public BalanceDataSource(Context context) {
        dbHelper = BRSQLiteHelper.getInstance(context);
    }

    /**
     * 存入数据库
     *
     * @param be
     */
    public void insertTokenBalance(BalanceEntity be) {
        if (be == null) {
            MyLog.e("putCurrencies: failed: " + be);
            return;
        }

        try {
            database = openDatabase();
            database.beginTransaction();
            ContentValues values = new ContentValues();
            values.put(BRSQLiteHelper.BALANCE_ID, be.bid);
            values.put(BRSQLiteHelper.BALANCE_WALLET_ID, be.wid);
            values.put(BRSQLiteHelper.BALANCE_TOKEN_NAME, be.iso);
            values.put(BRSQLiteHelper.BALANCE_MONEY, be.money);
            MyLog.i("putCurrencies:bid=" + be.bid + ";wid=" + be.wid + ";name=" + be.iso + ";money=" + be.money);
            database.insertWithOnConflict(BRSQLiteHelper.BALANCE_TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
            database.setTransactionSuccessful();
        } catch (Exception ex) {
        } finally {
            database.endTransaction();
            closeDatabase();
        }
    }


    /**
     * 获取当前所有钱包对应余额
     *
     * @param wid
     * @return
     */
    public Map<String,String> getWalletTokensBalance(String wid) {

        Map<String,String> balances = new HashMap<>();
        Cursor cursor = null;
        try {
            database = openDatabase();

            cursor = database.query(BRSQLiteHelper.BALANCE_TABLE_NAME, allColumns, BRSQLiteHelper.BALANCE_WALLET_ID + " = ? COLLATE NOCASE",
                    new String[]{wid}, null, null, null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
//                BalanceEntity be = cursorToCurrency(cursor);
                balances.put(cursor.getString(2),cursor.getString(3));
                cursor.moveToNext();
            }
            // make sure to close the cursor
        } finally {
            if (cursor != null)
                cursor.close();
            closeDatabase();
        }
        MyLog.e("getAllCurrencies: size:" + balances.size());
        return balances;
    }

    /**
     * 获取一个Token余额
     *
     * @param iso
     * @param wid
     * @return
     */
    public synchronized BalanceEntity getTokenBalance(String iso, String wid) {
        Cursor cursor = null;
        BalanceEntity result = null;
        try {
            database = openDatabase();
            cursor = database.query(BRSQLiteHelper.BALANCE_TABLE_NAME,
                    allColumns, BRSQLiteHelper.BALANCE_WALLET_ID + " = ? AND " + BRSQLiteHelper.BALANCE_TOKEN_NAME + " = ? COLLATE NOCASE",
                    new String[]{wid, iso}, null, null, null);

            cursor.moveToNext();
            if (!cursor.isAfterLast()) {
                result = cursorToCurrency(cursor);
            }

            return result;
        } finally {
            if (cursor != null)
                cursor.close();
            closeDatabase();
        }
    }

    private BalanceEntity cursorToCurrency(Cursor cursor) {
        return new BalanceEntity(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3));
    }

    @Override
    public SQLiteDatabase openDatabase() {
        if (database == null || !database.isOpen())
            database = dbHelper.getWritableDatabase();
        dbHelper.setWriteAheadLoggingEnabled(Constants.WRITE_AHEAD_LOGGING);
        return database;
    }

    @Override
    public void closeDatabase() {
    }

}