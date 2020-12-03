package com.streetferret.opus.location;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

public class LocationMatcher {
	private static final File MATCH_FILE = Paths.get("download", "PADUS_fee_overlap.csv").toFile();

	private Map<Long, List<LocationMatch>> matchIndex;

	private static final double MATCH_THRESH = 0.95;
	
	static void load() {
		System.out.println("Loading location match file");
		try (CSVReader csvReader = new CSVReader(new FileReader(MATCH_FILE))) {
			String[] values = null;
			while ((values = csvReader.readNext()) != null) {

				String type = values[3];
				long osmID = Long.valueOf(values[4]);
				
//				records.add(Arrays.asList(values));

			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CsvValidationException e) {
			e.printStackTrace();
		}
	}

}
