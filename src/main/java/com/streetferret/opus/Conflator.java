package com.streetferret.opus;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;

import com.streetferret.opus.osmdb.OSMProtectedAreaRecord;
import com.streetferret.opus.osmdb.StateProtectedAreaDatabase;

public class Conflator {

	public static void conflateByName(SortedMap<String, ProtectedAreaConflation> protectedAreaMap,
			StateProtectedAreaDatabase db) {

		Iterator<OSMProtectedAreaRecord> it = db.getRecords().iterator();

		List<String> conflatedRecordsToRemove = new ArrayList<>();

		while (it.hasNext()) {
			OSMProtectedAreaRecord osmRec = it.next();

			String name = osmRec.getName();

			if (protectedAreaMap.containsKey(name)) {
				osmRec.setConflationNote("exact name");
				List<OSMProtectedAreaRecord> areas = protectedAreaMap.get(name).getOsmAreas();
				boolean alreadyGotIt = areas.stream().map(OSMProtectedAreaRecord::getId)
						.filter(id -> Long.compare(id, osmRec.getId()) == 0).findAny().isPresent();
				if (!alreadyGotIt) {
					areas.add(osmRec);
				}

				conflatedRecordsToRemove.add(name);
			}
		}

		db.removeRecordsNamed(conflatedRecordsToRemove);
	}
}
