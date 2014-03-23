package org.geodi.lodtester;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.geodi.lodtester.test.GetValues;
import org.geodi.lodtester.test.HttpTester;
import org.geodi.lodtester.utils.SimpleCsvWriter;
import org.geodi.lodtester.utils.SimpleEndpointLocator;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.model.CityResponse;

public class SimpleTester {
	SimpleCsvWriter csv;
	SimpleEndpointLocator locator;

	public static void main(String[] args) throws HttpException, IOException {
		new SimpleTester().start(true);
	}

	private void start(boolean justErrors) throws HttpException, IOException {

		csv = new SimpleCsvWriter(true);
		locator = new SimpleEndpointLocator();

		csv.writeIt("\"endpoint URI\",\"place\",\"enpoint is online\",\"test URI\",\"the endpoint hosts a page for humans\",\"the endpoint supports SPARQL content negotiation\",\"the endpoint supports JSONP calls\",\"the endpoint use port 80\",\"the endpoint URL is easy to deduce from resources\",\"the URI is online\",\"the URI supports content negotiation rdf+xml\",\"the resources and the endpoint are on the same domain\",\"total number of triples\",\"total number of entities\",\"total number of blankNodes\",\"total number of distinct classes\",\"total number of distinct predicates\",\"total number of entities described by dc:title\",\"total number of entities described by rdfs:label\",\"total number of entities described by dc:date\"");
		csv.newLine();

		Map<String, String> endpoints = new LinkedHashMap<String, String>();
		Set<String> onlineEndpoints = new TreeSet<String>();
		ArrayList<String> testDomains = new ArrayList<String>();
		Set<String> skipEndpoints = new HashSet<String>();
		if (justErrors) {
			skipEndpoints = restoreFromBackup();
		}
		endpoints = populatheFromDataHub();

		// ENDPOINT / GRAPH
		endpoints.put("http://linkedstat.spaziodati.eu/sparql", "");
		endpoints.put("http://dati.camera.it/sparql", "");
		endpoints.put("http://dwrgsweb-lb.rgs.mef.gov.it/DWRGSXL/sparql", "");
		endpoints.put("http://lod.xdams.org/sparql", "");
		endpoints.put("http://dati.senato.it/sparql", "");
		endpoints.put("http://it.dbpedia.org/sparql", "");//
		endpoints.put("http://spcdata.digitpa.gov.it:8899/sparql", "");
		endpoints.put("http://dati.culturaitalia.it/sparql/", "");
		endpoints.put("http://www.provincia.carboniaiglesias.it/sparql", "");
		endpoints.put("http://linkeddata.comune.fi.it:8080/sparql", "");
		endpoints.put("http://dati.acs.beniculturali.it/sparql", "");

		int tot = endpoints.size();
		int count = 1;
		for (Map.Entry<String, String> endpoint : endpoints.entrySet()) {
			System.out.print("\r\n\r\n -- " + count + "/" + tot + " ");
			if (!endpoint.getKey().equals("") && !skipEndpoints.contains(endpoint.getKey())) {
				if (doTest(endpoint.getKey())) {
					onlineEndpoints.add(endpoint.getKey());
					doStats(endpoint.getKey());
				}
				csv.newLine();
			}
			count++;
		}
		System.out.println("***************************\r\n\r\n***************************\r\n\r\n online " + (onlineEndpoints.size() - 1) + " on " + tot + "\r\n\r\n***************************\r\n\r\n***************************\r\n\r\n");
		// MORE DOMAINS TO TEST THE CLOUD
		testDomains.add("http://rdf.freebase.com");
		testDomains.add("http://sws.geonames.org");
		// testDomains.add("http://linkedgeodata.org");
		// testDomains.add("http://dbpedia.org");
		// testDomains.add("http://aims.fao.org");
		//

		verifyCloud(endpoints, testDomains);
	}

	private Set<String> restoreFromBackup() {
		Map<String, String> endpointLines = new HashMap<String, String>();
		try {
			endpointLines = csv.recoverFromBackup();
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (String line : endpointLines.values()) {
			csv.writeIt(line + "\r\n");
			System.out.println("fromBackup: " + line);
		}
		return endpointLines.keySet();
	}

	@SuppressWarnings("unchecked")
	private static Map<String, String> populatheFromDataHub() throws HttpException, IOException {

		Map<String, String> endpoints = new HashMap<String, String>();
		HttpClient client = new HttpClient();
		GetMethod method = new GetMethod("http://datahub.io/api/2/search/resource?format=api/sparql&all_fields=1&limit=1000");
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
		try {
			client.executeMethod(method);
			StringWriter writer = new StringWriter();
			IOUtils.copy(method.getResponseBodyAsStream(), writer, "UTF-8");
			String responseString = writer.toString();

			HashMap<String, Object> a = new ObjectMapper().readValue(responseString, HashMap.class);
			ArrayList<HashMap<String, Object>> ab = (ArrayList<HashMap<String, Object>>) a.get("results");
			for (HashMap<String, Object> hashMap : ab) {
				endpoints.put((String) hashMap.get("url"), "");
			}
		} finally {
			// Release the connection.
			method.releaseConnection();
		}

		return endpoints;
	}

	private static void verifyCloud(Map<String, String> endpoints, ArrayList<String> testDomains) {
		Map<String, String> m = new HashMap<String, String>();
		System.out.println("collecting uris to test...");
		for (Map.Entry<String, String> endpoint : endpoints.entrySet()) {
			try {
				String anURI = GetValues.pickAnUri(endpoint + "?query=");
				m.put(endpoint.getKey(), anURI);
			} catch (Exception e) {
				System.err.println("endpoint " + endpoint + " unavailable? " + e.getMessage());
			}
		}
		System.out.println("... find connections!");
		for (Map.Entry<String, String> endpoint : endpoints.entrySet()) {
			System.out.println("\r\n\r\n---------------\r\n\r\nendpoint: " + endpoint.getKey());
			GetValues.findConnections(m, testDomains, endpoint);
		}

	}

	private void doStats(String endpoint) {
		System.out.println("\r\n\r\n***** endpoint: " + endpoint);

		JsonNode list = DefaultParamsProvider.getStatsQueries();
		for (JsonNode jsonNode : list.findPath("list")) {
			System.out.print(jsonNode.findPath("key").toString());
			try {
				String tot = GetValues.getTot(endpoint + "?query=" + URLEncoder.encode(jsonNode.findPath("value").getValueAsText(), "UTF-8"));
				NumberFormat nf = NumberFormat.getNumberInstance(Locale.ITALIAN);
				String result = nf.format(Integer.parseInt(tot));
				System.out.println("\t" + result);
				if (Integer.parseInt(tot) > -1) {
					csv.writeValueUnquoted(Integer.parseInt(tot) + "");
				} else {
					csv.writeValueQuoted("unavailable");
				}

			} catch (Exception e) {
				System.out.println(0);
				csv.writeValueQuoted("unavailable");
			}

		}

	}

	private boolean doTest(String endpoint) {
		System.out.println("--------------------\r\nendpoint: " + endpoint);
		csv.writeValueQuoted(endpoint);
		System.out.print("probably located in ");
		csv.writeValueQuoted(locator.locateEndpoint(endpoint));
		System.out.print("\r\n\r\nthe endpoint is online:\t\t\t\t\t***********\t\t");
		boolean isOnline = false;
		try {
			isOnline = HttpTester.testAvailability(endpoint, false, true);
			if (isOnline) {
				System.out.println("PASS");
				csv.writeValueUnquoted("true");
			} else {
				System.out.println("FAIL");
				csv.writeValueUnquoted("false");
			}
		} catch (Exception e) {
			System.out.println("FAIL - " + e.getMessage());
			csv.writeValueUnquoted("false");
		}
		if (isOnline) {

			String anURI = "";
			try {
				anURI = GetValues.pickAnUri(endpoint + "?query=");
				System.out.println("\r\ntest URI " + anURI + "\r\n");
				csv.writeValueQuoted(anURI);
			} catch (Exception e) {
				System.out.println("\r\nunable to pick an URI - " + e.getMessage() + "\r\n");
				csv.writeValueQuoted("not available");
			}
			System.out.print("the endpoint hosts a page for humans:\t\t\t***********\t\t");
			try {
				if (HttpTester.testAvailability(endpoint, false, false)) {
					System.out.println("PASS");
					csv.writeValueUnquoted("true");
				} else {
					System.out.println("FAIL");
					csv.writeValueUnquoted("false");
				}
			} catch (Exception e) {
				System.out.println("FAIL - " + e.getMessage());
				csv.writeValueUnquoted("false");
			}

			System.out.print("the endpoint supports SPARQL content negotiation:\t***********\t\t");
			try {
				if (HttpTester.testSpecificContent(endpoint + "?query=" + java.net.URLEncoder.encode(DefaultParamsProvider.standardQuery, "UTF-8"), "application/sparql-results+json")) {
					System.out.println("PASS");
					csv.writeValueUnquoted("true");
				} else {
					System.out.println("FAIL");
					csv.writeValueUnquoted("false");
				}
			} catch (Exception e) {
				System.out.println("FAIL - " + e.getMessage());
				csv.writeValueUnquoted("false");
			}

			System.out.print("the endpoint supports JSONP calls:\t\t\t***********\t\t");
			try {
				if (HttpTester.testJsonP(endpoint + "?query=" + java.net.URLEncoder.encode(DefaultParamsProvider.standardQuery, "UTF-8"))) {
					System.out.println("PASS");
					csv.writeValueUnquoted("true");
				} else {
					System.out.println("FAIL");
					csv.writeValueUnquoted("false");
				}
			} catch (Exception e) {
				System.out.println("FAIL - " + e.getMessage());
				csv.writeValueUnquoted("false");
			}

			System.out.print("the endpoint use port 80:\t\t\t\t***********\t\t");
			if (endpoint.equals(endpoint.replaceAll(":[0-9]+/", "/"))) {
				System.out.println("PASS");
				csv.writeValueUnquoted("true");
			} else {
				System.out.println("FAIL");
				csv.writeValueUnquoted("false");
			}

			System.out.print("the endpoint URL is easy to deduce from resources:\t***********\t\t");
			try {
				if (endpoint.equals(endpoint.replaceAll("(http://[^/]+/).*", "$1sparql"))) {
					System.out.println("PASS");
					csv.writeValueUnquoted("true");
				} else {
					System.out.println("FAIL");
					csv.writeValueUnquoted("false");
				}
			} catch (Exception e) {
				System.out.println("FAIL - " + e.getMessage());
				csv.writeValueUnquoted("false");
			}

			System.out.print("the URI is online:\t\t\t\t\t***********\t\t");
			isOnline = false;
			try {
				isOnline = HttpTester.testAvailability(anURI, false, false);
				if (isOnline) {
					System.out.println("PASS");
					csv.writeValueUnquoted("true");
				} else {
					System.out.println("FAIL");
					csv.writeValueUnquoted("false");
				}
			} catch (Exception e) {
				System.out.println("FAIL - " + e.getMessage());
				csv.writeValueUnquoted("false");
			}

			System.out.print("the URI supports content negotiation rdf+xml:\t\t***********\t\t");
			if (isOnline) {
				try {
					if (HttpTester.testSpecificContent(anURI, "application/rdf+xml")) {
						System.out.println("PASS");
						csv.writeValueUnquoted("true");
					} else {
						System.out.println("FAIL");
						csv.writeValueUnquoted("false");
					}
				} catch (Exception e) {
					System.out.println("FAIL - " + e.getMessage());
					csv.writeValueUnquoted("false");
				}
			} else {
				System.out.println("NC");
				csv.writeValueQuoted("unavailable");
			}

			System.out.print("the resources and the endpoint are on the same domain:\t***********\t\t");
			try {
				if (anURI.startsWith(endpoint.replaceAll("(http://[^/:]+).*", "$1"))) {
					System.out.println("PASS");
					csv.writeValueUnquoted("true");
				} else {
					System.out.println("FAIL");
					csv.writeValueUnquoted("false");
				}
			} catch (Exception e) {
				System.out.println("FAIL - " + e.getMessage());
				csv.writeValueUnquoted("false");
			}
			return true;
		} else {
			return false;
		}
	}
}
