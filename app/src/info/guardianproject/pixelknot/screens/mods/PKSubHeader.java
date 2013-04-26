package info.guardianproject.pixelknot.screens.mods;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class PKSubHeader extends TextView {
	Context c;
	private static Typeface t;
	private boolean isSet = false;
	
	public PKSubHeader(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.c = context;
		
		doType();
		
	}
	
	private void doType ()
	{
		if (!isSet)
		{
			if(t == null)
				t = Typeface.createFromAsset(this.c.getAssets(), "vladimir.ttf");
			
			setTypeface(t);
			isSet = true;
			
		}
	}
	
	public PKSubHeader(Context context) {
		super(context);
		this.c = context;
		
		doType();
	}
	
	@Override
	public void setTypeface(Typeface typeface) {
		super.setTypeface(typeface);
	}

}
