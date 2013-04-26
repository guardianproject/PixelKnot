package info.guardianproject.pixelknot.crypto;
/*
 * Based on code from K9Mail:
 * http://code.google.com/p/k9mail/source/browse/k9mail/branches/apg-integration/
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import java.io.File;
import java.io.Serializable;


abstract public class CryptoProvider implements Serializable
{
    static final long serialVersionUID = 0x21071234;

    protected long mEncryptionKeyIds[] = null;
    protected long mSignatureKeyId = 0;
    protected String mSignatureUserId = null;
    protected boolean mSignatureSuccess = false;
    protected boolean mSignatureUnknown = false;
    protected String mDecryptedData = null;
    protected String mEncryptedData = null;

    abstract public boolean isAvailable(Context context);
    abstract public boolean isEncrypted(String message);
    abstract public boolean isSigned(String message);
    abstract public boolean onActivityResult(Activity activity, int requestCode, int resultCode,
            Intent data);
    abstract public boolean selectSecretKey(Activity activity);
    abstract public boolean selectEncryptionKeys(Activity activity, String emails);
    abstract public boolean encrypt(Activity activity, String data);
    abstract public boolean encryptFile(Activity activity, File file);

    abstract public boolean decrypt(Activity activity, String data);
    abstract public long[] getSecretKeyIdsFromEmail(Context context, String email);
    abstract public String getUserId(Context context, long keyId);
    abstract public String getName();
    abstract public boolean test(Context context);

    public static CryptoProvider createInstance()
    {
        return Apg.createInstance();
      
      //  return None.createInstance(account);
    }

    public void setSignatureKeyId(long keyId)
    {
        mSignatureKeyId = keyId;
    }

    public long getSignatureKeyId()
    {
        return mSignatureKeyId;
    }

    public void setEncryptionKeys(long keyIds[])
    {
        mEncryptionKeyIds = keyIds;
    }

    public long[] getEncryptionKeys()
    {
        return mEncryptionKeyIds;
    }

    public boolean hasSignatureKey()
    {
        return mSignatureKeyId != 0;
    }

    public boolean hasEncryptionKeys()
    {
        return (mEncryptionKeyIds != null) && (mEncryptionKeyIds.length > 0);
    }

    public String getEncryptedData()
    {
        return mEncryptedData;
    }

    public String getDecryptedData()
    {
        return mDecryptedData;
    }

    public void setSignatureUserId(String userId)
    {
        mSignatureUserId = userId;
    }

    public String getSignatureUserId()
    {
        return mSignatureUserId;
    }

    public boolean getSignatureSuccess()
    {
        return mSignatureSuccess;
    }

    public boolean getSignatureUnknown()
    {
        return mSignatureUnknown;
    }
}