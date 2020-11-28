package com.streetferret.opus;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class RestUtil {

	private static final String USER_AGENT = "Mozilla/5.0";
	public static String OVERPASS_API = "";

	public static String queryOverpass(String query) throws IOException {

		long failRetry = 30_000;

		while (failRetry < TimeUnit.MILLISECONDS.convert(2, TimeUnit.HOURS)) {
			String url = OVERPASS_API;
			try {
				return tryPost(url, query);
			} catch (IOException e) {
				if (isRetryable(e)) {
					System.err.println("Overpass: too many, retrying in " + failRetry + " to " + url);
					tryToSleep(failRetry);
					failRetry *= 1.5;
				} else {
					throw e;
				}
			} catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException e) {
				throw new IOException(e);
			}
		}

		throw new IOException("Overpass server is denying our requests");
	}

	public static void tryToSleep(long sleep) {
		try {
			Thread.sleep(sleep);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private static final String[] RETRYABLE_MESSAGES = new String[] { "Server returned HTTP response code: 429 for URL",
			"Server returned HTTP response code: 502 for URL" };

	private static boolean isRetryable(IOException e) {
		if (e.getMessage() == null) {
			return false;
		}
		return Arrays.asList(RETRYABLE_MESSAGES).stream().filter(e.getMessage()::contains).findAny().isPresent();
	}

	// HTTP POST request
	private static String tryPost(String url, String query)
			throws IOException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		URL obj = new URL(url);

		HttpURLConnection http = (HttpURLConnection) obj.openConnection();

		// add request header
		http.setRequestMethod("POST");
		http.setRequestProperty("User-Agent", USER_AGENT);
		http.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

		// Send post request
		http.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(http.getOutputStream());
		wr.writeBytes(query);
		wr.flush();
		wr.close();

		BufferedReader in = new BufferedReader(new InputStreamReader(http.getInputStream()));
		String inputLine;
		StringBuilder builder = new StringBuilder();

		while ((inputLine = in.readLine()) != null) {
			builder.append(inputLine);
			builder.append("\n");
		}
		in.close();

		return builder.toString();
	}
}
