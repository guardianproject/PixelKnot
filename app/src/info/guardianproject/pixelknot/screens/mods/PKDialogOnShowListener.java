package info.guardianproject.pixelknot.screens.mods;

import info.guardianproject.pixelknot.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.widget.Button;

public class PKDialogOnShowListener implements OnShowListener {
	Context ctx;
	
	public PKDialogOnShowListener(Context ctx) {
		this.ctx = ctx;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onShow(DialogInterface dialog) {
		Button pos = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE);
		Button neg = ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_NEGATIVE);
		for(Button b : new Button[] {pos, neg}) {
			b.setBackgroundDrawable(ctx.getResources().getDrawable(R.drawable.dialog_selector));
			b.setTextAppearance(ctx, R.style.p_dialog);
		}
	}

}
