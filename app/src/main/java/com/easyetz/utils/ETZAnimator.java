package com.easyetz.utils;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import com.easyetz.R;
import com.easyetz.base.Constants;
import com.easyetz.bean.TransactionRecords;
import com.easyetz.modules.scanQr.ScanQRActivity;
import com.easyetz.modules.tools.threads.ETZExecutor;
import com.easyetz.modules.walletoperation.wallet.FragmentTxDetails;
import com.easyetz.view.BRDialogView;
import com.easyetz.view.ETZDialog;



public class ETZAnimator {
    private static final String TAG = ETZAnimator.class.getName();
    private static boolean clickAllowed = true;
    public static final int SLIDE_ANIMATION_DURATION = 300;
    public static void openScanner(Activity app, int requestID) {
        try {
            if (app == null) return;

            // Check if the camera permission is granted
            if (ContextCompat.checkSelfPermission(app,
                    Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(app,
                        Manifest.permission.CAMERA)) {
                    ETZDialog.showCustomDialog(app, app.getString(R.string.Send_cameraUnavailabeTitle_android),
                            app.getString(R.string.Send_cameraUnavailabeMessage_android), app.getString(R.string.AccessibilityLabels_close), null, new BRDialogView.BROnClickListener() {
                                @Override
                                public void onClick(BRDialogView brDialogView) {
                                    brDialogView.dismiss();
                                }
                            }, null, null, 0);
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(app,
                            new String[]{Manifest.permission.CAMERA},
                            Constants.CAMERA_REQUEST_ID);
                }
            } else {
                // Permission is granted, open camera
                Intent intent = new Intent(app, ScanQRActivity.class);
                app.startActivityForResult(intent, requestID);
                app.overridePendingTransition(R.anim.fade_up, R.anim.fade_down);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static LayoutTransition getDefaultTransition() {
        LayoutTransition itemLayoutTransition = new LayoutTransition();
        itemLayoutTransition.setStartDelay(LayoutTransition.APPEARING, 0);
        itemLayoutTransition.setStartDelay(LayoutTransition.DISAPPEARING, 0);
        itemLayoutTransition.setStartDelay(LayoutTransition.CHANGE_APPEARING, 0);
        itemLayoutTransition.setStartDelay(LayoutTransition.CHANGE_DISAPPEARING, 0);
        itemLayoutTransition.setStartDelay(LayoutTransition.CHANGING, 0);
        itemLayoutTransition.setDuration(100);
        itemLayoutTransition.setInterpolator(LayoutTransition.CHANGING, new OvershootInterpolator(2f));
        Animator scaleUp = ObjectAnimator.ofPropertyValuesHolder((Object) null, PropertyValuesHolder.ofFloat(View.SCALE_X, 1, 1), PropertyValuesHolder.ofFloat(View.SCALE_Y, 0, 1));
        scaleUp.setDuration(50);
        scaleUp.setStartDelay(50);
        Animator scaleDown = ObjectAnimator.ofPropertyValuesHolder((Object) null, PropertyValuesHolder.ofFloat(View.SCALE_X, 1, 1), PropertyValuesHolder.ofFloat(View.SCALE_Y, 1, 0));
        scaleDown.setDuration(2);
        itemLayoutTransition.setAnimator(LayoutTransition.APPEARING, scaleUp);
        itemLayoutTransition.setAnimator(LayoutTransition.DISAPPEARING, null);
        itemLayoutTransition.enableTransitionType(LayoutTransition.CHANGING);
        return itemLayoutTransition;
    }



    /**
     * 休眠300毫秒
     * @return
     */
    public static boolean isClickAllowed() {
        if (clickAllowed) {
            clickAllowed = false;
            ETZExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    clickAllowed = true;
                }
            });
            return true;
        } else return false;
    }

    public static void killAllFragments(Activity app) {
        if (app != null && !app.isDestroyed())
            app.getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }



    public static void animateBackgroundDim(final ViewGroup backgroundLayout, boolean reverse) {
        int transColor = reverse ? R.color.black_trans : android.R.color.transparent;
        int blackTransColor = reverse ? android.R.color.transparent : R.color.black_trans;

        ValueAnimator anim = new ValueAnimator();
        anim.setIntValues(transColor, blackTransColor);
        anim.setEvaluator(new ArgbEvaluator());
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                backgroundLayout.setBackgroundColor((Integer) valueAnimator.getAnimatedValue());
            }
        });

        anim.setDuration(SLIDE_ANIMATION_DURATION);
        anim.start();
    }


    public interface OnSlideAnimationEnd {
        void onAnimationEnd();
    }

    public static void showTransactionDetails(Activity app, TransactionRecords item, String iso
    ) {

        FragmentTxDetails txDetails = (FragmentTxDetails) app.getFragmentManager().findFragmentByTag(FragmentTxDetails.class.getName());

        if (txDetails != null && txDetails.isAdded()) {
            MyLog.e( "showTransactionDetails: Already showing");

            return;
        }

        txDetails = new FragmentTxDetails();
        txDetails.setTransaction(item,iso);
        txDetails.show(app.getFragmentManager(), "txDetails");
    }

}