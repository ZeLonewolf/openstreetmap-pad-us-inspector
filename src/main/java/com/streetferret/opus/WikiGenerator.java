package com.streetferret.opus;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class WikiGenerator {

	private static SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");

	public static void generateWiki(String state, SortedMap<String, List<ProtectedAreaTagging>> mapList)
			throws IOException {

		try (PrintStream wikiPrint = new PrintStream("output" + File.separator + state + ".wiki")) {

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
				if(name.isEmpty()) {
					name = "(unnamed)";
				}
				String actualUse = "";

				try {
					actualUse = OverpassLookup.overpassProtectedAreaLookup(name);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				if ("Not Found".equals(padClassStr)) {
					rowBuilder.append("{{PADUS_problem_row|");
					rowBuilder.append(name);
					rowBuilder.append("|");
					rowBuilder.append(state);
					rowBuilder.append("|");
					rowBuilder.append(actualUse);
					rowBuilder.append("}}\n");
				} else {
					rowBuilder.append("{{PADUS_row|");
					rowBuilder.append(name);
					rowBuilder.append("|");
					rowBuilder.append(color);

					// Ownership type
					rowBuilder.append("|");
					rowBuilder.append(e.getValue().get(0).getOwnership());// TODO

					// Access
					rowBuilder.append("|");
					rowBuilder.append(e.getValue().get(0).getAccess());// TODO

					// IUCN Cat
					rowBuilder.append("|");
					rowBuilder.append(padClassStr);

					rowBuilder.append("|");
					rowBuilder.append(defaultTagging.getMinLon());
					rowBuilder.append("|");
					rowBuilder.append(defaultTagging.getMinLat());
					rowBuilder.append("|");
					rowBuilder.append(defaultTagging.getMaxLon());
					rowBuilder.append("|");
					rowBuilder.append(defaultTagging.getMaxLat());

					rowBuilder.append("|");
					rowBuilder.append(actualUse);
					rowBuilder.append("}}\n");
				}

				System.out.println("Generated wiki for " + name);
			});

			InputStream inputStream = WikiGenerator.class.getResourceAsStream("/pad.wiki");
			String wikiTemplate = StringUtil.readFromInputStream(inputStream);
			wikiTemplate = wikiTemplate.replace("$STATE", state);
			wikiTemplate = wikiTemplate.replace("$DATE", sdf.format(new Date()));
			wikiTemplate = wikiTemplate.replace("$TABLE", rowBuilder.toString());

			wikiPrint.println(wikiTemplate);
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

	public static void generateSummaryWiki(SortedMap<String, Integer> map) throws IOException {
		int total = map.values().stream().collect(Collectors.summingInt(Integer::intValue));
		StringBuilder row = new StringBuilder();

		row.append("|-\n");

		map.entrySet().forEach(e -> {
			row.append("|[[United_States/Public_lands/PAD-US/");
			row.append(e.getKey());
			row.append("]]\n");

			row.append("|");
			row.append(e.getValue());
			row.append("\n");
		});

		try (PrintStream wikiPrint = new PrintStream("output" + File.separator + "_summary.wiki")) {

			InputStream inputStream = WikiGenerator.class.getResourceAsStream("/summary.wiki");
			String wikiTemplate = StringUtil.readFromInputStream(inputStream);
			wikiTemplate = wikiTemplate.replace("$TOTAL", String.valueOf(total));
			wikiTemplate = wikiTemplate.replace("$DATE", sdf.format(new Date()));
			wikiTemplate = wikiTemplate.replace("$TABLE", row.toString());

			wikiPrint.println(wikiTemplate);
		}
	}
}
