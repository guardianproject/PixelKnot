package info.guardianproject.pixelknot.screens;

import info.guardianproject.pixelknot.R;
import info.guardianproject.pixelknot.utils.PassphraseDialogListener;
import com.actionbarsherlock.app.SherlockFragment;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.TextView;

public abstract class InputPassphraseDialog {
	
	@SuppressLint("InflateParams") 
	public static AlertDialog getDialog(final SherlockFragment a) {
		View passphrase_dialog = a.getActivity().getLayoutInflater().inflate(R.layout.input_passphrase_dialog, null);
		final TextView passphrase_holder = (TextView) passphrase_dialog.findViewById(R.id.passphrase_holder);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(a.getActivity());
		builder.setView(passphrase_dialog);
		builder.setPositiveButton(R.string.set, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String passphrase = passphrase_holder.getText().toString();
				if(passphrase.length() == 0) {
					passphrase = null;
				}
				
				((PassphraseDialogListener) a).onPassphraseSuccessfullySet(passphrase);
				
			}
		});
		
		return builder.create();
	}

}
