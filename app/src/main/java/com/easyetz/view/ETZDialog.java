package com.easyetz.view;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.text.SpannableString;

import com.easyetz.R;
import com.easyetz.modules.tools.threads.ETZExecutor;
import com.easyetz.utils.MyLog;

/**
 * BreadWallet
 * <p/>
 * Created by Mihail Gutan on <mihail@breadwallet.com> 3/15/17.
 * Copyright (c) 2017 breadwallet LLC
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
public class ETZDialog {
    private static final String TAG = ETZDialog.class.getName();
    private static BRDialogView dialog;

    /**
     * Safe from any threads
     *
     * @param app needs to be activity
     */
    public static void showCustomDialog(@NonNull final Context app, @NonNull final String title, @NonNull final String message,
                                        @NonNull final String posButton, final String negButton, final BRDialogView.BROnClickListener posListener,
                                        final BRDialogView.BROnClickListener negListener, final DialogInterface.OnDismissListener dismissListener, final int iconRes) {
        if (((Activity) app).isDestroyed()) {
            MyLog.e("showCustomDialog: FAILED, context is destroyed");
            return;
        }
        ETZExecutor.getInstance().forMainThreadTasks().execute(new Runnable() {
            @Override
            public void run() {
                dialog = new BRDialogView();
                dialog.setTitle(title);
                dialog.setMessage(message);
                dialog.setPosButton(posButton);
                dialog.setNegButton(negButton);
                dialog.setPosListener(posListener);
                dialog.setNegListener(negListener);
                dialog.setDismissListener(dismissListener);
                dialog.setIconRes(iconRes);
                if (!((Activity) app).isDestroyed())
                    dialog.show(((Activity) app).getFragmentManager(), dialog.getClass().getName());
            }
        });

    }

    public static void showHelpDialog(@NonNull final Context app, @NonNull final String title, @NonNull final String message, @NonNull final String posButton, @NonNull final String negButton, final BRDialogView.BROnClickListener posListener, final BRDialogView.BROnClickListener negListener, final BRDialogView.BROnClickListener helpListener) {

        if (((Activity) app).isDestroyed()) {
            MyLog.e( "showCustomDialog: FAILED, context is destroyed");
            return;
        }

        ETZExecutor.getInstance().forMainThreadTasks().execute(new Runnable() {
            @Override
            public void run() {
                dialog = new BRDialogView();
                dialog.setTitle(title);
                dialog.setMessage(message);
                dialog.setPosButton(posButton);
                dialog.setNegButton(negButton);
                dialog.setPosListener(posListener);
                dialog.setNegListener(negListener);
                dialog.setHelpListener(helpListener);
                dialog.showHelpIcon(true);
                dialog.show(((Activity) app).getFragmentManager(), dialog.getClass().getName());

            }
        });
    }

    public static void showSimpleDialog(@NonNull final Context app, @NonNull final String title, @NonNull final String message) {
        showCustomDialog(app, title, message, app.getString(R.string.AccessibilityLabels_close), null, new BRDialogView.BROnClickListener() {
            @Override
            public void onClick(BRDialogView brDialogView) {
                brDialogView.dismissWithAnimation();
            }
        }, null, null, 0);
    }

    //same but with a SpannableString as message to be able to click on a portion of the text with a listener
    public static void showCustomDialog(@NonNull final Context app, @NonNull final String title, @NonNull final SpannableString message,
                                        @NonNull final String posButton, final String negButton, final BRDialogView.BROnClickListener posListener,
                                        final BRDialogView.BROnClickListener negListener, final DialogInterface.OnDismissListener dismissListener, final int iconRes) {
        if (((Activity) app).isDestroyed()) {
            MyLog.e( "showCustomDialog: FAILED, context is destroyed");
            return;
        }

        ETZExecutor.getInstance().forMainThreadTasks().execute(new Runnable() {
            @Override
            public void run() {
                dialog = new BRDialogView();
                dialog.setTitle(title);
                dialog.setSpan(app,message);//setting Span instead of String
                dialog.setMessage(message.toString());
                dialog.setPosButton(posButton);
                dialog.setNegButton(negButton);
                dialog.setPosListener(posListener);
                dialog.setNegListener(negListener);
                dialog.setDismissListener(dismissListener);
                dialog.setIconRes(iconRes);
                dialog.show(((Activity) app).getFragmentManager(), dialog.getClass().getName());
            }
        });
    }

    public static void hideDialog() {
        if (dialog != null) dialog.dismiss();
    }
}
