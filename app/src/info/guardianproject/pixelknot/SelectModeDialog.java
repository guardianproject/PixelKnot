package info.guardianproject.pixelknot;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface.OnClickListener;
import android.view.View;

public abstract class SelectModeDialog {
	@SuppressLint("InflateParams")
	public static AlertDialog getDialog(Activity a, OnClickListener select_encrypt_listener, OnClickListener select_decrypt_listener) {
		View select_mode_dialog = a.getLayoutInflater().inflate(R.layout.select_mode_dialog, null);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(a);
		builder.setView(select_mode_dialog);
		
		builder.setPositiveButton(a.getString(R.string.encrypt), select_encrypt_listener);
		builder.setNegativeButton(a.getString(R.string.decrypt), select_decrypt_listener);
		
		return builder.create();
	}

}
