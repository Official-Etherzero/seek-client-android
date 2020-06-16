package com.easyetz.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.StringRes;
import android.widget.Toast;

import com.easyetz.app.MyApp;
import com.easyetz.view.CustomToast;

public class ToastUtils {
    private ToastUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    private static   CustomToast myToast ;
    /**
     * 显示短时吐司
     *
     * @param text 文本
     */
    public static void showShortToast(CharSequence text) {
        myToast=new CustomToast(MyApp.getBreadContext());
        myToast.setMessage(text);
        myToast.showTime(2000);
        myToast.show();
    }

    /**
     * 显示短时吐司
     *
     * @param resId 资源Id
     */
    public static void showShortToast(@StringRes int resId) {
        myToast=new CustomToast(MyApp.getBreadContext());
        myToast.setMessage(resId);
        myToast.showTime(2000);
        myToast.show();
    }



    /**
     * 显示长时吐司
     *
     * @param text 文本
     */
    public static void showLongToast(Context app,CharSequence text) {
        myToast=new CustomToast(app);
        myToast.setMessage(text);
        myToast.showTime(4000);
        myToast.show();
    }

    /**
     * 显示长时吐司
     *
     * @param resId 资源Id
     */
    public static void showLongToast(@StringRes int resId) {
        myToast=new CustomToast(MyApp.getBreadContext());
        myToast.setMessage(resId);
        myToast.showTime(4000);
        myToast.show();
    }
    /**
     * 显示长时吐司
     *
     * @param resId 资源Id
     */
    public static void showLongToast(Context ctx, @StringRes int resId) {
        myToast=new CustomToast(ctx);
        myToast.setMessage(resId);
        myToast.showTime(4000);
        myToast.show();
    }

    /**
     * 取消吐司显示
     */
    public static void cancelToast() {
        if (myToast != null) {
            myToast.dismiss();
        }
    }
}
