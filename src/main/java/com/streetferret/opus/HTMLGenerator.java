package com.streetferret.opus;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.streetferret.opus.location.LocationDatabase;
import com.streetferret.opus.location.LocationMatch;
import com.streetferret.opus.location.PADDetails;

public class HTMLGenerator {

	private static SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");

	private static String T_STATE_PAGE;
	private static String T_MISSING_ROW;
	private static String T_PAD_ROW;
	static String T_REL_ITEM;
	static String T_WAY_ITEM;
	static String T_NODE_ITEM;

	private static Comparator<LocationMatch> sameIDCompare = Comparator.comparing(LocationMatch::getId);

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
		return StringUtil
			.readFromInputStream(HTMLGenerator.class.getResourceAsStream("/html-template" + File.separator + fragment));
	}

	public static void generateHTML(String state, LocationDatabase db) throws IOException {

		new File("state").mkdirs();

		File stateHTML = Paths.get("state", StateGeocode.ABBREV_TO_FILENAME.getProperty(state) + ".html").toFile();
		
		try (PrintStream mdPrint = new PrintStream(stateHTML)) {

			StringBuilder rowBuilder = new StringBuilder();

			db.getNameMap()
				.asMap()
				.entrySet()
				.stream()
				.sorted((e1, e2) -> StringUtils.compare(e1.getKey(), (e2.getKey())))
				.forEach(row -> {

					String name = row.getKey();
					Collection<Long> padIDs = row.getValue();

					List<PADDetails> padDetails = padIDs.stream()
						.map(id -> db.getPadDetails().get(id))
						.filter(Objects::nonNull)
						.collect(Collectors.toList());

					if (padDetails.isEmpty()) {
						// OSM unmatched

						String actualUse = HTMLItemGenerator.matchingItemHTML(name, db.getOsmUnmatched().get(name));

						rowBuilder.append(T_MISSING_ROW.replace("$NAME", name)
							.replace("$STATE", state)
							.replace("$OSM_OBJECTS", actualUse));

					} else {
						Set<LocationMatch> osmMatches = padIDs.stream()
							.flatMap(id -> db.getMatchMap().get(id).stream())
							.collect(Collectors.toCollection(() -> new TreeSet<LocationMatch>(sameIDCompare)));

						String actualUse = HTMLItemGenerator.matchingItemHTML(name, osmMatches);

						rowBuilder.append(printMatchingRow(padDetails, db, name).replace("$OSM_OBJECTS", actualUse));
					}
				});

			String page = T_STATE_PAGE.replace("$STATE", state)
				.replace("$DATE", sdf.format(new Date()))
				.replace("$TABLE", rowBuilder.toString());

			mdPrint.println(page);
		}
		System.out.println("Generated html for " + state);
	}

	private static String printMatchingRow(List<PADDetails> padDetails, LocationDatabase db, String name) {

		Set<String> padClasses = padDetails.stream()
			.map(PADDetails::getIucn)
			.map(s -> s.replace("Other Conservation Area", "Other"))
			.map(s -> s.replace("Unassigned", "Other"))
			.sorted()
			.collect(Collectors.toSet());

		Set<String> padOwners = padDetails.stream().map(PADDetails::getOwner).sorted().collect(Collectors.toSet());

		Set<String> padOwnerTypes = padDetails.stream()
			.map(PADDetails::getOwnershipType)
			.filter(Objects::nonNull)
			.filter(s -> !s.isBlank())
			.sorted()
			.collect(Collectors.toSet());

		Set<String> padOperators = padDetails.stream()
			.map(PADDetails::getOperator)
			.filter(Objects::nonNull)
			.filter(s -> !s.isBlank())
			.sorted()
			.collect(Collectors.toSet());

		double n = padDetails.stream().mapToDouble(PADDetails::getNorth).max().getAsDouble();
		double e = padDetails.stream().mapToDouble(PADDetails::getEast).max().getAsDouble();
		double s = padDetails.stream().mapToDouble(PADDetails::getSouth).min().getAsDouble();
		double w = padDetails.stream().mapToDouble(PADDetails::getWest).min().getAsDouble();

		boolean hasIUCN = padClasses.stream().filter(IUCN.VALID_IUCN::contains).findFirst().isPresent();
		if (!hasIUCN) {
			return "";
		}

		String padClassStr = commaSeparated(padClasses);

		String colorStyle = getRowColor(padClasses);

		if (name.isEmpty()) {
			name = "(unnamed)";
		}

		String owner = bulletSeparated(padOwners);
		String operator = bulletSeparated(padOperators);
		String ownerType = commaSeparated(padOwnerTypes);

		return new StringBuilder()
			.append(T_PAD_ROW.replace("$NAME", name)
				.replace("$ROW_STYLE", colorStyle)
				.replace("$LON1", String.valueOf(w))
				.replace("$LAT1", String.valueOf(s))
				.replace("$LON2", String.valueOf(e))
				.replace("$LAT2", String.valueOf(n))
				.replace("$OWNER_TYPE", ownerType)
				.replace("$OWNER_NAME", owner)
				.replace("$OPERATOR", operator)
				.replace("$IUCN", String.valueOf(padClassStr)))
			.toString();
	}

	private static String getRowColor(Set<String> padClasses) {

		int stripeSize = 25;

		if (padClasses.size() > 1) {

			int px = 0;
			StringBuilder grad = new StringBuilder("style=\"background: repeating-linear-gradient(135deg,");

			List<String> cssFrags = new ArrayList<>();

			for (String aClass : padClasses) {
				String color = HTMLGenerator.iucnToColor(aClass);
				color = color + " " + px + "px," + color + " " + (px + stripeSize) + "px";
				cssFrags.add(color);
				px += stripeSize;
			}

			grad.append(cssFrags.stream().collect(Collectors.joining(",")));
			grad.append(");\"");

			return grad.toString();

		}

		return "style=\"background-color: " + iucnToColor(padClasses.iterator().next()) + "\"";
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

	private static String commaSeparated(Collection<String> c) {
		return c.stream().collect(Collectors.joining(", "));
	}

	private static String bulletSeparated(Collection<String> c) {
		if (c.isEmpty()) {
			return "";
		}
		if (c.size() == 1) {
			return c.iterator().next();
		}
		return "<ul><li>" + c.stream().collect(Collectors.joining("</li><li>")) + "</li></ul>";
	}
}
