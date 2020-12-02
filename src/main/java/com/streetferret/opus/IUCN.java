package com.streetferret.opus;

import java.util.Arrays;
import java.util.List;

public class IUCN {
	public static final List<String> VALID_IUCN = Arrays.asList("Ia", "Ib", "II", "III", "IV", "V", "VI",
			"Not Reported");

	public static boolean hasValidAreas(List<ProtectedAreaTagging> tags) {
		for (ProtectedAreaTagging tag : tags) {
			if (VALID_IUCN.contains(tag.getIucnClass())) {
				return true;
			}
		}
		return false;
	}

	public static void sanitizeInvalidAreas(List<ProtectedAreaTagging> tags) {
		for (ProtectedAreaTagging tag : tags) {
			if (!VALID_IUCN.contains(tag.getIucnClass())) {
				tag.setIucnClass("Other");
			}
		}
	}

}
