package com.easyetz.utils;

import android.content.Context;
import android.net.Uri;

import com.easyetz.base.Constants;
import com.easyetz.bean.CryptoRequest;
import com.easyetz.blockchain.BtcWalletManager;
import com.easyetz.blockchain.EtzWalletManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CryptoUriParser {
    private static final String TAG = CryptoUriParser.class.getName();
    public static Uri createCryptoUrl(Context app, String iso,String symbol , String
            addr, BigDecimal cryptoAmount, String label, String message, String rURL) {
        Uri.Builder builder = new Uri.Builder();
        String walletScheme = symbol;
        String cleanAddress = addr;
        if (addr.contains(":")) {
            cleanAddress = addr.split(":")[1];
    }
        builder = builder.scheme(walletScheme);
        if (!Util.isNullOrEmpty(cleanAddress)) {
            builder = builder.appendPath(cleanAddress);
        }
        if (cryptoAmount.compareTo(BigDecimal.ZERO) != 0) {
            if (iso.equalsIgnoreCase("SEEK")||iso.equalsIgnoreCase("ETH")) {
                BigDecimal ethAmount = cryptoAmount.divide(new BigDecimal("1"), 3, Constants.ROUNDING_MODE);
                builder = builder.appendQueryParameter("value", ethAmount.toPlainString() + "e18");
            } else if (iso.equalsIgnoreCase("BTC") || iso.equalsIgnoreCase("BCH")) {
                BigDecimal amount = cryptoAmount.divide(new BigDecimal(BtcWalletManager.ONE_BITCOIN_IN_SATOSHIS), 8, Constants.ROUNDING_MODE);
                builder = builder.appendQueryParameter("amount", amount.toPlainString());
            } else {
                throw new RuntimeException("URI not supported for: " + iso);
            }
        }
        if (label != null && !label.isEmpty()) {
            builder = builder.appendQueryParameter("label", label);
        }
        if (message != null && !message.isEmpty()) {
            builder = builder.appendQueryParameter("message", message);
        }
        if (rURL != null && !rURL.isEmpty()) {
            builder = builder.appendQueryParameter("r", rURL);
        }

        return Uri.parse(builder.build().toString().replace("/", ""));

    }

    private static String cleanUrl(String url) {
        return url.trim().replaceAll("\n", "").replaceAll(" ", "%20");
    }

    public static CryptoRequest parseRequest(Context app, String str) {
        if (str == null || str.isEmpty()) return null;
        CryptoRequest obj = new CryptoRequest();

        String tmp = cleanUrl(str);

        Uri u = Uri.parse(tmp);
        String scheme = u.getScheme();
        obj.scheme = scheme;

        String schemeSpecific = u.getSchemeSpecificPart();
        if (schemeSpecific.startsWith("//")) {
            // Fix invalid bitcoin uri
            schemeSpecific = schemeSpecific.substring(2);
        }

        u = Uri.parse(scheme + "://" + schemeSpecific);

        String host = u.getHost();
        if (host != null) {
            String addrs = host.trim();
            if (!Util.isNullOrEmpty(addrs) ) {
                obj.address = addrs;
            }
        }
        String query = u.getQuery();
        if (query == null) {
            return obj;
        }
        String[] params = query.split("&");
        for (String s : params) {
            String[] keyValue = s.split("=", 2);
            if (keyValue.length != 2) {
                continue;
            }
            if (keyValue[0].trim().equals("amount")) {
                try {
                    BigDecimal bigDecimal = new BigDecimal(keyValue[1].trim());
                    obj.amount = bigDecimal.multiply(new BigDecimal("100000000"));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                // ETH payment request amounts are called `value`
            } else if (keyValue[0].trim().equals("value")) {
                obj.value = new BigDecimal(keyValue[1].trim());
            } else if (keyValue[0].trim().equals("label")) {
                obj.label = keyValue[1].trim();
            } else if (keyValue[0].trim().equals("message")) {
                obj.message = keyValue[1].trim();
            } else if (keyValue[0].trim().startsWith("req")) {
                obj.req = keyValue[1].trim();
            } else if (keyValue[0].trim().startsWith("r")) {
                obj.r = keyValue[1].trim();
            }
        }
        return obj;
    }
    public static boolean isCryptoUrl(Context app, String url) {
        if (Util.isNullOrEmpty(url)) {
            return false;
        }
        CryptoRequest requestObject = parseRequest(app, url);
        // return true if the request is valid url and has param: r or param: address
        // return true if it is a valid bitcoinPrivKey
        return (requestObject != null && (requestObject.isPaymentProtocol() || requestObject.hasAddress()));
    }



}
