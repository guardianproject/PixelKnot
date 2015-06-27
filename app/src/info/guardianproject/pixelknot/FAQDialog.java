package info.guardianproject.pixelknot;

import info.guardianproject.pixelknot.Constants.Logger;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Typeface;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public abstract class FAQDialog {
	public final static String LOG = Logger.UI;
	
	@SuppressLint("InflateParams")
	public static AlertDialog getDialog(final Activity a) {
		View faq_root = LayoutInflater.from(a).inflate(R.layout.faq_fragment, null);
		LinearLayout faq_holder = (LinearLayout) faq_root.findViewById(R.id.faq_holder);
		
		String[] faq_q = a.getResources().getStringArray(R.array.faq_q);
		String[] faq_a = a.getResources().getStringArray(R.array.faq_a);
		
		for(int f=0; f<faq_q.length; f++) {
			TextView[] faq = new TextView[2];
			
			faq[0] = new TextView(a);
			faq[0].setText(faq_q[f]);
			faq[0].setTextSize(20);
			faq[0].setTypeface(null, Typeface.BOLD);
			
			faq[1] = new TextView(a);
			faq[1].setText(faq_a[f]);
			faq[1].setTextSize(20);
			faq[1].setPadding(0, 0, 0, 30);
			Linkify.addLinks(faq[1], Linkify.WEB_URLS);
			
			for(TextView t : faq) {
				t.setTextColor(a.getResources().getColor(R.color.pk_black));
				faq_holder.addView(t);
			}
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(a);
		builder.setView(faq_root);
		builder.setPositiveButton(a.getString(R.string.ok), null);
		
		return builder.create();
	}

}
