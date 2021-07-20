package info.guardianproject.pixelknot.views;


import android.content.Context;
import com.google.android.material.textfield.TextInputEditText;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;

public class FadingEditText extends TextInputEditText {

    public interface OnBackListener {
        boolean onBackPressed(FadingEditText textView);
    }
    private OnBackListener mOnBackListener;

    public FadingEditText(Context context) {
        super(context);
    }

    public FadingEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FadingEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setOnBackListener(OnBackListener listener) {
        mOnBackListener = listener;
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            if (mOnBackListener != null) {
                if (mOnBackListener.onBackPressed(this))
                    return true; // dont close keyboard
            }
        }
        return super.onKeyPreIme(keyCode, event);
    }

    @Override
    public void setVisibility(int visibility) {
        int oldVisibility = getVisibility();
        if (visibility != oldVisibility) {
            if (visibility == View.VISIBLE) {
                showView();
            } else if (visibility == View.INVISIBLE) {
                hideView(false);
            } else {
                hideView(true);
            }
        }
    }

    private void showView() {
        Animation animAlpha = new AlphaAnimation(0, 1);
        animAlpha.setInterpolator(new AccelerateInterpolator());
        animAlpha.setDuration(1000);

        animAlpha.setAnimationListener(new Animation.AnimationListener()
        {
            public void onAnimationEnd(Animation animation)
            {
            }

            public void onAnimationRepeat(Animation animation) {}
            public void onAnimationStart(Animation animation) {
            }
        });
        setEnabled(true);
        FadingEditText.super.setVisibility(View.VISIBLE);
        startAnimation(animAlpha);
    }

    private void hideView(final boolean setToGone) {
        Animation animAlpha = new AlphaAnimation(1, 0);
        animAlpha.setInterpolator(new AccelerateInterpolator());
        animAlpha.setDuration(1000);

        animAlpha.setAnimationListener(new Animation.AnimationListener()
        {
            public void onAnimationEnd(Animation animation)
            {
                if (setToGone)
                    FadingEditText.super.setVisibility(View.GONE);
                else
                    FadingEditText.super.setVisibility(View.INVISIBLE);
            }

            public void onAnimationRepeat(Animation animation) {}
            public void onAnimationStart(Animation animation) {
            }
        });
        setEnabled(false);
        startAnimation(animAlpha);
    }
}
