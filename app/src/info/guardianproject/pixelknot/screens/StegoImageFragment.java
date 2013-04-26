package info.guardianproject.pixelknot.screens;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.actionbarsherlock.app.SherlockFragment;

import info.guardianproject.pixelknot.Constants;
import info.guardianproject.pixelknot.Constants.PixelKnot.Keys;
import info.guardianproject.pixelknot.R;
import info.guardianproject.pixelknot.utils.ActivityListener;
import info.guardianproject.pixelknot.utils.FragmentListener;

import java.io.File;

public class StegoImageFragment extends SherlockFragment implements Constants, ActivityListener {
	View root_view;
	ImageView cover_image_holder;
	
	String path_to_cover_image = null;
	File cover_image_file = null;
	
	Activity a;
	Handler h = new Handler();
	
	private static final String LOG = Logger.UI;
		
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
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		
		((FragmentListener) a).getPixelKnot().setCoverImageName(getArguments().getString(Keys.COVER_IMAGE_NAME));
	}
	
	public void setImageData(String path_to_cover_image) {
		this.path_to_cover_image = path_to_cover_image;
		setImageData();
	}
	
	public void setImageData() {
		h.post(new Runnable() {
			@Override
			public void run() {
				
				BitmapFactory.Options opts = new BitmapFactory.Options();
				opts.inJustDecodeBounds = true;
				
				Bitmap b = BitmapFactory.decodeFile(path_to_cover_image, opts);
				int scale = Math.min(4, opts.outWidth/10);
				if(opts.outHeight > opts.outWidth)
					scale = Math.min(4, opts.outHeight/10);

				opts = new BitmapFactory.Options();
				opts.inSampleSize = scale;

				Bitmap b_ = BitmapFactory.decodeFile(path_to_cover_image, opts);
				cover_image_holder.setImageBitmap(b_);
			}
		});
		
		h.postDelayed(new Runnable() {
			@Override
			public void run() {
				((FragmentListener) a).getPixelKnot().setCoverImageName(path_to_cover_image);
				
				((FragmentListener) a).setCanAutoAdvance(true);
				((FragmentListener) a).autoAdvance();
			}
		}, 200);
		
	}

	@Override
	public void updateUi() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initButtons() {
		// TODO Auto-generated method stub
		
	}
}
