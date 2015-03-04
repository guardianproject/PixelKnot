package info.guardianproject.pixelknot.screens;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import com.actionbarsherlock.app.SherlockFragment;

import info.guardianproject.pixelknot.Constants;
import info.guardianproject.pixelknot.Constants.PixelKnot.Keys;
import info.guardianproject.pixelknot.R;
import info.guardianproject.pixelknot.utils.ActivityListener;
import info.guardianproject.pixelknot.utils.PassphraseDialogListener;
import info.guardianproject.pixelknot.utils.PixelKnotListener;

import org.json.JSONException;

public class SetMessageFragment extends SherlockFragment implements Constants, ActivityListener, PassphraseDialogListener {
	Activity a;
	View root_view;
	Handler h = new Handler();

	EditText secret_message_holder;
	
	int capacity = 0;
	int num_tries_password_set = 0;

	private static final String LOG = Logger.UI;
	
	InputFilter monitor_stego_space = new InputFilter() {

		@Override
		public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
			h.post(new Runnable() {
				@Override
				public void run() {
					((PixelKnotListener) a).getPixelKnot().setSecretMessage(secret_message_holder.getText().toString());
				}
			});
			return source;
		}

	};

	@Override
	public View onCreateView(LayoutInflater li, ViewGroup container, Bundle savedInstanceState) {
		root_view = li.inflate(R.layout.set_message_fragment, container, false);
		
		secret_message_holder = (EditText) root_view.findViewById(R.id.secret_message_holder);
		secret_message_holder.setFilters(new InputFilter[] {monitor_stego_space});
		
		return root_view;
	}

	@Override
	public void onAttach(Activity a) {
		super.onAttach(a);
		Log.d(LOG, "onAttach (fragment:SetMessageFragment) called");
		this.a = a;
		
		capacity = 0;
		try {
			capacity = ((PixelKnotListener) a).getPixelKnot().getInt(Keys.CAPACITY);
		} catch (JSONException e) {}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.d(LOG, "onActivityCreated (fragment:SetMessageFragment) called");
	}
	
	private void setPassphrase() {
		setPassphrase(null);
	}

	private void setPassphrase(String passphrase) {
		if(passphrase == null) {
			try {
				if(((PixelKnotListener) a).getPixelKnot().has(Keys.PASSWORD)) {
					passphrase = ((PixelKnotListener) a).getPixelKnot().getString(Keys.PASSWORD);
				}
			} catch (JSONException e) {}
		}

		SetPassphraseDialog.getDialog(this, passphrase).show();
	}

	@Override
	public void initButtons() {
		ImageButton share_unprotected = new ImageButton(a);
		share_unprotected.setBackgroundColor(getResources().getColor(android.R.color.transparent));
		share_unprotected.setPadding(0, 0, 0, 0);
		share_unprotected.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				((PixelKnotListener) a).getPixelKnot().setPasswordOverride(true);
				((PixelKnotListener) a).setCanAutoAdvance(true);
				((PixelKnotListener) a).autoAdvance();
			}

		});
		share_unprotected.setImageResource(R.drawable.share_selector);

		ImageButton passphrase_protect = new ImageButton(a);
		passphrase_protect.setBackgroundColor(getResources().getColor(android.R.color.transparent));
		passphrase_protect.setPadding(0, 0, 0, 0);
		passphrase_protect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setPassphrase();
			}
		});
		passphrase_protect.setImageResource(R.drawable.password_selector);
		
		((PixelKnotListener) a).setButtonOptions(new ImageButton[] {passphrase_protect, share_unprotected});
	}

	@Override
	public void updateUi() {
		try {
			String secret_message = ((PixelKnotListener) a).getPixelKnot().has(Keys.SECRET_MESSAGE) ? ((PixelKnotListener) a).getPixelKnot().getString(Keys.SECRET_MESSAGE) : null;
			if(secret_message == null)
				secret_message_holder.setText("");

			((PixelKnotListener) a).showKeyboard(secret_message_holder);
		} catch (JSONException e) {}
	}

	@Override
	public void onPassphraseSuccessfullySet(String passphrase) {
		((PixelKnotListener) a).getPixelKnot().setPassphrase(passphrase);
		((PixelKnotListener) a).setCanAutoAdvance(true);
		((PixelKnotListener) a).autoAdvance();
	}

	@Override
	public void onRandomPassphraseRequested() {
		String random_passphrase = ((PixelKnotListener) a).getPixelKnot().generateRandomPassword();
		SetPassphraseDialog.getDialog(this, random_passphrase).show();
	}
}
