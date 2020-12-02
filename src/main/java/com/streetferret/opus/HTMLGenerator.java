package com.streetferret.opus;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.streetferret.opus.osmdb.StateProtectedAreaDatabase;

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

	public static void generateHTML(String state, SortedMap<String, ProtectedAreaConflation> protectedAreaMap,
			StateProtectedAreaDatabase db) throws IOException {

		new File("state").mkdirs();

		try (PrintStream mdPrint = new PrintStream("state" + File.separator + state + ".html")) {

			StringBuilder rowBuilder = new StringBuilder();

			protectedAreaMap.entrySet().forEach(e -> {

				ProtectedAreaConflation c = e.getValue();

				Set<String> padClasses = new TreeSet<>();

				for (ProtectedAreaTagging tagging : c.getPadAreas()) {
					padClasses.add(tagging.getIucnClass());
				}

				ProtectedAreaTagging defaultTagging = c.getPadAreas().get(0);

				String padClassStr = padClasses.stream().collect(Collectors.joining(", "));

				String colorStyle = getRowColor(padClasses);
				String name = e.getKey();
				if (name.isEmpty()) {
					name = "(unnamed)";
				}
				String actualUse = "";

				try {
					actualUse = OverpassLookup.overpassProtectedAreaLookupHTML(c.getOsmAreas());
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				if ("Not Found".equals(padClassStr)) {
					rowBuilder.append(T_MISSING_ROW.replace("$NAME", name).replace("$STATE", state)
							.replace("$OSM_OBJECTS", actualUse));

				} else {
					rowBuilder.append(T_PAD_ROW.replace("$NAME", name).replace("$ROW_STYLE", colorStyle)
							.replace("$LON1", String.valueOf(defaultTagging.getMinLon()))
							.replace("$LAT1", String.valueOf(defaultTagging.getMinLat()))
							.replace("$LON2", String.valueOf(defaultTagging.getMaxLon()))
							.replace("$LAT2", String.valueOf(defaultTagging.getMaxLat()))
							.replace("$LAT2", String.valueOf(defaultTagging.getMaxLat()))
							.replace("$OWNER_TYPE", defaultTagging.getOwnership())
							.replace("$OWNER", defaultTagging.getOwner())
							.replace("$OPERATOR", defaultTagging.getOperator())
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

		int stripeSize = 25;

		if (padClasses.size() > 1) {

			int px = 0;
			StringBuilder grad = new StringBuilder("background: repeating-linear-gradient(135deg,");

			List<String> cssFrags = new ArrayList<>();

			for (String aClass : padClasses) {
				String color = HTMLGenerator.iucnToColor(aClass);
				color = color + " " + px + "px," + color + " " + (px + stripeSize) + "px";
				cssFrags.add(color);
				px += stripeSize;
			}

			grad.append(cssFrags.stream().collect(Collectors.joining(",")));
			grad.append(");");

			return grad.toString();

		}

		return "background-color: " + iucnToColor(padClasses.iterator().next());
	}

	private static String iucnToColor(String iucn) {
		switch (iucn) {
		case "Ia":
			return "#fdd";
		case "Ib":
			return "#dfd";
		case "II":
			return "#ddf";
		case "III":
			return "#dff";
		case "IV":
			return "#fdf";
		case "V":
			return "#ffd";
		case "VI":
			return "#ddd";
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
