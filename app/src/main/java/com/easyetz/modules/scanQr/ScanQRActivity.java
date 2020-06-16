package com.easyetz.modules.scanQr;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v13.app.ActivityCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.easyetz.R;
import com.easyetz.base.BaseActivity;
import com.easyetz.modules.main.discovery.FragmentDiscovery;
import com.easyetz.modules.normalvp.NormalPresenter;
import com.easyetz.modules.normalvp.NormalView;
import com.easyetz.utils.CryptoUriParser;
import com.easyetz.utils.MyLog;
import com.easyetz.utils.SharedPrefsUitls;
import com.easyetz.utils.SpringAnimator;
import com.easyetz.view.QRCodeReaderView;

import butterknife.ButterKnife;
import butterknife.OnClick;


public class ScanQRActivity extends BaseActivity<NormalView, NormalPresenter> implements ActivityCompat.OnRequestPermissionsResultCallback, QRCodeReaderView.OnQRCodeReadListener {
    private static final String TAG = ScanQRActivity.class.getName();
    private ImageView cameraGuide;
    private TextView descriptionText;
    private long lastUpdated;
    private UIUpdateTask task;
    private boolean handlingCode;
    public static boolean appVisible = false;
    private static ScanQRActivity app;
    private static final int MY_PERMISSION_REQUEST_CAMERA = 56432;

    private ViewGroup mainLayout;

    private QRCodeReaderView qrCodeReaderView;

    public static ScanQRActivity getApp() {
        return app;
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_qr;
    }

    @Override
    public NormalPresenter initPresenter() {
        return new NormalPresenter();
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        setCenterTitle(getResources().getString(R.string.property_scan));
        cameraGuide = (ImageView) findViewById(R.id.scan_guide);
        descriptionText = (TextView) findViewById(R.id.description_text);
        task = new UIUpdateTask();
        task.start();

        cameraGuide.setImageResource(R.mipmap.cameraguide);
        cameraGuide.setVisibility(View.GONE);

        if (android.support.v4.app.ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            initQRCodeReaderView();
        } else {
//            requestCameraPermission();
            MyLog.e("onCreate: Permissions needed? HUH?");
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                cameraGuide.setVisibility(View.VISIBLE);
                SpringAnimator.showExpandCameraGuide(cameraGuide);
            }
        }, 400);
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
        appVisible = true;
        app = this;
        if (qrCodeReaderView != null) {
            qrCodeReaderView.startCamera();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        appVisible = false;
        if (qrCodeReaderView != null) {
            qrCodeReaderView.stopCamera();
        }
        task.stopTask();
    }

    @Override
    public void onBackPressed() {
        overridePendingTransition(R.anim.fade_down, 0);
        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }

    @OnClick(R.id.scan_cancel)
    public void onViewClicked() {
        finish();
    }

    private class UIUpdateTask extends Thread {
        public boolean running = true;

        @Override
        public void run() {
            super.run();
            while (running) {
                if (System.currentTimeMillis() - lastUpdated > 300) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            cameraGuide.setImageResource(R.mipmap.cameraguide);
                            descriptionText.setText("");
                        }
                    });
                }
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void stopTask() {
            running = false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != MY_PERMISSION_REQUEST_CAMERA) {
            return;
        }

        if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            Snackbar.make(mainLayout, "Camera permission was granted.", Snackbar.LENGTH_SHORT).show();
            initQRCodeReaderView();
        } else {
//            Snackbar.make(mainLayout, "Camera permission request was denied.", Snackbar.LENGTH_SHORT)
//                    .show();
        }
    }

    @Override
    public void onQRCodeRead(final String text, PointF[] points) {
        lastUpdated = System.currentTimeMillis();
        if (handlingCode) return;
        handlingCode = true;
        if (text.contains("http") && FragmentDiscovery.fd != null) {//扫描加载DAPP
            FragmentDiscovery.fd.initUrl(text);
            finish();
        } else if (CryptoUriParser.isCryptoUrl(this, text)) {//扫描地址转帐
            MyLog.e("onQRCodeRead: isCrypto");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        cameraGuide.setImageResource(R.mipmap.cameraguide);
                        descriptionText.setText("");
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("result", text);
                        setResult(Activity.RESULT_OK, returnIntent);
                        finish();
                    } finally {
                        handlingCode = false;
                    }

                }
            });
        } else {
            MyLog.e("onQRCodeRead: not a crypto url");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        cameraGuide.setImageResource(R.mipmap.cameraguide_red);
                        lastUpdated = System.currentTimeMillis();
                        descriptionText.setText("Not a valid " + SharedPrefsUitls.getInstance().getCurrentWallet() + " address");
                    } finally {
                        handlingCode = false;
                    }
                }
            });

        }

    }

    private void initQRCodeReaderView() {
        qrCodeReaderView = findViewById(R.id.qrdecoderview);
        qrCodeReaderView.setAutofocusInterval(500L);
        qrCodeReaderView.setOnQRCodeReadListener(this);
        qrCodeReaderView.setBackCamera();
        qrCodeReaderView.startCamera();
    }

}