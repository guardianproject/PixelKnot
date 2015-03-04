package info.guardianproject.pixelknot.screens;

import android.annotation.SuppressLint;
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
import com.actionbarsherlock.app.SherlockFragment;
import info.guardianproject.pixelknot.Constants;
import info.guardianproject.pixelknot.Constants.PixelKnot.Keys;
import info.guardianproject.pixelknot.PixelKnotActivity.TrustedShareActivity;
import info.guardianproject.pixelknot.R;
import info.guardianproject.pixelknot.utils.ActivityListener;
import info.guardianproject.pixelknot.utils.PixelKnotListener;
import java.util.List;

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

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	private void embed() {
		if(!((PixelKnotListener) a).getPixelKnot().hasPassword() && !((PixelKnotListener) a).getPixelKnot().getPasswordOverride()) {
			title.setText(getString(R.string.warning));
			
			TextView content = new TextView(a);
			content.setText(getString(R.string.error_password_required));
			content_holder.addView(content);
			
			return;
		}
		
		if(!((PixelKnotListener) a).getPixelKnot().has(Keys.SECRET_MESSAGE)) {
			title.setText(getString(R.string.oh_no));

			TextView content = new TextView(a);
			content.setText(getString(R.string.error_no_secret_message));
			content_holder.addView(content);
			return;
		}

		if(!((PixelKnotListener) a).getPixelKnot().has(Keys.COVER_IMAGE_NAME)) {
			title.setText(getString(R.string.uh_oh));

			TextView content = new TextView(a);
			content.setText(getString(R.string.error_no_cover_image));
			content_holder.addView(content);
			return;
		}

		((PixelKnotListener) a).getPixelKnot().save();
	}

	@SuppressLint("InflateParams") 
	@Override
	public void updateUi() {
		content_holder.removeAllViews();

		if(!((PixelKnotListener) a).getIsDecryptOnly() && !((PixelKnotListener) a).getHasSuccessfullyEmbed()) {
				title.setText(getString(R.string.please_wait));
				embed();
				return;
		}

		((PixelKnotListener) a).updateButtonProminence(1, R.drawable.share_padded_selector, true);

		List<TrustedShareActivity> trusted_share_activities = ((PixelKnotListener) a).getTrustedShareActivities();
		if(trusted_share_activities.size() > 0) {
			title.setText(getString(R.string.share_with_selected_apps));
			content_holder.addView(LayoutInflater.from(a).inflate(R.layout.share_options, null));

			TableLayout share_options_holder = (TableLayout) content_holder.findViewById(R.id.share_options_holder);
			TableRow tr = new TableRow(a);
			tr.setGravity(Gravity.CENTER);
			share_options_holder.addView(tr);

			int t = 0;

			for(TrustedShareActivity tsa : trusted_share_activities) {
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
		} else
			title.setText(getString(R.string.share_no_apps));
}

@Override
public void initButtons() {
	int share_resource = R.drawable.share_padded_inactive_selector;

	ImageButton share = new ImageButton(a);
	share.setBackgroundColor(getResources().getColor(android.R.color.transparent));
	share.setPadding(0, 0, 0, 0);

	if(((PixelKnotListener) a).getHasSuccessfullyEmbed() || ((PixelKnotListener) a).getIsDecryptOnly()) {
		share.setEnabled(true);
		share_resource = R.drawable.share_padded_selector;
	} else
		share.setEnabled(false);

	share.setImageResource(share_resource);
	share.setOnClickListener(new OnClickListener() {
		@Override
		public void onClick(View v) {
			((PixelKnotListener) a).share();
		}
	});


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
}
