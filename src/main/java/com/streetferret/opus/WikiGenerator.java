package com.streetferret.opus;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class WikiGenerator {
	public static void generateWiki(String state, SortedMap<String, List<ProtectedAreaTagging>> mapList)
			throws IOException {

		try (PrintStream wikiPrint = new PrintStream("output" + File.separator + state + ".wiki")) {

			StringBuilder rowBuilder = new StringBuilder();

			mapList.entrySet().forEach(e -> {
				if (isValidIUCN(e)) {

					Set<String> padClasses = new TreeSet<>();

					for (ProtectedAreaTagging tagging : e.getValue()) {
						if (VALID_IUCN.contains(tagging.getIucnClass())) {
							padClasses.add(tagging.getIucnClass());
						}
					}

					String padClassStr = padClasses.stream().collect(Collectors.joining(", "));

					String color = getRowColor(padClasses);
					String name = e.getKey();
					String actualUse = "";

					try {
						actualUse = OverpassLookup.overpassProtectedAreaLookup(name);
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

					rowBuilder.append("|- bgcolor=\"");
					rowBuilder.append(color);
					rowBuilder.append("\"\n");

					// Name
					rowBuilder.append("|");
					rowBuilder.append(name);
					rowBuilder.append("\n");

					// IUCN Cat
					rowBuilder.append("|");
					rowBuilder.append(padClassStr);
					rowBuilder.append("\n");

					rowBuilder.append("|");
					rowBuilder.append(actualUse);
					rowBuilder.append("\n");

					System.out.println("Generated wiki for " + name);
				}

			});

			SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");

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

	private static final List<String> VALID_IUCN = Arrays.asList("Ia", "Ib", "II", "III", "IV", "V", "VI");

	private static boolean isValidIUCN(Entry<String, List<ProtectedAreaTagging>> e) {

		for (ProtectedAreaTagging tagging : e.getValue()) {
			if (VALID_IUCN.contains(tagging.getIucnClass()))
				return true;
		}
		return false;
	}
}
