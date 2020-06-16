package com.easyetz.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.easyetz.app.MyApp;
import com.easyetz.base.Constants;
import com.easyetz.bean.WalletBean;
import com.easyetz.utils.ETZKeyStore;
import com.easyetz.utils.MyLog;

import java.util.ArrayList;
import java.util.List;

public class WalletDataStore implements BRDataSourceInterface {

    private SQLiteDatabase database;
    private final BRSQLiteHelper dbHelper;
    private static WalletDataStore instance;
    public static final String[] allColumns = {
            BRSQLiteHelper.WALLET_ID,
            BRSQLiteHelper.WALLET_NAME,
            BRSQLiteHelper.WALLET_ADDRESS,
            BRSQLiteHelper.WALLET_PASSWORD,
            BRSQLiteHelper.WALLET_KEYSTOREPATH,
            BRSQLiteHelper.WALLET_MNEMONIC,
            BRSQLiteHelper.WALLET_STARTCOLOR,
            BRSQLiteHelper.WALLET_ENDCOLOR,
            BRSQLiteHelper.WALLET_DECIMALS,
    };

    private WalletDataStore(Context context) {
        dbHelper = BRSQLiteHelper.getInstance(context);
    }

    public static WalletDataStore getInstance() {
        if (instance == null) {
            instance = new WalletDataStore(MyApp.getmInstance());
        }
        return instance;
    }

    /**
     * 增加钱包
     *
     * @param values
     * @return
     */
    public boolean insertWallet(ContentValues values) {
        database = openDatabase();
        Long sum = database.insert(BRSQLiteHelper.WALLET_TABLE_NAME, null, values);
        MyLog.i("database.insert" + sum);
        closeDatabase();
        if (sum > 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 获取钱包列表
     *
     * @return
     */
    public List<WalletBean> queryAllWallets() {
        List<WalletBean> cList = new ArrayList<>();
        Cursor cursor = null;
        try {
            database = openDatabase();
            cursor = database.query(BRSQLiteHelper.WALLET_TABLE_NAME,
                    allColumns, null, null, null, null, null);

            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                WalletBean entity = new WalletBean(cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getString(7),
                        cursor.getInt(8));
                cList.add(entity);
                cursor.moveToNext();
            }

        } finally {
            closeDatabase();
            if (cursor != null)
                cursor.close();
        }

        return cList;

    }

    public WalletBean queryWallet(String wid) {
        Cursor cursor = null;
        WalletBean walletBean=null;
        database = openDatabase();
        cursor = database.query(BRSQLiteHelper.WALLET_TABLE_NAME,allColumns,BRSQLiteHelper.WALLET_ID + "=?", new String[]{wid},null,null,null);
        cursor.moveToFirst();
        if (!cursor.isAfterLast()){
            walletBean=new WalletBean(cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getString(6),
                    cursor.getString(7),
                    cursor.getInt(8));
        }
        return walletBean;
    }

    /**
     * 删除钱包
     *
     * @param walletId
     * @return
     */
    public boolean deleteWallet(String walletId) {
        try {
            database = openDatabase();
            int num = database.delete(BRSQLiteHelper.WALLET_TABLE_NAME, BRSQLiteHelper.WALLET_ID + "=?", new String[]{walletId});
            MyLog.i("database.delete" + num);
            if (num > 0) {
                return true;
            }
        } finally {
            closeDatabase();
        }
        return false;

    }

    /**
     * 更新钱包
     *
     * @param value
     * @param walletId
     * @return
     */
    public boolean updataWallet(ContentValues value, String walletId) {
        try {
            database = openDatabase();
            int r = database.update(BRSQLiteHelper.WALLET_TABLE_NAME, value, BRSQLiteHelper.WALLET_ID + "=?", new String[]{walletId});
            MyLog.i("database.update" + r);
            if (r > 0)
                return true;
        } finally {
            closeDatabase();
        }
        return false;
    }

    /**
     * 助记词判断钱包是否存在
     * @param walletType
     * @param mnemonic
     * @return
     */
    public boolean mnemonicQueryWallet(String walletType,String mnemonic){


        List<WalletBean> beanList=queryAllWallets();
        for (WalletBean bean:beanList) {
            String mne=ETZKeyStore.decodetData(bean.getMnemonic());
            if (mne.equals(mnemonic)&&bean.getId().startsWith(walletType))return true;
        }
//        Cursor cursor = null;
//        database = openDatabase();
//        List<String> list=new ArrayList<>();
//        cursor = database.query(BRSQLiteHelper.WALLET_TABLE_NAME,allColumns,BRSQLiteHelper.WALLET_MNEMONIC + "=?", new String[]{mnemonic},null,null,null);
//        cursor.moveToFirst();
//        while (!cursor.isAfterLast()){
//            list.add(cursor.getString(0));
//            cursor.moveToNext();
//        }
//        for (String str:list ) {
//           if( str.startsWith(walletType)) return true;
//        }
        return false;
    }
    /**
     * address判断钱包是否存在
     * @param walletType
     * @param address
     * @return
     */
    public boolean addressQueryWallet(String walletType,String address){
        Cursor cursor = null;
        database = openDatabase();
        List<String> list=new ArrayList<>();
        cursor = database.query(BRSQLiteHelper.WALLET_TABLE_NAME,allColumns,BRSQLiteHelper.WALLET_ADDRESS + "=?", new String[]{address},null,null,null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            list.add(cursor.getString(0));
            cursor.moveToNext();
        }
        for (String str:list ) {
           if( str.startsWith(walletType)) return true;
        }
        return false;
    }

    /**
     * walletName是否存在
     * @param name
     * @return
     */
    public boolean QueryWalletName(String name){
        Cursor cursor = null;
        database = openDatabase();
        List<String> list=new ArrayList<>();
        cursor = database.query(BRSQLiteHelper.WALLET_TABLE_NAME,allColumns,BRSQLiteHelper.WALLET_NAME + "=?", new String[]{name},null,null,null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            list.add(cursor.getString(0));
            cursor.moveToNext();
        }
        if (list.size()>0)return true;

        return false;
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
        database.close();
    }
}
