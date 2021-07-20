package info.guardianproject.pixelknot.views;


import android.content.Context;
import android.os.Build;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import android.text.InputType;
import android.util.AttributeSet;

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
