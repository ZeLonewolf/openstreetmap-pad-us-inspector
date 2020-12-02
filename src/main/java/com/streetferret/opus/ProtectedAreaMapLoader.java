package com.streetferret.opus;

import java.util.SortedMap;

public class ProtectedAreaMapLoader {

	public static void storeTagging(SortedMap<String, ProtectedAreaConflation> protectedAreaMap, String name,
			ProtectedAreaTagging tagging) {
		if (protectedAreaMap.containsKey(name)) {
			protectedAreaMap.get(name).getPadAreas().add(tagging);
		} else {
			ProtectedAreaConflation c = new ProtectedAreaConflation();
			c.getPadAreas().add(tagging);
			protectedAreaMap.put(name, c);
		}

	}

}
