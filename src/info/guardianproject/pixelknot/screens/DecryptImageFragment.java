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
import android.widget.ImageView;
import android.widget.Toast;

public class DecryptImageFragment extends Fragment implements Constants, ActivityListener {
	View root_view;
	ImageView cover_image_holder;
	
	String path_to_cover_image = null;
	File cover_image_file = null;
	
	Activity a;
	Handler h = new Handler();
	
	@Override
	public View onCreateView(LayoutInflater li, ViewGroup container, Bundle savedInstanceState) {
		root_view = li.inflate(R.layout.cover_image_fragment, container, false);
		
		cover_image_holder = (ImageView) root_view.findViewById(R.id.cover_image_holder);
		if(!getArguments().containsKey(Keys.COVER_IMAGE_NAME))
			a.finish();
		
		setImageData(getArguments().getString(Keys.COVER_IMAGE_NAME));
		
		return root_view;
	}
	
	@Override
	public void onAttach(Activity a) {
		super.onAttach(a);
		this.a = a;
	}
	
	public void setImageData(String path_to_cover_image) {
		this.path_to_cover_image = path_to_cover_image;
		setImageData();
	}
	
	public void setImageData() {
		h.post(new Runnable() {
			@Override
			public void run() {
				Bitmap b = BitmapFactory.decodeFile(path_to_cover_image);
				int scale = Math.min(4, b.getWidth()/10);
				if(b.getHeight() > b.getWidth())
					scale = Math.min(4, b.getHeight()/10);

				BitmapFactory.Options opts = new BitmapFactory.Options();
				opts.inSampleSize = scale;
				b.recycle();

				Bitmap b_ = BitmapFactory.decodeFile(path_to_cover_image, opts);
				cover_image_holder.setImageBitmap(b_);
			}
		});
		
		h.postDelayed(new Runnable() {
			@Override
			public void run() {
				((FragmentListener) a).getPixelKnot().setCoverImageName(path_to_cover_image);
				((FragmentListener) a).getPixelKnot().decrypt();
			}
		}, 200);
	}

	@Override
	public void updateUi() {
		try {
			StringBuilder sb = new StringBuilder();
			sb.append(getString(R.string.result_title));
			sb.append("\n\n");
			sb.append(((FragmentListener) a).getPixelKnot().getString(Keys.SECRET_MESSAGE));
			
			Builder builder = new AlertDialog.Builder(a);
			builder.setMessage(sb.toString());
			builder.setPositiveButton(R.string.result_save, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					
				}
			});
			builder.setNegativeButton(R.string.ok, null);
			builder.show();
			
			
			initButtons();
		} catch (JSONException e) {
			Log.e(Logger.UI, e.toString());
			e.printStackTrace();
		}
		
	}

	@Override
	public void initButtons() {}
}
