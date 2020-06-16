package com.easyetz.utils;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;

import com.easyetz.R;


public class ClipboardManager {

    @SuppressLint("NewApi")
    public static void putClipboard(Context context, String text) {
        try {
            MyLog.i("putClipboard----"+text);
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context
                    .getSystemService(Context.CLIPBOARD_SERVICE);
            MyLog.i("putClipboard----"+"1");
            android.content.ClipData clip = android.content.ClipData
                    .newPlainText("message", text);
            MyLog.i("putClipboard----"+"2");
            clipboard.setPrimaryClip(clip);
            MyLog.i("putClipboard----"+"3");
            ToastUtils.showLongToast(context,R.string.Alerts_copiedAddressesHeader);
        } catch (Exception e) {
            MyLog.i("putClipboard----"+e.toString());
            e.printStackTrace();
        }
    }

    @SuppressLint("NewApi")
    public static String getClipboard(Context context) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context
                .getSystemService(Context.CLIPBOARD_SERVICE);

        // Gets a content resolver instance
        ContentResolver cr = context.getContentResolver();

        // Gets the clipboard data from the clipboard
        ClipData clip = clipboard.getPrimaryClip();
        if (clip != null) {

            // Gets the first item from the clipboard data
            ClipData.Item item = clip.getItemAt(0);

            return coerceToText(item).toString();
        }
        return "";
    }

    @SuppressLint("NewApi")
    private static CharSequence coerceToText(ClipData.Item item) {
        // If this Item has an explicit textual value, simply return that.
        CharSequence text = item.getText();
        if (text != null) {
            return text;
        } else {
            return "no text";
        }

    }

}