package info.guardianproject.pixelknot.screens;

import java.util.List;

import org.json.JSONException;

import com.actionbarsherlock.app.SherlockFragment;

import info.guardianproject.pixelknot.Constants;
import info.guardianproject.pixelknot.Constants.PixelKnot.Keys;
import info.guardianproject.pixelknot.PixelKnotActivity.TrustedShareActivity;
import info.guardianproject.pixelknot.R;
import info.guardianproject.pixelknot.utils.ActivityListener;
import info.guardianproject.pixelknot.utils.FragmentListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
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
		
		if(!((FragmentListener) a).getPixelKnot().has(Keys.PASSWORD) && !((FragmentListener) a).getPixelKnot().getPasswordOverride()) {
			// TODO: pop-up for password set
			warnPassword();
			return;
		}

		((FragmentListener) a).getPixelKnot().save();
	}
	
	private void warnPassword() {
		Builder ad = new AlertDialog.Builder(a);
		ad.setTitle(getResources().getString(R.string.wait));
		ad.setMessage(getResources().getString(R.string.warn_password_message));
		ad.setPositiveButton(getResources().getString(R.string.warn_password_yes), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				((FragmentListener) a).getPixelKnot().setPasswordOverride(true);
			}
			
		});
		ad.setNegativeButton(getResources().getString(R.string.warn_password_no), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				setPassword();
			}
			
		});
		ad.show();
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
					embed();
				}
			}
		});
		
		ad.show();
		((FragmentListener) a).showKeyboard(password_holder);
	}

	@Override
	public void updateUi() {
		content_holder.removeAllViews();

		if(!((FragmentListener) a).getHasSuccessfullyEmbed()) {
			title.setText(getString(R.string.please_wait));
			embed();
		} else {
			((FragmentListener) a).updateButtonProminence(0, R.drawable.share_selector);


			List<TrustedShareActivity> trusted_share_activities = ((FragmentListener) a).getTrustedShareActivities();
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
