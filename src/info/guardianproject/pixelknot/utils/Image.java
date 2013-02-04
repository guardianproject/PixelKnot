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

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.provider.MediaStore;
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
		activities.put("com.android.bluetooth", ActivityNames.BLUETOOTH);
		activities.put("com.yahoo.mobile.client.android.flickr", ActivityNames.FLICKR);
		activities.put("com.dropbox.android", ActivityNames.DROPBOX);
		activities.put("com.bumptech.bumpga", ActivityNames.BUMP);
		activities.put("com.google.android.apps.docs", ActivityNames.DRIVE);
		activities.put("com.google.android.apps.plus", ActivityNames.GOOGLE_PLUS);
		activities.put("com.instagram.android", ActivityNames.INSTAGRAM);
		activities.put("com.tumblr", ActivityNames.TUMBLR);
		activities.put("org.wordpress.android", ActivityNames.WORDPRESS);
		activities.put("com.skype.raider", ActivityNames.SKYPE);
		activities.put("com.google.android.email", ActivityNames.EMAIL);
		activities.put("com.htc.android.mail", ActivityNames.EMAIL);
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
		ActivityNames.BLUETOOTH,
		ActivityNames.EMAIL,
		ActivityNames.GMAIL,
		ActivityNames.BUMP,
		ActivityNames.DRIVE,
		ActivityNames.DROPBOX,
		ActivityNames.FLICKR,
		ActivityNames.SKYPE
	};

	public static String downsampleImage(String cover_image_name, File dump) {
		int scale = 1;

		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(cover_image_name, opts);
		int x = Math.max(opts.outHeight, opts.outWidth);

		if(x > 1280)
			scale = (int) Math.ceil(x/1280.0);

		Log.d(Logger.UI, "FYI scale is " + scale + " on img of " + opts.outWidth + " x " + opts.outHeight);

		opts = new BitmapFactory.Options();
		opts.inSampleSize = scale;

		Bitmap b = BitmapFactory.decodeFile(cover_image_name, opts);
		try {
			File downsampled_image = new File(dump, System.currentTimeMillis() + ".jpg"); // + "_PixelKnot.jpg"); we shouldn't indicate this is a pixelkno image
			FileOutputStream fos = new FileOutputStream(downsampled_image);
			b.compress(CompressFormat.JPEG, 100, fos);
			fos.flush();
			fos.close();

			b.recycle();
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
			File downsampled_image = new File(dump, System.currentTimeMillis() + ".jpg"); // + "_PixelKnot.jpg"); we shouldn't indicate this is a pixelkno image
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

	public static int getScale(int memory_class) {
		Log.d(Logger.UI, "memory class: " + memory_class);
		if(memory_class >= 60)
			return 3;
		else
			return 4;
	}

	public static boolean cleanUp(Context c, String[] file_names) {
		for(String fn : file_names) {
			File file = new File(fn);
			Uri uri = Uri.fromFile(file);
			if(file.exists()) {
				if(uri.getScheme().equals("file")) {
					Uri[] bases = {
							MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
							MediaStore.Images.Media.INTERNAL_CONTENT_URI
					};

					for(Uri base : bases) {
						Cursor cursor = c.getContentResolver().query(
								base, 
								new String[] {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA}, 
								MediaStore.Images.Media.DATA + " =?",
								new String[] {uri.getPath()},
								null);
						while(cursor.moveToNext()) {
							long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID));
							c.getContentResolver().delete(ContentUris.withAppendedId(base, id), null, null);
						}
						
						cursor.close();
					}
				} else 
					c.getContentResolver().delete(uri, null, null);
				
				file.delete();
			}
		}

		return true;
	}
}
