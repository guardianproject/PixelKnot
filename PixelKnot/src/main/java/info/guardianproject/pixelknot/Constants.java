package info.guardianproject.pixelknot;

public class Constants {
    public final static String PASSWORD_SENTINEL = "----* PK v 1.0 REQUIRES PASSWORD ----*";
    public final static String PGP_SENTINEL = "-----BEGIN PGP MESSAGE-----";

    public final static byte[] DEFAULT_PASSWORD_SALT = new String("When I say \"make some\", you say \"noise\"!").getBytes();
    public final static byte[] DEFAULT_F5_SEED = new String("Make some [noise!]  Make some [noise!]").getBytes();
    public final static int PASSPHRASE_MIN_LENGTH = 4;

    public static class Logger {
        public final static String UI = "PixelKnot";
        public final static String PREFS = "PixelKnot (Prefs)";
        public static final String MODEL = "PixelKnot (Model)";
        public static final String AES = "PixelKnot (AES Util)";
        public static final String F5 = "PixelKnot (F5)";
        public static final String LOADER = "PixelKnot (Loader Screen)";
        public static final String RPG = "PixelKnot (Random Phrase Generator)";
    }

    public static class Steps {
        public static final int EMBED = 10;
        public static final int EXTRACT = 8;
        public static final int DECRYPT = 1;
        public static final int ENCRYPT = 1;
    }

    public static final int MAX_IMAGE_PIXEL_SIZE = 1280;
    public static final int OUTPUT_IMAGE_QUALITY = 90;
    public static final int MAX_SENDING_DIALOG_COUNT = 2;
}
