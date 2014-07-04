package com.imdea.networks.apol;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class SystematicDownloads {
	
	private final int MTU 			= 1500;
	private int MAX_RETRIES 		= 5;
	
	private Timer timer = new Timer();

	/****************** SERVER FILE ADDRESS ******************/
	public String URL 		= "http://testvelocidad1.orange.es/speedtest/random1000x1000.jpg";
	public String tiny_URL 	= "http://testvelocidad1.orange.es/speedtest/random500x500.jpg";
	public String forofor 	= "http://www.google.com/i/love/pretzels";
	/****************** SERVER FILE ADDRESS ******************/
	
	public void comence(int period) {
		if(timer != null) stop(); else timer.schedule(new TimerTask() { public void run() { start(); } }, 0, period*60000);
	}
	
	public void stop() { timer.cancel(); timer = new Timer();}

	void start() {
		int tries = 0;
		while(!downloadFile(this.tiny_URL, 50 * this.MTU) && (tries++) <= this.MAX_RETRIES);

		/* Marker download */
		downloadFile(this.forofor, 50 * this.MTU);

		tries = 0;
		while(!downloadFile(this.tiny_URL, 50 * this.MTU) && (tries++) <= this.MAX_RETRIES);

		/* Marker download */
		downloadFile(this.forofor, 50 * this.MTU);

		tries = 0;
		while(!downloadFile(this.URL, 2*1024*1024) && (tries++) <= this.MAX_RETRIES);

		/* Marker download */
		downloadFile(this.forofor, 50 * this.MTU);

		tries = 0;
		while(!downloadFile(this.tiny_URL, 50 * this.MTU) && (tries++) <= this.MAX_RETRIES);

		/* Marker download */
		downloadFile(this.forofor, 50 * this.MTU);

		tries = 0;
		while(!downloadFile(this.tiny_URL, 50 * this.MTU) && (tries++) <= this.MAX_RETRIES);

		/* Marker download */
		downloadFile(this.forofor, 50 * this.MTU);
		
		

	}
	
	private boolean downloadFile(String url, int bytes) {
		int bytes_read = 0;

		InputStream input 				= null;
		HttpURLConnection connection 	= null;
		
		try {
			URL u 		= new URL(url);
			connection 	= (HttpURLConnection) u.openConnection();
			connection.connect();

			if(connection.getResponseCode() != 200) {
				// FILE NOT FOUND
				connection.disconnect();
				return false;
			}

			input = connection.getInputStream();

			while(input.read() != -1)
			{
				if(bytes_read < bytes) {
					bytes_read++;
				}
				else {
					input.close();
					connection.disconnect();
					return true;
				}
			}

			input.close();
			connection.disconnect();
			return true;

		} catch(Exception e) {
			// CONN EXCEPTION
			e.printStackTrace();
			connection.disconnect();
			return false;
		}

	}
}
