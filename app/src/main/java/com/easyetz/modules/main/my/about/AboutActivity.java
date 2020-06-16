package com.easyetz.modules.main.my.about;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.allenliu.versionchecklib.core.http.HttpRequestMethod;
import com.allenliu.versionchecklib.v2.AllenVersionChecker;
import com.allenliu.versionchecklib.v2.builder.UIData;
import com.allenliu.versionchecklib.v2.callback.RequestVersionListener;
import com.easyetz.R;
import com.easyetz.base.BaseActivity;
import com.easyetz.base.BaseUrl;
import com.easyetz.modules.normalvp.NormalPresenter;
import com.easyetz.modules.normalvp.NormalView;
import com.easyetz.utils.MyLog;
import com.easyetz.utils.ToastUtils;
import com.easyetz.utils.Util;
import com.easyetz.view.LoadingDialog;
import com.easyetz.view.MText;

import org.json.JSONObject;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AboutActivity extends BaseActivity<NormalView, NormalPresenter> implements NormalView {
    private static final String TAG = AboutActivity.class.getName();

    private static AboutActivity app;
    private static final int DEFAULT_VERSION_CODE = 0;
    private static final String DEFAULT_VERSION_NAME = "0";
    @BindView(R.id.info_text)
    MText infoText;
    private LoadingDialog loadingDialog;
    public static AboutActivity getApp() {
        return app;
    }


    @Override
    public int getLayoutId() {
        return R.layout.activity_about;
    }

    @Override
    public NormalPresenter initPresenter() {
        return new NormalPresenter();
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        setCenterTitle(getString(R.string.about_we));
        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        int versionCode = packageInfo != null ? packageInfo.versionCode : DEFAULT_VERSION_CODE;
        String versionName = packageInfo != null ? packageInfo.versionName : DEFAULT_VERSION_NAME;

        infoText.setText(String.format(Locale.getDefault(), getString(R.string.About_footer), versionName, versionCode));
    }

    @Override
    protected void initData() {

    }

    @Override
    public void initEvent() {

    }


    @Override
    protected void onResume() {
        super.onResume();
        app = this;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }


    @OnClick({R.id.iv_back, R.id.updata_apk})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                onBackPressed();
                break;
            case R.id.updata_apk:
                loadingDialog = new LoadingDialog(activity);
                loadingDialog.show();
                checkVersionUpdate();
                break;
        }
    }

    public String getVersionCode() {
        Context ctx = this.getApplicationContext();
        PackageManager packageManager = ctx.getPackageManager();
        PackageInfo packageInfo;
        String versionCode = "";
        try {
            packageInfo = packageManager.getPackageInfo(ctx.getPackageName(), 0);
            versionCode = packageInfo.versionCode + "";
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    public void checkVersionUpdate() {
        Context ctx = this.getApplicationContext();
        AllenVersionChecker
                .getInstance()
                .requestVersion()
                .setRequestMethod(HttpRequestMethod.GET)
                .setRequestUrl(BaseUrl.versionCheekUrl())
                .request(new RequestVersionListener() {
                    @Nullable
                    @Override
                    public UIData onRequestVersionSuccess(String result) {

                        try {
                            if (Util.isNullOrEmpty(result)) {
                                MyLog.i("onRequestVersionSuccess: 获取新版本失败1");
                            }
                            JSONObject json = new JSONObject(result);
                            MyLog.i("onRequestVersionSuccess: json==" + json);
                            JSONObject json1 = new JSONObject(json.getString("result"));

                            String dlUrl = json1.getString("url");
                            String dlContent = json1.getString("content");
                            String versionCode = json1.getString("versionCode");
                            String versionName = json1.getString("version");

                            StringBuilder stringBuilder = new StringBuilder();
                            stringBuilder.append(getString(R.string.current_version));
                            stringBuilder.append(versionName);
                            stringBuilder.append("\n");
                            stringBuilder.append(getString(R.string.update_content));
                            stringBuilder.append(dlContent);

                            String finalString = stringBuilder.toString();

                            if (Integer.parseInt(versionCode) > Integer.parseInt(getVersionCode())) {
                                UIData uiData = UIData
                                        .create()
                                        .setDownloadUrl(dlUrl)
                                        .setTitle(getString(R.string.download_latest_version))
                                        .setContent(finalString);
                                loadingDialog.dismiss();
                                return uiData;
                            } else {
                                loadingDialog.dismiss();
                                ToastUtils.showLongToast(activity, "已是最新版本");
                                return null;
                            }

                        } catch (Exception e) {
                            MyLog.i("onRequestVersionSuccess: 获取新版本失败2");
                        }

                        return null;
                    }

                    @Override
                    public void onRequestVersionFailure(String message) {

                    }
                })
                .excuteMission(ctx);

    }
}
