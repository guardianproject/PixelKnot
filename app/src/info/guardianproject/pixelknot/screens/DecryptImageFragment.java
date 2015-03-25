package info.guardianproject.pixelknot.screens;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
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
import info.guardianproject.pixelknot.screens.mods.PKDialogOnShowListener;
import info.guardianproject.pixelknot.utils.ActivityListener;
import info.guardianproject.pixelknot.utils.PassphraseDialogListener;
import info.guardianproject.pixelknot.utils.PixelKnotListener;

import org.json.JSONException;

public class DecryptImageFragment extends SherlockFragment implements Constants, ActivityListener, PassphraseDialogListener {
	View root_view;	
	EditText secret_message_holder;

	Activity a;
	Handler h = new Handler();
	
	@Override
	public View onCreateView(LayoutInflater li, ViewGroup container, Bundle savedInstanceState) {
		root_view = li.inflate(R.layout.decryt_image_fragment, container, false);
		secret_message_holder = (EditText) root_view.findViewById(R.id.secret_message_holder);
		
		return root_view;
	}

	@Override
	public void onAttach(Activity a) {
		super.onAttach(a);
		this.a = a;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void updateUi() {
		if(!((PixelKnotListener) a).getPixelKnot().getPasswordOverride() && !((PixelKnotListener) a).getPixelKnot().hasPassword()) {
			AlertDialog ad = InputPassphraseDialog.getDialog(this);
			ad.setOnShowListener(new PKDialogOnShowListener(a));
			ad.show();
			
			return;
		}
		
		if(!((PixelKnotListener) a).getHasSuccessfullyExtracted()) {
			((PixelKnotListener) a).getPixelKnot().extract();
			return;
		}
		
		try {
			secret_message_holder.setText(((PixelKnotListener) a).getPixelKnot().getString(Keys.SECRET_MESSAGE));
			((PixelKnotListener) a).doWait(false);
		} catch (JSONException e) {
			Log.e(Logger.UI, e.toString());
			e.printStackTrace();
		}
	}

	@Override
	public void initButtons() {
		ImageButton share = new ImageButton(a);
		share.setBackgroundColor(getResources().getColor(android.R.color.transparent));
		share.setPadding(0, 0, 0, 0);
		share.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				((PixelKnotListener) a).getPixelKnot().setPasswordOverride(true);
				((PixelKnotListener) a).setCanAutoAdvance(true);
				((PixelKnotListener) a).autoAdvance();
			}
			
		});
		share.setImageResource(R.drawable.share_selector);
		
		ImageButton start_over = new ImageButton(a);
		start_over.setBackgroundColor(getResources().getColor(android.R.color.transparent));
		start_over.setPadding(0, 0, 0, 0);
		start_over.setImageResource(R.drawable.camera_selector);
		start_over.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				((PixelKnotListener) a).clearPixelKnot();
			}
		});

		((PixelKnotListener) a).setButtonOptions(new ImageButton[] {start_over, share});
	}

	@Override
	public void onPassphraseSuccessfullySet(String passphrase) {
		if(passphrase == null) {
			((PixelKnotListener) a).getPixelKnot().setPasswordOverride(true);
		} else {
			((PixelKnotListener) a).getPixelKnot().setPassphrase(passphrase);
		}
		updateUi();
	}

	@Override
	public void onRandomPassphraseRequested() {}
}
