package info.guardianproject.pixelknot.utils;

import android.view.View;
import android.widget.ImageButton;

import info.guardianproject.pixelknot.PixelKnotActivity.PixelKnot;
import info.guardianproject.pixelknot.PixelKnotActivity.TrustedShareActivity;

import java.util.List;

public interface PixelKnotListener {
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
	public boolean getCanAutoAdvance();
	public void setIsDecryptOnly(boolean isDecryptOnly);
	public boolean getIsDecryptOnly();
	public void setCanAutoAdvance(boolean canAutoAdvance);
	public void share();
	public List<TrustedShareActivity> getTrustedShareActivities();
	public void updateButtonProminence(int which, int new_resource, boolean enabled);
	public void autoAdvance();
	public void autoAdvance(int position);
	public void showKeyboard(View target);
	public void hideKeyboard();
	public void doWait(boolean status);
	public void onProcessComplete(String result_text);
}
