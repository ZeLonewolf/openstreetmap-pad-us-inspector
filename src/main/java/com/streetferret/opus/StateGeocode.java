package com.streetferret.opus;

import java.io.IOException;
import java.util.Properties;

public class StateGeocode {

	public static Properties ABBREV_TO_AREA_ID = new Properties();
	public static Properties ABBREV_TO_FILENAME = new Properties();

	static {
		try {
			ABBREV_TO_AREA_ID.load(StateGeocode.class.getResourceAsStream("/state_abbr_geocode.properties"));
			ABBREV_TO_FILENAME.load(StateGeocode.class.getResourceAsStream("/state_abbr_name.properties"));
		} catch (IOException e) {
			System.exit(0);
		}
	}

}
