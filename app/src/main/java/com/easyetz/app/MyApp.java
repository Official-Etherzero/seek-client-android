package com.easyetz.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.preference.PreferenceManager;

import com.easyetz.blockchain.BtcConfiguration;
import com.easyetz.service.BlockchainService;
import com.easyetz.utils.AppFilePath;
import com.easyetz.utils.LocalManageUtil;
import com.easyetz.utils.MyLog;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheEntity;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.cookie.CookieJarImpl;
import com.lzy.okgo.cookie.store.SPCookieStore;
import com.lzy.okgo.https.HttpsUtils;
import com.lzy.okgo.interceptor.HttpLoggingInterceptor;
import com.lzy.okgo.model.HttpHeaders;
import com.tencent.bugly.Bugly;
import com.tencent.bugly.crashreport.CrashReport;
import com.zhy.autolayout.config.AutoLayoutConifg;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import okhttp3.OkHttpClient;

public class MyApp extends Application {
    private static MyApp mInstance;
    public SPCookieStore mSPCookieStore;
    public static AtomicInteger activityCounter = new AtomicInteger();

    public static MyApp getmInstance() {
        return mInstance;
    }

    private static Activity currentActivity;
    private static Context mContext;
    private BtcConfiguration config;


    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        mContext = this;
        mSPCookieStore = new SPCookieStore(this);
        //获取代币列表
//        TokenUtil.getTokenDatas();
        AutoLayoutConifg.getInstance().useDeviceSize();
        LocalManageUtil.setApplicationLanguage(getApplicationContext());
        AppFilePath.init(this);
        Bugly.init(getApplicationContext(), "f57525a0ca", false);
// 日志输出
        try {
            initOkGo();
        } catch (IOException e) {
            e.printStackTrace();
        }
        BlockchainService.start(this, false);
    }

    @Override
    protected void attachBaseContext(Context base) {
        //保存系统选择语言
        LocalManageUtil.saveSystemCurrentLanguage(base);
        super.attachBaseContext(LocalManageUtil.setLocal(base));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        MyLog.i("+-+-+-+-+-+");
        super.onConfigurationChanged(newConfig);
        //保存系统选择语言
        LocalManageUtil.onConfigurationChanged(getApplicationContext());
    }

    public void initOkGo() throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.put("Content-Type", "application/json; charset=utf-8");
        headers.put("Accept", "application/json");

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        //log相关
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor("OkHttp");
        loggingInterceptor.setPrintLevel(HttpLoggingInterceptor.Level.BODY);        //log打印级别，决定了log显示的详细程度
        loggingInterceptor.setColorLevel(Level.INFO);                               //log颜色级别，决定了log在控制台显示的颜色
        builder.addInterceptor(loggingInterceptor);                                 //添加OkGo默认debug日志
        //超时时间设置
        builder.readTimeout(10000, TimeUnit.MILLISECONDS);      //全局的读取超时时间
        builder.writeTimeout(10000, TimeUnit.MILLISECONDS);     //全局的写入超时时间
        builder.connectTimeout(10000, TimeUnit.MILLISECONDS);   //全局的连接超时时间
        builder.cookieJar(new CookieJarImpl(mSPCookieStore));            //使用sp保持cookie，如果cookie不过期，则一直有效


        HttpsUtils.SSLParams sslParams = HttpsUtils.getSslSocketFactory(getAssets().open("server.cer"));
        builder.sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager);
//        //配置https的域名匹配规则，使用不当会导致https握手失败
        builder.hostnameVerifier(HttpsUtils.UnSafeHostnameVerifier);

        // 其他统一的配置
        OkGo.getInstance().init(this)                           //必须调用初始化
                .setOkHttpClient(builder.build())               //必须设置OkHttpClient
                .setCacheMode(CacheMode.NO_CACHE)               //全局统一缓存模式，默认不使用缓存，可以不传
                .setCacheTime(CacheEntity.CACHE_NEVER_EXPIRE)   //全局统一缓存时间，默认永不过期，可以不传
                .setRetryCount(1)          //全局统一超时重连次数，默认为三次，那么最差的情况会请求4次(一次原始请求，三次重连请求)，不需要可以设置为0
                .addCommonHeaders(headers);              //全局公共头
//                .addCommonParams(httpParams);                       //全局公共参数

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (mInstance != null) {
            mInstance = null;
        }
    }

    public static void setBreadContext(Activity app) {
        activityCounter.incrementAndGet();
        currentActivity = app;
    }

    public static boolean isAppInBackground(final Context context) {
        return context == null || activityCounter.get() <= 0;
    }

    public static Context getBreadContext() {
        Context app = currentActivity;
//        if (app == null) app = SyncReceiver.app;
        if (app == null) app = mContext;
        return app;
    }

    public synchronized BtcConfiguration getConfiguration() {
        if (config == null)
            config = new BtcConfiguration(PreferenceManager.getDefaultSharedPreferences(this), getResources());
        return config;
    }

    private PackageInfo packageInfo;

    public synchronized PackageInfo packageInfo() {
        // replace by BuildConfig.VERSION_* as soon as it's possible
        if (packageInfo == null) {
            try {
                packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            } catch (final PackageManager.NameNotFoundException x) {
                throw new RuntimeException(x);
            }
        }
        return packageInfo;
    }



}
