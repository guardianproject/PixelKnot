package info.guardianproject.pixelknot.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import info.guardianproject.pixelknot.Constants;
import info.guardianproject.pixelknot.Constants.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;

public class IO {
	private static final String LOG = Logger.UI;

	@SuppressWarnings("deprecation")
	public static String pullPathFromUri(Context context, Uri uri) {
		if(uri.getScheme() != null && uri.getScheme().equals("file"))
			return URLDecoder.decode(uri.toString().replace("file://", ""));
		else if(uri.getScheme() != null && uri.getScheme().equals("content")) {
			try {
				File tmp = new File(Constants.DUMP, System.currentTimeMillis() + ".jpg");
				
				InputStream is = context.getContentResolver().openInputStream(uri);
				byte[] file_data = new byte[is.available()];
				
				is.read(file_data, 0, file_data.length);
				is.close();
				
				FileOutputStream fos = new FileOutputStream(tmp);
				fos.write(file_data);
				fos.flush();
				fos.close();
				
				return tmp.getAbsolutePath();
			} catch(FileNotFoundException e) {
				Log.e(LOG, e.toString());
				e.printStackTrace();
				return null;
			} catch (IOException e) {
				Log.e(LOG, e.toString());
				e.printStackTrace();
				return null;
			}
	} else {
		String path = null;
		String[] cols = {MediaStore.Images.Media.DATA};
		Cursor c = context.getContentResolver().query(uri, null, null, null, null);

		if(c != null && c.moveToFirst()) {

			for(String s : c.getColumnNames()) {
				Log.d(LOG, "col: " + s);
			}

			path = c.getString(c.getColumnIndex(MediaStore.Images.Media.DATA));
			c.close();
			Log.d(LOG, "path found: " + path);
		} else {
			Log.d(LOG, "fucking URI is null");
		}

		return path;
	}
}

public static byte[] getBytesFromFile(String path) {
	Log.d(Logger.UI, "getting bytes from " + path);

	try {
		FileInputStream fis = new FileInputStream(new File(path));
		byte[] buffer = new byte[fis.available()];
		fis.read(buffer);
		fis.close();

		return buffer;

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
