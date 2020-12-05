package com.streetferret.opus.location;

import java.util.Comparator;

public class LocationMatchSorter implements Comparator<LocationMatch> {

	@Override
	public int compare(LocationMatch m1, LocationMatch m2) {
		return Double.compare(m1.getMatchExact() + m1.getMatchOverlap() + m1.getMatchInside(),
				m2.getMatchExact() + m2.getMatchOverlap() + m2.getMatchInside());
	}

}
