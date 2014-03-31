package com.imdea.networks.apol;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import android.net.MailTo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class Logger extends Activity {

	private ApplicationLogger al;

	public boolean isLogging = false;
	public static Context context;

	public static Vibrator vib;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_logger);

		this.context = getApplicationContext();
		this.al = new ApplicationLogger();

		this.vib = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);
		registerEventListeners();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.logger, menu);
		return true;
	}



	public void registerEventListeners() {
		ImageButton loggingButton = (ImageButton) findViewById(R.id.logbutton);
		loggingButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				Logger.vib.vibrate((long) 150);

				ImageButton loggingButton = (ImageButton) arg0.findViewById(R.id.logbutton);
				if(!isLogging) {
					loggingButton.setImageResource(R.drawable.stoplogging);
					al.startLogging();
				} else {
					loggingButton.setImageResource(R.drawable.startlogging);
					al.stopLoggin();
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
				emailIntent.putExtra(Intent.EXTRA_SUBJECT, "I found a BUG in the IMDEA ATOL app");
				
				try {
			         startActivity(Intent.createChooser(emailIntent, "Send mail..."));
			         finish();
			      } catch (android.content.ActivityNotFoundException ex) {
			         Toast.makeText(Logger.this, 
			         "There is no email client installed.", Toast.LENGTH_SHORT).show();
			      }
				
			}
			
		});
	}


}
