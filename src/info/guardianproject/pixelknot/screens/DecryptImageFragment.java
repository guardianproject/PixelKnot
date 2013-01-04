package info.guardianproject.pixelknot.screens;

import org.json.JSONException;

import com.actionbarsherlock.app.SherlockFragment;

import info.guardianproject.pixelknot.Constants;
import info.guardianproject.pixelknot.R;
import info.guardianproject.pixelknot.Constants.PixelKnot.Keys;
import info.guardianproject.pixelknot.utils.ActivityListener;
import info.guardianproject.pixelknot.utils.FragmentListener;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

public class DecryptImageFragment extends SherlockFragment implements Constants, ActivityListener {
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
	public void updateUi() {
		if(!((FragmentListener) a).getHasSuccessfullyExtracted()) {
			((FragmentListener) a).getPixelKnot().extract();
			return;
		}
		
		try {
			secret_message_holder.setText(((FragmentListener) a).getPixelKnot().getString(Keys.SECRET_MESSAGE));
		} catch (JSONException e) {
			Log.e(Logger.UI, e.toString());
			e.printStackTrace();
		}

		((FragmentListener) a).showKeyboard(secret_message_holder);
	}

	@Override
	public void initButtons() {
		ImageButton start_over = new ImageButton(a);
		start_over.setBackgroundColor(getResources().getColor(android.R.color.transparent));
		start_over.setImageResource(R.drawable.camera_selector);
		start_over.setPadding(0, 0, 0, 0);
		start_over.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				((FragmentListener) a).clearPixelKnot();
			}
		});
		
		((FragmentListener) a).setButtonOptions(new ImageButton[] {start_over});
	}
}
