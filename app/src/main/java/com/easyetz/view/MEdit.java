package com.easyetz.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.EditText;

import com.easyetz.R;
import com.easyetz.utils.FontManagerUtil;
import com.easyetz.utils.Util;

/**
 * EditText
 */
@SuppressLint("AppCompatCustomView") // we don't need to support older versions
public class MEdit extends EditText {
    private static final String TAG = MEdit.class.getName();
    private final int ANIMATION_DURATION = 200;
    private int currentX = 0;
    private int currentY = 0;
    private boolean isBreadButton; //meaning is has the special animation and shadow

    public MEdit(Context context) {
        super(context);
    }

    public MEdit(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MEdit(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public MEdit(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context ctx, AttributeSet attrs) {
        TypedArray a = ctx.obtainStyledAttributes(attrs, R.styleable.MEdit);
        String customFont = a.getString(R.styleable.MEdit_customEFont);
        FontManagerUtil.setCustomFont(ctx, this, Util.isNullOrEmpty(customFont) ? "CircularPro-Medium.otf" : customFont);
        a.recycle();
    }

}
