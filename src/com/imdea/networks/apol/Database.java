package com.imdea.networks.apol;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.Settings.Secure;


public class Database extends SQLiteOpenHelper {

	private static String android_id = Secure.getString(Logger.context.getContentResolver(), Secure.ANDROID_ID); 
	
	private static final String DEFAULT_ADDRESS = "127.0.0.1";
	public static final String address 			= DEFAULT_ADDRESS;

	private static final int DB_VERSION = 1;
	public static final String DB_NAME = "IMDEA_atol_db_" + android_id + '-' + ((int) ((System.currentTimeMillis() / 1000L)));

	private final static String KEY_MEASUREMENTS_TABLE 	= "measurements";

	private final static String KEY_ID 			= "id";
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
		String CREATE_MEASUREMENTS_TABLE = "CREATE TABLE IF NOT EXISTS " + KEY_MEASUREMENTS_TABLE + " ( " +
				KEY_ID 			+ " INTEGER PRIMARY KEY AUTOINCREMENT, " +
				KEY_TIMESTAMP 	+ " INTEGER, " +
				KEY_LATITUDE 	+ " REAL," +
				KEY_LONGITUDE 	+ " REAL," +
				KEY_ACCURACY 	+ " REAL," +
				KEY_BEARING 	+ " REAL," +
				KEY_SATELLITES 	+ " INTEGER," +
				KEY_CELL_ID 	+ " INTEGER," +
				KEY_CELL_LAC 	+ " INTEGER, " +
				KEY_WIFI 		+ " INTEGER" +
				" )";

		db.execSQL(CREATE_MEASUREMENTS_TABLE);

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

	public int measurements() {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.rawQuery("SELECT count(*) from " + KEY_MEASUREMENTS_TABLE, null);

		if(cursor != null) {
			cursor.moveToFirst();
			return cursor.getInt(0);
		}

		return 0;
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

}
