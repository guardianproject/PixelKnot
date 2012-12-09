package info.guardianproject.pixelknot.utils;

import info.guardianproject.pixelknot.PixelKnotActivity.PixelKnot;
import android.widget.Button;

public interface FragmentListener {
	public void setButtonOptions(Button[] options);
	public PixelKnot getPixelKnot();
	public void clearPixelKnot();
	public boolean getHasSeenFirstPage();
	public void setHasSeenFirstPage(boolean hasSeenFirstPage);
	public boolean getHasSuccessfullyEmbed();
	public void setHasSuccessfullyEmbed(boolean hasSuccessfullyEmbed);
	public boolean getHasSuccessfullyExtracted();
	public void setHasSuccessfullyExtracted(boolean hasSuccessfullyExtracted);
	public void share();
}
