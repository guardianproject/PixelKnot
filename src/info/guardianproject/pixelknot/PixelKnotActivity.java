package info.guardianproject.pixelknot;

import info.guardianproject.f5android.Embed;
import info.guardianproject.f5android.Embed.EmbedListener;
import info.guardianproject.f5android.Extract;
import info.guardianproject.f5android.Extract.ExtractionListener;
import info.guardianproject.pixelknot.Constants.Logger;
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
import info.guardianproject.pixelknot.utils.PixelKnotMediaScanner.MediaScannerListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class PixelKnotActivity extends FragmentActivity implements Constants, FragmentListener, ViewPager.OnPageChangeListener, View.OnClickListener, MediaScannerListener, EmbedListener, ExtractionListener {
	private PKPager pk_pager;
	private ViewPager view_pager;
	Button start_over;

	File dump;
	int d, d_;

	private LinearLayout options_holder;
	private LinearLayout progress_holder;

	private static final String LOG = Logger.UI;

	private PixelKnot pixel_knot = new PixelKnot();
	
	private ProgressDialog progress_dialog;
	Handler h = new Handler();
	
	boolean hasSeenFirstPage = false;
	boolean hasSuccessfullyEmbed = false;
	boolean hasSuccessfullyExtracted = false;
	boolean hasSuccessfullyEncrypted = false;
	boolean hasSuccessfullyDecrypted = false;
	boolean hasSuccessfullyPasswordProtected = false;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		dump = new File(DUMP);
		if(!dump.exists())
			dump.mkdir();

		d = R.drawable.pixelknot_bullet_inactive;
		d_ = R.drawable.pixelknot_bullet_active;

		setContentView(R.layout.pixel_knot_activity);
		
		start_over = (Button) findViewById(R.id.start_over);
		start_over.setText(getString(R.string.start_over));
		start_over.setOnClickListener(this);

		options_holder = (LinearLayout) findViewById(R.id.options_holder);
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

	public class PixelKnot extends JSONObject {
		String cover_image_name = null;
		String secret_message = null;
		
		String password = null;
		byte[] iv = null;

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
		
		public boolean getEncryption() {
			return has_encryption;
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
					if(has(Keys.PASSWORD)) {
						remove(Keys.PASSWORD);
						iv = null;
					}
				} catch(JSONException e) {}
			} else {
				if(has(Keys.HAS_ENCRYPTION))
					remove(Keys.HAS_ENCRYPTION);
			}
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
			iv = null;
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
							Entry<byte[], String> pack = Aes.EncryptWithPassword(pixel_knot.getString(Keys.PASSWORD), secret_message).entrySet().iterator().next();
							iv = pack.getKey();
							secret_message = PASSWORD_SINTENEL.concat(pack.getValue());
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
				return;
			}
			
			if(pixel_knot.getEncryption() && !hasSuccessfullyEncrypted) {
				apg.encrypt(PixelKnotActivity.this, pixel_knot.secret_message);
				return;
			}
			
			if((!pixel_knot.has_encryption && !pixel_knot.getPassword()) || (hasSuccessfullyEncrypted || hasSuccessfullyPasswordProtected)) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						Embed embed = new Embed(PixelKnotActivity.this, dump.getName(), downsampleImage(), secret_message);
					}
				}).start();
			}
			
		}
		
		private String downsampleImage() {
			Bitmap b = BitmapFactory.decodeFile(cover_image_name);
			int scale = 3;
						
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inSampleSize = scale;
			b.recycle();

			Bitmap b_ = BitmapFactory.decodeFile(cover_image_name, opts);
			try {
				File downsampled_image = new File(dump, System.currentTimeMillis() + "_PixelKnot.jpg");
				FileOutputStream fos = new FileOutputStream(downsampled_image);
				b_.compress(CompressFormat.JPEG, 80, fos);
				fos.flush();
				fos.close();
				
				b_.recycle();
				return downsampled_image.getAbsolutePath();
			} catch (FileNotFoundException e) {
				Log.e(Logger.UI, e.toString());
				e.printStackTrace();
			} catch (IOException e) {
				Log.e(Logger.UI, e.toString());
				e.printStackTrace();
			}
			
			return null;
		}
		
		public void decrypt() {
			new Thread(new Runnable() {
				@Override
				public void run() {
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
				try {
					progress_dialog.dismiss();
				} catch(NullPointerException e) {}
				
				pixel_knot.setSecretMessage(new String(baos.toByteArray()));
				hasSuccessfullyExtracted = true;
				
				((ActivityListener) pk_pager.getItem(view_pager.getCurrentItem())).updateUi();
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
				
				((ActivityListener) pk_pager.getItem(view_pager.getCurrentItem())).updateUi();
				share();
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
			}
		}
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