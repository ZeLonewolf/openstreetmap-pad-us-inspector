package com.streetferret.opus;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

public class ProtectedAreaMapLoader {

	public static void storeTagging(SortedMap<String, List<ProtectedAreaTagging>> protectedAreaMap, String name,
			ProtectedAreaTagging tagging) {
		if (protectedAreaMap.containsKey(name)) {
			protectedAreaMap.get(name).add(tagging);
		} else {
			List<ProtectedAreaTagging> tagList = new ArrayList<>();
			tagList.add(tagging);
			protectedAreaMap.put(name, tagList);
		}

	}

}
