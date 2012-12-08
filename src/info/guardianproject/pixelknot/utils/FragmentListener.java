package info.guardianproject.pixelknot.utils;

import info.guardianproject.pixelknot.PixelKnotActivity.PixelKnot;
import android.widget.Button;

public interface FragmentListener {
	public void setButtonOptions(Button[] options);
	public PixelKnot getPixelKnot();
	public void clearPixelKnot();
}
