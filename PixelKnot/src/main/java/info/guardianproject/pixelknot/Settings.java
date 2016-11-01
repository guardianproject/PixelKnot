package info.guardianproject.pixelknot;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Settings
{
    public static final String LOGTAG = "Settings";
    public static final boolean LOGGING = false;

    private final SharedPreferences mPrefs;
    private final Context context;

    private static final String KEY_SKIP_SENT_DIALOG = "skip_sent_dialog";
    private static final String KEY_SENDING_DIALOG_COUNT = "sending_dialog_count";
    private static final String KEY_SKIP_GALLERY_INFO = "skip_gallery_info";
    private static final String KEY_SKIP_UNSAFE_SHARE_INFO = "skip_unsafe_share_info";

    public Settings(Context _context)
    {
        context = _context;
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void registerChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener)
    {
        mPrefs.registerOnSharedPreferenceChangeListener(listener);
    }

    public void unregisterChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener)
    {
        mPrefs.unregisterOnSharedPreferenceChangeListener(listener);
    }

    public boolean skipSentDialog()
    {
        return mPrefs.getBoolean(KEY_SKIP_SENT_DIALOG, false);
    }

    public void setSkipSentDialog(boolean skip)
    {
        mPrefs.edit().putBoolean(KEY_SKIP_SENT_DIALOG, skip).commit();
    }

    public int sendingDialogCount()
    {
        return mPrefs.getInt(KEY_SENDING_DIALOG_COUNT, 0);
    }

    public void setSendingDialogCount(int count)
    {
        mPrefs.edit().putInt(KEY_SENDING_DIALOG_COUNT, count).commit();
    }

    public boolean skipGalleryInfo()
    {
        return mPrefs.getBoolean(KEY_SKIP_GALLERY_INFO, false);
    }

    public void setSkipGalleryInfo(boolean skip)
    {
        mPrefs.edit().putBoolean(KEY_SKIP_GALLERY_INFO, skip).commit();
    }

    public boolean skipUnsafeShareInfo()
    {
        return mPrefs.getBoolean(KEY_SKIP_UNSAFE_SHARE_INFO, false);
    }

    public void setSkipUnsafeShareInfo(boolean skip)
    {
        mPrefs.edit().putBoolean(KEY_SKIP_UNSAFE_SHARE_INFO, skip).commit();
    }
}