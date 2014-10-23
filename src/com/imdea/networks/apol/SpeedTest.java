package com.imdea.networks.apol;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class SpeedTest {
	public static int SLOW_START_DURATION = 5;
	public static int BENCHMARK_DURATION = 10;
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
	static double tm;

	TextView sl;

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

	private static DecimalFormat rf = new DecimalFormat("##.## Mbps");
	private static DecimalFormat sf = new DecimalFormat("##.#MB");

	public static int MAX_SPEED = 2;

	public void initialize() {
		cumulative = new double [STEADY_STATE_TIME];
		average = new double [STEADY_STATE_TIME];
		cumulative_ptr = 0;
		slow_start_ptr = 0;

		fr = 0;
		tm = 0;

		for(ImageView i : si) {
			i.setImageResource(R.drawable.serveroffline);
		}

		sc = new int[3];
		sc[0] = -1;
		sc[1] = -1;
		sc[2] = -1;

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

		this.dts[0] = new DownloadTask("http://testvelocidad1.orange.es/speedtest/random4000x4000.jpg", 0);
		this.dts[1] = new DownloadTask("http://speedtest.mad.adamo.es/speedtest/random4000x4000.jpg", 1);
		this.dts[2] = new DownloadTask("http://testmadmovistar.telefonica.com/speedtest/random4000x4000.jpg", 2);


		File directory = new File(Environment.getExternalStorageDirectory() + File.separator + Logger.FOLDER_NAME + File.separator + ".bnchmrk");

		if (!directory.exists()) {
			directory.mkdir();
		}

	}
	
	public void resetBenchmark() {
		initialize();
	}

	public void startBenchmark() {		

		if(!onGoing[0] && !onGoing[1] && !onGoing[2]) {
			bb.setImageResource(R.drawable.stopam);

			new Thread(this.dts[0]).start();
			new Thread(this.dts[1]).start();
			new Thread(this.dts[2]).start();

			for(int i = 0; i < 3; i++) {
				onGoing[i] = true;
			}
		} else {
			benchmarkAchieved = true;
			cumulativeTimer.cancel();
			resetBenchmark();
		}

	}

	void updateServerImages(int id) {
		if(sc[id] == 200)
			si[id].setImageResource(R.drawable.serveronline);
		else
			si[id].setImageResource(R.drawable.serveroffline);
	}

	public static void startCumulativeBenchmark() {

		cumulativeTimer = new Timer();
		cumulativeTimer.scheduleAtFixedRate(new TimerTask(){

			@Override
			public void run() {
				Benchmark.UiUpdater.post(Benchmark.UiUpdaterRunnable);
				if(!benchmarkAchieved) {
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

							float median = (float) average[(STEADY_STATE_TIME) / 2] / 1048576;

							double count = 0;
							for(int i = 0; i < STEADY_STATE_TIME; i++) {
								count += average[i];
							}
							float avg = (float) (count / STEADY_STATE_TIME / 1048576);
							fr = avg;

							double tot_mb = (Benchmark.totals[0] + Benchmark.totals[1] + Benchmark.totals[2]);
							tot_mb = tot_mb / 1048576 / 8;
							tm = tot_mb;

							this.cancel();
						}
					}
				} 
			}
		},0, 1000);
	}
}
