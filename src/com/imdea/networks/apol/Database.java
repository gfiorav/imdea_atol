package com.imdea.networks.apol;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.Settings.Secure;


public class Database extends SQLiteOpenHelper {
	
	private static String android_id = Secure.getString(Logger.context.getContentResolver(), Secure.ANDROID_ID); 
	
	private static final int DB_VERSION = 1;
	private static final String DB_NAME = "IMDEA_atol_db_" + android_id;
	
	private final static String KEY_MEASUREMENTS_TABLE 	= "measurements";
	
	private final static String KEY_TIMESTAMP 	= "timestamp";
	private final static String KEY_LATITUDE 	= "latitude";
	private final static String KEY_LONGITUDE 	= "longitude";
	private final static String KEY_ACCURACY 	= "accuracy";
	private final static String KEY_BEARING 	= "bearing";
	private final static String KEY_SATELLITES 	= "satellites";
	private final static String KEY_CELL_ID 	= "cell_id";
	private final static String KEY_CELL_LAC 	= "cell_lac";
	private final static String KEY_WIFI 		= "wifi";

	public Database(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + KEY_MEASUREMENTS_TABLE + " ( " +
					"id INTEGER PRIMARY KEY AUTOINCREMENT, " +
					"timestamp INTEGER, " +
					"latitude REAL," +
					"longitude REAL," +
					"accuracy REAL," +
					"bearing REAL," +
					"satellites INTEGER," +
					"cell_id INTEGER," +
					"cell_lac INTEGER, " +
					"wifi INTEGER" +
					" )";
		
		db.execSQL(CREATE_TABLE);
	}

	public void add(Measurement m) {
		SQLiteDatabase db = this.getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(KEY_TIMESTAMP, m.timestamp);
		values.put(KEY_LATITUDE, m.latitude);
		values.put(KEY_LONGITUDE, m.longitude);
		values.put(KEY_ACCURACY, m.accuracy);
		values.put(KEY_BEARING, m.bearing);
		values.put(KEY_SATELLITES, m.satellites);
		values.put(KEY_CELL_ID, m.cell_id);
		values.put(KEY_CELL_LAC, m.cell_lac);
		values.put(KEY_WIFI, m.wifi);

		db.insert(KEY_MEASUREMENTS_TABLE, null, values);
		
		db.close();
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}

}
