package info.guardianproject.pixelknot;

import android.os.Environment;

public interface Constants {
	public final static String PASSWORD_SENTINEL = "----* PK v 1.0 REQUIRES PASSWORD ----*";
	public final static String PGP_SENTINEL = "-----BEGIN PGP MESSAGE-----";
	public final static byte[] PASSWORD_SALT = new byte[] {
		(byte) 0xC3,
		(byte) 0xAA,
		(byte) 0xBB,
		(byte) 0x34,
		(byte) 0x7B,
		(byte) 0x66,
		(byte) 0x3A,
		(byte) 0x81
	};
	
	public static class Logger {
		public final static String UI = "***************** PixelKnot **************";
		public final static String PREFS = "***************** PixelKnot (Prefs) **************";
	}
	
	public static class Settings {
		public static final String LANGUAGE = "pk_language";
		public static final class Locales {
			public final static int DEFAULT = 0;
			public final static int EN = 1;
			public final static int FA = 2;
		}
	}
	
	public final static String DUMP = Environment.getExternalStorageDirectory().getAbsolutePath() + "/PixelKnot";
	public static final String CURRENT_TAB = "currentTab";
	
	public static class Source {
		public static final int CAMERA = 1000;
		public static final int GALLERY = 1001;
		public static final String DECRYPT = "doDecryptOn";
	}
	
	public static class Screens {
		public static class Loader {
			public static int[] KNOT_IMAGES = {
				R.drawable.knot_bow,
				R.drawable.knot_catspaw,
				R.drawable.knot_double_overhand,
				R.drawable.knot_fishermans_eye,
				R.drawable.knot_lariatloop,
				R.drawable.knot_overhand,
				R.drawable.knot_running,
				R.drawable.knot_sailors,
				R.drawable.knot_sheepshank,
				R.drawable.knot_stevedors,
				R.drawable.knot_surgeons
			};
			
			public static class Steps {
				public static final int EMBED = 12;
				public static final int EXTRACT = 12;
				public static final int DECRYPT = 6;
				public static final int ENCRYPT = 7;
			}
		}
	}
	
	public static class PixelKnot {
		public static class Keys {
			public static final String COVER_IMAGE_NAME = "cover_image_name";
			public static final String SECRET_MESSAGE = "secret_message";
			public static final String PASSWORD = "password";
			public static final String CAN_SAVE = "can_save";
			public static final String HAS_ENCRYPTION = "has_encryption";
			public static final String CAPACITY = "capacity";
			public static final String OUT_FILE_NAME = "out_file_name";
		}
		
		public static class ActivityNames {
			// package names from appbrain.com
			public static final String TWITTER = "Twitter";
			public static final String FACEBOOK = "Facebook";
			public static final String GMAIL = "GMail";
			public static final String BLUETOOTH = "Bluetooth";
			public static final String FLICKR = "Flickr";
			public static final String DROPBOX = "Dropbox";
			public static final String BUMP = "Bump";
			public static final String DRIVE = "Drive";
			public static final String GOOGLE_PLUS = "Google+";
			public static final String INSTAGRAM = "Instagram";
			public static final String TUMBLR = "Tumblr";
			public static final String WORDPRESS = "Wordpress";
			public static final String SKYPE = "Skype";
			public static final String EMAIL = "Email";
		}
	}
}
