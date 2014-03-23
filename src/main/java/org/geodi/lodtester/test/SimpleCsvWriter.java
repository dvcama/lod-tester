package org.geodi.lodtester.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

public class SimpleCsvWriter {
	File csvFile;
	File csvFileBackup;

	public SimpleCsvWriter(boolean append) throws FileNotFoundException {
		csvFile = new File("result.csv");
		csvFileBackup = new File("resultBck.csv");
		if (append && csvFile.exists()) {
			try {
				FileUtils.copyFile(csvFile, csvFileBackup);
			} catch (IOException e) {
				e.printStackTrace();
			}
			csvFile.delete();
		}

	}

	public void writeValueQuoted(String content) {
		try {
			FileUtils.writeStringToFile(csvFile, "\"" + content + "\",", true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void writeValueUnquoted(String content) {
		try {
			FileUtils.writeStringToFile(csvFile, content + ",", true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void writeIt(String content) {
		try {
			FileUtils.writeStringToFile(csvFile, content, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void newLine() {
		try {
			FileUtils.writeStringToFile(csvFile, "\r\n", true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Map<String, String> recoverFromBackup() throws IOException {
		Map<String, String> result = new LinkedHashMap<String, String>();

		List<String> lines = FileUtils.readLines(csvFileBackup);
		for (String line : lines) {
			if (!line.contains("unavailable")) {
				String endpoint = line.replaceAll("^\"(http://[^\"]+)\".*", "$1");
				if (!endpoint.equals("")) {
					result.put(endpoint, line);
				}
			}
		}

		return result;
	}
}
