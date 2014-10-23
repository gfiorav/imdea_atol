package com.imdea.networks.apol;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class Logger extends Activity {

	public static Context context;

	public static Vibrator vib;

	public static boolean GPSOn = false;
	public static boolean SDOn = false;
	public static boolean uploading = false;

	public static String nick;

	public static final String FOLDER_NAME = "IMDEA";
	public static final int BUFF_LEN = 1024;

	public static String file_path;
	public static final String tcpdump_path = "/sdcard/TCPDump-traces/";
	public static String dump_file_name;
	public static final int bytes_per_packet = 80;
	public static final int MB_per_file = 30;

	public static final String COMMAND = "tcpdump -w " + tcpdump_path + " -s " + bytes_per_packet + " -C " + MB_per_file + "\n";

	public static Database db;

	public static UploadDB udb;
	
	PacketCounter pc;
	Timer t;
	TimerTask tt;

	LocationManager lm;
	Location location;

	TelephonyManager tm;
	ConnectivityManager cm;

	SystematicDownloads sd;

	private int LOCATION_REFRESH_DISTANCE 		= 30;
	private int LOCATION_REFRESH_TIME 			= 15000;

	private int SYSTEMATIC_DOWNLOAD_PERIOD_MIN 	= 50; 

	public static final String START_TCPDUMP = "Start TCPDump";
	public static final String STOP_TCPDUMP = "Stop TCPDump";
	public static boolean TCPDumpRunning = false;

	public static boolean hasNewPoints 				= false;

	public static Handler UiUpdater;
	public static Runnable UiUpdaterRunnable;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_logger);
	
		setUp();
		registerEventListeners();
	}

	public void setUp() {
		Logger.nick = Build.MODEL;
		
		tt = new PacketCounter();

	}
	
	public void start() {
		startTCPDump();
		
		t = new Timer();
		t.scheduleAtFixedRate(tt, 0, 1000);
		
		TCPDumpRunning = true;
	}
	
	public void stop() {
		stopTCPDump();
		
		t.cancel();
		t = null;
		
		TCPDumpRunning = false;
	}
	
	public void registerEventListeners() {

		Button tcpdump 	= (Button) findViewById(R.id.start_tcpdump);
		tcpdump.setOnClickListener(new OnClickListener() {
			public void onClick(View button) {
				Button b = (Button) button;

				if(!TCPDumpRunning) {
					b.setText(STOP_TCPDUMP);
					b.setBackgroundColor(Color.RED);

					start();
				}
				else {
					b.setText(START_TCPDUMP);
					b.setBackgroundColor(Color.GRAY);

					stop();
				}

			}
		});


		TextView developerLink = (TextView) findViewById(R.id.bugs_email);
		developerLink.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				Intent emailIntent = new Intent(Intent.ACTION_SEND);

				emailIntent.setData(Uri.parse("mailto:"));
				emailIntent.setType("text/plain");

				emailIntent.putExtra(Intent.EXTRA_EMAIL  , new String[]{getResources().getString(R.string.bugs_email)});
				emailIntent.putExtra(Intent.EXTRA_SUBJECT, "BUG in the IMDEA ATOL App!");
				emailIntent.putExtra(Intent.EXTRA_TEXT, "Hello!\n\nI was using the IMDEA ATOL App on my " +android.os.Build.MODEL +" and discovered the following bug: \n\n");

				try {
					startActivity(Intent.createChooser(emailIntent, "Send mail..."));


				} catch (android.content.ActivityNotFoundException ex) {
					Toast.makeText(getApplicationContext(), 
							"There is no email client installed.", Toast.LENGTH_SHORT).show();
				}


			}

		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.logger, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		return true;
	}

	public void startGPS () {
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, LocationListener);
	}

	public void stopGPS () {
		lm.removeUpdates(LocationListener);
	}

	public void recordPoint () {
		GsmCellLocation cl 					= (GsmCellLocation) tm.getCellLocation();
		NetworkInfo WiFi 					= cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		double latitude 	= 0;
		double longitude 	= 0;
		if(location != null) {
			latitude 	= location.getLatitude();
			longitude 	= location.getLongitude();
		}
		float accuracy 	= location.getAccuracy();
		float bearing 	= location.getBearing();
		int satellites 	= -1;
		try { satellites 	= location.getExtras().getInt("satellites"); } catch (Exception e) {}
		int cell_id 	= -1;
		int cell_lac 	= -1;
		if(cl != null) {
			cell_id 	= cl.getCid();
			cell_lac 	= cl.getLac();
		}
		int wifi 		= 0;
		if(WiFi.isConnected()) {
			wifi = 1;

		}

		Measurement m = new Measurement(latitude, longitude, accuracy, bearing, satellites, cell_id, cell_lac, wifi);

		db.add(m);

		hasNewPoints = true;

		setUp();

		Logger.UiUpdater.post(Logger.UiUpdaterRunnable);
	}

	public void startTCPDump () {	

		EditText file_prefix = (EditText) findViewById(R.id.file_prefix);

		File path = new File(tcpdump_path);
		if(!path.exists()) {
			path.mkdir();
		}

		file_path = tcpdump_path + file_prefix.getText().toString() + '-' + System.currentTimeMillis() + ".dump";

		try {
			String command="/system/xbin/tcpdump -w '" + file_path + "' -s " + bytes_per_packet + " -C " + MB_per_file + "\n";
			Log.wtf("command", command);

			Process process = Runtime.getRuntime().exec("su");  
			DataOutputStream os = new DataOutputStream(process.getOutputStream());  
			os.writeBytes(command);  
			os.flush();  

			os.close(); 
		}
		catch(Exception e) {
			e.printStackTrace();
		}

	}

	public void stopTCPDump () {

		try {
			String command="ps tcpdump\n";

			Process process = Runtime.getRuntime().exec("su");  
			DataOutputStream os = new DataOutputStream(process.getOutputStream());  
			os.writeBytes(command);  
			os.flush();  

			BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = null;

			ArrayList<String> pids = new ArrayList<String>();

			int lines_read = 0;
			while ( lines_read < 2 ) {
				line = br.readLine();
				if(lines_read != 0) { 
					if(line != null) {
						String [] parts = line.split("\\s+");
						pids.add(parts[1]);
					}
				}
				lines_read++;
			}

			for(String pid : pids) {
				String kill_command = "kill " + pid + "\n";
				os.writeBytes(kill_command);
				os.flush();


			}

			os.writeBytes("exit\n");
			os.flush();

			os.close(); 

		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	private LocationListener LocationListener = new LocationListener() {
		@Override
		public void onLocationChanged(final Location location) {
			recordPoint();
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub

		}
	};

	public void startSD() { this.sd.comence(SYSTEMATIC_DOWNLOAD_PERIOD_MIN); }
	public void stopSD() { this.sd.stop(); }

	


}
