package org.sexftp.core.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class UrlGet {
	public static void main(String[] args) throws Exception {
		System.out.println(
				doGet("http://localhost:8080/kkfun_data_statistics/dailyReleaseStat/getDailyReleaseStats.action?table=V350_RESULT&monitorTotalReport=true&selfHostView=true",
						"utf-8"));
	}

	public static String doGet(String urlPath, String encode) throws Exception {
		URL url = new URL(urlPath);
		HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

		urlConnection.setDoOutput(true);
		urlConnection.setDoInput(true);
		urlConnection.setUseCaches(false);
		urlConnection.setRequestMethod("GET");
		urlConnection.getOutputStream().flush();
		urlConnection.getOutputStream().close();

		urlConnection.connect();
		InputStream in = urlConnection.getInputStream();

		BufferedReader reader = new BufferedReader(new InputStreamReader(in, encode));

		StringBuffer data = new StringBuffer();
		String lines;
		while ((lines = reader.readLine()) != null) {
			data.append(lines);
		}
		reader.close();

		urlConnection.disconnect();
		return data.toString();
	}
}