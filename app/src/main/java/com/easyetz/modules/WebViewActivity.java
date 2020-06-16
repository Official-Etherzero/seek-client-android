package com.easyetz.modules;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebHistoryItem;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.easyetz.R;
import com.easyetz.base.BaseActivity;
import com.easyetz.modules.dapp.DappTransaction;
import com.easyetz.modules.main.discovery.FragmentDiscovery;
import com.easyetz.modules.normalvp.NormalPresenter;
import com.easyetz.modules.normalvp.NormalView;
import com.easyetz.utils.MyLog;
import com.easyetz.utils.SPLUtil;
import com.easyetz.utils.SharedPrefsUitls;
import com.easyetz.utils.Util;
import com.easyetz.view.BRDialogView;
import com.easyetz.view.ETZDialog;
import com.easyetz.view.MText;
import com.gyf.barlibrary.ImmersionBar;

import org.json.JSONException;
import org.json.JSONObject;
import org.web3j.crypto.Hash;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WebViewActivity extends BaseActivity<NormalView, NormalPresenter> implements NormalView {

    @BindView(R.id.web_ac_pb)
    ProgressBar pb;
    @BindView(R.id.web_ac_mweb)
    WebView web;
    @BindView(R.id.web_ac_back)
    ImageView webBack;
    @BindView(R.id.web_ac_title)
    MText webTitle;
    private final static int FILECHOOSER_RESULTCODE = 1;// 表单的结果回调</span>

    private Uri imageUri;
    private ValueCallback<Uri> mUploadMessage;// 表单的数据信息
    private ValueCallback<Uri[]> mUploadCallbackAboveL;
    private String mFailingUrl = "http://seek.fh768.io/";
    public static String tid = "";
    private static WebViewActivity instance;

    public static WebViewActivity getInstance() {
        return instance;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_webview;
    }

    @Override
    public NormalPresenter initPresenter() {
        return new NormalPresenter();
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        instance = this;
        ImmersionBar.with(this)
                .transparentStatusBar()
                .statusBarDarkFont(true, 1f)
                .init();
        WebSettings ws = web.getSettings();
        ws.setLoadWithOverviewMode(true);
        ws.setUseWideViewPort(true);
        ws.setJavaScriptEnabled(true);
        ws.setSupportMultipleWindows(true);
        ws.setAllowFileAccess(true);
        ws.setBuiltInZoomControls(true);
        ws.setDomStorageEnabled(true);// 设置适应Html5的一些方法
        ws.setBlockNetworkImage(false);
        ws.setTextZoom(100);
        ws.setCacheMode(WebSettings.LOAD_NO_CACHE);
        // 5.0以上手机播视频需设置
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            ws.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
        ws.setSupportZoom(true);
        ws.setJavaScriptCanOpenWindowsAutomatically(true);
//        ws.setRenderPriority(WebSettings.RenderPriority.HIGH);//设置渲染的优先级

        if (Build.VERSION.SDK_INT < 19) {
            if (Build.VERSION.SDK_INT > 8) {
                ws.setPluginState(WebSettings.PluginState.ON);
            }
        }
        web.requestFocus();
        // 适应全屏 39适应竖屏 57适应横屏
        web.setInitialScale(39);
        web.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        if (Build.VERSION.SDK_INT >= 11)
            ws.setDisplayZoomControls(false);

        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(final WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (pb == null) return;
                if (newProgress == 100) {
                    pb.setVisibility(View.GONE);
                } else {
                    pb.setVisibility(View.VISIBLE);
                    pb.setProgress(newProgress);
                }
            }

            @Override
            public boolean onShowFileChooser(WebView webView,
                                             ValueCallback<Uri[]> filePathCallback,
                                             FileChooserParams fileChooserParams) {
                mUploadCallbackAboveL = filePathCallback;
                take();
                return true;
            }

            @SuppressWarnings("unused")
            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                mUploadMessage = uploadMsg;
                take();
            }

            @SuppressWarnings("unused")
            public void openFileChooser(ValueCallback<Uri> uploadMsg,
                                        String acceptType) {
                mUploadMessage = uploadMsg;
                take();
            }

            @SuppressWarnings("unused")
            public void openFileChooser(ValueCallback<Uri> uploadMsg,
                                        String acceptType, String capture) {
                mUploadMessage = uploadMsg;
                take();
            }

            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog,
                                          boolean isUserGesture, Message resultMsg) {
                return super.onCreateWindow(view, isDialog, isUserGesture,
                        resultMsg);
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                if (!Util.isNullOrEmpty(title) && title.toLowerCase().contains("error")) {
                    view.stopLoading();
                    view.clearAnimation();
                    web.loadUrl("file:///android_asset/anomalies/index.html");
                }
                if (!Util.isNullOrEmpty(title) && !title.toLowerCase().contains("error")) {
                    if (webTitle != null)
                        webTitle.setText(title);
                }

//                super.onReceivedTitle(view, title);
            }

        });
        web.setOnKeyListener(new View.OnKeyListener() {

            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if ((keyCode == KeyEvent.KEYCODE_BACK) && web.canGoBack()) {
                    web.goBack(); // goBack()表示返回WebView的上一页面
                    return true;
                } else {
                    finish();
                }
                return false;
            }
        });
        web.setWebViewClient(new WebViewClient() {
            private boolean bo;

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                mFailingUrl = url;
                MyLog.i("weburl=" + url);
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                MyLog.i("weburl=" + url);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                MyLog.i("weburl=" + url);
                super.onPageFinished(view, url);
                if (bo == true)
                    web.getSettings().setBlockNetworkImage(false);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    return;
                }
                view.stopLoading();
                view.clearAnimation();
                web.getSettings().setBlockNetworkImage(false);
                mFailingUrl = failingUrl;
                web.loadUrl("file:///android_asset/anomalies/index.html");
            }

            // 新版本，只会在Android6及以上调用
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (request.isForMainFrame()) { // 或者： if(request.getUrl().toString() .equals(getUrl()))
                    // 在这里显示自定义错误页
                    view.stopLoading();
                    view.clearAnimation();
                    if (web == null) return;
                    web.getSettings().setBlockNetworkImage(false);
                    web.loadUrl("file:///android_asset/anomalies/index.html");
                }
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                super.onReceivedSslError(view, handler, error);
            }
        });
        web.setDownloadListener(downloadListener);
        web.addJavascriptInterface(new JsInterface(WebViewActivity.this), "easyetz");
        web.loadUrl("http://seek.fh768.io/");
    }

    @Override
    protected void initData() {
    }

    @Override
    public void initEvent() {
        webBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myLastUrl().equals("http://seek.fh768.io/#/index")) finish();
                if (web.canGoBack()) {
                    web.goBack();
                } else {
                    finish();
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }


    /**
     * H5交互
     */
    public class JsInterface {
        Context ctx;

        public JsInterface(Context app) {
            this.ctx = app;
        }

        @JavascriptInterface
        public void etzTransaction(String strJson) {
            SharedPrefsUitls.getInstance().putlastDappHash("");

            MyLog.i("etzTransaction----address=" + strJson);
            try {
                JSONObject jsonObject = new JSONObject(strJson);
                FragmentDiscovery.tid = jsonObject.optString("keyTime");
                String value = jsonObject.optString("etzValue");
                if (!Util.isNullOrEmpty(value)) {
                    value = new BigDecimal(value).toPlainString();
                } else {
                    value = "0";
                }
                MyLog.i("etzTransaction----address=" + value);
                Intent intent = new Intent(ctx, DappTransaction.class);
                intent.putExtra("to", jsonObject.optString("contractAddress"));
                intent.putExtra("value", value);
                intent.putExtra("data", jsonObject.optString("datas"));
                intent.putExtra("gasL", jsonObject.optString("gasLimit"));
                intent.putExtra("gasP", jsonObject.optString("gasPrice"));
                ctx.startActivity(intent);
            } catch (JSONException e) {
                e.printStackTrace();
                ETZDialog.showSimpleDialog(ctx, ctx.getString(R.string.Alert_error), e.getMessage());
            }
        }

        @JavascriptInterface
        public String getAddress() {
//        Toast.makeText(BreadApp.getMyApp(), WalletEthManager.getInstance(getApplication()).getAddress(), Toast.LENGTH_LONG).show();
            return SharedPrefsUitls.getInstance().getCurrentWalletAddress();
        }

        /**
         * * Keccak-256 hash function that operates on a UTF-8 encoded String.
         *
         * @param utf8String UTF-8 encoded string
         * @return hash value as hex encoded string
         */
        @JavascriptInterface
        public String getSha3String(String utf8String) {
            Toast.makeText(WebViewActivity.this, utf8String, Toast.LENGTH_LONG).show();
            return Hash.sha3String(utf8String);
        }

        @JavascriptInterface
        public String getBalance(String iso) {
            String balance = null;
            if (iso.equalsIgnoreCase("SEEK")) {
//                balance = String.valueOf(BRSharedPrefs.getCachedBalance(ctx, iso).divide(new BigDecimal(WalletEthManager.ETHER_WEI)));
            } else {
//                balance = String.valueOf(SharedPrefsUitls.getInstance().getCachedBalance(ctx, iso));
            }
            return balance;
        }

        @JavascriptInterface
        public void closeWeb() {
            ((Activity) ctx).finish();
        }

        @JavascriptInterface
        public String getTransactionHash() {
            String hash = SharedPrefsUitls.getInstance().getlastDappHash();
            return hash;
        }

        @JavascriptInterface
        public String getMinerInfo() {
            MyLog.i("getMinerMore====" + SharedPrefsUitls.getInstance().getMinerInfo());
            return SharedPrefsUitls.getInstance().getMinerInfo();
        }

        @JavascriptInterface
        public String getCurrentLanguage() {
            int hash = SPLUtil.getInstance(activity).getSelectLanguage();
            switch (hash) {
                case 0:
                    String lang = Locale.getDefault().getLanguage();
                    if (lang.equalsIgnoreCase("zh")) {
                        return "CNS";
                    } else if (lang.equalsIgnoreCase("ko")) {
                        return "KO";
                    } else {
                        return "EN";
                    }
                case 1:
                    return "CNS";
                case 2:
                    return "EN";
                case 3:
                    return "KO";
                default:
                    return "EN";
            }

        }

        private void sayInvalidClipboardData(String title) {
            ETZDialog.showCustomDialog(ctx, "", title,
                    ctx.getString(R.string.AccessibilityLabels_close), null, new BRDialogView.BROnClickListener() {
                        @Override
                        public void onClick(BRDialogView brDialogView) {
                            brDialogView.dismiss();
                        }
                    }, null, null, 0);
        }

        @JavascriptInterface
        public void errorReload() {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if (mFailingUrl != null) {
                        web.loadUrl(mFailingUrl);
                    }
                }
            });
        }

    }

    /**
     * APK下载监听
     */
    DownloadListener downloadListener = new DownloadListener() {
        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage && null == mUploadCallbackAboveL)
                return;
            Uri result = data == null || resultCode != RESULT_OK ? null : data
                    .getData();
            if (mUploadCallbackAboveL != null) {
                onActivityResultAboveL(requestCode, resultCode, data);
            } else if (mUploadMessage != null) {

                if (result != null) {
                    String path = getPath(WebViewActivity.this, result);
                    Uri uri = Uri.fromFile(new File(path));
                    mUploadMessage.onReceiveValue(uri);
                } else {
                    mUploadMessage.onReceiveValue(imageUri);
                }
                mUploadMessage = null;

            }
        }
    }

    @SuppressWarnings("null")
    @TargetApi(Build.VERSION_CODES.ECLAIR_MR1)
    private void onActivityResultAboveL(int requestCode, int resultCode,
                                        Intent data) {
        if (requestCode != FILECHOOSER_RESULTCODE
                || mUploadCallbackAboveL == null) {
            return;
        }

        Uri[] results = null;

        if (resultCode == RESULT_OK) {

            if (data == null) {

                results = new Uri[]{imageUri};
            } else {
                String dataString = data.getDataString();
                ClipData clipData = data.getClipData();

                if (clipData != null) {
                    results = new Uri[clipData.getItemCount()];
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        results[i] = item.getUri();
                    }
                }

                if (dataString != null)
                    results = new Uri[]{Uri.parse(dataString)};
            }
        }
        if (results != null) {
            mUploadCallbackAboveL.onReceiveValue(results);
            mUploadCallbackAboveL = null;
        } else {
            results = new Uri[]{imageUri};
            mUploadCallbackAboveL.onReceiveValue(results);
            mUploadCallbackAboveL = null;
        }

        return;
    }

    private void take() {
        File imageStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "MyApp");
        // Create the storage directory if it does not exist
        if (!imageStorageDir.exists()) {
            imageStorageDir.mkdirs();
        }
        File file = new File(imageStorageDir + File.separator + "IMG_"
                + String.valueOf(System.currentTimeMillis()) + ".jpg");
        imageUri = Uri.fromFile(file);

        final List<Intent> cameraIntents = new ArrayList<Intent>();
        final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getApplication().getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(
                captureIntent, 0);
        for (ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent i = new Intent(captureIntent);
            i.setComponent(new ComponentName(res.activityInfo.packageName,
                    res.activityInfo.name));
            i.setPackage(packageName);
            i.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            cameraIntents.add(i);

        }
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        Intent chooserIntent = Intent.createChooser(i, "Image Chooser");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
                cameraIntents.toArray(new Parcelable[]{}));
        startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);
    }

    @SuppressLint("NewApi")
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getPath(final Context context, final Uri uri) {
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/"
                            + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection,
                        selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();

        }

        return null;
    }

    public static String getDataColumn(Context context, Uri uri,
                                       String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection,
                    selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri
                .getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri
                .getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri
                .getAuthority());
    }

    @Override
    public void onResume() {
        super.onResume();
        MyLog.i("hash**********=");
        if (web != null) {
            web.onResume();
            String hash = SharedPrefsUitls.getInstance().getlastDappHash();
//交易回调JS方法，成功返回Hash,取消交易hash为“”
            MyLog.i("hash**********=");
            if (!Util.isNullOrEmpty(hash) && !Util.isNullOrEmpty(tid)) {
                hash = hash.substring(2, hash.length());
                MyLog.i("hash**********=" + hash);
                web.evaluateJavascript("javascript:makeSaveData('" + hash + "','" + tid + "')", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        MyLog.i("onReceiveValue-----" + value);
                    }

                });
            } else {
                hash = "";
                MyLog.i("onReceiveValue-----***********" + tid);
                web.evaluateJavascript("javascript:makeSaveData('" + hash + "','" + tid + "')", new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        MyLog.i("onReceiveValue-----" + value);
                    }

                });
            }
            web.resumeTimers();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (web != null) {
            ViewParent parent = web.getParent();
            if (parent != null) {
                ((ViewGroup) parent).removeView(web);
            }
            web.removeAllViews();
            tid = "";
            web.destroy();
            web = null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (web != null) {
            web.onPause();
            web.pauseTimers();
        }
    }

    /**
     * 拿到上一页的路径
     */
    private String myLastUrl() {
        WebBackForwardList backForwardList = web.copyBackForwardList();
        if (backForwardList != null && backForwardList.getSize() != 0) {
            //当前页面在历史队列中的位置
            int currentIndex = backForwardList.getCurrentIndex();
            WebHistoryItem historyItem =
                    backForwardList.getItemAtIndex(currentIndex - 1);
            if (historyItem != null) {
                String backPageUrl = historyItem.getUrl();
                MyLog.i("weburl=========" + backPageUrl);
                return backPageUrl;

            }

        }
        return "";
    }

}
