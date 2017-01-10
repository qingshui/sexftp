package org.sexftp.core.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import org.sexftp.core.exceptions.SFConnectionException;

public class UrlUtil {
	public static byte[] requestUrlData(String url, byte[] requestdata) {
		OutputStream outputStream = null;
		InputStream in = null;
		byte[] respDatas;
		try {
			HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();

			urlConnection.setReadTimeout(120000);
			urlConnection.setDoOutput(true);
			urlConnection.setDoInput(true);
			urlConnection.setUseCaches(false);
			urlConnection.setRequestMethod("GET");
			urlConnection.setRequestProperty("Content-type", "text/html");
			if ((requestdata.length > 0) && (!url.endsWith(".htm"))) {
				outputStream = urlConnection.getOutputStream();
				outputStream.write(requestdata);
				outputStream.flush();
				outputStream.close();
			}

			urlConnection.connect();
			in = urlConnection.getInputStream();

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buf = new byte[2048];
			int len = 0;
			while ((len = in.read(buf)) != -1) {
				baos.write(buf, 0, len);
			}

			respDatas = baos.toByteArray();

			in.close();
		} catch (ConnectException e) {
			throw new SFConnectionException(e);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			try {
				if (outputStream != null)
					outputStream.close();
				if (in != null)
					in.close();
			} catch (IOException localIOException1) {
			}
		}
		return respDatas;
	}

	public static void main(String[] args) throws Exception {
		byte[] get = requestUrlData("http://deveasy.googlecode.com/svn/trunk/desy_dev/sexftp/genupdate.htm",
				new byte[1]);
		System.out.println(new String(get));
	}
}
