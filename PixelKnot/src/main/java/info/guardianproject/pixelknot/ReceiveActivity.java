package info.guardianproject.pixelknot;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import info.guardianproject.pixelknot.views.CircularProgress;
import info.guardianproject.pixelknot.views.ColorFilterImageView;
import info.guardianproject.pixelknot.views.FadingPasswordEditText;

public class ReceiveActivity extends ActivityBase implements StegoDecryptionJob.OnProgressListener {

    private static final boolean LOGGING = false;
    private static final String LOGTAG = "ReceiveActivity";

    private View mRootView;
    private ColorFilterImageView mPhotoView;
    private TextView mMessage;
    private FadingPasswordEditText mPassword;
    private Button mBtnUnlock;
    private StegoDecryptionJob mJob;
    private File mOutputFile;
    private View mLayoutPassword;
    private View mLayoutProcessing;
    private CircularProgress mProgress;
    private TextView mProgressText;
    private View mPasswordError;

    private enum Mode {
        ERROR,
        IDLE,
        PROCESSING,
        SHOWING
    }

    ;
    private Mode mCurrentMode = Mode.IDLE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final ColorDrawable cd = new ColorDrawable(getResources().getColor(R.color.colorPrimary));
        cd.setAlpha(0);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(cd);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("");

        mRootView = findViewById(R.id.main_content);
        mPhotoView = (ColorFilterImageView) mRootView.findViewById(R.id.selected_image);

        mLayoutPassword = mRootView.findViewById(R.id.layout_password);

        mMessage = (TextView) findViewById(R.id.secret_message);
        mMessage.setAutoLinkMask(Linkify.WEB_URLS);
        mMessage.setMovementMethod(LinkMovementMethod.getInstance());

        mPassword = (FadingPasswordEditText) findViewById(R.id.secret_password);
        mPassword.setDisclosureIndicator(R.drawable.ic_eye_primary_18dp);
        mPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_NULL || actionId == EditorInfo.IME_ACTION_DONE) {
                    createDecryptionJob();
                    closeKeyboard();
                }
                return false;
            }
        });
        mPasswordError = findViewById(R.id.secret_password_error);

        mBtnUnlock = (Button) findViewById(R.id.btnUnlock);
        mBtnUnlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createDecryptionJob();
                closeKeyboard();
            }
        });

        mLayoutProcessing = findViewById(R.id.layout_processing);
        mProgress = (CircularProgress) findViewById(R.id.progress);
        mProgress.setMax(100);
        mProgressText = (TextView) findViewById(R.id.tvProgress);

        setMode(Mode.IDLE);
        handleIntentData(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntentData(intent);
    }

    @Override
    public void finish() {
        try {
            if (mOutputFile != null && mOutputFile.exists())
                mOutputFile.delete();
        } catch (Exception e) {
        }
        super.finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleIntentData(Intent intent) {
        if (intent.getType() != null && intent.getType().indexOf("image/") != -1) {
            Uri uri = null;
            if (intent.getData() != null && intent.getData() instanceof Uri) {
                uri = (Uri) intent.getData();
            } else if (intent.getDataString() != null) {
                uri = Uri.parse(intent.getDataString());
            } else if (intent.hasExtra(Intent.EXTRA_STREAM)) {
                uri = (Uri) intent.getExtras().getParcelable(Intent.EXTRA_STREAM);
            } else if (intent.hasExtra(Intent.EXTRA_TEXT)) {
                uri = Uri.parse(intent.getStringExtra(Intent.EXTRA_TEXT));
            }
            if (uri != null) {
                try {
                    if (mOutputFile == null)
                        mOutputFile = App.getInstance().getFileManager().createFileForJob("inbox");
                    InputStream is = getContentResolver().openInputStream(uri);
                    if (is != null) {
                        FileOutputStream fos = new FileOutputStream(mOutputFile, false);
                        byte[] buffer = new byte[1024]; // Adjust if you want
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                        }
                        fos.close();
                        is.close();
                        Picasso.with(this).load(mOutputFile).into(mPhotoView, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {

                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setMode(Mode mode) {
        mCurrentMode = mode;
        mMessage.setVisibility(mode == Mode.SHOWING ? View.VISIBLE : View.GONE);
        mLayoutPassword.setVisibility((mode == Mode.IDLE || mode == Mode.ERROR) ? View.VISIBLE : View.GONE);
        mLayoutProcessing.setVisibility((mode == Mode.PROCESSING) ? View.VISIBLE : View.GONE);
        mPasswordError.setVisibility(mode == Mode.ERROR ? View.VISIBLE : View.GONE);
        if (mode == Mode.PROCESSING)
            mProgress.startAnimating();
        else
            mProgress.stopAnimating();
    }

    private void createDecryptionJob() {
        if (mJob != null && mJob.getThread() != null) {
            mJob.getThread().requestInterrupt();
        }
        setMode(Mode.PROCESSING);
        mJob = new StegoDecryptionJob(App.getInstance(), mOutputFile, mPassword.getText().toString());
        mJob.setOnProgressListener(this);
        onProgressUpdate(mJob, 0);
    }

    private void closeKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        Handler threadHandler = new Handler();
        if (!imm.hideSoftInputFromWindow(mRootView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS, new ResultReceiver(threadHandler) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                super.onReceiveResult(resultCode, resultData);
                mRootView.requestFocus();
            }
        })) {
            mRootView.requestFocus(); // Keyboard not open
        }
    }

    @Override
    public void onProgressUpdate(final StegoDecryptionJob job, final int percent) {
        if (LOGGING)
            Log.d(LOGTAG, "onProgressUpdate: " + percent);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressText.setText("" + percent + "%");
                mProgress.setProgress(percent);
                mPhotoView.setFilterCurrent((float) percent / 100f);
                if (job.getProcessingStatus() == StegoJob.ProcessingStatus.EXTRACTED_SUCCESSFULLY) {
                    if (mJob != null) {
                        mJob.cleanup();
                        mJob = null;
                    }
                    setMessageText(job.getMessage());
                    setMode(Mode.SHOWING);
                } else if (job.getProcessingStatus() == StegoJob.ProcessingStatus.ERROR) {
                    mPassword.setText("");
                    setMode(Mode.ERROR);
                }
            }
        });
    }

    private void setMessageText(String message) {
        mMessage.setLinksClickable(true);
        mMessage.setText(message);
    }
}