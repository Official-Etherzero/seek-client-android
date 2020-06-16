package com.easyetz.sqlite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.easyetz.utils.MyLog;


public class BRSQLiteHelper extends SQLiteOpenHelper {
    private static BRSQLiteHelper instance;

    private BRSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static BRSQLiteHelper getInstance(Context context) {
        if (instance == null) instance = new BRSQLiteHelper(context);
        return instance;
    }

    public static final String DATABASE_NAME = "breadwallet.db";
    private static final int DATABASE_VERSION = 16;//15

    /**
     * MerkleBlock table
     */
    public static final String MB_TABLE_NAME = "merkleBlockTable_v2";
    public static final String MB_COLUMN_ID = "_id";
    public static final String MB_BUFF = "merkleBlockBuff";
    public static final String MB_HEIGHT = "merkleBlockHeight";
    public static final String MB_ISO = "merkleBlockIso";

    private static final String MB_DATABASE_CREATE = "create table if not exists " + MB_TABLE_NAME + " (" +
            MB_COLUMN_ID + " integer primary key autoincrement, " +
            MB_BUFF + " blob, " +
            MB_ISO + " text DEFAULT 'BTC' , " +
            MB_HEIGHT + " integer);";

    /**
     * Transaction table
     */

    public static final String TX_TABLE_NAME = "transactionTable_v2";
    public static final String TX_COLUMN_ID = "_id";
    public static final String TX_BUFF = "transactionBuff";
    public static final String TX_BLOCK_HEIGHT = "transactionBlockHeight";
    public static final String TX_TIME_STAMP = "transactionTimeStamp";
    public static final String TX_ISO = "transactionISO";

    private static final String TX_DATABASE_CREATE = "create table if not exists " + TX_TABLE_NAME + " (" +
            TX_COLUMN_ID + " text, " +
            TX_BUFF + " blob, " +
            TX_BLOCK_HEIGHT + " integer, " +
            TX_TIME_STAMP + " integer, " +
            TX_ISO + " text DEFAULT 'BTC' );";

    /**
     * Peer table
     */

    public static final String PEER_TABLE_NAME = "peerTable_v2";
    public static final String PEER_COLUMN_ID = "_id";
    public static final String PEER_ADDRESS = "peerAddress";
    public static final String PEER_PORT = "peerPort";
    public static final String PEER_TIMESTAMP = "peerTimestamp";
    public static final String PEER_ISO = "peerIso";

    private static final String PEER_DATABASE_CREATE = "create table if not exists " + PEER_TABLE_NAME + " (" +
            PEER_COLUMN_ID + " integer primary key autoincrement, " +
            PEER_ADDRESS + " blob," +
            PEER_PORT + " blob," +
            PEER_TIMESTAMP + " blob," +
            PEER_ISO + "  text default 'BTC');";
    /**
     * Currency table
     */

    public static final String CURRENCY_TABLE_NAME = "currencyTable_v2";
    public static final String CURRENCY_CODE = "code";
    public static final String CURRENCY_NAME = "name";
    public static final String CURRENCY_RATE = "rate";
    public static final String CURRENCY_ISO = "iso";//iso for the currency of exchange (BTC, BCH, ETH)

    private static final String CURRENCY_DATABASE_CREATE = "create table if not exists " + CURRENCY_TABLE_NAME + " (" +
            CURRENCY_CODE + " text," +
            CURRENCY_NAME + " text," +
            CURRENCY_RATE + " integer," +
            CURRENCY_ISO + " text DEFAULT 'BTC', " +
            "PRIMARY KEY (" + CURRENCY_CODE + ", " + CURRENCY_ISO + ")" +
            ");";
    /**
     * contacts table
     */

    public static final String CONTACTS_TABLE_NAME = "contactsTable";
    public static final String CONTACTS_NAME = "name";
    public static final String CONTACTS_WALLRT_ADDRESS = "wallrt_address";
    public static final String CONTACTS_PHONE = "phone";//
    public static final String CONTACTS_REMARKS = "remarks";//

    private static final String CONTACTS_DATABASE_CREATE = "create table if not exists " + CONTACTS_TABLE_NAME + " (" +
            CONTACTS_NAME + " text," +
            CONTACTS_PHONE + " text," +
            CONTACTS_WALLRT_ADDRESS + " text," +
            CONTACTS_REMARKS + " text );";
    /**
     * balance table
     */

    public static final String BALANCE_TABLE_NAME = "balanceTable";
    public static final String BALANCE_ID = "id";//
    public static final String BALANCE_WALLET_ID = "wid";
    public static final String BALANCE_TOKEN_NAME = "token_name";
    public static final String BALANCE_MONEY = "money";//


    private static final String BALANCE_DATABASE_CREATE = "create table if not exists " + BALANCE_TABLE_NAME + " (" +
            BALANCE_ID + " text primary key," +
            BALANCE_WALLET_ID + " text," +
            BALANCE_TOKEN_NAME + " text," +
            BALANCE_MONEY + " text );";
    /**
     * wallets table
     */
    public static final String WALLET_TABLE_NAME = "walletTable";
    public static final String WALLET_ID = "id";
    public static final String WALLET_NAME = "name";
    public static final String WALLET_ADDRESS = "address";
    public static final String WALLET_PASSWORD = "password";
    public static final String WALLET_KEYSTOREPATH = "keystorePath";
    public static final String WALLET_MNEMONIC= "mnemonic";
    public static final String WALLET_STARTCOLOR= "startColor";
    public static final String WALLET_ENDCOLOR= "endColor";
    public static final String WALLET_DECIMALS= "decimals";

    private static final String WALLET_DATABASE_CREATE = "create table if not exists " + WALLET_TABLE_NAME + " (" +
            WALLET_ID + " text primary key," +
            WALLET_NAME + " text," +
            WALLET_ADDRESS + " text," +
            WALLET_PASSWORD + " text," +
            WALLET_KEYSTOREPATH + " text," +
            WALLET_MNEMONIC + " text," +
            WALLET_STARTCOLOR + " text," +
            WALLET_ENDCOLOR + " text," +
            WALLET_DECIMALS + " integer );";



    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(MB_DATABASE_CREATE);
        database.execSQL(TX_DATABASE_CREATE);
        database.execSQL(PEER_DATABASE_CREATE);
        database.execSQL(CURRENCY_DATABASE_CREATE);
        database.execSQL(CONTACTS_DATABASE_CREATE);
        database.execSQL(WALLET_DATABASE_CREATE);
        database.execSQL(BALANCE_DATABASE_CREATE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * 判断表是否存在
     * @param tableName
     * @param db
     * @return
     */
    public boolean tableExists(String tableName, SQLiteDatabase db) {

        Cursor cursor = db.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '" + tableName + "'", null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }

    /**
     * 打印表结构
     * @param db
     * @param tableName
     */
    public void printTableStructures(SQLiteDatabase db, String tableName) {
        MyLog.e( "printTableStructures: " + tableName);
        String tableString = String.format("Table %s:\n", tableName);
        Cursor allRows = db.rawQuery("SELECT * FROM " + tableName, null);
        if (allRows.moveToFirst()) {
            String[] columnNames = allRows.getColumnNames();
            do {
                for (String name : columnNames) {
                    tableString += String.format("%s: %s\n", name,
                            allRows.getString(allRows.getColumnIndex(name)));
                }
                tableString += "\n";

            } while (allRows.moveToNext());
        }

        MyLog.e( "SQL:" + tableString);
    }

}