package com.imdea.networks.apol;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.TrafficStats;

public class ApplicationLogger {

	final ActivityManager activityManager = (ActivityManager) Logger.context.getSystemService(Context.ACTIVITY_SERVICE);
	final List<RunningTaskInfo> recentTasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

	private List<ApplicationInfo> appsInfo = Logger.context.getPackageManager().getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);

	private long prevTxBytes [] = new long [this.appsInfo.size()];
	private long prevRxBytes [] = new long [this.appsInfo.size()];
	private long prevTxPackets [] = new long [this.appsInfo.size()];
	private long prevRxPackets [] = new long [this.appsInfo.size()];

	private Timer timer;
	
	private String path = "sdcard/Download/imdea_atol.xml";


	// Constructor	
	public ApplicationLogger() {
		initiateRxTxMatrices();
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

	}

	public void startLogging() {

		this.timer = new Timer();


		this.timer.schedule(new TimerTask() {			
			public void run() {
				logActivity();
			}

		}, 0, 1000);

	}

	public void stopLoggin() {
		this.timer.cancel();
	}

	@SuppressLint("NewApi")
	public void logActivity () {
		try {
			boolean appending = false;
			
			// Signal if file exists, we don't declare xml header
			RandomAccessFile rof = new RandomAccessFile(path, "rw");
			
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

			Calendar c = Calendar.getInstance(); 
			int millisecond 	= c.get(Calendar.MILLISECOND);
			int second 			= c.get(Calendar.SECOND);
			int minute 			= c.get(Calendar.MINUTE);
			int hour			= c.get(Calendar.HOUR);
			int day				= c.get(Calendar.DAY_OF_MONTH);
			int month 			= c.get(Calendar.MONTH);
			int year 			= c.get(Calendar.YEAR);

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
					rof.write("\t<event>\n".getBytes());
					
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

}
