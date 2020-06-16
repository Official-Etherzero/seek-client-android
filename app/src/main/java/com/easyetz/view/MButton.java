package com.easyetz.view;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;

import com.easyetz.R;
import com.easyetz.utils.FontManagerUtil;
import com.easyetz.utils.Util;


@SuppressLint("AppCompatCustomView") // we don't need to support older versions
public class MButton extends Button {
    private static final String TAG = MButton.class.getName();
    private static int ANIMATION_DURATION = 30;
    private Bitmap shadow;
    private Rect shadowRect;
    private RectF bRect;
    private int width;
    private int height;
    private int modifiedWidth;
    private int modifiedHeight;
    private Paint bPaint;
    private Paint bPaintStroke;
    private int type = 2;
    private static final float SHADOW_PRESSED = 0.88f;
    private static final float SHADOW_UNPRESSED = 0.95f;
    private float shadowOffSet = SHADOW_UNPRESSED;
    private static final int ROUND_PIXELS = 16;
    private boolean isBreadButton; //meaning is has the special animation and shadow
    private boolean hasShadow; // allows us to add/remove the drop shadow from the button without affecting the animation

    public MButton(Context context) {
        super(context);
        init(context, null);
    }

    public MButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public MButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context ctx, AttributeSet attrs) {
        shadow = BitmapFactory.decodeResource(getResources(), R.mipmap.shadow);
        bPaint = new Paint();
        bPaintStroke = new Paint();
        shadowRect = new Rect(0, 0, 100, 100);
        bRect = new RectF(0, 0, 100, 100);
        TypedArray a = ctx.obtainStyledAttributes(attrs, R.styleable.MButton);
        String customFont = a.getString(R.styleable.MButton_customBFont);
        FontManagerUtil.setCustomFont(ctx, this, Util.isNullOrEmpty(customFont) ? "CircularPro-Medium.otf" : customFont);
        float px16 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, getResources().getDisplayMetrics());
        //check attributes you need, for example all paddings
        int[] attributes = new int[]{android.R.attr.paddingStart, android.R.attr.paddingTop, android.R.attr.paddingEnd, android.R.attr.paddingBottom, R.attr.isBreadButton, R.attr.buttonType};
        //then obtain typed array
        TypedArray arr = ctx.obtainStyledAttributes(attrs, attributes);
        //You can check if attribute exists (in this example checking paddingRight)

        isBreadButton = a.getBoolean(R.styleable.MButton_isBreadButton, false);
        int paddingLeft = arr.hasValue(0) ? arr.getDimensionPixelOffset(0, -1) : (int) px16;
        int paddingTop = arr.hasValue(1) ? arr.getDimensionPixelOffset(1, -1) : 0;
        int paddingRight = arr.hasValue(2) ? arr.getDimensionPixelOffset(2, -1) : (int) px16;
        int paddingBottom = arr.hasValue(3) ? arr.getDimensionPixelOffset(3, -1) + (isBreadButton ? (int) px16 : 0) : (isBreadButton ? (int) px16 : 0);
        hasShadow = a.getBoolean(R.styleable.MButton_hasShadow, true);

        int type = a.getInteger(R.styleable.MButton_buttonType, 0);
        setType(type);

        bPaint.setAntiAlias(true);
        bPaintStroke.setAntiAlias(true);

        if (isBreadButton) {
            setBackground(getContext().getDrawable(R.drawable.shadow_trans));
        }

        setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
        a.recycle();
        arr.recycle();
        final ViewTreeObserver observer = getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (observer.isAlive()) {
                    observer.removeOnGlobalLayoutListener(this);
                }
                Util.correctTextSizeIfNeeded(MButton.this);
                correctTextBalance();
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isBreadButton) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                }
                if (type != 3)
                    press(ANIMATION_DURATION);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                unPress(ANIMATION_DURATION);
            }
        }

        return super.onTouchEvent(event);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;

    }

    private void correctTextBalance() {
        //implement if needed in the future
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isBreadButton) {
            if (hasShadow) {
                shadowRect.set(5, height / 4, width - 5, (int) (height * shadowOffSet));
                canvas.drawBitmap(shadow, null, shadowRect, null);

            }
            modifiedWidth = width - 10;
            modifiedHeight = height - height / 4 - 5;
            bRect.set(5, 5, modifiedWidth, modifiedHeight + 5);
            canvas.drawRoundRect(bRect, ROUND_PIXELS, ROUND_PIXELS, bPaint);
            if (type == 2 || type == 3)
                canvas.drawRoundRect(bRect, ROUND_PIXELS, ROUND_PIXELS, bPaintStroke);
        }
        super.onDraw(canvas);

    }

    public void setHasShadow(boolean hasShadow) {
        this.hasShadow = hasShadow;
        invalidate();
    }

    public void setColor(int color) {
        bPaint.setColor(color);
        invalidate();
    }

    public void setType(int type) {
        if (type == 3) press(1);
        this.type = type;

        if (type == 1) { //blue
            bPaint.setColor(getResources().getColor(R.color.button_primary_normal));
            setTextColor(getResources().getColor(R.color.white));
        } else if (type == 2) { //gray stroke
            bPaintStroke.setColor(getResources().getColor(R.color.white));
            bPaintStroke.setStyle(Paint.Style.STROKE);
            bPaintStroke.setStrokeWidth(Util.getPixelsFromDps(getContext(), 1));
            setTextColor(getResources().getColor(R.color.zt_lu));
            bPaint.setColor(getResources().getColor(R.color.white));
            bPaint.setStyle(Paint.Style.FILL);
        } else if (type == 3) { //blue strokeww
            bPaintStroke.setColor(getResources().getColor(R.color.button_primary_normal));
            bPaintStroke.setStyle(Paint.Style.STROKE);
            bPaintStroke.setStrokeWidth(Util.getPixelsFromDps(getContext(), 1));
            setTextColor(getResources().getColor(R.color.button_primary_normal));
            bPaint.setColor(getResources().getColor(R.color.button_secondary));
            bPaint.setStyle(Paint.Style.FILL);
        } else if (type == 4) {
            bPaintStroke.setColor(getResources().getColor(R.color.btn_bg_lu));
            bPaintStroke.setStyle(Paint.Style.STROKE);
            bPaintStroke.setStrokeWidth(Util.getPixelsFromDps(getContext(), 1));
            setTextColor(getResources().getColor(R.color.white));
            bPaint.setColor(getResources().getColor(R.color.btn_bg_lu));
            bPaint.setStyle(Paint.Style.FILL);
        } else if (type == 5) {
            bPaintStroke.setColor(getResources().getColor(R.color.blue));
            bPaintStroke.setStyle(Paint.Style.STROKE);
            bPaintStroke.setStrokeWidth(Util.getPixelsFromDps(getContext(), 1));
            setTextColor(getResources().getColor(R.color.blue));
            bPaint.setColor(getResources().getColor(R.color.white));
            bPaint.setStyle(Paint.Style.FILL);

        } else if (type == 6) { //blue
            bPaint.setColor(getResources().getColor(R.color.red));
            setTextColor(getResources().getColor(R.color.white));
        }
        invalidate();
    }

    public void makeGradient(int startColor, int endColor) {
        bPaint.setShader(new LinearGradient(0, 0, getWidth(), 0, startColor, endColor, Shader.TileMode.MIRROR));
        invalidate();
    }

    private void press(int duration) {
        ScaleAnimation scaleAnim = new ScaleAnimation(
                1f, 0.96f,
                1f, 0.96f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 1f);
        scaleAnim.setDuration(duration);
        scaleAnim.setRepeatCount(0);
        scaleAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleAnim.setFillAfter(true);
        scaleAnim.setFillBefore(true);
        scaleAnim.setFillEnabled(true);

        ValueAnimator shadowAnim = ValueAnimator.ofFloat(SHADOW_UNPRESSED, SHADOW_PRESSED);
        shadowAnim.setDuration(duration);
        shadowAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                shadowOffSet = (float) animation.getAnimatedValue();
                invalidate();
            }
        });

        startAnimation(scaleAnim);
        shadowAnim.start();

    }

    private void unPress(int duration) {
        ScaleAnimation scaleAnim = new ScaleAnimation(
                0.96f, 1f,
                0.96f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 1f);
        scaleAnim.setDuration(duration);
        scaleAnim.setRepeatCount(0);
        scaleAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleAnim.setFillAfter(true);
        scaleAnim.setFillBefore(true);
        scaleAnim.setFillEnabled(true);

        ValueAnimator shadowAnim = ValueAnimator.ofFloat(SHADOW_PRESSED, SHADOW_UNPRESSED);
        shadowAnim.setDuration(duration);
        shadowAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                shadowOffSet = (float) animation.getAnimatedValue();
                invalidate();
            }
        });

        startAnimation(scaleAnim);
        shadowAnim.start();
    }
}
