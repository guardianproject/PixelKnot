package info.guardianproject.pixelknot;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;

import info.guardianproject.f5android.plugins.PluginNotificationListener;
import info.guardianproject.f5android.plugins.f5.Extract;
import info.guardianproject.f5android.plugins.f5.james.Jpeg;
import info.guardianproject.pixelknot.crypto.Aes;

class StegoDecryptionJob extends StegoJob {
    private static final boolean LOGGING = false;
    private static final String LOGTAG = "StegoDecryptionJob";

    public interface OnProgressListener {
        void onProgressUpdate(StegoDecryptionJob job, int percent);
    }

    private final DummyListenerActivity mActivity;
    private String mMessage;
    private final String mPassword;
    private final InputStream mStream;
    private OnProgressListener mOnProgressListener;

    public StegoDecryptionJob(IStegoThreadHandler threadHandler, InputStream is, String password) {
        super(threadHandler);
        mStream = is;
        mMessage = null;
        mPassword = password;

        mActivity = new DummyListenerActivity();

        addProcess(new Runnable() {
            @SuppressLint("LongLogTag")
            @Override
            public void run() {
                try {
                    Extract extract = new Extract(mActivity, mStream, getF5Seed());
                    extract.run();
                } catch (Exception e) {
                    Log.e(Jpeg.LOG, e.toString());
                    e.printStackTrace();
                    abortJob();
                }
            }
        }, Constants.Steps.EXTRACT);
        if (hasPassword()) {
            addProcess(new Runnable() {
                @Override
                public void run() {
                    if (mMessage != null && mMessage.indexOf(Constants.PASSWORD_SENTINEL) == 0) {
                        String secret_message = mMessage.substring(Constants.PASSWORD_SENTINEL.length());

                        int idx = secret_message.indexOf("\n");

                        byte[] message = Base64.decode(secret_message.substring(idx + 1), Base64.DEFAULT);
                        byte[] iv = Base64.decode(secret_message.substring(0, idx), Base64.DEFAULT);

                        String sm = Aes.DecryptWithPassword(extractPassword(mPassword), iv, message, extractPasswordSalt(mPassword).getBytes());
                        if (sm != null) {
                            mMessage = sm;
                        } else {
                            // Wrong password
                            abortJob();
                        }
                    }
                    onProgressTick();
                }
            }, 1);
        }
        addProcess(new Runnable() {
            @Override
            public void run() {
                setProcessingStatus(mMessage != null ? ProcessingStatus.EXTRACTED_SUCCESSFULLY : ProcessingStatus.ERROR);
                onProgressTick();
            }
        }, 1);
        Run();
    }

    public String getMessage() {
        return mMessage;
    }

    public void setOnProgressListener(OnProgressListener listener) {
        mOnProgressListener = listener;
    }

    private boolean hasPassword() {
        return !TextUtils.isEmpty(mPassword);
    }

    private String extractPasswordSalt(String from_password) {
        return from_password.substring(from_password.length()/3, (from_password.length()/3)*2);
    }

    private String extractF5Seed(String from_password) {
        return from_password.substring((from_password.length()/3)*2);
    }

    private String extractPassword(String from_password) {
        return from_password.substring(0, from_password.length()/3);
    }

    private String getPassword() {
        if(!hasPassword()) {
            return null;
        }

        return extractPassword(mPassword);
    }

    private byte[] getPasswordSalt() {
        if(!hasPassword()) {
            return Constants.DEFAULT_PASSWORD_SALT;
        }

        return extractPasswordSalt(mPassword).getBytes();
    }

    private byte[] getF5Seed() {
        if(!hasPassword()) {
            return Constants.DEFAULT_F5_SEED;
        }
        return extractF5Seed(mPassword).getBytes();
    }

    @Override
    void setProcessingStatus(ProcessingStatus processingStatus) {
        super.setProcessingStatus(processingStatus);
        if (mOnProgressListener != null) {
            mOnProgressListener.onProgressUpdate(StegoDecryptionJob.this, getProgressPercent());
        }
    }

    @Override
    void onProgressTick() {
        super.onProgressTick();
        if (mOnProgressListener != null) {
            mOnProgressListener.onProgressUpdate(StegoDecryptionJob.this, getProgressPercent());
        }
    }

    private class DummyListenerActivity extends Activity implements PluginNotificationListener, Extract.ExtractionListener {

        @Override
        public Resources getResources() {
            return mThreadHandler.getContext().getResources();
        }

        @Override
        public void onUpdate(String with_message) {
            onProgressTick();
        }

        @Override
        public void onFailure() {
            mMessage = null;
            abortJob();
        }

        @Override
        public void onExtractionResult(ByteArrayOutputStream baos) {
            mMessage = new String(baos.toByteArray());
        }
    }
}
