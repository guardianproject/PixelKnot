package info.guardianproject.pixelknot;

import android.os.Environment;

public interface Constants {
	public final static String PASSWORD_SENTINEL = "----* PK v 1.0 REQUIRES PASSWORD ----*";
	public final static String PGP_SENTINEL = "-----BEGIN PGP MESSAGE-----";
	public final static byte[] PASSWORD_SALT = {(byte) 0xA4, (byte) 0x0B, (byte) 0xC8,
	      (byte) 0x34, (byte) 0xD6, (byte) 0x95, (byte) 0xF3, (byte) 0x13};
	
	public static class Logger {
		public final static String UI = "***************** PixelKnot **************";
	}
	
	
	public final static String DUMP = Environment.getExternalStorageDirectory().getAbsolutePath() + "/PixelKnot";
	public static final String CURRENT_TAB = "currentTab";
	
	public static class Source {
		public static final int CAMERA = 1000;
		public static final int GALLERY = 1001;
		public static final String DECRYPT = "doDecryptOn";
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
	}
}
