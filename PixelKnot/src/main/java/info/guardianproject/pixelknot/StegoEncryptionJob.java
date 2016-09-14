package info.guardianproject.pixelknot;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.FileObserver;
import android.text.TextUtils;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import info.guardianproject.f5android.plugins.PluginNotificationListener;
import info.guardianproject.f5android.plugins.f5.james.Jpeg;
import info.guardianproject.f5android.plugins.f5.james.JpegEncoder;
import info.guardianproject.pixelknot.crypto.Aes;

public class StegoEncryptionJob extends StegoJob {
    private static final boolean LOGGING = false;
    private static final String LOGTAG = "StegoEncryptionJob";

    public interface OnProgressListener {
        void onProgressUpdate(StegoEncryptionJob job, int percent);
    }

    private DummyListenerActivity mActivity;
    private Bitmap mBitmap;
    private String mImageName;
    private String mMessage;
    private String mPassword;
    private File mOutputFile;
    private FileObserver mOutputFileObserver;
    private boolean mHasBeenShared;
    private int mOutputFileHandleCount;
    private boolean mDeleteOutputFileOnClose;
    private OnProgressListener mOnProgressListener;

    public StegoEncryptionJob(IStegoThreadHandler threadHandler, Bitmap bitmap, String imageName, String message, String password) {
        super(threadHandler);
        mBitmap = bitmap.copy(bitmap.getConfig(), false);
        mImageName = imageName;
        mMessage = message;
        mPassword = password;
        mOutputFileHandleCount = 0;

        mActivity = new DummyListenerActivity();

        addProcess(new Runnable() {
            @Override
            public void run() {
                if (mBitmap.getWidth() > Constants.MAX_IMAGE_PIXEL_SIZE || mBitmap.getHeight() > Constants.MAX_IMAGE_PIXEL_SIZE) {
                    int newWidth = Constants.MAX_IMAGE_PIXEL_SIZE;
                    int newHeight = Constants.MAX_IMAGE_PIXEL_SIZE;
                    if (mBitmap.getWidth() > mBitmap.getHeight()) {
                        newHeight = (int)(mBitmap.getHeight() * ((float)Constants.MAX_IMAGE_PIXEL_SIZE / (float)mBitmap.getWidth()));
                    } else {
                        newWidth = (int)(mBitmap.getWidth() * ((float)Constants.MAX_IMAGE_PIXEL_SIZE / (float)mBitmap.getHeight()));
                    }
                    mBitmap = Bitmap.createScaledBitmap(mBitmap, newWidth, newHeight, true);
                }
                onProgressTick();
            }
        }, 1);

        if(hasPassword()) {
            addProcess(new Runnable() {
                @Override
                public void run() {
                    Map.Entry<String, String> pack = Aes.EncryptWithPassword(getPassword(), mMessage, getPasswordSalt()).entrySet().iterator().next();
                    mMessage = Constants.PASSWORD_SENTINEL.concat(new String(pack.getKey())).concat(pack.getValue());
                    onProgressTick();
                }
            }, 1);
        }

        addProcess(new Runnable() {
            @SuppressLint("LongLogTag")
            @Override
            public void run() {
                boolean success = false;
                try {
                    createOutputFile();
                    FileOutputStream fos = new FileOutputStream(mOutputFile);
                    JpegEncoder jpg = new JpegEncoder(mActivity /*mThreadHandler.getActivity()*/, mBitmap.copy(mBitmap.getConfig(), false), Constants.OUTPUT_IMAGE_QUALITY, fos, getF5Seed(), getThread());
                    success = jpg.Compress(new ByteArrayInputStream(mMessage.getBytes()));
                    fos.close();
                } catch (Exception e) {
                    Log.e(Jpeg.LOG, e.toString());
                    e.printStackTrace();
                    abortJob();
                }
                setProcessingStatus(success ? ProcessingStatus.EMBEDDED_SUCCESSFULLY : ProcessingStatus.ERROR);
            }
        }, Constants.Steps.EMBED);

        /*
        StegoProcessThread debug = new StegoProcessThread(Jpeg.LOG) {
            @SuppressLint("LongLogTag")
            @Override
            public void run() {
                super.run();
                boolean success = false;
                try {
                    Thread.sleep(2000);
                    createOutputFile();
                    InputStream is = mThreadHandler.getActivity().getResources().getAssets().open("test.jpg");
                    FileOutputStream fos = new FileOutputStream(mOutputFile);
                    int cb = 0;
                    byte[] buf = new byte[1024];
                    while ((cb = is.read(buf)) >= 0) {
                        fos.write(buf, 0, cb);
                    }
                    fos.close();
                    is.close();
                } catch (Exception e) {
                    Log.e(Jpeg.LOG, e.toString());
                    e.printStackTrace();
                    ((PluginNotificationListener) mThreadHandler.getActivity()).onFailure();
                }
                mThreadHandler.onProcessDone(this);
                mThreadHandler.onJobDone(StegoEncryptionJob.this, success);
            }
        };
        mThreadHandler.addProcess(debug);
        */
        Run();
    }

    public void setOnProgressListener(OnProgressListener listener) {
        mOnProgressListener = listener;
    }

    public boolean hasPassword() {
        if(!TextUtils.isEmpty(mPassword))
            return true;
        return false;
    }

    private String extractPasswordSalt(String from_password) {
        return from_password.substring((int) (from_password.length()/3), (int) ((from_password.length()/3)*2));
    }

    private String extractF5Seed(String from_password) {
        return from_password.substring((int) ((from_password.length()/3)*2));
    }

    private String extractPassword(String from_password) {
        return from_password.substring(0, (int) (from_password.length()/3));
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

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public long getOutputLength() {
        return mOutputFile.length();
    }

    public File getOutputFile() {
        return mOutputFile;
    }

    public String getOutputImageName() {
        return mImageName;
    }

    public boolean getHasBeenShared() {
        return mHasBeenShared;
    }

    public void setHasBeenShared(boolean hasBeenShared) {
        mHasBeenShared = hasBeenShared;
    }

    private void createOutputFile() throws IOException {
        mOutputFile = App.getInstance().getFileManager().createFileForJob(getId());
        mOutputFileObserver = new FileObserver(mOutputFile.getAbsolutePath(), FileObserver.OPEN | FileObserver.CLOSE_WRITE | FileObserver.CLOSE_NOWRITE) {
            @Override
            public void onEvent(int event, String path) {
                synchronized (StegoEncryptionJob.this) {
                    if (event == FileObserver.OPEN) {
                        mHasBeenShared = true;
                        mOutputFileHandleCount++;
                        if (LOGGING)
                            Log.d(LOGTAG, "file opened, handle count " + mOutputFileHandleCount + " for file " + mOutputFile.getPath());
                    } else if (event == FileObserver.CLOSE_WRITE || event == FileObserver.CLOSE_NOWRITE) {
                        mOutputFileHandleCount--;
                        if (LOGGING)
                            Log.d(LOGTAG, "file closed, handle count " + mOutputFileHandleCount + " for file " + mOutputFile.getPath());
                        if (mOutputFileHandleCount == 0 && mDeleteOutputFileOnClose) {
                            if (LOGGING)
                                Log.d(LOGTAG, "file handle count is 0 and file marked for deletion, deleting file " + mOutputFile.getPath());
                            mDeleteOutputFileOnClose = false;
                            mOutputFileObserver.stopWatching();
                            mOutputFile.delete();
                        }
                    }
                }
            }
        };
        mOutputFileObserver.startWatching();
    }

    @Override
    public void cleanup() {
        super.cleanup();
        try {
            synchronized (this) {
                if (mOutputFile != null) {
                    if (mOutputFileHandleCount == 0) {
                        if (LOGGING)
                            Log.d(LOGTAG, "cleanup - file not open, deleting it now");
                        mOutputFileObserver.stopWatching();
                        mOutputFile.delete();
                    } else {
                        if (LOGGING)
                            Log.d(LOGTAG, "cleanup - file is open, mark for deletion");
                        mDeleteOutputFileOnClose = true;
                    }
                }
            }
        } catch (Exception e) {}
    }

    @Override
    protected void setProcessingStatus(ProcessingStatus processingStatus) {
        super.setProcessingStatus(processingStatus);
        if (mOnProgressListener != null) {
            mOnProgressListener.onProgressUpdate(StegoEncryptionJob.this, getProgressPercent());
        }
    }

    @Override
    protected void onProgressTick() {
        super.onProgressTick();
        if (mOnProgressListener != null) {
            mOnProgressListener.onProgressUpdate(StegoEncryptionJob.this, getProgressPercent());
        }
    }

    private class DummyListenerActivity extends Activity implements PluginNotificationListener {

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

        }
    }
}
