package info.guardianproject.pixelknot.utils;

import info.guardianproject.pixelknot.Constants.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLDecoder;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

public class IO {
	@SuppressWarnings("deprecation")
	public static String pullPathFromUri(Context context, Uri uri) {
		if(uri.getScheme() != null && uri.getScheme().equals("file"))
    		return URLDecoder.decode(uri.toString().replace("file://", ""));
		else {
    		String path = null;
    		String[] cols = {MediaStore.Images.Media.DATA};
    		Cursor c = context.getContentResolver().query(uri, cols, null, null, null);
    		
    		if(c != null && c.moveToFirst()) {
    			path = c.getString(c.getColumnIndex(MediaStore.Images.Media.DATA));
    			c.close();
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
