package info.guardianproject.pixelknot.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import info.guardianproject.pixelknot.R;

public class RoundedImageView extends View implements Target {

    private Paint mPaint;
    private RectF mViewRect;
    private float mRounding = 0;
    private float mLightFilter = 0;
    private Shader mShader;
    private Bitmap mBitmap;

    public RoundedImageView(Context context) {
        super(context);
        init(context, null);
    }

    public RoundedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RoundedImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RoundedImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mPaint = null;

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RoundedImageView);
            if (a != null) {
                mRounding = a.getFloat(R.styleable.RoundedImageView_rounding, 0);
                mLightFilter = a.getFraction(R.styleable.RoundedImageView_lightFilter, 1, 1, 0);
                a.recycle();
            }
        }
        // Make sure this works on old devices, see https://developer.android.com/guide/topics/graphics/hardware-accel.html
        if (Build.VERSION.SDK_INT < 17)
            setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        update();
    }

    public float getRounding() {
        return mRounding;
    }

    public void setRounding(float rounding) {
        mRounding = rounding;
        update();
        postInvalidate();
    }

    public float getLightFilter() { return mLightFilter; }

    public void setLightFilter(float value) {
        mLightFilter = value;
        updateLightFilter();
        postInvalidate();
    }

    private void update() {
        mViewRect = new RectF(0.0f, 0.0f, getWidth(), getHeight());
        updateShader();
    }

    private void updateShader() {
        if (mBitmap != null && mShader != null) {
            Matrix matrix = new Matrix();

            float dx = 0;
            float dy = 0;
            float scale = 1f;
            if (mBitmap.getWidth() * getHeight() > getWidth() * mBitmap.getHeight()) {
                scale = (float)getHeight() / (float) mBitmap.getHeight();
                dx = (getWidth() - mBitmap.getWidth() * scale) * 0.5f;
            } else {
                scale = (float)getWidth() / (float) mBitmap.getWidth();
                dy = (getHeight() - mBitmap.getHeight() * scale) * 0.5f;
            }
            matrix.setScale(scale, scale);
            matrix.postTranslate((int) (dx + 0.5f),
                    (int) (dy + 0.5f));
            mShader.setLocalMatrix(matrix);
        }
    }

    private void updateLightFilter() {
        if (mPaint != null) {
            if (mLightFilter == 0) {
                mPaint.setColorFilter(null);
            }
            else {
                int color = Color.argb((int)(255f * mLightFilter), 255, 255, 255);
                mPaint.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.LIGHTEN));
            }
        }
    }

    public void setImageBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
        if (bitmap == null || bitmap.isRecycled()) {
            if (mPaint != null) {
                mPaint.setShader(null);
                mShader = null;
                mPaint = null;
            }
        } else {
            mShader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setShader(mShader);
            updateLightFilter();
        }
        updateShader();
        postInvalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mPaint != null && mViewRect != null)
            canvas.drawRoundRect(mViewRect, getRounding() * ((float)getWidth() / 2f), getRounding() * ((float)getHeight() / 2f), mPaint);
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom loadedFrom) {
        setImageBitmap(bitmap);
    }

    @Override
    public void onBitmapFailed(Drawable drawable) {
        Log.d("LOG", "Failed");
    }

    @Override
    public void onPrepareLoad(Drawable drawable) {

    }
}
