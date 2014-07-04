package com.imdea.networks.apol;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Timestamp;

import android.os.Environment;
import android.util.Log;


public class DownloadTask implements Runnable {	
	private String link;
	private int id;

	private final int BITS_IN_BYTE = 8;

	public DownloadTask (String link, int id) {
		this.link = link;
		this.id = id;
	}

	@Override
	public void run() {
		InputStream input = null;
		OutputStream output = null;
		HttpURLConnection connection = null;
		try {
			URL url = new URL(this.link);
			connection = (HttpURLConnection) url.openConnection();
			connection.connect();

			// expect HTTP 200 OK, so we don't mistakenly save error report
			// instead of the file
			int code = connection.getResponseCode();
			if (code != HttpURLConnection.HTTP_OK) {
				Log.wtf("HTTP", "CODE: " + connection.getResponseCode());
			}

			Benchmark.sc[this.id] = code;

			// this will be useful to display download percentage
			// might be -1: server did not report the length
			Benchmark.file_sizes[this.id] = connection.getContentLength() * BITS_IN_BYTE;

			// download the file
			input = connection.getInputStream();
			output = new FileOutputStream(Environment.getExternalStorageDirectory() + File.separator + Logger.FOLDER_NAME + File.separator + ".bnchmrk" + File.separator + this.id + "-bnch.mrk");

			byte data[] = new byte[4096];

			int count;
			int total = 0;

			Benchmark.ready[this.id] = true;
			Benchmark.UiUpdater.post(Benchmark.UiUpdaterRunnable);

			// We wait for the other downloads to be ready before we start 
			while(!Benchmark.ready[0] || !Benchmark.ready[1] || !Benchmark.ready[2]);

			// 0, for example, starts the timer
			if(this.id == 0) {
				Benchmark.startCumulativeBenchmark();
			}

			while ((count = input.read(data)) != -1) {
				if(Benchmark.benchmarkAchieved)
					connection.disconnect();

				output.write(data, 0, count);
				if(Benchmark.benchmark_start[this.id] == 0) {
					Benchmark.benchmark_start[this.id] = System.currentTimeMillis();
				}
				
				if(Benchmark.slow_start_ptr >= Benchmark.SLOW_START_DURATION) {
					total += count;
					Benchmark.totals[this.id] = total  * BITS_IN_BYTE;
				}
				
				Benchmark.UiUpdater.post(Benchmark.UiUpdaterRunnable);
			}

			Benchmark.benchmark_stop[this.id] = System.currentTimeMillis();
			Benchmark.onGoing[this.id] = false;
		} catch (Exception ignored) {
			//e.printStackTrace();
		} finally {
			try {
				if (output != null)
					output.close();
				if (input != null)
					input.close();
			} catch (IOException ignored) {
			}

			if (connection != null)
				connection.disconnect();

		}
		return;

	}

}