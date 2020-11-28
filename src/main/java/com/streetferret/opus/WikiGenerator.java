package com.streetferret.opus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;

public class WikiGenerator {
	public static void generateWiki(String state, SortedMap<String, ProtectedAreaTagging> map) throws IOException {

		StringBuilder rowBuilder = new StringBuilder();

		map.entrySet().forEach(e -> {
			if (isValidIUCN(e)) {
				ProtectedAreaTagging tagging = e.getValue();

				String color = getRowColor(tagging);

				rowBuilder.append("|- bgcolor=\"");
				rowBuilder.append(color);
				rowBuilder.append("\"\n");

				// Name
				rowBuilder.append("|");
				rowBuilder.append(e.getKey());
				rowBuilder.append("\n");

				// IUCN Cat
				rowBuilder.append("|");
				rowBuilder.append(tagging.getIucnClass());
				rowBuilder.append("\n");

				rowBuilder.append("|\n");
				rowBuilder.append("|\n");
			}

		});

		SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");
		
		InputStream inputStream = WikiGenerator.class.getResourceAsStream("/pad.wiki");
		String wikiTemplate = readFromInputStream(inputStream);
		wikiTemplate = wikiTemplate.replace("$STATE", state);
		wikiTemplate = wikiTemplate.replace("$DATE", sdf.format(new Date()));
		wikiTemplate = wikiTemplate.replace("$TABLE", rowBuilder.toString());

		System.out.println(wikiTemplate);
	}

	private static String getRowColor(ProtectedAreaTagging tagging) {
		switch (tagging.getIucnClass()) {
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

	private static boolean isValidIUCN(Entry<String, ProtectedAreaTagging> e) {
		ProtectedAreaTagging tagging = e.getValue();
		return VALID_IUCN.contains(tagging.getIucnClass());
	}

	private static String readFromInputStream(InputStream inputStream) throws IOException {
		StringBuilder resultStringBuilder = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
			String line;
			while ((line = br.readLine()) != null) {
				resultStringBuilder.append(line).append("\n");
			}
		}
		return resultStringBuilder.toString();
	}
}
