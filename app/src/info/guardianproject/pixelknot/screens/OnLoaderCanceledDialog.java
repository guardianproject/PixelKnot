package info.guardianproject.pixelknot.screens;

import info.guardianproject.pixelknot.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.TextView;

public abstract class OnLoaderCanceledDialog {
	
	@SuppressLint("InflateParams")
	public static AlertDialog getDialog(final Activity a, final String message_text, final DialogInterface.OnClickListener positive_ocl) {
		View view = a.getLayoutInflater().inflate(R.layout.loader_canceled_dialog, null);
		
		TextView message = (TextView) view.findViewById(R.id.loader_canceled_message);
		message.setText(message_text);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(a);
		
		builder.setView(view);
		builder.setPositiveButton(a.getString(R.string.yes), positive_ocl);
		builder.setNegativeButton(a.getString(R.string.no), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});

		return builder.create();
	}
}
