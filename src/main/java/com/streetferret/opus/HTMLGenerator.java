package com.streetferret.opus;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class HTMLGenerator {

	private static SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");

	private static String T_STATE_PAGE;
	private static String T_MISSING_ROW;
	private static String T_PAD_ROW;
	static String T_REL_ITEM;
	static String T_WAY_ITEM;
	static String T_NODE_ITEM;

	static {
		try {
			T_STATE_PAGE = loadFragment("state-page.html");
			T_MISSING_ROW = loadFragment("missing-row.html");
			T_PAD_ROW = loadFragment("pad-row.html");
			T_REL_ITEM = loadFragment("relation-item.html");
			T_WAY_ITEM = loadFragment("way-item.html");
			T_NODE_ITEM = loadFragment("node-item.html");
		} catch (IOException e) {
			System.exit(-1);// Fatal error
		}
	}

	private static String loadFragment(String fragment) throws IOException {
		return StringUtil.readFromInputStream(
				HTMLGenerator.class.getResourceAsStream("/html-template" + File.separator + fragment));
	}

	public static void generateHTML(String state, SortedMap<String, List<ProtectedAreaTagging>> mapList)
			throws IOException {

		try (PrintStream mdPrint = new PrintStream("state" + File.separator + state + ".html")) {

			StringBuilder rowBuilder = new StringBuilder();

			mapList.entrySet().forEach(e -> {

				Set<String> padClasses = new TreeSet<>();

				for (ProtectedAreaTagging tagging : e.getValue()) {
					padClasses.add(tagging.getIucnClass());
				}

				ProtectedAreaTagging defaultTagging = e.getValue().get(0);

				String padClassStr = padClasses.stream().collect(Collectors.joining(", "));

				String color = getRowColor(padClasses);
				String name = e.getKey();
				if (name.isEmpty()) {
					name = "(unnamed)";
				}
				String actualUse = "";

				try {
					actualUse = OverpassLookup.overpassProtectedAreaLookupHTML(name, state);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				if ("Not Found".equals(padClassStr)) {
					rowBuilder.append(T_MISSING_ROW.replace("$NAME", name).replace("$STATE", name)
							.replace("$OSM_OBJECTS", actualUse));

				} else {
					rowBuilder.append(T_PAD_ROW.replace("$NAME", name).replace("$COLOR", color)
							.replace("$LON1", String.valueOf(defaultTagging.getMinLon()))
							.replace("$LAT1", String.valueOf(defaultTagging.getMinLat()))
							.replace("$LON2", String.valueOf(defaultTagging.getMaxLon()))
							.replace("$LAT2", String.valueOf(defaultTagging.getMaxLat()))
							.replace("$LAT2", String.valueOf(defaultTagging.getMaxLat()))
							.replace("$OWNER_TYPE", defaultTagging.getOwnership())
							.replace("$ACCESS", defaultTagging.getAccess())
							.replace("$IUCN", String.valueOf(padClassStr)).replace("$OSM_OBJECTS", actualUse));
				}

				System.out.println("Generated html for " + name);
			});

			String page = T_STATE_PAGE.replace("$STATE", state).replace("$DATE", sdf.format(new Date()))
					.replace("$TABLE", rowBuilder.toString());

			mdPrint.println(page);
		}
	}

	private static String getRowColor(Set<String> padClasses) {

		if (padClasses.size() > 1) {
			return "#fff";
		}

		switch (padClasses.iterator().next()) {
		case "Ia":
			return "#fcc";
		case "Ib":
			return "#cfc";
		case "II":
			return "#ccf";
		case "III":
			return "#cff";
		case "IV":
			return "#fcf";
		case "V":
			return "#ffc";
		case "VI":
			return "#ccc";
		}
		return "#fff";
	}

//	public static void generateSummaryWiki(SortedMap<String, Integer> map) throws IOException {
//		int total = map.values().stream().collect(Collectors.summingInt(Integer::intValue));
//		StringBuilder row = new StringBuilder();
//
//		row.append("|-\n");
//
//		map.entrySet().forEach(e -> {
//			row.append("|[[United_States/Public_lands/PAD-US/");
//			row.append(e.getKey());
//			row.append("]]\n");
//
//			row.append("|");
//			row.append(e.getValue());
//			row.append("\n");
//		});
//
//		try (PrintStream wikiPrint = new PrintStream("output" + File.separator + "_summary.wiki")) {
//
//			InputStream inputStream = MDGenerator.class.getResourceAsStream("/summary.wiki");
//			String wikiTemplate = StringUtil.readFromInputStream(inputStream);
//			wikiTemplate = wikiTemplate.replace("$TOTAL", String.valueOf(total));
//			wikiTemplate = wikiTemplate.replace("$DATE", sdf.format(new Date()));
//			wikiTemplate = wikiTemplate.replace("$TABLE", row.toString());
//
//			wikiPrint.println(wikiTemplate);
//		}
//	}
}