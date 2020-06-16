package com.easyetz.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.easyetz.R;
import com.easyetz.utils.MyLog;
import com.easyetz.utils.ToastUtils;
import com.easyetz.utils.Util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class BRDialogView extends DialogFragment {

    private static final String TAG = BRDialogView.class.getName();

    private String title = "";
    private String message = "";
    private String posButton = "";
    private String negButton = "";
    private BRDialogView.BROnClickListener posListener;
    private BRDialogView.BROnClickListener negListener;
    private BRDialogView.BROnClickListener helpListener;
    private DialogInterface.OnDismissListener dismissListener;
    private int iconRes = 0;
    private MButton negativeButton;
    private MButton positiveButton;
    private LinearLayout buttonsLayout;
//    private ImageButton helpButton;
    private LinearLayout mainLayout;

    //provide the way to have clickable span in the message
    private SpannableString spanMessage;

    private boolean showHelpIcon;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.etz_alert_dialog, null);
        TextView titleText = view.findViewById(R.id.dialog_title);
        TextView messageText = view.findViewById(R.id.dialog_text);
        MButton positiveButton = view.findViewById(R.id.pos_button);
        negativeButton = view.findViewById(R.id.neg_button);
//        ImageView icon = (ImageView) view.findViewById(R.id.dialog_icon);
        mainLayout = view.findViewById(R.id.main_layout);
        buttonsLayout = view.findViewById(R.id.linearLayout3);
//        helpButton = view.findViewById(R.id.help_icon);

        //assuming that is the last text to bet set.
        MyLog.i( "showSimpleDialog=="+title+"---"+message);
        if (Util.isNullOrEmpty(title))
            mainLayout.removeView(titleText);
        if (Util.isNullOrEmpty(message))
            mainLayout.removeView(messageText);

        // Resize the title text if it is greater than 4 lines

        titleText.setText(title);
        if (titleText.getLineCount() > 4) {
            titleText.setTextSize(16);
        }


        // Resize the message text if it is greater than 4 lines
        messageText.setText(message);
        if (messageText.getLineCount() > 4) {
            messageText.setTextSize(16);
        }
        if (spanMessage != null) {
            messageText.setText(spanMessage);
            messageText.setMovementMethod(LinkMovementMethod.getInstance());
        }

        positiveButton.setColor(Color.parseColor("#FF30D1A1"));
        positiveButton.setHasShadow(false);
        positiveButton.setText(posButton);
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (!BRAnimator.isClickAllowed()) return;
                if (posListener != null)
                    posListener.onClick(BRDialogView.this);
            }
        });
        if (Util.isNullOrEmpty(negButton)) {
            MyLog.e( "onCreateDialog: removing negative button");
            buttonsLayout.removeView(negativeButton);
            buttonsLayout.requestLayout();

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER_HORIZONTAL;
            params.weight = 1.0f;
            positiveButton.setLayoutParams(params);
        }

        negativeButton.setColor(Color.parseColor("#b3c0c8"));
        negativeButton.setHasShadow(false);
        negativeButton.setText(negButton);
        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (!BRAnimator.isClickAllowed()) return;
                if (negListener != null)
                    negListener.onClick(BRDialogView.this);
            }
        });
//        if (iconRes != 0)
//            icon.setImageResource(iconRes);

        builder.setView(view);

        if (showHelpIcon) {
//            helpButton.setVisibility(View.VISIBLE);

            messageText.setPadding(0, 0, 0, Util.getPixelsFromDps(getActivity(), 16));

//            helpButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    if (!BRAnimator.isClickAllowed()) return;
//                    if (helpListener != null)
//                        helpListener.onClick(BRDialogView.this);
//                }
//            });

        } else {
//            helpButton.setVisibility(View.INVISIBLE);

        }
//        builder.setOnDismissListener(dismissListener);
        // Create the AlertDialog object and return it
        return builder.create();
    }

    public void showHelpIcon(boolean show) {
        this.showHelpIcon = show;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (dismissListener != null)
            dismissListener.onDismiss(dialog);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setSpan(Context app,SpannableString message) {
        if (message == null) {
            ToastUtils.showLongToast( app,"setSpan with null message");
            return;
        }
        this.spanMessage = message;
    }

    public void setPosButton(@NonNull String posButton) {
        this.posButton = posButton;
    }

    public void setNegButton(String negButton) {
        this.negButton = negButton;
    }

    public void setPosListener(BRDialogView.BROnClickListener posListener) {
        this.posListener = posListener;
    }

    public void setNegListener(BRDialogView.BROnClickListener negListener) {
        this.negListener = negListener;
    }

    public void setHelpListener(BROnClickListener helpListener) {
        this.helpListener = helpListener;
    }

    public void setDismissListener(DialogInterface.OnDismissListener dismissListener) {
        this.dismissListener = dismissListener;
    }

    public void setIconRes(int iconRes) {
        this.iconRes = iconRes;
    }

    public static interface BROnClickListener {
        void onClick(BRDialogView brDialogView);
    }

    public void dismissWithAnimation() {
        BRDialogView.this.dismiss();

    }

    @Override
    public void show(FragmentManager manager, String tag) {
//        super.show(manager, tag);
        try {
            Class c= Class.forName("android.app.DialogFragment");
            Constructor con = c.getConstructor();
            Object obj = con.newInstance();
            Field dismissed = c.getDeclaredField(" mDismissed");
            dismissed.setAccessible(true);
            dismissed.set(obj,false);
            Field shownByMe = c.getDeclaredField("mShownByMe");
            shownByMe.setAccessible(true);
            shownByMe.set(obj,false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        FragmentTransaction ft = manager.beginTransaction();
        ft.add(this, tag);
        ft.commitAllowingStateLoss();
    }
}
