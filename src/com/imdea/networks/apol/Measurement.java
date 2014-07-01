package com.imdea.networks.apol;

public class Measurement {

	public double latitude, longitude;
	public int timestamp, cell_id, cell_lac, wifi;
	
	public Measurement(double latitude, double longitude, int cell_id, int cell_lac, int wifi) {
		this.timestamp 	= (int) ((System.currentTimeMillis() / 1000L));
		this.longitude 	= longitude;
		this.latitude 	= latitude;
		this.cell_id 	= cell_id;
		this.cell_lac 	= cell_lac;
		this.wifi 		= wifi;
	}
	
}
