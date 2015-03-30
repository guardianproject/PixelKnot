package info.guardianproject.pixelknot;

import info.guardianproject.pixelknot.Constants.Logger;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.Html;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public abstract class AboutDialog {
	public final static String LOG = Logger.UI;
	
	@SuppressLint("InflateParams")
	public static AlertDialog getDialog(final Activity a) {
		View about = LayoutInflater.from(a).inflate(R.layout.about_fragment, null);
		
		TextView about_gp_email = (TextView) about.findViewById(R.id.about_gp_email);
		about_gp_email.setText(Html.fromHtml("<a href='mailto:" + about_gp_email.getText().toString() + "'>" + about_gp_email.getText().toString() + "</a>"));
		
		TextView about_version = (TextView) about.findViewById(R.id.about_version);
		try {
			about_version.setText(a.getPackageManager().getPackageInfo(a.getPackageName(), 0).versionName);
		} catch (NameNotFoundException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
			about_version.setText("1.0");
		}
		
		LinearLayout license_holder = (LinearLayout) about.findViewById(R.id.about_license_holder);
		String[] licenses = a.getResources().getStringArray(R.array.about_software);
		String[] licenses_ = a.getResources().getStringArray(R.array.about_software_);
		for(int l=0; l<licenses.length; l++) {
			TextView license = new TextView(a);
			license.setText(licenses[l]);
			license.setTextColor(a.getResources().getColor(R.color.pk_black));
			license.setTextSize(20);
			
			TextView license_ = new TextView(a);
			license_.setText(Html.fromHtml("<a href='" + licenses_[l] + "'>" + licenses_[l] + "</a>"));
			license_.setLinksClickable(true);
			Linkify.addLinks(license_, Linkify.ALL);
			license_.setPadding(0, 0, 0, 30);
			license_.setTextSize(20);
			
			license_holder.addView(license);
			license_holder.addView(license_);
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(a);
		builder.setView(about);
		builder.setPositiveButton(a.getString(R.string.ok), null);
		
		return builder.create();
	}
}
