package com.streetferret.opus;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.streetferret.opus.location.ConflationCSV;
import com.streetferret.opus.location.LocationDatabase;
import com.streetferret.opus.osmdb.OSMProtectedAreaLookupResults;

public class OPUSInspect {

	public static void main(String... args) throws Exception {

		if ("-parse".equals(args[0])) {
			parse(args[1]);
		}
		if ("-download".equals(args[0])) {
			download();
		}
	}

	private static void download() throws IOException {
		// TODO: hook this up once we have a perma-home for the conflation spreadsheet.
		// This utility currently depends on a spreadsheet generated by Kevin Kenny. The
		// goal is to generate that on an automated basis and pull it down from a known
		// URL.
	}

	private static List<String> SKIP_STATES = Arrays.asList("UNKF");

	private static void parse(String overpassURL) throws Exception {

		RestUtil.OVERPASS_API = overpassURL;

		Set<String> states = ConflationCSV.getStates();

		states.forEach(state -> {

			if (SKIP_STATES.contains(state)) {
				return;
			}

			File stateHTML = Paths.get("state", StateGeocode.ABBREV_TO_FILENAME.getProperty(state) + ".html").toFile();
			if (stateHTML.exists()) {
				return;
			}

			LocationDatabase db = ConflationCSV.load(state);

			try {
				OSMProtectedAreaLookupResults osmDB = OverpassLookup.downloadOSMProtectedAreas(state);
				System.out.println("Downloaded protected areas for " + state);

				osmDB.removeAssociatedAreas(db);

				db.merge(osmDB);

				HTMLGenerator.generateHTML(state, db);
			} catch (Exception e) {
				System.err.println("Failed processing " + state);
				e.printStackTrace();
				System.exit(-1);
			}

		});
	}
}