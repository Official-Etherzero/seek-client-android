/**
 * BreadWallet
 * <p/>
 * Created by Jade Byfield <jade@breadwallet.com> on 9/13/2018.
 * Copyright (c) 2018 breadwallet LLC
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.easyetz.utils;

import android.content.Context;
import android.util.Log;

import com.easyetz.R;
import com.easyetz.app.MyApp;
import com.easyetz.base.BaseUrl;
import com.easyetz.bean.TokenInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public final class TokenUtil {

    private static final String TAG = TokenUtil.class.getSimpleName();

    private static final String ENDPOINT_CURRENCIES_SALE_ADDRESS = "/currencies?saleAddress=";
    private static final String FIELD_CODE = "symbol";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_SCALE = "decimals";
    private static final String FIELD_CONTRACT_ADDRESS = "address";
    private static final String FIELD_IMAG = "logoUrl";
    private static final String FIELD_START_COLOR = "colorLeft";
    private static final String FIELD_END_COLOR = "colorRight";
    private static final String FIELD_CONTRACT_INITIAL_VALUE = "contract_initial_value";
    private static final String TOKENS_FILENAME = "tokens.json";
    private static String tJson = "";

    // TODO: In DROID-878 fix this so we don't have to store this mTokenItems... (Should be stored in appropriate wallet.)
    private static ArrayList<TokenInfo> mTokenItems;
    private static ArrayList<TokenInfo> ethTokens;

    private TokenUtil() {
    }

    /**
     * When the app first starts, fetch our local copy of tokens.json from the resource folder
     *
     * @param context The Context of the caller
     */
    public static void initialize(Context context) {
        String filePath = context.getFilesDir().getAbsolutePath() + File.separator + TOKENS_FILENAME;
        File tokensFile = new File(filePath);

        if (!tokensFile.exists()) {
            MyLog.i("++++++++++++++++++++++未获取");
            InputStream tokensInputStream = context.getResources().openRawResource(R.raw.tokens);
            BufferedReader bufferedReader = null;
            try {
                bufferedReader = new BufferedReader(new InputStreamReader(tokensInputStream, "UTF-8"));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append('\n');
                }
                bufferedReader.close();
                tokensInputStream.close();
                mTokenItems = parseJsonToTokenList(context, stringBuilder.toString());

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                Log.e(TAG, "Could not read from resource file at res/raw/tokens.json ", e);
            }

        }
    }

    /**
     * This method fetches a specific token by saleAddress
     *
     * @param iso Optional sale address value if we are looking for a specific token response.
     */
    public static TokenInfo getTokenItem(String iso) {
        if (mTokenItems != null && mTokenItems.size() > 0) {
            for (TokenInfo item : mTokenItems) {
                if (item.symbol.equalsIgnoreCase(iso)) {
                    return item;
                }
            }
        }
        if (ethTokens!= null && ethTokens.size() > 0) {
            for (TokenInfo item : ethTokens) {
                if (item.symbol.equalsIgnoreCase(iso)) {
                    return item;
                }
            }
        }
        return null;
    }

    public static synchronized ArrayList<TokenInfo> getEthTokens(Context context) {
        if (ethTokens == null) {
            ethTokens = getEthTokensFromFile(context);
        }
        return ethTokens;
    }
    public static synchronized ArrayList<TokenInfo> getTokenItems(Context context) {
        if (mTokenItems == null) {
            mTokenItems = getTokensFromFile(context);
        }
        return mTokenItems;
    }


    private static ArrayList<TokenInfo> parseJsonToTokenList(Context context, String jsonString) {
        ArrayList<TokenInfo> tokenItems = new ArrayList<>();

        // Iterate over the token list and announce each token to Core
        try {
            JSONObject boyJson = new JSONObject(jsonString);
            JSONArray tokenListArray = boyJson.optJSONArray("result");

            for (int i = 0; i < tokenListArray.length(); i++) {
                JSONObject tokenObject = tokenListArray.getJSONObject(i);
                String address = "";
                String name = "";
                String symbol = "";
                String contractInitialValue = "";
                String image = "";
                String mStartColor = "";
                String mEndColor = "";
                int decimals = 0;

                if (tokenObject.has(FIELD_CONTRACT_ADDRESS)) {
                    address = tokenObject.optString(FIELD_CONTRACT_ADDRESS);
                }

                if (tokenObject.has(FIELD_NAME)) {
                    name = tokenObject.optString(FIELD_NAME);
                }

                if (tokenObject.has(FIELD_CODE)) {
                    symbol = tokenObject.optString(FIELD_CODE);
                }

                if (tokenObject.has(FIELD_SCALE)) {
                    decimals = tokenObject.optInt(FIELD_SCALE);
                }

                if (tokenObject.has(FIELD_CONTRACT_INITIAL_VALUE)) {
                    contractInitialValue = tokenObject.getString(FIELD_CONTRACT_INITIAL_VALUE);
                }

                if (!Util.isNullOrEmpty(address) && !Util.isNullOrEmpty(name) && !Util.isNullOrEmpty(symbol)) {
                    // Keep a local reference to the token list, so that we can make token symbols to their
                    // gradient colors in WalletListAdapter
                    if (tokenObject.has(FIELD_IMAG)) {
                        image = tokenObject.optString(FIELD_IMAG);
                    }
                    if (tokenObject.has(FIELD_START_COLOR)) {
                        mStartColor = tokenObject.optString(FIELD_START_COLOR);
                    }
                    if (tokenObject.has(FIELD_END_COLOR)) {
                        mEndColor = tokenObject.optString(FIELD_END_COLOR);
                    }

                    TokenInfo item = new TokenInfo(address, name, symbol, image, mStartColor, mEndColor, decimals);

                    tokenItems.add(item);
                }
            }

        } catch (JSONException e) {
            Log.e(TAG, "Error parsing token list response from server:", e);
        }
        return tokenItems;
    }

    private static void saveTokenListToFile(Context context, String jsonResponse) {
        String filePath = MyApp.getmInstance().getFilesDir().getAbsolutePath() + File.separator + TOKENS_FILENAME;
        try {
            BufferedWriter fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath, true), "UTF-8"));
//            FileWriter fileWriter = new FileWriter(filePath);
            fileWriter.write(jsonResponse);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            Log.e(TAG, "Error writing tokens JSON response to tokens.json:", e);
        }
    }

    private static ArrayList<TokenInfo> getTokensFromFile(Context context) {
        try {
            File tokensFile = new File(context.getFilesDir().getPath() + File.separator + TOKENS_FILENAME);
            byte[] fileBytes;
            if (tokensFile.exists()) {
                FileInputStream fileInputStream = new FileInputStream(tokensFile);
                int size = fileInputStream.available();
                fileBytes = new byte[size];
                fileInputStream.read(fileBytes);
                fileInputStream.close();
            } else {
                InputStream json = context.getResources().openRawResource(R.raw.tokens);
                int size = json.available();
                fileBytes = new byte[size];
                json.read(fileBytes);
                json.close();
            }
//            return parseJsonToTokenList(context, new String(fileBytes,"UTF-8"));
            if (Util.isNullOrEmpty(tJson)) {
                return parseJsonToTokenList(context, new String(fileBytes));

            } else {

                saveTokenListToFile(MyApp.getmInstance(), tJson);
                return parseJsonToTokenList(context, tJson);
            }


        } catch (IOException e) {
            Log.e(TAG, "Error reading tokens.json file: ", e);
            return parseJsonToTokenList(context, tJson);
        }

    }
    private static ArrayList<TokenInfo> getEthTokensFromFile(Context context) {
        byte[] fileBytes;
        try {
                InputStream json = context.getResources().openRawResource(R.raw.ethtokens);
                int size = json.available();
                fileBytes = new byte[size];
                json.read(fileBytes);
                json.close();
            return parseJsonToTokenList(context, new String(fileBytes));
        } catch (IOException e) {
            Log.e(TAG, "Error reading tokens.json file: ", e);
            return new ArrayList<TokenInfo>();
        }

    }


    public static String getTokenStartColor(String currencyCode) {
        for (TokenInfo token : mTokenItems) {
            if (token.symbol.equalsIgnoreCase(currencyCode)) {
                return token.mStartColor;
            }
        }

        return "#ffffff";
    }

    public static String getTokenEndColor(String currencyCode) {
        for (TokenInfo token : mTokenItems) {
            if (token.symbol.equalsIgnoreCase(currencyCode)) {
                return token.mEndColor;
            }
        }

        return "#ffffff";
    }

    public static void getTokenDatas() {
        OkHttpClient http = new OkHttpClient();
        Request request = new Request.Builder().url(BaseUrl.HTTP_TOKEN_LISTURL).build();
        http.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                synchronized (TokenInfo.class) {
                    assert response.body() != null;
                    String responseBody = response.body().string();
                    tJson = responseBody;
//                    MyLog.i("-------------------------ADDRESS-" + responseBody);
//                    mTokenItems = parseJsonToTokenList(BreadApp.getBreadContext(), responseBody);
                    saveTokenListToFile(MyApp.getmInstance(), responseBody);
                }
            }
        });
    }
    /**
     * 获取ETZ当前钱包的Token
     * @param strings
     * @return
     */
    public static List<TokenInfo> getCurrentEtzTokens(List<String> strings) {
        if (mTokenItems==null) getTokenItems(MyApp.getmInstance());
        List<TokenInfo> tokens = new ArrayList<>();
        for (TokenInfo token : mTokenItems) {
            for (String str:strings) {
                if (token.symbol.equalsIgnoreCase(str)){
                    tokens.add(token);
                }
            }
        }
        return tokens;
    }

    /**
     * 获取ETH当前钱包的Token
     * @param strings
     * @return
     */
    public static List<TokenInfo> getCurrentEthTokens(List<String> strings) {
        if (ethTokens==null) getEthTokens(MyApp.getmInstance());
        List<TokenInfo> tokens = new ArrayList<>();
        for (TokenInfo token : ethTokens) {
            for (String str:strings) {
                if (token.symbol.equalsIgnoreCase(str)){
                    tokens.add(token);
                }
            }
        }
        return tokens;
    }
}
