package info.guardianproject.pixelknot.screens;

import info.guardianproject.pixelknot.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.TextView;

public class OnFailureDialog {
	@SuppressLint("InflateParams")
	public static AlertDialog getDialog(final Activity a, final String message_text, final DialogInterface.OnClickListener failure_onclick_listener) {
		View view = a.getLayoutInflater().inflate(R.layout.on_failure_dialog, null);
		
		TextView message = (TextView) view.findViewById(R.id.on_failure_message);
		message.setText(message_text);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(a);
		
		builder.setView(view);
		builder.setPositiveButton(a.getString(R.string.ok), failure_onclick_listener);

		return builder.create();
	}
}
