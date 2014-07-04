package com.imdea.networks.apol;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class Logger extends Activity {

	public static Context context;

	public static Vibrator vib;

	public static boolean isLogging = false;
	
	public static final String FOLDER_NAME = "IMDEA";
	
	Database db;

	LocationManager lm;
	Location location;

	TelephonyManager tm;
	ConnectivityManager cm;

	private final int LOCATION_REFRESH_TIME = 15000;
	private final int LOCATION_REFRESH_DISTANCE = 30;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_logger);

		Logger.context = getApplicationContext();

		Logger.vib = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
		registerEventListeners();

		setUp();
		
		db = new Database(Logger.context);
		lm 					= (LocationManager) Logger.context.getSystemService(Context.LOCATION_SERVICE);
		location  					= lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		
		tm  				= (TelephonyManager) Logger.context.getSystemService(Context.TELEPHONY_SERVICE);
		cm  				= (ConnectivityManager) Logger.context.getSystemService(Context.CONNECTIVITY_SERVICE);
	}

	public void setUp() {
		ImageButton sb = (ImageButton) findViewById(R.id.logbutton);
		if(isLogging) {
			sb.setImageResource(R.drawable.stoplogging);
		} else {
			sb.setImageResource(R.drawable.startlogging);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.logger, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch (item.getItemId()) {
		case R.id.benchmark:
			Intent benchmark = new Intent(getApplicationContext(), Benchmark.class);
			startActivity(benchmark);
			return true;

		default:
			return false;
		}
	}

	public void startLogging () {
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, LocationListener);
	}

	public void stopLogging () {
		lm.removeUpdates(LocationListener);
	}


	private LocationListener LocationListener = new LocationListener() {
		@Override
		public void onLocationChanged(final Location location) {
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
			try { satellites 	= Integer.parseInt(location.getExtras().getString("satellites")); } catch (Exception e) {}
			int cell_id = -1;
			int cell_lac = -1;
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

	public void registerEventListeners() {
		ImageButton loggingButton = (ImageButton) findViewById(R.id.logbutton);
		loggingButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				Logger.vib.vibrate((long) 150);

				ImageButton loggingButton = (ImageButton) arg0.findViewById(R.id.logbutton);
				if(!isLogging) {
					loggingButton.setImageResource(R.drawable.stoplogging);
					startLogging();
				} else {
					loggingButton.setImageResource(R.drawable.startlogging);
					stopLogging();
				}

				isLogging = !isLogging;

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


}
