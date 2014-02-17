package org.geodi.lodtester;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
		Map<String, String> endpoints = new LinkedHashMap<String, String>();
		ArrayList<String> testDomains = new ArrayList<String>();

		// ENDPOINT / GRAPH
		endpoints.put("http://dati.camera.it/sparql", "http://dati.camera.it/ocd/");
		endpoints.put("http://dwrgsweb-lb.rgs.mef.gov.it/DWRGSXL/sparql","");
		endpoints.put("http://lod.xdams.org/sparql","");
		endpoints.put("http://dati.senato.it/sparql","");
		endpoints.put("http://it.dbpedia.org/sparql","");//
		endpoints.put("http://spcdata.digitpa.gov.it:8899/sparql","");
		endpoints.put("http://dati.culturaitalia.it/sparql/","");
		endpoints.put("http://data.cnr.it/sparql-proxy","");
		endpoints.put("http://www.provincia.carboniaiglesias.it/sparql","");
		endpoints.put("http://linkeddata.comune.fi.it:8080/sparql","");
		endpoints.put("http://dati.acs.beniculturali.it/sparql","");
		endpoints.put("http://spcdata.digitpa.gov.it:8899/sparql","");
		endpoints.put("http://linkeddata.comune.fi.it:8080/sparql","");
		
		for (Map.Entry<String, String> endpoint : endpoints.entrySet()) {
			  doTest(endpoint.getKey());
		}

		for (Map.Entry<String, String> endpoint : endpoints.entrySet()) {
			  doStats(endpoint.getKey());
		}
		
//		testDomains.add("http://www.provincia.carboniaiglesias.it");
//		testDomains.add("http://linkeddata.comune.fi.it");
//		testDomains.add("http://spcdata.digitpa.gov.it");
//		testDomains.add("http://dati.camera.it");
//		testDomains.add("http://www.cnr.it");
//
		
		// MORE DOMAINS TO TEST THE CLOUD
		testDomains.add("http://rdf.freebase.com");
		testDomains.add("http://sws.geonames.org");
		testDomains.add("http://linkedgeodata.org");
		testDomains.add("http://dbpedia.org");
		testDomains.add("http://aims.fao.org");
//		


		//verifyCloud(endpoints, testDomains);

	}

	private static void verifyCloud(Map<String, String> endpoints, ArrayList<String> testDomains) throws UnsupportedEncodingException {
		Map<String, String> m = new HashMap<String, String>();
		System.out.println("collecting uris to test...");
		for (Map.Entry<String, String> endpoint : endpoints.entrySet()) {
			try {
				String anURI = GetValues.pickAnUri(endpoint + "?query=" + java.net.URLEncoder.encode(DefaultParamsProvider.pickAnUri, "UTF-8"));
				m.put(endpoint.getKey(), anURI);
			} catch (UnsupportedEncodingException e) {
				System.err.println("endpoint " + endpoint + " unavailable");
			}
		}
		System.out.println("... find connections!");
		for (Map.Entry<String, String> endpoint : endpoints.entrySet()) {
			System.out.println("\n\n---------------\n\nendpoint: " + endpoint.getKey());
			GetValues.findConnections(m, testDomains, endpoint);
		}

	}

	private static void doStats(String endpoint) {
		System.out.println("\n\n--------------------\nendpoint: " + endpoint);

		JsonNode list = DefaultParamsProvider.getStatsQueries();
		for (JsonNode jsonNode : list.findPath("list")) {
			System.out.print(jsonNode.findPath("key").toString());
			try {
				String tot = GetValues.getTot(endpoint + "?query=" + URLEncoder.encode(jsonNode.findPath("value").getValueAsText(), "UTF-8"));
				NumberFormat nf = NumberFormat.getNumberInstance(Locale.ITALIAN);
				System.out.println("\t"+nf.format(Integer.parseInt(tot)));
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

		System.out.print("the endpoint supports JSONP calls:\t\t\t***********\t\t");
		try {
			if (HttpTester.testJsonP(endpoint + "?query=" + java.net.URLEncoder.encode(DefaultParamsProvider.standardQuery, "UTF-8"))) {
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
