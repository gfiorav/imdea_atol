package com.imdea.networks.apol;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.os.Build;

public class Benchmark extends Activity {

	public static int BENCHMARK_DURATION = 20;
	public static int SLOW_START_DURATION = 5;
	public static int STEADY_STATE_TIME = BENCHMARK_DURATION - SLOW_START_DURATION;
	
	public static double cumulative [];
	public static double average[];
	public static int cumulative_ptr;
	public static int slow_start_ptr;

	static Timer cumulativeTimer;

	static TextView tv [];
	static ImageButton bb;
	static TextView r [];
	static ImageView si [];
	static int sc[];
	static double fr;

	private float sr[];

	static boolean onGoing[];
	static boolean ready[];
	static boolean benchmarkAchieved;

	static long benchmark_start [];
	static long benchmark_stop [];

	static int file_sizes[];
	static double totals[];

	private DownloadTask dts [];

	public static Handler UiUpdater;
	public static Runnable UiUpdaterRunnable;

	private static DecimalFormat df = new DecimalFormat("##.## Mbps");

	public static int MAX_SPEED = 2;

	public void initialize() {
		cumulative = new double [STEADY_STATE_TIME];
		average = new double [STEADY_STATE_TIME];
		cumulative_ptr = 0;
		slow_start_ptr = 0;

		fr = 0;

		si = new ImageView[3];
		si[0] = (ImageView) findViewById(R.id.server_img_1);
		si[1] = (ImageView) findViewById(R.id.server_img_2);
		si[2] = (ImageView) findViewById(R.id.server_img_3);

		sc = new int[3];
		sc[0] = -1;
		sc[1] = -1;
		sc[2] = -1;

		r = new TextView[3];
		r[0] = (TextView) findViewById(R.id.benchmark_result_1);
		r[1] = (TextView) findViewById(R.id.benchmark_result_2);
		r[2] = (TextView) findViewById(R.id.benchmark_result_3);

		sr = new float[3];
		sr[0] = 0;
		sr[1] = 0;
		sr[2] = 0;


		file_sizes = new int [3];
		totals = new double [3];
		benchmark_start = new long [3]; 
		benchmark_stop = new long [3];
		onGoing = new boolean [3];
		ready = new boolean[3];
		benchmarkAchieved = false;

		for(int i = 0; i < 3; i++) {
			file_sizes[i] = 1;
			totals[i] = 0;
			benchmark_start[i] = 0;
			benchmark_stop[i] = 0;
			onGoing[i] = false;
			ready[i] = false;
		}

		this.dts = new DownloadTask [3];
		this.dts[0] = new DownloadTask("http://www.tassar.es/IMDEA/20MB.imdeab", 0);
		this.dts[1] = new DownloadTask("http://who.guido.is/IMDEA/20MB.imdeab", 1);
		this.dts[2] = new DownloadTask("http://people.networks.imdea.org/~foivos_michelinakis/staticfiles/50MB.zip", 2);

		File directory = new File(Environment.getExternalStorageDirectory() + File.separator + ApplicationLogger.FOLDER_NAME + File.separator + ".bnchmrk");

		if (!directory.exists()) {
			directory.mkdir();
		}

		Benchmark.UiUpdater = new Handler();
		Benchmark.UiUpdaterRunnable = new Runnable() {
			public void run() {
				updateUi();
				return;
			}
		};

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_benchmark);
		// Show the Up button in the action bar.
		setupActionBar();

		initialize();

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

	public void resetBenchmark() {
		initialize();
	}

	public void startBenchmark(View view) {		
		bb = (ImageButton) view;

		if(!onGoing[0] && !onGoing[1] && !onGoing[2]) {
			bb.setImageResource(R.drawable.stopam);

			new Thread(this.dts[0]).start();
			new Thread(this.dts[1]).start();
			new Thread(this.dts[2]).start();

			for(int i = 0; i < 3; i++) {
				onGoing[i] = true;
			}
		}

	}

	void updateResults(int id) {
		long elapsed_time = (System.currentTimeMillis()) - benchmark_start[id];

		if(elapsed_time /1000 != 0) {
			float rate = (float) ((totals[id] /1048576) / (elapsed_time /1000));
			r[id].setText(df.format(rate));
			sr[id] = rate;
		}
	}

	void updateTotal() {
		TextView r = (TextView) findViewById(R.id.final_result);
		float result = sr[0] + sr[1] +sr[2];

		r.setText(df.format(result));
	}

	void updateServerImages(int id) {
		if(sc[id] == 200)
			si[id].setImageResource(R.drawable.serveronline);
		else
			si[id].setImageResource(R.drawable.serveroffline);
	}

	private void updateUi() {

		for(int i = 0; i < 3; i++) {
			if(onGoing[i])
			{
				updateResults(i);
				updateServerImages(i);
			}
		}

		if(benchmarkAchieved) {
			TextView frt = (TextView) findViewById(R.id.final_result);
			frt.setText(df.format(fr));

			TextView sl = (TextView) findViewById(R.id.remaining);
			sl.setText("Benchmark completed");

			//resetBenchmark();
		} else {
			TextView sl = (TextView) findViewById(R.id.remaining);
			int slow_start_rem = SLOW_START_DURATION - slow_start_ptr;
			int seconds_remaining = (STEADY_STATE_TIME - cumulative_ptr) + slow_start_rem;
			if(slow_start_rem != 0) {
				sl.setText("Slow start ends in ... " + slow_start_rem +"s");
			} else {
				sl.setText(seconds_remaining +" seconds remaining");
			}
		}

	}

	public static void startCumulativeBenchmark() {

		cumulativeTimer = new Timer();
		cumulativeTimer.scheduleAtFixedRate(new TimerTask(){

			@Override
			public void run() {
				Benchmark.UiUpdater.post(Benchmark.UiUpdaterRunnable);
				if(slow_start_ptr < SLOW_START_DURATION) {
					slow_start_ptr++;
				} else {
					if(cumulative_ptr < STEADY_STATE_TIME) {
						if(cumulative_ptr == 0) {
							double total_cumulative = Benchmark.totals[0] + Benchmark.totals[1] + Benchmark.totals[2];
							cumulative[cumulative_ptr] = total_cumulative;
							average[cumulative_ptr] = total_cumulative / 3;
						} else {
							double total_cumulative = (Benchmark.totals[0] + Benchmark.totals[1] + Benchmark.totals[2]) - cumulative[cumulative_ptr - 1];
							cumulative[cumulative_ptr] = total_cumulative;
							average[cumulative_ptr] = total_cumulative / 3;
						}
						cumulative_ptr++;
					} else {
						benchmarkAchieved = true;

						Arrays.sort(average);
						
//						for(int i = 0; i < 15; i++) {
//							Log.wtf("MEDIAN", i + " " + average[i] / 1048576);
//						}
						
						float median = (float) average[(STEADY_STATE_TIME) / 2] / 1048576;
						
						fr = median;
						
						this.cancel();
					}
				}
			}

		},0, 1000);
	}



}
