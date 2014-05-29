package com.imdea.networks.apol;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

public class ApplicationLogger extends Service {

	final ActivityManager activityManager = (ActivityManager) Logger.context.getSystemService(Context.ACTIVITY_SERVICE);
	final List<RunningTaskInfo> recentTasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

	public static final String FOLDER_NAME = "IMDEA";

	private List<ApplicationInfo> appsInfo = Logger.context.getPackageManager().getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);

	private long prevTxBytes [] = new long [this.appsInfo.size()];
	private long prevRxBytes [] = new long [this.appsInfo.size()];
	private long prevTxPackets [] = new long [this.appsInfo.size()];
	private long prevRxPackets [] = new long [this.appsInfo.size()];
	private long prevTotalRxPackets;
	private long prevTotalTxPackets;

	public static LocationManager lm= (LocationManager) Logger.context.getSystemService(Context.LOCATION_SERVICE);
	public static Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

	private final int LOCATION_REFRESH_TIME = 60000;
	private final int LOCATION_REFRESH_DISTANCE = 90;

	private final int LOG_TIMER = 1000;
	private final int DOWLOAD_TIMER = 5*60*1000;
	
	private final int MTU = 1500;
	private int MAX_RETRIES = 5;
	
	/****************** SERVER FILE ADDRESS ******************/
	private final String URL = "http://www.mona.ps.e-technik.tu-darmstadt.de/staticfiles/file5mb.data";
	/****************** SERVER FILE ADDRESS ******************/

	private int current_day;

	private Timer loggerTimer;
	private Timer downloadTimer;

	private String path;

	public boolean isLogging = false;

	public void initiate() {
		this.current_day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
		initiateRxTxMatrices();
		setUp();
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME,
				LOCATION_REFRESH_DISTANCE, LocationListener);
	}

	void setUp() {
		// Check if directory exists
		File directory = new File(Environment.getExternalStorageDirectory() + File.separator + FOLDER_NAME);

		if (!directory.exists()) {
			directory.mkdir();
		}

		updateFilePath();

	}

	void updateFilePath() {
		Calendar calendar = Calendar.getInstance();

		int day				= calendar.get(Calendar.DAY_OF_MONTH);
		int month 			= calendar.get(Calendar.MONTH);
		int year 			= calendar.get(Calendar.YEAR);

		String date = year + "-" + month + "-" +day;
		this.path = Environment.getExternalStorageDirectory() + File.separator + FOLDER_NAME + File.separator + date + ".xml";

		this.current_day = calendar.get(Calendar.DAY_OF_MONTH);
	}

	@SuppressLint("NewApi")
	public void initiateRxTxMatrices () {		
		int i = 0;
		for(ApplicationInfo ai : this.appsInfo) {
			this.prevRxBytes[i] = TrafficStats.getUidRxBytes(ai.uid);
			this.prevTxBytes[i] = TrafficStats.getUidTxBytes(ai.uid);
			this.prevRxPackets[i] = TrafficStats.getUidRxPackets(ai.uid);
			this.prevTxPackets[i] = TrafficStats.getUidTxPackets(ai.uid);
			i++;
		}

		prevTotalRxPackets = TrafficStats.getTotalRxPackets();
		prevTotalTxPackets = TrafficStats.getTotalTxPackets();

	}

	public void startLogging() {
		this.loggerTimer 	= new Timer();
		this.downloadTimer 	= new Timer();

		this.loggerTimer.schedule(new TimerTask() {			
			public void run() {
				logActivity();
			}

		}, 0, LOG_TIMER);

		this.downloadTimer.schedule(new TimerTask() {
			public void run() {
				downloadFiles();
			}
		}, 0, DOWLOAD_TIMER);

		Logger.isLogging = true;
	}

	public void stopLoggin() {
		this.loggerTimer.cancel();
		this.downloadTimer.cancel();
		Logger.isLogging = false;
	}

	private LocationListener LocationListener = new LocationListener() {
		@Override
		public void onLocationChanged(final Location location) {
			ApplicationLogger.location = location;
		}

		@Override
		public void onProviderDisabled(String arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderEnabled(String arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			// TODO Auto-generated method stub

		}
	};

	@SuppressLint("NewApi")
	public void logActivity () {
		// Check if any new data was registered before logging
		long currentTotalRxPackets = TrafficStats.getTotalRxPackets();
		long currentTotalTxPackets = TrafficStats.getTotalTxPackets();

		if((currentTotalRxPackets - this.prevTotalRxPackets) > 0 || (currentTotalTxPackets - this.prevTotalTxPackets) > 0) {
			try {
				// Check if day has changed and therefore need a new file
				Calendar calendar = Calendar.getInstance();

				if(this.current_day != calendar.get(Calendar.DAY_OF_MONTH)){
					updateFilePath();
				}

				boolean appending = false;

				// Signal if file exists, we don't declare xml header
				RandomAccessFile rof = new RandomAccessFile(this.path, "rw");

				if(rof.length() > 0)
				{
					appending = true;
				}

				// If file existed, start before closing tag </events>, else declare header
				if(appending) {
					rof.seek(rof.length() -("</events>".length() +1));
				} else {
					rof.writeBytes("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone= \"yes\"?>\n");
					rof.writeBytes("<events>\n");
				}

				int millisecond 	= calendar.get(Calendar.MILLISECOND);
				int second 			= calendar.get(Calendar.SECOND);
				int minute 			= calendar.get(Calendar.MINUTE);
				int hour			= calendar.get(Calendar.HOUR_OF_DAY);
				int day				= calendar.get(Calendar.DAY_OF_MONTH);
				int month 			= calendar.get(Calendar.MONTH);
				int year 			= calendar.get(Calendar.YEAR);

				TelephonyManager tm = (TelephonyManager) Logger.context.getSystemService(Context.TELEPHONY_SERVICE);
				GsmCellLocation cl = (GsmCellLocation) tm.getCellLocation();

				int cellId = cl.getCid();
				int cellLac = cl.getLac();

				ConnectivityManager cm = (ConnectivityManager) Logger.context.getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

				int wifiState = 0;
				if(wifi.isConnected()){
					wifiState = 1;
				}

				int a = 0;
				for(ApplicationInfo ai : this.appsInfo)
				{

					int uid = ai.uid;

					long raw_rx_bytes = TrafficStats.getUidRxBytes(uid);
					long raw_tx_bytes = TrafficStats.getUidTxBytes(uid);
					long raw_rx_packets = TrafficStats.getUidRxPackets(uid);
					long raw_tx_packets = TrafficStats.getUidTxPackets(uid);

					long rx_bytes = raw_rx_bytes -this.prevRxBytes[a];
					long tx_bytes = raw_tx_bytes -this.prevTxBytes[a];
					long rx_packets = raw_rx_packets -this.prevRxPackets[a];
					long tx_packets = raw_tx_packets -this.prevTxPackets[a];

					if(rx_bytes != 0 || tx_bytes != 0) {
						rof.writeBytes("\t<event>\n");

						String package_name = ai.packageName;
						rof.writeBytes("\t\t<time>\n");
						rof.writeBytes("\t\t\t<day>" + day + "</day>" +"\n");
						rof.writeBytes("\t\t\t<month>" + month + "</month>" +"\n");
						rof.writeBytes("\t\t\t<year>" + year + "</year>" +"\n");
						rof.writeBytes("\t\t\t<hour>" + hour + "</hour>" +"\n");
						rof.writeBytes("\t\t\t<minute>" + minute + "</minute>" +"\n");
						rof.writeBytes("\t\t\t<second>" + second + "</second>" +"\n");
						rof.writeBytes("\t\t\t<millisecond>" + millisecond + "</millisecond>" +"\n");
						rof.writeBytes("\t\t</time>\n");
						rof.writeBytes("\t\t<location>\n");
						if(location != null) {
							rof.writeBytes("\t\t\t<latitude>" + location.getLatitude() + "</latitude>\n");
							rof.writeBytes("\t\t\t<longitude>" + location.getLongitude() + "</longitude>\n");
						}else {
							rof.writeBytes("\t\t\t<latitude>unavailable</latitude>\n");
							rof.writeBytes("\t\t\t<longitude>unavailable</longitude>\n");
						}
						rof.writeBytes("\t\t</location>\n");
						rof.writeBytes("\t\t<cell>\n");
						rof.writeBytes("\t\t\t<id>" + cellId + "</id>\n");
						rof.writeBytes("\t\t\t<lac>" + cellLac + "</lac>\n");
						rof.writeBytes("\t\t</cell>\n");
						rof.writeBytes("\t\t<wifi>" + wifiState + "</wifi>\n");
						rof.writeBytes("\t\t<app>" + package_name + "</app>" +"\n");
						rof.writeBytes("\t\t<rx>"+"\n");
						rof.writeBytes("\t\t\t<bytes>"+ rx_bytes +"</bytes>" +"\n");
						rof.writeBytes("\t\t\t<packets>"+ rx_packets +"</packets>" +"\n");
						rof.writeBytes("\t\t</rx>"+"\n");
						rof.writeBytes("\t\t<tx>"+"\n");
						rof.writeBytes("\t\t\t<bytes>"+ tx_bytes +"</bytes>" +"\n");
						rof.writeBytes("\t\t\t<packets>"+ tx_packets +"</packets>" +"\n");
						rof.writeBytes("\t\t</tx>"+"\n");
						rof.writeBytes("\t</event>\n");
					}

					this.prevRxBytes[a] = raw_rx_bytes;
					this.prevTxBytes[a] = raw_tx_bytes;
					this.prevRxPackets[a] = raw_rx_packets;
					this.prevTxPackets[a] = raw_tx_packets;

					a++;

				}

				rof.writeBytes("</events>\n");

				rof.close();
			} catch (IOException ioe) {
				// TODO Auto-generated catch block
				ioe.printStackTrace();
			}
		}

		this.prevTotalRxPackets = currentTotalRxPackets;
		this.prevTotalTxPackets = currentTotalTxPackets;
	}

	public void downloadFiles() {
		int tries = 0;
		while(!downloadFile(this.URL, 50 * this.MTU) && (tries++) <= this.MAX_RETRIES);
		
		tries = 0;
		while(!downloadFile(this.URL, 50 * this.MTU) && (tries++) <= this.MAX_RETRIES);
		
		tries = 0;
		while(!downloadFile(this.URL, 2*1024*1024) && (tries++) <= this.MAX_RETRIES);
		
		tries = 0;
		while(!downloadFile(this.URL, 50 * this.MTU) && (tries++) <= this.MAX_RETRIES);
		
		tries = 0;
		while(!downloadFile(this.URL, 50 * this.MTU) && (tries++) <= this.MAX_RETRIES);
		
		Log.wtf("DOWNLOAD", "SERIES COMPLETED");
	}

	private boolean downloadFile(String url, int bytes) {
		int bytes_read = 0;

		InputStream input = null;
		HttpURLConnection connection = null;
		try {
			URL u = new URL(url);
			connection = (HttpURLConnection) u.openConnection();
			connection.connect();

			if(connection.getResponseCode() == 200) {
				Log.wtf("CONN ERROR", "FILE NOT FOUND");
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
				}
			}

		} catch(Exception e) {
			connection.disconnect();
			return false;
		}
		
		connection.disconnect();
		return true;
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		initiate();
		startLogging();
		return Service.START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent arg0) {

		return null;
	}

}
