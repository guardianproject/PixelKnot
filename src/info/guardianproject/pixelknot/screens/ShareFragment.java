package info.guardianproject.pixelknot.screens;

import com.actionbarsherlock.app.SherlockFragment;

import info.guardianproject.pixelknot.Constants;
import info.guardianproject.pixelknot.Constants.PixelKnot.Keys;
import info.guardianproject.pixelknot.PixelKnotActivity.TrustedShareActivity;
import info.guardianproject.pixelknot.R;
import info.guardianproject.pixelknot.utils.ActivityListener;
import info.guardianproject.pixelknot.utils.FragmentListener;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class ShareFragment extends SherlockFragment implements Constants, ActivityListener {
	Activity a;
	View root_view;
	Handler h = new Handler();
	
	TextView title;
	LinearLayout content_holder;
	
	@Override
	public View onCreateView(LayoutInflater li, ViewGroup container, Bundle savedInstanceState) {
		root_view = li.inflate(R.layout.share_fragment, container, false);
		
		title = (TextView) root_view.findViewById(R.id.title);
		content_holder = (LinearLayout) root_view.findViewById(R.id.content_holder);
				
		return root_view;
	}
	
	@Override
	public void onAttach(Activity a) {
		super.onAttach(a);
		this.a = a;
	}
	
	private void embed() {
		if(!((FragmentListener) a).getPixelKnot().has(Keys.SECRET_MESSAGE)) {
			title.setText(getString(R.string.oh_no));
			
			TextView content = new TextView(a);
			content.setText(getString(R.string.error_no_secret_message));
			content_holder.addView(content);
			return;
		}
		
		if(!((FragmentListener) a).getPixelKnot().has(Keys.COVER_IMAGE_NAME)) {
			title.setText(getString(R.string.uh_oh));
			
			TextView content = new TextView(a);
			content.setText(getString(R.string.error_no_cover_image));
			content_holder.addView(content);
			return;
		}
		
		((FragmentListener) a).getPixelKnot().save();
	}

	@Override
	public void updateUi() {
		content_holder.removeAllViews();
		
		if(!((FragmentListener) a).getHasSuccessfullyEmbed()) {
			title.setText(getString(R.string.please_wait));
			embed();
		} else {
			((FragmentListener) a).updateButtonProminence(0, R.drawable.share_selector);
			
			title.setText(getString(R.string.share_with_selected_apps));
			content_holder.addView(LayoutInflater.from(a).inflate(R.layout.share_options, null));
			
			TableLayout share_options_holder = (TableLayout) content_holder.findViewById(R.id.share_options_holder);
			TableRow tr = new TableRow(a);
			tr.setGravity(Gravity.CENTER);
			share_options_holder.addView(tr);
			int t = 0;
			
			for(TrustedShareActivity tsa : ((FragmentListener) a).getTrustedShareActivities()) {
				try {
					((TableRow) tsa.view.getParent()).removeView(tsa.view);
				} catch (NullPointerException e) {}
				
				if(t % 2 == 0 && t != 0) {
					tr = new TableRow(a);
					tr.setGravity(Gravity.CENTER);
					share_options_holder.addView(tr);
				}
				
				tr.addView(tsa.view);
				t++;
			}
		}
		
	}

	@Override
	public void initButtons() {
		int share_resource = R.drawable.share_inactive_selector;
		
		ImageButton share = new ImageButton(a);
		share.setBackgroundColor(getResources().getColor(android.R.color.transparent));
		share.setPadding(0, 0, 0, 0);
		
		if(((FragmentListener) a).getHasSuccessfullyEmbed()) {
			share.setEnabled(true);
			share_resource = R.drawable.share_selector;
		} else
			share.setEnabled(false);
		
		share.setImageResource(share_resource);
		share.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				((FragmentListener) a).share();
			}
		});
		
		
		ImageButton start_over = new ImageButton(a);
		start_over.setBackgroundColor(getResources().getColor(android.R.color.transparent));
		start_over.setPadding(0, 0, 0, 0);
		start_over.setImageResource(R.drawable.camera_selector);
		start_over.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				((FragmentListener) a).clearPixelKnot();
			}
		});
		
		((FragmentListener) a).setButtonOptions(new ImageButton[] {share, start_over});
		
	}
}
