package com.imdea.networks.apol;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import android.os.AsyncTask;

public class UploadDB extends AsyncTask<String, Void, String>{
	public void upload() {
		try {
			InetAddress srvr_addr = InetAddress.getByName("172.16.4.151");
			
			Socket sock = new Socket(srvr_addr, 6000);
		
			OutputStream out = sock.getOutputStream();
			PrintWriter output = new PrintWriter(out);

			output.println("CONNECTED!!!");
			
			out.flush();
			out.close();

			sock.close();


		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	protected String doInBackground(String... arg0) {
		upload();		
		return null;
	}
}
