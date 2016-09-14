package info.guardianproject.pixelknot.views;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
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

    public void setDisclosureIndicator(int resId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, resId, 0);
            setTextAlignment(TEXT_ALIGNMENT_VIEW_START);
        } else {
            setCompoundDrawablesWithIntrinsicBounds(0, 0, resId, 0);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            // RTL means it can end up on left or right. Doesn't matter, we only have one drawable set, so find it...
            Drawable[] drawables = getCompoundDrawables();
            if (Build.VERSION.SDK_INT >= 17)
                drawables = getCompoundDrawablesRelative();
            Drawable disclosureDrawable = null;
            for (Drawable d : drawables) {
                if (d != null) {
                    disclosureDrawable = d;
                    break;
                }
            }
            if (disclosureDrawable != null) {
                int x = (int)event.getX();
                int dir = ViewCompat.getLayoutDirection(this);
                if ((dir == ViewCompat.LAYOUT_DIRECTION_LTR && x >= (getRight() - disclosureDrawable.getBounds().width()))
                        ||
                        (dir == ViewCompat.LAYOUT_DIRECTION_RTL && x <= disclosureDrawable.getBounds().width())) {
                    if ((getInputType() & InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
                        setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    else
                        setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    return true;
                }
            }
        }
        return super.onTouchEvent(event);
    }
}
