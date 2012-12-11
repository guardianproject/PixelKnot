package info.guardianproject.pixelknot;

import info.guardianproject.f5android.Embed;
import info.guardianproject.f5android.Embed.EmbedListener;
import info.guardianproject.f5android.Extract;
import info.guardianproject.f5android.Extract.ExtractionListener;
import info.guardianproject.pixelknot.Constants.PixelKnot.Keys;
import info.guardianproject.pixelknot.crypto.Aes;
import info.guardianproject.pixelknot.crypto.Apg;
import info.guardianproject.pixelknot.screens.CoverImageFragment;
import info.guardianproject.pixelknot.screens.DecryptImageFragment;
import info.guardianproject.pixelknot.screens.SetMessageFragment;
import info.guardianproject.pixelknot.screens.ShareFragment;
import info.guardianproject.pixelknot.screens.StegoImageFragment;
import info.guardianproject.pixelknot.utils.ActivityListener;
import info.guardianproject.pixelknot.utils.FragmentListener;
import info.guardianproject.pixelknot.utils.IO;
import info.guardianproject.pixelknot.utils.Image;
import info.guardianproject.pixelknot.utils.PixelKnotMediaScanner.MediaScannerListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;

import javax.crypto.Cipher;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PixelKnotActivity extends FragmentActivity implements Constants, FragmentListener, ViewPager.OnPageChangeListener, View.OnClickListener, OnGlobalLayoutListener, MediaScannerListener, EmbedListener, ExtractionListener {
	private PKPager pk_pager;
	private ViewPager view_pager;
	
	File dump;
	int d, d_, last_diff;

	private View activity_root;
	private LinearLayout options_holder, action_display, action_display_;
	private LinearLayout progress_holder;
	Button start_over;

	private static final String LOG = Logger.UI;

	private PixelKnot pixel_knot = new PixelKnot();

	private ProgressDialog progress_dialog;
	Handler h = new Handler();
	InputMethodManager imm;

	boolean hasSeenFirstPage = false;
	boolean hasSuccessfullyEmbed = false;
	boolean hasSuccessfullyExtracted = false;
	boolean hasSuccessfullyEncrypted = false;
	boolean hasSuccessfullyDecrypted = false;
	boolean hasSuccessfullyPasswordProtected = false;

	private List<TrustedShareActivity> trusted_share_activities;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		dump = new File(DUMP);
		if(!dump.exists())
			dump.mkdir();

		d = R.drawable.pixelknot_bullet_inactive;
		d_ = R.drawable.pixelknot_bullet_active;

		setContentView(R.layout.pixel_knot_activity);

		imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

		options_holder = (LinearLayout) findViewById(R.id.options_holder);

		action_display = (LinearLayout) findViewById(R.id.action_display);
		action_display_ = (LinearLayout) findViewById(R.id.action_display_);

		progress_holder = (LinearLayout) findViewById(R.id.progress_holder);

		List<Fragment> fragments = new Vector<Fragment>();

		if(getIntent().getData() == null) {
			Fragment cover_image_fragment = Fragment.instantiate(this, CoverImageFragment.class.getName());
			Fragment set_message_fragment = Fragment.instantiate(this, SetMessageFragment.class.getName());
			Fragment share_fragment = Fragment.instantiate(this, ShareFragment.class.getName());

			fragments.add(0, cover_image_fragment);
			fragments.add(1, set_message_fragment);
			fragments.add(2, share_fragment);
		} else {
			Fragment stego_image_fragment = Fragment.instantiate(this, StegoImageFragment.class.getName());
			Bundle args = new Bundle();
			args.putString(Keys.COVER_IMAGE_NAME, IO.pullPathFromUri(this, getIntent().getData()));
			stego_image_fragment.setArguments(args);

			Fragment decrypt_image_fragment = Fragment.instantiate(this, DecryptImageFragment.class.getName());

			fragments.add(0, stego_image_fragment);
			fragments.add(1, decrypt_image_fragment);
		}

		pk_pager = new PKPager(getSupportFragmentManager(), fragments);
		view_pager = (ViewPager) findViewById(R.id.fragment_holder);
		view_pager.setAdapter(pk_pager);
		view_pager.setOnPageChangeListener(this);

		int windowWidth = ((WindowManager) this.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
		LayoutParams lp = new LinearLayout.LayoutParams(windowWidth / fragments.size(), LayoutParams.WRAP_CONTENT);

		for(int p=0; p<fragments.size(); p++) {
			ImageView progress_view = new ImageView(this);
			progress_view.setLayoutParams(lp);
			progress_view.setBackgroundResource(p == 0 ? d_ : d);
			progress_holder.addView(progress_view);
		}

		last_diff = 0;
		activity_root = findViewById(R.id.activity_root);
		activity_root.getViewTreeObserver().addOnGlobalLayoutListener(this);
	}

	private void restart() {
		h.post(new Runnable() {
			@Override
			public void run() {
				getIntent().setData(null);

				Intent intent = getIntent();
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
				overridePendingTransition(0, 0);
				finish();

				overridePendingTransition(0, 0);
				startActivity(intent);
			}
		});
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
		} catch(NullPointerException e) {
			Log.e(Logger.UI, "this view hasn't been set there yet");
		}

		if(state) {
			action_display.setVisibility(View.GONE);

			action_display_.setVisibility(View.VISIBLE);
			action_display_.addView(options_holder, action_display_.getChildCount());
		} else {
			action_display_.setVisibility(View.GONE);

			action_display.setVisibility(View.VISIBLE);
			action_display.addView(options_holder, action_display.getChildCount());
		}
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

		public void setIntent() {
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

			// TODO: get and set new capacity!
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

			if(pixel_knot.getPassword() || !hasSuccessfullyPasswordProtected) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							Entry<String, String> pack = Aes.EncryptWithPassword(pixel_knot.getString(Keys.PASSWORD), secret_message).entrySet().iterator().next();

							secret_message = PASSWORD_SENTINEL.concat(new String(pack.getKey())).concat(pack.getValue());
							hasSuccessfullyPasswordProtected = true;
							h.post(new Runnable() {
								@Override
								public void run() {
									try {
										progress_dialog.dismiss();
									} catch(NullPointerException e) {}
									save();
								}
							});
						} catch (JSONException e) {}
					}
				}).start();
				progress_dialog = ProgressDialog.show(PixelKnotActivity.this, "", PixelKnotActivity.this.getString(R.string.please_wait));
			}

			if(pixel_knot.getEncryption() && !hasSuccessfullyEncrypted) {
				apg.encrypt(PixelKnotActivity.this, pixel_knot.secret_message);
			}

			if((!pixel_knot.has_encryption && !pixel_knot.getPassword()) || (hasSuccessfullyEncrypted || hasSuccessfullyPasswordProtected)) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						@SuppressWarnings("unused")
						Embed embed = new Embed(PixelKnotActivity.this, dump.getName(), Image.downsampleImage(pixel_knot.cover_image_name, dump), secret_message);
					}
				}).start();
			}

		}

		public void extract() {
			new Thread(new Runnable() {
				@Override
				public void run() {
					@SuppressWarnings("unused")
					Extract extract = new Extract(PixelKnotActivity.this, cover_image_name);
				}
			}).start();
			progress_dialog = ProgressDialog.show(PixelKnotActivity.this, "", PixelKnotActivity.this.getString(R.string.please_wait));
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
			message_string = message_string.substring(PASSWORD_SENTINEL.length());
			Log.d(Logger.UI, message_string);

			byte[] message = Base64.decode(message_string.split("\n")[1], Base64.DEFAULT);
			byte[] iv =  Base64.decode(message_string.split("\n")[0], Base64.DEFAULT);

			String sm = Aes.DecryptWithPassword(password, iv, message);
			if(sm != null)
				pixel_knot.setSecretMessage(sm);
			else
				pixel_knot.setSecretMessage(new String(message));

			hasSuccessfullyExtracted = true;
			h.post(new Runnable() {
				@Override
				public void run() {
					try {
						progress_dialog.dismiss();
					} catch(NullPointerException e) {}

					((ActivityListener) pk_pager.getItem(view_pager.getCurrentItem())).updateUi();
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
	public void setButtonOptions(Button[] options) {
		options_holder.removeAllViews();
		for(Button o : options) {
			try {
				((LinearLayout) o.getParent()).removeView(o);
			} catch(NullPointerException e) {}

			options_holder.addView(o);
		}

		rearrangeForKeyboard(false);
	}

	@Override
	public void updateButtonProminence(final int which, final String new_text) {
		h.postDelayed(new Runnable() {
			@Override
			public void run() {
				((Button) options_holder.getChildAt(which)).setText(new_text);
			}
		}, 1);
		
	}
	
	@Override
	public void onPageScrollStateChanged(int arg0) {}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {}

	@Override
	public void onPageSelected(int page) {
		for(int v=0; v<progress_holder.getChildCount(); v++) {
			View view = progress_holder.getChildAt(v);
			if(view instanceof ImageView)
				((ImageView) view).setBackgroundResource(page == v ? d_ : d);
		}

		Fragment f = pk_pager.getItem(page);
		((ActivityListener) f).initButtons();
		((ActivityListener) f).updateUi();
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
		pixel_knot.setCoverImageName(path);
		((CoverImageFragment) pk_pager.fragments.get(0)).setImageData();
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
					progress_dialog.dismiss();
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
				try {
					progress_dialog.dismiss();
				} catch(NullPointerException e) {}

				hasSuccessfullyEmbed = true;
				pixel_knot.setOutFile(out_file);
				((Button) options_holder.getChildAt(0)).setEnabled(true);
				try {
					progress_dialog.dismiss();
				} catch(NullPointerException e) {}

				((ActivityListener) pk_pager.getItem(view_pager.getCurrentItem())).updateUi();
			}
		});

	}
	

	@Override
	public void onClick(View v) {
		if(v == start_over) {
			pixel_knot.clear();
			restart();
		}

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
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == Activity.RESULT_OK) {
			switch(requestCode) {
			case Apg.ENCRYPT_MESSAGE:
				pixel_knot.apg.onActivityResult(this, requestCode, resultCode, data);
				pixel_knot.setSecretMessage(pixel_knot.apg.getEncryptedData());
				setHasSuccessfullyEncrypted(true);
				pixel_knot.save();
				break;
			case Apg.DECRYPT_MESSAGE:
				pixel_knot.apg.onActivityResult(this, requestCode, resultCode, data);
				pixel_knot.setSecretMessage(pixel_knot.apg.getDecryptedData());
				setHasSuccessfullyExtracted(true);
				((ActivityListener) pk_pager.getItem(view_pager.getCurrentItem())).updateUi();
				try {
					progress_dialog.dismiss();
				} catch(NullPointerException e) {}
				break;
			}
		}
	}

	@Override
	public List<TrustedShareActivity> getTrustedShareActivities() {
		if(trusted_share_activities == null) {
			trusted_share_activities = new Vector<TrustedShareActivity>();

			Intent intent = new Intent(Intent.ACTION_SEND)
			.setType("image/jpeg");

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
	public boolean setHasSuccessfullyPasswordProtected() {
		return hasSuccessfullyPasswordProtected;
	}

	@Override
	public void setHasSuccessfullyPasswordProtected(boolean hasSuccessfullyPasswordProtected) {
		this.hasSuccessfullyPasswordProtected = hasSuccessfullyPasswordProtected;
	}

}