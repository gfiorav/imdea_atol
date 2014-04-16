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
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.wtf("HTTP", "CODE: " + connection.getResponseCode());
            }

            // this will be useful to display download percentage
            // might be -1: server did not report the length
            Benchmark.file_sizes[this.id] = connection.getContentLength();

            // download the file
            input = connection.getInputStream();
            output = new FileOutputStream(Environment.getExternalStorageDirectory() + File.separator + ApplicationLogger.FOLDER_NAME + File.separator + ".bnchmrk" + File.separator + this.id + "-bnch.mrk");

            byte data[] = new byte[4096];
            
            // Timestamp
            Benchmark.benchmark_start[this.id] = System.currentTimeMillis();
            
            int count;
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
                Benchmark.totals[this.id] += count;
                
                Benchmark.UiUpdater.post(Benchmark.UiUpdaterRunnable);
            }
            
            Benchmark.benchmark_stop[this.id] = System.currentTimeMillis();
        } catch (Exception e) {
            e.printStackTrace();
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