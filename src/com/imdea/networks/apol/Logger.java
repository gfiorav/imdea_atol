package com.imdea.networks.apol;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
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

	public static Database db;
	
	public static UploadDB udb;

	LocationManager lm;
	Location location;

	TelephonyManager tm;
	ConnectivityManager cm;

	SystematicDownloads sd;

	private int LOCATION_REFRESH_DISTANCE 		= 30;
	private int LOCATION_REFRESH_TIME 			= 15000;

	private int SYSTEMATIC_DOWNLOAD_PERIOD_MIN 	= 50; 
	
	public static boolean hasNewPoints 				= false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_logger);

		Logger.context = getApplicationContext();

		Logger.vib = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
		registerEventListeners();
		
		udb 				= new UploadDB();

		db 					= new Database(Logger.context);
		lm 					= (LocationManager) Logger.context.getSystemService(Context.LOCATION_SERVICE);
		location  					= lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);

		tm  				= (TelephonyManager) Logger.context.getSystemService(Context.TELEPHONY_SERVICE);
		cm  				= (ConnectivityManager) Logger.context.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		setUp();
	}

	public void setUp() {
		this.sd = new SystematicDownloads();
		
		Logger.nick = Build.MODEL;

		EditText sd_period = (EditText) findViewById(R.id.sd_period);
		sd_period.setText("" + SYSTEMATIC_DOWNLOAD_PERIOD_MIN);

		EditText min_distance = (EditText) findViewById(R.id.min_distance);
		min_distance.setText("" + LOCATION_REFRESH_DISTANCE);

		EditText min_time = (EditText) findViewById(R.id.min_time);
		min_time.setText("" + LOCATION_REFRESH_TIME / 1000);
		
		TextView points_in_db = (TextView) findViewById(R.id.points_in_db);
		points_in_db.setText(db.measurements() + " measurments in the DB");
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

	public void startGPS () {
		lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_REFRESH_TIME, LOCATION_REFRESH_DISTANCE, LocationListener);
	}

	public void stopGPS () {
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

	public void registerEventListeners() {
		
		Button upload 	= (Button) findViewById(R.id.upload_db);
		upload.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				udb = new UploadDB();
				if(!uploading) {
					uploading = !uploading;
					udb.execute();
				}
				else {
					udb.cancel(true);
					uploading = !uploading;
				}
			}
		});

		Switch SDSwitch = (Switch) findViewById(R.id.sytematic_downloads_switch);
		SDSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton Switch, boolean checked) {
				Logger.vib.vibrate((long) 150);

				if(checked) startSD(); else stopSD();

				SDOn = !SDOn;
				
			}
		});

		EditText sd_period = (EditText) findViewById(R.id.sd_period);
		sd_period.setOnEditorActionListener(new OnEditorActionListener() {

			public boolean onEditorAction(TextView text, int key, KeyEvent action) {
				try { SYSTEMATIC_DOWNLOAD_PERIOD_MIN = Integer.parseInt(text.getText().toString()); } catch (Exception e) { setUp(); }
				return false;
			}
		});

		Switch GPSSwitch = (Switch) findViewById(R.id.GPS_switch);
		GPSSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			public void onCheckedChanged(CompoundButton Switch, boolean checked) {
				Logger.vib.vibrate((long) 150);

				if(checked) startGPS(); else stopGPS();

				GPSOn = !GPSOn;
			}

		});
		
		EditText min_distance = (EditText) findViewById(R.id.min_distance);
		min_distance.setOnEditorActionListener(new OnEditorActionListener() {

			public boolean onEditorAction(TextView text, int key, KeyEvent action) {
				try { LOCATION_REFRESH_DISTANCE = Integer.parseInt(text.getText().toString()); } catch (Exception e) { setUp(); }
				return false;
			}
		});
		
		EditText min_time = (EditText) findViewById(R.id.min_time);
		min_time.setOnEditorActionListener(new OnEditorActionListener() {

			public boolean onEditorAction(TextView text, int key, KeyEvent action) {
				try { LOCATION_REFRESH_TIME = Integer.parseInt(text.getText().toString()) * 1000; } catch (Exception e) { e.printStackTrace(); setUp(); }
				return false;
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
