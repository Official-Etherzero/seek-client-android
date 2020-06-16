package com.easyetz.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.util.DisplayMetrics;

import com.easyetz.R;
import com.easyetz.app.MyApp;
import com.easyetz.bean.languageEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 国际化语言设置选项
 */

public class LocalManageUtil {

    private static final String TAG = "LocalManageUtil";

    /**
     * 获取系统的locale
     *
     * @return Locale对象
     */
    public static Locale getSystemLocale(Context context) {
        return SPLUtil.getInstance(context).getSystemCurrentLocal();
    }

    /**
     * 获取选择的语言设置
     *
     * @param context
     * @return
     */
    public static Locale getSetLanguageLocale(Context context) {

//        MyLog.i("******************==" + SPLUtil.getInstance(context).getSelectLanguage() + "----" + getSystemLocale(context).getLanguage());
//        int lid = SPLUtil.getInstance(context).getSelectLanguage();
        String lang = SPLUtil.getInstance(context).getSystemCurrentLocal().getLanguage();
        switch (SPLUtil.getInstance(context).getSelectLanguage()) {
            case 0:
                if (lang.equalsIgnoreCase("zh"))
                    return Locale.SIMPLIFIED_CHINESE;//中文简体
                else
                    return getSystemLocale(context);
            case 1:
                return Locale.SIMPLIFIED_CHINESE;//中文简体
            case 2:
                return Locale.ENGLISH;//英语
            case 3:
                return Locale.KOREAN;//韩语
            case 4:
                return Locale.GERMAN;//德语
            case 5:
                return Locale.JAPANESE;//日本语
            case 6:
                return Locale.TRADITIONAL_CHINESE;//香港
            case 7:
                return Locale.TRADITIONAL_CHINESE;//台湾

            default:
                return Locale.ENGLISH;
        }
    }

    public static void saveSelectLanguage(Context context, int select) {
        SPLUtil.getInstance(context).saveLanguage(select);
        setApplicationLanguage(context);
    }

    public static Context setLocal(Context context) {
        return updateResources(context, getSetLanguageLocale(context));
    }

    private static Context updateResources(Context context, Locale locale) {
        Locale.setDefault(locale);

        Resources res = context.getResources();
        Configuration config = new Configuration(res.getConfiguration());
        if (Build.VERSION.SDK_INT >= 17) {
            config.setLocale(locale);
            context = context.createConfigurationContext(config);
        } else {
            config.locale = locale;
            res.updateConfiguration(config, res.getDisplayMetrics());
        }
        return context;
    }


    /**
     * 设置语言类型
     */
    public static void setApplicationLanguage(Context context) {
        Resources resources = context.getApplicationContext().getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();
        Locale locale = getSetLanguageLocale(context);
        config.locale = locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LocaleList localeList = new LocaleList(locale);
            LocaleList.setDefault(localeList);
            config.setLocales(localeList);
            context.getApplicationContext().createConfigurationContext(config);
            Locale.setDefault(locale);
        }
        resources.updateConfiguration(config, dm);
    }

    public static void saveSystemCurrentLanguage(Context context) {
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = LocaleList.getDefault().get(0);
        } else {
            locale = Locale.getDefault();
        }
        SPLUtil.getInstance(context).setSystemCurrentLocal(locale);
    }

    public static void onConfigurationChanged(Context context) {
        saveSystemCurrentLanguage(context);
        setLocal(context);
        setApplicationLanguage(context);
    }

    public static List<languageEntity> getLanguageList(Context context) {
        List<languageEntity> languageEntities = new ArrayList<>();
        languageEntities.add(new languageEntity(1, "中文"));
        languageEntities.add(new languageEntity(2, "English"));
        languageEntities.add(new languageEntity(3, "한글"));
        return languageEntities;
    }
}