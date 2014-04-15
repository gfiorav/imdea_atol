package com.imdea.networks.apol;

import java.io.File;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.os.Build;

public class Benchmark extends Activity {

	static TextView tv [];
	static ProgressBar pb [];
	static ImageButton bb;
	static TextView r [];
	
	static boolean onGoing[];
	
	static int benchmark_start [];
	static int benchmark_stop [];
	
	static int file_size[];
	
	DownloadTask dt1;
	DownloadTask dt2;
	DownloadTask dt3;
	
	public void intialize() {
		tv = new TextView[3];
		tv[0] = (TextView) findViewById(R.id.progress_text_1);
		tv[1] = (TextView) findViewById(R.id.progress_text_2);
		tv[2] = (TextView) findViewById(R.id.progress_text_3);
		
		pb = new ProgressBar[3];
		pb[0] = (ProgressBar) findViewById(R.id.progress_bar_1);
		pb[1] = (ProgressBar) findViewById(R.id.progress_bar_2);
		pb[2] = (ProgressBar) findViewById(R.id.progress_bar_3);
		
		r = new TextView[3];
		r[0] = (TextView) findViewById(R.id.benchmark_result_1);
		r[1] = (TextView) findViewById(R.id.benchmark_result_2);
		r[2] = (TextView) findViewById(R.id.benchmark_result_3);
		
		file_size = new int [3];
		benchmark_start = new int [3]; 
		benchmark_stop = new int [3];
		onGoing = new boolean [3];
		
		for(int i = 0; i < 3; i++) {
			file_size[i] = 0;
			benchmark_start[i] = 0;
			benchmark_stop[i] = 0;
			onGoing[i] = false;
		}
		
		File directory = new File(Environment.getExternalStorageDirectory() + File.separator + ApplicationLogger.FOLDER_NAME + File.separator + ".bnchmrks");

		if (!directory.exists()) {
			directory.mkdir();
		}

		
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_benchmark);
		// Show the Up button in the action bar.
		setupActionBar();
		
		intialize();

	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.benchmark, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public void startBenchmark(View view) {
		bb = (ImageButton) view;
		
		if(onGoing[0] || onGoing[1] || onGoing[2]) {
			bb.setImageResource(R.drawable.startam);
			
//			dt1.cancel(true);
//			dt2.cancel(true);
//			dt3.cancel(true);
//			
			for(TextView t : tv){
				t.setText(R.string.default_benchmark);
			}
			
			for(int i = 0; i < 3; i++) {
				onGoing[i] = false;
			}
		} else {
			bb.setImageResource(R.drawable.stopam);
			
			this.dt1 = new DownloadTask(Benchmark.this);
			this.dt2 = new DownloadTask(Benchmark.this);
			this.dt3 = new DownloadTask(Benchmark.this);
			
			this.dt1.execute("87.98.231.87", 0 +"");
//			this.dt2.execute("http://who.guido.is/IMDEA/10MB.zip", 1 +"");
//			this.dt3.execute("http://people.networks.imdea.org/~foivos_michelinakis/staticfiles/10MB.zip", 2 +"");
//
//			Log.wtf("DOWNLOAD", "3 Threads executed!");
//			
//			int ts = (int) (System.currentTimeMillis() / 1000L); 
//			for(Integer b : benchmark_start) {
//				b = ts;
//			}
//			
			for(int i = 0; i < 3; i++) {
				onGoing[i] = true;
			}
		}

	}

}
