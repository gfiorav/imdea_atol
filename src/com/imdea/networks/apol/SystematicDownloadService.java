package com.imdea.networks.apol;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class SystematicDownloadService extends Service {
	
	private Timer t;

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	void startTimer() {
		this.t = new Timer();
		this.t.scheduleAtFixedRate(new TimerTask(){

			@Override
			public void run() {
				
				int bits_read = 0;

				InputStream input = null;
				HttpURLConnection connection = null;
				try {
					URL url = new URL("http://testvelocidad1.orange.es/speedtest/random4000x4000.jpg");
					connection = (HttpURLConnection) url.openConnection();
					connection.connect();

					if(connection.getResponseCode() == 200) {
						Log.wtf("CONN ERROR", "FILE NOT FOUND");
					}

					input = connection.getInputStream();

					while(input.read() != -1)
					{
						if(bits_read < ((SystematicDownloads.kb)*1000)) {
							bits_read += 8;
						}
						else {
							input.close();
						}
					}
					
					connection.disconnect();


				} catch(Exception e) {

				}
				
				Log.wtf("granularity", "" +SystematicDownloads.granularity);
				Log.wtf("kb", "" +SystematicDownloads.kb);
			}

		}, 0, SystematicDownloads.granularity * 1000);
		
		
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		startTimer();
		return Service.START_NOT_STICKY;
		
	}
	
	@Override
	public void onDestroy() {
		this.t.cancel();
	}

}
