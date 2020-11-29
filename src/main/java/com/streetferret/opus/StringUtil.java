package com.streetferret.opus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StringUtil {
	public static String readFromInputStream(InputStream inputStream) throws IOException {
		StringBuilder resultStringBuilder = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
			String line;
			while ((line = br.readLine()) != null) {
				resultStringBuilder.append(line).append("\n");
			}
		}
		return resultStringBuilder.toString();
	}

	public static String cleanAreaName(String name) {
		return name.replace("_", " ").replace("\"", "").replace("*", "").replace("–", "-")
				.replaceAll("([A-Za-z])\\s*,\\s*([A-Za-z])", "$1, $2");
	}
}
