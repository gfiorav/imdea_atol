package com.imdea.networks.apol;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;

public class SystematicDownloads extends Activity {

	private boolean isRunning;
	private Intent sds;

	public static int kb = 10;
	public static int granularity = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_systematic_downloads);
		// Show the Up button in the action bar.
		setupActionBar();

		this.isRunning = false;
	}

	public void startSystematic(View view) {
		updateValues();
		if(!this.isRunning) {
			this.sds = new Intent(getApplicationContext(), SystematicDownloadService.class);
			startService(sds);
			ImageButton ib = (ImageButton) view;
			ib.setImageResource(R.drawable.systematic_downloads_stop);
		} else {
			stopService(sds);
			ImageButton ib = (ImageButton) view;
			ib.setImageResource(R.drawable.systematic_downloads_start);
		}

		this.isRunning = !this.isRunning;
	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.systematic_downloads, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void updateValues() {
		try {
			int granularity = Integer.parseInt(((EditText) findViewById(R.id.granularity)).getText().toString());
			if(granularity != 0) {
				this.granularity = granularity;
			}

			int kb = Integer.parseInt(((EditText) findViewById(R.id.kb_of_data)).getText().toString());
			if(kb != 0) {
				this.kb = kb;
			}
		} catch (NumberFormatException nfe) {
			// IGNORE
		}
	}

}
