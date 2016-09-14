package info.guardianproject.pixelknot.views;

import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.widget.ImageView;

public class TopCroppedImageView extends ImageView {
    private Matrix mMatrix;
    private boolean mHasFrame;

    public TopCroppedImageView(Context context) {
        this(context, null, 0);
    }

    public TopCroppedImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TopCroppedImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mHasFrame = false;
        mMatrix = new Matrix();
    }

    @Override
    protected boolean setFrame(int l, int t, int r, int b) {
        float width = r - l;
        float height = b - t;
        if (getDrawable() != null&& getDrawable().getIntrinsicWidth() > 0 && getDrawable().getIntrinsicHeight() > 0) {
            Matrix matrix = getImageMatrix();
            float scaleFactor, scaleFactorWidth, scaleFactorHeight;
            scaleFactorWidth = (float) width / (float) getDrawable().getIntrinsicWidth();
            scaleFactorHeight = (float) height / (float) getDrawable().getIntrinsicHeight();

            if (scaleFactorHeight > scaleFactorWidth) {
                scaleFactor = scaleFactorHeight;
            } else {
                scaleFactor = scaleFactorWidth;
            }

            matrix.setScale(scaleFactor, scaleFactor, 0, 0);
            setImageMatrix(matrix);
        }
        return super.setFrame(l, t, r, b);
    }

}