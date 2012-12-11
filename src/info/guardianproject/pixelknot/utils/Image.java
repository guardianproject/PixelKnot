package info.guardianproject.pixelknot.utils;

import info.guardianproject.pixelknot.Constants;
import info.guardianproject.pixelknot.Constants.PixelKnot.ActivityNames;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;

public class Image implements Constants {
	
	public static class ShareActivity {
		String name, package_name, starting_activity, starting_intent;
	}
	
	public final static Map<String, String> Activities;
	static {
		// package names from appbrain.com
		Map<String, String> activities = new HashMap<String, String>();
		activities.put("com.twitter.android", ActivityNames.TWITTER);
		activities.put("com.facebook.katana", ActivityNames.FACEBOOK);
		activities.put("com.google.android.gm", ActivityNames.GMAIL);
		activities.put("BLUETOOTH", ActivityNames.BLUETOOTH);
		activities.put("com.yahoo.mobile.client.android.flickr", ActivityNames.FLICKR);
		activities.put("com.dropbox.android", ActivityNames.DROPBOX);
		activities.put("com.bumptech.bumpga", ActivityNames.BUMP);
		activities.put("com.google.android.apps.docs", ActivityNames.DRIVE);
		activities.put("GOOGLE_PLUS", ActivityNames.GOOGLE_PLUS);
		activities.put("com.instagram.android", ActivityNames.INSTAGRAM);
		activities.put("com.tumblr", ActivityNames.TUMBLR);
		activities.put("org.wordpress.android", ActivityNames.WORDPRESS);
		activities.put("com.skype.raider", ActivityNames.SKYPE);
		Activities = Collections.unmodifiableMap(activities);
	}
	
	public final static Map<String, Integer[]> Resize;
	static {
		Map<String, Integer[]> resize = new HashMap<String, Integer[]>();
		resize.put(ActivityNames.TWITTER, new Integer[] {1024,768});
		resize.put(ActivityNames.FLICKR, new Integer[] {1024, 768});
		Resize = Collections.unmodifiableMap(resize);
	}
	
	public final static String[] TRUSTED_SHARE_ACTIVITIES = {
		ActivityNames.GMAIL,
		ActivityNames.BLUETOOTH,
		ActivityNames.BUMP,
		ActivityNames.DRIVE,
		ActivityNames.DROPBOX,
		ActivityNames.FLICKR,
		ActivityNames.SKYPE
	};
	
	public static String downsampleImage(String cover_image_name, File dump) {
		Bitmap b = BitmapFactory.decodeFile(cover_image_name);
		int scale = 3;

		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inSampleSize = scale;
		b.recycle();

		Bitmap b_ = BitmapFactory.decodeFile(cover_image_name, opts);
		try {
			File downsampled_image = new File(dump, System.currentTimeMillis() + "_PixelKnot.jpg");
			FileOutputStream fos = new FileOutputStream(downsampled_image);
			b_.compress(CompressFormat.JPEG, 80, fos);
			fos.flush();
			fos.close();

			b_.recycle();
			return downsampled_image.getAbsolutePath();
		} catch (FileNotFoundException e) {
			Log.e(Logger.UI, e.toString());
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(Logger.UI, e.toString());
			e.printStackTrace();
		}

		return null;
	}
	
	public static String cropFor3rdParty(String cover_image_name, File dump, String for_name) {
		Bitmap b = BitmapFactory.decodeFile(cover_image_name);
		Bitmap b_ = Bitmap.createBitmap(b, 0, 0, Resize.get(for_name)[0], Resize.get(for_name)[1]);
		try {
			File downsampled_image = new File(dump, System.currentTimeMillis() + "_PixelKnot.jpg");
			FileOutputStream fos = new FileOutputStream(downsampled_image);
			b_.compress(CompressFormat.JPEG, 80, fos);
			fos.flush();
			fos.close();

			b_.recycle();
			return downsampled_image.getAbsolutePath();
		} catch (FileNotFoundException e) {
			Log.e(Logger.UI, e.toString());
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(Logger.UI, e.toString());
			e.printStackTrace();
		}

		return null;
	}
}
