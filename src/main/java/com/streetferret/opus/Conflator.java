package com.streetferret.opus;

import java.util.Iterator;
import java.util.SortedMap;

import com.streetferret.opus.osmdb.OSMProtectedAreaRecord;
import com.streetferret.opus.osmdb.StateProtectedAreaDatabase;

public class Conflator {

	public static void conflateByName(SortedMap<String, ProtectedAreaConflation> protectedAreaMap,
			StateProtectedAreaDatabase db) {

		Iterator<OSMProtectedAreaRecord> it = db.getRecords().iterator();

		while(it.hasNext()) {
			OSMProtectedAreaRecord osmRec = it.next();

			if(protectedAreaMap.containsKey(osmRec.getName())) {
				protectedAreaMap.get(osmRec.getName()).getOsmAreas().add(osmRec);
				it.remove();
			}
		}
		
	}

}
