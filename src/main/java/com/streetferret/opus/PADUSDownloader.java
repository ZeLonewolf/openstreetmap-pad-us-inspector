package com.streetferret.opus;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PADUSDownloader {

	private static final String PADUS_URL = "https://www.usgs.gov/core-science-systems/science-analytics-and-synthesis/gap/science/pad-us-data-download?qt-science_center_objects=0";

	public static void downloadPADStates() throws IOException {

		String padusIndex = "";
		try (Scanner padusScanner = new Scanner(new URL(PADUS_URL).openStream(), "UTF-8")) {
			padusIndex = padusScanner.useDelimiter("\\A").next();
		}

		Paths.get("download", "kmz").toFile().mkdirs();
		Paths.get("download", "kml").toFile().mkdirs();

		Pattern p = Pattern.compile("<a href=\"(.*?)\".*?>([A-Za-z\\s]*?)<\\/a>");
		Matcher m = p.matcher(padusIndex);

		while (m.find()) {
			String dlURL = m.group(1);
			String stateString = m.group(2);

			if (!stateString.contains("KMZ")) {
				continue;
			}

			String state = stateString.replace("KMZ", "").trim();

			System.out.println(dlURL);
			System.out.print("Downloading KMZ for " + state + ".");

			File stateFileZ = Paths.get("download", "kmz", state + ".kmz").toFile();
			File stateFile = Paths.get("download", "kml", state + ".kml").toFile();

			if (!stateFileZ.exists()) {
				InputStream in = new URL(dlURL).openStream();
				Files.copy(in, stateFileZ.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}

			System.out.print("..");

			if (!stateFile.exists()) {
				ZipUtil.unzipKMLLayer(stateFileZ, stateFile);
			}

			System.out.println("done!");
		}

//		System.out.println(padusIndex);
	}

}
