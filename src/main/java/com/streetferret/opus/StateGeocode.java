package com.streetferret.opus;

import java.io.IOException;
import java.util.Properties;

public class StateGeocode {

	public static Properties LIST = new Properties();

	static {
		try {
			LIST.load(StateGeocode.class.getResourceAsStream("/state_geocode.txt"));
		} catch (IOException e) {
			System.exit(0);
		}
	}

}
