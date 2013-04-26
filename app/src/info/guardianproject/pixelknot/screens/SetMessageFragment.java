package info.guardianproject.pixelknot.screens;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
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
import info.guardianproject.pixelknot.crypto.Apg;
import info.guardianproject.pixelknot.utils.ActivityListener;
import info.guardianproject.pixelknot.utils.FragmentListener;

import org.json.JSONException;

public class SetMessageFragment extends SherlockFragment implements Constants, ActivityListener {
	Activity a;
	View root_view;
	Handler h = new Handler();

	EditText secret_message_holder;
	int capacity = 0;

	Apg apg = null;
	long secret_key = 0L;
	long[] encryption_ids = null;

	private static final String LOG = Logger.UI;

	InputFilter monitor_stego_space = new InputFilter() {

		@Override
		public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
			h.post(new Runnable() {
				@Override
				public void run() {
					((FragmentListener) a).getPixelKnot().setSecretMessage(secret_message_holder.getText().toString());
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
			capacity = ((FragmentListener) a).getPixelKnot().getInt(Keys.CAPACITY);
		} catch (JSONException e) {}


	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.d(LOG, "onActivityCreated (fragment:SetMessageFragment) called");
	}

	private void setPassword() {
		final EditText password_holder = new EditText(a);
		try {
			if(((FragmentListener) a).getPixelKnot().has(Keys.PASSWORD))
				password_holder.setText(((FragmentListener) a).getPixelKnot().getString(Keys.PASSWORD));
			else
				password_holder.setHint(getString(R.string.password));

		} catch (JSONException e) {
			password_holder.setHint(getString(R.string.password));
		}

		Builder ad = new AlertDialog.Builder(a);
		ad.setView(password_holder);
		ad.setPositiveButton(getString(R.string.set), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(password_holder.getText().length() > 0) {
					((FragmentListener) a).getPixelKnot().setPassword(password_holder.getText().toString());

					((FragmentListener) a).setCanAutoAdvance(true);
					((FragmentListener) a).autoAdvance();
				}
			}
		});

		ad.show();
		((FragmentListener) a).showKeyboard(password_holder);
	}

	@Override
	public void initButtons() {

		ImageButton share_unprotected = new ImageButton(a);
		share_unprotected.setBackgroundColor(getResources().getColor(android.R.color.transparent));
		share_unprotected.setPadding(0, 0, 0, 0);
		share_unprotected.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				((FragmentListener) a).getPixelKnot().setPasswordOverride(true);
				((FragmentListener) a).setCanAutoAdvance(true);
				((FragmentListener) a).autoAdvance();
			}

		});
		share_unprotected.setImageResource(R.drawable.share_selector);

		ImageButton password_protect = new ImageButton(a);
		password_protect.setBackgroundColor(getResources().getColor(android.R.color.transparent));
		password_protect.setPadding(0, 0, 0, 0);
		password_protect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setPassword();
			}
		});
		password_protect.setImageResource(R.drawable.password_selector);

		((FragmentListener) a).setButtonOptions(new ImageButton[] {share_unprotected, password_protect});
	}

	@Override
	public void updateUi() {
		try {
			String secret_message = ((FragmentListener) a).getPixelKnot().has(Keys.SECRET_MESSAGE) ? ((FragmentListener) a).getPixelKnot().getString(Keys.SECRET_MESSAGE) : null;
			if(secret_message == null)
				secret_message_holder.setText("");

			((FragmentListener) a).showKeyboard(secret_message_holder);
		} catch (JSONException e) {}
	}
}
