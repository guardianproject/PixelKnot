package info.guardianproject.pixelknot.screens;

import com.actionbarsherlock.app.SherlockFragment;
import info.guardianproject.pixelknot.Constants;
import info.guardianproject.pixelknot.R;
import info.guardianproject.pixelknot.utils.PassphraseDialogListener;
import info.guardianproject.pixelknot.utils.PixelKnotListener;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public abstract class SetPassphraseDialog {
	static int num_tries = 0;
	
	@SuppressLint("InflateParams") 
	public static AlertDialog getDialog(final SherlockFragment a, final String passphrase) {
		View passphrase_dialog = a.getActivity().getLayoutInflater().inflate(R.layout.set_passphrase_dialog, null);
		
		final TextView passphrase_monitor = (TextView) passphrase_dialog.findViewById(R.id.passphrase_monitor);
		final ImageButton generate_random_password = (ImageButton) passphrase_dialog.findViewById(R.id.generate_random_passphrase);
		final EditText passphrase_holder = (EditText) passphrase_dialog.findViewById(R.id.passphrase_holder);
		
		if(passphrase != null) {
			passphrase_holder.setText(passphrase);
		}
		
		final String passphrase_length_string = a.getString(R.string.password_monitor);
		passphrase_monitor.setText(String.format(passphrase_length_string, passphrase == null ? 0 : passphrase.length()));
		
		generate_random_password.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				passphrase_holder.setText(((PixelKnotListener) a.getActivity()).getPixelKnot().generateRandomPassword());
				
			}
		});
		
		passphrase_holder.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {				
				passphrase_monitor.setText(String.format(passphrase_length_string, passphrase_holder.getText().length()));
				return false;
			}
			
		});
		
		AlertDialog.Builder builder = new AlertDialog.Builder(a.getActivity());
		
		builder.setView(passphrase_dialog);
		builder.setPositiveButton(a.getActivity().getString(R.string.set), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(passphrase_holder.getText().length() < Constants.PASSPHRASE_MIN_LENGTH) {
					num_tries++;
					
					if(num_tries > 4) {
						Toast.makeText(a.getActivity(), a.getResources().getString(R.string.password_generating), Toast.LENGTH_SHORT).show();
						((PassphraseDialogListener) a).onRandomPassphraseRequested();
					} else {
						Toast.makeText(a.getActivity(), a.getResources().getString(R.string.password_too_short), Toast.LENGTH_SHORT).show();
					}
					
					return;
				}
				
				((PassphraseDialogListener) a).onPassphraseSuccessfullySet(passphrase_holder.getText().toString());
			}
		});
		
		return builder.create();
	}

}
