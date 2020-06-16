package com.easyetz.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.TextView;

import com.easyetz.R;
import com.easyetz.utils.FontManagerUtil;
import com.easyetz.utils.Util;

/**
 * TextView
 */
@SuppressLint("AppCompatCustomView") // we don't need to support older versions
public class MText extends TextView {
    private static final String TAG = MText.class.getName();

    public MText(Context context) {
        super(context);
    }

    public MText(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MText(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public MText(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context ctx, AttributeSet attrs) {
        TypedArray a = ctx.obtainStyledAttributes(attrs, R.styleable.MText);
        String customFont = a.getString(R.styleable.MText_customTFont);
        FontManagerUtil.setCustomFont(ctx, this, Util.isNullOrEmpty(customFont) ? "CircularPro-Book.otf" : customFont);
        a.recycle();
        setLineSpacing(0, 1.3f);

        //setTextDirection(TEXT_DIRECTION_LTR);
    }

}
