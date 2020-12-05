package com.streetferret.opus.location;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.streetferret.opus.StringUtil;

public class ConflationCSV {
	private static final File MATCH_FILE = Paths.get("download", "PADUS_fee_overlap_with_unmatched.csv").toFile();

	private static Map<String, Integer> loadHeaders(CSVReader csvReader) throws CsvValidationException, IOException {
		Map<String, Integer> col = new HashMap<>();
		String[] headers = csvReader.readNext();
		for (int i = 0; i < headers.length; i++) {
			col.put(headers[i], i);
		}
		return col;
	}

	public static LocationDatabase load(String state) {

		try (CSVReader csvReader = new CSVReader(new FileReader(MATCH_FILE))) {

			Map<String, Integer> col = loadHeaders(csvReader);

			LocationDatabase db = new LocationDatabase();

			String[] v = null;
			while ((v = csvReader.readNext()) != null) {

				if (!state.equals(v[col.get("PADUS_state_nm")])) {
					continue;
				}

				Long padID = Long.valueOf(v[col.get("ogc_fid")]);
				String padName = StringUtil.cleanAreaName(v[col.get("PADUS_unit_nm")]);
				String padOwner = v[col.get("PADUS_loc_mang")];
				String padOperator = v[col.get("OSM_operator")];
				String padIUCN = v[col.get("PADUS_iucn_cat")];
				String padOwnType = v[col.get("PADUS_own_nm")];

				double north = Double.valueOf(v[col.get("north")]);
				double south = Double.valueOf(v[col.get("south")]);
				double east = Double.valueOf(v[col.get("east")]);
				double west = Double.valueOf(v[col.get("west")]);

				PADDetails pad = new PADDetails();
				pad.setOperator(padOperator);
				pad.setOwner(padOwner);
				pad.setIucn(padIUCN);
				pad.setOwnershipType(padOwnType);
				pad.setBounds(north, south, east, west);

				Map<String, String> tags = new HashMap<>();

				loadTag(tags, v, col, "landuse");
				loadTag(tags, v, col, "leisure");
				loadTag(tags, v, col, "boundary");
				loadTag(tags, v, col, "protect_class");

				db.getPadDetails().put(padID, pad);
				db.getNameMap().put(padName, padID);

				String osmName = v[col.get("OSM_name")];

				if (padID == null) {
					System.err.println("fail");
					System.exit(-1);
				}

				if (!osmName.isBlank()) {

					tags.put("name", osmName);

					LocationMatch match = new LocationMatch();
					match.setId(Long.valueOf(v[col.get("OSM_id")]));
					match.setType(v[col.get("OSM_objtype")]);
					match.setTags(tags);

					double pArea = Double.valueOf(v[col.get("PADUS_area")]);
					double osmArea = Double.valueOf(v[col.get("OSM_area")]);
					double overlap = Double.valueOf(v[col.get("Overlap_area")]);

					double osmCoverMatch = overlap / pArea;
					double padCoverMatch = overlap / osmArea;
					double exactMatch = osmCoverMatch * padCoverMatch;

					match.setMatchExact(exactMatch);
					match.setMatchOverlap(padCoverMatch);
					match.setMatchInside(osmCoverMatch);

					db.getMatchMap().put(padID, match);
				}
			}

			return db;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CsvValidationException e) {
			e.printStackTrace();
		}

		return null;
	}

	private static void loadTag(Map<String, String> tags, String[] v, Map<String, Integer> col, String key) {
		String value = v[col.get("OSM_" + key)];
		if (value == null || value.isBlank()) {
			return;
		}
		tags.put(key, value);
	}

	public static Set<String> getStates() throws IOException, CsvValidationException {

		TreeSet<String> states = new TreeSet<>();

		try (CSVReader csvReader = new CSVReader(new FileReader(MATCH_FILE))) {

			Map<String, Integer> col = loadHeaders(csvReader);

			String[] v = null;
			while ((v = csvReader.readNext()) != null) {
				states.add(v[col.get("PADUS_state_nm")]);
			}
		}

		return states;
	}
}
