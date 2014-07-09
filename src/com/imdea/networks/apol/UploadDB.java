package com.imdea.networks.apol;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.AsyncTask;
import android.util.Log;

public class UploadDB extends AsyncTask<String, Void, String>{

	private final int PORT 			= 1991;
	private final int BUFFER_SIZE 	= 1024;
	private final int EOF 			= -1;

	private final String TICKI 		= "TICKI";
	private final String TACKA 		= "TACKA";
	private final String FFP 		= "FREEFORPASS";
	private final char SEP 			= ':';

	public void upload() {
		try {
			InetAddress srvr_addr 	= InetAddress.getByName(Logger.db.address);
			Socket sock 			= new Socket(srvr_addr, PORT);

			File db_file 			= Logger.context.getDatabasePath(Logger.db.getDatabaseName());
			FileInputStream fis 	= new FileInputStream(db_file);
			OutputStream os 		= sock.getOutputStream();
			InputStream is 			= sock.getInputStream();

			int read;
			byte [] buffer = new byte [BUFFER_SIZE];

			// Wait server confirmation
			read = is.read(buffer);
			String received = new String(buffer);
			if(!received.contains(FFP)) { this.cancel(true); }

			// Send TICKI to server
			String ticki 			= constructTicki(db_file.getName(), db_file.length()); 
			os.write(ticki.getBytes(), 0, ticki.length());


			// Wait for TACKA from server
			read = is.read(buffer);
			received = new String(buffer);
			if(!received.contains(TACKA)) { this.cancel(true); }


			buffer = new byte [(int) db_file.length()];
			// Send File
			while((read = fis.read(buffer)) != EOF) {
				os.write(buffer, 0, read);
			}

			os.flush();
			os.close();
			fis.close();
			sock.close();
			
			

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {

		}
	}

	String constructTicki(String name, long size) {
		return TICKI + SEP + name + SEP + size + SEP + Logger.nick;
	}

	@Override
	protected String doInBackground(String... arg0) {
		upload();		
		return null;
	}
}
