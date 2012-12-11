package info.guardianproject.pixelknot.screens;

import org.json.JSONException;

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
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SetMessageFragment extends Fragment implements Constants, ActivityListener {
	Activity a;
	View root_view;
	Handler h = new Handler();
	
	EditText secret_message_holder;
	TextView secret_message_chars_left, encrypt_message_select, password_protect_select;
	
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
					String sm = secret_message_holder.getText().toString();
					byte[] bytes = sm.getBytes(); 
					/*
					String binary = "";
					for(byte b : bytes) {
						int val = b;
						for(int i=0; i < 8; i++) {
							binary += ((val & 128) == 0 ? 0 : 1);
							val <<= 1;
						}
					}
					*/
					secret_message_chars_left.setText(String.valueOf(capacity - bytes.length));
					((FragmentListener) a).getPixelKnot().setSecretMessage(sm);
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
		
		secret_message_chars_left = (TextView) root_view.findViewById(R.id.secret_message_chars_left);		
		
		encrypt_message_select = (TextView) root_view.findViewById(R.id.encrypt_message_select);
		encrypt_message_select.setText(getString(R.string.e));
		
		password_protect_select = (TextView) root_view.findViewById(R.id.password_protect_select);
		password_protect_select.setText(getString(R.string.p));
		
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
		secret_message_chars_left.setText(String.valueOf(capacity));
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
		encrypt_message_select.setText(getString(R.string.e_));
		password_protect_select.setText(getString(R.string.p));
		((FragmentListener) a).setEncryption(apg);
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
					password_protect_select.setText(getString(R.string.p_));
					encrypt_message_select.setText(getText(R.string.e));
					((FragmentListener) a).getPixelKnot().setPassword(password_holder.getText().toString());
				} else
					password_protect_select.setText(getString(R.string.p));
				
			}
		});
		
		ad.show();
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
		Button encrypt_message = new Button(a);
		encrypt_message.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setSecretKey();
			}
			
		});
		encrypt_message.setText(getString(R.string.encryption_select));
		
		Button password_protect = new Button(a);
		password_protect.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setPassword();
			}
		});
		password_protect.setText(getString(R.string.password_protect_select));
		
		Button[] options = new Button[] {encrypt_message, password_protect};
		((FragmentListener) a).setButtonOptions(options);
	}

	@Override
	public void updateUi() {
		try {
			String secret_message = ((FragmentListener) a).getPixelKnot().has(Keys.SECRET_MESSAGE) ? ((FragmentListener) a).getPixelKnot().getString(Keys.SECRET_MESSAGE) : null;
			if(secret_message == null) {
				secret_message_holder.setText("");
				
				String e = getString(R.string.e);
				String p = getString(R.string.p);
				
				if(((PixelKnotActivity.PixelKnot) ((FragmentListener) a).getPixelKnot()).getEncryption())
					e = getString(R.string.e_);
				else if (((PixelKnotActivity.PixelKnot) ((FragmentListener) a).getPixelKnot()).getPassword()) {
					p = getString(R.string.p_);
				}
				
				encrypt_message_select.setText(e);
				password_protect_select.setText(p);
				
				updateCapacity();
			}
		} catch (JSONException e) {
			Log.e(Logger.UI, e.toString());
			e.printStackTrace();
		}
	}
}
