package org.geodi.lodtester;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.geodi.lodtester.test.HttpTester;
import org.geodi.lodtester.test.GetValues;

public class SimpleTester {

	public static void main(String[] args) throws UnsupportedEncodingException {
		// TODO
		// rdfs:label, dc:title
		// sameas
		ArrayList<String> endpoints = new ArrayList<String>();
		ArrayList<String> testDomains = new ArrayList<String>();

		endpoints.add("http://dati.camera.it/sparql");
		/*	endpoints.add("http://dwrgsweb-lb.rgs.mef.gov.it/DWRGSXL/sparql");
		endpoints.add("http://lod.xdams.org/sparql");
		endpoints.add("http://www.provincia.carboniaiglesias.it/sparql");
		endpoints.add("http://data.cnr.it/sparql-proxy");
		endpoints.add("http://dati.culturaitalia.it/sparql/");
		endpoints.add("http://dati.senato.it/sparql");
		endpoints.add("http://it.dbpedia.org/sparql");
		endpoints.add("http://dati.acs.beniculturali.it/sparql");*/
		// endpoints.add("http://spcdata.digitpa.gov.it:8899/sparql");
		// endpoints.add("http://linkeddata.comune.fi.it:8080/sparql");

		// endpoints.add("http://opendata.ccd.uniroma2.it/LMF/sparql/select");

		for (String endpoint : endpoints) {
			// doTest(endpoint);
		}

		for (String endpoint : endpoints) {
			// doStats(endpoint);
		}

		testDomains.add("http://rdf.freebase.com");
		testDomains.add("http://sws.geonames.org");
		testDomains.add("http://linkedgeodata.org");
		testDomains.add("http://dbpedia.org");

		verifyCloud(endpoints, testDomains);

	}

	private static void verifyCloud(ArrayList<String> endpoints, ArrayList<String> testDomains) throws UnsupportedEncodingException {
		Map<String, String> m = new HashMap<String, String>();
		System.out.println("collecting uris to test...");
		for (String endpoint : endpoints) {
			try {
				String anURI = GetValues.pickAnUri(endpoint + "?query=" + java.net.URLEncoder.encode(DefaultParamsProvider.pickAnUri, "UTF-8"));
				m.put(endpoint, anURI);
			} catch (UnsupportedEncodingException e) {
				System.err.println("endpoint " + endpoint + " unavailable");
			}
		}

		System.out.println("... find connections!");
		for (String endpoint : endpoints) {
			System.out.println("endpoint: " + endpoint);
			GetValues.findConnections(m, testDomains, endpoint);
		}

	}

	private static void doStats(String endpoint) {
		System.out.println("\n\n--------------------\nendpoint: " + endpoint);

		JsonNode list = DefaultParamsProvider.getStatsQueries();
		for (JsonNode jsonNode : list.findPath("list")) {
			System.out.println("\n" + jsonNode.findPath("key").toString());
			try {
				String tot = GetValues.getTot(endpoint + "?query=" + URLEncoder.encode(jsonNode.findPath("value").getValueAsText(), "UTF-8"));
				NumberFormat nf = NumberFormat.getNumberInstance(Locale.ITALIAN);
				System.out.println(nf.format(Integer.parseInt(tot)));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				System.out.println(0);
			}

		}

	}

	private static void doTest(String endpoint) throws UnsupportedEncodingException {
		System.out.println("\n\n--------------------\nendpoint: " + endpoint);
		String anURI = GetValues.pickAnUri(endpoint + "?query=" + java.net.URLEncoder.encode(DefaultParamsProvider.pickAnUri, "UTF-8"));
		System.out.println("test URI " + anURI + "\n\n");

		System.out.print("the endpoint hosts a page for humans:\t\t\t***********\t\t");
		try {
			if (HttpTester.testAvailability(endpoint, true)) {
				System.out.println("PASS");
			} else {
				System.out.println("FAIL");
			}
		} catch (Exception e) {
			System.out.println("FAIL");
		}

		System.out.print("the endpoint supports SPARQL content negotiation:\t***********\t\t");
		try {
			if (HttpTester.testSpecificContent(endpoint + "?query=" + java.net.URLEncoder.encode(DefaultParamsProvider.standardQuery, "UTF-8"), "application/sparql-results+json")) {
				System.out.println("PASS");
			} else {
				System.out.println("FAIL");
			}
		} catch (Exception e) {
			System.out.println("FAIL");
		}
		System.out.print("the endpoint use port 80:\t\t\t\t***********\t\t");
		if (endpoint.equals(endpoint.replaceAll(":[0-9]+/", "/"))) {
			System.out.println("PASS");
		} else {
			System.out.println("FAIL");
		}

		System.out.print("the endpoint URL is easy to deduce from resources:\t***********\t\t");
		if (endpoint.equals(endpoint.replaceAll("(http://[^/]+/).*", "$1sparql"))) {
			System.out.println("PASS");
		} else {
			System.out.println("FAIL");
		}
		System.out.print("the URI is online:\t\t\t\t\t***********\t\t");
		boolean isOnline = HttpTester.testAvailability(anURI, false);
		try {
			if (isOnline) {
				System.out.println("PASS");
			} else {
				System.out.println("FAIL");
			}
		} catch (Exception e) {
			System.out.println("FAIL");
		}

		System.out.print("the URI supports content negotiation rdf+xml:\t\t***********\t\t");
		if (isOnline) {
			try {
				if (HttpTester.testSpecificContent(anURI, "application/rdf+xml")) {
					System.out.println("PASS");
				} else {
					System.out.println("FAIL");
				}
			} catch (Exception e) {
				System.out.println("FAIL");
			}
		} else {
			System.out.println("NC");
		}

		System.out.print("the resources and the endpoint are on the same domain:\t***********\t\t");
		if (anURI.startsWith(endpoint.replaceAll("(http://[^/]+/).*", "$1"))) {
			System.out.println("PASS");
		} else {
			System.out.println("FAIL");
		}

	}

}
