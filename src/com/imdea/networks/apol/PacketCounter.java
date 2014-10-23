package com.imdea.networks.apol;

import java.util.TimerTask;

import android.net.TrafficStats;
import android.util.Log;

public class PacketCounter extends TimerTask {
	final int defaultThreshold = 500;
	
	private long totalRx;
	private long totalTx;

	private int threshold;

	private TrafficStats ts;

	public PacketCounter() {
		this.threshold = defaultThreshold;

		init();
	}

	public PacketCounter(int threshold) {
		this.threshold = threshold;

		init();
	}

	@Override
	public void run() {
		long accRx = ts.getMobileRxBytes();
		long accTx = ts.getMobileTxBytes();
		
		
		long currentRx = totalRx - accRx;
		long currentTx = totalTx - accTx;

		if(currentRx > threshold || currentTx > threshold) {
			// Launch speedtest

			Log.wtf("Log", currentRx + " : " + currentTx);
		}
		
		
		totalRx = accRx;
		totalTx = accTx;
	}

	void init() {
		ts = new TrafficStats();

		totalRx = ts.getMobileRxBytes(); 
		totalTx = ts.getMobileTxBytes();

	}

}
