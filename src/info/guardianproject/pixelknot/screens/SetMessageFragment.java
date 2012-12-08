package info.guardianproject.pixelknot.screens;

import org.json.JSONException;

import info.guardianproject.pixelknot.Constants;
import info.guardianproject.pixelknot.R;
import info.guardianproject.pixelknot.Constants.PixelKnot.Keys;
import info.guardianproject.pixelknot.utils.ActivityListener;
import info.guardianproject.pixelknot.utils.FragmentListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SetMessageFragment extends Fragment implements Constants, OnClickListener, ActivityListener {
	Activity a;
	View root_view;
	Handler h = new Handler();
	
	EditText secret_message_holder;
	CheckBox encrypt_message_select, password_protect_select;
	TextView secret_message_chars_left;
	
	int capacity, charsLeft;
	
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
					secret_message_chars_left.setText(String.valueOf(charsLeft - bytes.length));
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
		
		encrypt_message_select = (CheckBox) root_view.findViewById(R.id.encrypt_message_select);
		encrypt_message_select.setOnClickListener(this);
		
		password_protect_select = (CheckBox) root_view.findViewById(R.id.password_protect_select);
		password_protect_select.setOnClickListener(this);
		
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
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void updateCapacity() {
		secret_message_chars_left.setText(String.valueOf(capacity));
	}
	
	private void setPassword() {
		final EditText password_holder = new EditText(a);
		password_holder.setHint(getString(R.string.password));
		
		Builder ad = new AlertDialog.Builder(a);
		ad.setView(password_holder);
		ad.setPositiveButton(getString(R.string.set), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(password_holder.getText().length() > 0) {
					password_protect_select.setText(getString(R.string.password_protect_select_));
					((FragmentListener) a).getPixelKnot().setPassword(password_holder.getText().toString());
				} else
					password_protect_select.setChecked(false);
				
			}
		});
		
		ad.show();
	}
	
	@Override
	public void initButtons() {
		Button save = new Button(a);
		save.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(secret_message_holder.getText().length() == 0) {
					Toast.makeText(a, getString(R.string.error_no_secret_message), Toast.LENGTH_LONG).show();
					return;
				}
				
				((FragmentListener) a).getPixelKnot().setSecretMessage(secret_message_holder.getText().toString());
				
				if(!((FragmentListener) a).getPixelKnot().has(Keys.COVER_IMAGE_NAME)) {
					Toast.makeText(a, getString(R.string.error_no_cover_image), Toast.LENGTH_LONG).show();
					return;
				}
				
				((FragmentListener) a).getPixelKnot().save();
			}
			
		});
		save.setText(getString(R.string.save));
		
		Button[] options = new Button[] {save};
		((FragmentListener) a).setButtonOptions(options);
	}

	@Override
	public void onClick(View v) {
		if(v == password_protect_select) {
			if(!password_protect_select.isChecked()) {
				((FragmentListener) a).getPixelKnot().setPassword(null);
				password_protect_select.setText(getString(R.string.password_protect_select));
			} else
				setPassword();
		} else if(v == encrypt_message_select) {
			
		}
		
	}

	@Override
	public void updateUi() {
		try {
			String secret_message = ((FragmentListener) a).getPixelKnot().has(Keys.SECRET_MESSAGE) ? ((FragmentListener) a).getPixelKnot().getString(Keys.SECRET_MESSAGE) : null;
			if(secret_message == null) {
				secret_message_holder.setText("");
				
				encrypt_message_select.setChecked(false);
				
				password_protect_select.setChecked(false);
				password_protect_select.setText(getString(R.string.password_protect_select));
				
				capacity = 0;
				updateCapacity();
			}
		} catch (JSONException e) {
			Log.e(Logger.UI, e.toString());
			e.printStackTrace();
		}
		
	}
}
