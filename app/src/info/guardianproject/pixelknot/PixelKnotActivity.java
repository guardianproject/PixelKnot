package info.guardianproject.pixelknot;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.text.util.Linkify;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import info.guardianproject.f5android.Embed;
import info.guardianproject.f5android.Embed.EmbedListener;
import info.guardianproject.f5android.Extract;
import info.guardianproject.f5android.Extract.ExtractionListener;
import info.guardianproject.f5android.F5Buffers.F5Notification;
import info.guardianproject.pixelknot.Constants.PixelKnot.Keys;
import info.guardianproject.pixelknot.Constants.Screens.Loader;
import info.guardianproject.pixelknot.crypto.Aes;
import info.guardianproject.pixelknot.crypto.Apg;
import info.guardianproject.pixelknot.screens.CoverImageFragment;
import info.guardianproject.pixelknot.screens.DecryptImageFragment;
import info.guardianproject.pixelknot.screens.PixelKnotLoader;
import info.guardianproject.pixelknot.screens.SetMessageFragment;
import info.guardianproject.pixelknot.screens.ShareFragment;
import info.guardianproject.pixelknot.screens.StegoImageFragment;
import info.guardianproject.pixelknot.utils.ActivityListener;
import info.guardianproject.pixelknot.utils.FragmentListener;
import info.guardianproject.pixelknot.utils.IO;
import info.guardianproject.pixelknot.utils.Image;
import info.guardianproject.pixelknot.utils.PixelKnotMediaScanner;
import info.guardianproject.pixelknot.utils.PixelKnotMediaScanner.MediaScannerListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Vector;

import javax.crypto.Cipher;

@SuppressLint("NewApi")
public class PixelKnotActivity extends SherlockFragmentActivity implements F5Notification, Constants, FragmentListener, ViewPager.OnPageChangeListener, OnGlobalLayoutListener, MediaScannerListener, EmbedListener, ExtractionListener {
	private PKPager pk_pager;
	private ViewPager view_pager;

	File dump;
	int d, d_, last_diff, scale;
	private String last_locale;

	private View activity_root;
	private LinearLayout options_holder, action_display, action_display_;
	private LinearLayout progress_holder;
	private ActionBar action_bar;

	private static final String LOG = Logger.UI;

	private PixelKnot pixel_knot = new PixelKnot();

	PixelKnotLoader loader;
	ProgressDialog please_wait;

	Handler h = new Handler();
	InputMethodManager imm;
	boolean keyboardIsShowing = false;

	boolean hasSeenFirstPage = false;
	boolean hasSuccessfullyEmbed = false;
	boolean hasSuccessfullyExtracted = false;
	boolean hasSuccessfullyEncrypted = false;
	boolean hasSuccessfullyDecrypted = false;
	boolean hasSuccessfullyPasswordProtected = false;
	boolean hasSuccessfullyUnlocked = false;
	boolean isDecryptOnly = false;
	boolean canAutoAdvance = false;

	private List<TrustedShareActivity> trusted_share_activities;
	public ActivityManager am;
	
	private int steps_taken = 0;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.d(LOG, "onCreate (main) called");
		
		dump = new File(DUMP);
		if(!dump.exists())
			dump.mkdir();

		d = R.drawable.progress_off_selected;
		d_ = R.drawable.progress_on;
		
		action_bar = getSupportActionBar();
		View action_bar_root = LayoutInflater.from(this).inflate(R.layout.action_bar, null);
		action_bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		action_bar.setCustomView(action_bar_root);
		
		setContentView(R.layout.pixel_knot_activity);
		
		am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
		
		imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

		options_holder = (LinearLayout) findViewById(R.id.options_holder);

		action_display = (LinearLayout) findViewById(R.id.action_display);
		action_display_ = (LinearLayout) action_bar_root.findViewById(R.id.action_display_);

		progress_holder = (LinearLayout) findViewById(R.id.progress_holder);

		List<Fragment> fragments = new Vector<Fragment>();

		if(Intent.ACTION_MAIN.equals(getIntent().getAction())) {
			Fragment cover_image_fragment = Fragment.instantiate(this, CoverImageFragment.class.getName());
			Fragment set_message_fragment = Fragment.instantiate(this, SetMessageFragment.class.getName());
			Fragment share_fragment = Fragment.instantiate(this, ShareFragment.class.getName());

			fragments.add(0, cover_image_fragment);
			fragments.add(1, set_message_fragment);
			fragments.add(2, share_fragment);
		} else {
			Log.d(LOG, "this type: " + getIntent().getType());
			
			setIsDecryptOnly(true);
			
			Fragment stego_image_fragment = Fragment.instantiate(this, StegoImageFragment.class.getName());
			Bundle args = new Bundle();
			
			try {
				args.putString(Keys.COVER_IMAGE_NAME, IO.pullPathFromUri(this, getIntent().getData()));
			} catch(NullPointerException e) {
				if(getIntent().hasExtra(Intent.EXTRA_STREAM)) {
					args.putString(Keys.COVER_IMAGE_NAME, IO.pullPathFromUri(this, (Uri) getIntent().getExtras().get(Intent.EXTRA_STREAM)));
				} else if(getIntent().hasExtra(Intent.EXTRA_TEXT)) {
					args.putString(Keys.COVER_IMAGE_NAME, IO.pullPathFromUri(this, Uri.parse(getIntent().getStringExtra(Intent.EXTRA_TEXT))));
				}
				
			}
			
			stego_image_fragment.setArguments(args);

			Fragment decrypt_image_fragment = Fragment.instantiate(this, DecryptImageFragment.class.getName());
			Fragment share_fragment = Fragment.instantiate(this, ShareFragment.class.getName());

			fragments.add(0, stego_image_fragment);
			fragments.add(1, decrypt_image_fragment);
			fragments.add(2, share_fragment);
		}

		pk_pager = new PKPager(getSupportFragmentManager(), fragments);
		view_pager = (ViewPager) findViewById(R.id.fragment_holder);
		view_pager.setAdapter(pk_pager);
		view_pager.setOnPageChangeListener(this);

		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		lp.setMargins(5, 0, 5, 0);

		for(int p=0; p<fragments.size(); p++) {
			ImageView progress_view = new ImageView(this);
			progress_view.setLayoutParams(lp);
			progress_view.setBackgroundResource(p == 0 ? d_ : d);
			progress_holder.addView(progress_view);
		}

		last_diff = 0;
		activity_root = findViewById(R.id.activity_root);
		activity_root.getViewTreeObserver().addOnGlobalLayoutListener(this);
		
		last_locale = PreferenceManager.getDefaultSharedPreferences(this).getString(Settings.LANGUAGE, "0");
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		Log.d(LOG, "onResume (main) called");
				
		String currentLocale = PreferenceManager.getDefaultSharedPreferences(this).getString(Settings.LANGUAGE, "0");
		if(!last_locale.equals(currentLocale))
			setNewLocale(currentLocale);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		doWait(false);
		Log.d(LOG, "onDestroy (main) called");
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		MenuInflater mi = getSupportMenuInflater();
		mi.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.pk_about:
			showAbout();
			return true;
		case R.id.pk_preferences:
			last_locale = PreferenceManager.getDefaultSharedPreferences(this).getString(Settings.LANGUAGE, "0");
			startActivity(new Intent(this, Preferences.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void restart() {
		Log.d(LOG, "RESTARTING?");
		h.post(new Runnable() {
			@Override
			public void run() {
				getIntent().setData(null);

				Intent intent = getIntent();
				intent.setAction(Intent.ACTION_MAIN);
				
				if(intent.hasExtra(Intent.EXTRA_STREAM))
					intent.removeExtra(Intent.EXTRA_STREAM);
				else if(intent.hasExtra(Intent.EXTRA_TEXT))
					intent.removeExtra(Intent.EXTRA_TEXT);
			
				
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
				overridePendingTransition(0, 0);
				finish();

				overridePendingTransition(0, 0);
				startActivity(intent);
			}
		});
	}
	
	private void showAbout() {
		AlertDialog.Builder ad = new AlertDialog.Builder(this);
		View about = LayoutInflater.from(this).inflate(R.layout.about_fragment, null);
		
		TextView about_gp_email = (TextView) about.findViewById(R.id.about_gp_email);
		about_gp_email.setText(Html.fromHtml("<a href='mailto:" + about_gp_email.getText().toString() + "'>" + about_gp_email.getText().toString() + "</a>"));
		
		TextView about_version = (TextView) about.findViewById(R.id.about_version);
		try {
			about_version.setText(getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
		} catch (NameNotFoundException e) {
			Log.e(LOG, e.toString());
			e.printStackTrace();
			about_version.setText("1.0");
		}
		
		LinearLayout license_holder = (LinearLayout) about.findViewById(R.id.about_license_holder);
		String[] licenses = getResources().getStringArray(R.array.about_software);
		String[] licenses_ = getResources().getStringArray(R.array.about_software_);
		for(int l=0; l<licenses.length; l++) {
			TextView license = new TextView(this);
			license.setText(licenses[l]);
			license.setTextSize(20);
			
			TextView license_ = new TextView(this);
			license_.setText(Html.fromHtml("<a href='" + licenses_[l] + "'>" + licenses_[l] + "</a>"));
			license_.setLinksClickable(true);
			Linkify.addLinks(license_, Linkify.ALL);
			license_.setPadding(0, 0, 0, 30);
			license_.setTextSize(20);
			
			license_holder.addView(license);
			license_holder.addView(license_);
		}
		
		
		
		ad.setView(about);
		ad.setPositiveButton(getString(R.string.ok), null);
		ad.show();
	}

	@Override
	public void share() {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_SEND);
		intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(pixel_knot.out_file));
		intent.setType("image/jpeg");
		startActivity(Intent.createChooser(intent, getString(R.string.embed_success)));
	}

	private void rearrangeForKeyboard(boolean state) {
		try {
			action_display_.removeView(options_holder);
			action_display.removeView(options_holder);
		} catch(NullPointerException e) {}

		if(state) {
			action_display.setVisibility(View.GONE);

			action_display_.setVisibility(View.VISIBLE);
			action_display_.addView(options_holder, action_display_.getChildCount());
			keyboardIsShowing = true;
		} else {
			action_display_.setVisibility(View.GONE);

			action_display.setVisibility(View.VISIBLE);
			action_display.addView(options_holder, action_display.getChildCount());
			keyboardIsShowing = false;
		}
	}
	
	@Override
	public void showKeyboard(View target) {
		if(!keyboardIsShowing) {
			imm.toggleSoftInput(0, InputMethodManager.SHOW_IMPLICIT);
			target.requestFocus();
		}
	}
	
	@Override
	public void hideKeyboard() {
		if(keyboardIsShowing)
			imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
	}
	
	public class TrustedShareActivity {
		public String app_name, package_name;
		public Drawable icon;
		public Intent intent;
		public View view;

		public TrustedShareActivity() {}

		public TrustedShareActivity(String app_name, String package_name) {
			this.app_name = app_name;
			this.package_name = package_name;			
		}

		public void setIcon(Drawable icon) {
			this.icon = icon;
		}

		private void setIntent() {
			intent = new Intent(Intent.ACTION_SEND)
				.setType("image/jpeg")
				.setPackage(package_name)
				.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(pixel_knot.out_file));

			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					PixelKnotActivity.this.startActivity(intent);
				}
			});
		}

		public void createView() {
			view = LayoutInflater.from(PixelKnotActivity.this).inflate(R.layout.trusted_share_activity_view, null);

			TextView app_name_holder = (TextView) view.findViewById(R.id.tsa_title);
			app_name_holder.setText(app_name);

			ImageView icon_holder = (ImageView) view.findViewById(R.id.tsa_icon);
			icon_holder.setImageDrawable(icon);
		}
	}

	public class PixelKnot extends JSONObject {
		String cover_image_name = null;
		String secret_message = null;

		String password = null;

		boolean can_save = false;
		boolean has_encryption = false;
		boolean password_override = false;

		int capacity = 0;
		Embed embed = null;
		File out_file = null;

		Apg apg = null;

		public PixelKnot() {
			try {
				put(Keys.CAPACITY, capacity);
			} catch (JSONException e) {
				Log.e(LOG, e.toString());
				e.printStackTrace();
			}
		}

		private boolean checkIfReadyToSave() {
			try {
				if(getString(Keys.COVER_IMAGE_NAME) != null && getString(Keys.SECRET_MESSAGE) != null)
					return true;
			} catch(JSONException e) {}

			return false;
		}

		public void setCoverImageName(String cover_image_name) {
			this.cover_image_name = cover_image_name;
			try {
				put(Keys.COVER_IMAGE_NAME, cover_image_name);
				put(Keys.CAPACITY, capacity);
			} catch (JSONException e) {}


			can_save = checkIfReadyToSave();
		}

		public void setSecretMessage(String secret_message) {
			this.secret_message = secret_message;
			try {
				if(!secret_message.equals(""))
					put(Keys.SECRET_MESSAGE, secret_message);
				else {
					this.secret_message = null;
					if(has(Keys.SECRET_MESSAGE))
						remove(Keys.SECRET_MESSAGE);
				}

			} catch(JSONException e) {}

			can_save = checkIfReadyToSave();
		}
		
		public void setPasswordOverride(boolean password_override) {
			this.password_override = password_override;
			if(password_override && getPassword())
				remove(Keys.PASSWORD);
				
		}
		
		public boolean getPasswordOverride() {
			return this.password_override;
		}

		public void setPassword(String password) {
			this.password = password;
			this.has_encryption = false;

			try {
				put(Keys.PASSWORD, password);
				if(has(Keys.HAS_ENCRYPTION))
					remove(Keys.HAS_ENCRYPTION);
			} catch(JSONException e) {}
		}

		public boolean getPassword() {
			if(has(Keys.PASSWORD))
				return true;

			return false;
		}

		public void setEncryption(boolean has_encryption, Apg apg) {
			this.has_encryption = has_encryption;
			if(has_encryption) {
				this.password = null;
				this.apg = apg;

				try {
					put(Keys.HAS_ENCRYPTION, true);
					if(has(Keys.PASSWORD))
						remove(Keys.PASSWORD);

				} catch(JSONException e) {}
			} else {
				if(has(Keys.HAS_ENCRYPTION))
					remove(Keys.HAS_ENCRYPTION);
			}
		}

		public boolean getEncryption() {
			return has_encryption;
		}

		public void setCapacity(int capacity) {
			this.capacity = capacity;
			try {
				put(Keys.CAPACITY, capacity);
			} catch (JSONException e) {}
		}

		public void clear() {
			cover_image_name = null;
			secret_message = null;
			password = null;
			can_save = false;
			has_encryption = false;
			capacity = 0;
			out_file = null;
			apg = null;

			@SuppressWarnings("unchecked")
			Iterator<String> keys = keys();
			List<String> keys_ = new ArrayList<String>();
			while(keys.hasNext()) 
				keys_.add(keys.next());

			for(String key : keys_)
				remove(key);

			try {
				put(Keys.CAPACITY, capacity);
			} catch (JSONException e) {}
		}

		public void save() {
			if(hasSuccessfullyEmbed)
				return;

			loader = new PixelKnotLoader(PixelKnotActivity.this);
			loader.show();
			

			if(pixel_knot.getPassword() && !hasSuccessfullyPasswordProtected) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							loader.init(Loader.Steps.ENCRYPT);
							onUpdate();
							Entry<String, String> pack = Aes.EncryptWithPassword(pixel_knot.getString(Keys.PASSWORD), secret_message).entrySet().iterator().next();

							secret_message = PASSWORD_SENTINEL.concat(new String(pack.getKey())).concat(pack.getValue());
							hasSuccessfullyPasswordProtected = true;
							h.post(new Runnable() {
								@Override
								public void run() {
									try {
										loader.finish();
									} catch(NullPointerException e) {}
									save();
								}
							});
						} catch (JSONException e) {}
					}
				}).start();
			}

			if(pixel_knot.getEncryption() && !hasSuccessfullyEncrypted) {
				apg.encrypt(PixelKnotActivity.this, pixel_knot.secret_message);
				h.post(new Runnable() {
					@Override
					public void run() {
						try {
							loader.finish();
						} catch(NullPointerException e) {}
					}
				});

			}

			if((!pixel_knot.has_encryption && !pixel_knot.getPassword()) || (hasSuccessfullyEncrypted || hasSuccessfullyPasswordProtected)) {
				loader.init(Loader.Steps.EMBED);
				
				new Thread(new Runnable() {
					@Override
					public void run() {
						pixel_knot.setCoverImageName(Image.downsampleImage(pixel_knot.cover_image_name, dump));
						
						@SuppressWarnings("unused")
						Embed embed = new Embed(PixelKnotActivity.this, dump.getName(), cover_image_name, secret_message);
					}
				}).start();
			}

		}

		public void extract() {
			Log.d(LOG, "now extracting " + cover_image_name);
			pixel_knot.out_file = new File(cover_image_name);
			
			new Thread(new Runnable() {
				@Override
				public void run() {
					@SuppressWarnings("unused")
					Extract extract = new Extract(PixelKnotActivity.this, cover_image_name);
				}
			}).start();

			loader = new PixelKnotLoader(PixelKnotActivity.this);
			loader.show();
			loader.init(Loader.Steps.EXTRACT);
		}

		public void setOutFile(File out_file) {
			this.out_file = out_file;
			try {
				put(Keys.OUT_FILE_NAME, out_file.getAbsolutePath());
			} catch(JSONException e) {}

		}

		public int checkForProtection(String sm) {
			if(sm.contains(PGP_SENTINEL) && sm.indexOf(PGP_SENTINEL) == 0)
				return Apg.DECRYPT_MESSAGE;
			else if(sm.contains(PASSWORD_SENTINEL) && sm.indexOf(PASSWORD_SENTINEL) == 0)
				return Cipher.DECRYPT_MODE;

			return Activity.RESULT_OK;
		}

		public void unlock(String password, String message_string) {
			final String total_message = message_string;

			message_string = message_string.substring(PASSWORD_SENTINEL.length());

			byte[] message = Base64.decode(message_string.split("\n")[1], Base64.DEFAULT);
			byte[] iv =  Base64.decode(message_string.split("\n")[0], Base64.DEFAULT);

			String sm = Aes.DecryptWithPassword(password, iv, message);
			if(sm != null) {
				pixel_knot.setSecretMessage(sm);
				hasSuccessfullyUnlocked = true;
			} else
				pixel_knot.setSecretMessage(new String(message));

			hasSuccessfullyExtracted = true;
			h.post(new Runnable() {
				@Override
				public void run() {
					try {
						loader.finish();
					} catch(NullPointerException e) {}

					((ActivityListener) pk_pager.getItem(view_pager.getCurrentItem())).updateUi();
					if(!hasSuccessfullyUnlocked) {
						Builder ad = new AlertDialog.Builder(PixelKnotActivity.this);
						ad.setMessage(PixelKnotActivity.this.getString(R.string.error_bad_password));
						ad.setPositiveButton(PixelKnotActivity.this.getString(R.string.retry), new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								loader = new PixelKnotLoader(PixelKnotActivity.this);
								loader.show();
								loader.init(Loader.Steps.DECRYPT);
								
								ByteArrayOutputStream baos = new ByteArrayOutputStream();
								try {
									baos.write(total_message.getBytes());
								} catch (IOException e) {
									Log.e(Logger.UI, e.toString());
									e.printStackTrace();
								}
								onExtractionResult(baos);

							}
						});
						ad.setNegativeButton(PixelKnotActivity.this.getString(R.string.ok), null);
						ad.show();
					}
				}
			});

		}
	}

	public class PKPager extends FragmentStatePagerAdapter {
		List<Fragment> fragments;
		FragmentManager fm;

		public PKPager(FragmentManager fm, List<Fragment> fragments) {
			super(fm);
			this.fm = fm;
			this.fragments = fragments;
		}

		@Override
		public Fragment getItem(int position) {
			return fragments.get(position);
		}

		@Override
		public int getCount() {
			return fragments.size();
		}
	}

	@Override
	public void setButtonOptions(ImageButton[] options) {
		// XXX: Samsung Galaxy S3 and Note are bullshit devices that for some reason tosses out declared views?
		try {
			Log.d(LOG, "The View in question is " + options_holder.getClass().getName() + "\nand you can see this because the device is functioning as it should...");
			options_holder.removeAllViews();
			
			for(ImageButton o : options) {
				try {
					((LinearLayout) o.getParent()).removeView(o);
				} catch(NullPointerException e) {}

				options_holder.addView(o);
			}

			rearrangeForKeyboard(false);
		} catch(NullPointerException e) {
			Log.d(LOG, "for some reason, options_holder is null?");	
		}
		
		
		
		
	}

	@Override
	public void updateButtonProminence(final int which, final int new_resource, final boolean enable) {
		h.postDelayed(new Runnable() {
			@Override
			public void run() {
				ImageButton ib = ((ImageButton) options_holder.getChildAt(which));
				ib.setImageResource(new_resource);
				ib.setEnabled(enable);
					
			}
		}, 1);

	}

	@Override
	public void onPageScrollStateChanged(int state) {
		if(state == 0) {
			h.post(new Runnable() {
				@Override
				public void run() {
					for(int v=0; v<progress_holder.getChildCount(); v++) {
						View view = progress_holder.getChildAt(v);
						if(view instanceof ImageView)
							((ImageView) view).setBackgroundResource(view_pager.getCurrentItem() == v ? d_ : d);
					}

					
					Fragment f = pk_pager.getItem(view_pager.getCurrentItem());
					
					if(view_pager.getCurrentItem() != 1)
						hideKeyboard();
					
					((ActivityListener) f).initButtons();
					((ActivityListener) f).updateUi();
					
				}
			});
		}
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {}

	@Override
	public void onPageSelected(int page) {
		Log.d(LOG, "the current pager is " + view_pager.getCurrentItem());
	}


	@Override
	public PixelKnot getPixelKnot() {
		return pixel_knot;
	}


	@Override
	public void clearPixelKnot() {
		pixel_knot.clear();
		restart();
	}


	@Override
	public void onMediaScanned(String path, Uri uri) {
		Log.d(LOG, "media scan finished");
		if(!hasSuccessfullyEmbed) {
			pixel_knot.setCoverImageName(path);
			((CoverImageFragment) pk_pager.fragments.get(0)).setImageData(path, uri);
		}
	}


	@Override
	public void onExtractionResult(final ByteArrayOutputStream baos) {
		h.post(new Runnable() {
			@Override
			public void run() {
				final String sm = new String(baos.toByteArray());

				switch(pixel_knot.checkForProtection(sm)) {
				case(Apg.DECRYPT_MESSAGE):
					pixel_knot.setEncryption(true, Apg.getInstance());
				pixel_knot.apg.decrypt(PixelKnotActivity.this, sm);
				break;
				case(Cipher.DECRYPT_MODE):
					final EditText password_holder = new EditText(PixelKnotActivity.this);
				password_holder.setHint(getString(R.string.password));

				Builder builder = new AlertDialog.Builder(PixelKnotActivity.this);
				builder.setView(password_holder);
				builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if(password_holder.getText().length() > 0) {
							new Thread(new Runnable() {
								@Override
								public void run() {
									pixel_knot.unlock(password_holder.getText().toString(), sm);
								}
							}).start();								
						}
					}
				});
				builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						pixel_knot.setSecretMessage(sm);
					}
				});

				builder.show();
				break;
				case(Activity.RESULT_OK):
					pixel_knot.setSecretMessage(sm);
				try {
					loader.finish();
				} catch(NullPointerException e) {}

				hasSuccessfullyExtracted = true;
				((ActivityListener) pk_pager.getItem(view_pager.getCurrentItem())).updateUi();
				break;
				}


			}
		});
	}


	@Override
	public void onEmbedded(final File out_file) {
		h.post(new Runnable() {
			@Override
			public void run() {
				hasSuccessfullyEmbed = true;
				
				if(Image.cleanUp(PixelKnotActivity.this, new String[] {pixel_knot.cover_image_name, new File(DUMP, "temp_img.jpg").getAbsolutePath()})) {
					out_file.renameTo(new File(pixel_knot.cover_image_name));
					pixel_knot.setOutFile(new File(pixel_knot.cover_image_name));
				} else
					pixel_knot.setOutFile(out_file);
				
				try {
					new PixelKnotMediaScanner(PixelKnotActivity.this, pixel_knot.getString(Keys.OUT_FILE_NAME));
				} catch (JSONException e) {
					Log.e(Logger.UI, e.toString());
					e.printStackTrace();
				}
				((ImageButton) options_holder.getChildAt(0)).setEnabled(true);

				try {
					loader.finish();
				} catch(NullPointerException e) {
					Log.e(Logger.UI, e.toString());
					e.printStackTrace();
				}

				((ActivityListener) pk_pager.getItem(view_pager.getCurrentItem())).updateUi();
			}
		});

	}

	@Override
	public void onGlobalLayout() {
		Rect r = new Rect();
		activity_root.getWindowVisibleDisplayFrame(r);

		int height_diff = activity_root.getRootView().getHeight() - (r.bottom - r.top);
		if(height_diff != last_diff) {
			if (height_diff > 100)
				rearrangeForKeyboard(true);
			else
				rearrangeForKeyboard(false);
		}

		last_diff = height_diff;
	}

	@Override
	public void setEncryption(Apg apg) {
		pixel_knot.setEncryption(true, apg);
	}

	@Override
	public List<TrustedShareActivity> getTrustedShareActivities() {
		if(trusted_share_activities == null) {
			trusted_share_activities = new Vector<TrustedShareActivity>();

			Intent intent = new Intent(Intent.ACTION_SEND)
			.setType("image/*");

			PackageManager pm = getPackageManager();
			for(ResolveInfo ri : pm.queryIntentActivities(intent, 0)) {
				if(Arrays.asList(Image.TRUSTED_SHARE_ACTIVITIES).contains(Image.Activities.get(ri.activityInfo.packageName))) {
					try {
						ApplicationInfo ai = pm.getApplicationInfo(ri.activityInfo.packageName, 0);					
						TrustedShareActivity tsa = new TrustedShareActivity(Image.Activities.get(ri.activityInfo.packageName), ri.activityInfo.packageName);

						tsa.setIcon(pm.getApplicationIcon(ai));
						tsa.createView();
						tsa.setIntent();

						trusted_share_activities.add(tsa);

					} catch(PackageManager.NameNotFoundException e) {
						Log.e(Logger.UI, e.toString());
						e.printStackTrace();
						continue;
					}
				}
			}
		}

		return trusted_share_activities;
	}

	@Override
	public boolean getHasSeenFirstPage() {
		return hasSeenFirstPage;
	}

	@Override
	public void setHasSeenFirstPage(boolean hasSeenFirstPage) {
		this.hasSeenFirstPage = hasSeenFirstPage;
	}

	@Override
	public boolean getHasSuccessfullyEmbed() {
		return hasSuccessfullyEmbed;
	}

	@Override
	public void setHasSuccessfullyEmbed(boolean hasSuccessfullyEmbed) {
		this.hasSuccessfullyEmbed = hasSuccessfullyEmbed;

	}

	@Override
	public boolean getHasSuccessfullyExtracted() {
		return hasSuccessfullyExtracted;
	}

	@Override
	public void setHasSuccessfullyExtracted(boolean hasSuccessfullyExtracted) {
		this.hasSuccessfullyExtracted = hasSuccessfullyExtracted;
	}

	@Override
	public void setHasSuccessfullyEncrypted(boolean hasSuccessfullyEncrypted) {
		this.hasSuccessfullyEncrypted = hasSuccessfullyEncrypted; 

	}

	@Override
	public boolean getHasSuccessfullyEncrypted() {
		return hasSuccessfullyEncrypted;
	}

	@Override
	public boolean getHasSuccessfullyDecrypted() {
		return hasSuccessfullyDecrypted;
	}

	@Override
	public void setHasSuccessfullyDecrypted(boolean hasSuccessfullyDecrypted) {
		this.hasSuccessfullyDecrypted = hasSuccessfullyDecrypted;
	}

	@Override
	public boolean getHasSuccessfullyPasswordProtected() {
		return hasSuccessfullyPasswordProtected;
	}

	@Override
	public void setHasSuccessfullyPasswordProtected(boolean hasSuccessfullyPasswordProtected) {
		this.hasSuccessfullyPasswordProtected = hasSuccessfullyPasswordProtected;
	}

	@Override
	public boolean getHasSuccessfullyUnlocked() {
		return hasSuccessfullyUnlocked;
	}

	@Override
	public void setHasSuccessfullyUnlocked(boolean hasSuccessfullyUnlocked) {
		this.hasSuccessfullyUnlocked = hasSuccessfullyUnlocked;
	}
	

	@Override
	public void setIsDecryptOnly(boolean isDecryptOnly) {
		this.isDecryptOnly = isDecryptOnly;
	}

	@Override
	public boolean getIsDecryptOnly() {
		return isDecryptOnly;
	}

	@Override
	public void autoAdvance() {
		Log.d(LOG, "AUTO ADVANCING?");
		if(this.getCanAutoAdvance()) {
			autoAdvance(view_pager.getCurrentItem() == pk_pager.getCount() - 1 ? 0 : view_pager.getCurrentItem() + 1);
			setCanAutoAdvance(false);
		}
	}
	
	@Override
	public boolean getCanAutoAdvance() {
		return this.canAutoAdvance;
	}

	@Override
	public void setCanAutoAdvance(boolean canAutoAdvance) {
		this.canAutoAdvance = canAutoAdvance;
	}

	@Override
	public void autoAdvance(int position) {
		view_pager.setCurrentItem(position, true);		
	}
	
	private void setNewLocale(String locale_code) {
		Configuration configuration = new Configuration();
		switch(Integer.parseInt(locale_code)) {
		case Settings.Locales.DEFAULT:
			configuration.locale = new Locale(Locale.getDefault().getLanguage());
			break;
		case Settings.Locales.EN:
			configuration.locale = new Locale("en");
			break;
		case Settings.Locales.FA:
			configuration.locale = new Locale("fa");
			break;
		}
		getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());
		restart();
	}
	
	@Override
	public void onUpdate(int steps, int interval) {
		// XXX: I don't use this.
	}

	@Override
	public void onUpdate() {
		steps_taken++;
		loader.post();
		Log.d(Logger.UI, "steps taken: " + steps_taken);
	}

	@Override
	public void onFailure() {
		try {
			loader.cancel();
		} catch(NullPointerException e) {}
		finish();
		
		// TODO: maybe there is a message, too.
	}

	@Override
	public void doWait(boolean status) {
		if(status) {
			please_wait = new ProgressDialog(this);
			please_wait.setCancelable(false);
			please_wait.setMessage(getResources().getString(R.string.please_wait));
			
			please_wait.show();
		} else {
			try {
				please_wait.dismiss();
			} catch(NullPointerException e) {}
		}
		
	}
}