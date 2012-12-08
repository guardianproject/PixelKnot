package info.guardianproject.pixelknot.utils;

import android.app.Activity;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;

public class PixelKnotMediaScanner implements MediaScannerConnectionClient {
	MediaScannerListener msl;
	MediaScannerConnection msc;
	
	String f;
	
	public interface MediaScannerListener {
		public void onMediaScanned(String path, Uri uri);
	}
	
	public PixelKnotMediaScanner(Activity a, String f) {
		this.f = f;
		msl = ((MediaScannerListener) a);
		msc = new MediaScannerConnection(a, this);
		msc.connect();
		
	}
	
	@Override
	public void onMediaScannerConnected() {
		msc.scanFile(f, null);
	}

	@Override
	public void onScanCompleted(String path, Uri uri) {
		msl.onMediaScanned(path, uri);
	}

}
