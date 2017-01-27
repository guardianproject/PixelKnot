package info.guardianproject.pixelknot.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AnimationUtils;

import info.guardianproject.pixelknot.R;

public class CircularProgress extends View {

    private int mThickness;
    private Paint mPaintProgress;
    private Paint mPaintBackground;
    private RectF mRect;
    private int mMax;
    private int mProgress;
    private float mAnimationRotation;
    private boolean mIsAnimating;

    public CircularProgress(Context context) {
        super(context);
        init(context, null);
    }

    public CircularProgress(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CircularProgress(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @SuppressLint("NewApi")
    public CircularProgress(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        mAnimationRotation = 0f;
        mThickness = 10;

        int progressColor = Color.TRANSPARENT;
        int backgroundColor = Color.TRANSPARENT;

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircularProgress);
            if (a != null) {
                progressColor = a.getColor(R.styleable.CircularProgress_colorProgress, Color.TRANSPARENT);
                backgroundColor = a.getColor(R.styleable.CircularProgress_colorBackground, Color.TRANSPARENT);
                mThickness = a.getDimensionPixelSize(R.styleable.CircularProgress_thickness, mThickness);
                a.recycle();
            }
        }

        mPaintProgress = new Paint();
        mPaintProgress.setColor(progressColor);
        mPaintProgress.setStyle(Paint.Style.STROKE);
        mPaintProgress.setStrokeCap(Paint.Cap.ROUND);
        mPaintProgress.setStrokeWidth(mThickness);
        mPaintProgress.setAntiAlias(true);

        mPaintBackground = new Paint();
        mPaintBackground.setColor(backgroundColor);
        mPaintBackground.setStyle(Paint.Style.STROKE);
        mPaintBackground.setStrokeWidth(mThickness);
        mPaintBackground.setAntiAlias(true);

        mRect = new RectF();
        setWillNotDraw(false);
    }

    public void setMax(int max) {
        mMax = max;
        postInvalidate();
    }
    public void setProgress(int progress) {
        mProgress = progress;
        postInvalidate();
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        int padding = mThickness / 2;
        mRect.set(padding, padding, getWidth() - padding, getHeight() - padding);
        canvas.drawOval(mRect, mPaintBackground);
        if (mMax > 0 && mProgress > 0) {
            float ratio = (float)mProgress / (float)mMax;
            canvas.drawArc(mRect, 270 + mAnimationRotation, 360f * ratio, false, mPaintProgress);
        }
    }

    private final Runnable mAnimationRunnable = new Runnable() {
        @Override
        public void run() {
            long ms = AnimationUtils.currentAnimationTimeMillis();
            long rem = ms % 2000;
            mAnimationRotation = 360f * (rem / 2000f);
            invalidate();
            if (mIsAnimating)
                postDelayed(mAnimationRunnable, 20);
        }
    };

    public void startAnimating() {
        if (!mIsAnimating) {
            mIsAnimating = true;
            post(mAnimationRunnable);
        }
    }

    public void stopAnimating() {
        if (mIsAnimating) {
            mIsAnimating = false;
            removeCallbacks(mAnimationRunnable);
            mAnimationRotation = 0;
            invalidate();
        }
    }
}