package info.guardianproject.pixelknot.screens;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.actionbarsherlock.app.SherlockFragment;

import info.guardianproject.pixelknot.Constants;
import info.guardianproject.pixelknot.Constants.PixelKnot.Keys;
import info.guardianproject.pixelknot.R;
import info.guardianproject.pixelknot.utils.ActivityListener;
import info.guardianproject.pixelknot.utils.PixelKnotListener;
import info.guardianproject.pixelknot.utils.IO;
import info.guardianproject.pixelknot.utils.PixelKnotMediaScanner;

import org.json.JSONException;

import java.io.File;

public class CoverImageFragment extends SherlockFragment implements Constants, ActivityListener {
	View root_view;
	ImageView cover_image_holder;
	int blank_image;

	Uri cover_image_uri = null;
	String path_to_cover_image = null;
	File cover_image_file = null;

	Activity a;
	Handler h = new Handler();
	
	private static final String LOG = Logger.UI;
	
	OnClickListener choose_picture_listener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			((PixelKnotListener) a).doWait(true);
			
			Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT).setType("image/*");
			startActivityForResult(galleryIntent, Source.GALLERY);
		}

	};

	@Override
	public View onCreateView(LayoutInflater li, ViewGroup container, Bundle savedInstanceState) {
		Log.d(LOG, "onCreateView (fragment) called");
		root_view = li.inflate(R.layout.cover_image_fragment, container, false);
		cover_image_holder = (ImageView) root_view.findViewById(R.id.cover_image_holder);
		cover_image_holder.setImageResource(blank_image);
		
		if(!((PixelKnotListener) a).getHasSuccessfullyEmbed()) {
			cover_image_holder.setOnClickListener(choose_picture_listener);
		} else {
			cover_image_holder.setOnClickListener(null);
		}
		
		return root_view;
	}

	@Override
	public void onAttach(Activity a) {
		super.onAttach(a);
		Log.d(LOG, "onAttach (fragment) called");
		this.a = a;

		switch(Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this.a).getString(Settings.LANGUAGE, String.valueOf(Settings.Locales.DEFAULT)))) {
		case Settings.Locales.FA:
			blank_image = R.drawable.pixelknot_blank_image_fa;
			break;
		default:
			blank_image = R.drawable.pixelknot_blank_image_en;
			break;
		}

		
		
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		if(!((PixelKnotListener) a).getHasSeenFirstPage()) {
			initButtons();
			((PixelKnotListener) a).setHasSeenFirstPage(true);
		}
		
		updateUi();
	}

	public void setImageData(String path_to_cover_image) {
		this.path_to_cover_image = path_to_cover_image;
		setImageData();
	}
	
	public void setImageData(String path_to_cover_image, Uri uri) {
		this.path_to_cover_image = path_to_cover_image;
		cover_image_file = new File(path_to_cover_image);
		cover_image_uri = uri;
		setImageData();
	}

	public void setImageData() {
		h.post(new Runnable() {
			@Override
			public void run() {
				BitmapFactory.Options opts = new BitmapFactory.Options();
				opts.inJustDecodeBounds = true;
				
				BitmapFactory.decodeFile(path_to_cover_image, opts);
				int scale = Math.min(4, opts.outWidth/10);
				if(opts.outHeight > opts.outWidth)
					scale = Math.min(4, opts.outHeight/10);

				opts = new BitmapFactory.Options();
				opts.inSampleSize = scale;

				Bitmap b_ = BitmapFactory.decodeFile(path_to_cover_image, opts);
				cover_image_holder.setImageBitmap(b_);
				
				
				
			}
		});
		
		((PixelKnotListener) a).doWait(false);

		((PixelKnotListener) a).getPixelKnot().setCoverImageName(path_to_cover_image);
		h.postDelayed(new Runnable() {
			@Override
			public void run() {
				((PixelKnotListener) a).autoAdvance();
			}
		}, 1000);

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		((PixelKnotListener) a).doWait(false);
		
		if(resultCode == Activity.RESULT_OK) {
			if(requestCode == Source.GALLERY || requestCode == Source.CAMERA) {
				((PixelKnotListener) a).setCanAutoAdvance(true);
				if(requestCode == Source.GALLERY) {
					cover_image_uri = data.getData();
					path_to_cover_image = IO.pullPathFromUri(a, cover_image_uri);
					cover_image_file = new File(path_to_cover_image);
					setImageData();
				} else if(requestCode == Source.CAMERA) {
					((PixelKnotListener) a).doWait(true);
					new PixelKnotMediaScanner(a, path_to_cover_image);
				}    			
			}
		}
	}

	@Override
	public void initButtons() {
		ImageButton take_picture = new ImageButton(a);
		take_picture.setBackgroundColor(getResources().getColor(android.R.color.transparent));
		take_picture.setPadding(0, 0, 0, 0);
		take_picture.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				((PixelKnotListener) a).doWait(true);
				
				cover_image_file = new File(DUMP, "temp_img.jpg");
				cover_image_uri = Uri.fromFile(cover_image_file);
				path_to_cover_image = cover_image_file.getAbsolutePath();
				Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, cover_image_uri);
				startActivityForResult(cameraIntent, Source.CAMERA);
			}

		});
		take_picture.setImageResource(R.drawable.camera_selector);

		ImageButton choose_picture = new ImageButton(a);
		choose_picture.setBackgroundColor(getResources().getColor(android.R.color.transparent));
		choose_picture.setPadding(0, 0, 0, 0);
		choose_picture.setOnClickListener(choose_picture_listener);
		choose_picture.setImageResource(R.drawable.gallery_selector);

		((PixelKnotListener) a).setButtonOptions(new ImageButton[] {take_picture, choose_picture});
	}

	@Override
	public void updateUi() {
		try {
			path_to_cover_image = ((PixelKnotListener) a).getPixelKnot().has(Keys.COVER_IMAGE_NAME) ? ((PixelKnotListener) a).getPixelKnot().getString(Keys.COVER_IMAGE_NAME) : null;
		} catch (JSONException e) {
			Log.e(Logger.UI, e.toString());
			e.printStackTrace();
		} 

		if(path_to_cover_image == null) {
			cover_image_file = null;
			cover_image_uri = null;
			cover_image_holder.setImageResource(blank_image);
		} else {
			setImageData();
		}
		
		if(!((PixelKnotListener) a).getHasSuccessfullyEmbed()) {
			cover_image_holder.setOnClickListener(choose_picture_listener);
		} else {
			cover_image_holder.setOnClickListener(null);
		}
		

	}
}