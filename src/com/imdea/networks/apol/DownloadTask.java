package com.imdea.networks.apol;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.net.UnknownHostException;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

public class DownloadTask extends AsyncTask<String, Integer, String> {

	private Socket s;
	private int server_port = 20;

	private InputStream is;

	private int id;

	private int chunk_size = 512;
	private int timeout = 10000;

	private Context context;

	public DownloadTask(Context context) {
		this.context = context;
	}

	@Override
	protected String doInBackground(String... params) {
		try {
			SocketAddress sockaddr = new InetSocketAddress(
                    params[0], this.server_port);
			this.s = new Socket();
			this.s.connect(sockaddr, this.timeout);
			
			this.is = this.s.getInputStream();
		} catch (UnknownHostException e) {
			Log.wtf("IP RESOLVE", "Unknown host: " + params[0]);
			e.printStackTrace();
		} catch (MalformedURLException e) {
			Log.wtf("IP RESOLVE", "Malformed address");
			e.printStackTrace();
		} catch (IOException e) {
			Log.wtf("SOCKET RESOLVE", "IOException");
			e.printStackTrace();
		}
		this.id = Integer.parseInt(params[1]);

		byte bytes [] = new byte [chunk_size];
		int bytes_read;

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		if(is != null) {
			FileOutputStream fos = null;
			BufferedOutputStream bos = null;

			try {
				fos = new FileOutputStream(Environment.getExternalStorageDirectory() + File.separator + ApplicationLogger.FOLDER_NAME + File.separator + ".bnchmrks" + File.separator + this.id + ".adf");
				bos = new BufferedOutputStream(fos);

				bytes_read = is.read(bytes, 0, this.chunk_size);

				do {
					baos.write(bytes);
					bytes_read = is.read(bytes);
				} while (bytes_read != -1);

				bos.write(baos.toByteArray());
				bos.flush();
				bos.close();
				this.s.close();

			} catch (IOException e) {
				Log.wtf("WRITING DOWNLOAD", "IOException");
			}
		}
		return null;
	}

	@Override
	protected void onPostExecute(String result) {
	}

	@Override
	protected void onPreExecute() {
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
	}

}