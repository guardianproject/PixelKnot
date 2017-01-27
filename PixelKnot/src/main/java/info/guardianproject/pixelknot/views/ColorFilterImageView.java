package info.guardianproject.pixelknot.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;

import info.guardianproject.pixelknot.R;

/**
 * An ImageView with a color filter applied on top
 */
public class ColorFilterImageView extends ImageView {

    private int mFilterStartColor;
    private int mFilterEndColor;
    private float mFilterCurrent;

    public ColorFilterImageView(Context context) {
        super(context);
        init(context, null);
    }

    public ColorFilterImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ColorFilterImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ColorFilterImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mFilterCurrent = 0;
        mFilterStartColor = Color.TRANSPARENT;
        mFilterEndColor = Color.TRANSPARENT;
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ColorFilterImageView);
            if (a != null) {
                mFilterCurrent = a.getFloat(R.styleable.ColorFilterImageView_filterCurrent, 0f);
                mFilterStartColor = a.getColor(R.styleable.ColorFilterImageView_filterStartColor, Color.TRANSPARENT);
                mFilterEndColor = a.getColor(R.styleable.ColorFilterImageView_filterEndColor, Color.TRANSPARENT);
                a.recycle();
            }
        }
    }

    public float getFilterCurrent() {
        return mFilterCurrent;
    }

    public void setFilterCurrent(float filterCurrent) {
        mFilterCurrent = filterCurrent;
        postInvalidate();
    }

    public int getFilterStartColor() { return mFilterStartColor; }

    public void setFilterStartColor(int filterStartColor) {
        mFilterStartColor = filterStartColor;
    }

    public int getFilterEndColor() { return mFilterEndColor; }

    public void setFilterEndColor(int filterEndColor) {
        mFilterEndColor = filterEndColor;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mFilterStartColor != Color.TRANSPARENT || mFilterEndColor != Color.TRANSPARENT) {
            int a = mixColorComponent(Color.alpha(mFilterStartColor), Color.alpha(mFilterEndColor), mFilterCurrent);
            int r = mixColorComponent(Color.red(mFilterStartColor), Color.red(mFilterEndColor), mFilterCurrent);
            int g = mixColorComponent(Color.green(mFilterStartColor), Color.green(mFilterEndColor), mFilterCurrent);
            int b = mixColorComponent(Color.blue(mFilterStartColor), Color.blue(mFilterEndColor), mFilterCurrent);
            int color = Color.argb(a, r, g, b);
            canvas.drawColor(color);
        }
    }

    private int mixColorComponent(int c1, int c2, float mix) {
        float diff = c2 - c1;
        return c1 + (int)(mix * diff);
    }
}
