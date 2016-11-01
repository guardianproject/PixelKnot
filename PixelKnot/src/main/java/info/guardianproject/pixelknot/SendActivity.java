package info.guardianproject.pixelknot;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.MediaStore;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import info.guardianproject.pixelknot.adapters.AskForPermissionAdapter;
import info.guardianproject.pixelknot.adapters.OutboxAdapter;
import info.guardianproject.pixelknot.adapters.OutboxViewHolder;
import info.guardianproject.pixelknot.adapters.PhotoAdapter;
import info.guardianproject.pixelknot.views.FadingEditText;
import info.guardianproject.pixelknot.views.FadingPasswordEditText;
import info.guardianproject.pixelknot.views.RoundedImageView;

public class SendActivity extends ActivityBase implements PhotoAdapter.PhotoAdapterListener, OutboxAdapter.OutboxAdapterListener, TabLayout.OnTabSelectedListener, View.OnClickListener {

    private static final boolean LOGGING = false;
    private static final String LOGTAG = "SendActivity";

    private static final int READ_EXTERNAL_STORAGE_PERMISSION_REQUEST = 1;
    private static final int CAPTURE_IMAGE_REQUEST = 2;
    private static final int SHARE_REQUEST = 3;
    private static final int SELECT_FROM_ALBUMS_REQUEST = 4;

    private CoordinatorLayout mRootView;
    private TabLayout mTabs;
    private RecyclerView mRecyclerView;
    private ImageView mPhotoView;
    private File mSelectedImageFile;
    private String mSelectedImageName;
    private FadingEditText mMessage;
    private FadingPasswordEditText mPassword;
    private View mLayoutPassword;
    private Button mBtnPasswordSkip;
    private Button mBtnPasswordSet;
    private AlbumLayoutManager mLayoutManager;
    private View mContainerNewMessage;
    private View mContainerOutbox;
    private View mLayoutGalleryInfo;
    private RoundedImageView mOutboxZoomContainer;

    private static final int FLAG_PHOTO_SET = 1;
    private static final int FLAG_MESSAGE_SET = 2;
    private int mCurrentStatus = 0;

    private StegoEncryptionJob mLastSharedJob;

    /* For animating from gallery to fullscreen */
    private AnimatorSet mCurrentAnimator;
    private int mShortAnimationDuration;

    // Outbox
    private RecyclerView mRecyclerViewOutbox;
    private OutboxAdapter mOutboxAdapter;

    private final Handler mHandler = new Handler();
    private MenuItem mMenuItemDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRootView = (CoordinatorLayout) findViewById(R.id.main_content);
        mContainerNewMessage = mRootView.findViewById(R.id.rlNewMessage);
        mContainerOutbox = mRootView.findViewById(R.id.rlOutbox);

        mTabs = (TabLayout) findViewById(R.id.tabs);
        mTabs.addOnTabSelectedListener(this);

        // Retrieve and cache the system's default "short" animation time.
        mShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_shortAnimTime);

        mPhotoView = (ImageView) findViewById(R.id.selected_image);

        mMessage = (FadingEditText) findViewById(R.id.secret_message);
        mMessage.setSingleLine(true);
        mMessage.setLines(1); // desired number of lines
        mMessage.setMaxLines(5);
        mMessage.setHorizontallyScrolling(false);
        int colorAccent;
        if (Build.VERSION.SDK_INT >= 23)
            colorAccent = getResources().getColor(R.color.colorAccent, getTheme());
        else
            colorAccent = getResources().getColor(R.color.colorAccent);
        mMessage.setHighlightColor(colorAccent);
        mMessage.setOnBackListener(new FadingEditText.OnBackListener() {
            @Override
            public boolean onBackPressed(FadingEditText textView) {
                clearStatusFlag(FLAG_PHOTO_SET);
                mMessage.setText("");
                return false;
            }
        });
        mMessage.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_NULL || actionId == EditorInfo.IME_ACTION_NEXT) {
                    moveToPasswordScreen();
                    return true;
                }
                return false;
            }
        });

        mLayoutPassword = findViewById(R.id.layout_password);

        mPassword = (FadingPasswordEditText) findViewById(R.id.secret_password);
        mPassword.setHighlightColor(colorAccent);
        mPassword.setOnBackListener(new FadingEditText.OnBackListener() {
            @Override
            public boolean onBackPressed(FadingEditText textView) {
                clearStatusFlag(FLAG_MESSAGE_SET);
                mPassword.setText("");
                // Reset to invisible passwords
                mPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                mMessage.post(new Runnable() {
                    @Override
                    public void run() {
                        mMessage.requestFocus();
                    }
                });
                return true;
            }
        });
        mPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_NULL || actionId == EditorInfo.IME_ACTION_DONE) {
                    if (mPassword.length() >= Constants.PASSPHRASE_MIN_LENGTH || mPassword.length() == 0) {
                        // Reset to invisible passwords
                        mPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        onClick(mBtnPasswordSet);
                        return false;
                    }
                    return true; // Dont close, we are not done
                }
                return false;
            }
        });
        mPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                mBtnPasswordSet.setEnabled(editable.length() >= Constants.PASSPHRASE_MIN_LENGTH || editable.length() == 0);
                View error = mLayoutPassword.findViewById(R.id.secret_password_error);
                if (editable.length() > 0 && editable.length() < Constants.PASSPHRASE_MIN_LENGTH) {
                    error.setVisibility(View.VISIBLE);
                } else {
                    error.setVisibility(View.INVISIBLE);
                }
            }
        });
        mBtnPasswordSkip = (Button) mLayoutPassword.findViewById(R.id.btnSkip);
        mBtnPasswordSet = (Button) mLayoutPassword.findViewById(R.id.btnSet);
        mBtnPasswordSet.setOnClickListener(this);
        mBtnPasswordSkip.setOnClickListener(this);

        mLayoutGalleryInfo = mRootView.findViewById(R.id.gallery_info);
        Button btnOk = (Button) mLayoutGalleryInfo.findViewById(R.id.btnGalleryInfoOk);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                App.getInstance().getSettings().setSkipGalleryInfo(true);
                mLayoutGalleryInfo.setVisibility(View.GONE);
            }
        });
        mLayoutGalleryInfo.setVisibility(View.GONE);

        // Set the actual min length value in the UI
        TextView minLengthInfo = (TextView) mLayoutPassword.findViewById(R.id.tvMinLengthInfo);
        minLengthInfo.setText(getString(R.string.secret_password_minlength_info, Constants.PASSPHRASE_MIN_LENGTH));

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_albums);
        int colWidth = getResources().getDimensionPixelSize(R.dimen.photo_column_size);
        mLayoutManager = new AlbumLayoutManager(this, colWidth);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mOutboxZoomContainer = (RoundedImageView) mContainerOutbox.findViewById(R.id.outbox_zoom_container);

        setupOutboxRecyclerView();
        mTabs.getTabAt(0).select();
        setCurrentMode();
        onStatusUpdated();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mLastSharedJob != null)
            mLastSharedJob.setHasBeenShared(false);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.hasExtra("showTab")) {
            try {
                mTabs.getTabAt(intent.getIntExtra("showTab", 0)).select();
            } catch (Exception ignored) {
            }
        } else if (intent.hasExtra("share") && intent.getBooleanExtra("share", false)) {
            Log.d(LOGTAG, "Share");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mRecyclerView != null && mRecyclerView.getAdapter() instanceof PhotoAdapter) {
            ((PhotoAdapter) mRecyclerView.getAdapter()).update();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case READ_EXTERNAL_STORAGE_PERMISSION_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    applyCurrentMode();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.manu_send, menu);
        mMenuItemDone = menu.findItem(R.id.action_done);
        mMenuItemDone.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (hasStatusFlag(FLAG_MESSAGE_SET)) {
                clearStatusFlag(FLAG_MESSAGE_SET);
                return true;
            } else if (hasStatusFlag(FLAG_PHOTO_SET)) {
                clearStatusFlag(FLAG_PHOTO_SET);
                return true;
            }
        } else if (item.getItemId() == R.id.action_done && hasStatusFlag(FLAG_PHOTO_SET) && !hasStatusFlag(FLAG_MESSAGE_SET)) {
            moveToPasswordScreen();
        }
        return super.onOptionsItemSelected(item);
    }

    private void moveToPasswordScreen() {
        setStatusFlag(FLAG_MESSAGE_SET);
    }

    @Override
    public void onClick(View view) {
        if (view == mBtnPasswordSkip)
            mPassword.setText(null);
        if (view == mBtnPasswordSet || view == mBtnPasswordSkip) {

            // Warn for empty passwords
            //
            if (mPassword.getText().length() == 0) {
                AlertDialog.Builder alert = new AlertDialog.Builder(SendActivity.this).setTitle(R.string.confirm_passphrase_override_title).setMessage(R.string.confirm_passphrase_override)
                        .setPositiveButton(R.string.confirm_passphrase_override_continue, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                                createEncryptionJob();
                            }
                        })
                        .setNegativeButton(R.string.confirm_passphrase_override_back, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                alert.show();
            } else {
                createEncryptionJob();
            }
        }
    }

    @Override
    public void onPhotoSelected(String photo, final View thumbView) {
        final Uri uri = Uri.parse(photo);
        if (uri != null) {
            try {
                if (mSelectedImageFile != null) {
                    if (mSelectedImageFile.exists())
                        mSelectedImageFile.delete();
                }
                mSelectedImageFile = App.getInstance().getFileManager().createFileForJob("selected_" + UUID.randomUUID().toString());
                InputStream is;
                if (uri.getScheme() != null && uri.getScheme().contentEquals("content"))
                    is = getContentResolver().openInputStream(uri);
                else
                    is = new FileInputStream(new File(uri.toString()));
                if (is != null) {
                    FileOutputStream fos = new FileOutputStream(mSelectedImageFile, false);
                    byte[] buffer = new byte[8196];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                    fos.close();
                    is.close();
                    mSelectedImageName = uri.getLastPathSegment();
                    Picasso.with(this)
                            .load(mSelectedImageFile)
                            .fit()
                            .centerCrop()
                            .into(mPhotoView, new Callback() {
                                @Override
                                public void onSuccess() {
                                    onPostPhotoSelected(thumbView);
                                }

                                @Override
                                public void onError() {
                                    mPhotoView.setImageURI(uri);
                                    onPostPhotoSelected(thumbView);
                                }
                            });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCameraSelected() {
        takePicture();
    }

    @Override
    public void onAlbumsSelected() {
        Intent intentAlbums = new Intent(this, AlbumsActivity.class);
        startActivityForResult(intentAlbums, SELECT_FROM_ALBUMS_REQUEST);
    }

    private void onPostPhotoSelected(final View thumbView) {
        if (Build.VERSION.SDK_INT >= 16)
            mPhotoView.setImageAlpha(0);
        else
            mPhotoView.setAlpha(0);
        mPhotoView.setBackgroundColor(0x00000000);
        setStatusFlag(FLAG_PHOTO_SET);
        mMessage.requestFocus();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(mMessage, 0, new ResultReceiver(mHandler) {
                        @Override
                        protected void onReceiveResult(int resultCode, Bundle resultData) {
                            super.onReceiveResult(resultCode, resultData);
                            zoomImageFromThumb(mPhotoView, thumbView);
                        }
                    });
                }

            }
        });
    }

    private void setCurrentMode() {
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        if (Build.VERSION.SDK_INT <= 18)
            permissionCheck = PackageManager.PERMISSION_GRANTED; // For old devices we ask in the manifest!
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            AskForPermissionAdapter adapter = new AskForPermissionAdapter(this);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            mRecyclerView.setAdapter(adapter);
        } else {
            applyCurrentMode();
        }
    }

    public void askForReadExternalStoragePermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                READ_EXTERNAL_STORAGE_PERMISSION_REQUEST);
    }

    private void applyCurrentMode() {
        mLayoutGalleryInfo.setVisibility(App.getInstance().getSettings().skipGalleryInfo() ? View.GONE : View.VISIBLE);
        setPhotosAdapter(null, true, true);
    }

    private void setPhotosAdapter(String album, boolean showCamera, boolean showAlbums) {
        mRecyclerView.setLayoutManager(mLayoutManager);
        PhotoAdapter adapter = new PhotoAdapter(this, album, showCamera, showAlbums);
        adapter.setListener(this);
        int colWidth = getResources().getDimensionPixelSize(R.dimen.photo_column_size);
        mLayoutManager.setColumnWidth(colWidth);
        mRecyclerView.setAdapter(adapter);
    }

    private void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            try {
                File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                File image = new File(storageDir, "cameracapture");
                if (image.exists()) {
                    image.delete();
                }
                image.createNewFile();

                Uri photoURI = FileProvider.getUriForFile(this,
                        "info.guardianproject.pixelknot.camera_capture",
                        image);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAPTURE_IMAGE_REQUEST);
            } catch (IOException ignored) {
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_REQUEST && resultCode == RESULT_OK) {
            try {
                File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                File image = new File(storageDir, "cameracapture");
                if (image.exists()) {
                    onPhotoSelected(image.getAbsolutePath(), null);
                    image.delete();
                }
            } catch (Exception ignored) {
            }
        } else if (requestCode == SHARE_REQUEST) {
            // Only remove if we really shared
            if (mLastSharedJob != null) {
                if (mLastSharedJob.getHasBeenShared()) {
                    fadeAndForgetJob(mLastSharedJob);
                }
                mLastSharedJob = null;
            }

        } else if (requestCode == SELECT_FROM_ALBUMS_REQUEST) {
            if (resultCode == RESULT_OK && data != null && data.hasExtra("uri")) {
                onPhotoSelected(data.getStringExtra("uri"), null);
            }
        }
    }

    private void setStatusFlag(int flag) {
        mCurrentStatus |= flag;
        onStatusUpdated();
    }

    private void clearStatusFlag(int flag) {
        mCurrentStatus &= ~flag;
        onStatusUpdated();
        if (flag == FLAG_PHOTO_SET)
            closeKeyboard(null);
    }

    private boolean hasStatusFlag(int flag) {
        return (mCurrentStatus & flag) != 0;
    }

    private void onStatusUpdated() {
        mPhotoView.setVisibility(hasStatusFlag(FLAG_PHOTO_SET) ? View.VISIBLE : View.INVISIBLE);
        mLayoutPassword.setVisibility((hasStatusFlag(FLAG_PHOTO_SET) && hasStatusFlag(FLAG_MESSAGE_SET)) ? View.VISIBLE : View.GONE);
        if (mLayoutPassword.getVisibility() == View.VISIBLE)
            mPassword.requestFocus();
        mMessage.setVisibility((hasStatusFlag(FLAG_PHOTO_SET) && !hasStatusFlag(FLAG_MESSAGE_SET)) ? View.VISIBLE : View.GONE);
        if (mMenuItemDone != null) {
            mMenuItemDone.setVisible(hasStatusFlag(FLAG_PHOTO_SET) && !hasStatusFlag(FLAG_MESSAGE_SET));
        }
        if (hasStatusFlag(FLAG_PHOTO_SET)) {
            mTabs.setVisibility(View.GONE);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.title_new);
            AppBarLayout appBar = (AppBarLayout) findViewById(R.id.appbar);
            appBar.setExpanded(true, false);
            mRecyclerView.getLayoutManager().scrollToPosition(0);
        } else {
            mTabs.setVisibility(View.VISIBLE);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setTitle(R.string.app_name);
        }
    }

    private void createEncryptionJob() {
        // We need to wait until the keyboard has closed and a layout pass has been done, to get the actual size of the imageview that we are
        // animating down into the outbox.
        mTabs.setVisibility(View.VISIBLE);
        closeKeyboard(new Runnable() {
            @Override
            public void run() {
                mContainerOutbox.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            mContainerOutbox.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        } else {
                            mContainerOutbox.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        }

                        int countShown = App.getInstance().getSettings().sendingDialogCount();
                        if (countShown < Constants.MAX_SENDING_DIALOG_COUNT) {
                            // Update count
                            countShown++;
                            App.getInstance().getSettings().setSendingDialogCount(countShown);

                            AlertDialog.Builder alert = new AlertDialog.Builder(SendActivity.this).setTitle(R.string.message_encrypting_title).setMessage(R.string.message_encrypting).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            doCreateEncryptionJob();
                                        }
                                    });
                                }
                            });
                            alert.show();
                        } else {
                            doCreateEncryptionJob();
                        }
                    }
                });
                mContainerOutbox.requestLayout();
            }
        });
    }

    private void doCreateEncryptionJob() {
        String imageName = mSelectedImageName;
        if (TextUtils.isEmpty(imageName)) {
            imageName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".jpg";
        }
        StegoEncryptionJob job = new StegoEncryptionJob(App.getInstance(), mSelectedImageFile, imageName, mMessage.getText().toString(), mPassword.getText().toString());
        App.getInstance().storeJob(job);

        // Cover the screen with dark blue at the moment, will zoom down to outbox once the image is loaded!
        mOutboxZoomContainer.setBackgroundResource(R.drawable.main_background);
        mOutboxZoomContainer.setTranslationX(0);
        mOutboxZoomContainer.setTranslationY(0);
        mOutboxZoomContainer.setScaleX(1.0f);
        mOutboxZoomContainer.setScaleY(1.0f);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mOutboxZoomContainer.getLayoutParams(); // Make it square, so that zoom down animation looks smooth (the target is square)
        params.width = params.height = Math.max(mContainerOutbox.getWidth(), mContainerOutbox.getHeight());
        mOutboxZoomContainer.setLayoutParams(params);
        mOutboxZoomContainer.setVisibility(View.VISIBLE);

        reset();
        animateImageToOutboxJob(job);
    }

    private void closeKeyboard(final Runnable callback) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        Handler threadHandler = new Handler();
        if (!imm.hideSoftInputFromWindow(mRootView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS, new ResultReceiver(threadHandler) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                super.onReceiveResult(resultCode, resultData);
                mRootView.requestFocus();
                if (callback != null)
                    mHandler.post(callback);
            }
        })) {
            mRootView.requestFocus(); // Keyboard not open
            if (callback != null)
                mHandler.post(callback);
        }
    }

    private void reset() {
        mPhotoView.setImageBitmap(null);
        mPassword.setText("");
        mMessage.setText("");
        mSelectedImageName = null;
        if (mSelectedImageFile != null) {
            if (mSelectedImageFile.exists())
                mSelectedImageFile.delete();
            mSelectedImageFile = null;
        }
        mCurrentStatus = 0;
        onStatusUpdated();
        setCurrentMode();
        mTabs.getTabAt(1).select();
    }

    /* Taken from https://developer.android.com/training/animation/zoom.html */
    private void zoomImageFromThumb(final ImageView expandedImageView, final View thumbView) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        expandedImageView.setBackgroundColor(0xffffffff);
        if (Build.VERSION.SDK_INT >= 16)
            expandedImageView.setImageAlpha(255);
        else
            expandedImageView.setAlpha(1);

        if (thumbView == null) {
            return;
        }

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        findViewById(R.id.main_content)
                .getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScale;
        if ((float) finalBounds.width() / finalBounds.height()
                > (float) startBounds.width() / startBounds.height()) {
            // Extend start bounds horizontally
            startScale = (float) startBounds.height() / finalBounds.height();
            float startWidth = startScale * finalBounds.width();
            float deltaWidth = (startWidth - startBounds.width()) / 2;
            startBounds.left -= deltaWidth;
            startBounds.right += deltaWidth;
        } else {
            // Extend start bounds vertically
            startScale = (float) startBounds.width() / finalBounds.width();
            float startHeight = startScale * finalBounds.height();
            float deltaHeight = (startHeight - startBounds.height()) / 2;
            startBounds.top -= deltaHeight;
            startBounds.bottom += deltaHeight;
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        //thumbView.setAlpha(0f);
        expandedImageView.setVisibility(View.VISIBLE);

        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
                        startBounds.left, finalBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                        startBounds.top, finalBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                        startScale, 1f)).with(ObjectAnimator.ofFloat(expandedImageView,
                View.SCALE_Y, startScale, 1f));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
            }
        });
        set.start();
        mCurrentAnimator = set;
    }

    private void showOutbox() {
        mContainerNewMessage.setVisibility(View.INVISIBLE);
        mContainerOutbox.setVisibility(View.VISIBLE);

    }

    private void showNewMessage() {
        mContainerOutbox.setVisibility(View.INVISIBLE);
        mContainerNewMessage.setVisibility(View.VISIBLE);
        if (mRecyclerView != null && mRecyclerView.getAdapter() instanceof PhotoAdapter) {
            ((PhotoAdapter) mRecyclerView.getAdapter()).update();
        }
    }

    private void setupOutboxRecyclerView() {
        mRecyclerViewOutbox = (RecyclerView) findViewById(R.id.recycler_view_outbox);
        mRecyclerViewOutbox.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mOutboxAdapter = new OutboxAdapter(this);
        mOutboxAdapter.setListener(this);
        mOutboxAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if (mOutboxAdapter.getItemCount() == 0) {
                    stopTimer();
                } else {
                    startTimer();
                }
            }
        });
        mRecyclerViewOutbox.setAdapter(mOutboxAdapter);

        ItemTouchHelper.Callback callback = new ItemTouchHelper.Callback() {
            @Override
            public boolean isLongPressDragEnabled() {
                return false;
            }

            @Override
            public boolean isItemViewSwipeEnabled() {
                return true;
            }

            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                int dragFlags = 0;
                int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
                return makeMovementFlags(dragFlags, swipeFlags);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                final StegoJob job = ((OutboxViewHolder) viewHolder).getJob();
                Snackbar.make(viewHolder.itemView, R.string.outbox_item_deleted, Snackbar.LENGTH_LONG)
                        .setAction(R.string.outbox_item_deleted_undo, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                App.getInstance().storeJob(job); // Put it back in list
                                job.Run();
                            }
                        }).show();
                App.getInstance().forgetJob(((OutboxViewHolder) viewHolder).getJob(), false);
            }
        };
        new ItemTouchHelper(callback).attachToRecyclerView(mRecyclerViewOutbox);
    }

    @Override
    public void onOutboxItemClicked(StegoEncryptionJob job) {
        if (job.getProcessingStatus() == StegoJob.ProcessingStatus.EMBEDDED_SUCCESSFULLY) {
            mLastSharedJob = job;

            //Intent intent = new Intent();
            //intent.setAction(Intent.ACTION_SEND);
            //intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            //intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(PixelKnotContentProvider.CONTENT_URI + job.getId()));
            //intent.setType("*/*");
            //Intent chooser = Intent.createChooser(intent, "test");
            //startActivityForResult(chooser, SHARE_REQUEST);

            ShareChooserDialog.createChooser(mRootView, this, SHARE_REQUEST, Uri.parse(PixelKnotContentProvider.CONTENT_URI + job.getId()));
        } else if (job.getProcessingStatus() == StegoJob.ProcessingStatus.ERROR) {
            job.Run(); // retry
        }
    }

    private void animateImageToOutboxJob(final StegoEncryptionJob job) {
        final int position = App.getInstance().getJobs().indexOf(job);
        if (position >= 0) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Bitmap bmp = null;
                    try {
                        int viewSize = UIHelpers.dpToPx(180, SendActivity.this);
                        bmp = Picasso.with(SendActivity.this)
                                .load(job.getBitmapFile())
                                .resize(viewSize, viewSize)
                                .centerCrop()
                                .get();
                    } catch (Exception ignored) {
                    }
                    final Bitmap finalBmp = bmp;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mOutboxZoomContainer.setImageBitmap(finalBmp);
                            mRecyclerViewOutbox.getLayoutManager().scrollToPosition(position);
                            mRecyclerViewOutbox.post(new Runnable() {
                                @Override
                                public void run() {
                                    OutboxViewHolder viewHolder = (OutboxViewHolder) mRecyclerViewOutbox.findViewHolderForAdapterPosition(position);
                                    if (viewHolder != null) {
                                        zoomImageToThumb(mOutboxZoomContainer, viewHolder.getPhotoView());
                                    }
                                }
                            });
                        }
                    });
                }
            }).start();
        }
    }

    private void zoomImageToThumb(final RoundedImageView expandedImageView, final View thumbView) {
        // If there's an animation in progress, cancel it
        // immediately and proceed with this one.
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
        }

        expandedImageView.setBackgroundColor(Color.TRANSPARENT);

        // Calculate the starting and ending bounds for the zoomed-in image.
        // This step involves lots of math. Yay, math.
        final Rect startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the container
        // view. Also set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBounds);
        findViewById(R.id.rlOutbox)
                .getGlobalVisibleRect(finalBounds, globalOffset);

        expandedImageView.getDrawingRect(finalBounds);
        ((ViewGroup)mContainerOutbox).offsetDescendantRectToMyCoords(expandedImageView, finalBounds);

        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);

        // Adjust the start bounds to be the same aspect ratio as the final
        // bounds using the "center crop" technique. This prevents undesirable
        // stretching during the animation. Also calculate the start scaling
        // factor (the end scaling factor is always 1.0).
        float startScaleX = (float) startBounds.width() / finalBounds.width();
        float startScaleY = (float) startBounds.height() / finalBounds.height();

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it will position the zoomed-in view in the place of the
        // thumbnail.
        thumbView.setAlpha(0f);
        expandedImageView.setVisibility(View.VISIBLE);
        // Set the pivot point for SCALE_X and SCALE_Y transformations
        // to the top-left corner of the zoomed-in view (the default
        // is the center of the view).
        expandedImageView.setPivotX(0f);
        expandedImageView.setPivotY(0f);

        // Construct and run the parallel animation of the four translation and
        // scale properties (X, Y, SCALE_X, and SCALE_Y).
        AnimatorSet set = new AnimatorSet();
        set
                .play(ObjectAnimator.ofFloat(expandedImageView, View.X,
                        finalBounds.left, startBounds.left))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
                        finalBounds.top, startBounds.top))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
                        1f, startScaleX))
                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_Y,
                        1f, startScaleY))
                .with(ObjectAnimator.ofFloat(expandedImageView, "rounding", 0f, 1f))
                .with(ObjectAnimator.ofFloat(expandedImageView, "lightFilter", 0f, getResources().getFraction(R.fraction.outbox_lightness_filter, 1, 1)));
        set.setDuration(mShortAnimationDuration);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentAnimator = null;
                thumbView.setAlpha(1);
                expandedImageView.setVisibility(View.INVISIBLE);
                expandedImageView.setImageBitmap(null);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentAnimator = null;
                thumbView.setAlpha(1);
                expandedImageView.setVisibility(View.INVISIBLE);
                expandedImageView.setImageBitmap(null);
            }

            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
            }
        });
        set.start();
        mCurrentAnimator = set;
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        if (tab.getPosition() == 0)
            showNewMessage();
        else
            showOutbox();
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
        onTabSelected(tab);
    }

    private void fadeAndForgetJob(final StegoEncryptionJob job) {
        boolean animationStarted = false;
        final int position = App.getInstance().getJobs().indexOf(job);
        if (position >= 0) {
            OutboxViewHolder viewHolder = (OutboxViewHolder) mRecyclerViewOutbox.findViewHolderForAdapterPosition(position);
            if (viewHolder != null) {
                viewHolder.getStatusTextView().setText(R.string.sending);
                animationStarted = true;
                Animation a = AnimationUtils.loadAnimation(this, R.anim.outbox_fade_job);
                a.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        onJobSent(job);
                        App.getInstance().forgetJob(job, true);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                viewHolder.itemView.startAnimation(a);
            }
        }
        if (!animationStarted) {
            // No animation, so just remove immediately
            onJobSent(job);
            App.getInstance().forgetJob(job, true);
        }
    }

    private void onJobSent(StegoJob job) {
        if (!App.getInstance().getSettings().skipSentDialog()) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this).setTitle(R.string.image_sent_title).setMessage(R.string.image_sent);
            final View view = LayoutInflater.from(this).inflate(R.layout.dialog_sent, null, false);
            alert.setView(view);
            alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    CheckBox cb = (CheckBox) view.findViewById(R.id.cbDontShowAgain);
                    if (cb.isChecked()) {
                        App.getInstance().getSettings().setSkipSentDialog(true);
                    }
                }
            });
            alert.show();
        }
    }

    private final Runnable mUpdateTimestampsInOutboxRunnable = new Runnable() {
        @Override
        public void run() {
            LinearLayoutManager lm = (LinearLayoutManager)mRecyclerViewOutbox.getLayoutManager();
            int first = lm.findFirstVisibleItemPosition();
            int last = lm.findLastVisibleItemPosition();
            if (first != RecyclerView.NO_POSITION && last != RecyclerView.NO_POSITION) {
                for (int i = first; i <= last; i++) {
                    OutboxViewHolder vh = (OutboxViewHolder) mRecyclerViewOutbox.findViewHolderForAdapterPosition(i);
                    if (vh != null) {
                        StegoEncryptionJob job = vh.getJob();
                        vh.getTimestampTextView().setText(UIHelpers.dateDiffDisplayString(job.getCreationDate(), SendActivity.this,
                                R.string.outbox_item_created_recently,
                                    R.string.outbox_item_created_recently, R.string.outbox_item_created_minutes, R.string.outbox_item_created_minute, R.string.outbox_item_created_hours,
                                    R.string.outbox_item_created_hour, R.string.outbox_item_created_days, R.string.outbox_item_created_day));
                    }
                }
            }
            mRecyclerViewOutbox.postDelayed(mUpdateTimestampsInOutboxRunnable, 2000); // 20 seconds
        }
    };

    private void startTimer() {
        mRecyclerViewOutbox.removeCallbacks(mUpdateTimestampsInOutboxRunnable);
        mRecyclerViewOutbox.post(mUpdateTimestampsInOutboxRunnable);
    }

    private void stopTimer() {
        mRecyclerViewOutbox.removeCallbacks(mUpdateTimestampsInOutboxRunnable);
    }
}
