package com.easyetz.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.TextView;

import java.util.Hashtable;

public class FontManager {

    private static final Hashtable<String, Typeface> cache = new Hashtable<>();

    private FontManager() {
    }

    public static void overrideFonts(TextView... v) {
        if (v == null) return;
        Typeface FONT_REGULAR = Typeface.create("sans-serif-light", Typeface.NORMAL);
        for (TextView view : v) {
            try {
                if (view != null) {
                    view.setTypeface(FONT_REGULAR);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static Typeface get(Context c, String name) {
        synchronized (cache) {
            if (!cache.containsKey(name)) {
                Typeface t = Typeface.createFromAsset(
                        c.getAssets(),
                        String.format("fonts/%s", name)
                );
                cache.put(name, t);
            }
            return cache.get(name);
        }
    }

    public static boolean setCustomFont(Context ctx, TextView v, String asset) {
        //make CircularPro-Book.otf default
        Typeface tf = FontManager.get(ctx, asset);
        v.setTypeface(tf);
        return true;
    }

}
