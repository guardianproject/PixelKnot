package info.guardianproject.pixelknot.screens;

import java.io.File;

import org.json.JSONException;

import info.guardianproject.pixelknot.Constants;
import info.guardianproject.pixelknot.R;
import info.guardianproject.pixelknot.Constants.PixelKnot.Keys;
import info.guardianproject.pixelknot.utils.ActivityListener;
import info.guardianproject.pixelknot.utils.FragmentListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

public class DecryptImageFragment extends Fragment implements Constants, ActivityListener {
	View root_view;	
	EditText secret_message_holder;

	Activity a;
	Handler h = new Handler();

	@Override
	public View onCreateView(LayoutInflater li, ViewGroup container, Bundle savedInstanceState) {
		root_view = li.inflate(R.layout.decryt_image_fragment, container, false);

		secret_message_holder = (EditText) root_view.findViewById(R.id.secret_message_holder);

		return root_view;
	}

	@Override
	public void onAttach(Activity a) {
		super.onAttach(a);
		this.a = a;
	}

	@Override
	public void updateUi() {
		if(!((FragmentListener) a).getHasSuccessfullyExtracted())
			((FragmentListener) a).getPixelKnot().decrypt();
		else {
			try {
				secret_message_holder.setText(((FragmentListener) a).getPixelKnot().getString(Keys.SECRET_MESSAGE));
			} catch (JSONException e) {
				Log.e(Logger.UI, e.toString());
				e.printStackTrace();
			}
		}
	}

	@Override
	public void initButtons() {}
}
