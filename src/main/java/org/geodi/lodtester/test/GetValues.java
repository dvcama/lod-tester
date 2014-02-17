package org.geodi.lodtester.test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.geodi.lodtester.DefaultParamsProvider;

public class GetValues {

	public static String pickAnUri(String endpointUrl) throws UnsupportedEncodingException {
		String uri = "";
		HttpClient client = new HttpClient();
		GetMethod method = new GetMethod(endpointUrl);
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
		method.addRequestHeader("Accept", "application/sparql-results+json");
		try {
			int statusCode = client.executeMethod(method);
			if (statusCode != HttpStatus.SC_OK) {
				// System.out.println("Method failed: " +
				// method.getStatusLine());
				// System.out.println(endpointUrl);
				return uri;
			}
			// System.out.println(method.getResponseBodyAsString());
			ObjectMapper m = new ObjectMapper();
			StringWriter writer = new StringWriter();
			IOUtils.copy(method.getResponseBodyAsStream(), writer, "UTF-8");
			String responseString = writer.toString();
			JsonNode rootNode = m.readTree(responseString);
			uri = rootNode.findPath("s").findPath("value").getTextValue();
		} catch (Exception e) {
			// e.printStackTrace();
		} finally {
			// Release the connection.
			method.releaseConnection();
		}

		return uri;

	}

	public static String getTot(String endpointUrl) {
		HttpClient client = new HttpClient();
		GetMethod method = new GetMethod(endpointUrl);
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
		method.addRequestHeader("Accept", "application/sparql-results+json");
		int statusCode = 0;
		String responseString = "";
		try {
			statusCode = client.executeMethod(method);
			// System.out.println(method.getResponseBodyAsString());
			ObjectMapper m = new ObjectMapper();
			StringWriter writer = new StringWriter();
			IOUtils.copy(method.getResponseBodyAsStream(), writer, "UTF-8");
			responseString = writer.toString();
			JsonNode rootNode = m.readTree(responseString);

			return rootNode.findPath("no").findPath("value").getTextValue();
		} catch (Exception e) {
			System.out.println("statusCode: " + statusCode + " | " + responseString);
		} finally {
			// Release the connection.
			method.releaseConnection();
		}

		return "0";

	}

	public static List<String> getObjPropList(Map.Entry<String, String> thisEndpoint) throws UnsupportedEncodingException {
		List<String> result = new ArrayList<String>();
		String query = DefaultParamsProvider.objPropQuery;
		String domain = thisEndpoint.getKey().replaceAll("(http://[^/:]+).+", "$1");
		System.out.println("\t\tdomain " + domain);
		query = query.replaceAll("\\$\\{domain\\}", domain);
		if (thisEndpoint.getValue().equals("")) {
			query = query.replaceAll("\\$\\{graph\\}", "");
		} else {
			query = query.replaceAll("\\$\\{graph\\}", " FROM <" + thisEndpoint.getValue() + ">");
		}
		HttpClient client = new HttpClient();
		GetMethod method = new GetMethod(thisEndpoint.getKey() + "?query=" + URLEncoder.encode(query, "UTF-8"));
		method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
		method.addRequestHeader("Accept", "application/sparql-results+json");
		int statusCode = 0;
		String responseString = "";
		try {
			statusCode = client.executeMethod(method);
			// System.out.println(method.getResponseBodyAsString());
			ObjectMapper m = new ObjectMapper();
			StringWriter writer = new StringWriter();
			IOUtils.copy(method.getResponseBodyAsStream(), writer, "UTF-8");
			responseString = writer.toString();
			JsonNode rootNode = m.readTree(responseString);
			// System.out.println(responseString);
			for (JsonNode jsonNode : rootNode.findPath("bindings").findValues("value")) {
				// System.out.println(jsonNode);
				result.add(jsonNode.getTextValue());
			}
		} catch (Exception e) {
			System.out.println("statusCode: " + statusCode + " | " + responseString);
		} finally {
			// Release the connection.
			method.releaseConnection();
		}

		return result;
	}

	public static List<String> findConnections(Map<String, String> endpoints, ArrayList<String> testDomains, Map.Entry<String, String> thisEndpoint) throws UnsupportedEncodingException {
		List<String> result = new ArrayList<String>();
		List<String> prop = GetValues.getObjPropList(thisEndpoint);
		List<String> skipProp = DefaultParamsProvider.skipProps();

		for (String aProp : prop) {

			if (skipProp.contains(aProp) || skipProp.contains(aProp.replaceAll("(http://[^/:]+).+", "$1"))) {
				continue;
			}
			for (String endpoint : endpoints.keySet()) {
				if (!endpoint.equals(thisEndpoint.getKey())) {
					String domain = endpoint.replaceAll("(http://[^/:]+).+", "$1");
					String query = DefaultParamsProvider.connectionsQuery.replaceAll("\\$\\{domain\\}", domain);
					query = query.replaceAll("\\$\\{prop\\}", aProp);
					if (thisEndpoint.getValue().equals("")) {
						query = query.replaceAll("\\$\\{graph\\}", "");
					} else {
						query = query.replaceAll("\\$\\{graph\\}", " FROM <" + thisEndpoint.getValue() + ">");
					}
					String tot = getTot(thisEndpoint.getKey() + "?query=" + URLEncoder.encode(query, "UTF-8"));
					if (!tot.equals("0")) {
						System.out.println("\t\tquery " + query);
						System.out.println(tot);
					}

				}
			}
			for (String domain : testDomains) {
				String query = DefaultParamsProvider.connectionsQuery.replaceAll("\\$\\{domain\\}", domain);
				query = query.replaceAll("\\$\\{prop\\}", aProp);
				query = query.replaceAll("\\$\\{graph\\}", "");
				String tot = getTot(thisEndpoint.getKey() + "?query=" + URLEncoder.encode(query, "UTF-8"));
				if (!tot.equals("0")) {
					System.out.println("\t\tquery " + query);
					System.out.println(tot);
				}
			}

		}
		return result;
	}
}
