package org.geodi.lodtester.utils;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;

public class SimpleEndpointLocator {
	File database = new File("GeoLite2-Country.mmdb");
	File domains = new File("domainToCountry.csv");

	DatabaseReader reader = null;
	List<String> lines;

	public SimpleEndpointLocator() throws IOException {
		reader = new DatabaseReader.Builder(database).build();
		lines = FileUtils.readLines(domains);
	}

	public String locateEndpoint(String key) {
		String place = "";
		String domain = key.toLowerCase().replaceAll("http://[^/]+(\\.[a-z]+)(/|$).*", "$1");
		for (String line : lines) {
			if (line.indexOf("\"" + domain + "\"") == 0) {
				place = line.replaceAll("^\"" + domain + "\",\"([^\"]+)\"$", "$1");
				break;
			}
		}
		if (place.equals("")) {
			place = locateFromIp(key);
		}
		return place;
	}

	public String locateFromIp(String key) {
		String place = "";
		try {
			key = key.replaceAll("http://([^/:]+).*", "$1");

			CityResponse response = reader.city(InetAddress.getByName(key));
			place = "";
			System.out.print(response.getCountry().getName()); // 'United
			place = response.getCountry().getName();
			if (response.getMostSpecificSubdivision().getName() != null) {
				System.out.println(" / " + response.getMostSpecificSubdivision().getName()); // 'Minnesota'
				place += " / " + response.getMostSpecificSubdivision().getName();
			}
		} catch (Exception e) {
			System.out.println("unable to locate IP - " + e.getMessage());
		}
		return place;
	}

	public static void main(String[] args) {
		try {
			new SimpleEndpointLocator().locateEndpoint("http://www.test.to");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
