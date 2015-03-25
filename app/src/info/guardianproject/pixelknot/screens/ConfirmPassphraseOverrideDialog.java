package info.guardianproject.pixelknot.screens;

import info.guardianproject.pixelknot.R;
import info.guardianproject.pixelknot.utils.PixelKnotListener;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;

import com.actionbarsherlock.app.SherlockFragment;

public abstract class ConfirmPassphraseOverrideDialog {
	@SuppressLint("InflateParams") 
	public static AlertDialog getDialog(final SherlockFragment a) {
		View confirm_passphrase_override_dialog = a.getActivity().getLayoutInflater().inflate(R.layout.confirm_passphrase_override_dialog, null);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(a.getActivity());
		builder.setView(confirm_passphrase_override_dialog);
		
		builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				((PixelKnotListener) a.getActivity()).getPixelKnot().setPasswordOverride(true);
				((PixelKnotListener) a.getActivity()).setCanAutoAdvance(true);
				((PixelKnotListener) a.getActivity()).autoAdvance();
			}
		});
		
		builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				((PixelKnotListener) a.getActivity()).getPixelKnot().setPasswordOverride(false);
				dialog.cancel();
			}
		});		
		
		return builder.create();
	}
}