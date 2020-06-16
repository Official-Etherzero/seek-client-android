package com.easyetz.modules.main;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.LinearLayout;

import com.easyetz.R;
import com.easyetz.adapter.HomePagerAdapter;
import com.easyetz.base.BaseActivity;
import com.easyetz.base.BaseUrl;
import com.easyetz.base.Constants;
import com.easyetz.bean.CryptoRequest;
import com.easyetz.modules.main.discovery.FragmentDiscovery;
import com.easyetz.modules.main.my.FragmentMy;
import com.easyetz.modules.main.property.PropertyFragment;
import com.easyetz.modules.normalvp.NormalPresenter;
import com.easyetz.modules.normalvp.NormalView;
import com.easyetz.modules.tools.threads.ETZExecutor;
import com.easyetz.modules.walletoperation.send.SendActivity;
import com.easyetz.utils.CryptoUriParser;
import com.easyetz.utils.MyLog;
import com.easyetz.utils.ToastUtils;
import com.easyetz.utils.Util;
import com.easyetz.utils.nodeUtiles;
import com.easyetz.view.NoScrollViewPager;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends BaseActivity<NormalView, NormalPresenter> implements NormalView {

    @BindView(R.id.vp_home)
    NoScrollViewPager vpHome;
    @BindView(R.id.main_daohanglan)
    LinearLayout mainDaohanglan;


    protected static MainActivity mActivity = null;
    private HomePagerAdapter homePagerAdapter;

    public static MainActivity getApp() {
        return mActivity;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public NormalPresenter initPresenter() {
        return new NormalPresenter();
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        mActivity = this;
        vpHome.setOffscreenPageLimit(3);
        List<Fragment> fragmentList = new ArrayList<>();
        fragmentList.add(new PropertyFragment());
        fragmentList.add(new FragmentDiscovery());
        fragmentList.add(new FragmentMy());
        homePagerAdapter = new HomePagerAdapter(getSupportFragmentManager(), fragmentList);
        vpHome.setAdapter(homePagerAdapter);
        vpHome.setCurrentItem(0, false);
        isClickLL(true, false, false);
        ETZExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                nodeUtiles.seekNodeList();
            }
        });
    }

    @Override
    protected void initData() {

    }

    @Override
    public void initEvent() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }

    @OnClick({R.id.main_ll_wallet, R.id.main_ll_discivery, R.id.main_ll_my})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.main_ll_wallet:
                isClickLL(true, false, false);
                vpHome.setCurrentItem(0, false);
                break;
            case R.id.main_ll_discivery:
                ToastUtils.showLongToast(activity,R.string.staytuned);
//                isClickLL(false, true, false);
//                vpHome.setCurrentItem(1, false);
                break;
            case R.id.main_ll_my:
                isClickLL(false, false, true);
                vpHome.setCurrentItem(2, false);
                break;
        }
    }

    public void isClickLL(boolean tab01, boolean tab02, boolean tab03) {
        findViewById(R.id.main_ll_wallet).setSelected(tab01);
        findViewById(R.id.main_ll_discivery).setSelected(tab02);
        findViewById(R.id.main_ll_my).setSelected(tab03);
    }

    public void isShowNavigationBar(boolean isShow) {
        if (isShow) {
            mainDaohanglan.setVisibility(View.VISIBLE);
        } else {
            mainDaohanglan.setVisibility(View.GONE);
        }
    }

    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        MyLog.i("**************" + requestCode);
        if (data == null) return;
        switch (requestCode) {
            case Constants.SCANNER_REQUEST:
                String result = data.getStringExtra("result");
                MyLog.i("**************" + result);

                if (!Util.isNullOrEmpty(result)&&Util.isAddressValid(result)){
                    Intent intent =new Intent(this, SendActivity.class);
                    intent.putExtra("toAddress",result);
                    intent.putExtra("iso","SEEK");
                    startActivity(intent);
                }else {
                    ToastUtils.showLongToast(activity,R.string.Send_invalidAddressTitle);
                }

//                if (CryptoUriParser.isCryptoUrl(MainActivity.this, result)) {
//                    CryptoRequest cryptoRequest = CryptoUriParser.parseRequest(MainActivity.this, result);
//                    Intent intent =new Intent(this, SendActivity.class);
//                    intent.putExtra("iso",cryptoRequest.label);
//                    intent.putExtra("money", cryptoRequest.value==null?"":cryptoRequest.value.toPlainString());
//                    intent.putExtra("toAddress",cryptoRequest.address);
//                    startActivity(intent);
//                }
                break;
        }
    }

//    public String getVersionCode() {
//        Context ctx = this.getApplicationContext();
//        PackageManager packageManager = ctx.getPackageManager();
//        PackageInfo packageInfo;
//        String versionCode = "";
//        try {
//            packageInfo = packageManager.getPackageInfo(ctx.getPackageName(), 0);
//            versionCode = packageInfo.versionCode + "";
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
//        return versionCode;
//    }

//    public void checkVersionUpdate() {
//        Context ctx = this.getApplicationContext();
//        AllenVersionChecker
//                .getInstance()
//                .requestVersion()
//                .setRequestMethod(HttpRequestMethod.GET)
//                .setRequestUrl(BaseUrl.versionCheekUrl())
//                .request(new RequestVersionListener() {
//                    @Nullable
//                    @Override
//                    public UIData onRequestVersionSuccess(String result) {
//
//                        try {
//                            if (Util.isNullOrEmpty(result)) {
//                                MyLog.i("onRequestVersionSuccess: 获取新版本失败1");
//                            }
//                            JSONObject json = new JSONObject(result);
//                            MyLog.i("onRequestVersionSuccess: json==" + json);
//                            JSONObject json1 = new JSONObject(json.getString("result"));
//
//                            String dlUrl = json1.getString("url");
//                            String dlContent = json1.getString("content");
//                            String versionCode = json1.getString("versionCode");
//                            String versionName = json1.getString("version");
//
//                            StringBuilder stringBuilder = new StringBuilder();
//                            stringBuilder.append(getString(R.string.current_version));
//                            stringBuilder.append(versionName);
//                            stringBuilder.append("\n");
//                            stringBuilder.append(getString(R.string.update_content));
//                            stringBuilder.append(dlContent);
//
//                            String finalString = stringBuilder.toString();
//
//                            if (Integer.parseInt(versionCode) > Integer.parseInt(getVersionCode())) {
//                                UIData uiData = UIData
//                                        .create()
//                                        .setDownloadUrl(dlUrl)
//                                        .setTitle(getString(R.string.download_latest_version))
//                                        .setContent(finalString);
//                                return uiData;
//                            } else {
//                                return null;
//                            }
//
//                        } catch (Exception e) {
//                            MyLog.i("onRequestVersionSuccess: 获取新版本失败2");
//                        }
//
//                        return null;
//                    }
//
//                    @Override
//                    public void onRequestVersionFailure(String message) {
//
//                    }
//                })
//                .excuteMission(ctx);
//
//    }

}
