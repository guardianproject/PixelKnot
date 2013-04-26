package info.guardianproject.pixelknot.screens;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import info.guardianproject.pixelknot.Constants.Screens.Loader;
import info.guardianproject.pixelknot.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class PixelKnotLoader extends AlertDialog {
	Context context;
	ProgressBar knot_progress;
	ImageView knot_image;
	TextView knot_title, knot_warning;
	
	List<Integer> display_order;
	Iterator<Integer> load_engine;
	
	Handler h = new Handler();
	Runnable r = new Runnable() {
		@Override
		public void run() {
			step++;
			loadAKnot();
		}
	};
	
	int num_steps = 0;
	int step = -1;
	
	public PixelKnotLoader(Activity c) {
		super(c);
		this.context = c;
		
		View root = LayoutInflater.from(c).inflate(R.layout.pixel_knot_loader, null);
		knot_progress = (ProgressBar) root.findViewById(R.id.knot_progress);
		knot_image = (ImageView) root.findViewById(R.id.knot_image);
		knot_title = (TextView) root.findViewById(R.id.knot_title);
		
		this.setView(root);
		this.setCancelable(false);
		randomizeOrder();
		
		load_engine = display_order.iterator();
		this.show();
	}
	
	public void init(int num_steps) {
		this.num_steps = num_steps;
		knot_progress.setMax(num_steps);
		post();
	}
	
	public void update(int additional_steps) {
		this.num_steps += additional_steps;
		knot_progress.setMax(num_steps);
	}
	
	public void post() {
		h.post(r);
	}
	
	public void finish() {
		h.post(new Runnable() {
			@Override
			public void run() {
				if(load_engine.hasNext()) {
					post();
					h.postDelayed(this, 500);
				} else
					PixelKnotLoader.this.cancel();
			}
		});
		
		
	}
	
	private void loadAKnot() {
		if(!load_engine.hasNext())
			load_engine = display_order.iterator();
		
		int k = load_engine.next();
		
		knot_image.setImageDrawable(context.getResources().getDrawable(Loader.KNOT_IMAGES[k]));
		knot_title.setText(context.getResources().getStringArray(R.array.knot_names)[k]);
		knot_progress.setProgress(step);
	}
	
	private void randomizeOrder() {
		Integer[] display_order = new Integer[Loader.KNOT_IMAGES.length];
		for(int d=0;d<display_order.length; d++)
			display_order[d] = d;
		
		this.display_order = new ArrayList<Integer>(Arrays.asList(display_order));
		
		Collections.shuffle(this.display_order);
	}
}
