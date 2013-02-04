package info.guardianproject.pixelknot.screens;

import org.json.JSONException;

import com.actionbarsherlock.app.SherlockFragment;

import info.guardianproject.pixelknot.Constants;
import info.guardianproject.pixelknot.PixelKnotActivity;
import info.guardianproject.pixelknot.R;
import info.guardianproject.pixelknot.Constants.PixelKnot.Keys;
import info.guardianproject.pixelknot.crypto.Apg;
import info.guardianproject.pixelknot.utils.ActivityListener;
import info.guardianproject.pixelknot.utils.FragmentListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.Toast;

public class SetMessageFragment extends SherlockFragment implements Constants, ActivityListener {
	Activity a;
	View root_view;
	Handler h = new Handler();
	
	EditText secret_message_holder;
	int capacity = 0;
	
	Apg apg = null;
	long secret_key = 0L;
	long[] encryption_ids = null;
	
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
		updateCapacity();
		return root_view;
	}
	
	@Override
	public void onAttach(Activity a) {
		super.onAttach(a);
		this.a = a;
		
		capacity = 0;
		try {
			capacity = ((FragmentListener) a).getPixelKnot().getInt(Keys.CAPACITY);
		} catch (JSONException e) {}
		
		
	}
	
	private void updateCapacity() {
		
	}
	
	public void updateButtonProminence() {
		int password_protect_resource = R.drawable.password_off_selector;
		
		if (((PixelKnotActivity.PixelKnot) ((FragmentListener) a).getPixelKnot()).getPassword())
			password_protect_resource = R.drawable.password_on_selector;
		
		((FragmentListener) a).updateButtonProminence(0, password_protect_resource);
		
		/*
		int encrypt_message_resource = R.drawable.encrypt_off_selector;
		int password_protect_resource = R.drawable.password_off_selector;
		
		if(((PixelKnotActivity.PixelKnot) ((FragmentListener) a).getPixelKnot()).getEncryption())
			encrypt_message_resource = R.drawable.encrypt_on_selector;
		else if (((PixelKnotActivity.PixelKnot) ((FragmentListener) a).getPixelKnot()).getPassword())
			password_protect_resource = R.drawable.password_on_selector;
		
		((FragmentListener) a).updateButtonProminence(0, encrypt_message_resource);		
		((FragmentListener) a).updateButtonProminence(1, password_protect_resource);
		*/
	}

	private void setEncryptionIds() {
		apg.selectEncryptionKeys(this, a, null);
	}
	
	private void setSecretKey() {
		apg = Apg.getInstance();
		if(!apg.isAvailable(a.getApplicationContext()))
			Toast.makeText(a, getString(R.string.apg_error_activity_not_found), Toast.LENGTH_LONG).show();
		else
			apg.selectSecretKey(this);
	}
	
	private void setEncryption() {
		((FragmentListener) a).setEncryption(apg);
		updateButtonProminence();
		
		((FragmentListener) a).setCanAutoAdvance(true);
		((FragmentListener) a).autoAdvance();
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
					updateButtonProminence();
					
					((FragmentListener) a).setCanAutoAdvance(true);
					((FragmentListener) a).autoAdvance();
				}
			}
		});
		
		ad.show();
		((FragmentListener) a).showKeyboard(password_holder);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(Logger.UI, "result code: " + resultCode);
		if(resultCode == Activity.RESULT_OK) {
			switch(requestCode) {
			case Apg.SELECT_SECRET_KEY:
				apg.onActivityResult(a, requestCode, resultCode, data);
				secret_key = apg.getSignatureKeyId();
				setEncryptionIds();
				
				break;
			case Apg.SELECT_PUBLIC_KEYS:
				apg.onActivityResult(a, requestCode, resultCode, data);
				encryption_ids = apg.getEncryptionKeys();
				
				setEncryption();
				break;
			case Apg.ENCRYPT_MESSAGE:
				apg.onActivityResult(a, requestCode, resultCode, data);
				break;
			}
		}
	}
	
	@Override
	public void initButtons() {
		/*
		ImageButton encrypt_message = new ImageButton(a);
		encrypt_message.setBackgroundColor(getResources().getColor(android.R.color.transparent));
		encrypt_message.setPadding(0, 0, 0, 0);
		encrypt_message.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setSecretKey();
			}
			
		});
		encrypt_message.setImageResource(R.drawable.encrypt_off_selector);
		*/
		
		ImageButton password_protect = new ImageButton(a);
		password_protect.setBackgroundColor(getResources().getColor(android.R.color.transparent));
		password_protect.setPadding(0, 0, 0, 0);
		password_protect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setPassword();
			}
		});
		password_protect.setImageResource(R.drawable.password_off_selector);
		
		//((FragmentListener) a).setButtonOptions(new ImageButton[] {encrypt_message, password_protect});
		((FragmentListener) a).setButtonOptions(new ImageButton[] {password_protect});
	}
	
	@Override
	public void updateUi() {
		try {
			String secret_message = ((FragmentListener) a).getPixelKnot().has(Keys.SECRET_MESSAGE) ? ((FragmentListener) a).getPixelKnot().getString(Keys.SECRET_MESSAGE) : null;
			if(secret_message == null) {
				secret_message_holder.setText("");
				updateCapacity();
			}
			
			updateButtonProminence();
			((FragmentListener) a).showKeyboard(secret_message_holder);
		} catch (JSONException e) {}
	}
}
