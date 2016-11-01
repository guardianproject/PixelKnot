package info.guardianproject.pixelknot.views;


import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewCompat;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.MotionEvent;

import info.guardianproject.pixelknot.R;

public class FadingPasswordEditText extends FadingEditText {

    public FadingPasswordEditText(Context context) {
        super(context);
        init(context);
    }

    public FadingPasswordEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FadingPasswordEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            setTextAlignment(TEXT_ALIGNMENT_VIEW_START);
        }
    }
}
