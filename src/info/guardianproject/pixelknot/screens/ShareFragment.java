package info.guardianproject.pixelknot.screens;

import org.json.JSONObject;

import info.guardianproject.pixelknot.Constants;
import info.guardianproject.pixelknot.Constants.PixelKnot.Keys;
import info.guardianproject.pixelknot.R;
import info.guardianproject.pixelknot.utils.ActivityListener;
import info.guardianproject.pixelknot.utils.FragmentListener;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class ShareFragment extends Fragment implements Constants, ActivityListener, OnClickListener {
	Activity a;
	View root_view;
	Handler h = new Handler();
	
	TextView error_title, error_content;
	Button share;
	
	@Override
	public View onCreateView(LayoutInflater li, ViewGroup container, Bundle savedInstanceState) {
		root_view = li.inflate(R.layout.share_fragment, container, false);
		
		error_title = (TextView) root_view.findViewById(R.id.error_title);
		error_content = (TextView) root_view.findViewById(R.id.error_content);
		
		share = (Button) root_view.findViewById(R.id.share);
		share.setOnClickListener(this);
		
		return root_view;
	}
	
	@Override
	public void onAttach(Activity a) {
		super.onAttach(a);
		this.a = a;
	}
	
	private void embed() {
		if(!((FragmentListener) a).getPixelKnot().has(Keys.SECRET_MESSAGE)) {
			error_content.setText(getString(R.string.error_no_secret_message));
			return;
		}
		
		if(!((FragmentListener) a).getPixelKnot().has(Keys.COVER_IMAGE_NAME)) {
			error_content.setText(getString(R.string.error_no_cover_image));
			return;
		}
		
		error_title.setVisibility(View.INVISIBLE);
		((FragmentListener) a).getPixelKnot().save();
	}

	@Override
	public void updateUi() {
		
		if(!((FragmentListener) a).getHasSuccessfullyEmbed())
			embed();
		else {
			error_content.setText(((JSONObject) ((FragmentListener) a).getPixelKnot()).toString());
			share.setVisibility(View.VISIBLE);
		}
		
	}

	@Override
	public void initButtons() {
		((FragmentListener) a).setButtonOptions(new Button[] {});
		
	}

	@Override
	public void onClick(View v) {
		if(v == share)
			((FragmentListener) a).share();
		
	}
}
