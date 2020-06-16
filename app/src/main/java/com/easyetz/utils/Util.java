package com.easyetz.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.TypedValue;
import android.widget.TextView;

import com.easyetz.app.MyApp;

import java.util.Collection;

public class Util {
    /**
     * 字符串是否为空。空返回true
     * @param str
     * @return
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty()||str.equalsIgnoreCase("null");
    }
    /**
     * byte[]是否为空。空返回true
     * @param arr
     * @return
     */
    public static boolean isNullOrEmpty(byte[] arr) {
        return arr == null || arr.length == 0;
    }
    /**
     * Collection是否为空。空返回true
     * @param collection
     * @return
     */
    public static boolean isNullOrEmpty(Collection collection) {
        return collection == null || collection.size() == 0;
    }

    public static int getPixelsFromDps(Context context, int dps) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dps * scale + 0.5f);
    }

    public static void correctTextSizeIfNeeded(TextView v) {
        int limit = 100;
        int lines = v.getLineCount();
        float px = v.getTextSize();
        while (lines > 1 && !v.getText().toString().contains("\n")) {
            limit--;
            px -= 1;
            v.setTextSize(TypedValue.COMPLEX_UNIT_PX, px);
            lines = v.getLineCount();
            if (limit <= 0) {
                MyLog.e( "correctTextSizeIfNeeded: Failed to rescale, limit reached, final: " + px);
                break;
            }
        }
    }
    /**
     * dip 转 px
     * @param dipValue
     * @return
     */
    public static int dip2px(float dipValue) {
        float scale = MyApp.getmInstance().getResources().getDisplayMetrics().density;
        return (int)(dipValue * scale + 0.5F);
    }

    /**
     * 把String转化为double
     * @param number
     * @param defaultValue
     * @return
     */
    public static double convertToDouble(String number, double defaultValue) {
        if (TextUtils.isEmpty(number)) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(number);
        } catch (Exception e) {
            return defaultValue;
        }

    }
    public static boolean isAddressValid(String address) {
        String regExp="^0[xX][0-9a-fA-F]{40}$";
        return address.matches(regExp);
//        return !isNullOrEmpty(address) && address.startsWith("0x")&&address.length()==42;
    }
}
