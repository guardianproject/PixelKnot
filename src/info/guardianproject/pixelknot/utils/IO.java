package info.guardianproject.pixelknot.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

public class IO {
	public static String pullPathFromUri(Context context, Uri uri) {
		if(uri.getScheme() != null && uri.getScheme().equals("file"))
    		return uri.toString().replace("file://", "");
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
}
