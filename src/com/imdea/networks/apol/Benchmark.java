package com.imdea.networks.apol;

import java.io.File;
import java.text.DecimalFormat;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.app.Activity;
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

	static long benchmark_start [];
	static long benchmark_stop [];

	static int file_sizes[];
	static double totals[];

	private DownloadTask dts [];

	public static Handler UiUpdater;
	public static Runnable UiUpdaterRunnable;

	private DecimalFormat df = new DecimalFormat("##.## MBps");

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

		file_sizes = new int [3];
		totals = new double [3];
		benchmark_start = new long [3]; 
		benchmark_stop = new long [3];
		onGoing = new boolean [3];

		for(int i = 0; i < 3; i++) {
			file_sizes[i] = 1;
			totals[i] = 0;
			benchmark_start[i] = 0;
			benchmark_stop[i] = 0;
			onGoing[i] = false;
		}

		this.dts = new DownloadTask [3];
		this.dts[0] = new DownloadTask("http://www.tassar.es/IMDEA/10MB.zip", 0);
		this.dts[1] = new DownloadTask("http://who.guido.is/IMDEA/10MB.zip", 1);
		this.dts[2] = new DownloadTask("http://people.networks.imdea.org/~foivos_michelinakis/staticfiles/10MB.zip", 2);

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

			for(TextView t : tv){
				t.setText(R.string.default_benchmark);
			}

			for(int i = 0; i < 3; i++) {
				onGoing[i] = false;
			}
		} else {
			bb.setImageResource(R.drawable.stopam);

			new Thread(this.dts[0]).start();
			new Thread(this.dts[1]).start();
			new Thread(this.dts[2]).start();

			for(int i = 0; i < 3; i++) {
				onGoing[i] = true;
			}
		}

	}

	void updateProgressBar(int id, int progress) {
		pb[id].setProgress(progress);
	}

	void updatePercentages(int id, int percentage) {
		tv[id].setText(percentage + "%");
	}

	void updateResults(int id) {
		long elapsed_time = (System.currentTimeMillis()) - benchmark_start[id];

		if(elapsed_time /1000 != 0) {
			float rate = (float) ((totals[id] /1048576) / (elapsed_time /1000));
			r[id].setText(this.df.format(rate));
		}
	}

	private void updateUi() {
		for(int i = 0; i < 3; i++) {
			if(benchmark_stop [i] == 0) {
				int progress = (int) (totals[i] * 100) / file_sizes[i];

				updateProgressBar(i, progress);
				updatePercentages(i, progress);
				updateResults(i);
			} else {
				updateProgressBar(i, 100);
				updatePercentages(i, 100);
			}
		}

		if(benchmark_stop [0] != 0 && benchmark_stop [1] != 0 && benchmark_stop [2] != 0) {
			long t0, t1, t2;
			float res0, res1, res2;

			t0 = (benchmark_stop[0] -benchmark_start[0]) /1000;
			t1 = (benchmark_stop[1] -benchmark_start[1]) /1000;
			t2 = (benchmark_stop[2] -benchmark_start[2]) /1000;

			res0 = (float) (totals[0] /1048576) /t0;
			res1 = (float) (totals[1] /1048576) /t1;
			res2 = (float) (totals[2] /1048576) /t2;

			float total = res0 + res1 + res2;

			TextView ft = (TextView) this.findViewById(R.id.final_result);

			ft.setText(df.format(total));
		}
	}



}
