package info.guardianproject.pixelknot.utils;

import java.util.List;

import info.guardianproject.pixelknot.PixelKnotActivity.PixelKnot;
import info.guardianproject.pixelknot.PixelKnotActivity.TrustedShareActivity;
import info.guardianproject.pixelknot.crypto.Apg;
import android.widget.ImageButton;

public interface FragmentListener {
	public void setButtonOptions(ImageButton[] options);
	public PixelKnot getPixelKnot();
	public void clearPixelKnot();
	public boolean getHasSeenFirstPage();
	public void setHasSeenFirstPage(boolean hasSeenFirstPage);
	public boolean getHasSuccessfullyEmbed();
	public void setHasSuccessfullyEmbed(boolean hasSuccessfullyEmbed);
	public boolean getHasSuccessfullyExtracted();
	public void setHasSuccessfullyExtracted(boolean hasSuccessfullyExtracted);
	public boolean getHasSuccessfullyEncrypted();
	public void setHasSuccessfullyEncrypted(boolean hasSuccessfullyEncrypted);
	public boolean getHasSuccessfullyDecrypted();
	public void setHasSuccessfullyDecrypted(boolean hasSuccessfullyDecrypted);
	public boolean getHasSuccessfullyPasswordProtected();
	public void setHasSuccessfullyPasswordProtected(boolean hasSuccessfullyPasswordProtected);
	public boolean getHasSuccessfullyUnlocked();
	public void setHasSuccessfullyUnlocked(boolean hasSuccessfullyUnlocked);
	public void share();
	public List<TrustedShareActivity> getTrustedShareActivities();
	public void setEncryption(Apg apg);
	public void updateButtonProminence(int which, int new_resource);
	public void autoAdvance();
	public void autoAdvance(int position);
}
